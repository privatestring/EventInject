# KSP ServiceAggregator 设计方案

## 1. 背景与目标

### 现状

项目使用 Java SPI (`ServiceLoader`) 机制实现模块间服务发现：

| SPI 文件 | 接口 | 注册数量 | 查找方式 |
|----------|------|---------|---------|
| `META-INF/services/...IViewProvider` | `IViewProvider` | 90+ | 按 `key: String` 匹配 |
| `META-INF/services/...IFragmentProvider` | `IFragmentProvider` | — | 按 `key: String` 匹配 |
| `META-INF/services/...IService` | `IService` | 35+ | 按子接口 `Class.isAssignableFrom()` 匹配 |

当前注册方式：`@AutoService(IViewProvider::class)` → auto-service 生成 SPI 文件 → 运行时 `ServiceLoader` 反射加载。

### 痛点

1. `ServiceLoader` 运行时逐个反射实例化 130+ 个类，首次加载有性能开销
2. 无法编译期校验（如 key 重复只能运行时发现）
3. 所有实例在 ServiceLoader 遍历时就被创建，无法按需实例化

### 目标

- KSP 替代 auto-service，编译期生成模块级聚合类
- 每个模块一个聚合类，内部直接 `new`，消除逐个反射
- 支持多聚合接口拆分，支持后续扩展新 service 类型
- 模块无注册内容时不生成任何文件

---

## 2. 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                        编译期 (KSP)                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  MarketModule                    TickerModule               │
│  ┌─────────────────────┐        ┌─────────────────────┐    │
│  │ @ServiceRegistry    │        │ @ServiceRegistry    │    │
│  │ class A: IViewProv  │        │ class D: IViewProv  │    │
│  │ class B: IService   │        │ class E: IService   │    │
│  └────────┬────────────┘        └────────┬────────────┘    │
│           ▼                               ▼                 │
│  ┌─────────────────────┐        ┌─────────────────────┐    │
│  │ MarketModule_       │        │ TickerModule_       │    │
│  │ ServiceAggregator   │        │ ServiceAggregator   │    │
│  │ + SPI 注册文件       │        │ + SPI 注册文件       │    │
│  └─────────────────────┘        └─────────────────────┘    │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│                        运行时                                │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ServiceLoader.load(IViewAggregator::class)                 │
│  ServiceLoader.load(IServiceAggregator::class)              │
│       ▼                                                     │
│  flatMap { it.provideViewProviders() }  → allViewProviders  │
│  flatMap { it.provideServices() }       → allServices       │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. 模块结构

```
launcher-joke/                              ← 注解模块
  src/main/java/wb/service/
    ServiceRegistry.kt                      ← @ServiceRegistry
    ServiceGroup.kt                         ← @ServiceGroup

launcher-compiler-wb-ksp/                   ← KSP Processor
  src/main/kotlin/launcher/wb/
    WbKspProcessor.kt                       ← generations 列表注册
    codegeneration/
      BaseGeneration.kt                     ← 公共 extractModuleName/toPascalCase
      ServiceAggregatorGeneration.kt        ← 聚合器代码生成

base-component/CommonModule/                ← 聚合器接口（运行时引用）
  IViewAggregator.kt
  IServiceAggregator.kt
```

---

## 4. 注解定义

### 4.1 `@ServiceRegistry`

```kotlin
package wb.service

import kotlin.reflect.KClass

/**
 * 标记一个类注册到 ServiceAggregator。
 * 迁移：@AutoService(X::class) → @ServiceRegistry(X::class)
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ServiceRegistry(val value: KClass<*>)
```

### 4.2 `@ServiceGroup`

```kotlin
package wb.service

import kotlin.reflect.KClass

/**
 * 标记聚合接口方法对应哪个 SPI 接口组。
 * Processor 扫描此注解动态建立映射，新增类型无需改 Processor。
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ServiceGroup(val value: KClass<*>)
```

---

## 5. 聚合器接口

支持多接口拆分，Processor 自动发现所有带 `@ServiceGroup` 的接口：

```kotlin
interface IViewAggregator {
    @ServiceGroup(IViewProvider::class)
    fun provideViewProviders(): List<IViewProvider> = emptyList()

    @ServiceGroup(IFragmentProvider::class)
    fun provideFragmentProviders(): List<IFragmentProvider> = emptyList()
}

interface IServiceAggregator {
    @ServiceGroup(IService::class)
    fun provideServices(): List<IService> = emptyList()

    @ServiceGroup(AbTestProvider::class)
    fun provideAbTestProviders(): List<AbTestProvider> = emptyList()
}
```

也可合并为单个接口，Processor 同样支持。

**带参数的聚合方法**：

```kotlin
@ServiceGroup(IViewProvider::class)
fun provideViewProviders(context: Context): List<IViewProvider> = emptyList()
```

约定：所有非 object 实现类的主构造函数必须与聚合方法参数完全匹配，否则编译报错。

---

## 6. KSP Processor 实现

### 核心设计

- **零硬编码**：注解名通过 `::class.qualifiedName` 获取，聚合接口通过 `@ServiceGroup` 所在 `parentDeclaration` 动态发现
- **多接口支持**：生成的聚合类实现所有发现的聚合接口
- **排序稳定**：接口、方法、实现类均按全限定名排序
- **模块名**：优先 `ksp { arg("module_name", "xxx") }`，fallback 从路径自动提取 PascalCase
- **参数透传**：聚合方法有参数时，自动复制签名并传入构造函数，编译期校验构造函数匹配
- **object 处理**：`ClassKind.OBJECT` 直接引用不加 `()`，不传参

### 发现映射的方式

由于 KSP 的 `getSymbolsWithAnnotation` 对接口默认方法上的注解支持不稳定，采用遍历源文件方式：

```kotlin
for (file in resolver.getAllFiles()) {
    for (decl in file.declarations) {
        if (decl is KSClassDeclaration && decl.classKind == ClassKind.INTERFACE) {
            for (func in decl.getDeclaredFunctions()) {
                // 查找 @ServiceGroup 注解
            }
        }
    }
}
```

### 注册到 WbKspProcessor

```kotlin
private val generations: List<BaseGeneration> by lazy {
    listOf(
        // ... 其他 Generation
        ServiceAggregatorGeneration(codeGenerator, logger, options),
    )
}
```

---

## 7. 生成产物示例

```kotlin
// com/webull/service/App_ServiceAggregator.kt
package com.webull.service

class App_ServiceAggregator : IServiceAggregator, IViewAggregator {

    override fun provideViewProviders() = listOf(
        EconomicEventViewProvider(),
        HotSearchRankingCardViewProvider(),
        MarketBannerViewProvider  // object
    )

    override fun provideFragmentProviders() = listOf(
        MarketHomeFragmentProvider(),
        TickerNewsFragmentProvider()
    )

    override fun provideServices() = listOf(
        AppInfoService,  // object
        BondService(),
        RankService()
    )

    override fun provideAbTestProviders() = listOf(
        MarketABTestProvider(),
        TickerABTestProvider()
    )
}
```

SPI 文件：
- `META-INF/services/com.joker.event.service.IViewAggregator`
- `META-INF/services/com.joker.event.service.IServiceAggregator`

---

## 8. Gradle 配置

```kotlin
// 业务模块 build.gradle（无需额外配置，已有 ksp 依赖即可）
ksp {
    arg("module_name", project.name)  // 可选，不配则自动从路径提取
}
```

依赖关系：
```
业务模块 → CommonModule（运行时引用聚合接口）
业务模块 → launcher-joke（编译时引用 @ServiceRegistry）
业务模块 ksp→ launcher-compiler-wb-ksp（KSP 处理）
```

---

## 9. 运行时改造（兼容模式）

**策略：KSP 聚合器优先，ServiceLoader fallback。**

```kotlin
object ServiceAggregatorHolder {
    val viewAggregators: List<IViewAggregator> by lazy {
        ServiceLoader.load(IViewAggregator::class.java, ...).toList()
    }
    val serviceAggregators: List<IServiceAggregator> by lazy {
        ServiceLoader.load(IServiceAggregator::class.java, ...).toList()
    }
}
```

查找逻辑：
1. 先从 KSP 聚合器列表查找
2. 找不到 → fallback 到原 ServiceLoader
3. 全部迁移完成后移除 fallback

---

## 10. 迁移步骤

| Phase | 内容 | 状态 |
|-------|------|------|
| 1 | 注解 + Processor + BaseGeneration 公共方法 | ✅ 已完成 |
| 2 | 业务模块 `@AutoService` → `@ServiceRegistry` | 待执行 |
| 3 | 运行时改造（KSP 优先 + legacy fallback） | 待执行 |
| 4 | 清理 auto-service 依赖和旧 SPI 文件 | 待执行 |

---

## 11. 扩展新 service 类型

只需两步，Processor 无需改动：

1. 聚合接口新增方法：`@ServiceGroup(IJumpStrategy::class) fun provideJumpStrategies(): List<IJumpStrategy> = emptyList()`
2. 业务模块实现类加 `@ServiceRegistry(IJumpStrategy::class)`

---

## 12. 关键设计决策

| 决策 | 选择 | 原因 |
|------|------|------|
| 注解包名 | `wb.service` | 与现有 `wb.*` 注解统一 |
| 注解命名 | `@ServiceRegistry` + `@ServiceGroup` | 简洁，语义清晰 |
| 映射发现 | 遍历源文件 `getDeclaredFunctions()` | `getSymbolsWithAnnotation` 对接口默认方法不稳定 |
| 多接口支持 | 聚合类实现所有发现的接口 | 灵活拆分，按职责分离 |
| 排序稳定 | priority 降序 + 全限定名升序 | 高优先级在前，同优先级稳定 |
| 模块名 | 优先 ksp arg，fallback 路径提取 | 灵活 + 零配置 |
| 参数透传 | 聚合方法有参 → 构造函数必须匹配 | 编译期校验，运行时安全 |
| object | 直接引用不加 `()` | 保持单例语义 |
| `@ServiceGroup` Retention | `RUNTIME` | 确保 KSP 能发现 |
| key 重复检测 | JSON 元数据 + Gradle Task | 跨模块全局校验 |
| 统计报告 | warn 日志 + txt 文件 | 编译可见 + CI 可收集 |

---

## 13. 性能对比

| 指标 | 改造前 | 改造后 |
|------|--------|--------|
| ServiceLoader 加载次数 | 3 次 | 1-2 次（按聚合接口数） |
| 反射实例化数量 | 130+ 个类 | ~15 个聚合类 |
| 实例创建方式 | ServiceLoader 反射 | 聚合类内直接 new |
| 首次加载耗时 | 较高 | 显著降低 |

---

## 14. 优先级排序（priority）

### 14.1 注解参数

```kotlin
@ServiceRegistry(
    value: KClass<*>,
    priority: Int = 0  // 数值越大优先级越高，默认 0
)
```

### 14.2 排序规则

生成的 `listOf(...)` 中实现类按以下规则排列：
1. **priority 降序**：数值越大越靠前
2. **同优先级按类全限定名升序**：保证增量编译稳定

### 14.3 使用场景

| 场景 | 示例 |
|------|------|
| View 展示顺序 | Banner(200) > 热搜(100) > 普通卡片(0) |
| AB 测试覆盖 | 新版 Provider(100) 优先于旧版(0) |
| 服务初始化顺序 | 核心服务(100) 先于辅助服务(0) |

### 14.4 示例

```kotlin
@ServiceRegistry(IViewProvider::class, priority = 200)
object MarketBannerViewProvider : IViewProvider { ... }

@ServiceRegistry(IViewProvider::class, priority = 100)
class EconomicEventViewProvider : IViewProvider { ... }

@ServiceRegistry(IViewProvider::class)  // priority = 0
class HotSearchProvider : IViewProvider { ... }
```

生成代码：
```kotlin
override fun provideViewProviders() = listOf(
    MarketBannerViewProvider,       // priority=200
    EconomicEventViewProvider(),    // priority=100
    HotSearchProvider()             // priority=0
)
```

---

## 15. 跨模块 Key 重复检测

### 15.1 方案概述

每个模块 KSP 生成 JSON 元数据文件 → App 模块 Gradle Task 汇总 → 检测 key 冲突。

详细设计见：[service-registry-validation.md](./service-registry-validation.md)

### 15.2 元数据文件

路径：`META-INF/service-registry/{ModuleName}.json`

```json
{
  "module": "MarketModule",
  "registrations": [
    {"class": "com.webull.market.BannerProvider", "interface": "IViewProvider", "priority": 100, "key": "market_banner"}
  ]
}
```

### 15.3 校验规则

| 规则 | 级别 |
|------|------|
| 同 interface 下 key 重复 | ERROR |
| 同 class 重复注册 | ERROR |
| 同 class 多模块出现 | WARNING |

---

## 16. 编译期统计报告

### 16.1 输出形式

1. **KSP warn 日志**（编译时控制台可见）
2. **报告文件** `META-INF/service-registry/{Module}_report.txt`（CI 可收集）

### 16.2 报告内容

```
[ServiceAggregator] ═══ Module: MarketModule ═══
  IViewProvider       : 12 registrations (2 objects, 10 classes, 3 with priority)
  IFragmentProvider   : 5 registrations (0 objects, 5 classes)
  IService            : 8 registrations (1 object, 7 classes, 2 with priority)
  AbTestProvider      : 3 registrations (0 objects, 3 classes)
  ──────────────────────────────────────────────────────
  Total: 28 registrations (3 objects, 25 classes)
  Aggregator interfaces: 2
```

### 16.3 用途

- 开发者了解各模块注册量分布
- CI 监控注册数增长趋势
- 性能分析时定位重量级模块

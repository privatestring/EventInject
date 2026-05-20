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
- 支持懒加载（按需实例化），通过返回类型自动推断
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
│  flatMap { it.provideServiceEntries() } → allServiceEntries │
│       ▼                                                     │
│  entries.first { it.isType(T::class) }.instance  ← 按需创建 │
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
    ServiceEntry.kt                         ← ServiceEntry<T>（懒加载包装）

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
 *
 * @param value 指定归入哪个 SPI 接口
 * @param priority 优先级，数值越大越靠前。默认 0，同优先级按类名字母序排列。
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ServiceRegistry(
    val value: KClass<*>,
    val priority: Int = 0
)
```

### 4.2 `@ServiceGroup`

```kotlin
package wb.service

import kotlin.reflect.KClass

/**
 * 标记聚合接口方法对应哪个 SPI 接口组。
 * Processor 扫描此注解动态建立映射，新增类型无需改 Processor。
 *
 * 懒加载判断：Processor 通过方法返回类型自动推断：
 * - 返回 List<X> → eager 模式，直接实例化
 * - 返回 List<ServiceEntry<X>> → lazy 模式，生成工厂
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ServiceGroup(val value: KClass<*>)
```

### 4.3 `ServiceEntry<T>`

```kotlin
package wb.service

/**
 * 服务注册条目，支持按需实例化。
 * 当聚合方法返回 List<ServiceEntry<X>> 时，KSP 自动生成工厂模式。
 *
 * @param implClass 实现类的 Class，用于类型匹配（如 isAssignableFrom）
 * @param factory 延迟创建工厂，首次访问 instance 时调用
 */
class ServiceEntry<T>(
    val implClass: Class<out T>,
    private val factory: () -> T
) {
    /** 首次访问时创建实例，后续复用（线程安全） */
    val instance: T by lazy { factory() }

    /** 判断实现类是否为指定类型（支持子接口匹配） */
    fun isType(targetClass: Class<*>): Boolean =
        targetClass.isAssignableFrom(implClass)
}
```

---

## 5. 聚合器接口

支持多接口拆分，Processor 自动发现所有带 `@ServiceGroup` 的接口。

**通过返回类型控制生成模式**：返回 `List<X>` 为 eager，返回 `List<ServiceEntry<X>>` 为 lazy。

```kotlin
interface IViewAggregator {
    // eager：返回 List<IViewProvider>，直接实例化
    @ServiceGroup(IViewProvider::class)
    fun provideViewProviders(): List<IViewProvider> = emptyList()

    @ServiceGroup(IFragmentProvider::class)
    fun provideFragmentProviders(): List<IFragmentProvider> = emptyList()
}

interface IServiceAggregator {
    // lazy：返回 List<ServiceEntry<IService>>，按需实例化
    @ServiceGroup(IService::class)
    fun provideServiceEntries(context: Context): List<ServiceEntry<IService>> = emptyList()

    // eager
    @ServiceGroup(AbTestProvider::class)
    fun provideAbTestProviders(): List<AbTestProvider> = emptyList()
}
```

**带参数的聚合方法**：

```kotlin
@ServiceGroup(IService::class)
fun provideServiceEntries(context: Context): List<ServiceEntry<IService>> = emptyList()
```

约定：所有非 object 实现类的主构造函数必须与聚合方法参数完全匹配，否则编译报错。

---

## 6. KSP Processor 实现

### 核心设计

- **零硬编码**：注解名、ServiceEntry 类名均通过 `::class.qualifiedName` 获取
- **返回类型推断**：检测方法返回类型是否为 `List<ServiceEntry<X>>`，自动决定生成模式
- **多接口支持**：生成的聚合类实现所有发现的聚合接口
- **排序稳定**：priority 降序 + 全限定名升序
- **模块名**：优先 `ksp { arg("module_name", "xxx") }`，fallback 从路径自动提取 PascalCase
- **参数透传**：聚合方法有参数时，自动复制签名并传入构造函数/工厂 lambda
- **object 处理**：`ClassKind.OBJECT` 直接引用不加 `()`，不传参

### 懒加载检测逻辑

```kotlin
private fun isServiceEntryReturnType(returnType: KSType?): Boolean {
    if (returnType == null) return false
    val decl = returnType.declaration.qualifiedName?.asString()
    if (decl != "kotlin.collections.List") return false
    val typeArg = returnType.arguments.firstOrNull()?.type?.resolve() ?: return false
    return typeArg.declaration.qualifiedName?.asString() == ServiceEntry::class.qualifiedName
}
```

### 发现映射的方式

由于 KSP 的 `getSymbolsWithAnnotation` 对接口默认方法上的注解支持不稳定，采用遍历源文件方式：

```kotlin
for (file in resolver.getAllFiles()) {
    for (decl in file.declarations) {
        if (decl is KSClassDeclaration && decl.classKind == ClassKind.INTERFACE) {
            for (func in decl.getDeclaredFunctions()) {
                // 查找 @ServiceGroup 注解 + 检测返回类型
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

### eager 模式（返回 `List<X>`）

```kotlin
override fun provideViewProviders(): List<IViewProvider> = listOf(
    MarketBannerViewProvider,           // priority=200, object
    AlertCardViewProvider(),            // priority=100
    EconomicEventViewProvider(),        // priority=100
    HotSearchRankingCardViewProvider()  // priority=0
)
```

### lazy 模式（返回 `List<ServiceEntry<X>>`）

```kotlin
override fun provideServiceEntries(context: Context): List<ServiceEntry<IService>> = listOf(
    ServiceEntry(AppInfoService::class.java) { AppInfoService(context) },
    ServiceEntry(BondService::class.java) { BondService(context) },
    ServiceEntry(RankService::class.java) { RankService(context) }
)
```

### 完整生成文件

```kotlin
// com/webull/service/MarketModule_ServiceAggregator.kt
package com.webull.service

class MarketModule_ServiceAggregator : IServiceAggregator, IViewAggregator {

    override fun provideViewProviders(): List<IViewProvider> = listOf(
        MarketBannerViewProvider,
        AlertCardViewProvider(),
        EconomicEventViewProvider(),
        HotSearchRankingCardViewProvider()
    )

    override fun provideFragmentProviders(): List<IFragmentProvider> = listOf(
        MarketHomeFragmentProvider(),
        TickerNewsFragmentProvider()
    )

    override fun provideServiceEntries(context: Context): List<ServiceEntry<IService>> = listOf(
        ServiceEntry(AppInfoService::class.java) { AppInfoService(context) },
        ServiceEntry(BondService::class.java) { BondService(context) },
        ServiceEntry(RankService::class.java) { RankService(context) }
    )

    override fun provideAbTestProviders(): List<AbTestProvider> = listOf(
        MarketABTestProvider(),
        TickerABTestProvider()
    )
}
```

SPI 文件：
- `META-INF/services/com.webull.xxx.IViewAggregator`
- `META-INF/services/com.webull.xxx.IServiceAggregator`

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
业务模块 → launcher-joke（编译时引用 @ServiceRegistry + ServiceEntry）
业务模块 ksp→ launcher-compiler-wb-ksp（KSP 处理）
```

---

## 9. 运行时使用

### eager 模式查找

```kotlin
// 按 key 查找 ViewProvider
val provider = viewAggregators
    .flatMap { it.provideViewProviders() }
    .firstOrNull { it.key == "market_banner" }
```

### lazy 模式查找（按需实例化）

```kotlin
// 按子接口类型查找，只实例化匹配的那一个
inline fun <reified T : IService> findService(context: Context): T? {
    return serviceAggregators
        .flatMap { it.provideServiceEntries(context) }
        .firstOrNull { it.isType(T::class.java) }
        ?.instance as? T
}

// 使用
val bondService = findService<IBondService>(context)
```

### 兼容模式

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

---

## 10. 迁移步骤

| Phase | 内容 | 状态 |
|-------|------|------|
| 1 | 注解 + ServiceEntry + Processor | ✅ 已完成 |
| 2 | 业务模块 `@AutoService` → `@ServiceRegistry` | 待执行 |
| 3 | 运行时改造（KSP 优先 + legacy fallback） | 待执行 |
| 4 | 清理 auto-service 依赖和旧 SPI 文件 | 待执行 |

---

## 11. 扩展新 service 类型

只需两步，Processor 无需改动：

1. 聚合接口新增方法（通过返回类型决定 eager/lazy）：
   - eager: `@ServiceGroup(IJumpStrategy::class) fun provideJumpStrategies(): List<IJumpStrategy> = emptyList()`
   - lazy: `@ServiceGroup(IJumpStrategy::class) fun provideJumpStrategyEntries(): List<ServiceEntry<IJumpStrategy>> = emptyList()`
2. 业务模块实现类加 `@ServiceRegistry(IJumpStrategy::class)`

---

## 12. 关键设计决策

| 决策 | 选择 | 原因 |
|------|------|------|
| 注解包名 | `wb.service` | 与现有 `wb.*` 注解统一 |
| 注解命名 | `@ServiceRegistry` + `@ServiceGroup` | 简洁，语义清晰 |
| 懒加载判断 | 方法返回类型推断 | 无需额外注解参数，声明即契约 |
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

| 指标 | 改造前 | 改造后（eager） | 改造后（lazy） |
|------|--------|----------------|----------------|
| ServiceLoader 加载次数 | 3 次 | 1-2 次 | 1-2 次 |
| 反射实例化数量 | 130+ 个类 | ~15 个聚合类 | ~15 个聚合类 |
| 实例创建方式 | ServiceLoader 反射 | 聚合类内直接 new | 首次访问时 new |
| 查找 1 个 IService | 实例化 35 个 | 实例化 35 个 | 实例化 1 个 |
| 内存占用（首次查找） | 35 个对象 | 35 个对象 | 1 个对象 + 35 个 lambda |

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

## 15. 懒加载 / 按需实例化

### 15.1 设计原则

**声明即契约**：不引入额外注解参数，Processor 通过聚合方法的返回类型自动判断生成模式。

| 返回类型 | 生成模式 | 适用场景 |
|----------|----------|----------|
| `List<X>` | eager — 直接 new | 需要遍历全部（IViewProvider 按 key 查找） |
| `List<ServiceEntry<X>>` | lazy — 工厂 lambda | 按类型查找单个（IService 按子接口匹配） |

### 15.2 ServiceEntry 使用

```kotlin
// 按子接口类型查找，只实例化匹配的那一个
val bondService = serviceEntries
    .firstOrNull { it.isType(IBondService::class.java) }
    ?.instance as? IBondService

// ServiceEntry.instance 是 by lazy，首次访问创建，后续复用
val same = entry.instance === entry.instance  // true
```

### 15.3 适用范围

| 接口 | 推荐模式 | 原因 |
|------|----------|------|
| IService | lazy | 按类型查找，通常只需 1 个 |
| IViewProvider | eager | 需要遍历全部按 key 查找 |
| IFragmentProvider | eager | 同上 |
| AbTestProvider | eager | 需要收集所有 keys |

### 15.4 object 在 lazy 模式下的处理

object 类型在 lazy 模式下同样包装为 `ServiceEntry`，但工厂直接引用单例：

```kotlin
ServiceEntry(AppInfoService::class.java) { AppInfoService }  // object 直接引用
```

---

## 16. 跨模块 Key 重复检测

### 16.1 方案概述

每个模块 KSP 生成 JSON 元数据文件 → App 模块 Gradle Task 汇总 → 检测 key 冲突。

详细设计见：[service-registry-validation.md](./service-registry-validation.md)

### 16.2 元数据文件

路径：`META-INF/service-registry/{ModuleName}.json`

```json
{
  "module": "MarketModule",
  "registrations": [
    {"class": "com.webull.market.BannerProvider", "interface": "IViewProvider", "priority": 100}
  ]
}
```

### 16.3 校验规则

| 规则 | 级别 |
|------|------|
| 同 interface 下 key 重复 | ERROR |
| 同 class 重复注册 | ERROR |
| 同 class 多模块出现 | WARNING |

---

## 17. 编译期统计报告

### 17.1 输出形式

1. **KSP warn 日志**（编译时控制台可见）
2. **报告文件** `META-INF/service-registry/{Module}_report.txt`（CI 可收集）

### 17.2 报告内容

```
[ServiceAggregator] ═══ Module: MarketModule ═══
  IViewProvider       : 12 registrations (2 objects, 10 classes, 3 with priority)
  IFragmentProvider   : 5 registrations (0 objects, 5 classes)
  IService            : 8 registrations (1 object, 7 classes, lazy)
  AbTestProvider      : 3 registrations (0 objects, 3 classes)
  ──────────────────────────────────────────────────────
  Total: 28 registrations (3 objects, 25 classes)
  Aggregator interfaces: 2
```

### 17.3 用途

- 开发者了解各模块注册量分布
- CI 监控注册数增长趋势
- 性能分析时定位重量级模块

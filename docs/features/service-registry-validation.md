# 跨模块 ServiceRegistry 校验方案

## 1. 背景

单模块内的 key 重复可以在 KSP 阶段检测，但跨模块的 key 冲突（如 MarketModule 和 TickerModule 都注册了 `key = "ticker_chart"`）只有运行时才能发现。

本方案通过 **KSP 生成元数据 + Gradle Task 汇总校验** 实现编译期跨模块检测。

---

## 2. 架构

```
┌─────────────────────────────────────────────────────────────┐
│                     KSP 阶段（各模块独立）                     │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  MarketModule KSP                  TickerModule KSP         │
│  ┌───────────────────┐            ┌───────────────────┐    │
│  │ 生成:              │            │ 生成:              │    │
│  │ MarketModule.json  │            │ TickerModule.json  │    │
│  └───────────────────┘            └───────────────────┘    │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│                     Gradle 阶段（App 模块）                   │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  validateServiceRegistry Task                               │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ 1. 收集所有 META-INF/service-registry/*.json         │   │
│  │ 2. 按 interface 分组                                 │   │
│  │ 3. 检测 key 重复 → 报错                              │   │
│  │ 4. 检测 class 重复注册 → 警告                         │   │
│  │ 5. 输出全局统计报告                                   │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. 元数据 JSON 格式

每个模块 KSP 生成 `META-INF/service-registry/{ModuleName}.json`：

```json
{
  "module": "MarketModule",
  "registrations": [
    {
      "class": "com.webull.market.HotSearchProvider",
      "interface": "IViewProvider",
      "priority": 0,
      "key": "hot_search"
    },
    {
      "class": "com.webull.market.BannerProvider",
      "interface": "IViewProvider",
      "priority": 100,
      "key": "market_banner"
    },
    {
      "class": "com.webull.market.MarketService",
      "interface": "IService",
      "priority": 0
    }
  ]
}
```

**字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| `class` | String | 实现类全限定名 |
| `interface` | String | 注册的 SPI 接口短名 |
| `priority` | Int | 优先级 |
| `key` | String? | 可选，仅 IViewProvider/IFragmentProvider 等有 key 属性的接口 |

---

## 4. Key 提取策略

KSP 无法直接读取属性的运行时初始值，提供两种方案：

### 方案 A：@ServiceKey 注解（推荐）

```kotlin
@ServiceRegistry(IViewProvider::class)
@ServiceKey("hot_search")
class HotSearchProvider : IViewProvider {
    override val key = "hot_search"
}
```

优点：编译期 100% 可靠提取。
缺点：key 写两遍（注解 + 属性），但可通过 lint 检查一致性。

### 方案 B：源码文本匹配（当前实现）

通过正则匹配 `val key.*=.*"(.*)"` 提取。
局限：无法处理变量引用、常量引用、计算表达式等复杂场景。

### 当前策略

先实现 JSON 框架，`key` 字段暂时为 null。后续引入 `@ServiceKey` 注解后补全。
即使 key 为 null，仍可检测：
- 同一个 class 被重复注册到同一个 interface
- 同一个 class 在多个模块中出现

---

## 5. Gradle 校验 Task

### 5.1 注册方式

在 App 模块的 `build.gradle` 中：

```kotlin
// 方式一：独立 Plugin
plugins {
    id("com.webull.service-registry-validation")
}

// 方式二：直接在 build.gradle 中注册 Task（轻量方案）
tasks.register("validateServiceRegistry") {
    dependsOn("mergeDebugJavaResource")  // 确保 JSON 已合并
    doLast {
        val metaDir = file("build/intermediates/merged_java_res/debug/META-INF/service-registry")
        if (!metaDir.exists()) return@doLast

        val allRegistrations = mutableListOf<Map<String, Any?>>()
        metaDir.listFiles { f -> f.extension == "json" }?.forEach { jsonFile ->
            // 解析 JSON，汇总 registrations
        }

        // 按 interface 分组，检测 key 重复
        val keyGroups = allRegistrations
            .filter { it["key"] != null }
            .groupBy { "${it["interface"]}:${it["key"]}" }

        val duplicates = keyGroups.filter { it.value.size > 1 }
        if (duplicates.isNotEmpty()) {
            val msg = buildString {
                appendLine("ServiceRegistry key conflict detected:")
                duplicates.forEach { (key, regs) ->
                    appendLine("  $key registered by:")
                    regs.forEach { appendLine("    - ${it["class"]} (module: ${it["module"]})") }
                }
            }
            throw GradleException(msg)
        }
    }
}
tasks.named("assembleDebug") { dependsOn("validateServiceRegistry") }
```

### 5.2 校验规则

| 规则 | 级别 | 说明 |
|------|------|------|
| 同 interface 下 key 重复 | ERROR | 阻断编译 |
| 同 class 注册到同 interface 多次 | ERROR | 阻断编译 |
| 同 class 在多模块出现 | WARNING | 可能是模块拆分遗留 |
| 总注册数超过阈值 | INFO | 性能提醒 |

---

## 6. 统计报告

### 模块级报告（KSP 生成）

每个模块生成 `META-INF/service-registry/{Module}_report.txt`：

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

### 全局报告（Gradle Task 生成）

Gradle 校验 Task 汇总后输出：

```
[ServiceRegistry Global Report]
═══════════════════════════════════════════════════
Modules: 8
Total registrations: 156

By interface:
  IViewProvider       : 45 (across 6 modules)
  IFragmentProvider   : 22 (across 4 modules)
  IService            : 67 (across 8 modules)
  AbTestProvider      : 22 (across 5 modules)

Top modules by registration count:
  1. MarketModule     : 35
  2. TickerModule     : 28
  3. TradeModule      : 24
  ...

Priority usage: 12 registrations with non-zero priority
═══════════════════════════════════════════════════
```

---

## 7. 迁移计划

| Phase | 内容 | 依赖 |
|-------|------|------|
| 1 | KSP 生成 JSON 元数据 + 统计报告 | ✅ 本次实现 |
| 2 | App 模块添加 Gradle 校验 Task | Phase 1 |
| 3 | 引入 @ServiceKey 注解，KSP 提取 key 写入 JSON | Phase 1 |
| 4 | Gradle Task 启用 key 重复检测 | Phase 2 + 3 |

---

## 8. 注意事项

- JSON 文件放在 `META-INF/service-registry/` 下，不会与 SPI 文件冲突
- 报告文件为 `.txt` 格式，不影响运行时
- 模块无注册内容时不生成 JSON 和报告（与聚合类行为一致）
- Gradle Task 仅在 App 模块执行，不影响 library 模块编译速度

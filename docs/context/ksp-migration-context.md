# KSP 迁移上下文 — 新对话启动指南

> 本文档为新对话提供完整上下文，用于继续完成 launcher-wb-compiler-ksp 模块的后续功能迁移。

---

## 1. 项目概况

### 1.1 工作区路径

```
/Users/joker/webull/webull/inject/EventInject
```

### 1.2 项目目标

将 `launcher-compiler`（KAPT，JavaPoet 生成 Java）中的功能逐步迁移到 `launcher-wb-compiler-ksp`（KSP，KotlinPoet 生成 Kotlin），使用 `object` + `@JvmStatic`/`@JvmField` 确保 Java 互操作兼容。

### 1.3 迁移进度

| 功能 | 文档 | 状态 | 模块 |
|------|------|------|------|
| 功能一：Activity/Fragment Launcher | `docs/features/01-activity-launcher.md` | ❌ 不在此模块（已在 launcher-compiler-ksp） | `launcher-compiler-ksp` |
| 功能二：Router 路由系统 | `docs/features/02-router.md` | ❌ 不在此模块（已在 launcher-compiler-ksp） | `launcher-compiler-ksp` |
| **功能三：Function 功能地图** | `docs/features/03-function-map.md` | ✅ 已完成 | `launcher-wb-compiler-ksp` |
| **功能四：MarketViewRoute** | `docs/features/04-market-view-route.md` | 🔲 待实现 | `launcher-wb-compiler-ksp` |
| **功能五：TradeInterface** | `docs/features/05-trade-interface.md` | 🔲 待实现 | `launcher-wb-compiler-ksp` |
| **功能六：TradeServiceMaker** | `docs/features/06-trade-service-maker.md` | 🔲 待实现 | `launcher-wb-compiler-ksp` |
| **功能七：Mapper** | `docs/features/07-mapper.md` | 🔲 待实现 | `launcher-wb-compiler-ksp` |

---

## 2. 模块结构

### 2.1 launcher-wb-compiler-ksp 目录

```
launcher-wb-compiler-ksp/
├── build.gradle
└── src/main/
    ├── kotlin/launcher/wb/
    │   ├── WbKspProvider.kt              # SPI 入口
    │   ├── WbKspProcessor.kt            # 调度中心
    │   └── codegeneration/
    │       ├── BaseGeneration.kt         # 生成器抽象基类
    │       └── FunctionGeneration.kt     # 功能三（已完成）
    └── resources/META-INF/services/
        └── com.google.devtools.ksp.processing.SymbolProcessorProvider
```

### 2.2 相关模块

| 模块 | 作用 |
|------|------|
| `launcher-joke` | 注解定义（`@Function`, `@MarketViewRoute`, `@TradeInterface`, `@TradeServiceMaker`, `@Mapper` 等） |
| `launcher-compiler` | 原始 KAPT 处理器（参考实现） |
| `launcher-compiler-ksp` | 功能一/二的 KSP 版本（JavaPoet 生成 Java） |
| `launcher-wb-compiler-ksp` | 功能三~七的 KSP 版本（KotlinPoet 生成 Kotlin）← **当前工作模块** |
| `app` | 测试应用，已配置 `ksp project(':launcher-wb-compiler-ksp')` |

### 2.3 settings.gradle

```groovy
include ':app'
include ':EventInjectCompiler', ':EventAnnotation'
include ':launcher-compiler',':launcher-compiler-ksp',':launcher-wb-compiler-ksp',':launcher-joke'
rootProject.name='EventInject'
```

---

## 3. 架构设计

### 3.1 调度模式

```
WbKspProcessor（调度中心）
  ├── FunctionGeneration        ← 功能三（已完成）
  ├── MarketViewRouteGeneration ← 功能四（待实现）
  ├── TradeInterfaceGeneration  ← 功能五（待实现）
  ├── TradeServiceMakerGeneration ← 功能六（待实现）
  └── MapperGeneration          ← 功能七（待实现）
```

### 3.2 新增功能的步骤

1. 在 `codegeneration/` 下新建 `XxxGeneration.kt`，继承 `BaseGeneration`
2. 实现 `collect(resolver)` — 收集注解
3. 实现 `hasDataToGenerate()` — 判断是否有数据
4. 实现 `generate()` — 用 KotlinPoet 生成代码
5. 在 `WbKspProcessor.generations` 列表中注册

### 3.3 BaseGeneration 接口

```kotlin
abstract class BaseGeneration(
    protected val codeGenerator: CodeGenerator,
    protected val logger: KSPLogger
) {
    abstract fun collect(resolver: Resolver): List<KSAnnotated>
    abstract fun generate()
    abstract fun hasDataToGenerate(): Boolean
    protected fun writeKotlinFile(fileSpec: FileSpec, dependencies: Dependencies)
}
```

---

## 4. 依赖配置

### build.gradle

```groovy
apply plugin: 'kotlin'

dependencies {
    implementation "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version"
    implementation project(':launcher-joke')
    implementation 'com.squareup:kotlinpoet:1.18.1'
    implementation 'com.squareup:kotlinpoet-ksp:1.18.1'
    compileOnly 'com.google.devtools.ksp:symbol-processing-api:2.0.21-1.0.28'
}

kotlin { jvmToolchain(17) }
java { withSourcesJar() }
```

---

## 5. 注解定义（launcher-joke 模块）

路径：`launcher-joke/src/main/java/launcher/`

```kotlin
// Function.kt
annotation class Function(
    val functionId: String = "",
    val desc: String,
    val group: Array<String> = []
)

// MarketViewRoute.kt
annotation class MarketViewRoute(
    val key: String = "",
    val desc: String = ""
)

// TradeInterface.kt — 需要查看
// TradeServiceMaker.kt — 需要查看
// （Mapper 相关注解在 launcher-joke/src/main/java/mapper/ 目录）
```

---

## 6. 原始 KAPT 实现参考

路径：`launcher-compiler/src/main/java/launcher/codegeneration/`

| 文件 | 对应功能 |
|------|---------|
| `FunctionGeneration.kt` | 功能三（已迁移） |
| `MarketViewRouteGeneration.kt` | 功能四 |
| `TradeInterfaceGeneration.kt` | 功能五 |
| `TradeServiceAggregatorGeneration.kt` | 功能六 |
| `MapperGeneration.kt` | 功能七 |

---

## 7. 功能文档

详细业务文档位于 `docs/features/` 目录：

| 文档 | 复杂度 | 关键点 |
|------|--------|--------|
| `03-function-map.md` | ⭐ | 已完成，可作为模板参考 |
| `04-market-view-route.md` | ⭐ | 类似 Function，收集 key → 生成路由表 |
| `05-trade-interface.md` | ⭐⭐ | 涉及注解中 `Class` 值获取（KSP 中需特殊处理） |
| `06-trade-service-maker.md` | ⭐⭐⭐ | 涉及包扫描 + 继承分析 |
| `07-mapper.md` | ⭐⭐⭐⭐⭐ | 最复杂，1970 行代码生成 |

---

## 8. 编译验证命令

```bash
# 编译 KSP 处理器模块
./gradlew :launcher-wb-compiler-ksp:compileKotlin

# 运行 KSP 生成（app 模块）
./gradlew :app:kspDebugKotlin

# 清理后重新生成
rm -rf app/build/generated/ksp && ./gradlew :app:kspDebugKotlin

# 查看生成结果
cat app/build/generated/ksp/debug/kotlin/com/webull/functionmap/FunctionFactory.kt
```

工作目录：`/Users/joker/webull/webull/inject/EventInject`

---

## 9. 设计原则

1. **生成 Kotlin 代码**（KotlinPoet），使用 `object` 单例
2. **`@JvmStatic` / `@JvmField`** 确保 Java 调用方兼容
3. **优化反射**：用 Lambda 构造器 `{ XxxClass() }` 替代 `Class.newInstance()`
4. **保留兼容 API**：不删除原有方法，只新增优化方法
5. **不建议删除现有配置**：迁移指南中不建议删除混淆规则等现有配置
6. **每个功能独立**：各 Generation 之间无依赖，可独立开发和测试

---

## 10. 已完成的 FunctionGeneration 作为模板

关键模式（后续功能可参考）：

```kotlin
class XxxGeneration(
    codeGenerator: CodeGenerator,
    logger: KSPLogger
) : BaseGeneration(codeGenerator, logger) {

    private val collectedData = mutableListOf<KSClassDeclaration>()

    override fun collect(resolver: Resolver): List<KSAnnotated> {
        val unprocessed = mutableListOf<KSAnnotated>()
        resolver.getSymbolsWithAnnotation("launcher.XxxAnnotation").forEach { symbol ->
            if (!symbol.validate()) { unprocessed += symbol; return@forEach }
            if (symbol is KSClassDeclaration) collectedData += symbol
        }
        return unprocessed
    }

    override fun hasDataToGenerate(): Boolean = collectedData.isNotEmpty()

    override fun generate() {
        val fileSpec = FileSpec.builder("com.webull.xxx", "XxxFactory")
            .addFileComment("Generated code! Do not modify.")
            .addType(buildObject())
            .build()
        val sourceFiles = collectedData.mapNotNull { it.containingFile }
        writeKotlinFile(fileSpec, Dependencies(aggregating = true, *sourceFiles.toTypedArray()))
    }
}
```

---

## 11. 下一步工作

按复杂度递增顺序实现：

1. **功能四 MarketViewRoute** — 读取 `docs/features/04-market-view-route.md`，参考 `launcher-compiler/.../MarketViewRouteGeneration.kt`
2. **功能五 TradeInterface** — 读取 `docs/features/05-trade-interface.md`，注意 KSP 中获取注解 Class 值的方式
3. **功能六 TradeServiceMaker** — 读取 `docs/features/06-trade-service-maker.md`
4. **功能七 Mapper** — 读取 `docs/features/07-mapper.md`

每完成一个功能后：
- 在 `WbKspProcessor.generations` 中注册
- 在 `app` 模块中添加测试注解类
- 运行 `./gradlew :app:kspDebugKotlin` 验证
- 生成迁移文档到 `docs/migration/`

# Launcher — 编译时代码生成框架

## 概述

Launcher 是项目的编译时注解处理框架，负责自动生成 Activity/Fragment 启动器、路由、功能地图、交易服务工厂等代码。当前已完成从 KAPT 到 KSP 的迁移。

## 模块结构

```
core-layer/launcher/
├── launcher-joke/              # 注解定义包（纯 Kotlin/Java，无处理逻辑）
├── launcher-compiler/          # [已废弃] 旧 KAPT 注解处理器
├── launcher-compiler-ksp/      # [当前] KSP 处理器 — Launcher/Router
├── launcher-compiler-wb-ksp/   # [当前] KSP 处理器 — Function/MarketView/Trade
└── migration-ksp/              # 迁移文档与知识库
```

## 模块职责

### launcher-joke（注解定义）

纯注解包，不包含任何处理逻辑。业务模块通过 `implementation` 依赖此模块使用注解。

| 包 | 注解 | 用途 |
|----|------|------|
| `launcher` | `@Boom`, `@IncludeParentBoom`, `@MakeResult`, `@ParentCls`, `@Router`, `@RouterCheck` | Activity/Fragment 启动器 + 路由 |
| `wb` | `@Function`, `@MarketViewRoute`, `@TradeInterface`, `@TradeServiceMaker` | 功能地图 + 视图路由 + 交易服务 |
| `wb.service` | `@ServiceRegistry`, `@ServiceGroup`, `ServiceEntry<T>`, `IProvider` | 服务聚合（替代 @AutoService） |
| `wb.bean` | `@AutoUpdate`, `@AutoUpdateIgnore`, `@AutoUpdateAlways`, `@AutoUpdateCheck` | Bean 自动赋值 |
| `wb.bean` | `@AutoConvert`, `AutoConvertLifecycle<S, T>` | 数据转换 |
| `wb.bean` | `@ABTestKeys`, `@ABTestKeyExclude` | ABTest Key 自动收集 |
| `mapper` | `@Mapper`, `@Mapping`, `@MappingTarget`, `@MappingConfig` 等 | 对象映射 |

### launcher-compiler-ksp（KSP 处理器 A）

处理 `launcher` 包下的注解，生成 **Java** 代码。

| 功能 | 输入注解 | 输出 |
|------|----------|------|
| 启动器 | `@Boom` + `@IncludeParentBoom` + `@MakeResult` + `@ParentCls` | `XxxLauncher.java` |
| 路由 | `@Router` + `@RouterCheck` | `Xxx_XXXxxx.java` |

**技术栈：** KSP API + JavaPoet

### launcher-compiler-wb-ksp（KSP 处理器 B）

处理 `wb` 包下的注解，生成 **Kotlin** 代码。

| 功能 | 输入注解 | 输出 |
|------|----------|------|
| 功能地图 | `@Function` | `FunctionFactory.kt` |
| 视图路由 | `@MarketViewRoute` | `MarketViewRouteFactory.kt` |
| 交易服务工厂 | `@TradeInterface` | `TradeInterfaceFactory{Module}.kt` |
| 聚合接口 | `@TradeServiceMaker` | `ITradeManagerService.kt` |
| 自动赋值 | `@AutoUpdate` + `@AutoUpdateIgnore` + `@AutoUpdateAlways` + `@AutoUpdateCheck` | `{ClassName}AutoUpdate.kt` |
| 数据转换 | `@AutoConvert` + `AutoConvertLifecycle<S, T>` | `{Source}ConvertTo{Target}.kt` |
| ABTest Key 收集 | `@ABTestKeys` + `@ABTestKeyExclude` | `{ClassName}_AbKeys.kt` + `{ClassName}_AbTestProvider.kt` |
| 服务聚合 | `@ServiceRegistry` | `{Module}_ServiceAggregator.kt` + SPI 文件 |

**技术栈：** KSP API + KotlinPoet + JavaPoet

### launcher-compiler（已废弃）

旧 KAPT 实现，保留用于对比验证。迁移完成后将移除。

---

## 架构设计

### 为什么拆分为两个 KSP 处理器？

1. **职责分离**：Launcher/Router 是基础能力（所有模块都用），Function/Trade 是业务能力（特定模块使用）
2. **输出语言不同**：`launcher-compiler-ksp` 生成 Java（兼容旧代码调用），`launcher-compiler-wb-ksp` 生成 Kotlin（新架构）
3. **依赖隔离**：`wb-ksp` 额外依赖 KotlinPoet，不影响基础处理器的轻量性

### 处理流程

```
┌─────────────────────────────────────────────────────────────┐
│  KSP 框架                                                     │
│                                                               │
│  ┌─────────────────────┐    ┌──────────────────────────────┐ │
│  │ launcher-compiler-ksp│    │ launcher-compiler-wb-ksp      │ │
│  │                     │    │                              │ │
│  │ LauncherKspProvider │    │ WbKspProvider                │ │
│  │       ↓             │    │       ↓                      │ │
│  │ LauncherKspProcessor│    │ WbKspProcessor               │ │
│  │       ↓             │    │       ↓                      │ │
│  │ • 扫描 @Boom        │    │ • FunctionGeneration         │ │
│  │ • 扫描 @Router      │    │ • MarketViewRouteGeneration  │ │
│  │ • ClassBindingFactory│    │ • TradeInterfaceGeneration   │ │
│  │ • 生成 Launcher     │    │ • TradeServiceMakerGeneration│ │
│  │ • 生成 Router       │    │ • AutoUpdateGeneration       │ │
│  └─────────────────────┘    │ • AutoConvertGeneration      │ │
│                              │ • ABTestKeysGeneration       │ │
│                              │ • ServiceAggregatorGeneration│ │
│                              └──────────────────────────────┘ │
│                                                               │
│  输出: build/generated/ksp/stocksDebug/{java|kotlin}/         │
└─────────────────────────────────────────────────────────────┘
```

### wb-ksp 的 Generation 架构

`WbKspProcessor` 采用 **collect → generate** 两阶段模式：

```kotlin
interface BaseGeneration {
    fun collect(resolver: Resolver): List<KSAnnotated>  // 阶段一：收集注解符号
    fun hasDataToGenerate(): Boolean                    // 是否有数据需要生成
    fun generate()                                      // 阶段二：生成代码
}
```

新增功能只需：
1. 实现 `BaseGeneration` 接口
2. 在 `WbKspProcessor.generations` 列表中注册

---

## 业务模块接入

### Gradle 配置

```groovy
// build.gradle
apply plugin: 'com.google.devtools.ksp'

dependencies {
    implementation project(':launcher-joke')
    ksp project(':launcher-compiler-ksp')
    ksp project(':launcher-compiler-wb-ksp')  // 需要 Function/Trade 功能时添加
}

// 传递编译参数（TradeInterface 需要）
ksp {
    arg("module_name", "TradeCore")
}
```

### 生成代码位置

```
{module}/build/generated/ksp/stocksDebug/java/    # launcher-compiler-ksp 输出
{module}/build/generated/ksp/stocksDebug/kotlin/  # launcher-compiler-wb-ksp 输出
```

---

## 功能速查

### 功能一：启动器（@Boom）

```kotlin
class OrderDetailFragment : AppBaseFragment<...>() {
    @Boom(index = 0, desc = "订单ID")
    var orderId: String = ""

    @Boom(index = 1, isOptional = true, desc = "来源")
    var fromPage: String? = null
}

// 使用
OrderDetailFragmentLauncher.newInstance("ORDER_123").jump(context)
OrderDetailFragmentLauncher.newInstance("ORDER_123", "home").jump(context)
```

#### @IncludeParentBoom（继承父类参数）

标注在子类上，生成的 Launcher 会包含父类的 `@Boom` 属性。子类的 `@Boom` index 不能与父类重复。

```kotlin
open class BaseTickerFragment : AppBaseFragment<...>() {
    @Boom(index = 0, desc = "股票ID")
    var tickerId: String = ""
}

@IncludeParentBoom
class TickerDetailFragment : BaseTickerFragment() {
    @Boom(index = 1, desc = "Tab索引")  // index 不能与父类的 0 重复
    var tabIndex: Int = 0
}

// 生成的 Launcher 包含 tickerId + tabIndex 两个参数
TickerDetailFragmentLauncher.newInstance("AAPL", 2).jump(context)
```

### 功能二：路由（@Router）

```kotlin
@Router(routerPath = "webull://trade/order_detail")
class OrderDetailActivity : AppBaseActivity<...>() {
    @Boom(index = 0, desc = "订单ID")
    var orderId: String = ""
}

// 跨模块跳转
OrderDetailActivity_XXXxxx.jump(context, "ORDER_123")
```

### 功能三：功能地图（@Function）

```kotlin
@Function(functionId = "order_detail", desc = "订单详情", group = ["trade"])
class OrderDetailActivity : AppBaseActivity<...>() { ... }

// 运行时查找
FunctionFactory.createViewById(context, "order_detail")
```

### 功能四：视图路由（@MarketViewRoute）

```kotlin
@MarketViewRoute(key = "kline", desc = "K线图")
class KLineView(context: Context) : View(context) { ... }

// 动态创建
MarketViewRouteFactory.createViewById(context, "kline")
```

### 功能五：交易服务工厂（@TradeInterface）

```kotlin
@TradeInterface(value = ITradeAccountInterface::class)
class TradeAccountImpl : ITradeAccountInterface { ... }

// 运行时获取实现
val service = factory.createInstance(ITradeAccountInterface::class.java)
```

### 功能六：聚合接口（@TradeServiceMaker）

```kotlin
@TradeServiceMaker(
    baseInterface = ITradeInterface::class,
    scanPackages = ["com.webull.commonmodule.trade.service.trade"],
    additionalInterfaces = [IService::class],
    className = "ITradeManagerService"
)
interface TradeManagerServiceMarker
```

自动生成继承所有顶层 Trade 接口的聚合接口。

### 功能七：自动赋值（@AutoUpdate）

为 Bean 类自动生成字段赋值扩展函数，用于行情推送等场景下将新数据合并到已有对象。

#### 核心注解

| 注解 | 作用域 | 用途 |
|------|--------|------|
| `@AutoUpdate` | 类 | 标记需要生成 update 扩展函数的 Bean 类 |
| `@AutoUpdateIgnore` | 字段/属性 | 跳过该字段，不生成赋值代码（生成注释提示） |
| `@AutoUpdateAlways` | 字段/属性 | 无条件赋值，不做任何检查 |
| `@AutoUpdateCheck` | 字段/属性 | 自定义该字段的赋值条件表达式 |

#### @AutoUpdate 参数

```kotlin
@AutoUpdate(
    functionName = "",           // 生成的函数名，为空时默认 "update{ClassName}Fields"
    stringCheck = "{field}.valueIsNotEmpty()",  // String 字段的默认检查表达式
    stringCheckImport = "wb.bean.valueIsNotEmpty"  // stringCheck 中扩展函数的 import 路径
)
```

#### 默认赋值规则

| 字段类型 | 生成的条件 | 说明 |
|----------|-----------|------|
| `String?` | `if (from.xxx.valueIsNotEmpty())` | 非 null 且非空且非 "--" |
| `Int` | `if (from.xxx != 默认值)` | 默认值从声明中提取，未声明时为 0 |
| `Long` | `if (from.xxx != 默认值L)` | 默认值从声明中提取，未声明时为 0L |
| `Double` | `if (from.xxx != 默认值)` | 默认值从声明中提取，未声明时为 0.0 |
| `Float` | `if (from.xxx != 默认值f)` | 默认值从声明中提取，未声明时为 0.0f |
| `Boolean` | `if (from.xxx)` 或 `if (!from.xxx)` | 默认 false 时检查 true，默认 true 时检查 false |
| 可空对象 | `if (from.xxx != null)` | 非 null |
| 非空对象 | 直接赋值 | 无条件 |
| `IntArray` | 跳过（SKIP） | 需要 `@AutoUpdateAlways` 或 `@AutoUpdateCheck` 强制生成 |

**默认值提取机制：** 处理器通过读取源码文本解析属性声明中 `= xxx` 部分的初始值。例如 `var tradeStamp: Long = -1L` 会生成 `if (from.tradeStamp != -1L)` 而非 `if (from.tradeStamp != 0L)`。

#### 基本用法

```kotlin
@AutoUpdate
open class TickerTupleV5 : TickerBase(), Serializable {
    var close: String? = null
    var change: String? = null
    var tradeStamp: Long = 0
    var retryCount: Int = -1       // 默认值 -1，生成 if (from.retryCount != -1)

    @AutoUpdateIgnore  // 有特殊业务逻辑，手动处理
    var status: String? = null
}
```

生成代码：

```kotlin
// Generated: TickerTupleV5AutoUpdate.kt
fun TickerTupleV5.updateTickerTupleV5Fields(from: TickerTupleV5, callParent: Boolean = true) {
    if (callParent) {
        updateTickerBaseFields(from)  // 自动调用父类的 update 函数
    }

    if (from.change.valueIsNotEmpty()) change = from.change
    if (from.close.valueIsNotEmpty()) close = from.close
    if (from.retryCount != -1) retryCount = from.retryCount
    if (from.tradeStamp != 0L) tradeStamp = from.tradeStamp

    // @AutoUpdateIgnore: status
}
```

#### 继承关系自动处理

当子类和父类都标注了 `@AutoUpdate` 时：
- 子类生成的函数自动包含 `callParent` 参数（默认 `true`）
- 子类只生成自身声明的字段赋值，父类字段由父类的 update 函数处理
- 父类通过 `superTypes` 自动推断，无需手动指定

```kotlin
@AutoUpdate
open class TickerBase { ... }  // 生成 updateTickerBaseFields(from)

@AutoUpdate(functionName = "updateRealtimeV2Fields")
open class TickerRealtimeV2 : TickerTupleV5() { ... }
// 生成 updateRealtimeV2Fields(from, callParent = true)
// callParent=true 时自动调用 updateTickerTupleV5Fields(from)
```

#### @AutoUpdateCheck 自定义条件

```kotlin
@AutoUpdateCheck(condition = "{from}.listStatusInteger != null")
private Integer listStatus;

@AutoUpdateCheck(
    condition = "{field} != null && {field} != 0"
)
private Integer subType;
```

占位符说明：
- `{field}` → 替换为 `from.属性名`（如 `from.volume`）
- `{from}` → 替换为源对象 `from`（用于访问其他 getter/属性）

生成结果：

```kotlin
if (from.listStatusInteger != null) listStatus = from.listStatus
if (from.subType != null && from.subType != 0) subType = from.subType
```

#### @AutoUpdateAlways 无条件赋值

```kotlin
@AutoUpdateAlways
var microTrend: MicroTrend? = null  // 每次推送都覆盖，不检查 null
```

生成结果：`microTrend = from.microTrend`

> 也可用于 SKIP 类型（如 `IntArray`），强制生成赋值代码。

#### Java 类支持

对 Java 类，处理器通过 getter/setter 方法对推断 private 字段：
- `getXxx()` + `setXxx(value)` → 识别为字段 `xxx`
- `isXxx()` + `setXxx(value)` → 识别为字段 `isXxx`
- 注解可标注在字段声明上，处理器会正确读取

```java
@AutoUpdate
public class TickerBase implements Serializable {
    @AutoUpdateIgnore
    public String tickerId = "";  // 跳过

    @AutoUpdateCheck(condition = "{from}.listStatusInteger != null")
    private Integer listStatus;  // 自定义条件

    private String belongTickerId;  // 通过 getter/setter 推断
    public String getBelongTickerId() { return belongTickerId; }
    public void setBelongTickerId(String v) { this.belongTickerId = v; }
}
```

#### 生成文件

- 文件名：`{ClassName}AutoUpdate.kt`
- 包名：与源类相同
- 位置：`build/generated/ksp/{variant}/kotlin/{package}/`

### 功能八：数据转换（@AutoConvert）

为两个 Bean 类之间自动生成类型安全的转换扩展函数，适用于 DTO → Entity、Response → VO 等场景。

#### 核心注解与接口

| 注解/接口 | 作用域 | 用途 |
|-----------|--------|------|
| `@AutoConvert` | 类 | 标记转换器类，触发代码生成 |
| `AutoConvertLifecycle<S, T>` | 接口 | 定义转换生命周期回调（onStart / onEnd） |

#### @AutoConvert 参数

```kotlin
@AutoConvert(
    functionName = "",           // 生成的函数名，为空时默认 "convertTo{TargetClassName}"
    ignoreTargets = []           // 忽略的目标类属性名，不参与映射也不出现在未匹配注释中
)
```

#### AutoConvertLifecycle 接口

```kotlin
interface AutoConvertLifecycle<S, T> {
    /** 转换开始前调用，target 已创建但尚未赋值 */
    fun onStart(source: S, target: T) {}

    /** 转换结束后调用，自动映射已完成，可补充手动映射 */
    fun onEnd(source: S, target: T) {}
}
```

#### 匹配规则

| 条件 | 结果 |
|------|------|
| 同名 + 类型兼容 | 自动赋值 |
| 同名 + 类型不兼容 | 注释提醒（需在 `onEnd` 中手动处理） |
| 目标类有但源类无 | 注释提醒 |
| 在 `ignoreTargets` 中 | 跳过，不生成也不提醒 |

**类型兼容判断：**
- 完全相同类型 → 兼容
- Kotlin/Java 基本类型互通（如 `java.lang.String` ↔ `kotlin.String`）
- 源类可空 → 目标类非空 → 不兼容（需手动处理）
- 源类非空 → 目标类可空 → 兼容

#### 基本用法

```kotlin
// 1. 定义转换器
@AutoConvert
class OrderDtoConverter : AutoConvertLifecycle<OrderDto, OrderEntity> {
    override fun onEnd(source: OrderDto, target: OrderEntity) {
        // 手动处理不匹配的属性
        target.price = source.priceStr.toBigDecimal()
    }
}

// 2. 使用生成的扩展函数
val entity = orderDto.convertToOrderEntity()
// 或传入自定义 converter 实例
val entity = orderDto.convertToOrderEntity(MyCustomConverter())
```

#### 生成代码示例

```kotlin
// Generated: OrderDtoConvertToOrderEntity.kt
fun OrderDto.convertToOrderEntity(converter: OrderDtoConverter = OrderDtoConverter()): OrderEntity {
    val target = OrderEntity()
    converter.onStart(this, target)

    target.orderId = this.orderId
    target.symbol = this.symbol
    target.quantity = this.quantity

    // 以下目标属性在源类中未找到匹配：
    // - price: BigDecimal（源类类型不匹配：String）

    converter.onEnd(this, target)
    return target
}
```

#### 继承关系处理

当目标类有继承关系时，生成器会：
- 分别匹配当前类和父类的可写属性
- 父类属性赋值放入独立的 `convertParentFields()` 私有函数
- 父类中 private 且无 setter 的字段标记为"只读"

```kotlin
// 生成代码包含父类处理
fun SourceDto.convertToTargetEntity(converter: ...): TargetEntity {
    val target = TargetEntity()
    converter.onStart(this, target)

    convertParentFields(this, target)  // 父类属性

    target.ownField1 = this.ownField1  // 当前类属性
    target.ownField2 = this.ownField2

    converter.onEnd(this, target)
    return target
}

private fun convertParentFields(source: SourceDto, target: TargetEntity) {
    target.parentField1 = source.parentField1
    target.parentField2 = source.parentField2
}
```

#### Java 类支持

- 源类：通过 `getXxx()` / `isXxx()` 推断可读属性
- 目标类：通过 `setXxx(value)` 推断可写属性
- 生成代码自动使用 getter/setter 方法调用

```kotlin
// Java 源类有 getpValue1() → 生成 this.getpValue1()
// Java 目标类有 setpValue1(v) → 生成 target.setpValue1(value)
```

#### ignoreTargets 用法

```kotlin
@AutoConvert(ignoreTargets = ["serialVersionUID", "internalFlag"])
class MyConverter : AutoConvertLifecycle<Source, Target> { ... }
```

被忽略的属性不会出现在生成代码中，也不会出现在"未匹配"注释中。

#### 生成文件

- 文件名：`{SourceClassName}ConvertTo{TargetClassName}.kt`
- 包名：与转换器类相同
- 位置：`build/generated/ksp/{variant}/kotlin/{package}/`

### 功能九：ServiceAggregator 服务聚合（@ServiceRegistry）

替代 `@AutoService` + `ServiceLoader` 的编译时服务聚合方案。KSP 为每个模块生成一个聚合类，运行时通过 `ServiceAggregatorHolder` 统一查找，消除逐个反射实例化的性能开销。

#### 核心注解

| 注解/类 | 包 | 用途 |
|---------|-----|------|
| `@ServiceRegistry` | `wb.service` | 标记实现类注册到聚合器（替代 `@AutoService`） |
| `@ServiceGroup` | `wb.service` | 标记聚合接口方法对应的 SPI 接口组 |
| `ServiceEntry<T>` | `wb.service` | 懒加载包装，按需实例化 |

#### @ServiceRegistry

```kotlin
import wb.service.ServiceRegistry

// 迁移：@ServiceRegistry(IViewProvider::class) → @ServiceRegistry(IViewProvider::class)
@ServiceRegistry(IViewProvider::class, priority = 100)
class MarketBannerViewProvider : IViewProvider {
    override val key = "market_banner"
    override fun createView(context: Context?, vararg params: Any?): View? { ... }
}

@ServiceRegistry(IService::class)
class BondService(context: Context) : IService, IBondService { ... }
```

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `value` | `KClass<*>` | 必填 | 归入哪个 SPI 接口 |
| `priority` | `Int` | `0` | 优先级，数值越大越靠前 |

#### 聚合接口（IServiceAggregator）

统一接口，KSP 生成的聚合类实现此接口：

```kotlin
interface IServiceAggregator {
    @ServiceGroup(IViewProvider::class)
    fun provideViewProviders(): List<IViewProvider> = emptyList()

    @ServiceGroup(IFragmentProvider::class)
    fun provideFragmentProviders(): List<IFragmentProvider> = emptyList()

    @ServiceGroup(IService::class)
    fun provideServiceEntries(): List<ServiceEntry<IService>> = emptyList()
}
```

返回类型决定生成模式：
- `List<X>` → eager，直接实例化所有实现类
- `List<ServiceEntry<X>>` → lazy，按需实例化

#### 运行时使用（ServiceAggregatorHolder）

```kotlin
import com.webull.core.framework.service.ServiceAggregatorHolder

// 获取聚合器列表
val aggregators = ServiceAggregatorHolder.aggregators

// 按 key 查找 ViewProvider
val provider = aggregators
    .flatMap { it.provideViewProviders() }
    .firstOrNull { it.key == "market_banner" }

// 按 key 查找 FragmentProvider
val fragment = aggregators
    .flatMap { it.provideFragmentProviders() }
    .firstOrNull { it.key == "ticker_news" }
    ?.createFragment()

// 按类型查找 IService（lazy 模式，只实例化匹配的那一个）
val bondService = aggregators
    .flatMap { it.provideServiceEntries() }
    .firstOrNull { it.isType(IBondService::class.java) }
    ?.instance as? IBondService
```

#### KSP 生成产物示例

```kotlin
// Generated: MarketModule_ServiceAggregator.kt
package com.webull.service

class MarketModule_ServiceAggregator : IServiceAggregator {

    override fun provideViewProviders(): List<IViewProvider> = listOf(
        MarketBannerViewProvider,           // priority=200, object
        AlertCardViewProvider(),            // priority=100
        HotSearchRankingCardViewProvider()  // priority=0
    )

    override fun provideFragmentProviders(): List<IFragmentProvider> = listOf(
        MarketHomeFragmentProvider(),
        TickerNewsFragmentProvider()
    )

    override fun provideServiceEntries(): List<ServiceEntry<IService>> = listOf(
        ServiceEntry(BondService::class.java) { BondService() },
        ServiceEntry(RankService::class.java) { RankService() }
    )
}
```

SPI 注册文件：`META-INF/services/com.webull.core.framework.service.IServiceAggregator`

#### 迁移步骤

```
@ServiceRegistry(IViewProvider::class)  →  @ServiceRegistry(IViewProvider::class)
@AutoService(IService::class)       →  @ServiceRegistry(IService::class)
```

迁移期间 `AppViewProvider` 已内置 fallback：聚合器优先，无注册时回退到 `ServiceLoader`。

#### 扩展新 service 类型

只需两步，Processor 无需改动：

1. 在 `IServiceAggregator` 接口新增方法（通过返回类型决定 eager/lazy）
2. 业务模块实现类加 `@ServiceRegistry(NewInterface::class)`

#### 设计文档

详见 [migration-ksp/features/ksp-service-aggregator-design.md](migration-ksp/features/ksp-service-aggregator-design.md)

### 功能十：ABTest Key 自动收集（@ABTestKeys）

编译时自动收集 ABTest Key 类中的所有 String 属性，生成 `getAllAbKeys()` 函数。可选生成 `AbTestProvider` 实现类并自动注册到 ServiceAggregator。

#### 核心注解

| 注解 | 作用域 | 用途 |
|------|--------|------|
| `@ABTestKeys` | 类/object | 标记 ABTest Key 定义类，触发 key 收集 |
| `@ABTestKeyExclude` | 字段/属性 | 排除该属性，不参与收集 |

#### @ABTestKeys 参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `generateProvider` | `Boolean` | `false` | 是否额外生成 AbTestProvider 实现类并注册到 ServiceAggregator |

#### 基本用法

```kotlin
import wb.bean.ABTestKeys
import wb.bean.ABTestKeyExclude

@ABTestKeys(generateProvider = true)
object TradeCommonABTestKey : Serializable {
    /** 启用新下单流程 */
    val KEY_ENABLE_NEW_ORDER: String = "key_enable_new_order"

    /** 启用期权新 UI */
    val KEY_ENABLE_OPTION_UI: String = "key_enable_option_ui"

    @ABTestKeyExclude  // 内部使用，不注册到 ABTest 平台
    val KEY_DEBUG_MODE: String = "key_debug_mode"
}
```

#### 生成产物

**1. getAllAbKeys 函数**（始终生成）：

```kotlin
// Generated: TradeCommonABTestKey_AbKeys.kt
fun TradeCommonABTestKey_getAllAbKeys(): List<String> = listOf(
    TradeCommonABTestKey.KEY_ENABLE_NEW_ORDER, // 启用新下单流程
    TradeCommonABTestKey.KEY_ENABLE_OPTION_UI  // 启用期权新 UI
)
```

**2. AbTestProvider 实现类**（`generateProvider = true` 时生成）：

```kotlin
// Generated: TradeCommonABTestKey_AbTestProvider.kt
class TradeCommonABTestKey_AbTestProvider : AbTestProvider {
    override fun keys(): List<String> = TradeCommonABTestKey_getAllAbKeys()
}
```

Provider 自动注册到模块的 `ServiceAggregator`，运行时通过 `ServiceAggregatorHolder` 统一收集所有模块的 ABTest Key。

#### 支持的类型

- Kotlin `object`（推荐）
- Kotlin `class`
- Java `class`（public static final String 字段）

#### 收集规则

- 收集所有 `String` 类型的 public 属性/字段
- 排除 `@ABTestKeyExclude` 标注的属性
- 排除 `private` 修饰的属性
- 属性的 KDoc 注释会作为行内注释保留在生成代码中

#### 生成文件

- `{ClassName}_AbKeys.kt` — getAllAbKeys 函数
- `{ClassName}_AbTestProvider.kt` — Provider 实现类（仅 `generateProvider = true`）
- 位置：`build/generated/ksp/{variant}/kotlin/{package}/`

---

## 与旧 KAPT 实现的差异

详见 [migration-ksp/problem/ksp与kapt的差异点.md](migration-ksp/problem/ksp与kapt的差异点.md)

关键差异：
- **输出语言**：wb-ksp 生成 Kotlin 代码（KAPT 生成 Java）
- **MarketViewRouteFactory**：KSP 版本使用 Map + lambda 替代 switch-case，新增 `createViewById` 方法
- **TradeInterfaceFactory**：KSP 版本使用 Kotlin `when` 替代 Java `switch`
- **Launcher 字段赋值**：KSP 使用 setter 方法，KAPT 使用直接字段赋值

---

## 目录说明

### migration-ksp/（迁移知识库）

```
migration-ksp/
├── KAPT-TO-KSP-MIGRATION.md      # 迁移技术文档（API 映射、难点、步骤）
├── LAUNCHER-BUSINESS-SPEC.md      # 业务功能完整规格文档
├── context/                       # 迁移上下文资料
├── features/                      # 功能实现记录
├── migration/                     # 迁移过程记录
├── problem/                       # 问题记录
│   └── ksp与kapt的差异点.md        # KSP vs KAPT 生成代码对比报告
└── product/                       # 产出物
```

---

## 开发指南

### 新增注解处理功能

1. 在 `launcher-joke` 中定义注解
2. 选择处理器：
   - 基础能力（所有模块通用）→ `launcher-compiler-ksp`
   - 业务能力（特定模块）→ `launcher-compiler-wb-ksp`
3. 实现处理逻辑：
   - `launcher-compiler-ksp`：直接在 `LauncherKspProcessor.processTarget()` 中添加
   - `launcher-compiler-wb-ksp`：新建 `XxxGeneration` 类实现 `BaseGeneration`，注册到 `WbKspProcessor.generations`
4. 使用 `KspUtils.kt` 中的公共工具：
   - `findAnnotation()` / `getAnnotation()` — 类型安全的注解查找（禁止 `shortName` 字符串匹配）
   - `arg<T>(name)` — 注解参数读取
   - `toClassName()` — KSClassDeclaration → KotlinPoet ClassName
   - `JVM_STATIC` / `JVM_FIELD` — 公共 AnnotationSpec 常量

### 调试

```bash
# 查看 KSP 生成的代码
find . -path "*/build/generated/ksp/stocksDebug*" -name "*.java" -o -name "*.kt"

# 清理重新生成
./gradlew :MainApp:kspStocksDebugKotlin --rerun-tasks
```

### 注意事项

- `launcher-joke` 是注解定义包，**禁止在其中添加处理逻辑**
- KSP 处理器中 `Dependencies(aggregating = false, ...)` 用于单文件对单文件的生成
- KSP 处理器中 `Dependencies(aggregating = true, ...)` 用于多文件聚合生成（如 FunctionFactory）
- 生成 Java 文件时必须指定 `extensionName = "java"`
- **禁止硬编码类名字符串**：launcher-joke 中的类必须通过 `Xxx::class.qualifiedName!!` 引用，注解属性名必须通过 `Xxx::propertyName.name` 引用
- 注解查找必须使用 `findAnnotation()` / `getAnnotation()`（全限定名匹配），**禁止 `shortName.asString() == "Xxx"` 模式**

---

## 特别注意：硬编码类名

以下类名在 `launcher-compiler-wb-ksp` 中以字符串形式引用，因为它们**不在 compiler 的 classpath 中**（定义在 CommonModule / CoreModule 的 aar 中）。如果这些类发生重命名或移动包路径，需要同步修改对应的字符串常量。

| 文件 | 硬编码字符串 | 引用的类 | 所在模块 |
|------|-------------|---------|---------|
| `ABTestKeysGeneration.kt` | `"com.webull.commonmodule.abtest"` + `"AbTestProvider"` | `AbTestProvider` 接口 | CommonModule |
| `ServiceAggregatorGeneration.kt` | `"com.webull.core.framework.service.IServiceAggregator"` | `IServiceAggregator` 接口 | CoreModule |
| `TradeInterfaceGeneration.kt` | `"com.webull.commonmodule.trade.service.trade.base"` + `"ITradeInterface"` | `ITradeInterface` 接口 | CommonModule |
| `TradeInterfaceGeneration.kt` | `"com.webull.commonmodule.trade.service.trade.base"` + `"ITradeInterfaceFactory"` | `ITradeInterfaceFactory` 接口 | CommonModule |

**为什么无法改为类引用？**

`launcher-compiler-wb-ksp` 的 `build.gradle` 只依赖 `launcher-joke`：

```groovy
dependencies {
    implementation project(':launcher-joke')
    // CommonModule / CoreModule 不在 compiler 的 classpath 中
}
```

这些类定义在运行时模块（CommonModule / CoreModule）中，而 KSP 处理器是编译时工具，不应该依赖运行时模块。因此只能通过字符串引用。

**维护建议：** 如果上述类发生重命名，编译不会报错但生成代码会引用不存在的类，导致业务模块编译失败。修改时需全局搜索对应字符串。

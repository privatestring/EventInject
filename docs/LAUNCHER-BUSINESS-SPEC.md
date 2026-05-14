# Launcher 业务功能文档

## 功能一：Activity/Fragment/BroadcastReceiver/Model 启动器

### 1.1 功能描述

编译时自动为 Android 组件生成类型安全的启动代码，消除手动编写 Intent/Bundle 传参的样板代码。

### 1.2 支持的组件类型

| 组件类型 | 判断条件 | 生成类后缀 |
|----------|----------|------------|
| Activity | 继承 `android.app.Activity` | `XxxLauncher` |
| Fragment | 继承 `androidx.fragment.app.Fragment` 或 `android.app.Fragment` | `XxxLauncher` |
| BroadcastReceiver | 继承 `android.content.BroadcastReceiver` | `XxxLauncher` |
| Model | 以上都不是的普通类 | `XxxLauncher` |

### 1.3 注解说明

#### `@Boom`（标注在字段上）

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `index` | int | 必填 | 参数排序位置，决定生成方法中参数的顺序 |
| `key` | String | `""` | 自定义 Intent/Bundle key，为空时自动生成 `{包名}.{字段名}IntentKey` |
| `isOptional` | boolean | `false` | 是否可选参数，可选参数会生成多个重载方法 |
| `useFieldKey` | boolean | `false` | 是否使用属性名作为 key（而非全限定名） |
| `desc` | String | `""` | 参数描述，跨模块使用 Router 时必填 |

#### `@MakeResult`（标注在类上）

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `includeStartForResult` | boolean | `false` | 是否生成 `startForResult()` 方法（仅 Activity） |

#### `@ParentCls`（标注在类上）

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `isParentClass` | boolean | `true` | 标记为父类，生成 `addIntentParams(intent, params...)` 供子类复用 |

#### `@MulField`（标注在字段上）

无属性。标记多变参数，与 `@Boom` 配合使用。

### 1.4 支持的参数类型

| 类型分类 | 具体类型 |
|----------|----------|
| 基本类型 | `int`, `long`, `float`, `double`, `boolean`, `char`, `byte`, `short` |
| 字符串 | `String`, `CharSequence` |
| 基本类型数组 | `int[]`, `long[]`, `float[]`, `double[]`, `boolean[]`, `char[]`, `byte[]`, `short[]` |
| 对象数组 | `String[]`, `CharSequence[]` |
| ArrayList | `ArrayList<Integer>`, `ArrayList<String>`, `ArrayList<CharSequence>` |
| Parcelable | 任何实现 `Parcelable` 的类型 |
| Serializable | 任何实现 `Serializable` 的类型 |
| Parcelable ArrayList | `ArrayList<? extends Parcelable>` |

### 1.5 生成代码结构

#### Activity 生成方法

| 方法 | 签名 | 说明 |
|------|------|------|
| `bind` | `static void bind(Activity activity)` | 从 Intent 中取值赋给字段 |
| `getIntentFrom` | `static Intent getIntentFrom(Context context, params...)` | 构造 Intent |
| `startActivity` | `static void startActivity(Context context, params...)` | 启动 Activity |
| `startForResult` | `static void startForResult(Activity context, params..., int result)` | 带回调启动（需 `@MakeResult`） |

#### Fragment 生成方法

| 方法 | 签名 | 说明 |
|------|------|------|
| `bind` | `static void bind(Fragment fragment)` | 从 Bundle 中取值赋给字段 |
| `newInstance` | `static Fragment newInstance(params...)` | 创建 Fragment 并设置 Arguments |
| `getBundleFrom` | `static Bundle getBundleFrom(params...)` | 构造 Bundle |

#### ParentCls 生成方法

| 方法 | 签名 | 说明 |
|------|------|------|
| `addIntentParams` | `static Intent addIntentParams(Intent intent, params...)` | 子类向父类 Intent 添加参数 |
| `addBundleParams` | `static void addBundleParams(Bundle args, params...)` | 子类向父类 Bundle 添加参数 |

#### Model 生成方法

| 方法 | 签名 | 说明 |
|------|------|------|
| `bind` | `static void bind(Model model, Intent intent)` | 从 Intent 取值 |
| `bind` | `static void bind(Model model, Bundle arguments)` | 从 Bundle 取值 |
| `getIntentFrom` | `static Intent getIntentFrom(Context context, Class clazz, params...)` | 构造 Intent（需指定目标类） |
| `getArguments` | `static Bundle getArguments(params...)` | 构造 Bundle |

### 1.6 可选参数重载生成规则

当有 N 个参数，其中 M 个标记 `isOptional = true` 时，会生成 2^M 个方法重载（所有可选参数的组合）。

示例：3 个参数 (A, B[optional], C[optional]) → 生成 4 个重载：
- `method(A, B, C)`
- `method(A, B)`
- `method(A, C)`
- `method(A)`

### 1.7 字段访问规则

| 字段可见性 | 访问方式 | 生成代码 |
|------------|----------|----------|
| public / protected / package | 直接赋值 | `target.fieldName = value` |
| private + 有 setXxx() | 调用 setter | `target.setFieldName(value)` |
| private + isXxx 命名 + 有 setXxx() | 调用 setter（去 is） | `target.setFieldName(value)` |
| private + 无 setter | 编译报错 | `Inaccessible element` |

### 1.8 Key 生成规则

优先级：
1. `@Boom(key = "custom_key")` → 使用自定义 key
2. `@Boom(useFieldKey = true)` → 使用字段名作为 key
3. 默认 → `{包名}.{字段名}IntentKey`

生成的 key 常量命名：字段名转大写下划线 + `_INTENT_KEY`
- `userName` → `USER_NAME_INTENT_KEY`


### 1.9 使用示例

```kotlin
// 目标 Activity
@MakeResult(includeStartForResult = true)
class OrderDetailActivity : AppCompatActivity() {
    @Boom(index = 0, desc = "订单ID")
    var orderId: String = ""

    @Boom(index = 1, isOptional = true, desc = "来源页面")
    var fromPage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OrderDetailActivityLauncher.bind(this)  // 自动从 Intent 取值
    }
}

// 调用方
OrderDetailActivityLauncher.startActivity(context, "ORDER_123", "home")
OrderDetailActivityLauncher.startActivity(context, "ORDER_123")  // 可选参数省略
val intent = OrderDetailActivityLauncher.getIntentFrom(context, "ORDER_123")
```

### 1.10 编译错误场景

| 错误 | 触发条件 | 错误信息 |
|------|----------|----------|
| 不支持的类型 | 字段类型不在支持列表中 | `fields must extend from Serializable, Parcelable or be of type String, int...` |
| 字段不可访问 | private 且无 setter | `Inaccessable element` |
| 非类成员 | @Boom 标注在非类字段上 | `fields may only be contained in classes` |
| 私有类 | 类是 private 的 | `fields may not be contained in private classes` |
| index 重复 | 同一类中多个 @Boom 的 index 相同 | `has index parameters are the same` |

---

## 功能二：Router 路由系统

### 2.1 功能描述

基于 URL Scheme 的跨模块路由跳转系统。编译时为每个 `@Router` 类生成路由注册和跳转代码，运行时通过 `JumpManager` 统一分发。

### 2.2 注解说明

#### `@Router`（标注在类上）

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `routerPath` | String | 必填 | 路由路径，如 `"webull://trade/order_detail"` |
| `cls` | Class<?> | `Void.class` | 目标类，默认使用当前注解类 |

#### `@RouterCheck`（标注在类上）

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `checkers` | Class<? extends IRouterChecker>[] | 必填 | 路由拦截器类数组 |
| `cls` | Class<?> | `Void.class` | 目标类 |

### 2.3 生成代码结构

对每个 `@Router` 类生成 `XxxLauncher`（带 `_XXXxxx` 后缀的路由版本）：

| 生成内容 | 说明 |
|----------|------|
| `ROUTER_ACTION` 常量 | 路由路径字符串 |
| `putRouter()` | 注册路由到全局路由表 `router.WBRouter.putRouter(ROUTER_ACTION, className)` |
| `jump(context, params...)` | 通过 `JumpManager.jumpForRouter(context, scheme)` 跳转 |
| `jump(context, params..., callback)` | 带 `IRouterJumpCallback` 回调的跳转 |
| `getActionScheme(params...)` | 拼接完整 URL scheme（参数 URL encode） |

### 2.4 参数传递规则

- 路由参数通过 URL query string 传递：`webull://trade/order?orderId=xxx&fromPage=home`
- **当前仅支持 String 类型参数**，其他类型编译报错
- 参数值通过 `URLEncoder.encode(value, "UTF-8")` 编码
- 跨模块参数必须填写 `@Boom(desc = "...")` 描述，否则编译报错

### 2.5 路由拦截器

#### 接口定义

```java
public interface IRouterChecker {
    void doCheck(
        RouterCheckerChain chain,      // 责任链
        Object context,                // 上下文
        Map<String, String> params,    // 路由参数
        HashMap<String, String> extras,// 额外参数
        IRouterProceedCallback proceedCallback,  // 继续/取消默认跳转
        IRouterJumpCallback jumpCallback         // 自定义跳转
    );
    int priority();  // 优先级，数字越大越先执行
}
```

#### 执行流程

1. `RouterCheckerChain` 按 priority 排序所有 checker
2. 依次调用每个 checker 的 `doCheck()`
3. checker 可以：
   - `chain.doCheck(...)` — 继续下一个 checker
   - `proceedCallback.proceed(true)` — 结束检查，执行默认跳转
   - `proceedCallback.proceed(false)` — 结束检查，取消跳转
   - `jumpCallback.callback(param)` — 结束检查，自定义跳转

### 2.6 使用示例

```kotlin
@Router(routerPath = "webull://trade/order_detail")
class OrderDetailActivity : AppCompatActivity() {
    @Boom(index = 0, desc = "订单ID，格式为字符串")
    var orderId: String = ""
}

// 跳转
OrderDetailActivityLauncher.jump(context, "ORDER_123")

// 获取 scheme
val scheme = OrderDetailActivityLauncher.getActionScheme("ORDER_123")
// → "webull://trade/order_detail?ORDER_ID_INTENT_KEY=ORDER_123"
```

### 2.7 编译错误场景

| 错误 | 触发条件 | 错误信息 |
|------|----------|----------|
| 参数类型不支持 | 非 String 类型参数 | `目前JumpManager只支持String，待完善，跨模块间只能用基础数据结构传递` |
| 缺少描述 | 跨模块参数未填 desc | `跨模块需要添加 desc 的描述` |

---

## 功能三：Function 功能地图

### 3.1 功能描述

编译时收集所有标注 `@Function` 的类，生成统一的功能注册表 `FunctionFactory`，支持通过 ID 查找功能类、按分组获取功能列表。

### 3.2 注解说明

#### `@Function`（标注在类上）

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `functionId` | String | `""` | 功能 ID，为空时自动生成 `{类名}_function` |
| `desc` | String | 必填 | 功能描述 |
| `group` | String[] | `{}` | 所属分组，可属于多个分组 |

### 3.3 生成代码

生成 `com.webull.functionmap.FunctionFactory`：

```java
public final class FunctionFactory {
    // 每个功能的 ID 常量
    public static final String FUNCTION_ORDERDETAIL_ID = "order_detail_function";
    public static final String FUNCTION_TRADEHISTORY_ID = "trade_history";
    
    // ID → Class 映射缓存
    public static final Map<String, Class> functionCacheMap = new HashMap<>();
    
    // 初始化映射表
    public static void initFunction() {
        if (functionCacheMap.isEmpty()) {
            functionCacheMap.put(FUNCTION_ORDERDETAIL_ID, OrderDetail.class);
            functionCacheMap.put(FUNCTION_TRADEHISTORY_ID, TradeHistory.class);
        }
    }
    
    // 通过类名反查功能 ID
    public static String getFunctionId(String clsName) { ... }
    
    // 按分组获取功能列表
    public void initTradeFunction() { ... }
    public void initMarketFunction() { ... }
}
```

### 3.4 编译错误场景

| 错误 | 触发条件 | 错误信息 |
|------|----------|----------|
| ID 重复 | 多个类使用相同 functionId | `Found that the same FunctionId xxx corresponds to multiple different implementation classes` |

### 3.5 使用示例

```kotlin
@Function(functionId = "order_detail", desc = "订单详情", group = ["trade", "common"])
class OrderDetailActivity : AppCompatActivity() { ... }
```

---

## 功能四：MarketViewRoute 行情视图路由

### 4.1 功能描述

编译时收集所有标注 `@MarketViewRoute` 的 View 类，生成视图工厂 `MarketViewRouteFactory`，运行时通过 key 动态创建 View 实例。

### 4.2 注解说明

#### `@MarketViewRoute`（标注在类上）

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `key` | String | `""` | 视图唯一标识，为空时使用类的全限定名 |
| `desc` | String | `""` | 视图描述 |

### 4.3 生成代码

生成 `com.webull.market.common.base.MarketViewRouteFactory`：

```java
public final class MarketViewRouteFactory {
    // 每个 View 的 key 常量
    public static final String VIEW_KLINE = "kline";
    public static final String VIEW_DEPTH = "depth";
    
    // 通过 key 创建 View 实例
    @Nullable
    public static View createView(@NonNull Context context, @NonNull String key) {
        View view = null;
        switch (key) {
            case "kline":
                view = new KLineView(context);
                break;
            case "depth":
                view = new DepthView(context);
                break;
            default:
                break;
        }
        return view;
    }
}
```

### 4.4 编译错误场景

| 错误 | 触发条件 | 错误信息 |
|------|----------|----------|
| key 重复 | 多个 View 使用相同 key | 通过 `throwError` 回调报错 |

### 4.5 使用示例

```kotlin
@MarketViewRoute(key = "kline", desc = "K线图视图")
class KLineView(context: Context) : View(context) { ... }

// 运行时动态创建
val view = MarketViewRouteFactory.createView(context, "kline")
```


---

## 功能五：TradeInterface 交易服务工厂

### 5.1 功能描述

编译时收集所有标注 `@TradeInterface` 的实现类，生成服务工厂 `TradeInterfaceFactory{moduleName}`，运行时通过接口 Class 获取对应实现类实例。实现了编译时的服务定位器（Service Locator）模式。

### 5.2 注解说明

#### `@TradeInterface`（标注在实现类上）

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `value` | Class<?> | 必填 | 该实现类对应的接口类型 |
| `isInner` | boolean | `false` | 是否为内部接口（内部接口走单独的 fallback 方法） |

### 5.3 编译参数

需要通过 Gradle 传入 `module_name` 参数：
```groovy
javaCompileOptions {
    annotationProcessorOptions {
        arguments = [module_name: "Trade"]
    }
}
```

### 5.4 生成代码

生成 `com.webull.trade.services.TradeInterfaceFactory{moduleName}`：

```java
public class TradeInterfaceFactoryTrade implements ITradeInterfaceFactory {
    
    @Override
    public <T extends ITradeInterface> ITradeInterface createInstance(final Class<? extends T> clazz) {
        String className = clazz.getName();
        switch (className) {
            case "com.webull.trade.ITradeAccountInterface":
                return new com.webull.trade.impl.TradeAccountImpl();
            case "com.webull.trade.ITradeOrderInterface":
                return new com.webull.trade.impl.TradeOrderImpl();
            default:
                return createInnerInstance(clazz);
        }
    }
    
    private <T extends ITradeInterface> ITradeInterface createInnerInstance(final Class<? extends T> clazz) {
        String className = clazz.getName();
        switch (className) {
            case "com.webull.trade.inner.IInternalService":
                return new com.webull.trade.inner.InternalServiceImpl();
            default:
                return null;
        }
    }
}
```

### 5.5 使用示例

```java
@TradeInterface(value = ITradeAccountInterface.class)
public class TradeAccountImpl implements ITradeAccountInterface {
    // 实现接口方法...
}

@TradeInterface(value = IInternalService.class, isInner = true)
public class InternalServiceImpl implements IInternalService {
    // 内部接口实现...
}

// 运行时获取实现
ITradeAccountInterface service = factory.createInstance(ITradeAccountInterface.class);
```

---

## 功能六：TradeServiceMaker 聚合接口生成

### 6.1 功能描述

编译时扫描指定包下所有继承自 `baseInterface` 的接口，分析继承关系，找出顶层大接口（不被其他接口继承的接口），自动生成一个聚合接口继承所有顶层接口。

### 6.2 注解说明

#### `@TradeServiceMaker`（标注在标记接口上）

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `baseInterface` | Class<?> | 必填 | 基础接口类型，只有继承此接口的接口才会被收集 |
| `scanPackages` | String[] | `{}` | 要扫描的包名列表 |
| `additionalInterfaces` | Class<?>[] | `{}` | 额外需要继承的接口 |
| `packageName` | String | `""` | 生成接口的包名，为空使用注解所在类的包名 |
| `className` | String | `""` | 生成接口的类名，为空使用 `{类名}Generated` |

### 6.3 顶层接口筛选算法

```
给定包下所有继承 baseInterface 的接口集合 S：
对于 S 中的每个接口 A：
  如果 S 中不存在其他接口 B 使得 B 继承了 A，
  则 A 是顶层接口。
```

示例：
- `ITradeAccountInterface` 继承 `ITradeAccountInfoInterface` + `ITradeInterface`
- `ITradeAccountInfoInterface` 继承 `ITradeInterface`
- 结果：只保留 `ITradeAccountInterface`（大接口），排除 `ITradeAccountInfoInterface`（已被包含）

### 6.4 生成代码

```java
/**
 * 交易模块整体对外接口
 * 自动生成，继承所有 Trade 模块的顶层接口
 *
 * 继承的接口:
 *  - ITradeAccountInterface
 *  - ITradeOrderInterface
 *  - ITradePositionInterface
 *  - IService (additional)
 */
public interface ITradeManagerService extends
    ITradeAccountInterface,
    ITradeOrderInterface,
    ITradePositionInterface,
    IService {
}
```

### 6.5 使用示例

```java
@TradeServiceMaker(
    baseInterface = ITradeInterface.class,
    scanPackages = {"com.webull.commonmodule.trade.service.trade"},
    additionalInterfaces = {IService.class},
    packageName = "com.webull.commonmodule.trade.service",
    className = "ITradeManagerService"
)
interface TradeManagerServiceMarker {}
```

---

## 功能七：Mapper 对象映射

### 7.1 功能描述

类似 MapStruct 的编译时对象映射框架。为标注 `@Mapper` 的接口生成静态映射方法实现类，支持自动同名映射、显式映射、表达式映射、集合映射、嵌套对象映射、生命周期钩子等。

### 7.2 注解说明

#### `@Mapper`（标注在接口或抽象类上）

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `uses` | Class<?>[] | `{}` | 预留兼容属性 |
| `componentModel` | String | `""` | 预留兼容属性 |
| `implementationSuffix` | String | `"Impl"` | 生成实现类后缀 |

#### `@Mapping`（标注在方法上，可重复）

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `source` | String | `""` | 源属性路径，支持 `参数名.属性` 格式 |
| `target` | String | 必填 | 目标属性路径，支持嵌套如 `address.city` |
| `ignore` | boolean | `false` | 是否忽略该字段映射 |
| `constant` | String | `""` | 常量值，直接写入生成代码 |
| `expression` | String | `""` | Java 表达式，格式 `java(...)` |

#### `@MappingTarget`（标注在方法参数上）

标记更新目标对象的参数。有此注解的方法为"更新方法"，无此注解的方法为"创建方法"。

#### `@MappingConfig`（标注在类或方法上）

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `isNeedNullCheck` | boolean | `true` | 是否在赋值前进行空值检查 |

#### `@InheritConfiguration`（标注在方法上）

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `name` | String | 必填 | 要继承映射配置的方法名 |

#### `@BeforeMapping`（标注在方法上）

标记映射前执行的生命周期方法。方法必须有默认实现（default 方法）。

#### `@AfterMapping`（标注在方法上）

标记映射后执行的生命周期方法。方法必须有默认实现（default 方法）。

#### `@MappingIgnore`（标注在方法上）

标记不需要生成实现的辅助方法。这些方法通常用于 `expression` 中调用。

### 7.3 映射规则优先级

1. **显式 `@Mapping` 注解** — 最高优先级
2. **`@InheritConfiguration` 继承** — 继承的规则被当前方法的规则覆盖
3. **自动同名映射** — 源和目标有相同属性名且类型兼容时自动映射

### 7.4 方法类型

| 类型 | 特征 | 行为 |
|------|------|------|
| 创建方法 | 无 `@MappingTarget`，返回目标类型 | `new Target()` → 赋值 → `return target` |
| 更新方法 | 有 `@MappingTarget`，返回 void 或目标类型 | 直接在目标对象上赋值 |
| 集合互转方法 | 参数和返回值都是集合类型 | 循环调用元素映射方法 |

### 7.5 属性解析规则

**源对象读取（按优先级）：**
1. `getXxx()` 方法
2. `isXxx()` 方法（boolean 类型）
3. public 字段直接访问

**目标对象写入（按优先级）：**
1. `setXxx(value)` 方法
2. public 非 final 字段直接赋值（仅当无对应 setter 时）

### 7.6 类型兼容性处理

| 场景 | 处理方式 |
|------|----------|
| 类型完全相同 | 直接赋值 |
| 源类型可赋值给目标类型 | 直接赋值 |
| 集合类型不同但元素类型相同 | 生成 `new ArrayList<>(source)` 转换 |
| 集合元素类型不同 | 查找元素类型的映射方法，生成循环转换 |
| 普通类型不匹配 | 查找对应的映射方法，生成 `XxxImpl.mapMethod(source)` |
| 找不到映射方法 | 编译报错 |

### 7.7 集合映射代码生成

```java
// 源：List<OrderDto> → 目标：List<OrderEntity>
// 前提：存在 OrderEntity toEntity(OrderDto dto) 方法
if (source.getOrders() == null) {
    target.setOrders(null);
} else {
    ArrayList<OrderEntity> tempList = new ArrayList<>();
    for (int i = 0; i < source.getOrders().size(); i++) {
        tempList.add(OrderMapperImpl.toEntity(source.getOrders().get(i)));
    }
    target.setOrders(tempList);
}
```

### 7.8 空值检查行为

| `isNeedNullCheck` | 基本类型 | 引用类型 |
|-------------------|----------|----------|
| `true` | 不检查（基本类型不能为 null） | `if (source.getXxx() != null) { target.setXxx(source.getXxx()); }` |
| `false` | 直接赋值 | 直接赋值 |

方法级 `@MappingConfig` 覆盖类级配置。

### 7.9 Expression 表达式

格式：`java(表达式内容)`

```java
@Mapping(target = "fullName", expression = "java(source.getFirstName() + \" \" + source.getLastName())")
@Mapping(target = "ageGroup", expression = "java(source.getAge() > 18 ? \"ADULT\" : \"MINOR\")")
@Mapping(target = "formatted", expression = "java(formatDate(source.getDate()))")  // 调用 @MappingIgnore 方法
```

- `expression` 和 `source` 不能同时使用
- `expression` 和 `constant` 不能同时使用
- 表达式中可以引用方法参数名和 `@MappingIgnore` 方法

### 7.10 嵌套对象映射

支持 `target = "address.city"` 格式的嵌套路径：

```java
// 生成代码：
AddressEntity addressObj = target.getAddress();
if (addressObj == null) {
    addressObj = new AddressEntity();
}
addressObj.setCity(source.getAddress().getCity());
target.setAddress(addressObj);
```

### 7.11 生命周期方法

执行顺序：`@BeforeMapping` → 字段映射 → `@AfterMapping`

参数匹配规则：
- `@MappingTarget` 参数 → 匹配目标对象
- 非 `@MappingTarget` 参数 → 按类型匹配源对象
- 如果类型不匹配，跳过该生命周期方法（不报错）

### 7.12 Kotlin 源文件支持

- 通过 `kotlin.Metadata` 注解检测 Kotlin 源文件
- Kotlin 接口默认方法通过 `XxxMapper.DefaultImpls.method(null, params...)` 调用
- Java default 方法无法在静态方法中调用，生成空实现

### 7.13 生成代码特点

- **全静态方法** — 不实现接口，所有方法都是 `public static`
- **详细 Javadoc** — 列出所有映射详情（显式映射、自动映射、未映射字段、类型不匹配字段）
- **Source 空值检查** — 创建方法中，如果 source 为 null 直接返回 null
- **Target 非空校验** — 更新方法中，如果 @MappingTarget 为 null 抛出 IllegalArgumentException

### 7.14 使用示例

```kotlin
@Mapper
@MappingConfig(isNeedNullCheck = true)
interface OrderMapper {
    @Mapping(source = "userName", target = "name")
    @Mapping(target = "status", constant = "\"PENDING\"")
    @Mapping(target = "createTime", ignore = true)
    fun toEntity(dto: OrderDto): OrderEntity

    @InheritConfiguration(name = "toEntity")
    @Mapping(target = "updateTime", expression = "java(System.currentTimeMillis())")
    fun updateEntity(dto: OrderDto, @MappingTarget entity: OrderEntity)

    fun toEntityList(dtos: List<OrderDto>): List<OrderEntity>  // 自动集合互转

    @BeforeMapping
    fun beforeMapping(dto: OrderDto, @MappingTarget entity: OrderEntity) {
        entity.setVersion(entity.getVersion() + 1)
    }

    @MappingIgnore
    fun formatPrice(price: Double): String {
        return String.format("%.2f", price)
    }
}
```

生成 `OrderMapperImpl.java`：
```java
public final class OrderMapperImpl {
    public static OrderEntity toEntity(OrderDto dto) {
        if (dto == null) return null;
        OrderEntity target = new OrderEntity();
        OrderMapperImpl.beforeMapping(dto, target);
        if (dto.getUserName() != null) { target.setName(dto.getUserName()); }
        target.setStatus("PENDING");
        // createTime ignored
        // 自动映射同名字段...
        return target;
    }

    public static void updateEntity(OrderDto dto, OrderEntity entity) {
        if (entity == null) throw new IllegalArgumentException("...");
        // 继承 toEntity 的映射 + 覆盖
        entity.setUpdateTime(System.currentTimeMillis());
        // ...
    }

    public static List<OrderEntity> toEntityList(List<OrderDto> dtos) {
        if (dtos == null) return null;
        ArrayList<OrderEntity> tempList = new ArrayList<>();
        for (int i = 0; i < dtos.size(); i++) {
            tempList.add(OrderMapperImpl.toEntity(dtos.get(i)));
        }
        return tempList;
    }
}
```

### 7.15 编译错误场景

| 错误 | 触发条件 | 错误信息 |
|------|----------|----------|
| 非接口/抽象类 | @Mapper 标注在普通类上 | `@Mapper can only be applied to interface or abstract class` |
| 无映射方法 | 接口中没有抽象方法 | `No abstract mapping methods found inside @Mapper type` |
| 无源参数 | 方法没有非 @MappingTarget 参数 | `Mapper method must declare at least one source parameter` |
| 创建方法返回 void | 无 @MappingTarget 且返回 void | `Non update mapper method must return a target type` |
| target 为空 | @Mapping 的 target 为空 | `@Mapping target cannot be empty` |
| expression + source 冲突 | 同时指定 expression 和 source | `@Mapping cannot have both 'source' and 'expression' attributes` |
| 类型不匹配 | 源和目标类型不兼容且无映射方法 | `Type mismatch for property 'xxx'. Please add a mapping method` |
| 找不到属性 | 指定的 source/target 路径不存在 | `Cannot find getter or field for 'xxx'` |
| 循环继承 | @InheritConfiguration 形成环 | `Circular @InheritConfiguration detected` |
| 继承方法不存在 | @InheritConfiguration 引用不存在的方法 | `@InheritConfiguration refers to unknown method xxx` |
| @MappingTarget 多个 | 方法有多个 @MappingTarget 参数 | `Only one @MappingTarget parameter is supported` |

---

## 附录：全局编译行为

### 处理器执行顺序

1. 收集 `@Boom` / `@MakeResult` / `@Router` / `@ParentCls` 标注的类
2. 为每个类生成 Launcher 代码
3. 为 `@Router` 类额外生成路由代码
4. 收集 `@Function` / `@MarketViewRoute` 标注的类，生成工厂
5. 收集 `@TradeInterface` 标注的类，生成 TradeInterfaceFactory
6. 处理 `@TradeServiceMaker`，生成聚合接口
7. 处理 `@Mapper`，生成映射实现类

### 性能计时

每个处理阶段都有 `Timer` 计时，输出到编译日志：
```
[Performance] Total Processing took 1234ms
[Performance] Find Classes took 56ms
[Performance] Process Targets took 789ms
...
```

### 增量编译

- 类型：`AGGREGATING`（聚合模式）
- 含义：任何被注解的元素变更，都会触发所有注解元素的重新处理
- 原因：Function/MarketViewRoute/TradeInterface 等需要收集所有注解生成单一文件

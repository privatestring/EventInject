# 功能二：Router 路由系统

## 1. 功能概述

基于 URL Scheme 的跨模块路由跳转系统。编译时为每个 `@Router` 类生成路由注册代码和跳转方法，运行时通过 `JumpManager.jumpForRouter(context, scheme)` 统一分发跳转。支持路由拦截器链（`@RouterCheck`）实现跳转前的权限校验等逻辑。

---

## 2. 涉及源文件

### 注解定义（launcher-joke 模块）

| 文件 | 职责 |
|------|------|
| `launcher/Router.java` | 路由路径注解 |
| `launcher/RouterCheck.java` | 路由拦截器注解 |
| `launcher/IRouterChecker.java` | 拦截器接口 |
| `launcher/IRouterProceedCallback.java` | 继续/取消回调 |
| `launcher/IRouterJumpCallback.java` | 自定义跳转回调 |
| `launcher/RouterCheckerChain.java` | 责任链实现 |
| `launcher/JokeUtils.java` | URL 参数编码工具 |

### 处理器（launcher-compiler 模块）

| 文件 | 职责 |
|------|------|
| `launcher/codegeneration/RouterGeneration.kt` | Router 代码生成 |

---

## 3. 注解详细定义

### 3.1 `@Router`

```java
@Retention(RUNTIME)
@Target(TYPE)
public @interface Router {
    String routerPath();       // 路由路径，如 "webull://trade/order_detail"
    Class<?> cls() default Void.class;  // 目标类，默认使用当前注解类
}
```

### 3.2 `@RouterCheck`

```java
@Retention(RUNTIME)
@Target(TYPE)
public @interface RouterCheck {
    Class<? extends IRouterChecker>[] checkers();  // 拦截器类数组
    Class<?> cls() default Void.class;
}
```

---

## 4. 处理流程详解

### 4.1 触发条件

当一个类同时标注了 `@Router` 和 `@Boom` 字段时，`ClassBindingFactory` 会读取 `routerPath` 和 `cls`，在生成标准 Launcher 之后，额外生成 Router 版本的代码。

### 4.2 处理逻辑（ActivityLauncherProcessor.processTargets）

```
1. ClassBindingFactory.create() 读取 @Router 注解:
   - routerPath = typeElement.getAnnotation(Router::class.java)?.routerPath ?: ""
   - cls = 通过 MirroredTypeException 获取 @Router.cls 的 TypeMirror

2. 如果 routerPath 不为空:
   - 设置 classBinding.bindingClassName 为带 "_XXXxxx" 后缀的类名
   - 创建 RouterGeneration(classBinding)
   - 生成路由代码并写入 Filer
```

### 4.3 目标类解析

```kotlin
// cls 默认为 Void.class，此时使用注解所在类的全限定名
val clsPath = if (classBinding.cls.toString() == Void::class.java.name)
    "${bindingClassName.packageName()}.${bindingClassName.simpleName().replace("_XXXxxx", "")}"
else "${classBinding.cls}"
```

---

## 5. 生成代码详解

### 5.1 生成类结构

```java
public final class OrderDetailActivity_XXXxxx {
    // 路由路径常量
    public static final String ROUTER_ACTION = "webull://trade/order_detail";
    
    // 参数 key 常量（继承自 ClassGeneration）
    public static final String ORDER_ID_INTENT_KEY = "com.webull.trade.orderIdIntentKey";
    
    // bind 方法（简化版，调用标准 Launcher）
    public static void bind() { ActivityLauncher.bind(this, intent); }
    
    // 注册路由
    public static void putRouter() {
        router.WBRouter.putRouter(ROUTER_ACTION, "com.webull.trade.OrderDetailActivity");
    }
    
    // 跳转方法
    public static void jump(Context context, String orderId) { ... }
    
    // 带回调跳转
    public static void jump(Context context, String orderId, IRouterJumpCallback callback) { ... }
    
    // 获取完整 scheme
    public static String getActionScheme(String orderId) { ... }
}
```

### 5.2 `putRouter()` 方法

```java
public static void putRouter() {
    router.WBRouter.putRouter(ROUTER_ACTION, "com.webull.trade.OrderDetailActivity");
}
```

注册路由到全局路由表，应用启动时调用。

### 5.3 `jump(Context context, params...)` 方法

```java
public static void jump(Context context, String orderId) {
    JumpManager.jumpForRouter(context, getActionScheme(orderId));
}
```

### 5.4 `jump(Context context, params..., IRouterJumpCallback callback)` 方法

```java
public static void jump(Context context, String orderId, IRouterJumpCallback callback) {
    JumpManager.jumpForRouter(context, getActionScheme(orderId), callback);
}
```

### 5.5 `getActionScheme(params...)` 方法

无参数时：
```java
public static String getActionScheme() {
    return ROUTER_ACTION;
}
```

有参数时：
```java
public static String getActionScheme(String orderId) {
    StringBuilder sb = new StringBuilder();
    sb.append(ROUTER_ACTION);
    // 后续考虑是否支持多种基础类型
    sb.append(sb.indexOf("?") < 0 ? "?" : "&");
    // String
    sb.append(launcher.JokeUtils.addUrlParam(ORDER_ID_INTENT_KEY, orderId));
    return sb.toString();
}
```

生成结果示例：`webull://trade/order_detail?ORDER_ID_INTENT_KEY=ORDER_123`

---

## 6. URL 参数编码

```java
// JokeUtils.addUrlParam
public static String addUrlParam(String k, Object v) {
    String value = URLEncoder.encode(v.toString(), "UTF-8");
    if (k != null && value != null && !k.isEmpty()) {
        return k + "=" + value;
    }
    return "";
}
```

- 参数值通过 `URLEncoder.encode(value, "UTF-8")` 编码
- 第一个参数用 `?` 连接，后续用 `&` 连接
- key 使用生成的常量名（如 `ORDER_ID_INTENT_KEY`）

---

## 7. 路由拦截器机制

### 7.1 接口定义

```java
public interface IRouterChecker {
    void doCheck(
        RouterCheckerChain chain,           // 责任链引用
        Object context,                     // 上下文（通常是 Activity/Context）
        Map<String, String> params,         // 路由参数
        HashMap<String, String> extras,     // 额外参数
        IRouterProceedCallback proceedCallback,  // 继续/取消
        IRouterJumpCallback jumpCallback         // 自定义跳转
    );
    int priority();  // 优先级，数字越大越先执行
}

public interface IRouterProceedCallback {
    void proceed(boolean result);  // true=执行默认跳转, false=取消
}

public interface IRouterJumpCallback {
    Object callback(Object callbackParam);  // 自定义跳转逻辑
}
```

### 7.2 责任链执行流程（RouterCheckerChain）

```java
public class RouterCheckerChain implements IRouterChecker {
    private List<IRouterChecker> checkers = new ArrayList<>();
    private int index = 0;

    public void addChecker(Class<IRouterChecker> checkerClass) {
        // 反射创建实例
        IRouterChecker checker = checkerClass.getConstructor().newInstance();
        checkers.add(checker);
        sortCheckers();  // 按 priority 升序排列
    }

    @Override
    public void doCheck(chain, context, params, extras, proceedCallback, callback) {
        // 入参为空，不继续
        if (context == null || params == null || checkers == null || checkers.isEmpty()) {
            proceedCallback.proceed(false);
            return;
        }
        // 所有 checker 都执行完了
        if (index >= checkers.size()) {
            proceedCallback.proceed(true);  // 默认通过
            return;
        }
        // 取下一个 checker 执行
        IRouterChecker checker = checkers.get(index);
        index++;
        checker.doCheck(this, context, params, extras, proceedCallback, callback);
    }
}
```

### 7.3 Checker 的三种处理方式

| 方式 | 调用 | 效果 |
|------|------|------|
| 继续检查 | `chain.doCheck(chain, context, params, extras, proceedCallback, jumpCallback)` | 执行下一个 checker |
| 默认跳转 | `proceedCallback.proceed(true)` | 结束所有检查，执行默认跳转 |
| 取消跳转 | `proceedCallback.proceed(false)` | 结束所有检查，不跳转 |
| 自定义跳转 | `jumpCallback.callback(param)` | 结束所有检查，执行自定义逻辑 |

---

## 8. 参数限制

### 8.1 当前仅支持 String 类型

```kotlin
val supportType = arrayListOf(String::class.java.name)
if (!supportType.contains(typeNameStr)) {
    throw IllegalArgumentException("目前JumpManager只支持String，待完善，跨模块间只能用基础数据结构传递")
}
```

非 String 类型参数会导致编译失败。

### 8.2 desc 必填

跨模块使用 Router 时，所有 `@Boom` 参数必须填写 `desc`：

```kotlin
if (it.desc.isEmpty()) {
    throw IllegalArgumentException("${classBinding.targetTypeName} ${it.name} 跨模块需要添加 desc 的描述")
}
```

### 8.3 引用类型空值保护

```kotlin
if (arg.typeName.checkNotBox().not()) {
    beginControlFlow("if(${arg.name} != null)")
}
// ... 拼接参数
if (arg.typeName.checkNotBox().not()) {
    endControlFlow()
}
```

---

## 9. 编译错误清单

| 错误 | 触发条件 | 错误信息 |
|------|----------|----------|
| 参数类型不支持 | @Boom 字段非 String 类型 | `目前JumpManager只支持String，待完善，跨模块间只能用基础数据结构传递` |
| 缺少描述 | @Boom 字段未填 desc | `{类名} {字段名} 跨模块需要添加 desc 的描述` |

---

## 10. 生成的 Javadoc

Router 方法会生成参数文档：

```java
/**
 * @param orderId 订单ID，格式为字符串
 * @param fromPage 来源页面标识
 */
public static void jump(Context context, String orderId, String fromPage) { ... }
```

---

## 11. 使用示例

```kotlin
// 定义
@Router(routerPath = "webull://trade/order_detail")
class OrderDetailActivity : AppCompatActivity() {
    @Boom(index = 0, desc = "订单ID")
    var orderId: String = ""

    @Boom(index = 1, isOptional = true, desc = "来源页面")
    var fromPage: String? = null
}

// 跳转（其他模块）
OrderDetailActivity_XXXxxx.jump(context, "ORDER_123")
OrderDetailActivity_XXXxxx.jump(context, "ORDER_123", "home")

// 获取 scheme
val scheme = OrderDetailActivity_XXXxxx.getActionScheme("ORDER_123")
// → "webull://trade/order_detail?ORDER_ID_INTENT_KEY=ORDER_123"

// 注册路由（应用启动时）
OrderDetailActivity_XXXxxx.putRouter()
```

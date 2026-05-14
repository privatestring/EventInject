# 功能四：MarketViewRoute 行情视图路由

## 1. 功能概述

编译时收集所有标注 `@MarketViewRoute` 的 View 类，生成视图工厂 `MarketViewRouteFactory`。运行时通过 key 字符串动态创建对应的 View 实例，实现行情页面 View 的解耦和动态加载。

---

## 2. 涉及源文件

### 注解定义（launcher-joke 模块）

| 文件 | 职责 |
|------|------|
| `launcher/MarketViewRoute.java` | 视图路由注解 |

### 处理器（launcher-compiler 模块）

| 文件 | 职责 |
|------|------|
| `launcher/ActivityLauncherProcessor.kt` | 入口，`processFunction()` 中处理 |
| `launcher/codegeneration/MarketViewRouteGeneration.kt` | 代码生成 |

---

## 3. 注解详细定义

```java
public @interface MarketViewRoute {
    String key() default "";    // 视图唯一标识，为空时使用类的全限定名
    String desc() default "";   // 视图描述
}
```

---

## 4. 处理流程详解

### 4.1 收集阶段

在 `processFunction()` 中与 `@Function` 一起收集：

```kotlin
val allView = mutableListOf<TypeElement>()
for (classToProcess in classesToProcess) {
    val viewAno = classToProcess.getAnnotation(MarketViewRoute::class.java)
    if (viewAno != null) {
        allView.add(classToProcess)
    }
}
```

### 4.2 生成阶段

```kotlin
if (allView.isNotEmpty()) {
    MarketViewRouteGeneration(allView).brewJava().writeTo(filer)
}
```

---

## 5. 生成代码详解

### 5.1 生成类

**包名：** `com.webull.market.common.base`
**类名：** `MarketViewRouteFactory`

### 5.2 完整生成结构

```java
/**
 * 市场 View 映射
 */
public final class MarketViewRouteFactory {
    
    // ===== 1. 每个 View 的 key 常量 =====
    /** K线图视图 */
    public static final String VIEW_KLINE = "kline";
    /** 深度图视图 */
    public static final String VIEW_DEPTH = "depth";
    /** 分时图 */
    public static final String VIEW_TIMELINE = "timeline";
    
    // ===== 2. 工厂方法 =====
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
            case "timeline":
                view = new TimelineView(context);
                break;
            default:
                break;
        }
        return view;
    }
}
```

### 5.3 Key 常量命名规则

```
VIEW_{key值大写}
```

| key 值 | 常量名 |
|--------|--------|
| `kline` | `VIEW_KLINE` |
| `depth` | `VIEW_DEPTH` |
| `com.webull.market.TimelineView` | `VIEW_COM.WEBULL.MARKET.TIMELINEVIEW` |

如果 `@MarketViewRoute(key = "")` 为空，使用类的 `canonicalName` 作为 key。

### 5.4 Key 值规则

- 如果 `@MarketViewRoute(key = "kline")` 指定了 key → 使用指定值
- 如果 `key` 为空 → 使用 `ClassName.get(element).canonicalName()`

---

## 6. 重复 Key 检测

```kotlin
val allViewKeys = mutableListOf<String>()
all.forEach {
    val anno = it.getAnnotation(MarketViewRoute::class.java)
    if (allViewKeys.contains(anno.key)) {
        throwError?.invoke(anno.key, it)  // 编译报错
    }
    allViewKeys.add(anno.key)
}
```

---

## 7. 方法签名详解

```java
@Nullable
public static View createView(@NonNull Context context, @NonNull String key)
```

- 参数 `context`：带 `@NonNull` 注解
- 参数 `key`：带 `@NonNull` 注解
- 返回值：带 `@Nullable` 注解（key 不匹配时返回 null）

---

## 8. 编译错误清单

| 错误 | 触发条件 | 错误信息 |
|------|----------|----------|
| key 重复 | 多个 View 使用相同 key | 通过 `throwError` 回调报错 |

---

## 9. 使用示例

### 定义

```kotlin
@MarketViewRoute(key = "kline", desc = "K线图视图")
class KLineView(context: Context) : FrameLayout(context) {
    // K线图实现...
}

@MarketViewRoute(key = "depth", desc = "深度图视图")
class DepthView(context: Context) : FrameLayout(context) {
    // 深度图实现...
}

@MarketViewRoute(key = "timeline", desc = "分时图")
class TimelineView(context: Context) : FrameLayout(context) {
    // 分时图实现...
}
```

### 使用

```kotlin
// 动态创建 View
val klineView = MarketViewRouteFactory.createView(context, "kline")
val depthView = MarketViewRouteFactory.createView(context, MarketViewRouteFactory.VIEW_DEPTH)

// 添加到容器
container.addView(klineView)

// 使用常量引用
val key = MarketViewRouteFactory.VIEW_KLINE  // "kline"
```

---

## 10. 生成文件位置

- 包名：`com.webull.market.common.base`
- 文件名：`MarketViewRouteFactory.java`

# 功能三：Function 功能地图

## 1. 功能概述

编译时收集所有标注 `@Function` 的类，生成统一的功能注册表 `FunctionFactory`。支持通过功能 ID 查找对应的 Class、按分组获取功能列表。用于功能开关、A/B 测试、动态功能注册等场景。

---

## 2. 涉及源文件

### 注解定义（launcher-joke 模块）

| 文件 | 职责 |
|------|------|
| `launcher/Function.java` | 功能注解 |

### 处理器（launcher-compiler 模块）

| 文件 | 职责 |
|------|------|
| `launcher/ActivityLauncherProcessor.kt` | 入口，`processFunction()` 方法 |
| `launcher/codegeneration/FunctionGeneration.kt` | 代码生成 |

---

## 3. 注解详细定义

```java
public @interface Function {
    String functionId() default "";   // 功能 ID，为空时自动生成 "{类名}_function"
    String desc();                    // 功能描述（必填）
    String[] group() default {};      // 所属分组，可属于多个分组
}
```

---

## 4. 处理流程详解

### 4.1 收集阶段（ActivityLauncherProcessor.process）

```kotlin
processFunction(mutableSetOf<TypeElement>().apply {
    processAnnotation<Function>(env) { element ->
        add(element as TypeElement)  // @Function 标注在类上
    }
    processAnnotation<MarketViewRoute>(env) { element ->
        add(element as TypeElement)  // @MarketViewRoute 也参与
    }
})
```

### 4.2 生成阶段（processFunction）

```kotlin
private fun processFunction(classesToProcess: Set<TypeElement>) {
    val allFunction = mutableListOf<TypeElement>()
    val allView = mutableListOf<TypeElement>()
    val allFunctionGroup = mutableListOf<String>()

    for (classToProcess in classesToProcess) {
        val functionAno = classToProcess.getAnnotation(Function::class.java)
        if (functionAno != null) {
            allFunction.add(classToProcess)
            allFunctionGroup.addAll(functionAno.group)  // 收集所有分组
        }
        val viewAno = classToProcess.getAnnotation(MarketViewRoute::class.java)
        if (viewAno != null) {
            allView.add(classToProcess)
        }
    }

    // 生成 FunctionFactory
    if (allFunction.isNotEmpty()) {
        FunctionGeneration(allFunction, allFunctionGroup.distinct().filter { it.isNotEmpty() })
            .apply { throwError = { id, ele -> error(...) } }
            .brewJava()
            .writeTo(filer)
    }
}
```

---

## 5. 生成代码详解

### 5.1 生成类

**包名：** `com.webull.functionmap`
**类名：** `FunctionFactory`

### 5.2 完整生成结构

```java
public final class FunctionFactory {
    
    // ===== 1. 每个功能的 ID 常量 =====
    /** 订单详情 */
    public static final String FUNCTION_ORDERDETAILACTIVITY_ID = "order_detail";
    /** 交易历史 */
    public static final String FUNCTION_TRADEHISTORYACTIVITY_ID = "TradeHistoryActivity_function";
    
    // ===== 2. ID → Class 映射缓存 =====
    public static final Map<String, Class> functionCacheMap = new HashMap<>();
    
    // ===== 3. 初始化方法 =====
    public static void initFunction() {
        if (functionCacheMap.isEmpty()) {
            functionCacheMap.put(FUNCTION_ORDERDETAILACTIVITY_ID, com.webull.trade.OrderDetailActivity.class);
            functionCacheMap.put(FUNCTION_TRADEHISTORYACTIVITY_ID, com.webull.trade.TradeHistoryActivity.class);
        }
    }
    
    // ===== 4. 通过类名反查功能 ID =====
    public static String getFunctionId(String clsName) {
        String functionId = "";
        if (functionCacheMap.isEmpty()) {
            initFunction();
        }
        for (String s : functionCacheMap.keySet()) {
            Class clszz = functionCacheMap.get(s);
            if (clszz.getCanonicalName().contains(clsName)) {
                functionId = s;
                break;
            }
        }
        return functionId;
    }
    
    // ===== 5. 按分组获取功能列表 =====
    public void initTradeFunction() {
        List<Class> result = new ArrayList<>();
        result.add(order_detail);  // 属于 trade 分组的功能
        // ...
    }
    
    public void initMarketFunction() {
        List<Class> result = new ArrayList<>();
        // 属于 market 分组的功能
    }
}
```

### 5.3 ID 常量命名规则

```
FUNCTION_{类简单名大写}_ID
```

| 类名 | 常量名 |
|------|--------|
| `OrderDetailActivity` | `FUNCTION_ORDERDETAILACTIVITY_ID` |
| `TradeHistory` | `FUNCTION_TRADEHISTORY_ID` |

### 5.4 ID 值规则

- 如果 `@Function(functionId = "order_detail")` 指定了 ID → 使用指定值
- 如果 `functionId` 为空 → 自动生成 `"{类简单名}_function"`

---

## 6. 重复 ID 检测

```kotlin
val allFunction = mutableListOf<String>()
all.forEach { element ->
    val id = ani.functionId.ifEmpty { clazzName.plus("_function") }
    if (allFunction.contains(id)) {
        throwError?.invoke(id, element)  // 编译报错
    }
    allFunction.add(id)
}
```

---

## 7. 编译错误清单

| 错误 | 触发条件 | 错误信息 |
|------|----------|----------|
| ID 重复 | 多个类使用相同 functionId | `Found that the same FunctionId {id} corresponds to multiple different implementation classes` |

---

## 8. 使用示例

### 定义

```kotlin
@Function(functionId = "order_detail", desc = "订单详情页", group = ["trade", "common"])
class OrderDetailActivity : AppCompatActivity() { ... }

@Function(desc = "交易历史", group = ["trade"])
class TradeHistoryActivity : AppCompatActivity() { ... }

@Function(functionId = "market_kline", desc = "K线图", group = ["market"])
class KLineActivity : AppCompatActivity() { ... }
```

### 使用

```kotlin
// 初始化（应用启动时）
FunctionFactory.initFunction()

// 通过类名查找功能 ID
val id = FunctionFactory.getFunctionId("OrderDetailActivity")
// → "order_detail"

// 通过 ID 获取 Class
val clazz = FunctionFactory.functionCacheMap["order_detail"]
// → OrderDetailActivity.class

// 获取某分组的所有功能
FunctionFactory.initTradeFunction()
```

---

## 9. 生成文件位置

- 包名：`com.webull.functionmap`
- 文件名：`FunctionFactory.java`

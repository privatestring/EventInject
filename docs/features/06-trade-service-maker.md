# 功能六：TradeServiceMaker 聚合接口生成

## 1. 功能概述

编译时扫描指定包下所有继承自 `baseInterface` 的接口，分析继承关系，找出顶层大接口（不被其他接口继承的接口），自动生成一个聚合接口继承所有顶层接口。用于自动维护交易模块的总服务接口，避免手动维护大量接口继承关系。

---

## 2. 涉及源文件

### 注解定义（launcher-joke 模块）

| 文件 | 职责 |
|------|------|
| `launcher/TradeServiceMaker.java` | 聚合接口生成注解 |

### 处理器（launcher-compiler 模块）

| 文件 | 职责 |
|------|------|
| `launcher/ActivityLauncherProcessor.kt` | 入口，调用 `ServiceUtil.processTradeServiceAggregator()` |
| `launcher/service/ServiceUtil.kt` | 处理逻辑入口 + 注解值获取 |
| `launcher/codegeneration/TradeServiceAggregatorGeneration.kt` | 核心逻辑：包扫描、继承分析、代码生成 |

---

## 3. 注解详细定义

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface TradeServiceMaker {
    Class<?> baseInterface();                  // 基础接口类型（必填）
    String[] scanPackages() default {};        // 要扫描的包名列表
    Class<?>[] additionalInterfaces() default {};  // 额外需要继承的接口
    String packageName() default "";           // 生成接口的包名（为空用注解所在类的包名）
    String className() default "";             // 生成接口的类名（为空用 "{类名}Generated"）
}
```

---

## 4. 处理流程详解

### 4.1 入口（ServiceUtil.processTradeServiceAggregator）

```kotlin
fun processTradeServiceAggregator(env: RoundEnvironment, processingEnv: ProcessingEnvironment, filer: Filer) {
    for (element in env.getElementsAnnotatedWith(TradeServiceMaker::class.java)) {
        if (element is TypeElement) {
            val annotation = element.getAnnotation(TradeServiceMaker::class.java)

            // 1. 获取 baseInterface（通过 MirroredTypeException）
            val baseInterfaceType = getBaseInterfaceType(element)

            // 2. 获取 scanPackages
            val scanPackages = annotation.scanPackages.toList()

            // 3. 获取 additionalInterfaces（通过 MirroredTypesException）
            val additionalInterfaceTypes = getAdditionalInterfaceTypes(element)

            // 4. 确定包名和类名
            val packageName = annotation.packageName.ifEmpty {
                processingEnv.elementUtils.getPackageOf(element).qualifiedName.toString()
            }
            val className = annotation.className.ifEmpty {
                element.simpleName.toString() + "Generated"
            }

            // 5. 生成代码
            TradeServiceAggregatorGeneration(
                processingEnv, env,
                baseInterfaceType, scanPackages, additionalInterfaceTypes,
                packageName, className
            ).brewJava().writeTo(filer)
        }
    }
}
```

### 4.2 注解中 Class 值获取

```kotlin
// 单个 Class 属性
fun getBaseInterfaceType(element: TypeElement): TypeMirror? {
    try {
        element.getAnnotation(TradeServiceMaker::class.java).baseInterface
    } catch (mte: MirroredTypeException) {
        return mte.typeMirror
    }
    return null
}

// Class[] 数组属性
fun getAdditionalInterfaceTypes(element: TypeElement): List<TypeMirror> {
    try {
        element.getAnnotation(TradeServiceMaker::class.java).additionalInterfaces
    } catch (mte: MirroredTypesException) {
        return mte.typeMirrors  // 注意是 MirroredTypesException（复数）
    }
    return emptyList()
}
```

---

## 5. 核心算法：顶层接口筛选

### 5.1 整体流程

```
1. findAllSubInterfaces(baseElement):
   - 遍历 roundEnv.rootElements，递归查找所有类型元素
   - 遍历 scanPackages 中的包元素
   - 过滤出：是接口 + 包名匹配 + 继承自 baseInterface + 不是 baseInterface 自身
   - 去重

2. filterTopLevelInterfaces(allInterfaces):
   - 对于每个候选接口 A：
     - 检查是否存在其他接口 B 继承了 A
     - 如果没有 → A 是顶层接口
     - 如果有 → A 不是顶层接口（已被 B 包含）
```

### 5.2 包扫描策略

**方法一：遍历 rootElements**
```kotlin
for (element in roundEnv.rootElements) {
    collectInterfacesFromElement(element, packagePrefixes, baseElement, result)
}
```

递归遍历所有编译时可见的类型元素，检查包名是否匹配。

**方法二：通过包名直接查找（补充）**
```kotlin
for (pkgPrefix in packagePrefixes) {
    val packageElement = elementUtils.getPackageElement(pkgPrefix)
    if (packageElement != null) {
        for (enclosed in packageElement.enclosedElements) {
            // 检查是否是接口且继承自 baseInterface
        }
    }
}
```

### 5.3 包名匹配规则

```kotlin
val matches = packagePrefixes.any { prefix ->
    packageName == prefix || packageName.startsWith("$prefix.")
}
```

支持精确匹配和子包匹配。例如 `scanPackages = ["com.webull.trade.service"]` 会匹配：
- `com.webull.trade.service`（精确）
- `com.webull.trade.service.account`（子包）
- `com.webull.trade.service.order.detail`（深层子包）

### 5.4 继承关系判断

```kotlin
private fun isSubtypeOfInterface(element: TypeElement, baseElement: TypeElement): Boolean {
    val elementType = element.asType()
    val baseType = baseElement.asType()
    return typeUtils.isSubtype(typeUtils.erasure(elementType), typeUtils.erasure(baseType))
}
```

### 5.5 顶层接口筛选算法

```kotlin
private fun filterTopLevelInterfaces(allInterfaces: List<TypeElement>): List<TypeMirror> {
    val result = mutableListOf<TypeMirror>()

    for (candidate in allInterfaces) {
        var isTopLevel = true
        for (other in allInterfaces) {
            if (candidate == other) continue
            // 如果 other 继承了 candidate，则 candidate 不是顶层
            if (extendsInterface(other, candidate)) {
                isTopLevel = false
                break
            }
        }
        if (isTopLevel) {
            result.add(candidate.asType())
        }
    }
    return result
}

private fun extendsInterface(subInterface: TypeElement, superInterface: TypeElement): Boolean {
    for (parentType in subInterface.interfaces) {
        val parentElement = typeUtils.asElement(parentType) as? TypeElement ?: continue
        if (parentElement.qualifiedName == superInterface.qualifiedName) return true
        if (extendsInterface(parentElement, superInterface)) return true  // 递归
    }
    return false
}
```

### 5.6 算法示例

假设包下有以下接口：
```
ITradeInterface (base)
├── ITradeAccountInfoInterface
├── ITradeAccountInterface (extends ITradeAccountInfoInterface + ITradeInterface)
├── ITradeOrderInterface (extends ITradeInterface)
└── ITradePositionInterface (extends ITradeInterface)
```

筛选过程：
- `ITradeAccountInfoInterface`：被 `ITradeAccountInterface` 继承 → **不是顶层**
- `ITradeAccountInterface`：没有被其他接口继承 → **是顶层** ✓
- `ITradeOrderInterface`：没有被其他接口继承 → **是顶层** ✓
- `ITradePositionInterface`：没有被其他接口继承 → **是顶层** ✓

结果：生成的聚合接口继承 `ITradeAccountInterface`, `ITradeOrderInterface`, `ITradePositionInterface`

---

## 6. 生成代码详解

### 6.1 生成结构

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

### 6.2 排序规则

顶层接口按简单名字母顺序排序（`sortedBy { getSimpleName(it) }`），确保生成代码稳定。

### 6.3 Javadoc 内容

- 标注每个继承的接口名称
- `additionalInterfaces` 标注 `(additional)` 后缀

---

## 7. 编译日志

处理过程中输出详细日志（`Diagnostic.Kind.NOTE`）：

```
[Performance] Process Trade Service Aggregator took 123ms
TradeServiceAggregator: Scanning packages: [com.webull.trade.service]
TradeServiceAggregator: Processed 456 root elements, found 12 interfaces so far
TradeServiceAggregator: Found package element for com.webull.trade.service
TradeServiceAggregator: Found interface ITradeAccountInterface in package com.webull.trade.service
TradeServiceAggregator: Added interface ITradeAccountInterface
TradeServiceAggregator: Total interfaces found: 12
```

---

## 8. 编译错误清单

| 错误 | 触发条件 | 错误信息 |
|------|----------|----------|
| baseInterface 为空 | 注解未指定 baseInterface | `TradeServiceAggregator: baseInterface is required` |
| scanPackages 为空 | 未指定扫描包 | `TradeServiceAggregator: scanPackages is empty`（WARNING） |
| 包不存在 | 指定的包名找不到 | `TradeServiceAggregator: Package element not found for {pkg}`（WARNING） |
| 处理异常 | 代码生成过程出错 | `Error processing TradeServiceAggregator annotation: {message}` |

---

## 9. 使用示例

### 定义

```java
@TradeServiceMaker(
    baseInterface = ITradeInterface.class,
    scanPackages = {
        "com.webull.commonmodule.trade.service.trade",
        "com.webull.commonmodule.trade.service.account"
    },
    additionalInterfaces = {IService.class},
    packageName = "com.webull.commonmodule.trade.service",
    className = "ITradeManagerService"
)
interface TradeManagerServiceMarker {}
```

### 生成结果

```java
// com.webull.commonmodule.trade.service.ITradeManagerService.java
public interface ITradeManagerService extends
    ITradeAccountInterface,
    ITradeOrderInterface,
    ITradePositionInterface,
    IService {
}
```

### 使用

```kotlin
// 业务代码中使用聚合接口
class TradeManager : ITradeManagerService {
    // 需要实现所有顶层接口的方法
}
```

---

## 10. 生成文件位置

- 包名：由 `@TradeServiceMaker.packageName` 指定
- 文件名：由 `@TradeServiceMaker.className` 指定 + `.java`

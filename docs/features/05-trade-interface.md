# 功能五：TradeInterface 交易服务工厂

## 1. 功能概述

编译时收集所有标注 `@TradeInterface` 的实现类，生成服务工厂 `TradeInterfaceFactory{moduleName}`。运行时通过接口 Class 获取对应实现类实例，实现编译时的服务定位器（Service Locator）模式，避免运行时反射。

---

## 2. 涉及源文件

### 注解定义（launcher-joke 模块）

| 文件 | 职责 |
|------|------|
| `launcher/TradeInterface.java` | 服务实现标记注解 |

### 处理器（launcher-compiler 模块）

| 文件 | 职责 |
|------|------|
| `launcher/ActivityLauncherProcessor.kt` | 入口，`processTradeService()` 方法 |
| `launcher/codegeneration/TradeInterfaceGeneration.kt` | 代码生成 |
| `launcher/service/ServiceUtil.kt` | 注解值获取工具 |

---

## 3. 注解详细定义

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface TradeInterface {
    Class<?> value();              // 该实现类对应的接口类型（必填）
    boolean isInner() default false;  // 是否为内部接口
}
```

---

## 4. 编译参数

需要通过 Gradle 传入 `module_name`：

```groovy
android {
    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = [module_name: "Trade"]
            }
        }
    }
}
```

处理器中获取：
```kotlin
val moduleName = processingEnv.options["module_name"] ?: return
```

如果未配置 `module_name`，该子系统不执行。

---

## 5. 处理流程详解

### 5.1 收集阶段

```kotlin
private fun processTradeService(env: RoundEnvironment) {
    val moduleName = processingEnv.options["module_name"] ?: return
    val interfaceMap = mutableMapOf<String, String>()      // 普通接口
    val innerInterfaceMap = mutableMapOf<String, String>()  // 内部接口

    for (element in env.getElementsAnnotatedWith(TradeInterface::class.java)) {
        if (element is TypeElement) {
            val implClassName = element.qualifiedName.toString()
            // 通过 MirroredTypeException 获取注解中的 Class 值
            val interfaceType = ServiceUtil.getAnnotationInterfaceType(element)
            if (interfaceType != null) {
                val interfaceClassName = interfaceType.toString()
                val isInner = element.getAnnotation(TradeInterface::class.java).isInner

                if (isInner) {
                    innerInterfaceMap[interfaceClassName] = implClassName
                } else {
                    interfaceMap[interfaceClassName] = implClassName
                }
            }
        }
    }

    // 生成代码
    if (interfaceMap.isNotEmpty() || innerInterfaceMap.isNotEmpty()) {
        TradeInterfaceGeneration(interfaceMap, innerInterfaceMap, moduleName)
            .brewJava()
            .writeTo(filer)
    }
}
```

### 5.2 注解中 Class 值获取（MirroredTypeException 技巧）

```kotlin
// ServiceUtil.getAnnotationInterfaceType
fun getAnnotationInterfaceType(element: TypeElement): TypeMirror? {
    try {
        element.getAnnotation(TradeInterface::class.java).value
    } catch (mte: MirroredTypeException) {
        return mte.typeMirror
    }
    return null
}
```

这是 KAPT 中获取注解 `Class<?>` 属性的标准技巧：访问 Class 属性时会抛出 `MirroredTypeException`，从中获取 `TypeMirror`。

---

## 6. 生成代码详解

### 6.1 生成类

**包名：** `com.webull.trade.services`
**类名：** `TradeInterfaceFactory{moduleName}`（如 `TradeInterfaceFactoryTrade`）

### 6.2 完整生成结构

```java
/**
 * 自动生成的TradeInterfaceFactory类
 * 由TradeInterface注解处理器生成
 */
public class TradeInterfaceFactoryTrade implements ITradeInterfaceFactory {

    @Override
    public <T extends ITradeInterface> ITradeInterface createInstance(final Class<? extends T> clazz) {
        String className = clazz.getName();

        switch (className) {
            case "com.webull.trade.service.ITradeAccountInterface":
                return new com.webull.trade.impl.TradeAccountImpl();
            case "com.webull.trade.service.ITradeOrderInterface":
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

### 6.3 方法说明

| 方法 | 可见性 | 说明 |
|------|--------|------|
| `createInstance(Class<? extends T> clazz)` | public | 主入口，先查普通接口，找不到则 fallback 到内部接口 |
| `createInnerInstance(Class<? extends T> clazz)` | private | 内部接口查找，找不到返回 null |

### 6.4 实现的接口

```java
// 位于 com.webull.commonmodule.trade.service.trade.base 包
public interface ITradeInterfaceFactory {
    <T extends ITradeInterface> ITradeInterface createInstance(Class<? extends T> clazz);
}
```

---

## 7. isInner 的作用

- `isInner = false`（默认）：接口映射放入 `createInstance` 的 switch-case
- `isInner = true`：接口映射放入 `createInnerInstance` 的 switch-case

设计意图：普通接口找不到时，fallback 到内部接口查找。内部接口通常是模块内部使用的服务，不对外暴露。

---

## 8. 编译错误清单

| 错误 | 触发条件 | 错误信息 |
|------|----------|----------|
| 处理异常 | 注解处理过程中出错 | `Error processing TradeService annotation: {message}` |
| 缺少 module_name | 未配置编译参数 | 静默跳过（不生成代码） |

---

## 9. 使用示例

### 定义

```java
// 接口定义
public interface ITradeAccountInterface extends ITradeInterface {
    AccountInfo getAccountInfo();
    void updateAccount(AccountInfo info);
}

// 实现类标注
@TradeInterface(value = ITradeAccountInterface.class)
public class TradeAccountImpl implements ITradeAccountInterface {
    @Override
    public AccountInfo getAccountInfo() { ... }
    @Override
    public void updateAccount(AccountInfo info) { ... }
}

// 内部接口
@TradeInterface(value = IInternalService.class, isInner = true)
public class InternalServiceImpl implements IInternalService { ... }
```

### 使用

```kotlin
// 获取工厂实例（通常通过 DI 或 ServiceLoader）
val factory: ITradeInterfaceFactory = TradeInterfaceFactoryTrade()

// 通过接口获取实现
val accountService = factory.createInstance(ITradeAccountInterface::class.java) as ITradeAccountInterface
accountService.getAccountInfo()
```

---

## 10. 生成文件位置

- 包名：`com.webull.trade.services`
- 文件名：`TradeInterfaceFactory{moduleName}.java`

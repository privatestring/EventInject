# 功能五：TradeInterface 迁移文档

## 迁移概述

将 `TradeInterface` 注解处理从 KAPT（JavaPoet 生成 Java）迁移到 KSP（KotlinPoet 生成 Kotlin）。

| 项目 | KAPT 版本 | KSP 版本 |
|------|-----------|----------|
| 处理器 | `launcher-compiler` | `launcher-wb-compiler-ksp` |
| 代码生成 | JavaPoet → Java class | KotlinPoet → Kotlin object |
| 注解 Class 获取 | `MirroredTypeException` 技巧 | KSP 直接返回 `KSType` |
| 生成产物 | `TradeInterfaceFactory{Module}.java` | `TradeInterfaceFactory{Module}.kt` |

---

## 新增文件

| 文件 | 说明 |
|------|------|
| `launcher-wb-compiler-ksp/src/main/kotlin/launcher/wb/codegeneration/TradeInterfaceGeneration.kt` | KSP 代码生成器 |

## 修改文件

| 文件 | 变更 |
|------|------|
| `launcher-wb-compiler-ksp/src/main/kotlin/launcher/wb/WbKspProcessor.kt` | 注册 TradeInterfaceGeneration |
| `app/build.gradle` | 添加 `ksp { arg("module_name", "TradeAccount") }` |

## 测试文件

| 文件 | 说明 |
|------|------|
| `app/src/main/java/com/joker/event/tradeinterface/ITradeInterface.kt` | 基础接口 |
| `app/src/main/java/com/joker/event/tradeinterface/ITradeInterfaceFactory.kt` | 工厂接口 |
| `app/src/main/java/com/joker/event/tradeinterface/TradeInterfaceTestCases.kt` | 27 个测试用例（接口 + 实现类） |

---

## 关键技术差异

### 注解中 KClass 值的获取

**KAPT（MirroredTypeException 技巧）：**
```kotlin
fun getAnnotationInterfaceType(element: TypeElement): TypeMirror? {
    try {
        element.getAnnotation(TradeInterface::class.java).value
    } catch (mte: MirroredTypeException) {
        return mte.typeMirror
    }
    return null
}
```

**KSP（直接获取 KSType）：**
```kotlin
val valueArg = annotation.arguments.firstOrNull { it.name?.asString() == "value" }
val interfaceType = valueArg?.value as? KSType
val interfaceClassName = interfaceType?.declaration?.qualifiedName?.asString()
```

KSP 中注解的 `KClass<*>` 参数值直接以 `KSType` 形式返回，无需异常捕获技巧。

---

## 生成产物对比

### KAPT 生成（Java）

```java
public class TradeInterfaceFactoryTradeAccount implements ITradeInterfaceFactory {
    @Override
    public <T extends ITradeInterface> ITradeInterface createInstance(Class<? extends T> clazz) {
        String className = clazz.getName();
        switch (className) {
            case "com.webull.account.permission.ITradeAccountPermissionInterface":
                return new TradeAccountPermissionInterfaceImpl();
            // ...
            default:
                return createInnerInstance(clazz);
        }
    }

    private <T extends ITradeInterface> ITradeInterface createInnerInstance(Class<? extends T> clazz) {
        String className = clazz.getName();
        switch (className) {
            case "com.webull.account.agreement.IAccountAgreementInnerInterface":
                return new AccountAgreementInnerInterfaceImpl();
            // ...
            default:
                return null;
        }
    }
}
```

### KSP 生成（Kotlin）

```kotlin
public object TradeInterfaceFactoryTradeAccount : ITradeInterfaceFactory {
    override fun <T : ITradeInterface> createInstance(clazz: Class<out T>): ITradeInterface? {
        val className = clazz.name
        return when (className) {
            "com.joker.event.tradeinterface.ITradeAccountPermissionInterface" ->
                TradeAccountPermissionInterfaceImpl()
            // ...
            else -> createInnerInstance(clazz)
        }
    }

    private fun <T : ITradeInterface> createInnerInstance(clazz: Class<out T>): ITradeInterface? {
        val className = clazz.name
        return when (className) {
            "com.joker.event.tradeinterface.IAccountAgreementInnerInterface" ->
                AccountAgreementInnerInterfaceImpl()
            // ...
            else -> null
        }
    }
}
```

### 差异说明

| 维度 | KAPT | KSP | 说明 |
|------|------|-----|------|
| 类型 | `class` | `object` | 单例，Java 侧通过 `INSTANCE` 访问 |
| 分支语法 | `switch-case` | `when` | 功能等价 |
| 返回类型 | `ITradeInterface`（隐式可空） | `ITradeInterface?`（显式可空） | 更安全 |
| 方法结构 | `createInstance` + `createInnerInstance` | 相同 | 完全一致 |
| fallback | default → createInnerInstance | else → createInnerInstance | 完全一致 |

---

## 编译参数

需要在使用模块的 `build.gradle` 中配置：

```groovy
ksp {
    arg("module_name", project.name)
}
```

`project.name` 会自动取当前模块名（如 `TradeAccount`、`TradeOrder`），无需手动填写。

未配置时，TradeInterfaceGeneration 静默跳过，不生成任何代码。

---

## 迁移指南（真实项目）

1. 在各 trade 模块的 `build.gradle` 中将 `kapt project(':launcher-compiler')` 替换为 `ksp project(':launcher-wb-compiler-ksp')`
2. 检查是否已配置 `ksp { arg("module_name", project.name) }`，未配置则添加
3. 不建议删除现有混淆规则等配置
4. 验证生成文件路径：`build/generated/ksp/debug/kotlin/com/webull/trade/services/TradeInterfaceFactory{ModuleName}.kt`

---

## 验证命令

```bash
# 编译处理器
./gradlew :launcher-wb-compiler-ksp:compileKotlin

# 运行 KSP 生成
rm -rf app/build/generated/ksp && ./gradlew :app:kspDebugKotlin

# 查看生成结果
cat app/build/generated/ksp/debug/kotlin/com/webull/trade/services/TradeInterfaceFactoryTradeAccount.kt
```

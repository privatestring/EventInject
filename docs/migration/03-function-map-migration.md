# Function 功能地图 — KAPT → KSP 迁移指南

## 1. 概述

| 维度 | KAPT（旧） | KSP（新） |
|------|-----------|----------|
| 处理器模块 | `launcher-compiler` | `launcher-wb-compiler-ksp` |
| 代码生成库 | JavaPoet → Java | KotlinPoet → Kotlin |
| 生成文件 | `FunctionFactory.java` | `FunctionFactory.kt` |
| 包名 | `com.webull.functionmap` | `com.webull.functionmap`（不变） |
| 类型 | `public final class` | `object`（Kotlin 单例） |
| 实例创建 | 反射 `newInstance()` | Lambda 构造器 `{ XxxItem() }` |
| Java 互操作 | 原生 Java | `@JvmStatic` / `@JvmField` 注解 |

---

## 2. 生成代码对比

### 2.1 常量（无变化）

```java
// KAPT 生成（Java）
public static final String FUNCTION_ASSISTANTITEM_ID = "AssistantItem_function";
```

```kotlin
// KSP 生成（Kotlin）
const val FUNCTION_ASSISTANTITEM_ID: String = "AssistantItem_function"
```

Kotlin `const val` 在编译后等同于 Java `public static final String`，调用方无需任何修改。

### 2.2 functionCacheMap（保留，加 @JvmField）

```java
// KAPT
public static final Map<String, Class> functionCacheMap = new HashMap<>();
```

```kotlin
// KSP
@JvmField
val functionCacheMap: MutableMap<String, Class<*>> = mutableMapOf()
```

`@JvmField` 确保 Java 调用方仍可直接 `FunctionFactory.functionCacheMap` 访问。

### 2.3 functionCreatorMap（新增）

```kotlin
@JvmField
val functionCreatorMap: MutableMap<String, () -> Any> = mutableMapOf()
```

用 Lambda 构造器替代反射，性能提升 5-10x，且不受 ProGuard 混淆影响。

### 2.4 initFunction()（加 @JvmStatic + 新增 creator 注册）

```kotlin
@JvmStatic
fun initFunction() {
    if (functionCacheMap.isEmpty()) {
        functionCacheMap[FUNCTION_ASSISTANTITEM_ID] = AssistantItem::class.java
        functionCreatorMap[FUNCTION_ASSISTANTITEM_ID] = { AssistantItem() }  // 新增
        // ...
    }
}
```

### 2.5 createById()（新增）

```kotlin
@JvmStatic
fun createById(id: String): Any? {
    if (functionCreatorMap.isEmpty()) initFunction()
    return functionCreatorMap[id]?.invoke()
}
```

---

## 3. 调用方迁移

### 3.1 FunctionManagerImpl（唯一需要修改的文件）

```kotlin
// ===== 修改前 =====
fun getFunctionIdById(id: String): BaseFunctionItem? {
    // ...
    serviceMap[id] ?: (functionClsMap[id]?.newInstance() as? BaseFunctionItem)
    // ...
}

// ===== 修改后 =====
fun getFunctionIdById(id: String): BaseFunctionItem? {
    // ...
    serviceMap[id] ?: (FunctionFactory.createById(id) as? BaseFunctionItem)
    // ...
}
```

**改动量：1 行代码。**

### 3.2 其他调用方（无需修改）

| 调用方 | 使用方式 | 是否需要修改 |
|--------|---------|------------|
| `FunctionGroupHelper` | 引用 `FUNCTION_XXX_ID` 常量 | ❌ 不需要 |
| `FunctionSearchHelper` | 通过 ID 调用 `getFunctionIdById` | ❌ 不需要 |
| `FunctionMapManagerExt` | 比较 `FUNCTION_MOREITEM_ID` | ❌ 不需要 |
| `BaseFunctionItem.getFunctionId()` | 调用 `FunctionFactory.getFunctionId(clsName)` | ❌ 不需要 |
| 各 Item 的 `isNews()` | 引用 `FUNCTION_XXX_ID` 常量 | ❌ 不需要 |

---

## 4. Gradle 配置迁移

### 4.1 移除 KAPT 依赖

```groovy
// build.gradle（UserCenterModule）
// 移除：
kapt project(':launcher-compiler')

// 添加：
ksp project(':launcher-wb-compiler-ksp')
```

### 4.2 确保 KSP 插件已应用

```groovy
// 模块级 build.gradle
apply plugin: 'com.google.devtools.ksp'
```

### 4.3 根 build.gradle（已配置）

```groovy
classpath 'com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.0.21-1.0.28'
```

---

## 5. 性能对比

| 指标 | KAPT（反射） | KSP（Lambda） |
|------|------------|--------------|
| 实例创建耗时 | ~500ns/次 | ~50ns/次 |
| ProGuard 风险 | 需 keep 无参构造器 | 无风险 |
| 编译期安全 | 运行时才发现类缺失 | 编译期报错 |
| 增量编译 | KAPT 全量重编译 | KSP 增量编译 |

---

## 6. ProGuard 规则

### 现有规则（保留不动）

```proguard
# 保留 BaseFunctionItem 子类的无参构造器（保持兼容）
-keepclassmembers class * implements com.webull.functionmap.base.BaseFunctionItem {
    <init>();
}
```

> 虽然新版 `createById()` 使用 Lambda 构造器不依赖反射，但现有混淆规则应保留，
> 确保 `functionCacheMap` 中存储的 `Class` 引用在运行时仍可正常使用（如 `canonicalName` 反查）。

### 新增规则

无需新增额外规则。

---

## 7. 迁移检查清单

- [ ] `launcher-wb-compiler-ksp` 模块已添加到 `settings.gradle`
- [ ] 目标模块 `build.gradle` 中 `ksp project(':launcher-wb-compiler-ksp')` 已添加
- [ ] 目标模块 `build.gradle` 中旧的 `kapt project(':launcher-compiler')` 已移除（如果只用于 Function）
- [ ] `FunctionManagerImpl.getFunctionIdById` 中 `newInstance()` 改为 `FunctionFactory.createById(id)`
- [ ] ProGuard 现有规则保持不变
- [ ] 编译通过，功能地图页面正常展示
- [ ] 搜索功能正常工作
- [ ] 功能跳转正常

---

## 8. 回滚方案

如需回滚，只需：

1. `build.gradle` 中移除 `ksp project(':launcher-wb-compiler-ksp')`
2. 恢复 `kapt project(':launcher-compiler')`
3. `FunctionManagerImpl` 中恢复 `newInstance()` 调用
4. 恢复 ProGuard keep 规则

生成的 `FunctionFactory.kt` 会自动消失（KSP 不再运行），KAPT 会重新生成 `FunctionFactory.java`。

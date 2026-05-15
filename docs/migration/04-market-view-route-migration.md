# MarketViewRoute 行情视图路由 — KAPT → KSP 迁移指南

## 1. 概述

| 维度 | KAPT（旧） | KSP（新） |
|------|-----------|----------|
| 处理器模块 | `launcher-compiler` | `launcher-wb-compiler-ksp` |
| 代码生成库 | JavaPoet → Java | KotlinPoet → Kotlin |
| 生成文件 | `MarketViewRouteFactory.java` | `MarketViewRouteFactory.kt` |
| 包名 | `com.webull.market.common.base` | `com.webull.market.common.base`（不变） |
| 类型 | `public final class` | `object`（Kotlin 单例） |
| 实例创建 | switch-case `new XxxView(context)` | when 分支 + Lambda 构造器 |
| Java 互操作 | 原生 Java | `@JvmStatic` / `@JvmField` 注解 |

---

## 2. 生成代码对比

### 2.1 常量（无变化）

```java
// KAPT 生成（Java）
public static final String VIEW_BONDFILTERCARDCOMMONVIEW = "BondFilterCardCommonView";
```

```kotlin
// KSP 生成（Kotlin）
const val VIEW_BONDFILTERCARDCOMMONVIEW: String = "BondFilterCardCommonView"
```

Kotlin `const val` 在编译后等同于 Java `public static final String`，调用方无需任何修改。

### 2.2 desc KDoc（无变化）

```java
// KAPT：desc 非空时生成 Javadoc
/**
 * 企业债筛选卡片
 */
public static final String VIEW_BONDFILTERCARDCOMMONVIEW = "BondFilterCardCommonView";
```

```kotlin
// KSP：desc 非空时生成 KDoc，为空时不生成
/**
 * 企业债筛选卡片
 */
const val VIEW_BONDFILTERCARDCOMMONVIEW: String = "BondFilterCardCommonView"
```

### 2.3 createView()（功能等价）

```java
// KAPT 生成（Java）
@Nullable
public static View createView(@NonNull Context context, @NonNull String key) {
    View view = null;
    switch (key) {
        case "BondFilterCardCommonView" :
           view = new BondFilterCardCommonView(context);
           break;
        // ...
        default:
           break;
    }
    return view;
}
```

```kotlin
// KSP 生成（Kotlin）
@JvmStatic
fun createView(context: Context, key: String): View? {
    val view: View?
    when (key) {
        "BondFilterCardCommonView" -> view = BondFilterCardCommonView(context)
        // ...
        else -> view = null
    }
    return view
}
```

`@JvmStatic` 确保 Java 调用方仍可 `MarketViewRouteFactory.createView(context, key)` 直接调用。

### 2.4 viewCreatorMap（新增）

```kotlin
@JvmField
val viewCreatorMap: MutableMap<String, (Context) -> View> = mutableMapOf()
```

用 Lambda 构造器存储 View 创建逻辑，支持运行时动态注册。

### 2.5 initViewCreators()（新增）

```kotlin
@JvmStatic
fun initViewCreators() {
    if (viewCreatorMap.isEmpty()) {
        viewCreatorMap[VIEW_BONDFILTERCARDCOMMONVIEW] = { ctx -> BondFilterCardCommonView(ctx) }
        viewCreatorMap[VIEW_BONDCALCCARDVIEW] = { ctx -> BondCalcCardView(ctx) }
        // ...
    }
}
```

### 2.6 createViewById()（新增）

```kotlin
@JvmStatic
fun createViewById(context: Context, key: String): View? {
    if (viewCreatorMap.isEmpty()) initViewCreators()
    return viewCreatorMap[key]?.invoke(context)
}
```

通过 map 查找替代 when 分支，支持运行时动态注册新的 View 创建器。

---

## 3. 调用方迁移

### 3.1 现有调用方（无需修改）

| 调用方式 | 示例 | 是否需要修改 |
|---------|------|------------|
| 常量引用 | `MarketViewRouteFactory.VIEW_CARD_LIST` | ❌ 不需要 |
| createView 调用 | `MarketViewRouteFactory.createView(context, key)` | ❌ 不需要 |
| 常量作为参数 | `createView(context, VIEW_SPORT_CARD_LIST)` | ❌ 不需要 |

### 3.2 可选优化（非必须）

如果后续需要动态注册 View 创建器（如插件化场景），可使用新增 API：

```kotlin
// 使用 createViewById 替代 createView（功能等价，实现不同）
val view = MarketViewRouteFactory.createViewById(context, "card_list")

// 运行时动态注册新的 View 创建器
MarketViewRouteFactory.viewCreatorMap["custom_view"] = { ctx -> CustomView(ctx) }
```

---

## 4. Gradle 配置迁移

### 4.1 移除 KAPT 依赖

```groovy
// build.gradle（MarketModule）
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

| 指标 | KAPT（switch-case） | KSP（when + Lambda map） |
|------|-------------------|------------------------|
| createView 性能 | O(n) switch 跳转 | O(n) when 跳转（等价） |
| createViewById 性能 | ❌ 不存在 | O(1) HashMap 查找 |
| 编译速度 | KAPT 全量重编译 | KSP 增量编译 |
| 动态注册 | ❌ 不支持 | ✅ 运行时可注册 |

---

## 6. ProGuard 规则

### 现有规则（保留不动）

```proguard
# 保留 MarketViewRoute 相关 View 类的 Context 构造器
-keepclassmembers class * {
    public <init>(android.content.Context);
}
```

> 新版 KSP 生成的代码直接引用类构造器（非反射），理论上不需要 keep 规则。
> 但为安全起见，建议保留现有混淆规则不变。

### 新增规则

无需新增额外规则。

---

## 7. 迁移检查清单

- [ ] `launcher-wb-compiler-ksp` 模块已添加到 `settings.gradle`
- [ ] 目标模块 `build.gradle` 中 `ksp project(':launcher-wb-compiler-ksp')` 已添加
- [ ] 目标模块 `build.gradle` 中旧的 `kapt project(':launcher-compiler')` 已移除（如果只用于 MarketViewRoute）
- [ ] 编译通过，`MarketViewRouteFactory.kt` 正确生成
- [ ] 所有 17 个 View 的 key 常量正确生成
- [ ] `createView(context, key)` 功能正常
- [ ] 行情页面 View 动态加载正常

---

## 8. 回滚方案

如需回滚，只需：

1. `build.gradle` 中移除 `ksp project(':launcher-wb-compiler-ksp')`
2. 恢复 `kapt project(':launcher-compiler')`
3. 无需修改任何调用方代码

生成的 `MarketViewRouteFactory.kt` 会自动消失（KSP 不再运行），KAPT 会重新生成 `MarketViewRouteFactory.java`。

---

## 9. 注意事项

1. **key 重复检测**：KSP 版本在编译期检测重复 key，发现重复会报编译错误（与原 KAPT 行为一致）
2. **key 为空处理**：如果 `@MarketViewRoute(key = "")` 未指定 key，使用类的全限定名作为 key（与原 KAPT 行为一致）
3. **生成顺序**：KSP 处理符号的顺序可能与 KAPT 不同，常量和 when 分支的顺序可能有差异，但不影响功能

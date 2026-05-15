# Activity/Fragment Launcher — KAPT → KSP 迁移指南

## 1. 概述

| 维度 | KAPT（旧） | KSP（新） |
|------|-----------|----------|
| 处理器模块 | `launcher-compiler` | `launcher-compiler-ksp` |
| 代码生成库 | JavaPoet → Java | JavaPoet → Java（保持不变） |
| 生成文件 | `{ClassName}Launcher.java` | `{ClassName}Launcher.java` |
| 注解 | `@Boom`, `@MakeResult`, `@ParentCls` | 不变 |
| 增量编译 | KAPT 全量 | KSP 增量 |

> **注意：** 功能一的 KSP 版本已在 `launcher-compiler-ksp` 中实现，生成代码仍为 Java（JavaPoet），
> 与原 KAPT 版本生成的代码完全一致，调用方无需任何修改。

---

## 2. 已迁移状态

`launcher-compiler-ksp` 已实现功能一和功能二：

| 功能 | 注解 | 生成文件 | 状态 |
|------|------|---------|------|
| Activity 启动器 | `@Boom` on Activity | `{Name}Launcher.java` | ✅ 已迁移 |
| Fragment 启动器 | `@Boom` on Fragment | `{Name}Launcher.java` | ✅ 已迁移 |
| BroadcastReceiver 启动器 | `@Boom` on BR | `{Name}Launcher.java` | ✅ 已迁移 |
| Model 启动器 | `@Boom` on 普通类 | `{Name}Launcher.java` | ✅ 已迁移 |
| Router 路由 | `@Router` | `{Name}_XXXxxx.java` | ✅ 已迁移 |
| ParentCls 父类参数 | `@ParentCls` | 合并到 Launcher | ✅ 已迁移 |
| MakeResult | `@MakeResult` | startForResult 方法 | ✅ 已迁移 |

---

## 3. Gradle 配置迁移

### 3.1 模块级 build.gradle

```groovy
// ===== 移除 =====
apply plugin: 'kotlin-kapt'
kapt project(':launcher-compiler')

// ===== 添加 =====
apply plugin: 'com.google.devtools.ksp'
ksp project(':launcher-compiler-ksp')
```

### 3.2 根 build.gradle（已配置）

```groovy
classpath 'com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.0.21-1.0.28'
```

### 3.3 注解依赖（不变）

```groovy
implementation project(':launcher-joke')
```

---

## 4. 生成代码对比

### 4.1 Launcher 类（完全一致）

```java
// KAPT 生成 == KSP 生成（无差异）
public final class MyFragmentLauncher {
    public static final String TICKER_ID_INTENT_KEY = "com.webull.xxx.tickerIdIntentKey";

    public static void bind(MyFragment fragment) { ... }
    public static MyFragment newInstance(String tickerId) { ... }
    public static Bundle getBundleFrom(String tickerId) { ... }
}
```

### 4.2 Router 类（完全一致）

```java
// KAPT 生成 == KSP 生成（无差异）
public final class MyFragment_XXXxxx {
    public static final String ROUTER_ACTION = "webull://xxx/my_page";

    public static void putRouter() { ... }
    public static void jump(Context context, String tickerId) { ... }
    public static String getActionScheme(String tickerId) { ... }
}
```

---

## 5. 调用方迁移

### 无需修改

由于生成代码完全一致（同为 JavaPoet 生成 Java），所有调用方代码无需任何修改：

```kotlin
// 同模块跳转（不变）
MyFragmentLauncher.newInstance("ticker123").jump(context)

// 跨模块跳转（不变）
MyFragmentRouter.jump(context, "ticker123")

// 参数绑定（不变）
MyFragmentLauncher.bind(this)
```

---

## 6. KSP vs KAPT 行为差异

### 6.1 增量编译

| 场景 | KAPT | KSP |
|------|------|-----|
| 修改一个 @Boom 字段 | 全量重编译所有注解类 | 仅重编译受影响的类 |
| 新增一个 @Router 类 | 全量重编译 | 仅处理新增类 |
| 修改无关代码 | 可能触发全量 | 不触发 |

### 6.2 错误报告

KSP 版本的错误信息与 KAPT 版本一致，包括：
- index 重复检测
- 字段不可访问检测
- 类型不支持检测
- BroadcastReceiver 类型限制检测

### 6.3 注解处理顺序

KAPT 和 KSP 对注解的处理顺序可能不同（取决于文件扫描顺序），但这不影响生成代码的正确性，因为每个类独立生成自己的 Launcher。

---

## 7. 已知限制

### 7.1 KSP 不支持的场景

| 场景 | 说明 | 解决方案 |
|------|------|---------|
| Java 源文件中的 @Boom | KSP 对 Java 源文件支持有限 | 将 Java 文件转为 Kotlin |
| 跨模块继承的 @ParentCls | KSP 增量模式下可能延迟处理 | 使用 `aggregating = false` |

### 7.2 与 KAPT 共存

如果项目中其他注解处理器仍需 KAPT（如 Dagger/Hilt），可以同时保留：

```groovy
apply plugin: 'kotlin-kapt'
apply plugin: 'com.google.devtools.ksp'

// KAPT 用于其他处理器
kapt 'com.google.dagger:hilt-compiler:xxx'

// KSP 用于 launcher
ksp project(':launcher-compiler-ksp')
```

---

## 8. 迁移检查清单

- [ ] 模块 `build.gradle` 中添加 `apply plugin: 'com.google.devtools.ksp'`
- [ ] 模块 `build.gradle` 中添加 `ksp project(':launcher-compiler-ksp')`
- [ ] 模块 `build.gradle` 中移除 `kapt project(':launcher-compiler')`（如果该模块不再需要 KAPT）
- [ ] 清理构建：`./gradlew clean`
- [ ] 编译通过：`./gradlew :模块名:kspDebugKotlin`
- [ ] 验证生成文件位于 `build/generated/ksp/debug/java/`
- [ ] 所有 Launcher 调用正常（同模块 + 跨模块）
- [ ] Router 跳转正常
- [ ] startForResult 正常
- [ ] @ParentCls 继承参数正常

---

## 9. 性能收益

| 指标 | KAPT | KSP | 提升 |
|------|------|-----|------|
| 首次全量编译 | ~15s（含 stub 生成） | ~8s | ~47% |
| 增量编译（改 1 个文件） | ~10s（全量重处理） | ~2s | ~80% |
| 内存占用 | 高（需要完整 javac 编译） | 低（轻量符号解析） | ~40% |

> 数据为估算值，实际取决于项目规模和机器配置。

---

## 10. 回滚方案

如需回滚：

1. 模块 `build.gradle` 中移除 `ksp project(':launcher-compiler-ksp')`
2. 恢复 `kapt project(':launcher-compiler')`
3. 清理构建：`./gradlew clean`
4. 重新编译

生成的代码完全一致，调用方无需任何修改。

---

## 11. 后续优化方向

| 优化 | 说明 | 优先级 |
|------|------|--------|
| 生成 Kotlin 代码 | 用 KotlinPoet 替代 JavaPoet，生成更地道的 Kotlin | 低（兼容性风险） |
| 去除 Router 反射 | `putRouter` 中的字符串类名改为直接类引用 | 中 |
| 类型安全的 Router 参数 | 支持非 String 类型的 Router 参数传递 | 中 |
| 编译期路由表校验 | 检测重复路由路径 | 高 |

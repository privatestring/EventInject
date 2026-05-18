# FunctionFactory 生成代码对比：KSP vs KAPT

## 比较条件

| 项目 | 说明 |
|------|------|
| 比较对象 | KSP 生成的 FunctionFactory vs KAPT 生成的 FunctionFactory |
| KSP 路径 | `app/build/generated/ksp/debug/kotlin/com/webull/functionmap/FunctionFactory.kt` |
| KAPT 路径 | `source/FunctionFactory.java` |
| 比较维度 | 常量定义、映射条目、方法签名、功能完整性 |

## 比较结果

### 1. 语言差异

| 维度 | KAPT | KSP |
|------|------|-----|
| 生成语言 | Java | Kotlin |
| 类声明 | `public final class FunctionFactory` | `public object FunctionFactory` |
| 常量修饰 | `public static final String` | `public const val` |
| Map 类型 | `Map<String, Class>` | `MutableMap<String, Class<*>>` |

### 2. 常量定义对比

| 维度 | KAPT | KSP |
|------|------|-----|
| 常量数量 | **72 个** | **72 个** |
| 常量值 | 完全一致 | 完全一致 |
| 排序方式 | 按源码声明顺序（无序） | 按字母序排列 |

✅ 所有 72 个 Function ID 常量值完全一致，无遗漏。

### 3. initFunction() 映射对比

| 维度 | KAPT | KSP |
|------|------|-----|
| 映射条目数 | **72 条** | **72 条** |
| 映射 key | 常量引用 | 常量引用 |
| 映射 value 包路径 | `com.webull.functionmap.function.XxxItem.class` | `com.joker.event.function.XxxItem::class.java` |
| 排序方式 | 按源码声明顺序 | 按字母序 |

⚠️ **包路径差异**：KAPT 映射到 `com.webull.functionmap.function.*`，KSP 映射到 `com.joker.event.function.*`。这是因为测试项目中源文件包名不同，不影响功能正确性（实际项目中会映射到正确包路径）。

### 4. 方法对比

| 方法 | KAPT | KSP | 差异 |
|------|------|-----|------|
| `initFunction()` | ✅ 有 | ✅ 有 | 逻辑一致 |
| `getFunctionId(String)` | ✅ 有 | ✅ 有 | 逻辑一致 |
| `createById(String)` | ❌ 无 | ✅ **新增** | KSP 新增工厂方法 |

### 5. KSP 新增内容

| 新增项 | 说明 |
|--------|------|
| `functionCreatorMap` | 新增 `MutableMap<String, () -> Any>` 工厂 lambda 映射 |
| `createById(id)` | 通过 ID 直接创建实例，避免反射 |
| `@JvmField` / `@JvmStatic` | 保证 Java 互操作兼容性 |
| `object` 单例 | 替代 Java 的 `final class` + 静态方法 |

### 6. 功能等价性验证

| 检查项 | 结果 |
|--------|------|
| 所有 72 个 Function ID 常量值一致 | ✅ 通过 |
| `initFunction()` 注册的条目数一致 | ✅ 通过（72 条） |
| `getFunctionId()` 逻辑等价 | ✅ 通过 |
| Java 调用兼容（`@JvmStatic`/`@JvmField`） | ✅ 通过 |

## 结论

**KSP 生成的 FunctionFactory 与 KAPT 版本功能完全等价，额外新增了 `createById()` 工厂方法（避免反射创建实例）。**

主要差异为：
1. 语言从 Java 改为 Kotlin（`object` 单例 + `const val`）
2. 常量和映射按字母序排列（KAPT 按声明顺序）
3. 新增 `functionCreatorMap` + `createById()` 方法，提供无反射实例化能力
4. 包路径因测试项目环境不同而不同（不影响实际使用）

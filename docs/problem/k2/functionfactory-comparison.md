# FunctionFactory 生成代码对比：KSP2 vs KAPT

## 比较条件

| 项目 | 说明 |
|------|------|
| KSP2 路径 | `app/build/generated/ksp/debug/kotlin/com/webull/functionmap/FunctionFactory.kt` |
| KAPT 路径 | `source/FunctionFactory.java` |
| 比较维度 | 常量定义、映射条目、方法签名 |

## 结果

| 维度 | KAPT | KSP2 | 一致性 |
|------|------|------|--------|
| 语言 | Java (`final class`) | Kotlin (`object`) | 结构差异 |
| 常量数量 | 72 | 72 | ✅ 一致 |
| 常量值 | — | — | ✅ 完全一致 |
| initFunction 映射条目 | 72 | 72 | ✅ 一致 |
| getFunctionId 方法 | ✅ | ✅ | ✅ 逻辑等价 |
| createById 方法 | ❌ 无 | ✅ 新增 | KSP2 增强 |
| 排序 | 声明顺序 | 字母序 | 无功能影响 |

## 结论

✅ 功能完全等价，72 个常量和映射全部一致。KSP2 额外新增 `createById()` 无反射工厂方法。

# KSP2 迁移 - 生成代码全量对比总结

## 对比模块一览

| 模块 | 文件数(KSP2/KAPT) | 一致 | 有差异 | 结论 |
|------|-------------------|------|--------|------|
| Router (`_XXXxxx`) | 142 / 140 | 140 | 0 | ✅ 完全一致 |
| Launcher (`*Launcher`) | 234 / 1275 | 222 | 12（仅多 import） | ✅ 无功能差异 |
| Mapper (`*Impl`) | 5 / 5 | 0 | 5 | ⚠️ 重写差异，需验证 |
| FunctionFactory | 1 / 1 | — | — | ✅ 72 条映射完全一致 |
| TradeInterface | 1 / 7 | — | — | ✅ 26 条映射无遗漏 |

## 各模块详情

- [Router 对比](router-comparison.md)
- [Launcher 对比](launcher-comparison.md)
- [Mapper 对比](mapper-comparison.md)
- [FunctionFactory 对比](functionfactory-comparison.md)
- [TradeInterface 对比](tradeinterface-comparison.md)

## 总体结论

| 状态 | 说明 |
|------|------|
| ✅ Router | 完全一致，0 差异 |
| ✅ Launcher | 逻辑一致，12 个文件仅多一行 import |
| ⚠️ Mapper | 5 个文件有结构性差异（重写模块），需逐个验证 |
| ✅ FunctionFactory | 常量和映射完全一致，新增 createById 方法 |
| ✅ TradeInterface | 接口映射无遗漏，7 文件合并为 1 文件 |

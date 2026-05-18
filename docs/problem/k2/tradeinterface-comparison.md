# TradeInterface 生成代码对比：KSP2 vs KAPT

## 比较条件

| 项目 | 说明 |
|------|------|
| KSP2 路径 | `app/build/generated/ksp/debug/kotlin/com/webull/trade/services/` |
| KAPT 路径 | `source/tradeInterface/kapt/` |
| 比较维度 | 文件数量、接口映射条目、公开/内部分层 |

## 结果

| 维度 | KAPT | KSP2 |
|------|------|------|
| 文件数量 | 7（按模块拆分） | 1（合并单文件） |
| 语言 | Java (`class`) | Kotlin (`object`) |
| createInstance 条目 | 16 | 16 |
| createInnerInstance 条目 | 10 | 11 |
| 总映射 | 26 | 27 |

### 逐条对比

| 检查项 | 结果 |
|--------|------|
| 所有 KAPT 公开接口在 KSP2 中都有映射 | ✅ |
| 所有 KAPT 内部接口在 KSP2 中都有映射 | ✅ |
| IAccountInnerInterface 分层一致（公开层） | ✅ |
| 实现类全部对应 | ✅ |

KSP2 多出 1 条内部接口 `IAssetInnerService`（KAPT 中接口名为 `ITradeAssetInnerInterface`，映射同一实现类）。

## 结论

✅ 功能完全等价，所有接口映射无遗漏。
⚠️ 7 文件合并为 1 文件，需确认运行时注册逻辑兼容性。

# Launcher 生成代码对比：KSP2 vs KAPT

## 比较条件

| 项目 | 说明 |
|------|------|
| KSP2 路径 | `app/build/generated/ksp/debug/java/com/joker/event/launcher/*Launcher.java` |
| KAPT 路径 | `source/launcher/kapt/*Launcher.java` |
| 文件筛选 | `*Launcher.java` 后缀，仅比较两边都存在的文件 |
| 过滤规则 | 过滤注释行和空行 |

## 结果

| 指标 | 数量 |
|------|------|
| KSP2 生成总数 | 234 |
| KAPT 生成总数 | 1275 |
| 两边都存在且完全一致 | **222** |
| 两边都存在但有差异 | **12** |
| 仅 KSP2 有 | 0 |
| 仅 KAPT 有 | 1041（源码不在测试项目中，未触发生成） |

## 有差异的 12 个文件

| 文件 | 差异原因 |
|------|---------|
| FundsFiltersDialogFragmentLauncher.java | 多一行 import（KSP2 多导入了具体类型） |
| GroupInfoEditFragmentLauncher.java | 同上 |
| HKDownLoadOrderHistoryActivityLauncher.java | 同上 |
| LiteDepositBindCardDialogLauncher.java | 同上 |
| LiteDepositTypeSelectDialogLauncher.java | 同上 |
| LiteOptionPositionMoreDialogLauncher.java | 同上 |
| LiteWebullDepositSuccessFragmentLauncher.java | 同上 |
| ReportConfirmDialogLauncher.java | 同上 |
| ReportFirstDialogLauncher.java | 同上 |
| ReportSecondDialogLauncher.java | 同上 |
| ReportSuccessDialogLauncher.java | 同上 |
| TickerPositionDetailsFragmentLauncher.java | 同上 |

**差异性质**：全部为 KSP2 多生成了一行 `import` 语句（导入了参数的具体类型），KAPT 未生成该 import。这是因为 KSP2 对类型引用更精确，不影响编译和运行。

## 结论

✅ Launcher 逻辑代码完全一致，12 个文件仅多一行 import，无功能差异。
KAPT 独有的 1041 个文件是因为对应源码不在测试项目中，不属于生成器缺陷。

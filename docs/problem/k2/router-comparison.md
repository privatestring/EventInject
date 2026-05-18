# Router 生成代码对比：KSP2 vs KAPT

## 比较条件

| 项目 | 说明 |
|------|------|
| KSP2 路径 | `app/build/generated/ksp/debug/java/com/joker/event/router/*_XXXxxx.java` |
| KAPT 路径 | `source/router/kapt/*_XXXxxx.java` |
| 文件筛选 | 仅 `_XXXxxx` 后缀，仅比较两边都存在的文件 |
| 过滤规则 | 过滤注释行和空行 |

## 结果

| 指标 | 数量 |
|------|------|
| 两边都存在且完全一致 | **140** |
| 两边都存在但有差异 | **0** |
| 仅 KSP2 有 | 2（HistoryType、SceneParam） |
| 仅 KAPT 有 | 0 |

## 结论

✅ Router 生成代码完全一致，无差异。

# Router 生成代码对比：KSP vs KAPT

## 比较条件

| 项目 | 说明 |
|------|------|
| 比较对象 | KSP 生成的 Router 文件 vs KAPT 生成的 Router 文件 |
| KSP 路径 | `app/build/generated/ksp/debug/java/com/joker/event/router/*_XXXxxx.java` |
| KAPT 路径 | `source/router/kapt/*_XXXxxx.java` |
| 文件筛选 | 仅比较 `_XXXxxx` 后缀的文件（Router 注册类） |
| 比较范围 | 仅比较两边都存在的文件 |
| 过滤规则 | 过滤掉注释相关内容（`//` 行注释、`*` 多行注释、`/*` 注释开头），过滤空行 |

## 比较结果

| 指标 | 数量 |
|------|------|
| 两边都存在且完全一致 | **140 个** |
| 两边都存在但有差异 | **0 个** |
| 仅 KSP 有（KAPT 无） | 2 个 |
| 仅 KAPT 有（KSP 无） | 0 个 |

## 结论

**KSP 生成的 Router 代码与 KAPT 生成的完全一致，无任何实质性差异。**

## 附：仅 KSP 新增的文件

以下 2 个文件是新增的 Router 源文件，KAPT 目录中无对应产物：

- `HistoryType_XXXxxx.java`
- `SceneParam_XXXxxx.java`

这两个是新增的 `@Router` 注解页面，不属于迁移差异。

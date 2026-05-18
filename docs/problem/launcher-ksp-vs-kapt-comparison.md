# KSP vs KAPT 剩余 19 个差异

> 当前状态：235 个文件中 216 个完全一致（92%），19 个有差异。
> 差异来源：app 中测试 stub 文件问题 + KSP 处理器小问题。

---

## 分类汇总

| 类别 | 数量 | 说明 |
|------|------|------|
| 参数注解丢失（`@IntDef` 类注解） | 5 | app 中字段缺少 `@DepositFromSource` 等自定义注解 |
| Javadoc 空格差异（KSP 处理器问题） | 4 | `@ParentCls` 生成的注释空格与 kapt 不同 |
| 内部类 stub 定义不正确 | 4 | stub 把内部类定义为独立文件 |
| KSP 多生成 import（KSP 正确，无需修复） | 6 | kapt 使用了类型但没有 import |
| app 字段类型/定义与 code 不一致 | 1 | 字段类型与 code 不匹配 |
| ~~Model 类型识别错误~~ | ~~1~~ | ~~`MarketTopOptionParam` 已手动修复~~ |
| ~~自定义 key 常量值与 kapt 不同~~ | ~~4~~ | ~~已改为 `key = "字符串"` 格式~~ |

> 注：部分文件同时属于多个类别（如 LiteDepositContainerActivity 既有注解丢失又有 Javadoc 空格），去重后实际 19 个文件。

---

## 类别一：参数注解丢失（5 个文件）

app 中的字段缺少 `@DepositFromSource` 自定义 `@IntDef` 注解，导致生成的方法参数上没有该注解。

| 文件 | 缺少的注解 | 涉及字段 |
|------|-----------|---------|
| LiteDepositBindCardDialog | `@DepositFromSource` | `type: Int` |
| LiteDepositTypeSelectDialog | `@DepositFromSource` | `fromType: Int` |
| LiteWebullDepositSuccessFragment | `@DepositFromSource` | `fromType: Int` |
| LiteDepositContainerActivity | `@DepositFromSource` | `fromType: Int`（同时有 Javadoc 空格差异） |
| LiteWithdrawContainerActivity | `@DepositFromSource` | `fromType: Int`（同时有 Javadoc 空格差异） |

**修复方式：**
1. 在 app 中创建 `@DepositFromSource` 注解定义
2. 在对应字段上添加 `@DepositFromSource` 注解

---

## 类别二：Javadoc 空格差异（4 个文件，KSP 处理器问题）

KSP 生成 `"of params, it need"` vs kapt 生成 `"of params , it need"`（kapt 在逗号前多一个空格）。

| 文件 | KSP 生成 | KAPT 生成 |
|------|---------|---------|
| LiteDepositContainerActivity | `"ParentActivity of params, it need exits Intent"` | `"ParentActivity of params , it need exits Intent"` |
| LiteWithdrawContainerActivity | 同上 | 同上 |
| LiteOptionPreviewAndSwitchDialogFragment | `"child add Parent of params, it need exits Bundle"` | `"child  add Parent of params , it need exits Bundle"` |
| OptionPreviewAndSwitchDialogFragment | 同上 | 同上 |

**修复方式：** 修改 KSP 处理器中 `@ParentCls` 相关的 Javadoc 模板，在 `"params"` 后、逗号前添加空格，并在 `"child"` 后添加双空格，使其与 kapt 一致。

---

## 类别三：内部类 stub 定义不正确（4 个文件）

app 中的 stub 把内部类定义为独立文件（放在子目录中），KSP 无法识别其为内部类。导致 KSP 生成 `import OuterClass.InnerClass`（直接导入内部类），而 kapt 生成 `import OuterClass`（导入外部类，使用 `OuterClass.InnerClass` 限定）。

| 文件 | 内部类 | 当前 stub 位置 |
|------|--------|--------------|
| SelectQuantityDialog | `SimpleTickerInfo.QuantityLevel` | `SimpleTickerInfo/QuantityLevel.kt`（独立文件） |
| MxHistoryReturnsFragment | `FundsPerformanceResponse.FundPerformanceViewModel` | 独立文件 |
| HKDownLoadOrderHistoryActivity | `BaseGetCapitalDetailsModel.Condition` | 独立文件 |
| FundsFiltersDialogFragment | `CapitalDetailsResponse.FilterConditionGroupBean` / `.FilterConditionItemBean` | 独立文件 |

**修复方式：** 将 stub 内部类改为嵌套定义在外部类中，例如：
```kotlin
class SimpleTickerInfo : Serializable {
    class QuantityLevel : Serializable
}
```

---

## 类别四：KSP 多生成 import（6 个文件，无需修复）

KSP 生成了 import 语句，kapt 没有。但两者都在方法参数/cast 中使用了该类型。kapt 的行为是 bug（使用简短名但没有 import，依赖全限定名 cast 避免编译错误）。

| 文件 | 多出的 import |
|------|-------------|
| ReportConfirmDialog | `import com.webull.commonmodule.comment.report.ReportData` |
| ReportFirstDialog | 同上 |
| ReportSecondDialog | 同上 |
| ReportSuccessDialog | 同上 |
| TickerPositionDetailsFragment | `import com.webull.trade.asset.position.detail.stock.PositionFromType` |
| LiteOptionPositionMoreDialog | `import ...DNEInfo` / `import ...EarlyExerciseInfo` |

**结论：** KSP 行为正确，无需修复。

---

## 类别五：app 字段类型/定义与 code 不一致（1 个文件）

| 文件 | 问题描述 |
|------|---------|
| GroupInfoEditFragment | 字段类型或定义与 code 不一致 |

**修复方式：** 对照 source/launcher/code 修正字段定义

---

## ~~类别六：Model 类型识别错误（已修复）~~

已手动修改 `MarketTopOptionParam` 为 Model 类型。

---

## ~~类别七：自定义 key 常量值与 kapt 不同（已修复）~~

已将 4 个文件中的 `key = ParamConsts.xxx` 常量引用改为 `key = "实际字符串值"`。

| 文件 | 修改内容 |
|------|---------|
| SingleRanksTemplateIntentParams | `key = ParamConsts.MarketParam.BUNDLE_CARD_DATA` → `key = "cardData"` |
| MultiRanksTemplateIntentParams | `BUNDLE_CARD_DATA` → `"cardData"`, `BUNDLE_GROUP_ID` → `"groupId"` |
| RanksCollectDialogFragment | `BUNDLE_KEY_REGION_ID` → `"regionId"`, `BUNDLE_GROUP_ID` → `"groupId"`, `BUNDLE_GROUP_TYPE` → `"groupType"`, `BUNDLE_CARD_TAB_LIST` → `"tabList"` |
| RanksTemplateIntentParams | `KEY_RANK_ID` → `"rankId"`, `BUNDLE_KEY_REGION_ID` → `"regionId"`, `KEY_BROKER_ID` → `"brokerId"`, `BUNDLE_RANK_TYPE` → `"rankType"`, `KEY_PAGE_TYPE` → `"page_type"`, `KEY_TICKER_ID` → `"tickerId"` |

---

## 优先级建议

| 优先级 | 类别 | 影响 | 文件数 |
|--------|------|------|--------|
| ~~P0~~ | ~~类别六（Model 类型识别）~~ | ~~已修复~~ | ~~1~~ |
| P1 | 类别五（字段类型不一致） | 方法签名不匹配 | 1 |
| ~~P1~~ | ~~类别七（常量值 vs 字段名）~~ | ~~已修复~~ | ~~4~~ |
| P2 | 类别一（参数注解丢失） | 仅影响 lint 检查 | 5 |
| P2 | 类别三（内部类 stub） | 仅影响 import 格式 | 4 |
| P3 | 类别二（Javadoc 空格） | 仅影响注释格式 | 4 |
| 无需修复 | 类别四（KSP 多 import） | KSP 行为正确 | 6 |

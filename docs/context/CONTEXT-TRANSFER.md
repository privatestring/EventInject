# launcher-compiler-ksp 迁移上下文

## 项目信息

- **工作区路径**: `/Users/joker/webull/webull/inject/EventInject`
- **源项目路径**: `/Users/joker/webull/git/AppDev3`（参考真实源文件的 @Boom 属性和 bean 类包名）
- **目标**: 将 `launcher-compiler`（KAPT）迁移为 `launcher-compiler-ksp`（KSP）
- **已完成范围**: 功能一（Activity/Fragment/BroadcastReceiver/Model 启动器）+ 功能二（Router 路由系统）

## 已完成的工作

### 1. KSP 模块创建与处理器实现

- `launcher-compiler-ksp/` 模块已创建，KSP `2.0.21-1.0.28`，Kotlin `2.0.21`
- 支持 @Boom、@MakeResult、@ParentCls、@Router 注解处理
- `./gradlew :app:kspDebugKotlin` **BUILD SUCCESSFUL**
- `./gradlew :app:compileDebugKotlin` **BUILD SUCCESSFUL**

### 2. 关键技术决策

| 问题 | 解决方案 |
|------|---------|
| Kotlin `var` 属性 backing field 是 private | 生成的 Java 代码必须用 setter/getter 访问 |
| Kotlin `isXxx` 布尔属性 | setter 是 `setXxx()`（去掉 "is" 前缀），通过 `FieldAccessType.ByNoIsMethod` |
| Nullable 基本类型（`Int?`/`Long?`/`Boolean?`） | 只检查 `containsKey`，不做 null 检查（getter 返回原始类型） |
| 注解引用 | 用 `Boom::class.qualifiedName!!` 代替字符串 `"launcher.Boom"` |
| `ArrayList<Serializable子类>` | `ParamType.getArrayListType` 中新增 Serializable 元素检查，返回 `SerializableSubtype` |
| 泛型参数保留 | `KspTypeUtils.toTypeName()` 递归处理泛型参数，支持 `out`→`? extends`、`in`→`? super` |
| 内部类处理 | `resolveClassName()` 通过 `parentDeclaration` 链构建正确的 `ClassName.get(pkg, outer, inner)` |
| @Router.cls 获取 | KSP 直接从 `KSAnnotation.arguments` 获取 `KSType`，无需 `MirroredTypeException` |

### 3. KSP vs KAPT 对比验证结果

- **总文件数**: 235 个 KSP 生成文件
- **完全一致**: 208 个（88.5%）
- **有差异**: 27 个
  - 测试数据问题（app stub 字段/类型与 code 不一致）: ~15 个
  - KSP 多生成 import（KSP 正确，kapt 有 bug）: 4 个（Report 系列）
  - Stub 内部类定义方式不正确: ~5 个
  - 其他: ~3 个

### 4. 本轮修复内容

#### KSP 处理器修复
- `KspTypeUtils.kt`: 添加泛型参数处理（`ParameterizedTypeName`、`WildcardTypeName`）
- `KspTypeUtils.kt`: 添加内部类处理（`resolveClassName()` 通过 `parentDeclaration` 链）

#### 测试数据修复（app 中的 @Boom 注解补全）
- 补全 `key` 参数: 90 个文件（23 个手动 + 67 个脚本自动）
- 补全 `useFieldKey = true`: 2 个文件（StockRankDetailExpandFragment、UserCenterCommonBottomDialogFragment）
- 补全 `@MakeResult(includeStartForResult = true)`: 2 个文件（RecurringPlaceOrderActivity、WebullFundsDepositRecordDetailActivity2）

### 5. 测试用例（30 个文件）

- 位于 `app/src/main/java/com/joker/event/launcher/`
- 属性与 `docs/product/boom.txt` 前 30 条源文件完全一致（index、isOptional、key、desc）
- 覆盖类型：Activity(3)、Fragment(8)、DialogFragment(14)、Model(5)，含 1 个 Java 文件
- Bean stub 类按源项目真实包名创建（均实现 Serializable）

### 5.1 Router 测试用例（1 个文件）

- `RouterTestActivity.kt` — 带 `@Router` + `@Boom` 的 Activity，验证 Router 生成
- 生成 `RouterTestActivityLauncher.java`（标准 Launcher）+ `RouterTestActivity_XXXxxx.java`（Router）

### 6. Bean Stub 类包名对照

| Bean 类 | 包名 | 文件路径 |
|---------|------|---------|
| PLRangeBean / DateType | com.webull.commonmodule.datepick.bean | `app/.../datepick/bean/DatePickBeans.kt` |
| PostItemViewModel | com.webull.commonmodule.comment.ideas.viewmodel | `app/.../viewmodel/PostItemViewModel.kt` |
| FeedReportTypeItem | com.webull.commonmodule.networkinterface.socialapi.beans.common | `app/.../common/FeedReportTypeItem.kt` |
| ReportData | com.webull.commonmodule.comment.report | `app/.../report/ReportData.kt` |
| RanksData / RanksTabData | com.webull.rankstemplate.pojo | `app/.../pojo/RanksPojo.kt` |
| RanksCellConfig | com.webull.rankstemplate.bean | `app/.../bean/RanksCellConfig.kt` |
| TickerKey | com.webull.commonmodule.bean | `app/.../bean/TickerKey.kt` |
| MessageQuickOrderBean | com.webull.alert.technical.fragment.bean | `app/.../bean/MessageQuickOrderBean.kt` |
| AlertTypeBean | com.webull.alert.common.viewdata | `app/.../viewdata/AlertTypeBean.kt` |
| BaseWarningRuleBean | com.webull.alert.common.bean | `app/.../bean/BaseWarningRuleBean.kt` |
| ChartsDataType | com.webull.financechats.constants | `app/.../constants/ChartsDataType.kt` |
| ParamConsts | com.webull.commonmodule.jump.action | `app/.../action/ParamConsts.kt` |

## KSP 处理器核心文件

```
launcher-compiler-ksp/src/main/kotlin/launcher/
├── LauncherKspProcessor.kt          # 入口，扫描 @Boom/@MakeResult/@ParentCls/@Router
├── LauncherKspProvider.kt           # SymbolProcessorProvider
├── classbinding/
│   ├── ClassBinding.kt              # 类绑定数据模型（含 routerPath/cls）
│   ├── ClassBindingFactory.kt       # 解析注解构建 ClassBinding
│   └── KnownClassType.kt           # Activity/Fragment/BroadcastReceiver/Model 判断
├── codegeneration/
│   ├── ClassGeneration.kt           # 代码生成基类（含 addExtraToClass/addExtraTop 扩展点）
│   ├── IntentBinding.kt             # Intent 绑定基类（Activity/Model/BR 共用）
│   ├── ActivityGeneration.kt        # Activity Launcher 生成
│   ├── FragmentGeneration.kt        # Fragment Launcher 生成
│   ├── BroadcastReceiverGeneration.kt
│   ├── ModelGeneration.kt           # Model Launcher 生成
│   ├── RouterGeneration.kt          # Router _XXXxxx 生成（putRouter/jump/getActionScheme）
│   └── BindingHelpers.kt           # Bundle/Intent getter/setter 辅助
├── param/
│   ├── ParamType.kt                 # 参数类型枚举与类型解析
│   ├── ArgumentBinding.kt           # 参数绑定数据
│   ├── ArgumentFactory.kt           # 解析 @Boom 属性
│   └── FieldAccessor.kt            # 字段访问方式判断
├── utils/
│   ├── Utils.kt                     # 常量和工具方法（含 STRINGBUILDER）
│   └── KspTypeUtils.kt             # KSP 类型转 JavaPoet TypeName
└── error/
    └── Errors.kt                    # 错误信息常量
```

## 需要读取的关键文件（新对话开始时）

```
launcher-compiler-ksp/src/main/kotlin/launcher/LauncherKspProcessor.kt
launcher-compiler-ksp/src/main/kotlin/launcher/param/ParamType.kt
launcher-compiler-ksp/src/main/kotlin/launcher/param/FieldAccessor.kt
launcher-compiler-ksp/src/main/kotlin/launcher/codegeneration/ClassGeneration.kt
launcher-compiler-ksp/src/main/kotlin/launcher/codegeneration/IntentBinding.kt
launcher-compiler-ksp/src/main/kotlin/launcher/codegeneration/RouterGeneration.kt
launcher-compiler-ksp/src/main/kotlin/launcher/classbinding/ClassBindingFactory.kt
launcher-compiler-ksp/src/main/kotlin/launcher/classbinding/ClassBinding.kt
launcher-compiler-ksp/build.gradle
app/build.gradle
docs/product/boom.txt
```

## 待做（后续迁移）

- @Function → 生成 FunctionFactory
- @MarketViewRoute → 生成 MarketViewRouteFactory
- @TradeInterface → 生成 TradeInterfaceFactory
- @TradeServiceMaker → 生成 TradeServiceAggregator
- @Mapper 系列 → 生成 MapperImpl

## 使用方式

新对话开头粘贴：
```
继续 launcher-compiler-ksp 的 KAPT 转 KSP 迁移工作。请先读取 docs/CONTEXT-TRANSFER.md 了解当前状态。
```

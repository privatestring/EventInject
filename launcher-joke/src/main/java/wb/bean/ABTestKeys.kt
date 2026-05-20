package wb.bean

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2026/5/20
 *
 * 标注在 ABTest Key 类/object 上，KSP 编译时自动收集所有 String 类型属性，
 * 生成 `{ClassName}_getAllAbKeys(): List<String>` 顶层函数。
 *
 * 当 [generateProvider] = true 时，额外生成 AbTestProvider 实现类并自动注册到 ServiceAggregator。
 *
 * 支持 Kotlin object 和 Java class。
 *
 * 示例：
 * ```kotlin
 * @ABTestKeys(generateProvider = true)
 * object TradeCommonABTestKey : Serializable {
 *     val KEY_ENABLE_XXX: String = "key_enable_xxx"
 *
 *     @ABTestKeyExclude
 *     val KEY_INTERNAL_ONLY: String = "key_internal_only"  // 不会出现在 getAllAbKeys() 中
 * }
 *
 * // KSP 自动生成：
 * // 1. fun TradeCommonABTestKey_getAllAbKeys(): List<String> = listOf(...)
 * // 2. class TradeCommonABTestKey_AbTestProvider : AbTestProvider { override fun keys() = ... }
 * //    并自动注册到 ServiceAggregator
 * ```
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class ABTestKeys(
    /**
     * 是否自动生成 AbTestProvider 实现类并注册到 ServiceAggregator。
     * 默认 false，仅生成 getAllAbKeys() 函数。
     */
    val generateProvider: Boolean = false
)

/**
 * 标注在字段/属性上，表示该 key 不参与 getAllAbKeys() 的自动收集。
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class ABTestKeyExclude

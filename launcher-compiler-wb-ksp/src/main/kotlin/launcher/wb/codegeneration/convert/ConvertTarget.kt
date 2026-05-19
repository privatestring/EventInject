package launcher.wb.codegeneration.convert

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2025/5/19
 */

/** @AutoConvert 收集到的目标信息 */
data class ConvertTarget(
    val converterDecl: KSClassDeclaration,
    val sourceDecl: KSClassDeclaration,
    val targetDecl: KSClassDeclaration,
    val sourceType: KSType,
    val targetType: KSType,
    val functionName: String,
    val ignoreTargets: Set<String>,
    val packageName: String
)

/** 属性匹配结果 */
data class ConvertPropertyMatch(
    /** 当前类匹配成功 */
    val matched: List<MatchedProperty>,
    /** 父类匹配成功 */
    val parentMatched: List<MatchedProperty>,
    /** 当前类未匹配 */
    val unmatched: List<UnmatchedProperty>,
    /** 父类未匹配 */
    val parentUnmatched: List<UnmatchedProperty>,
    /** 被 ignoreTargets 忽略 */
    val ignored: List<String>
)

/** 匹配成功的属性 */
data class MatchedProperty(
    val name: String,
    /** 源类读取表达式，如 "this.orderId" 或 "this.getpValue1()" */
    val readExpr: String,
    /** 目标类写入表达式模板，{value} 为占位符，如 "target.orderId = {value}" 或 "target.setpValue1({value})" */
    val writeTemplate: String
)

/** 未匹配的属性 */
data class UnmatchedProperty(
    val name: String,
    val typeName: String,
    val reason: String
)

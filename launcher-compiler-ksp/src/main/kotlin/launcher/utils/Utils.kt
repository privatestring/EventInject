package launcher.utils

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec

// ============================================================
// JavaPoet 常用类型常量
// ============================================================
val INTENT = ClassName.get("android.content", "Intent")!!
val BUNDLE = ClassName.get("android.os", "Bundle")!!
val CONTEXT = ClassName.get("android.content", "Context")!!
val ACTIVITY = ClassName.get("android.app", "Activity")!!
val STRING = ClassName.get("java.lang", "String")!!
val STRINGBUILDER = ClassName.get("java.lang", "StringBuilder")!!
val CLAZZ = ClassName.get("java.lang", "Class")!!

// ============================================================
// 命名常量
// ============================================================
const val CLASS_NAME_END = "Launcher"                    // 生成类后缀：XxxLauncher
const val FIELD_NAME_END = "IntentKey"                   // 默认 key 后缀：xxxIntentKey
const val GET_INTENT_METHOD = "getIntentFrom"
const val ADD_INTENT_PARAMS = "addIntentParams"
const val BIND_THIS_CLASS = "bind"
const val START_METHOD_NAME = "startActivity"
const val START_RESULT_METHOD_NAME = "startForResult"

/** 驼峰转大写下划线：userName → USER_NAME */
fun camelCaseToUppercaseUnderscore(str: String): String = str
    .replace("([A-Z])".toRegex(), "_\$1")
    .uppercase()
    .let { if (it.isNotEmpty() && it[0] == '_') it.drop(1) else it }

/** 判断是否为 Java 基本类型（非 boxed），基本类型不需要 null 检查 */
fun TypeName.checkNotBox(): Boolean {
    return this == TypeName.VOID
            || this == TypeName.BOOLEAN
            || this == TypeName.BYTE
            || this == TypeName.SHORT
            || this == TypeName.INT
            || this == TypeName.LONG
            || this == TypeName.CHAR
            || this == TypeName.FLOAT
            || this == TypeName.DOUBLE
}

/**
 * 可选参数组合算法：对每个 optional 参数生成"包含"和"不包含"两个分支。
 * 例如 [A, B(opt), C] → [[A, B, C], [A, C]]
 *
 * 当 optional 参数超过 MAX_OPTIONAL_COMBINATIONS 个时，只生成全参数+仅必填两个版本，
 * 避免 2^N 指数级方法爆炸。
 */
private const val MAX_OPTIONAL_COMBINATIONS = 8

fun <T> List<T>.createSublists(isSplitter: (T) -> Boolean): List<List<T>> {
    val optionalCount = count { isSplitter(it) }
    // 超过上限时退化为两个版本：全参数 + 仅必填参数
    if (optionalCount > MAX_OPTIONAL_COMBINATIONS) {
        val requiredOnly = filter { !isSplitter(it) }
        return if (requiredOnly == this) listOf(this)
        else listOf(this, requiredOnly)
    }
    return createSublistsInternal(isSplitter)
}

private fun <T> List<T>.createSublistsInternal(isSplitter: (T) -> Boolean): List<List<T>> = when {
    isEmpty() -> listOf(listOf())
    none { isSplitter(it) } -> listOf(this)
    size == 1 -> listOf(this, listOf())
    isSplitter(last()) -> dropLast(1).createSublistsInternal(isSplitter)
        .flatMap { listOf(it + last(), it) }
    else -> dropLast(1).createSublistsInternal(isSplitter)
        .map { it + last() }
}

internal inline fun MethodSpec.Builder.doIf(check: Boolean, f: MethodSpec.Builder.() -> Unit) = apply {
    if (check) f()
}

internal inline fun TypeSpec.Builder.doIf(check: Boolean, f: TypeSpec.Builder.() -> Unit) = apply {
    if (check) f()
}

internal fun <T> List<T>.addIf(condition: Boolean, vararg e: T): List<T> {
    return if (condition) this + listOf(*e) else this
}

package launcher.utils

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec

val INTENT = ClassName.get("android.content", "Intent")!!
val BUNDLE = ClassName.get("android.os", "Bundle")!!
val CONTEXT = ClassName.get("android.content", "Context")!!
val ACTIVITY = ClassName.get("android.app", "Activity")!!
val STRING = ClassName.get("java.lang", "String")!!
val CLAZZ = ClassName.get("java.lang", "Class")!!

const val CLASS_NAME_END = "Launcher"
const val FIELD_NAME_END = "IntentKey"
const val GET_INTENT_METHOD = "getIntentFrom"
const val ADD_INTENT_PARAMS = "addIntentParams"
const val BIND_THIS_CLASS = "bind"
const val START_METHOD_NAME = "startActivity"
const val START_RESULT_METHOD_NAME = "startForResult"

fun camelCaseToUppercaseUnderscore(str: String): String = str
    .replace("([A-Z])".toRegex(), "_\$1")
    .uppercase()
    .let { if (it.isNotEmpty() && it[0] == '_') it.drop(1) else it }

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

fun <T> List<T>.createSublists(isSplitter: (T) -> Boolean): List<List<T>> = when {
    isEmpty() -> listOf(listOf())
    none { isSplitter(it) } -> listOf(this)
    size == 1 -> listOf(this, listOf())
    isSplitter(last()) -> dropLast(1).createSublists(isSplitter)
        .flatMap { listOf(it + last(), it) }
    else -> dropLast(1).createSublists(isSplitter)
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

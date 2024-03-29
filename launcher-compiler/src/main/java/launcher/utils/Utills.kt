package launcher.utils

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Element
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.lang.model.type.TypeVariable

val INTENT = ClassName.get("android.content", "Intent")
val BUNDLE = ClassName.get("android.os", "Bundle")
val CONTEXT = ClassName.get("android.content", "Context")
val ACTIVITY = ClassName.get("android.app", "Activity")
val STRING = ClassName.get("java.lang", "String")

val CLASS_NAME_END: String = "Launcher"
val FIELD_NAME_END: String = "IntentKey"
val GET_INTENT_METHOD: String = "getIntentFrom"
val ADD_INTENT_PARAMS: String = "addIntentParams"
val SAVE_BUNDLE: String = "saveBundle"
val BIND_THIS_CLASS: String = "bind"
val START_METHOD_NAME: String = "startActivity"
val START_RESULT_METHOD_NAME: String = "startForResult"

fun getElementType(element: Element): TypeMirror = element.asType()
        .let { if (it.kind == TypeKind.TYPEVAR) (it as TypeVariable).upperBound else it }

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

inline fun MethodSpec.Builder.doIf(check: Boolean, f: MethodSpec.Builder.() -> Unit) = apply {
    if (check) f()
}

inline fun TypeSpec.Builder.doIf(check: Boolean, f: TypeSpec.Builder.() -> Unit) = apply {
    if (check) f()
}

fun <T> List<T>.addIf(condition: Boolean, vararg e: T): List<T> {
    return if (condition) this + listOf(*e) else this
}

fun <T> List<T>.addIf(condition: Boolean, condition2: Boolean, vararg e: T): List<T> {
    return if (condition && condition2) this + listOf(*e) else this
}
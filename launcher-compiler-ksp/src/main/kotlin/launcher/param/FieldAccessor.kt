package launcher.param

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Origin
import java.util.Locale

/**
 * KSP 版字段访问器，判断字段的 setter/getter 访问方式。
 *
 * 关键差异：Kotlin 属性编译后 backing field 是 private 的，
 * 生成的 Java 代码必须通过 setter/getter 访问。
 * 只有 Java 源文件中的非 private 字段才能直接访问。
 */
class FieldAccessor(
    private val property: KSPropertyDeclaration,
    private val enclosingClass: KSClassDeclaration
) {
    private val fieldName: String = property.simpleName.asString()
    private val isKotlinSource: Boolean = property.origin == Origin.KOTLIN || property.origin == Origin.KOTLIN_LIB
    private val isExplicitlyPrivate: Boolean = property.modifiers.contains(Modifier.PRIVATE)
    private val setterType: FieldAccessType = resolveSetterType()
    private val getterType: FieldAccessType = resolveGetterType()

    fun isAccessible(): Boolean {
        // Kotlin 属性如果不是 private 的，一定有 setter（var）可以访问
        if (isKotlinSource && !isExplicitlyPrivate) return true
        return setterType != FieldAccessType.Inaccessible && getterType != FieldAccessType.Inaccessible
    }

    fun setToField(whatToSet: String): String = when (setterType) {
        FieldAccessType.Accessible -> "$fieldName = $whatToSet"
        FieldAccessType.ByMethod -> "set${capitalize(fieldName)}($whatToSet)"
        FieldAccessType.ByNoIsMethod -> "set${fieldName.substring(2)}($whatToSet)"
        FieldAccessType.Inaccessible -> throw Error(launcher.error.Errors.noSetter)
    }

    fun getFieldValue(): String = when (getterType) {
        FieldAccessType.Accessible -> fieldName
        FieldAccessType.ByMethod -> "get${capitalize(fieldName)}()"
        FieldAccessType.ByNoIsMethod -> "is${fieldName.substring(2)}()"
        FieldAccessType.Inaccessible -> throw Error(launcher.error.Errors.noSetter)
    }

    private fun resolveSetterType(): FieldAccessType {
        // Kotlin 源文件：属性编译后 backing field 是 private 的，必须通过 setter 访问
        if (isKotlinSource) {
            if (isExplicitlyPrivate) return FieldAccessType.Inaccessible
            // Kotlin boolean 属性以 "is" 开头时，setter 是 setXxx()（去掉 is 前缀）
            if (fieldName.length > 2 && fieldName.startsWith("is") && fieldName[2].isUpperCase()) {
                return FieldAccessType.ByNoIsMethod
            }
            // 普通 Kotlin var 属性自动生成 setXxx() 方法
            return FieldAccessType.ByMethod
        }
        // Java 源文件：沿用原有逻辑
        return when {
            !isExplicitlyPrivate -> FieldAccessType.Accessible
            hasNonPrivateMethod("set${capitalize(fieldName)}") -> FieldAccessType.ByMethod
            fieldName.length > 2 && fieldName.substring(0, 2) == "is" && hasNonPrivateMethod("set${fieldName.substring(2)}") -> FieldAccessType.ByNoIsMethod
            else -> FieldAccessType.Inaccessible
        }
    }

    private fun resolveGetterType(): FieldAccessType {
        // Kotlin 源文件：属性编译后 backing field 是 private 的，必须通过 getter 访问
        if (isKotlinSource) {
            if (isExplicitlyPrivate) return FieldAccessType.Inaccessible
            // Kotlin boolean 属性以 "is" 开头时，getter 就是 isXxx()
            if (fieldName.length > 2 && fieldName.startsWith("is") && fieldName[2].isUpperCase()) {
                return FieldAccessType.ByNoIsMethod
            }
            // 普通 Kotlin 属性自动生成 getXxx() 方法
            return FieldAccessType.ByMethod
        }
        // Java 源文件：沿用原有逻辑
        return when {
            !isExplicitlyPrivate -> FieldAccessType.Accessible
            hasNonPrivateMethod("get${capitalize(fieldName)}") -> FieldAccessType.ByMethod
            fieldName.length > 2 && fieldName.substring(0, 2) == "is" && hasNonPrivateMethod("is${fieldName.substring(2)}") -> FieldAccessType.ByNoIsMethod
            else -> FieldAccessType.Inaccessible
        }
    }

    private fun hasNonPrivateMethod(methodName: String): Boolean {
        return enclosingClass.getDeclaredFunctions().any { func ->
            func.simpleName.asString() == methodName && !func.modifiers.contains(Modifier.PRIVATE)
        }
    }

    private fun capitalize(str: String): String =
        str.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

    private enum class FieldAccessType {
        Accessible,
        ByMethod,
        ByNoIsMethod,
        Inaccessible
    }
}

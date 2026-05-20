package launcher.wb.codegeneration

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2026/5/20
 *
 * KSP 处理器公共工具方法。
 */

// ======================== 注解查找 ========================

/**
 * 通过全限定名查找类上的注解。
 * 比 shortName 匹配更安全，避免同名注解误匹配。
 *
 * @param qualifiedName 注解的全限定名（如 `Function::class.qualifiedName!!`）
 */
fun KSClassDeclaration.findAnnotation(qualifiedName: String): KSAnnotation? {
    return annotations.firstOrNull { anno ->
        anno.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName
    }
}

/**
 * 通过全限定名查找类上的注解（非空版本，找不到抛异常）。
 */
fun KSClassDeclaration.getAnnotation(qualifiedName: String): KSAnnotation {
    return findAnnotation(qualifiedName)
        ?: error("Annotation $qualifiedName not found on ${this.qualifiedName?.asString()}")
}

// ======================== 注解参数读取 ========================

/**
 * KSAnnotation 参数读取辅助扩展。
 *
 * 用法：
 * ```kotlin
 * val functionId = annotation.arg<String>("functionId")
 * val isInner = annotation.arg<Boolean>("isInner") ?: false
 * ```
 */
@Suppress("UNCHECKED_CAST")
inline fun <reified T> KSAnnotation.arg(name: String): T? {
    return arguments.firstOrNull { it.name?.asString() == name }?.value as? T
}

// ======================== KSClassDeclaration 扩展 ========================

/**
 * 将 KSClassDeclaration 转为 KotlinPoet ClassName，正确处理嵌套类。
 * 例如：OvernightRankCardView.Provider → ClassName("...view", "OvernightRankCardView", "Provider")
 */
fun KSClassDeclaration.toClassName(): ClassName {
    val names = mutableListOf(simpleName.asString())
    var parent = parentDeclaration
    while (parent is KSClassDeclaration) {
        names.add(0, parent.simpleName.asString())
        parent = parent.parentDeclaration
    }
    return ClassName(packageName.asString(), *names.toTypedArray())
}

// ======================== KotlinPoet 公共常量 ========================

/** @JvmStatic 注解 */
val JVM_STATIC: AnnotationSpec = AnnotationSpec.builder(JvmStatic::class).build()

/** @JvmField 注解 */
val JVM_FIELD: AnnotationSpec = AnnotationSpec.builder(JvmField::class).build()

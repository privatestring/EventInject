package launcher.utils

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName

/**
 * 将 KSP 的 KSType 转换为 JavaPoet 的 TypeName。
 * 处理 Kotlin→Java 类型映射、泛型、内部类、nullable boxing 等。
 */
fun KSType.toTypeName(): TypeName {
    val qualifiedName = declaration.qualifiedName?.asString() ?: return ClassName.get("java.lang", "Object")
    val nullable = isMarkedNullable

    // 基本类型映射（Kotlin → Java）
    val typeName = when (qualifiedName) {
        "kotlin.Int" -> TypeName.INT
        "kotlin.Long" -> TypeName.LONG
        "kotlin.Float" -> TypeName.FLOAT
        "kotlin.Double" -> TypeName.DOUBLE
        "kotlin.Boolean" -> TypeName.BOOLEAN
        "kotlin.Char" -> TypeName.CHAR
        "kotlin.Byte" -> TypeName.BYTE
        "kotlin.Short" -> TypeName.SHORT
        "kotlin.String", "java.lang.String" -> ClassName.get("java.lang", "String")
        "kotlin.CharSequence", "java.lang.CharSequence" -> ClassName.get("java.lang", "CharSequence")
        "kotlin.IntArray" -> ArrayTypeName.of(TypeName.INT)
        "kotlin.LongArray" -> ArrayTypeName.of(TypeName.LONG)
        "kotlin.FloatArray" -> ArrayTypeName.of(TypeName.FLOAT)
        "kotlin.DoubleArray" -> ArrayTypeName.of(TypeName.DOUBLE)
        "kotlin.BooleanArray" -> ArrayTypeName.of(TypeName.BOOLEAN)
        "kotlin.CharArray" -> ArrayTypeName.of(TypeName.CHAR)
        "kotlin.ByteArray" -> ArrayTypeName.of(TypeName.BYTE)
        "kotlin.ShortArray" -> ArrayTypeName.of(TypeName.SHORT)
        "kotlin.Array" -> {
            val elementType = arguments.firstOrNull()?.type?.resolve()
            if (elementType != null) ArrayTypeName.of(elementType.toTypeName())
            else ArrayTypeName.of(ClassName.get("java.lang", "Object"))
        }
        "java.util.ArrayList", "kotlin.collections.ArrayList" -> {
            val elementType = arguments.firstOrNull()?.type?.resolve()
            if (elementType != null) {
                ParameterizedTypeName.get(
                    ClassName.get("java.util", "ArrayList"),
                    elementType.toTypeName().box()
                )
            } else {
                ClassName.get("java.util", "ArrayList")
            }
        }
        "kotlin.collections.List", "kotlin.collections.MutableList", "java.util.List" -> {
            val elementType = arguments.firstOrNull()?.type?.resolve()
            if (elementType != null) {
                ParameterizedTypeName.get(
                    ClassName.get("java.util", "List"),
                    elementType.toTypeName().box()
                )
            } else {
                ClassName.get("java.util", "List")
            }
        }
        "kotlin.collections.Map", "kotlin.collections.MutableMap", "java.util.Map" -> {
            val keyType = arguments.getOrNull(0)?.type?.resolve()
            val valueType = arguments.getOrNull(1)?.type?.resolve()
            if (keyType != null && valueType != null) {
                ParameterizedTypeName.get(
                    ClassName.get("java.util", "Map"),
                    keyType.toTypeName().box(),
                    valueType.toTypeName().box()
                )
            } else {
                ClassName.get("java.util", "Map")
            }
        }
        "kotlin.collections.Set", "kotlin.collections.MutableSet", "java.util.Set" -> {
            val elementType = arguments.firstOrNull()?.type?.resolve()
            if (elementType != null) {
                ParameterizedTypeName.get(
                    ClassName.get("java.util", "Set"),
                    elementType.toTypeName().box()
                )
            } else {
                ClassName.get("java.util", "Set")
            }
        }
        "java.util.HashMap", "kotlin.collections.HashMap" -> {
            val keyType = arguments.getOrNull(0)?.type?.resolve()
            val valueType = arguments.getOrNull(1)?.type?.resolve()
            if (keyType != null && valueType != null) {
                ParameterizedTypeName.get(
                    ClassName.get("java.util", "HashMap"),
                    keyType.toTypeName().box(),
                    valueType.toTypeName().box()
                )
            } else {
                ClassName.get("java.util", "HashMap")
            }
        }
        "java.util.HashSet", "kotlin.collections.HashSet" -> {
            val elementType = arguments.firstOrNull()?.type?.resolve()
            if (elementType != null) {
                ParameterizedTypeName.get(
                    ClassName.get("java.util", "HashSet"),
                    elementType.toTypeName().box()
                )
            } else {
                ClassName.get("java.util", "HashSet")
            }
        }
        else -> {
            // 普通类（含内部类）
            val rawClassName = resolveClassName(declaration as? KSClassDeclaration, qualifiedName)

            // 处理泛型参数
            if (arguments.isNotEmpty()) {
                val typeArgs = arguments.mapNotNull { arg ->
                    val argType = arg.type?.resolve()
                    if (argType != null) {
                        // 处理 variance（out/in）
                        when (arg.variance) {
                            com.google.devtools.ksp.symbol.Variance.COVARIANT -> {
                                // out T → ? extends T
                                com.squareup.javapoet.WildcardTypeName.subtypeOf(argType.toTypeName().box())
                            }
                            com.google.devtools.ksp.symbol.Variance.CONTRAVARIANT -> {
                                // in T → ? super T
                                com.squareup.javapoet.WildcardTypeName.supertypeOf(argType.toTypeName().box())
                            }
                            else -> argType.toTypeName().box()
                        }
                    } else null
                }
                if (typeArgs.isNotEmpty()) {
                    ParameterizedTypeName.get(rawClassName, *typeArgs.toTypedArray())
                } else {
                    rawClassName
                }
            } else {
                rawClassName
            }
        }
    }

    // 如果是 nullable 且是基本类型，需要 box
    return if (nullable && typeName.isPrimitive) typeName.box() else typeName
}

/**
 * 解析 ClassName，正确处理内部类（JavaPoet 需要 OuterClass.InnerClass 格式）。
 */
private fun resolveClassName(classDecl: KSClassDeclaration?, qualifiedName: String): ClassName {
    if (classDecl != null) {
        // 收集嵌套类名链
        val names = mutableListOf<String>()
        var current: KSClassDeclaration? = classDecl
        while (current != null) {
            names.add(0, current.simpleName.asString())
            val parent = current.parentDeclaration
            current = parent as? KSClassDeclaration
        }
        if (names.size > 1) {
            // 内部类：第一个是外部类，其余是内部类
            val packageName = classDecl.packageName.asString()
            return ClassName.get(packageName, names[0], *names.drop(1).toTypedArray())
        }
    }
    // 普通类（非内部类）
    val parts = qualifiedName.split(".")
    val packageName = parts.dropLast(1).joinToString(".")
    val simpleName = parts.last()
    return ClassName.get(packageName, simpleName)
}

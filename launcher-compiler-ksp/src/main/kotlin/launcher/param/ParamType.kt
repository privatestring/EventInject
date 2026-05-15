package launcher.param

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.ClassKind

/**
 * 支持的参数类型枚举，与 KAPT 版本保持一致
 */
enum class ParamType {
    String,
    Int,
    Long,
    Float,
    Boolean,
    Double,
    Char,
    Byte,
    Short,
    CharSequence,

    BooleanArray,
    ByteArray,
    ShortArray,
    CharArray,
    IntArray,
    LongArray,
    FloatArray,
    DoubleArray,
    StringArray,
    CharSequenceArray,

    IntegerArrayList,
    StringArrayList,
    CharSequenceArrayList,

    ParcelableSubtype,
    SerializableSubtype,
    ParcelableArrayListSubtype;

    fun typeUsedBySupertype(): kotlin.Boolean = this in listOf(ParcelableSubtype, SerializableSubtype)

    fun isPrimitive(): kotlin.Boolean = when (this) {
        Int, Long, Float, Boolean, Double, Char, Byte, Short -> true
        else -> false
    }

    companion object {

        fun fromType(ksType: KSType): ParamType? {
            val qualifiedName = ksType.declaration.qualifiedName?.asString() ?: return null

            // Nullable 基本类型（Int?/Long?/Boolean? 等）在 KAPT 中是 boxed 类型（Integer/Long/Boolean），
            // 它们实现了 Serializable，所以 KAPT 将其识别为 SerializableSubtype。
            // KSP 中需要跳过基本类型匹配，让其走 getBySupertype 路径。
            val primitiveNames = setOf(
                "kotlin.Int", "kotlin.Long", "kotlin.Float", "kotlin.Double",
                "kotlin.Boolean", "kotlin.Char", "kotlin.Byte", "kotlin.Short"
            )
            if (ksType.isMarkedNullable && qualifiedName in primitiveNames) {
                return SerializableSubtype
            }

            // 基本类型和常见类型
            return getByQualifiedName(qualifiedName)
                ?: getArrayType(qualifiedName, ksType)
                ?: getArrayListType(qualifiedName, ksType)
                ?: getBySupertype(ksType)
        }

        private fun getByQualifiedName(name: kotlin.String): ParamType? = when (name) {
            "kotlin.Int", "java.lang.Integer" -> Int
            "kotlin.Long", "java.lang.Long" -> Long
            "kotlin.Float", "java.lang.Float" -> Float
            "kotlin.Double", "java.lang.Double" -> Double
            "kotlin.Boolean", "java.lang.Boolean" -> Boolean
            "kotlin.Char", "java.lang.Character" -> Char
            "kotlin.Byte", "java.lang.Byte" -> Byte
            "kotlin.Short", "java.lang.Short" -> Short
            "kotlin.String", "java.lang.String" -> String
            "kotlin.CharSequence", "java.lang.CharSequence" -> CharSequence
            "kotlin.IntArray" -> IntArray
            "kotlin.LongArray" -> LongArray
            "kotlin.FloatArray" -> FloatArray
            "kotlin.DoubleArray" -> DoubleArray
            "kotlin.BooleanArray" -> BooleanArray
            "kotlin.CharArray" -> CharArray
            "kotlin.ByteArray" -> ByteArray
            "kotlin.ShortArray" -> ShortArray
            "kotlin.Array" -> null // 需要进一步判断元素类型
            else -> null
        }

        private fun getArrayType(qualifiedName: kotlin.String, ksType: KSType): ParamType? {
            if (qualifiedName != "kotlin.Array") return null
            val elementType = ksType.arguments.firstOrNull()?.type?.resolve() ?: return null
            val elementName = elementType.declaration.qualifiedName?.asString() ?: return null
            return when (elementName) {
                "kotlin.String", "java.lang.String" -> StringArray
                "kotlin.CharSequence", "java.lang.CharSequence" -> CharSequenceArray
                else -> null
            }
        }

        private fun getArrayListType(qualifiedName: kotlin.String, ksType: KSType): ParamType? {
            if (qualifiedName != "java.util.ArrayList" && qualifiedName != "kotlin.collections.ArrayList") return null
            val elementType = ksType.arguments.firstOrNull()?.type?.resolve() ?: return null
            val elementName = elementType.declaration.qualifiedName?.asString() ?: return null
            return when (elementName) {
                "kotlin.Int", "java.lang.Integer" -> IntegerArrayList
                "kotlin.String", "java.lang.String" -> StringArrayList
                "kotlin.CharSequence", "java.lang.CharSequence" -> CharSequenceArrayList
                else -> {
                    // 检查元素是否是 Parcelable 的子类型
                    if (isSubtypeOf(elementType, "android.os.Parcelable")) ParcelableArrayListSubtype
                    // ArrayList 本身实现了 Serializable，如果元素也是 Serializable，整体作为 SerializableSubtype
                    else if (isSubtypeOf(elementType, "java.io.Serializable")) SerializableSubtype
                    else null
                }
            }
        }

        private fun getBySupertype(ksType: KSType): ParamType? = when {
            isSubtypeOf(ksType, "android.os.Parcelable") -> ParcelableSubtype
            isSubtypeOf(ksType, "java.io.Serializable") -> SerializableSubtype
            else -> null
        }

        fun isSubtypeOf(ksType: KSType, superTypeName: kotlin.String): kotlin.Boolean {
            val declaration = ksType.declaration
            if (declaration.qualifiedName?.asString() == superTypeName) return true
            if (declaration !is KSClassDeclaration) return false
            return declaration.superTypes.any { superTypeRef ->
                val resolved = superTypeRef.resolve()
                isSubtypeOf(resolved, superTypeName)
            }
        }
    }
}

package launcher.classbinding

import com.google.devtools.ksp.symbol.KSClassDeclaration
import launcher.param.ParamType

/**
 * KSP 版类型判断，通过递归检查父类型确定组件类型
 */
enum class KnownClassType(vararg val typeNames: String) {
    Activity("android.app.Activity"),
    Fragment("android.app.Fragment", "androidx.fragment.app.Fragment"),
    BroadcastReceiver("android.content.BroadcastReceiver"),
    Model("");

    companion object {
        fun getByType(classDeclaration: KSClassDeclaration): KnownClassType {
            return values().firstOrNull { type ->
                type.typeNames.any { typeName ->
                    typeName.isNotEmpty() && isSubtypeOf(classDeclaration, typeName)
                }
            } ?: Model
        }

        private fun isSubtypeOf(classDeclaration: KSClassDeclaration, superTypeName: String): Boolean {
            if (classDeclaration.qualifiedName?.asString() == superTypeName) return true
            return classDeclaration.superTypes.any { superTypeRef ->
                val resolved = superTypeRef.resolve()
                val declaration = resolved.declaration as? KSClassDeclaration ?: return@any false
                isSubtypeOf(declaration, superTypeName)
            }
        }
    }
}

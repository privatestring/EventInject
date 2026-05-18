package launcher.classbinding

import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2025/5/15
 *
 * Android 组件类型枚举，决定生成哪种 Launcher 代码。
 * 通过递归遍历父类型链判断类属于哪种组件。
 */
enum class KnownClassType(vararg val typeNames: String) {
    Activity("android.app.Activity"),                                      // → ActivityGeneration
    Fragment("android.app.Fragment", "androidx.fragment.app.Fragment"),     // → FragmentGeneration
    BroadcastReceiver("android.content.BroadcastReceiver"),                // → BroadcastReceiverGeneration
    Model("");                                                             // 兜底，非以上三种均为 Model

    companion object {
        fun getByType(classDeclaration: KSClassDeclaration): KnownClassType {
            return entries.firstOrNull { type ->
                type.typeNames.any { typeName ->
                    typeName.isNotEmpty() && isSubtypeOf(classDeclaration, typeName, mutableSetOf())
                }
            } ?: Model
        }

        /** 递归判断继承关系，visited 防止循环继承导致无限递归 */
        private fun isSubtypeOf(
            classDeclaration: KSClassDeclaration,
            superTypeName: String,
            visited: MutableSet<String>
        ): Boolean {
            val qualifiedName = classDeclaration.qualifiedName?.asString() ?: return false
            if (qualifiedName == superTypeName) return true
            if (!visited.add(qualifiedName)) return false // 已访问过，防环
            return classDeclaration.superTypes.any { superTypeRef ->
                val resolved = superTypeRef.resolve()
                val declaration = resolved.declaration as? KSClassDeclaration ?: return@any false
                isSubtypeOf(declaration, superTypeName, visited)
            }
        }
    }
}

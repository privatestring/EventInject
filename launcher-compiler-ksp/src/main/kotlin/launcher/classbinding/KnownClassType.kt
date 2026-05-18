package launcher.classbinding

import com.google.devtools.ksp.symbol.KSClassDeclaration
import launcher.utils.SubtypeChecker

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
                    typeName.isNotEmpty() && SubtypeChecker.isSubtypeOf(classDeclaration, typeName)
                }
            } ?: Model
        }
    }
}

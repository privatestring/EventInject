package launcher.classbinding

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import launcher.codegeneration.ActivityGeneration
import launcher.codegeneration.BroadcastReceiverGeneration
import launcher.codegeneration.ClassGeneration
import launcher.codegeneration.FragmentGeneration
import launcher.codegeneration.ModelGeneration
import launcher.param.ArgumentBinding
import launcher.utils.createSublists

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2025/5/15
 *
 * 一个被注解标注的类的完整绑定信息，包含类型、参数列表、注解配置等。
 * 由 [ClassBindingFactory] 构建，供代码生成器消费。
 */
internal class ClassBinding(
    val knownClassType: KnownClassType,       // Activity/Fragment/BroadcastReceiver/Model
    val targetTypeName: TypeName,             // 源类的 Java TypeName
    val bindingClassName: ClassName,          // 生成类名（如 XxxLauncher）
    val packageName: String,
    val argumentBindings: List<ArgumentBinding>, // 所有 @Boom 字段，按 index 排序
    val includeStartForResult: Boolean,       // @MakeResult.includeStartForResult
    val routerPath: String,                   // @Router.routerPath，为空表示无路由
    val isParentClass: Boolean,               // @ParentCls.isParentClass
    val cls: String?                          // @Router.cls 指定的目标类，null 表示使用当前类
) {
    /** 根据可选参数组合生成所有重载变体，按类型签名去重 */
    val argumentBindingVariants: List<List<ArgumentBinding>>
        get() = argumentBindings
            .createSublists { it.isOptional }
            .distinctBy { it.map { binding -> binding.typeName } }

    /** 根据组件类型选择对应的代码生成器 */
    internal fun getClassGeneration(): ClassGeneration = when (knownClassType) {
        KnownClassType.Activity -> ActivityGeneration(this)
        KnownClassType.Fragment -> FragmentGeneration(this)
        KnownClassType.BroadcastReceiver -> BroadcastReceiverGeneration(this)
        KnownClassType.Model -> ModelGeneration(this)
    }
}

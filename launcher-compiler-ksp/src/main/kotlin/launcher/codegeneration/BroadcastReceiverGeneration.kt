package launcher.codegeneration

import com.squareup.javapoet.MethodSpec
import launcher.classbinding.ClassBinding
import launcher.param.ArgumentBinding

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2025/5/15
 *
 * BroadcastReceiver Launcher 代码生成。
 * 生成：bind(receiver, intent) / getIntentFrom(context, params)
 * 注意：BroadcastReceiver 仅支持基本类型参数。
 */
internal class BroadcastReceiverGeneration(classBinding: ClassBinding) : IntentBinding(classBinding) {

    override fun createFillFieldsMethod() = fillByIntentBinding("receiver")

    override fun createStarters(variant: List<ArgumentBinding>): List<MethodSpec> = listOf(
        createGetIntentMethod(variant)
    )
}

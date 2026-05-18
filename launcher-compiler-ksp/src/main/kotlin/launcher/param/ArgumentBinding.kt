package launcher.param

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import launcher.utils.camelCaseToUppercaseUnderscore

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2025/5/15
 *
 * 单个 @Boom 字段的解析结果，包含参数名、key、类型、访问方式等信息。
 */
class ArgumentBinding(
    val name: String,                    // 字段名
    val key: String,                     // Intent/Bundle 存取的 key
    val paramType: ParamType,            // 参数类型枚举
    val typeName: TypeName,              // JavaPoet TypeName，用于生成代码
    val index: Int,                      // @Boom.index，决定方法参数顺序
    val isOptional: Boolean,             // @Boom.isOptional，可选参数会生成重载
    val accessor: FieldAccessor,         // 字段读写方式（直接/setter/getter）
    val annotationList: List<String>,    // 字段上的其他注解（排除 @Boom/@NotNull）
    val desc: String,                    // @Boom.desc，Router 跳转时必填
    val docString: String = ""           // 属性的 KDoc 注释，作为 desc 的 fallback
) {
    /** 生成的常量名，如 userName → USER_NAME_INTENT_KEY */
    val fieldName: String by lazy { camelCaseToUppercaseUnderscore(name) + "_INTENT_KEY" }

    /** 参数描述：优先使用 @Boom.desc，为空时 fallback 到属性 KDoc 注释 */
    val description: String get() = desc.ifEmpty { docString }

    /** 将注解全限定名转为 JavaPoet ClassName，用于生成参数注解 */
    val annotationCls: List<ClassName> by lazy {
        annotationList.mapNotNull { s ->
            runCatching {
                val list = s.split(".").filterNot { it.isEmpty() }
                ClassName.get(list.dropLast(1).joinToString("."), list.takeLast(1).joinToString("."))
            }.getOrNull()
        }
    }
}

package launcher.param

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import launcher.utils.camelCaseToUppercaseUnderscore

class ArgumentBinding(
    val name: String,
    val key: String,
    val paramType: ParamType,
    val typeName: TypeName,
    val index: Int,
    val isOptional: Boolean,
    val accessor: FieldAccessor,
    val annotationList: List<String>,
    val desc:String
) {
    val fieldName: String by lazy { camelCaseToUppercaseUnderscore(name) + "_INTENT_KEY" }

    val annotationCls: List<ClassName> by lazy {
        annotationList.mapNotNull { s ->
            kotlin.runCatching {
                val list = s.split(".").filterNot { it.isEmpty() }
                ClassName.get(list.dropLast(1).joinToString("."), list.takeLast(1).joinToString("."))
            }.getOrNull()
        }
    }
}
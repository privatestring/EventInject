package launcher.param

import launcher.param.FieldAccessor
import launcher.param.ParamType
import launcher.utils.camelCaseToUppercaseUnderscore
import com.squareup.javapoet.TypeName

class ArgumentBinding(
        val name: String,
        val key: String,
        val paramType: ParamType,
        val typeName: TypeName,
        val isOptional: Boolean,
        val accessor: FieldAccessor
) {
    val fieldName: String by lazy { camelCaseToUppercaseUnderscore(name) + "_INTENT_KEY" }
}
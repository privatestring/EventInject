package launcher.mapper

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

class PropertyResolver(private val processingEnv: ProcessingEnvironment) {

    private val readableCache = mutableMapOf<TypeElement, Map<String, ExecutableElement>>()
    private val writableCache = mutableMapOf<TypeElement, Map<String, ExecutableElement>>()
    private val writableFieldsCache = mutableMapOf<TypeElement, Map<String, javax.lang.model.element.VariableElement>>()

    fun readableProperties(type: TypeElement?): Map<String, ExecutableElement> {
        if (type == null) return emptyMap()
        return readableCache.getOrPut(type) { collectReadable(type) }
    }

    fun writeableProperties(type: TypeElement?): Map<String, ExecutableElement> {
        if (type == null) return emptyMap()
        return writableCache.getOrPut(type) { collectWritable(type) }
    }

    fun writableFields(type: TypeElement?): Map<String, javax.lang.model.element.VariableElement> {
        if (type == null) return emptyMap()
        return writableFieldsCache.getOrPut(type) { collectWritableFields(type) }
    }

    fun asTypeElement(type: TypeMirror?): TypeElement? {
        if (type == null) return null
        val element = processingEnv.typeUtils.asElement(type)
        return element as? TypeElement
    }

    fun findField(type: TypeElement?, fieldName: String): javax.lang.model.element.VariableElement? {
        if (type == null) return null
        return processingEnv.elementUtils.getAllMembers(type)
            .filter { it.kind == javax.lang.model.element.ElementKind.FIELD }
            .map { it as javax.lang.model.element.VariableElement }
            .firstOrNull {
                it.simpleName.toString() == fieldName &&
                        !it.modifiers.contains(javax.lang.model.element.Modifier.STATIC) &&
                        !it.modifiers.contains(javax.lang.model.element.Modifier.PRIVATE)
            }
    }

    private fun collectReadable(type: TypeElement): Map<String, ExecutableElement> {
        val methods = processingEnv.elementUtils.getAllMembers(type)
            .filter { it.kind == ElementKind.METHOD }
            .map { it as ExecutableElement }
            .filter { it.parameters.isEmpty() && !it.modifiers.contains(Modifier.STATIC) }

        val map = linkedMapOf<String, ExecutableElement>()
        methods.forEach { method ->
            val name = method.simpleName.toString()
            val property = when {
                name.startsWith("get") && name.length > 3 -> decap(name.substring(3))
                name.startsWith("is") && name.length > 2 -> decap(name.substring(2))
                else -> null
            }
            if (property != null) {
                // 如果已存在，优先使用getXxx而不是isXxx（getXxx更通用）
                if (!map.containsKey(property) || !name.startsWith("is")) {
                    map[property] = method
                }
            }
        }

        return map
    }

    private fun collectWritable(type: TypeElement): Map<String, ExecutableElement> {
        val map = linkedMapOf<String, ExecutableElement>()

        // 先收集setter方法
        val methods = processingEnv.elementUtils.getAllMembers(type)
            .filter { it.kind == ElementKind.METHOD }
            .map { it as ExecutableElement }
            .filter {
                it.parameters.size == 1 &&
                        it.simpleName.toString().startsWith("set") &&
                        !it.modifiers.contains(Modifier.STATIC)
            }
        methods.forEach { method ->
            val property = method.simpleName.toString().substring(3).takeIf { it.isNotEmpty() }?.let { decap(it) }
            if (property != null) {
                map[property] = method
            }
        }

        // 移除字段存在性检查：只要有 setter 就认为属性可写
        // 原因：父类的私有字段不会被 getAllMembers() 返回，但其 public setter 会被返回
        // 因此，对于父类私有字段 + public setter 的场景，如果检查字段存在性会导致 setter 被移除
        // 解决方案：完全依赖 getter/setter 的存在性，不要求必须有对应的字段

        return map
    }

    private fun collectWritableFields(type: TypeElement): Map<String, javax.lang.model.element.VariableElement> {
        val map = linkedMapOf<String, javax.lang.model.element.VariableElement>()

        // 先获取所有setter方法名，避免字段与setter冲突
        val setterMap = collectWritable(type)
        val setterPropertyNames = setterMap.keys.toSet()

        // 收集可写的字段（public且非final，且没有对应的setter）
        val fields = processingEnv.elementUtils.getAllMembers(type)
            .filter { it.kind == ElementKind.FIELD }
            .map { it as javax.lang.model.element.VariableElement }
            .filter {
                !it.modifiers.contains(Modifier.STATIC) &&
                        !it.modifiers.contains(Modifier.FINAL)
            }
        fields.forEach { field ->
            val fieldName = field.simpleName.toString()
            // 如果已经有对应的setter方法，跳过字段（优先使用setter）
            if (!setterPropertyNames.contains(fieldName)) {
                map[fieldName] = field
            }
        }

        return map
    }

    private fun decap(input: String): String {
        if (input.isEmpty()) return input
        return input.substring(0, 1).lowercase() + input.substring(1)
    }
}


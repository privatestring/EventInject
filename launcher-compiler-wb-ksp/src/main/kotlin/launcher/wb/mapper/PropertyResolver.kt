package launcher.wb.mapper

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2026/5/15
 *
 * KSP 版本的属性解析器。
 * 负责解析类的可读属性（getter）和可写属性（setter/field），
 * 用于 Mapper 的自动映射和显式映射。
 *
 * 与 KAPT 版本的区别：
 * - 使用 KSClassDeclaration 替代 TypeElement
 * - 使用 KSFunctionDeclaration 替代 ExecutableElement
 * - 使用 KSPropertyDeclaration 替代 VariableElement
 * - 通过 getAllFunctions()/getAllProperties() 获取包含继承的成员
 */
class PropertyResolver(private val logger: KSPLogger) {

    private val readableCache = mutableMapOf<String, Map<String, ReadableProperty>>()
    private val writableCache = mutableMapOf<String, Map<String, WritableProperty>>()
    private val writableFieldsCache = mutableMapOf<String, Map<String, WritableField>>()

    /**
     * 可读属性（通过 getter 方法访问）
     */
    data class ReadableProperty(
        val name: String,
        val getterName: String,
        val type: KSType
    )

    /**
     * 可写属性（通过 setter 方法访问）
     */
    data class WritableProperty(
        val name: String,
        val setterName: String,
        val paramType: KSType
    )

    /**
     * 可写字段（直接字段赋值）
     */
    data class WritableField(
        val name: String,
        val type: KSType,
        val declaration: KSPropertyDeclaration
    )

    /**
     * 获取类型的所有可读属性（getter 方法）
     */
    fun readableProperties(type: KSClassDeclaration?): Map<String, ReadableProperty> {
        if (type == null) return emptyMap()
        val key = type.qualifiedName?.asString() ?: return emptyMap()
        return readableCache.getOrPut(key) { collectReadable(type) }
    }

    /**
     * 获取类型的所有可写属性（setter 方法）
     */
    fun writeableProperties(type: KSClassDeclaration?): Map<String, WritableProperty> {
        if (type == null) return emptyMap()
        val key = type.qualifiedName?.asString() ?: return emptyMap()
        return writableCache.getOrPut(key) { collectWritable(type) }
    }

    /**
     * 获取类型的所有可写字段（public 非 final 字段，且没有对应 setter）
     */
    fun writableFields(type: KSClassDeclaration?): Map<String, WritableField> {
        if (type == null) return emptyMap()
        val key = type.qualifiedName?.asString() ?: return emptyMap()
        return writableFieldsCache.getOrPut(key) { collectWritableFields(type) }
    }

    /**
     * 将 KSType 解析为 KSClassDeclaration
     */
    fun asClassDeclaration(type: KSType?): KSClassDeclaration? {
        if (type == null) return null
        return type.declaration as? KSClassDeclaration
    }

    /**
     * 查找指定名称的字段
     */
    fun findField(type: KSClassDeclaration?, fieldName: String): KSPropertyDeclaration? {
        if (type == null) return null
        return getAllProperties(type).firstOrNull { prop ->
            prop.simpleName.asString() == fieldName &&
                    !prop.modifiers.contains(Modifier.PRIVATE) &&
                    prop.isMutable
        }
    }

    private fun collectReadable(type: KSClassDeclaration): Map<String, ReadableProperty> {
        val map = linkedMapOf<String, ReadableProperty>()

        // 收集所有 getter 方法（包括继承的）
        getAllFunctions(type).forEach { function ->
            val name = function.simpleName.asString()
            val params = function.parameters

            // 只处理无参方法
            if (params.isNotEmpty()) return@forEach
            // 跳过 static 方法
            if (function.modifiers.contains(Modifier.JAVA_STATIC)) return@forEach

            val returnType = function.returnType?.resolve() ?: return@forEach

            val property = when {
                name.startsWith("get") && name.length > 3 -> decap(name.substring(3))
                name.startsWith("is") && name.length > 2 -> decap(name.substring(2))
                else -> null
            }

            if (property != null) {
                // getXxx 优先于 isXxx
                if (!map.containsKey(property) || !name.startsWith("is")) {
                    map[property] = ReadableProperty(property, name, returnType)
                }
            }
        }

        // 对于 Kotlin 类，也收集 public 属性（它们可能没有显式的 getXxx 方法）
        getAllProperties(type).forEach { prop ->
            val propName = prop.simpleName.asString()
            if (!map.containsKey(propName) && !prop.modifiers.contains(Modifier.PRIVATE)) {
                val propType = prop.type.resolve()
                // Kotlin 属性的 getter 名称：getXxx() 或 isXxx()（Boolean）
                val getterName = if (propType.declaration.qualifiedName?.asString() == "kotlin.Boolean") {
                    "is${cap(propName)}"
                } else {
                    "get${cap(propName)}"
                }
                map[propName] = ReadableProperty(propName, getterName, propType)
            }
        }

        return map
    }

    private fun collectWritable(type: KSClassDeclaration): Map<String, WritableProperty> {
        val map = linkedMapOf<String, WritableProperty>()

        // 收集所有 setter 方法（包括继承的）
        getAllFunctions(type).forEach { function ->
            val name = function.simpleName.asString()
            val params = function.parameters

            // 只处理单参数方法
            if (params.size != 1) return@forEach
            // 必须以 set 开头
            if (!name.startsWith("set") || name.length <= 3) return@forEach
            // 跳过 static 方法
            if (function.modifiers.contains(Modifier.JAVA_STATIC)) return@forEach

            val property = decap(name.substring(3))
            val paramType = params.first().type.resolve()
            map[property] = WritableProperty(property, name, paramType)
        }

        // 对于 Kotlin 类，也收集 var 属性（它们有隐式 setter）
        getAllProperties(type).forEach { prop ->
            val propName = prop.simpleName.asString()
            if (!map.containsKey(propName) && prop.isMutable && !prop.modifiers.contains(Modifier.PRIVATE)) {
                val propType = prop.type.resolve()
                val setterName = "set${cap(propName)}"
                map[propName] = WritableProperty(propName, setterName, propType)
            }
        }

        return map
    }

    private fun collectWritableFields(type: KSClassDeclaration): Map<String, WritableField> {
        val map = linkedMapOf<String, WritableField>()
        val setterPropertyNames = collectWritable(type).keys

        // 收集可写字段（public、非 final、没有对应 setter）
        getAllProperties(type).forEach { prop ->
            val propName = prop.simpleName.asString()
            if (prop.modifiers.contains(Modifier.PRIVATE)) return@forEach
            if (!prop.isMutable) return@forEach
            // 如果已有 setter，跳过
            if (setterPropertyNames.contains(propName)) return@forEach

            val propType = prop.type.resolve()
            map[propName] = WritableField(propName, propType, prop)
        }

        return map
    }

    /**
     * 获取类的所有函数（包括继承的）
     */
    private fun getAllFunctions(type: KSClassDeclaration): Sequence<KSFunctionDeclaration> {
        return type.getAllFunctions()
    }

    /**
     * 获取类的所有属性（包括继承的）
     */
    private fun getAllProperties(type: KSClassDeclaration): Sequence<KSPropertyDeclaration> {
        return type.getAllProperties()
    }

    private fun decap(input: String): String {
        if (input.isEmpty()) return input
        return input.substring(0, 1).lowercase() + input.substring(1)
    }

    private fun cap(input: String): String {
        if (input.isEmpty()) return input
        return input.substring(0, 1).uppercase() + input.substring(1)
    }
}

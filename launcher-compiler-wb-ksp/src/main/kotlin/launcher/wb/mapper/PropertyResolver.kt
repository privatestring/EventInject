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
import com.google.devtools.ksp.symbol.Origin

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
     * 可读属性（通过 getter 方法或字段直接访问）
     */
    data class ReadableProperty(
        val name: String,
        val getterName: String,
        val type: KSType,
        val isFieldAccess: Boolean = false
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

        // 收集所有显式定义的 getter 方法（包括继承的）
        val explicitGetters = mutableSetOf<String>()
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
                    explicitGetters += property
                }
            }
        }

        // 对于 Kotlin 源文件的类，收集 public 属性（它们有隐式 getter）
        // 对于 Java 源文件的类，public 字段没有显式 getter 时用字段直接访问
        getAllProperties(type).forEach { prop ->
            val propName = prop.simpleName.asString()
            if (map.containsKey(propName)) return@forEach
            if (prop.modifiers.contains(Modifier.PRIVATE)) return@forEach

            val propType = prop.type.resolve()

            // 判断属性来源
            val isFromJava = prop.origin == Origin.JAVA || prop.origin == Origin.JAVA_LIB
            if (isFromJava && !explicitGetters.contains(propName)) {
                // Java public 字段没有显式 getter，用字段名直接访问
                map[propName] = ReadableProperty(propName, propName, propType, isFieldAccess = true)
            } else if (!isFromJava) {
                // Kotlin 属性有隐式 getter，检查 @JvmName 注解
                val getterName = resolveGetterJvmName(prop) ?: if (propType.declaration.qualifiedName?.asString() == "kotlin.Boolean") {
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

        // 收集所有显式定义的 setter 方法（包括继承的）
        val explicitSetters = mutableSetOf<String>()
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
            explicitSetters += property
        }

        // 对于 Kotlin 源文件的类，收集 var 属性（它们有隐式 setter）
        // 对于 Java 源文件的类，public 字段不应该走 setter 路径（应走 field 直接赋值）
        getAllProperties(type).forEach { prop ->
            val propName = prop.simpleName.asString()
            if (map.containsKey(propName)) return@forEach
            if (!prop.isMutable) return@forEach
            if (prop.modifiers.contains(Modifier.PRIVATE)) return@forEach

            // 判断属性来源：如果属性来自 Java 源文件且没有显式 setter，跳过（留给 writableFields）
            val isFromJava = prop.origin == Origin.JAVA || prop.origin == Origin.JAVA_LIB
            if (isFromJava && !explicitSetters.contains(propName)) {
                // Java public 字段没有显式 setter，不收集到 writeableProperties
                return@forEach
            }

            val propType = prop.type.resolve()
            // Kotlin 属性检查 @JvmName 注解
            val setterName = resolveSetterJvmName(prop) ?: "set${cap(propName)}"
            map[propName] = WritableProperty(propName, setterName, propType)
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

    /**
     * 解析 Kotlin 属性 getter 上的 @JvmName 注解值
     */
    private fun resolveGetterJvmName(prop: KSPropertyDeclaration): String? {
        // 检查属性 getter 上的 @JvmName
        prop.getter?.annotations?.forEach { anno ->
            if (anno.shortName.asString() == "JvmName") {
                val name = anno.arguments.firstOrNull { it.name?.asString() == "name" }?.value as? String
                if (!name.isNullOrBlank()) return name
            }
        }
        // 检查属性本身的 @get:JvmName（KSP 中可能直接在属性注解上）
        prop.annotations.forEach { anno ->
            if (anno.shortName.asString() == "JvmName" && anno.useSiteTarget?.name == "GET") {
                val name = anno.arguments.firstOrNull { it.name?.asString() == "name" }?.value as? String
                if (!name.isNullOrBlank()) return name
            }
        }
        return null
    }

    /**
     * 解析 Kotlin 属性 setter 上的 @JvmName 注解值
     */
    private fun resolveSetterJvmName(prop: KSPropertyDeclaration): String? {
        // 检查属性 setter 上的 @JvmName
        prop.setter?.annotations?.forEach { anno ->
            if (anno.shortName.asString() == "JvmName") {
                val name = anno.arguments.firstOrNull { it.name?.asString() == "name" }?.value as? String
                if (!name.isNullOrBlank()) return name
            }
        }
        // 检查属性本身的 @set:JvmName（KSP 中可能直接在属性注解上）
        prop.annotations.forEach { anno ->
            if (anno.shortName.asString() == "JvmName" && anno.useSiteTarget?.name == "SET") {
                val name = anno.arguments.firstOrNull { it.name?.asString() == "name" }?.value as? String
                if (!name.isNullOrBlank()) return name
            }
        }
        return null
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

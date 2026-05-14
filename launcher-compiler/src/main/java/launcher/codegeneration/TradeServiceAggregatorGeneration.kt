package launcher.codegeneration

import com.squareup.javapoet.*
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import kotlin.collections.distinctBy
import kotlin.collections.forEach
import kotlin.collections.sortedBy

/**
 * TradeServiceAggregator 注解处理器的代码生成类
 *
 * 用于生成聚合所有 Trade 接口的服务接口
 * 例如生成 ITradeManagerService，自动继承所有继承自 ITradeInterface 的顶层大接口
 */
class TradeServiceAggregatorGeneration(
    private val processingEnv: ProcessingEnvironment,
    private val roundEnv: RoundEnvironment,
    private val baseInterfaceType: TypeMirror,
    private val scanPackages: List<String>,
    private val additionalInterfaceTypes: List<TypeMirror>,
    private val targetPackageName: String,
    private val targetClassName: String
) {
    private val typeUtils: Types = processingEnv.typeUtils
    private val elementUtils: Elements = processingEnv.elementUtils

    /**
     * 生成 Java 文件
     */
    fun brewJava(): JavaFile {
        val interfaceSpec = createInterface()
        return JavaFile.builder(targetPackageName, interfaceSpec)
            .addFileComment("Generated code from TradeServiceAggregator annotation processor!\n")
            .addFileComment("Do not modify!")
            .build()
    }

    /**
     * 创建接口定义
     */
    private fun createInterface(): TypeSpec {
        val topLevelInterfaces = findTopLevelInterfaces()

        val interfaceBuilder = TypeSpec.interfaceBuilder(targetClassName)
            .addModifiers(Modifier.PUBLIC)
            .addJavadoc("交易模块整体对外接口\n")
            .addJavadoc("自动生成，继承所有 Trade 模块的顶层接口\n")
            .addJavadoc("\n")
            .addJavadoc("继承的接口:\n")

        // 添加顶层接口的继承（按字母顺序排序，便于比较和稳定输出）
        topLevelInterfaces.sortedBy { getSimpleName(it) }.forEach { interfaceType ->
            val typeName = TypeName.get(interfaceType)
            interfaceBuilder.addSuperinterface(typeName)
            interfaceBuilder.addJavadoc(" - \$L\n", getSimpleName(interfaceType))
        }

        // 添加额外的接口
        additionalInterfaceTypes.forEach { interfaceType ->
            val typeName = TypeName.get(interfaceType)
            interfaceBuilder.addSuperinterface(typeName)
            interfaceBuilder.addJavadoc(" - \$L (additional)\n", getSimpleName(interfaceType))
        }

        return interfaceBuilder.build()
    }

    /**
     * 查找所有继承自 baseInterface 的顶层大接口
     *
     * 顶层大接口的定义：
     * 1. 直接或间接继承自 baseInterface
     * 2. 不被其他继承自 baseInterface 的接口所继承（即它是"最大"的聚合接口）
     *
     * 例如：
     * - ITradeAccountInterface 继承了 ITradeAccountInfoInterface + ITradeInterface
     * - ITradeAccountInfoInterface 也继承了 ITradeInterface
     * - 我们只需要 ITradeAccountInterface（大接口），不需要 ITradeAccountInfoInterface（小接口）
     */
    private fun findTopLevelInterfaces(): List<TypeMirror> {
        val baseElement = typeUtils.asElement(baseInterfaceType) as? TypeElement ?: return emptyList()

        // 收集所有继承自 baseInterface 的接口
        val allSubInterfaces = findAllSubInterfaces(baseElement)

        // 过滤出顶层大接口（不被其他大接口继承的接口）
        return filterTopLevelInterfaces(allSubInterfaces)
    }

    /**
     * 查找所有继承自 baseInterface 的接口
     *
     * 通过扫描指定的包及其所有子包来查找所有接口
     * 使用 RoundEnvironment 遍历所有根元素，递归查找所有类型元素
     * 这样可以找到所有在编译时可见的类型（包括外部库中的类型，只要它们在 classpath 中）
     */
    private fun findAllSubInterfaces(baseElement: TypeElement): List<TypeElement> {
        val result = mutableListOf<TypeElement>()

        // 构建包名前缀集合，用于快速匹配
        val packagePrefixes = scanPackages.map { it.trim() }.filter { it.isNotEmpty() }

        if (packagePrefixes.isEmpty()) {
            processingEnv.messager.printMessage(
                javax.tools.Diagnostic.Kind.WARNING,
                "TradeServiceAggregator: scanPackages is empty"
            )
            return result
        }

        processingEnv.messager.printMessage(
            javax.tools.Diagnostic.Kind.NOTE,
            "TradeServiceAggregator: Scanning packages: $packagePrefixes"
        )

        // 方法1: 遍历 RoundEnvironment 中的所有根元素
        // rootElements 包含所有在编译时可见的根类型（包括外部库中的类型）
        var rootElementsCount = 0
        for (element in roundEnv.rootElements) {
            rootElementsCount++
            // 递归遍历元素树，查找所有类型元素
            collectInterfacesFromElement(element, packagePrefixes, baseElement, result)
        }

        processingEnv.messager.printMessage(
            javax.tools.Diagnostic.Kind.NOTE,
            "TradeServiceAggregator: Processed $rootElementsCount root elements, found ${result.size} interfaces so far"
        )

        // 方法2: 通过包名直接查找包元素（作为补充）
        // 这样可以找到一些可能不在 rootElements 中的类型
        for (pkgPrefix in packagePrefixes) {
            val packageElement = elementUtils.getPackageElement(pkgPrefix)
            if (packageElement != null) {
                processingEnv.messager.printMessage(
                    javax.tools.Diagnostic.Kind.NOTE,
                    "TradeServiceAggregator: Found package element for $pkgPrefix, scanning enclosed elements"
                )
                // 扫描包中的所有直接类型元素
                for (enclosed in packageElement.enclosedElements) {
                    if (enclosed is TypeElement && enclosed.kind.isInterface) {
                        val packageName = elementUtils.getPackageOf(enclosed).qualifiedName.toString()
                        val matches = packagePrefixes.any { prefix ->
                            packageName == prefix || packageName.startsWith("$prefix.")
                        }
                        if (matches) {
                            processingEnv.messager.printMessage(
                                javax.tools.Diagnostic.Kind.NOTE,
                                "TradeServiceAggregator: Found interface ${enclosed.qualifiedName} in package $packageName"
                            )
                            if (isSubtypeOfInterface(enclosed, baseElement) &&
                                enclosed.qualifiedName.toString() != baseElement.qualifiedName.toString()) {
                                result.add(enclosed)
                                processingEnv.messager.printMessage(
                                    javax.tools.Diagnostic.Kind.NOTE,
                                    "TradeServiceAggregator: Added interface ${enclosed.qualifiedName}"
                                )
                            }
                        }
                    }
                }
            } else {
                processingEnv.messager.printMessage(
                    javax.tools.Diagnostic.Kind.WARNING,
                    "TradeServiceAggregator: Package element not found for $pkgPrefix"
                )
            }
        }

        processingEnv.messager.printMessage(
            javax.tools.Diagnostic.Kind.NOTE,
            "TradeServiceAggregator: Total interfaces found: ${result.size}"
        )

        return result.distinctBy { it.qualifiedName.toString() }
    }

    /**
     * 递归遍历元素树，收集匹配包名的接口
     */
    private fun collectInterfacesFromElement(
        element: Element,
        packagePrefixes: List<String>,
        baseElement: TypeElement,
        result: MutableList<TypeElement>
    ) {
        // 如果是类型元素（类或接口）
        if (element is TypeElement) {
            // 检查是否是接口
            if (element.kind.isInterface) {
                val packageName = elementUtils.getPackageOf(element).qualifiedName.toString()

                // 检查包名是否匹配任何一个 scanPackages 前缀（包括子包）
                val matches = packagePrefixes.any { prefix ->
                    packageName == prefix || packageName.startsWith("$prefix.")
                }

                if (matches) {
                    // 检查是否直接或间接继承自 baseInterface（排除 baseInterface 自身）
                    if (isSubtypeOfInterface(element, baseElement) &&
                        element.qualifiedName.toString() != baseElement.qualifiedName.toString()) {
                        result.add(element)
                    }
                }
            }

            // 递归遍历内部元素（内部类、内部接口等）
            for (enclosed in element.enclosedElements) {
                collectInterfacesFromElement(enclosed, packagePrefixes, baseElement, result)
            }
        }
        // 如果是包元素
        else if (element is PackageElement) {
            // 递归遍历包中的所有元素
            for (enclosed in element.enclosedElements) {
                collectInterfacesFromElement(enclosed, packagePrefixes, baseElement, result)
            }
        }
    }

    /**
     * 检查 element 是否继承自 baseInterface
     */
    private fun isSubtypeOfInterface(element: TypeElement, baseElement: TypeElement): Boolean {
        val elementType = element.asType()
        val baseType = baseElement.asType()
        return typeUtils.isSubtype(typeUtils.erasure(elementType), typeUtils.erasure(baseType))
    }

    /**
     * 过滤出顶层大接口
     *
     * 规则：如果接口 A 继承了接口 B，且 A 和 B 都继承自 baseInterface，
     * 那么 B 不是顶层接口（因为它已经被 A 包含了）
     */
    private fun filterTopLevelInterfaces(allInterfaces: List<TypeElement>): List<TypeMirror> {
        val result = mutableListOf<TypeMirror>()

        for (candidate in allInterfaces) {
            var isTopLevel = true

            for (other in allInterfaces) {
                if (candidate == other) continue

                // 检查 other 是否继承了 candidate
                // 如果是，说明 candidate 已经被 other 包含，candidate 不是顶层接口
                if (extendsInterface(other, candidate)) {
                    isTopLevel = false
                    break
                }
            }

            if (isTopLevel) {
                result.add(candidate.asType())
            }
        }

        return result
    }

    /**
     * 检查 subInterface 是否直接或间接继承了 superInterface
     */
    private fun extendsInterface(subInterface: TypeElement, superInterface: TypeElement): Boolean {
        // 获取 subInterface 的所有直接父接口
        for (parentType in subInterface.interfaces) {
            val parentElement = typeUtils.asElement(parentType) as? TypeElement ?: continue

            if (parentElement.qualifiedName.toString() == superInterface.qualifiedName.toString()) {
                return true
            }

            // 递归检查
            if (extendsInterface(parentElement, superInterface)) {
                return true
            }
        }

        return false
    }

    /**
     * 获取类型的简单名称
     */
    private fun getSimpleName(type: TypeMirror): String {
        val element = typeUtils.asElement(type) as? TypeElement
        return element?.simpleName?.toString() ?: type.toString()
    }
}


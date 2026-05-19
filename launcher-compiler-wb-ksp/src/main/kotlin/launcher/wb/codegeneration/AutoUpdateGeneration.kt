package launcher.wb.codegeneration

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import launcher.wb.codegeneration.update.AutoUpdateCodeBuilder
import launcher.wb.codegeneration.update.AutoUpdatePropertyCollector
import launcher.wb.codegeneration.update.AutoUpdateTarget
import wb.bean.AutoUpdate

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2025/5/18
 *
 * AutoUpdate 代码生成器。
 * 扫描所有 @AutoUpdate 注解的类，为每个类生成一个纯字段赋值的扩展函数。
 */
class AutoUpdateGeneration(
    codeGenerator: CodeGenerator,
    logger: KSPLogger
) : BaseGeneration(codeGenerator, logger) {

    companion object {
        private val AUTO_UPDATE_QUALIFIED_NAME = AutoUpdate::class.qualifiedName!!
    }

    private val targetClasses = mutableListOf<AutoUpdateTarget>()
    private val propertyCollector = AutoUpdatePropertyCollector(logger)
    private val codeBuilder = AutoUpdateCodeBuilder()

    override fun collect(resolver: Resolver): List<KSAnnotated> {
        val unprocessed = mutableListOf<KSAnnotated>()

        resolver.getSymbolsWithAnnotation(AUTO_UPDATE_QUALIFIED_NAME).forEach { symbol ->
            if (!symbol.validate()) {
                unprocessed += symbol
                return@forEach
            }
            if (symbol is KSClassDeclaration) {
                val anno = symbol.annotations.first {
                    it.annotationType.resolve().declaration.qualifiedName?.asString() == AUTO_UPDATE_QUALIFIED_NAME
                }

                val functionName = anno.arg<String>(AutoUpdate::functionName.name).orEmpty()
                val stringCheck = anno.arg<String>(AutoUpdate::stringCheck.name) ?: AutoUpdate.DEFAULT_STRING_CHECK
                val stringCheckImport = anno.arg<String>(AutoUpdate::stringCheckImport.name) ?: AutoUpdate.DEFAULT_STRING_CHECK_IMPORT
                val generateCopy = anno.arg<Boolean>(AutoUpdate::generateCopy.name) ?: false

                // 使用类所在的包名作为生成代码的包名
                val packageName = symbol.packageName.asString()

                val (parentClassName, parentFunctionName) = resolveParentFromSuperTypes(symbol)

                targetClasses.add(
                    AutoUpdateTarget(
                        classDecl = symbol,
                        functionName = functionName,
                        parentClassName = parentClassName,
                        parentFunctionName = parentFunctionName,
                        packageName = packageName,
                        stringCheck = stringCheck,
                        stringCheckImport = stringCheckImport,
                        generateCopy = generateCopy
                    )
                )
            }
        }

        return unprocessed
    }

    override fun hasDataToGenerate(): Boolean = targetClasses.isNotEmpty()

    override fun generate() {
        for (target in targetClasses) {
            val properties = propertyCollector.collectProperties(target.classDecl)
            val copyProperties = if (target.generateCopy) {
                propertyCollector.collectAllProperties(target.classDecl)
            } else {
                emptyList()
            }
            val fileSpec = codeBuilder.buildFileSpec(target, properties, copyProperties)
            writeKotlinFile(
                fileSpec = fileSpec,
                dependencies = buildDependencies(aggregating = false, listOf(target.classDecl))
            )
        }
    }

    /**
     * 从类的 superTypes 中自动推断父类。
     * 查找直接父类中是否标注了 @AutoUpdate，如果是则返回其全限定名和 functionName。
     * @return Pair(父类全限定名, 父类 functionName)，无父类时返回 (null, null)
     */
    private fun resolveParentFromSuperTypes(classDecl: KSClassDeclaration): Pair<String?, String?> {
        for (superType in classDecl.superTypes) {
            val superDecl = superType.resolve().declaration as? KSClassDeclaration ?: continue
            val qualifiedName = superDecl.qualifiedName?.asString() ?: continue
            if (qualifiedName == "kotlin.Any" || qualifiedName == "java.lang.Object"
                || qualifiedName == "java.io.Serializable") continue
            val autoUpdateAnno = superDecl.annotations.firstOrNull {
                it.annotationType.resolve().declaration.qualifiedName?.asString() == AUTO_UPDATE_QUALIFIED_NAME
            }
            if (autoUpdateAnno != null) {
                val parentFuncName = autoUpdateAnno.arg<String>(AutoUpdate::functionName.name).orEmpty()
                return qualifiedName to parentFuncName
            }
        }
        return null to null
    }
}

/**
 * KSAnnotation 参数读取辅助扩展。
 */
@Suppress("UNCHECKED_CAST")
internal inline fun <reified T> com.google.devtools.ksp.symbol.KSAnnotation.arg(name: String): T? {
    return arguments.firstOrNull { it.name?.asString() == name }?.value as? T
}

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
                        stringCheckImport = stringCheckImport
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
            val fileSpec = codeBuilder.buildFileSpec(target, properties)
            writeKotlinFile(
                fileSpec = fileSpec,
                dependencies = buildDependencies(aggregating = false, listOf(target.classDecl))
            )
        }
        generateReport()
    }

    private fun generateReport() {
        val withParent = targetClasses.count { it.parentClassName != null }
        val lines = mutableListOf<String>()
        lines += "Classes         : ${targetClasses.size} annotated"
        lines += "With parent     : $withParent (inherits update logic)"
        lines += "Standalone      : ${targetClasses.size - withParent}"
        // 提取模块名（取第一个类的路径）
        val moduleName = targetClasses.firstOrNull()?.classDecl?.let { extractModuleName(it) } ?: "Unknown"
        emitReport("AutoUpdate", moduleName, lines, "Total: ${targetClasses.size} update functions generated")
    }

    /**
     * 从类的 superTypes 中自动推断父类。
     * 递归查找继承链中最近的标注了 @AutoUpdate 的祖先类。
     * @return Pair(父类全限定名, 父类 functionName)，无父类时返回 (null, null)
     */
    private fun resolveParentFromSuperTypes(classDecl: KSClassDeclaration): Pair<String?, String?> {
        return resolveParentRecursive(classDecl, mutableSetOf())
    }

    private fun resolveParentRecursive(
        classDecl: KSClassDeclaration,
        visited: MutableSet<String>
    ): Pair<String?, String?> {
        for (superType in classDecl.superTypes) {
            val superDecl = superType.resolve().declaration as? KSClassDeclaration ?: continue
            val qualifiedName = superDecl.qualifiedName?.asString() ?: continue
            if (qualifiedName == "kotlin.Any" || qualifiedName == "java.lang.Object"
                || qualifiedName == "java.io.Serializable") continue
            if (!visited.add(qualifiedName)) continue

            val autoUpdateAnno = superDecl.annotations.firstOrNull {
                it.annotationType.resolve().declaration.qualifiedName?.asString() == AUTO_UPDATE_QUALIFIED_NAME
            }
            if (autoUpdateAnno != null) {
                val parentFuncName = autoUpdateAnno.arg<String>(AutoUpdate::functionName.name).orEmpty()
                return qualifiedName to parentFuncName
            }

            // 递归向上查找
            val result = resolveParentRecursive(superDecl, visited)
            if (result.first != null) return result
        }
        return null to null
    }
}

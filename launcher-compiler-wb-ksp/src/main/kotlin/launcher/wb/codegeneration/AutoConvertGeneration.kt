package launcher.wb.codegeneration

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.validate
import launcher.wb.codegeneration.convert.ConvertCodeBuilder
import launcher.wb.codegeneration.convert.ConvertPropertyMatcher
import launcher.wb.codegeneration.convert.ConvertTarget
import wb.bean.AutoConvert
import wb.bean.AutoConvertLifecycle

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2025/5/19
 *
 * AutoConvert 代码生成器。
 * 扫描所有 @AutoConvert 注解的类，从 ConvertLifecycle<S, T> 泛型参数中推断源类和目标类，
 * 为源类生成 convertTo{TargetClassName} 扩展函数。
 */
class AutoConvertGeneration(
    codeGenerator: CodeGenerator,
    logger: KSPLogger
) : BaseGeneration(codeGenerator, logger) {

    companion object {
        private val AUTO_CONVERT_QUALIFIED_NAME = AutoConvert::class.qualifiedName!!
        private val CONVERT_LIFECYCLE_QUALIFIED_NAME = AutoConvertLifecycle::class.qualifiedName!!
    }

    private val targets = mutableListOf<ConvertTarget>()
    private val propertyMatcher = ConvertPropertyMatcher(logger)
    private val codeBuilder = ConvertCodeBuilder()

    override fun collect(resolver: Resolver): List<KSAnnotated> {
        val unprocessed = mutableListOf<KSAnnotated>()

        resolver.getSymbolsWithAnnotation(AUTO_CONVERT_QUALIFIED_NAME).forEach { symbol ->
            if (!symbol.validate()) {
                unprocessed += symbol
                return@forEach
            }
            if (symbol is KSClassDeclaration) {
                val target = parseConvertTarget(symbol)
                if (target != null) {
                    targets.add(target)
                } else {
                    logger.error(
                        "@AutoConvert class must implement AutoConvertLifecycle<Source, Target>",
                        symbol
                    )
                }
            }
        }

        return unprocessed
    }

    override fun hasDataToGenerate(): Boolean = targets.isNotEmpty()

    override fun generate() {
        for (target in targets) {
            val matchResult = propertyMatcher.match(
                sourceDecl = target.sourceDecl,
                targetDecl = target.targetDecl,
                ignoreTargets = target.ignoreTargets
            )
            val fileSpec = codeBuilder.buildFileSpec(target, matchResult)
            writeKotlinFile(
                fileSpec = fileSpec,
                dependencies = buildDependencies(
                    aggregating = false,
                    listOf(target.converterDecl, target.sourceDecl, target.targetDecl)
                )
            )
        }
        generateReport()
    }

    private fun generateReport() {
        val sourceClasses = targets.map { it.sourceDecl.qualifiedName?.asString() }.distinct()
        val targetClasses = targets.map { it.targetDecl.qualifiedName?.asString() }.distinct()
        val lines = mutableListOf<String>()
        lines += "Converters      : ${targets.size} annotated"
        lines += "Source classes   : ${sourceClasses.size} distinct"
        lines += "Target classes   : ${targetClasses.size} distinct"
        val moduleName = targets.firstOrNull()?.converterDecl?.let { extractModuleName(it) } ?: "Unknown"
        emitReport("AutoConvert", moduleName, lines, "Total: ${targets.size} convert functions generated")
    }

    /**
     * 从类声明中解析 ConvertTarget。
     * 通过 ConvertLifecycle<S, T> 的泛型参数推断源类和目标类。
     */
    private fun parseConvertTarget(classDecl: KSClassDeclaration): ConvertTarget? {
        // 查找 ConvertLifecycle 超类型并提取泛型参数
        val (sourceType, targetType) = resolveLifecycleTypeArgs(classDecl) ?: return null

        val sourceDecl = sourceType.declaration as? KSClassDeclaration ?: return null
        val targetDecl = targetType.declaration as? KSClassDeclaration ?: return null

        // 读取注解参数
        val anno = classDecl.annotations.first {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == AUTO_CONVERT_QUALIFIED_NAME
        }

        val functionName = anno.arg<String>(AutoConvert::functionName.name).orEmpty()

        @Suppress("UNCHECKED_CAST")
        val ignoreTargetsArray = anno.arg<List<String>>(AutoConvert::ignoreTargets.name) ?: emptyList()
        val ignoreTargets = ignoreTargetsArray.toSet()

        val packageName = classDecl.packageName.asString()

        return ConvertTarget(
            converterDecl = classDecl,
            sourceDecl = sourceDecl,
            targetDecl = targetDecl,
            sourceType = sourceType,
            targetType = targetType,
            functionName = functionName,
            ignoreTargets = ignoreTargets,
            packageName = packageName
        )
    }

    /**
     * 从类的超类型中查找 ConvertLifecycle<S, T>，返回 (sourceType, targetType)。
     */
    private fun resolveLifecycleTypeArgs(classDecl: KSClassDeclaration): Pair<KSType, KSType>? {
        for (superType in classDecl.superTypes) {
            val resolved = superType.resolve()
            val declaration = resolved.declaration
            val qualifiedName = declaration.qualifiedName?.asString() ?: continue

            if (qualifiedName == CONVERT_LIFECYCLE_QUALIFIED_NAME) {
                val typeArgs = resolved.arguments
                if (typeArgs.size != 2) continue

                val sourceType = typeArgs[0].type?.resolve() ?: continue
                val targetType = typeArgs[1].type?.resolve() ?: continue
                return sourceType to targetType
            }
        }
        return null
    }
}

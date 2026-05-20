package launcher.wb.codegeneration

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.FileSpec

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2025/5/15
 *
 * 代码生成器基类。
 * 每个功能模块（Function、MarketViewRoute、TradeInterface 等）实现此接口，
 * 由 WbKspProcessor 统一调度。
 *
 * 生命周期：
 * 1. [collect] — 从 Resolver 中收集本功能关注的注解符号
 * 2. [generate] — 根据收集结果生成代码文件
 */
abstract class BaseGeneration(
    protected val codeGenerator: CodeGenerator,
    protected val logger: KSPLogger
) {

    companion object {
        /** KSP arg key：模块名，各 Generation 共用 */
        const val OPTION_MODULE_NAME = "module_name"
    }

    /**
     * 收集本功能关注的注解符号。
     * @return 未能处理的符号（需要延迟到下一轮），由 Processor 汇总返回给 KSP
     */
    abstract fun collect(resolver: Resolver): List<KSAnnotated>

    /**
     * 根据收集到的数据生成代码。
     * 仅在 [collect] 收集到有效数据时调用。
     */
    abstract fun generate()

    /**
     * 是否有数据需要生成。
     * Processor 根据此标志决定是否调用 [generate]。
     */
    abstract fun hasDataToGenerate(): Boolean

    /**
     * 工具方法：将 FileSpec 写入 KSP CodeGenerator。
     */
    protected fun writeKotlinFile(
        fileSpec: FileSpec,
        dependencies: Dependencies
    ) {
        val file = codeGenerator.createNewFile(
            dependencies = dependencies,
            packageName = fileSpec.packageName,
            fileName = fileSpec.name,
            extensionName = "kt"
        )
        file.writer().use { writer ->
            fileSpec.writeTo(writer)
        }
    }

    /**
     * 工具方法：从类声明列表中构建 Dependencies，自动过滤 null 并检查空数组。
     */
    protected fun buildDependencies(
        aggregating: Boolean,
        classes: Collection<KSClassDeclaration>
    ): Dependencies {
        val sourceFiles = classes.mapNotNull { it.containingFile }
        if (sourceFiles.isEmpty()) {
            logger.warn("No source files found for dependencies, incremental compilation may not work correctly.")
        }
        return Dependencies(aggregating, *sourceFiles.toTypedArray())
    }

    /**
     * 从 KSClassDeclaration 的源文件路径中提取模块名，并转为 PascalCase。
     * 路径格式约定：.../模块名/src/main/...
     * 例如：trade-order → TradeOrder, MarketModule → MarketModule
     */
    protected fun extractModuleName(classDecl: KSClassDeclaration): String? {
        val filePath = classDecl.containingFile?.filePath ?: return null
        val srcIndex = filePath.indexOf("/src/")
        if (srcIndex > 0) {
            val beforeSrc = filePath.substring(0, srcIndex)
            val rawName = beforeSrc.substringAfterLast("/")
            return rawName.toPascalCase()
        }
        return null
    }

    /** 将模块名转为 PascalCase：trade-order → TradeOrder */
    protected fun String.toPascalCase(): String {
        return split("-", "_")
            .joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }
    }

    /**
     * 通用报告输出：warn 日志（编译控制台可见，CI 可从日志收集）。
     *
     * @param tag 处理器标识，如 "FunctionFactory"
     * @param moduleName 模块名
     * @param lines 每行统计内容
     * @param summary 汇总行
     */
    protected fun emitReport(
        tag: String,
        moduleName: String,
        lines: List<String>,
        summary: String
    ) {
        val report = buildString {
            appendLine("[ksp] [$tag] ═══ Module: $moduleName ═══")
            lines.forEach { appendLine("  $it") }
            appendLine("  ${"─".repeat(50)}")
            appendLine("  $summary")
        }
        logger.info(report)
    }
}

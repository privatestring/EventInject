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
     * 如果 dependencies 的 sources 为空，记录 warning（增量编译可能失效）。
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
}

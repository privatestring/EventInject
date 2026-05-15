package launcher.wb

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import launcher.wb.codegeneration.BaseGeneration
import launcher.wb.codegeneration.FunctionGeneration
import launcher.wb.codegeneration.MarketViewRouteGeneration

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2025/5/15
 *
 * launcher-wb-compiler-ksp 的 KSP 处理器调度中心。
 *
 * 职责：
 * 1. 管理所有功能模块的 Generation 实例
 * 2. 统一调度 collect → generate 生命周期
 * 3. 汇总各模块未处理的符号返回给 KSP 框架
 *
 * 已实现功能：
 * - 功能三：Function 功能地图（FunctionGeneration）
 *
 * 待实现功能（后续逐步添加）：
 * - 功能四：MarketViewRoute 行情视图路由
 * - 功能五：TradeInterface 交易服务工厂
 * - 功能六：TradeServiceMaker 聚合接口
 * - 功能七：Mapper 对象映射
 */
class WbKspProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    /** 防止多轮处理时重复生成 */
    private var processed = false

    /**
     * 所有功能模块的生成器列表。
     * 新增功能时只需在此处添加对应的 Generation 实例。
     */
    private val generations: List<BaseGeneration> by lazy {
        listOf(
            FunctionGeneration(codeGenerator, logger),
            MarketViewRouteGeneration(codeGenerator, logger),
            // TODO: TradeInterfaceGeneration(codeGenerator, logger),
            // TODO: TradeServiceMakerGeneration(codeGenerator, logger),
            // TODO: MapperGeneration(codeGenerator, logger),
        )
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (processed) return emptyList()

        val allUnprocessed = mutableListOf<KSAnnotated>()

        // 阶段一：各模块收集注解符号
        for (generation in generations) {
            val unprocessed = generation.collect(resolver)
            allUnprocessed.addAll(unprocessed)
        }

        // 阶段二：各模块生成代码
        var generated = false
        for (generation in generations) {
            if (generation.hasDataToGenerate()) {
                generation.generate()
                generated = true
            }
        }

        if (generated) processed = true

        return allUnprocessed
    }
}

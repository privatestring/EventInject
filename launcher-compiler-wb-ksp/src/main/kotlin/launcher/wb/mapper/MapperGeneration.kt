package launcher.wb.mapper

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import launcher.wb.codegeneration.BaseGeneration

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2026/5/15
 *
 * 功能七：Mapper 对象映射 代码生成器入口。
 *
 * 收集所有 @Mapper 注解的接口/抽象类，为每个 Mapper 生成对应的
 * Java 静态方法实现类（XxxImpl.java），与原 KAPT 版本完全兼容。
 *
 * 生成结构：
 * ```java
 * public final class OrderMapperImpl {
 *     public static OrderEntity toEntity(OrderDto dto) { ... }
 *     public static void updateEntity(OrderDto dto, OrderEntity entity) { ... }
 *     public static List<OrderEntity> toEntityList(List<OrderDto> dtos) { ... }
 * }
 * ```
 */
class MapperGeneration(
    codeGenerator: CodeGenerator,
    logger: KSPLogger
) : BaseGeneration(codeGenerator, logger) {

    /** 收集到的 Mapper 接口/抽象类 */
    private val collectedMappers = mutableListOf<KSClassDeclaration>()

    /** 属性解析器 */
    private val propertyResolver = PropertyResolver(logger)

    override fun collect(resolver: Resolver): List<KSAnnotated> {
        val unprocessed = mutableListOf<KSAnnotated>()

        resolver.getSymbolsWithAnnotation("mapper.Mapper").forEach { symbol ->
            if (!symbol.validate()) {
                unprocessed += symbol
                return@forEach
            }
            if (symbol is KSClassDeclaration) {
                collectedMappers += symbol
            }
        }

        return unprocessed
    }

    override fun hasDataToGenerate(): Boolean = collectedMappers.isNotEmpty()

    override fun generate() {
        collectedMappers.forEach { mapperElement ->
            try {
                processMapper(mapperElement)
            } catch (e: Exception) {
                logger.error(
                    "Error processing @Mapper: ${e.message ?: "Unknown error"}",
                    mapperElement
                )
            }
        }
    }

    /**
     * 处理单个 Mapper 接口
     */
    private fun processMapper(mapperElement: KSClassDeclaration) {
        // 构建描述符
        val descriptor = MapperUtils.handleMapper(mapperElement, propertyResolver, logger) ?: return

        // 生成 Java 代码
        val javaFile = MapperCodeGeneration(descriptor, propertyResolver, logger).brewJava()

        // 写入文件
        val dependencies = buildDependencies(aggregating = false, listOf(mapperElement))

        val file = codeGenerator.createNewFile(
            dependencies = dependencies,
            packageName = descriptor.packageName,
            fileName = descriptor.implementationName,
            extensionName = "java"
        )
        file.writer().use { writer ->
            javaFile.writeTo(writer)
        }
    }
}

package launcher.wb.codegeneration

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MUTABLE_MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.buildCodeBlock
import launcher.MarketViewRoute

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2026/5/15
 *
 * 功能四：MarketViewRoute 行情视图路由 代码生成器（KotlinPoet 版本）。
 *
 * 收集所有 @MarketViewRoute 注解的 View 类，生成 MarketViewRouteFactory object。
 * 运行时通过 key 字符串动态创建对应的 View 实例。
 *
 * 优化点（相比原 KAPT Java 版本）：
 * 1. 新增 viewCreatorMap：用 lambda 构造器替代 switch-case
 * 2. 新增 createViewById()：通过 map 查找创建，无需 when 分支
 * 3. 添加 @JvmStatic / @JvmField 注解确保 Java 互操作兼容
 * 4. 保留 createView() 方法（when 分支版本）兼容原有调用方
 *
 * 生成结构：
 * ```kotlin
 * package com.webull.market.common.base
 *
 * object MarketViewRouteFactory {
 *     const val VIEW_XXX: String = "xxx"
 *
 *     @JvmField val viewCreatorMap: MutableMap<String, (Context) -> View> = mutableMapOf()
 *
 *     @JvmStatic fun initViewCreators() { ... }
 *     @JvmStatic fun createView(context: Context, key: String): View? { ... }
 *     @JvmStatic fun createViewById(context: Context, key: String): View? { ... }
 * }
 * ```
 */
class MarketViewRouteGeneration(
    codeGenerator: CodeGenerator,
    logger: KSPLogger
) : BaseGeneration(codeGenerator, logger) {

    /** 收集到的标注 @MarketViewRoute 的类 */
    private val viewClasses = mutableListOf<KSClassDeclaration>()

    override fun collect(resolver: Resolver): List<KSAnnotated> {
        val unprocessed = mutableListOf<KSAnnotated>()

        resolver.getSymbolsWithAnnotation(MarketViewRoute::class.qualifiedName!!).forEach { symbol ->
            if (!symbol.validate()) {
                unprocessed += symbol
                return@forEach
            }
            if (symbol is KSClassDeclaration) {
                viewClasses += symbol
            }
        }

        return unprocessed
    }

    override fun hasDataToGenerate(): Boolean = viewClasses.isNotEmpty()

    override fun generate() {
        // 重复 key 检测
        validateUniqueKeys()

        val fileSpec = brewKotlin()
        val sourceFiles = viewClasses.mapNotNull { it.containingFile }
        writeKotlinFile(
            fileSpec = fileSpec,
            dependencies = Dependencies(aggregating = true, *sourceFiles.toTypedArray())
        )
    }

    private fun brewKotlin(): FileSpec {
        return FileSpec.builder(PACKAGE_NAME, CLASS_NAME)
            .addFileComment("Generated code from market View! Do not modify.")
            .addType(createFactoryObject())
            .build()
    }

    private fun createFactoryObject(): TypeSpec {
        return TypeSpec.objectBuilder(CLASS_NAME)
            .addKdoc("市场 View 映射\n")
            // ===== 1. Key 常量 =====
            .addViewKeyConstants()
            // ===== 2. lambda 构造器 map =====
            .addCreatorMapProperty()
            .addFunction(buildInitViewCreatorsMethod())
            .addFunction(buildCreateViewByIdMethod())
            // ===== 3. 兼容旧 API（委托给 createViewById） =====
            .addFunction(buildCreateViewMethod())
            .build()
    }

    // ======================== 重复 key 检测 ========================

    private fun validateUniqueKeys() {
        val seenKeys = mutableMapOf<String, KSClassDeclaration>()
        for (classDecl in viewClasses) {
            val key = getViewKey(classDecl)
            val existing = seenKeys[key]
            if (existing != null) {
                logger.error(
                    "MarketViewRoute key \"$key\" is duplicated! " +
                            "Already used by ${existing.qualifiedName?.asString()}",
                    classDecl
                )
            }
            seenKeys[key] = classDecl
        }
    }

    // ======================== 常量生成 ========================

    /**
     * 生成所有 View 的 VIEW_XXX 常量
     */
    private fun TypeSpec.Builder.addViewKeyConstants(): TypeSpec.Builder {
        viewClasses.forEach { classDecl ->
            val key = getViewKey(classDecl)
            val desc = getViewDesc(classDecl)
            val constName = "VIEW_${key.uppercase()}"

            val propBuilder = PropertySpec.builder(constName, STRING)
                .addModifiers(KModifier.CONST)
                .initializer("%S", key)

            // desc 非空时才生成 KDoc（对齐原始 KAPT 行为）
            if (desc.isNotBlank()) {
                propBuilder.addKdoc(desc)
            }

            addProperty(propBuilder.build())
        }
        return this
    }

    // ======================== 属性生成 ========================

    /**
     * 生成 viewCreatorMap 属性：MutableMap<String, (Context) -> View>
     */
    private fun TypeSpec.Builder.addCreatorMapProperty(): TypeSpec.Builder {
        val contextClass = ClassName("android.content", "Context")
        val viewClass = ClassName("android.view", "View")
        val creatorLambda = LambdaTypeName.get(
            parameters = arrayOf(contextClass),
            returnType = viewClass
        )
        val mapType = MUTABLE_MAP.parameterizedBy(STRING, creatorLambda)

        addProperty(
            PropertySpec.builder("viewCreatorMap", mapType)
                .addAnnotation(JVM_FIELD)
                .initializer("mutableMapOf()")
                .build()
        )
        return this
    }

    // ======================== 方法生成 ========================

    /**
     * 生成 initViewCreators() 方法
     * 填充 viewCreatorMap
     */
    private fun buildInitViewCreatorsMethod(): FunSpec {
        return FunSpec.builder("initViewCreators")
            .addAnnotation(JVM_STATIC)
            .addCode(buildCodeBlock {
                beginControlFlow("if (viewCreatorMap.isEmpty())")
                viewClasses.forEach { classDecl ->
                    val key = getViewKey(classDecl)
                    val qualifiedName = classDecl.qualifiedName?.asString() ?: return@forEach
                    val targetClass = ClassName.bestGuess(qualifiedName)
                    val constName = "VIEW_${key.uppercase()}"
                    addStatement("viewCreatorMap[$constName] = { ctx -> %T(ctx) }", targetClass)
                }
                endControlFlow()
            })
            .build()
    }

    /**
     * 生成 createView(context: Context, key: String): View? 方法
     * 委托给 createViewById()，保持 API 兼容，标记为 @Deprecated 引导迁移
     */
    private fun buildCreateViewMethod(): FunSpec {
        val contextClass = ClassName("android.content", "Context")
        val viewClass = ClassName("android.view", "View")

        return FunSpec.builder("createView")
            .addAnnotation(JVM_STATIC)
            .addAnnotation(
                AnnotationSpec.builder(Deprecated::class)
                    .addMember("message = %S", "Use createViewById instead")
                    .addMember("replaceWith = %T(%S)", ReplaceWith::class, "createViewById(context, key)")
                    .build()
            )
            .addParameter("context", contextClass)
            .addParameter("key", STRING)
            .returns(viewClass.copy(nullable = true))
            .addStatement("return createViewById(context, key)")
            .build()
    }

    /**
     * 生成 createViewById(context: Context, key: String): View? 方法
     * 通过 viewCreatorMap 查找创建，无需 when 分支（优化版本）
     */
    private fun buildCreateViewByIdMethod(): FunSpec {
        val contextClass = ClassName("android.content", "Context")
        val viewClass = ClassName("android.view", "View")

        return FunSpec.builder("createViewById")
            .addAnnotation(JVM_STATIC)
            .addParameter("context", contextClass)
            .addParameter("key", STRING)
            .returns(viewClass.copy(nullable = true))
            .addCode(buildCodeBlock {
                addStatement("if (viewCreatorMap.isEmpty()) initViewCreators()")
                addStatement("return viewCreatorMap[key]?.invoke(context)")
            })
            .build()
    }

    // ======================== 工具方法 ========================

    /**
     * 获取 @MarketViewRoute 的 key 值。
     * 如果 key 为空，使用类的全限定名。
     */
    private fun getViewKey(classDecl: KSClassDeclaration): String {
        val anno = classDecl.annotations.first {
            it.shortName.asString() == "MarketViewRoute"
        }
        val keyArg = anno.arguments.firstOrNull { it.name?.asString() == "key" }
        val key = keyArg?.value as? String ?: ""
        return key.ifEmpty { classDecl.qualifiedName?.asString() ?: classDecl.simpleName.asString() }
    }

    /**
     * 获取 @MarketViewRoute 的 desc 值
     */
    private fun getViewDesc(classDecl: KSClassDeclaration): String {
        val anno = classDecl.annotations.first {
            it.shortName.asString() == "MarketViewRoute"
        }
        val descArg = anno.arguments.firstOrNull { it.name?.asString() == "desc" }
        return descArg?.value as? String ?: ""
    }

    companion object {
        private const val PACKAGE_NAME = "com.webull.market.common.base"
        private const val CLASS_NAME = "MarketViewRouteFactory"
        private val JVM_STATIC = AnnotationSpec.builder(JvmStatic::class).build()
        private val JVM_FIELD = AnnotationSpec.builder(JvmField::class).build()
    }
}

package launcher.wb.codegeneration

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MUTABLE_MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.buildCodeBlock
import launcher.Function

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2025/5/15
 *
 * 功能三：Function 功能地图 代码生成器（KotlinPoet 版本）。
 *
 * 收集所有 @Function 注解的类，生成 FunctionFactory object。
 * 注意：@MarketViewRoute 标注的类也会参与收集（与原 KAPT 版本行为一致），
 * 但只有同时标注了 @Function 的类才会进入 FunctionFactory。
 *
 * 优化点（相比原 KAPT Java 版本）：
 * 1. 新增 functionCreatorMap：用 lambda 构造器替代反射 newInstance()
 * 2. 新增 createById()：无反射直接创建实例
 * 3. 添加 @JvmStatic / @JvmField 注解确保 Java 互操作兼容
 *
 * 生成结构：
 * ```kotlin
 * package com.webull.functionmap
 *
 * object FunctionFactory {
 *     const val FUNCTION_XXX_ID: String = "xxx"
 *
 *     @JvmField val functionCacheMap: MutableMap<String, Class<*>> = mutableMapOf()
 *     @JvmField val functionCreatorMap: MutableMap<String, () -> Any> = mutableMapOf()
 *
 *     @JvmStatic fun initFunction() { ... }
 *     @JvmStatic fun getFunctionId(clsName: String): String { ... }
 *     @JvmStatic fun createById(id: String): Any? { ... }
 * }
 * ```
 */
class FunctionGeneration(
    codeGenerator: CodeGenerator,
    logger: KSPLogger
) : BaseGeneration(codeGenerator, logger) {

    private val classType = ClassName("java.lang", "Class")
        .parameterizedBy(STAR)

    /** 收集到的标注 @Function 的类 */
    private val functionClasses = mutableListOf<KSClassDeclaration>()

    /** 收集到的所有分组 */
    private val allGroups = mutableListOf<String>()

    override fun collect(resolver: Resolver): List<KSAnnotated> {
        val unprocessed = mutableListOf<KSAnnotated>()
        val candidates = mutableSetOf<KSClassDeclaration>()

        // 扫描 @Function 类
        resolver.getSymbolsWithAnnotation(Function::class.qualifiedName!!).forEach { symbol ->
            if (!symbol.validate()) {
                unprocessed += symbol
                return@forEach
            }
            if (symbol is KSClassDeclaration) candidates += symbol
        }

        // 扫描 @MarketViewRoute 类（也参与收集，与原 KAPT 行为一致）
        resolver.getSymbolsWithAnnotation("launcher.MarketViewRoute").forEach { symbol ->
            if (!symbol.validate()) {
                unprocessed += symbol
                return@forEach
            }
            if (symbol is KSClassDeclaration) candidates += symbol
        }

        // 筛选出真正标注了 @Function 的类（使用全限定名匹配，避免同名注解误匹配）
        for (classDecl in candidates) {
            val functionAnno = classDecl.annotations.firstOrNull { anno ->
                anno.annotationType.resolve().declaration.qualifiedName?.asString() == Function::class.qualifiedName
            } ?: continue

            functionClasses.add(classDecl)

            // 收集 group 参数
            val groupArg = functionAnno.arguments.firstOrNull { it.name?.asString() == "group" }
            @Suppress("UNCHECKED_CAST")
            val groups = groupArg?.value as? List<String> ?: emptyList()
            allGroups.addAll(groups)
        }

        return unprocessed
    }

    override fun hasDataToGenerate(): Boolean = functionClasses.isNotEmpty()

    override fun generate() {
        val distinctGroups = allGroups.distinct().filter { it.isNotEmpty() }
        val fileSpec = brewKotlin(distinctGroups)
        if (fileSpec == null) {
            logger.error("FunctionGeneration: Duplicate functionId detected, skipping code generation.")
            return
        }

        writeKotlinFile(
            fileSpec = fileSpec,
            dependencies = buildDependencies(aggregating = true, functionClasses)
        )
        generateReport(distinctGroups)
    }

    private fun generateReport(distinctGroups: List<String>) {
        val lines = mutableListOf<String>()
        lines += "Functions       : ${functionClasses.size} registrations"
        if (distinctGroups.isNotEmpty()) {
            lines += "Groups          : ${distinctGroups.size} (${distinctGroups.joinToString(", ")})"
            distinctGroups.forEach { group ->
                val count = functionClasses.count { classDecl ->
                    val anno = classDecl.annotations.firstOrNull { it.shortName.asString() == "Function" }
                    @Suppress("UNCHECKED_CAST")
                    val groups = anno?.arguments?.firstOrNull { it.name?.asString() == "group" }?.value as? List<String> ?: emptyList()
                    groups.contains(group)
                }
                lines += "  $group: $count functions"
            }
        }
        emitReport("FunctionFactory", "Global", lines, "Total: ${functionClasses.size} functions, ${distinctGroups.size} groups")
    }

    private fun brewKotlin(distinctGroups: List<String>): FileSpec? {
        val factoryObject = createFactoryObject(distinctGroups) ?: return null
        return FileSpec.builder("com.webull.functionmap", "FunctionFactory")
            .addFileComment("Generated code from Function! Do not modify.")
            .addType(factoryObject)
            .build()
    }

    private fun createFactoryObject(distinctGroups: List<String>): TypeSpec? {
        val builder = TypeSpec.objectBuilder("FunctionFactory")
            .addKdoc("功能地图 映射\n")

        val (builderWithConstants, success) = builder.addFunctionIdConstants()
        if (!success) return null

        return builderWithConstants
            .addCacheMapProperty()
            .addCreatorMapProperty()
            .addFunction(buildInitFunctionMethod())
            .addFunction(buildGetFunctionIdMethod())
            .addFunction(buildCreateByIdMethod())
            .apply {
                distinctGroups.forEach { group ->
                    addFunction(buildGroupFunctionMethod(group))
                }
            }
            .build()
    }

    // ======================== 常量生成 ========================

    /**
     * 生成所有功能的 FUNCTION_XXX_ID 常量
     * @return false 表示检测到重复 ID，应中止生成
     */
    private fun TypeSpec.Builder.addFunctionIdConstants(): Pair<TypeSpec.Builder, Boolean> {
        val usedIds = mutableMapOf<String, KSClassDeclaration>()
        var hasDuplicate = false

        functionClasses.forEach { classDecl ->
            val simpleName = classDecl.simpleName.asString()
            val functionAnno = classDecl.annotations.first {
                it.shortName.asString() == "Function"
            }

            val functionIdArg = functionAnno.arguments.firstOrNull { it.name?.asString() == "functionId" }
            val functionId = (functionIdArg?.value as? String)?.ifEmpty { null }
                ?: "${simpleName}_function"

            val descArg = functionAnno.arguments.firstOrNull { it.name?.asString() == "desc" }
            val desc = descArg?.value as? String ?: ""

            // 重复 ID 检测
            val existing = usedIds[functionId]
            if (existing != null) {
                logger.error(
                    "Found that the same FunctionId $functionId corresponds to multiple different implementation classes: " +
                        "${existing.qualifiedName?.asString()} and ${classDecl.qualifiedName?.asString()}",
                    classDecl
                )
                hasDuplicate = true
                return@forEach
            }
            usedIds[functionId] = classDecl

            val constName = "FUNCTION_${simpleName.uppercase()}_ID"
            addProperty(
                PropertySpec.builder(constName, STRING)
                    .addKdoc(desc)
                    .addModifiers(KModifier.CONST)
                    .initializer("%S", functionId)
                    .build()
            )
        }
        return this to !hasDuplicate
    }

    // ======================== 属性生成 ========================

    /**
     * 生成 functionCacheMap 属性（保留，用于 getFunctionId 反查）
     */
    private fun TypeSpec.Builder.addCacheMapProperty(): TypeSpec.Builder {
        val mapType = MUTABLE_MAP.parameterizedBy(STRING, classType)
        addProperty(
            PropertySpec.builder("functionCacheMap", mapType)
                .addAnnotation(JVM_FIELD)
                .initializer("mutableMapOf()")
                .build()
        )
        return this
    }

    /**
     * 生成 functionCreatorMap 属性（新增，用 lambda 替代反射创建实例）
     */
    private fun TypeSpec.Builder.addCreatorMapProperty(): TypeSpec.Builder {
        val creatorLambda = LambdaTypeName.get(returnType = ANY)
        val mapType = MUTABLE_MAP.parameterizedBy(STRING, creatorLambda)
        addProperty(
            PropertySpec.builder("functionCreatorMap", mapType)
                .addAnnotation(JVM_FIELD)
                .initializer("mutableMapOf()")
                .build()
        )
        return this
    }

    // ======================== 方法生成 ========================

    /**
     * 生成 initFunction() 方法
     * 同时填充 functionCacheMap 和 functionCreatorMap
     */
    private fun buildInitFunctionMethod(): FunSpec {
        return FunSpec.builder("initFunction")
            .addAnnotation(JVM_STATIC)
            .addCode(buildCodeBlock {
                beginControlFlow("if (functionCacheMap.isEmpty())")
                functionClasses.forEach { classDecl ->
                    val simpleName = classDecl.simpleName.asString()
                    val qualifiedName = classDecl.qualifiedName?.asString() ?: return@forEach
                    val constName = "FUNCTION_${simpleName.uppercase()}_ID"
                    val targetClass = ClassName.bestGuess(qualifiedName)
                    addStatement("functionCacheMap[$constName] = %T::class.java", targetClass)
                    addStatement("functionCreatorMap[$constName] = { %T() }", targetClass)
                }
                endControlFlow()
            })
            .build()
    }

    /**
     * 生成 getFunctionId(clsName: String): String 方法
     * 通过类名反查功能 ID（保留原有逻辑）
     */
    private fun buildGetFunctionIdMethod(): FunSpec {
        return FunSpec.builder("getFunctionId")
            .addAnnotation(JVM_STATIC)
            .addParameter("clsName", STRING)
            .returns(STRING)
            .addCode(buildCodeBlock {
                addStatement("if (functionCacheMap.isEmpty()) initFunction()")
                beginControlFlow("for ((key, clazz) in functionCacheMap)")
                beginControlFlow("if (clazz.canonicalName?.contains(clsName) == true)")
                addStatement("return key")
                endControlFlow()
                endControlFlow()
                addStatement("return %S", "")
            })
            .build()
    }

    /**
     * 生成 createById(id: String): Any? 方法
     * 通过 ID 直接创建实例，无反射
     */
    private fun buildCreateByIdMethod(): FunSpec {
        return FunSpec.builder("createById")
            .addAnnotation(JVM_STATIC)
            .addParameter("id", STRING)
            .returns(ANY.copy(nullable = true))
            .addCode(buildCodeBlock {
                addStatement("if (functionCreatorMap.isEmpty()) initFunction()")
                addStatement("return functionCreatorMap[id]?.invoke()")
            })
            .build()
    }

    /**
     * 生成按分组获取功能列表的方法：initXxxFunction(): List<Class<*>>
     */
    private fun buildGroupFunctionMethod(group: String): FunSpec {
        val listType = ClassName("kotlin.collections", "List")
            .parameterizedBy(classType)

        return FunSpec.builder("init${group}Function")
            .addAnnotation(JVM_STATIC)
            .returns(listType)
            .addCode(buildCodeBlock {
                addStatement("val result = mutableListOf<%T>()", classType)
                functionClasses.forEach { classDecl ->
                    val functionAnno = classDecl.annotations.firstOrNull {
                        it.shortName.asString() == "Function"
                    } ?: return@forEach

                    val groupArg = functionAnno.arguments.firstOrNull { it.name?.asString() == "group" }
                    @Suppress("UNCHECKED_CAST")
                    val groups = groupArg?.value as? List<String> ?: emptyList()

                    if (groups.contains(group)) {
                        val qualifiedName = classDecl.qualifiedName?.asString() ?: return@forEach
                        val targetClass = ClassName.bestGuess(qualifiedName)
                        addStatement("result.add(%T::class.java)", targetClass)
                    }
                }
                addStatement("return result")
            })
            .build()
    }

    companion object {
        private val JVM_STATIC = AnnotationSpec.builder(JvmStatic::class).build()
        private val JVM_FIELD = AnnotationSpec.builder(JvmField::class).build()
    }
}

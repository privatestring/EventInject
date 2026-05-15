package launcher.wb.mapper

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeSpec
import launcher.wb.mapper.codegen.AssignmentCollector
import launcher.wb.mapper.codegen.AssignmentGenerator
import launcher.wb.mapper.codegen.TargetContext
import launcher.wb.mapper.codegen.TypeResolver
import javax.lang.model.element.Modifier

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2026/5/15
 *
 * Mapper 的 JavaPoet 代码生成器（KSP 版本）。
 * 生成与原 KAPT 版本完全兼容的 Java 静态方法实现类。
 */
class MapperCodeGeneration(
    private val descriptor: MapperDescriptor,
    private val propertyResolver: PropertyResolver,
    private val logger: KSPLogger
) {

    private val assignmentCollector = AssignmentCollector(descriptor, propertyResolver, logger)
    private val assignmentGenerator = AssignmentGenerator(descriptor, propertyResolver, logger)

    fun brewJava(): JavaFile {
        return JavaFile.builder(descriptor.packageName, createTypeSpec())
            .addFileComment("Auto Generated code from Mapper. Do not modify!!!!!!!")
            .build()
    }

    private fun createTypeSpec(): TypeSpec {
        val builder = TypeSpec.classBuilder(descriptor.implementationName)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)

        descriptor.methods.forEach { builder.addMethod(createStaticMethod(it)) }
        descriptor.beforeMappingMethods.forEach { builder.addMethod(createStaticDefaultMethod(it, hasReturn = false)) }
        descriptor.afterMappingMethods.forEach { builder.addMethod(createStaticDefaultMethod(it, hasReturn = false)) }
        descriptor.ignoredMethods.forEach { builder.addMethod(createStaticDefaultMethod(it, hasReturn = true)) }

        return builder.build()
    }

    // ======================== 映射方法生成 ========================

    private fun createStaticMethod(method: MapperMethodDescriptor): MethodSpec {
        val methodBuilder = MethodSpec.methodBuilder(method.name)
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(TypeResolver.resolveTypeName(method.returnType))

        val mappingComment = generateMappingComment(method)
        if (mappingComment.isNotEmpty()) methodBuilder.addJavadoc(mappingComment)

        val usedNames = mutableSetOf<String>()
        method.parameters.forEach { param ->
            methodBuilder.addParameter(ParameterSpec.builder(TypeResolver.resolveTypeName(param.type), param.name).build())
            usedNames += param.name
        }

        // 集合互转方法
        if (detectCollectionToCollectionMapping(method)) {
            return generateCollectionToCollectionMethod(method, methodBuilder, usedNames)
        }

        val targetContext = resolveTargetContext(method, usedNames) ?: run {
            methodBuilder.addStatement("throw new IllegalStateException(\$S)", "Unable to resolve target type")
            return methodBuilder.build()
        }

        // Source 空值检查
        val isVoidReturn = method.returnType.declaration.qualifiedName?.asString() == "kotlin.Unit"
        if (!isVoidReturn) {
            val primarySource = method.primarySource
            if (primarySource != null && !TypeResolver.isPrimitiveType(primarySource.type)) {
                methodBuilder.beginControlFlow("if (\$L == null)", primarySource.name)
                methodBuilder.addStatement("return null")
                methodBuilder.endControlFlow()
            }
        }

        // 创建目标对象或验证 @MappingTarget 非空
        if (!targetContext.isUpdate) {
            val typeName = TypeResolver.resolveTypeName(targetContext.type)
            methodBuilder.addStatement("\$T \$L = new \$T()", typeName, targetContext.varName, typeName)
            usedNames += targetContext.varName
        } else {
            methodBuilder.beginControlFlow("if (\$L == null)", targetContext.varName)
                .addStatement("throw new IllegalArgumentException(\$S)", "@MappingTarget parameter ${targetContext.varName} must not be null")
                .endControlFlow()
        }

        // @BeforeMapping → 字段映射 → @AfterMapping
        descriptor.beforeMappingMethods.forEach { callLifecycleMethod(methodBuilder, it, method, targetContext) }
        assignmentCollector.collectAssignments(method, targetContext).forEach { assignment ->
            assignmentGenerator.generateAssignment(methodBuilder, assignment, targetContext, method, usedNames)
        }
        descriptor.afterMappingMethods.forEach { callLifecycleMethod(methodBuilder, it, method, targetContext) }

        if (!isVoidReturn) methodBuilder.addStatement("return \$L", targetContext.varName)
        return methodBuilder.build()
    }

    // ======================== 生命周期/辅助方法生成（统一） ========================

    /**
     * 为 @BeforeMapping / @AfterMapping / @MappingIgnore 方法生成静态实现
     * @param hasReturn true 时检查返回类型决定是否加 return
     */
    private fun createStaticDefaultMethod(method: KSFunctionDeclaration, hasReturn: Boolean): MethodSpec {
        val methodBuilder = MethodSpec.methodBuilder(method.simpleName.asString())
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)

        val returnType = method.returnType?.resolve()
        methodBuilder.returns(TypeResolver.resolveTypeName(returnType))

        val paramNames = mutableListOf<String>()
        method.parameters.forEach { param ->
            val paramName = param.name?.asString() ?: "param"
            methodBuilder.addParameter(TypeResolver.resolveTypeName(param.type.resolve()), paramName)
            paramNames.add(paramName)
        }

        if (descriptor.isKotlinSource) {
            val interfaceName = descriptor.mapperElement.simpleName.asString()
            val methodName = method.simpleName.asString()
            val paramList = paramNames.joinToString(", ")
            val args = if (paramList.isEmpty()) "" else ", $paramList"

            methodBuilder.addComment("调用接口的默认实现")

            val isVoid = returnType == null || returnType.declaration.qualifiedName?.asString() == "kotlin.Unit"
            if (!hasReturn || isVoid) {
                methodBuilder.addStatement("\$L.DefaultImpls.\$L(null\$L)", interfaceName, methodName, args)
            } else {
                methodBuilder.addStatement("return \$L.DefaultImpls.\$L(null\$L)", interfaceName, methodName, args)
            }
        } else {
            generateEmptyImpl(methodBuilder, returnType)
        }

        return methodBuilder.build()
    }

    private fun callLifecycleMethod(
        methodBuilder: MethodSpec.Builder,
        lifecycleMethod: KSFunctionDeclaration,
        mappingMethod: MapperMethodDescriptor,
        targetContext: TargetContext
    ) {
        val paramNames = mutableListOf<String>()
        var allParamsMatched = true

        lifecycleMethod.parameters.forEach { param ->
            val paramType = param.type.resolve()
            val isMappingTarget = param.annotations.any { it.shortName.asString() == "MappingTarget" }

            if (isMappingTarget) {
                paramNames.add(targetContext.varName)
            } else {
                val matchingParam = mappingMethod.parameters.firstOrNull { mp ->
                    !mp.isMappingTarget && TypeResolver.isTypeCompatible(mp.type, paramType)
                }
                if (matchingParam != null) {
                    paramNames.add(matchingParam.name)
                } else {
                    val primarySource = mappingMethod.primarySource
                    if (primarySource != null && TypeResolver.isTypeCompatible(primarySource.type, paramType)) {
                        paramNames.add(primarySource.name)
                    } else {
                        allParamsMatched = false
                        return@forEach
                    }
                }
            }
        }

        if (!allParamsMatched) return

        val className = ClassName.get(descriptor.packageName, descriptor.implementationName)
        methodBuilder.addStatement("\$T.\$L(\$L)", className, lifecycleMethod.simpleName.asString(), paramNames.joinToString(", "))
    }

    private fun generateEmptyImpl(methodBuilder: MethodSpec.Builder, returnType: KSType?) {
        val name = returnType?.declaration?.qualifiedName?.asString()
        when {
            name == "kotlin.Unit" || name == null -> { /* void */ }
            name == "kotlin.Boolean" || name == "java.lang.Boolean" -> methodBuilder.addStatement("return false")
            TypeResolver.isPrimitiveNumericType(name) -> methodBuilder.addStatement("return 0")
            else -> methodBuilder.addStatement("return null")
        }
    }

    // ======================== 集合互转方法 ========================

    private fun detectCollectionToCollectionMapping(method: MapperMethodDescriptor): Boolean {
        if (method.mappingTarget != null) return false
        val sourceParams = method.parameters.filter { !it.isMappingTarget }
        if (sourceParams.size != 1) return false
        return TypeResolver.isCollectionType(sourceParams.first().type) && TypeResolver.isCollectionType(method.returnType)
    }

    private fun generateCollectionToCollectionMethod(
        method: MapperMethodDescriptor,
        methodBuilder: MethodSpec.Builder,
        usedNames: MutableSet<String>
    ): MethodSpec {
        val primarySource = method.primarySource ?: run {
            methodBuilder.addStatement("return null")
            return methodBuilder.build()
        }

        val sourceElementType = TypeResolver.getCollectionElementType(primarySource.type)
        val targetElementType = TypeResolver.getCollectionElementType(method.returnType)

        if (sourceElementType == null || targetElementType == null) {
            methodBuilder.addStatement("return null")
            return methodBuilder.build()
        }

        methodBuilder.beginControlFlow("if (\$L == null)", primarySource.name)
        methodBuilder.addStatement("return null")
        methodBuilder.endControlFlow()

        val tempListVarName = TypeResolver.generateUniqueName("tempList", usedNames)
        usedNames += tempListVarName
        val concreteListType = ClassName.get("java.util", "ArrayList")
        val targetElementTypeName = TypeResolver.resolveTypeName(targetElementType)

        methodBuilder.addStatement("\$T<\$T> \$L = new \$T<>()", concreteListType, targetElementTypeName, tempListVarName, concreteListType)
        methodBuilder.beginControlFlow("for (int i = 0; i < \$L.size(); i++)", primarySource.name)

        if (TypeResolver.isSameType(sourceElementType, targetElementType)) {
            methodBuilder.addStatement("\$L.add(\$L.get(i))", tempListVarName, primarySource.name)
        } else {
            val elementMapperMethod = TypeResolver.findElementMapperMethod(sourceElementType, targetElementType, descriptor.methods)
            if (elementMapperMethod != null) {
                val className = ClassName.get(descriptor.packageName, descriptor.implementationName)
                methodBuilder.addStatement("\$L.add(\$T.\$L(\$L.get(i)))", tempListVarName, className, elementMapperMethod.name, primarySource.name)
            } else {
                logger.error("Cannot find mapping method for element types in method ${method.name}.", method.element)
                methodBuilder.addStatement("\$L.add(null)", tempListVarName)
            }
        }

        methodBuilder.endControlFlow()
        methodBuilder.addStatement("return \$L", tempListVarName)
        return methodBuilder.build()
    }

    // ======================== 辅助方法 ========================

    private fun resolveTargetContext(method: MapperMethodDescriptor, usedNames: MutableSet<String>): TargetContext? {
        val isUpdate = method.mappingTarget != null
        val targetType = if (isUpdate) method.mappingTarget!!.type else method.returnType
        val targetDeclaration = propertyResolver.asClassDeclaration(targetType)

        if (targetDeclaration == null) {
            logger.error("Cannot resolve target type for method ${method.name}", method.element)
            return null
        }

        val varName = if (isUpdate) method.mappingTarget!!.name else TypeResolver.generateUniqueName("target", usedNames)
        return TargetContext(targetType, targetDeclaration, varName, isUpdate)
    }

    // ======================== Javadoc 注释 ========================

    private fun generateMappingComment(method: MapperMethodDescriptor): String {
        val primarySource = method.primarySource ?: return ""
        val targetType = if (method.mappingTarget != null) method.mappingTarget.type else method.returnType
        val targetDeclaration = propertyResolver.asClassDeclaration(targetType) ?: return ""

        val sourceReadable = propertyResolver.readableProperties(primarySource.typeDeclaration)
        val targetWritable = propertyResolver.writeableProperties(targetDeclaration)
        val targetFields = propertyResolver.writableFields(targetDeclaration)

        val allSourceProperties = sourceReadable.keys.toMutableSet()
        val allTargetProperties = (targetWritable.keys + targetFields.keys).toSet()

        val mappedTargets = mutableSetOf<String>()
        val mappedSources = mutableSetOf<String>()
        val explicitMappings = mutableListOf<Pair<String, String>>()
        val autoMapped = mutableListOf<String>()

        method.resolvedMappings.forEach { spec ->
            if (!spec.ignore) {
                val targetName = spec.target.split('.').first()
                mappedTargets += targetName
                if (!spec.source.isNullOrBlank()) {
                    mappedSources += spec.source.split('.').first()
                    explicitMappings += spec.source.split('.').first() to targetName
                } else {
                    mappedSources += targetName
                    autoMapped += targetName
                }
            }
        }

        (allTargetProperties - mappedTargets).forEach { targetProp ->
            if (allSourceProperties.contains(targetProp)) {
                val sourceType = sourceReadable[targetProp]?.type
                val tType = targetWritable[targetProp]?.paramType ?: targetFields[targetProp]?.type
                if (sourceType != null && tType != null && TypeResolver.isAssignable(sourceType, tType)) {
                    autoMapped += targetProp; mappedTargets += targetProp; mappedSources += targetProp
                }
            }
        }

        return buildString {
            append("字段映射详情：\n")
            append("源对象：${primarySource.typeDeclaration?.simpleName?.asString() ?: "Unknown"}\n")
            append("目标对象：${targetDeclaration.simpleName.asString()}\n\n")
            if (explicitMappings.isNotEmpty()) {
                append("显式映射（不同名）：\n")
                explicitMappings.forEach { (s, t) -> append("  - $s -> $t\n") }
                append("\n")
            }
            if (autoMapped.isNotEmpty()) {
                append("自动映射（同名）：\n")
                autoMapped.forEach { append("  - $it\n") }
                append("\n")
            }
            val unmappedSources = allSourceProperties - mappedSources
            if (unmappedSources.isNotEmpty()) {
                append("未映射的源字段：\n")
                unmappedSources.sorted().forEach { append("  - $it\n") }
                append("\n")
            }
            val unmappedTargets = allTargetProperties - mappedTargets
            if (unmappedTargets.isNotEmpty()) {
                append("未映射的目标字段：\n")
                unmappedTargets.sorted().forEach { append("  - $it\n") }
                append("\n")
            }
        }
    }
}

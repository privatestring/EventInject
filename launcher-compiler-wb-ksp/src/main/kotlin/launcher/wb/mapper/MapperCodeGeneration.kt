package launcher.wb.mapper

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2026/5/15
 *
 * Mapper 的 JavaPoet 代码生成器（KSP 版本）。
 * 生成与原 KAPT 版本完全兼容的 Java 静态方法实现类。
 *
 * 生成结构：
 * ```java
 * public final class XxxMapperImpl {
 *     public static TargetType toEntity(SourceType source) { ... }
 *     public static void updateEntity(SourceType source, TargetType target) { ... }
 *     public static List<TargetType> toEntityList(List<SourceType> sources) { ... }
 * }
 * ```
 */
class MapperCodeGeneration(
    private val descriptor: MapperDescriptor,
    private val propertyResolver: PropertyResolver,
    private val logger: KSPLogger
) {

    fun brewJava(): JavaFile {
        return JavaFile.builder(descriptor.packageName, createTypeSpec())
            .addFileComment("Auto Generated code from Mapper. Do not modify!!!!!!!")
            .build()
    }

    private fun createTypeSpec(): TypeSpec {
        val builder = TypeSpec.classBuilder(descriptor.implementationName)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)

        // 生成静态映射方法
        descriptor.methods.forEach { method ->
            builder.addMethod(createStaticMethod(method))
        }

        // 生成静态版本的 @BeforeMapping 和 @AfterMapping 方法
        descriptor.beforeMappingMethods.forEach { lifecycleMethod ->
            builder.addMethod(createStaticLifecycleMethod(lifecycleMethod))
        }
        descriptor.afterMappingMethods.forEach { lifecycleMethod ->
            builder.addMethod(createStaticLifecycleMethod(lifecycleMethod))
        }

        // 生成被 expression 引用的 @MappingIgnore 方法的静态版本
        descriptor.ignoredMethods.forEach { ignoredMethod ->
            builder.addMethod(createStaticIgnoredMethod(ignoredMethod))
        }

        return builder.build()
    }

    // ======================== 生命周期方法生成 ========================

    /**
     * 为 @MappingIgnore 方法生成静态实现
     */
    private fun createStaticIgnoredMethod(ignoredMethod: KSFunctionDeclaration): MethodSpec {
        val methodBuilder = MethodSpec.methodBuilder(ignoredMethod.simpleName.asString())
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(resolveTypeName(ignoredMethod.returnType?.resolve()))

        val paramNames = mutableListOf<String>()
        ignoredMethod.parameters.forEach { param ->
            val paramType = resolveTypeName(param.type.resolve())
            val paramName = param.name?.asString() ?: "param"
            methodBuilder.addParameter(paramType, paramName)
            paramNames.add(paramName)
        }

        if (descriptor.isKotlinSource) {
            val interfaceName = descriptor.mapperElement.simpleName.asString()
            val methodName = ignoredMethod.simpleName.asString()
            val paramList = paramNames.joinToString(", ")

            methodBuilder.addComment("调用接口的默认实现")
            val returnType = ignoredMethod.returnType?.resolve()
            val isVoid = returnType == null ||
                    returnType.declaration.qualifiedName?.asString() == "kotlin.Unit"

            if (isVoid) {
                methodBuilder.addStatement(
                    "\$L.DefaultImpls.\$L(null\$L)",
                    interfaceName, methodName,
                    if (paramList.isEmpty()) "" else ", $paramList"
                )
            } else {
                methodBuilder.addStatement(
                    "return \$L.DefaultImpls.\$L(null\$L)",
                    interfaceName, methodName,
                    if (paramList.isEmpty()) "" else ", $paramList"
                )
            }
        } else {
            generateEmptyImplementation(methodBuilder, ignoredMethod)
        }

        return methodBuilder.build()
    }

    /**
     * 为生命周期方法生成静态实现
     */
    private fun createStaticLifecycleMethod(lifecycleMethod: KSFunctionDeclaration): MethodSpec {
        val methodBuilder = MethodSpec.methodBuilder(lifecycleMethod.simpleName.asString())
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(resolveTypeName(lifecycleMethod.returnType?.resolve()))

        val paramNames = mutableListOf<String>()
        lifecycleMethod.parameters.forEach { param ->
            val paramType = resolveTypeName(param.type.resolve())
            val paramName = param.name?.asString() ?: "param"
            methodBuilder.addParameter(paramType, paramName)
            paramNames.add(paramName)
        }

        if (descriptor.isKotlinSource) {
            val interfaceName = descriptor.mapperElement.simpleName.asString()
            val paramList = paramNames.joinToString(", ")

            methodBuilder.addComment("调用接口的默认实现")
            methodBuilder.addStatement(
                "\$L.DefaultImpls.\$L(null\$L)",
                interfaceName, lifecycleMethod.simpleName.asString(),
                if (paramList.isEmpty()) "" else ", $paramList"
            )
        } else {
            generateEmptyImplementation(methodBuilder, lifecycleMethod)
        }

        return methodBuilder.build()
    }

    private fun generateEmptyImplementation(methodBuilder: MethodSpec.Builder, method: KSFunctionDeclaration) {
        val returnType = method.returnType?.resolve()
        val returnTypeName = returnType?.declaration?.qualifiedName?.asString()
        when {
            returnTypeName == "kotlin.Unit" || returnTypeName == null -> { /* void, 空实现 */ }
            returnTypeName == "kotlin.Boolean" || returnTypeName == "java.lang.Boolean" ->
                methodBuilder.addStatement("return false")
            isPrimitiveNumericType(returnTypeName) ->
                methodBuilder.addStatement("return 0")
            else -> methodBuilder.addStatement("return null")
        }
    }

    // ======================== 静态映射方法生成 ========================

    /**
     * 创建静态映射方法
     */
    private fun createStaticMethod(method: MapperMethodDescriptor): MethodSpec {
        val methodBuilder = MethodSpec.methodBuilder(method.name)
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(resolveTypeName(method.returnType))

        // 生成 Javadoc 注释
        val mappingComment = generateMappingComment(method)
        if (mappingComment.isNotEmpty()) {
            methodBuilder.addJavadoc(mappingComment)
        }

        val usedNames = mutableSetOf<String>()
        method.parameters.forEach { param ->
            val parameterSpec = ParameterSpec.builder(resolveTypeName(param.type), param.name).build()
            methodBuilder.addParameter(parameterSpec)
            usedNames += param.name
        }

        // 检测是否是集合互转方法
        val isCollectionToCollection = detectCollectionToCollectionMapping(method)
        if (isCollectionToCollection) {
            return generateCollectionToCollectionMethod(method, methodBuilder, usedNames)
        }

        // 解析目标上下文
        val targetContext = resolveTargetContext(method, usedNames) ?: run {
            methodBuilder.addStatement("throw new IllegalStateException(\$S)", "Unable to resolve target type")
            return methodBuilder.build()
        }

        // Source 空值检查
        val returnTypeName = method.returnType.declaration.qualifiedName?.asString()
        val isVoidReturn = returnTypeName == "kotlin.Unit"
        if (!isVoidReturn) {
            val primarySource = method.primarySource
            if (primarySource != null && !isPrimitiveType(primarySource.type)) {
                methodBuilder.beginControlFlow("if (\$L == null)", primarySource.name)
                methodBuilder.addStatement("return null")
                methodBuilder.endControlFlow()
            }
        }

        // 创建目标对象或验证 @MappingTarget 非空
        if (!targetContext.isUpdate) {
            val typeName = resolveTypeName(targetContext.type)
            methodBuilder.addStatement("\$T \$L = new \$T()", typeName, targetContext.varName, typeName)
            usedNames += targetContext.varName
        } else {
            methodBuilder.beginControlFlow("if (\$L == null)", targetContext.varName)
                .addStatement(
                    "throw new IllegalArgumentException(\$S)",
                    "@MappingTarget parameter ${targetContext.varName} must not be null"
                )
                .endControlFlow()
        }

        // 调用 @BeforeMapping 方法
        descriptor.beforeMappingMethods.forEach { beforeMethod ->
            callLifecycleMethod(methodBuilder, beforeMethod, method, targetContext)
        }

        // 字段映射
        collectAssignments(method, targetContext).forEach { assignment ->
            generateAssignment(methodBuilder, assignment, targetContext, method, usedNames)
        }

        // 调用 @AfterMapping 方法
        descriptor.afterMappingMethods.forEach { afterMethod ->
            callLifecycleMethod(methodBuilder, afterMethod, method, targetContext)
        }

        // 返回
        if (!isVoidReturn) {
            methodBuilder.addStatement("return \$L", targetContext.varName)
        }

        return methodBuilder.build()
    }

    // ======================== 生命周期方法调用 ========================

    /**
     * 调用生命周期方法（@BeforeMapping 或 @AfterMapping）
     */
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
                // 源对象参数：按类型匹配
                val matchingParam = mappingMethod.parameters.firstOrNull { mappingParam ->
                    !mappingParam.isMappingTarget && isTypeCompatible(mappingParam.type, paramType)
                }
                if (matchingParam != null) {
                    paramNames.add(matchingParam.name)
                } else {
                    val primarySource = mappingMethod.primarySource
                    if (primarySource != null && isTypeCompatible(primarySource.type, paramType)) {
                        paramNames.add(primarySource.name)
                    } else {
                        allParamsMatched = false
                        return@forEach
                    }
                }
            }
        }

        if (!allParamsMatched) return

        val methodName = lifecycleMethod.simpleName.asString()
        val paramList = paramNames.joinToString(", ")
        val className = ClassName.get(descriptor.packageName, descriptor.implementationName)
        methodBuilder.addStatement("\$T.\$L(\$L)", className, methodName, paramList)
    }

    // ======================== 赋值收集与生成 ========================

    /**
     * 收集所有字段赋值
     */
    private fun collectAssignments(
        method: MapperMethodDescriptor,
        targetContext: TargetContext
    ): List<Assignment> {
        val assignments = mutableListOf<Assignment>()
        val setterMap = propertyResolver.writeableProperties(targetContext.typeDeclaration)
        val fieldMap = propertyResolver.writableFields(targetContext.typeDeclaration)
        val handledTargets = mutableSetOf<String>()

        // 处理显式映射
        method.resolvedMappings.forEach { spec ->
            handledTargets += spec.target.split('.').first()
            if (spec.ignore) return@forEach

            val targetPath = spec.target.split('.').filter { it.isNotBlank() }
            if (targetPath.size > 1) {
                // 嵌套对象映射
                createNestedAssignment(method, targetContext, spec, targetPath)?.let { assignments += it }
            } else {
                val setter = setterMap[spec.target]
                val field = fieldMap[spec.target]
                when {
                    setter != null -> createAssignmentFromSpec(method, setter, spec)?.let { assignments += it }
                    field != null -> createFieldAssignmentFromSpec(method, field, spec)?.let { assignments += it }
                    else -> logger.error(
                        "No setter or writable field found for target '${spec.target}'.",
                        method.element
                    )
                }
            }
        }

        // 自动映射（同名属性）
        val allWritableTargets = (setterMap.keys + fieldMap.keys).toSet()
        val autoTargets = allWritableTargets - handledTargets
        val primarySource = method.primarySource

        if (autoTargets.isNotEmpty() && primarySource != null) {
            autoTargets.forEach { property ->
                val setter = setterMap[property]
                val field = fieldMap[property]
                when {
                    setter != null -> {
                        val assignment = createAutoAssignment(method, setter, primarySource, property)
                        if (assignment != null) {
                            assignments += assignment
                        } else {
                            checkAndReportTypeMismatch(method, primarySource, property, setter.paramType)
                        }
                    }
                    field != null -> {
                        val assignment = createAutoFieldAssignment(method, field, primarySource, property)
                        if (assignment != null) {
                            assignments += assignment
                        } else {
                            checkAndReportTypeMismatch(method, primarySource, property, field.type)
                        }
                    }
                }
            }
        }

        return assignments
    }

    /**
     * 生成单个赋值语句
     */
    private fun generateAssignment(
        methodBuilder: MethodSpec.Builder,
        assignment: Assignment,
        targetContext: TargetContext,
        method: MapperMethodDescriptor,
        usedNames: MutableSet<String>
    ) {
        when (assignment) {
            is PropertyAssignment -> {
                if (assignment.expression.startsWith("COLLECTION_MAPPING:")) {
                    val parts = assignment.expression.split(":", limit = 3)
                    if (parts.size == 3) {
                        generateCollectionMappingCodeBlock(
                            methodBuilder, targetContext.varName,
                            assignment.setterName, null,
                            parts[1], parts[2],
                            assignment.expressionType, usedNames
                        )
                    }
                } else {
                    val needNullCheck = method.needNullCheck ?: descriptor.needNullCheck
                    if (needNullCheck && assignment.expressionType != null &&
                        !isPrimitiveType(assignment.expressionType) &&
                        !assignment.expression.contains("?")
                    ) {
                        methodBuilder.beginControlFlow("if (\$L != null)", assignment.expression)
                        methodBuilder.addStatement("\$L.\$L(\$L)", targetContext.varName, assignment.setterName, assignment.expression)
                        methodBuilder.endControlFlow()
                    } else {
                        if (assignment.expressionType == null) {
                            // 自定义 expression，直接作为代码插入
                            methodBuilder.addCode(
                                CodeBlock.builder()
                                    .add("\$L.\$L(", targetContext.varName, assignment.setterName)
                                    .add(assignment.expression)
                                    .add(");\n")
                                    .build()
                            )
                        } else {
                            methodBuilder.addStatement("\$L.\$L(\$L)", targetContext.varName, assignment.setterName, assignment.expression)
                        }
                    }
                }
            }
            is FieldAssignment -> {
                if (assignment.expression.startsWith("COLLECTION_MAPPING:")) {
                    val parts = assignment.expression.split(":", limit = 3)
                    if (parts.size == 3) {
                        generateCollectionMappingCodeBlock(
                            methodBuilder, targetContext.varName,
                            null, assignment.fieldName,
                            parts[1], parts[2],
                            assignment.expressionType, usedNames
                        )
                    }
                } else {
                    val needNullCheck = method.needNullCheck ?: descriptor.needNullCheck
                    if (needNullCheck && assignment.expressionType != null &&
                        !isPrimitiveType(assignment.expressionType) &&
                        !assignment.expression.contains("?")
                    ) {
                        methodBuilder.beginControlFlow("if (\$L != null)", assignment.expression)
                        methodBuilder.addStatement("\$L.\$L = \$L", targetContext.varName, assignment.fieldName, assignment.expression)
                        methodBuilder.endControlFlow()
                    } else {
                        if (assignment.expressionType == null) {
                            methodBuilder.addCode(
                                CodeBlock.builder()
                                    .add("\$L.\$L = ", targetContext.varName, assignment.fieldName)
                                    .add(assignment.expression)
                                    .add(";\n")
                                    .build()
                            )
                        } else {
                            methodBuilder.addStatement("\$L.\$L = \$L", targetContext.varName, assignment.fieldName, assignment.expression)
                        }
                    }
                }
            }
            is NestedAssignment -> {
                generateNestedAssignment(methodBuilder, targetContext, assignment, usedNames)
            }
        }
    }

    // ======================== 显式映射赋值创建 ========================

    private fun createAssignmentFromSpec(
        method: MapperMethodDescriptor,
        setter: PropertyResolver.WritableProperty,
        spec: MappingSpec
    ): PropertyAssignment? {
        val expression = when {
            !spec.expression.isNullOrBlank() -> {
                val expr = spec.expression.trim()
                val processedExpr = if (expr.startsWith("java(") && expr.endsWith(")")) {
                    expr.substring(5, expr.length - 1)
                } else {
                    expr
                }
                ResolvedExpression(processedExpr, null)
            }
            !spec.constant.isNullOrBlank() -> ResolvedExpression(spec.constant, null)
            !spec.source.isNullOrBlank() -> resolveSourceExpression(method, spec.source)
            else -> resolveImplicitExpression(method, spec.target)
        }
        if (expression == null) {
            logger.error("Unable to resolve source for mapping target '${spec.target}'.", method.element)
            return null
        }

        val targetType = setter.paramType

        // expression 直接使用，不进行类型检查
        if (!spec.expression.isNullOrBlank()) {
            return PropertyAssignment(setter.setterName, expression.expression, null)
        }

        // 类型不匹配时尝试查找映射方法
        if (!isAssignable(expression.type, targetType)) {
            // 集合类型处理
            if (isCollectionType(expression.type) && isCollectionType(targetType)) {
                val sourceElementType = getCollectionElementType(expression.type)
                val targetElementType = getCollectionElementType(targetType)
                if (sourceElementType != null && targetElementType != null) {
                    if (!isAssignable(sourceElementType, targetElementType)) {
                        val elementMapperMethod = findElementMapperMethod(sourceElementType, targetElementType)
                        if (elementMapperMethod != null) {
                            val collectionExpr = "COLLECTION_MAPPING:${expression.expression}:${elementMapperMethod.name}"
                            return PropertyAssignment(setter.setterName, collectionExpr, targetType)
                        } else {
                            logger.error(
                                "Type mismatch for property '${spec.target}'. No mapping method found for element types.",
                                method.element
                            )
                            return null
                        }
                    } else {
                        val convExpr = generateCollectionConversionExpression(expression.expression, expression.type, targetType)
                        if (convExpr != null) {
                            return PropertyAssignment(setter.setterName, convExpr, targetType)
                        }
                    }
                }
            }

            // 普通类型映射方法查找
            val directMapperMethod = findElementMapperMethod(expression.type, targetType)
            if (directMapperMethod != null) {
                val className = descriptor.implementationName
                val mappingExpr = "${expression.expression} == null ? null : $className.${directMapperMethod.name}(${expression.expression})"
                return PropertyAssignment(setter.setterName, mappingExpr, targetType)
            }

            logger.error(
                "Type mismatch for property '${spec.target}'. Source type: ${expression.type}, Target type: $targetType",
                method.element
            )
            return null
        }

        // 集合类型转换（类型兼容但具体类型不同）
        if (isCollectionType(expression.type) && isCollectionType(targetType)) {
            val convExpr = generateCollectionConversionExpression(expression.expression, expression.type, targetType)
            if (convExpr != null) {
                return PropertyAssignment(setter.setterName, convExpr, targetType)
            }
        }

        return PropertyAssignment(setter.setterName, expression.expression, expression.type)
    }

    private fun createFieldAssignmentFromSpec(
        method: MapperMethodDescriptor,
        field: PropertyResolver.WritableField,
        spec: MappingSpec
    ): FieldAssignment? {
        val expression = when {
            !spec.expression.isNullOrBlank() -> {
                val expr = spec.expression.trim()
                val processedExpr = if (expr.startsWith("java(") && expr.endsWith(")")) {
                    expr.substring(5, expr.length - 1)
                } else {
                    expr
                }
                ResolvedExpression(processedExpr, null)
            }
            !spec.constant.isNullOrBlank() -> ResolvedExpression(spec.constant, null)
            !spec.source.isNullOrBlank() -> resolveSourceExpression(method, spec.source)
            else -> resolveImplicitExpression(method, spec.target)
        }
        if (expression == null) {
            logger.error("Unable to resolve source for mapping target '${spec.target}'.", method.element)
            return null
        }

        val targetType = field.type

        if (!spec.expression.isNullOrBlank()) {
            return FieldAssignment(field.name, expression.expression, null)
        }

        if (!isAssignable(expression.type, targetType)) {
            if (isCollectionType(expression.type) && isCollectionType(targetType)) {
                val sourceElementType = getCollectionElementType(expression.type)
                val targetElementType = getCollectionElementType(targetType)
                if (sourceElementType != null && targetElementType != null) {
                    if (!isAssignable(sourceElementType, targetElementType)) {
                        val elementMapperMethod = findElementMapperMethod(sourceElementType, targetElementType)
                        if (elementMapperMethod != null) {
                            val collectionExpr = "COLLECTION_MAPPING:${expression.expression}:${elementMapperMethod.name}"
                            return FieldAssignment(field.name, collectionExpr, targetType)
                        } else {
                            logger.error("Type mismatch for property '${spec.target}'.", method.element)
                            return null
                        }
                    } else {
                        val convExpr = generateCollectionConversionExpression(expression.expression, expression.type, targetType)
                        if (convExpr != null) return FieldAssignment(field.name, convExpr, targetType)
                    }
                }
            }

            val directMapperMethod = findElementMapperMethod(expression.type, targetType)
            if (directMapperMethod != null) {
                val className = descriptor.implementationName
                val mappingExpr = "${expression.expression} == null ? null : $className.${directMapperMethod.name}(${expression.expression})"
                return FieldAssignment(field.name, mappingExpr, targetType)
            }

            logger.error("Type mismatch for property '${spec.target}'.", method.element)
            return null
        }

        if (isCollectionType(expression.type) && isCollectionType(targetType)) {
            val convExpr = generateCollectionConversionExpression(expression.expression, expression.type, targetType)
            if (convExpr != null) return FieldAssignment(field.name, convExpr, targetType)
        }

        return FieldAssignment(field.name, expression.expression, expression.type)
    }

    // ======================== 自动映射赋值创建 ========================

    private fun createAutoAssignment(
        method: MapperMethodDescriptor,
        setter: PropertyResolver.WritableProperty,
        sourceParam: ParameterDescriptor,
        property: String
    ): PropertyAssignment? {
        val expression = resolveImplicitExpression(method, property, sourceParam) ?: return null
        val targetType = setter.paramType

        if (!isAssignable(expression.type, targetType)) {
            if (isCollectionType(expression.type) && isCollectionType(targetType)) {
                val sourceElementType = getCollectionElementType(expression.type)
                val targetElementType = getCollectionElementType(targetType)
                if (sourceElementType != null && targetElementType != null) {
                    if (!isAssignable(sourceElementType, targetElementType)) {
                        val elementMapperMethod = findElementMapperMethod(sourceElementType, targetElementType)
                        if (elementMapperMethod != null) {
                            val collectionExpr = "COLLECTION_MAPPING:${expression.expression}:${elementMapperMethod.name}"
                            return PropertyAssignment(setter.setterName, collectionExpr, targetType)
                        }
                    } else {
                        val convExpr = generateCollectionConversionExpression(expression.expression, expression.type, targetType)
                        if (convExpr != null) return PropertyAssignment(setter.setterName, convExpr, targetType)
                    }
                }
            }

            val directMapperMethod = findElementMapperMethod(expression.type, targetType)
            if (directMapperMethod != null) {
                val className = descriptor.implementationName
                val mappingExpr = "${expression.expression} == null ? null : $className.${directMapperMethod.name}(${expression.expression})"
                return PropertyAssignment(setter.setterName, mappingExpr, targetType)
            }

            return null
        }

        if (isCollectionType(expression.type) && isCollectionType(targetType)) {
            val convExpr = generateCollectionConversionExpression(expression.expression, expression.type, targetType)
            if (convExpr != null) return PropertyAssignment(setter.setterName, convExpr, targetType)
        }

        return PropertyAssignment(setter.setterName, expression.expression, expression.type)
    }

    private fun createAutoFieldAssignment(
        method: MapperMethodDescriptor,
        field: PropertyResolver.WritableField,
        sourceParam: ParameterDescriptor,
        property: String
    ): FieldAssignment? {
        val expression = resolveImplicitExpression(method, property, sourceParam) ?: return null
        val targetType = field.type

        if (!isAssignable(expression.type, targetType)) {
            if (isCollectionType(expression.type) && isCollectionType(targetType)) {
                val sourceElementType = getCollectionElementType(expression.type)
                val targetElementType = getCollectionElementType(targetType)
                if (sourceElementType != null && targetElementType != null) {
                    if (!isAssignable(sourceElementType, targetElementType)) {
                        val elementMapperMethod = findElementMapperMethod(sourceElementType, targetElementType)
                        if (elementMapperMethod != null) {
                            val collectionExpr = "COLLECTION_MAPPING:${expression.expression}:${elementMapperMethod.name}"
                            return FieldAssignment(field.name, collectionExpr, targetType)
                        }
                    } else {
                        val convExpr = generateCollectionConversionExpression(expression.expression, expression.type, targetType)
                        if (convExpr != null) return FieldAssignment(field.name, convExpr, targetType)
                    }
                }
            }

            val directMapperMethod = findElementMapperMethod(expression.type, targetType)
            if (directMapperMethod != null) {
                val className = descriptor.implementationName
                val mappingExpr = "${expression.expression} == null ? null : $className.${directMapperMethod.name}(${expression.expression})"
                return FieldAssignment(field.name, mappingExpr, targetType)
            }

            return null
        }

        if (isCollectionType(expression.type) && isCollectionType(targetType)) {
            val convExpr = generateCollectionConversionExpression(expression.expression, expression.type, targetType)
            if (convExpr != null) return FieldAssignment(field.name, convExpr, targetType)
        }

        return FieldAssignment(field.name, expression.expression, expression.type)
    }

    private fun checkAndReportTypeMismatch(
        method: MapperMethodDescriptor,
        primarySource: ParameterDescriptor,
        property: String,
        targetType: KSType
    ) {
        val sourceReadable = propertyResolver.readableProperties(primarySource.typeDeclaration)
        val sourceField = propertyResolver.findField(primarySource.typeDeclaration, property)

        val sourceType = when {
            sourceReadable.containsKey(property) -> sourceReadable[property]!!.type
            sourceField != null -> sourceField.type.resolve()
            else -> null
        }

        if (sourceType != null) {
            // 检查是否有映射方法可用
            if (isCollectionType(sourceType) && isCollectionType(targetType)) {
                val sourceElementType = getCollectionElementType(sourceType)
                val targetElementType = getCollectionElementType(targetType)
                if (sourceElementType != null && targetElementType != null) {
                    if (findElementMapperMethod(sourceElementType, targetElementType) != null) return
                }
            }
            if (findElementMapperMethod(sourceType, targetType) != null) return

            logger.error(
                "Auto-mapping failed: Type mismatch for property '$property'. " +
                        "Source type: $sourceType, Target type: $targetType",
                method.element
            )
        }
        // 如果 source 没有该属性，静默跳过
    }

    // ======================== 表达式解析 ========================

    private fun resolveImplicitExpression(
        method: MapperMethodDescriptor,
        property: String,
        sourceParam: ParameterDescriptor? = null
    ): ResolvedExpression? {
        val param = sourceParam ?: method.primarySource ?: return null
        return buildGetterChain(param, listOf(property), method, silent = true)
    }

    private fun resolveSourceExpression(
        method: MapperMethodDescriptor,
        sourcePath: String
    ): ResolvedExpression? {
        val parts = sourcePath.split('.').filter { it.isNotBlank() }
        if (parts.isEmpty()) return null

        val firstSegment = parts.first()
        val parameter = method.parameters
            .filter { !it.isMappingTarget }
            .firstOrNull { it.name == firstSegment }

        return if (parameter != null) {
            val propertyPath = parts.drop(1)
            if (propertyPath.isEmpty()) {
                ResolvedExpression(parameter.name, parameter.type)
            } else {
                buildGetterChain(parameter, propertyPath, method)
            }
        } else {
            buildGetterChain(method.primarySource ?: return null, parts, method)
        }
    }

    private fun buildGetterChain(
        parameter: ParameterDescriptor,
        path: List<String>,
        method: MapperMethodDescriptor,
        silent: Boolean = false
    ): ResolvedExpression? {
        if (path.isEmpty()) {
            return ResolvedExpression(parameter.name, parameter.type)
        }

        var currentDeclaration: KSClassDeclaration? = parameter.typeDeclaration
        var currentType: KSType = parameter.type
        var expression = parameter.name

        path.forEach { segment ->
            if (currentDeclaration == null) {
                if (!silent) {
                    logger.error("Cannot find type element for '$segment' on ${parameter.name}.", method.element)
                }
                return null
            }

            val getters = propertyResolver.readableProperties(currentDeclaration)
            val getter = getters[segment]
            if (getter != null) {
                expression += ".${getter.getterName}()"
                currentType = getter.type
                currentDeclaration = propertyResolver.asClassDeclaration(currentType)
            } else {
                val field = propertyResolver.findField(currentDeclaration, segment)
                if (field != null) {
                    expression += ".${field.simpleName.asString()}"
                    currentType = field.type.resolve()
                    currentDeclaration = propertyResolver.asClassDeclaration(currentType)
                } else {
                    if (!silent) {
                        logger.error("Cannot find getter or field for '$segment'.", method.element)
                    }
                    return null
                }
            }
        }
        return ResolvedExpression(expression, currentType)
    }

    // ======================== 嵌套对象映射 ========================

    private fun createNestedAssignment(
        method: MapperMethodDescriptor,
        targetContext: TargetContext,
        spec: MappingSpec,
        targetPath: List<String>
    ): Assignment? {
        val rootProperty = targetPath.first()
        val nestedPath = targetPath.drop(1)

        val setterMap = propertyResolver.writeableProperties(targetContext.typeDeclaration)
        val fieldMap = propertyResolver.writableFields(targetContext.typeDeclaration)
        val rootSetter = setterMap[rootProperty]
        val rootField = fieldMap[rootProperty]

        if (rootSetter == null && rootField == null) {
            logger.error("Cannot find root property '$rootProperty' for nested target '${spec.target}'.", method.element)
            return null
        }

        val sourceExpression = when {
            !spec.constant.isNullOrBlank() -> ResolvedExpression(spec.constant, null)
            !spec.source.isNullOrBlank() -> resolveSourceExpression(method, spec.source)
            else -> {
                val sourcePath = nestedPath.joinToString(".")
                resolveImplicitExpression(method, sourcePath)
            }
        }

        if (sourceExpression == null) {
            logger.error("Unable to resolve source for nested target '${spec.target}'.", method.element)
            return null
        }

        val intermediateType = rootSetter?.paramType ?: rootField?.type ?: return null
        val intermediateDeclaration = propertyResolver.asClassDeclaration(intermediateType)
        if (intermediateDeclaration == null) {
            logger.error("Cannot resolve intermediate type for nested target '${spec.target}'.", method.element)
            return null
        }

        return NestedAssignment(
            rootProperty = rootProperty,
            rootSetterName = rootSetter?.setterName,
            rootFieldName = rootField?.name,
            nestedPath = nestedPath,
            sourceExpression = sourceExpression,
            intermediateType = intermediateType,
            intermediateDeclaration = intermediateDeclaration
        )
    }

    private fun generateNestedAssignment(
        methodBuilder: MethodSpec.Builder,
        targetContext: TargetContext,
        assignment: NestedAssignment,
        usedNames: MutableSet<String>
    ) {
        val intermediateVarName = generateUniqueName("${assignment.rootProperty}Obj", usedNames)
        usedNames += intermediateVarName

        val intermediateTypeName = resolveTypeName(assignment.intermediateType)

        // 获取中间对象
        val getter = propertyResolver.readableProperties(targetContext.typeDeclaration)[assignment.rootProperty]
        if (getter != null) {
            methodBuilder.addStatement("\$T \$L = \$L.\$L()", intermediateTypeName, intermediateVarName, targetContext.varName, getter.getterName)
            methodBuilder.beginControlFlow("if (\$L == null)", intermediateVarName)
            methodBuilder.addStatement("\$L = new \$T()", intermediateVarName, intermediateTypeName)
            methodBuilder.endControlFlow()
        } else {
            methodBuilder.addStatement("\$T \$L = new \$T()", intermediateTypeName, intermediateVarName, intermediateTypeName)
        }

        // 设置嵌套属性
        val nestedProperty = assignment.nestedPath.first()
        val nestedSetters = propertyResolver.writeableProperties(assignment.intermediateDeclaration)
        val nestedFields = propertyResolver.writableFields(assignment.intermediateDeclaration)
        val nestedSetter = nestedSetters[nestedProperty]
        val nestedField = nestedFields[nestedProperty]

        when {
            nestedSetter != null -> methodBuilder.addStatement(
                "\$L.\$L(\$L)", intermediateVarName, nestedSetter.setterName, assignment.expression
            )
            nestedField != null -> methodBuilder.addStatement(
                "\$L.\$L = \$L", intermediateVarName, nestedField.name, assignment.expression
            )
            else -> logger.error("Cannot find setter or field '$nestedProperty' on intermediate type.", null)
        }

        // 设置回目标对象
        when {
            assignment.rootSetterName != null -> methodBuilder.addStatement(
                "\$L.\$L(\$L)", targetContext.varName, assignment.rootSetterName, intermediateVarName
            )
            assignment.rootFieldName != null -> methodBuilder.addStatement(
                "\$L.\$L = \$L", targetContext.varName, assignment.rootFieldName, intermediateVarName
            )
        }
    }

    // ======================== 集合互转方法 ========================

    private fun detectCollectionToCollectionMapping(method: MapperMethodDescriptor): Boolean {
        if (method.mappingTarget != null) return false
        val sourceParams = method.parameters.filter { !it.isMappingTarget }
        if (sourceParams.size != 1) return false
        val sourceType = sourceParams.first().type
        val returnType = method.returnType
        return isCollectionType(sourceType) && isCollectionType(returnType)
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

        val sourceType = primarySource.type
        val returnType = method.returnType

        val sourceElementType = getCollectionElementType(sourceType)
        val targetElementType = getCollectionElementType(returnType)

        if (sourceElementType == null || targetElementType == null) {
            logger.error("Cannot determine element types for collection mapping method ${method.name}.", method.element)
            methodBuilder.addStatement("return null")
            return methodBuilder.build()
        }

        // Source 空值检查
        methodBuilder.beginControlFlow("if (\$L == null)", primarySource.name)
        methodBuilder.addStatement("return null")
        methodBuilder.endControlFlow()

        val tempListVarName = generateUniqueName("tempList", usedNames)
        usedNames += tempListVarName

        val targetElementTypeName = resolveTypeName(targetElementType)
        val concreteListType = ClassName.get("java.util", "ArrayList")

        methodBuilder.addStatement(
            "\$T<\$T> \$L = new \$T<>()",
            concreteListType, targetElementTypeName, tempListVarName, concreteListType
        )

        methodBuilder.beginControlFlow("for (int i = 0; i < \$L.size(); i++)", primarySource.name)

        val isElementTypeSame = isSameType(sourceElementType, targetElementType)
        if (isElementTypeSame) {
            methodBuilder.addStatement("\$L.add(\$L.get(i))", tempListVarName, primarySource.name)
        } else {
            val elementMapperMethod = findElementMapperMethod(sourceElementType, targetElementType)
            if (elementMapperMethod != null) {
                val className = ClassName.get(descriptor.packageName, descriptor.implementationName)
                methodBuilder.addStatement(
                    "\$L.add(\$T.\$L(\$L.get(i)))",
                    tempListVarName, className, elementMapperMethod.name, primarySource.name
                )
            } else {
                logger.error(
                    "Cannot find mapping method for element types in method ${method.name}.",
                    method.element
                )
                methodBuilder.addStatement("\$L.add(null)", tempListVarName)
            }
        }

        methodBuilder.endControlFlow()
        methodBuilder.addStatement("return \$L", tempListVarName)

        return methodBuilder.build()
    }

    /**
     * 生成集合映射代码块（for 循环，兼容低版本 Android）
     */
    private fun generateCollectionMappingCodeBlock(
        methodBuilder: MethodSpec.Builder,
        targetVarName: String,
        targetSetterName: String?,
        targetFieldName: String?,
        sourceExpression: String,
        mapperMethodName: String,
        targetType: KSType?,
        usedNames: MutableSet<String>
    ) {
        val tempListVarName = generateUniqueName("tempList", usedNames)
        usedNames += tempListVarName

        val targetElementType = if (targetType != null) getCollectionElementType(targetType) else null
        val targetElementTypeName = if (targetElementType != null) resolveTypeName(targetElementType) else ClassName.OBJECT
        val concreteListType = ClassName.get("java.util", "ArrayList")
        val className = ClassName.get(descriptor.packageName, descriptor.implementationName)

        methodBuilder.beginControlFlow("if (\$L == null)", sourceExpression)
        when {
            targetSetterName != null -> methodBuilder.addStatement("\$L.\$L(null)", targetVarName, targetSetterName)
            targetFieldName != null -> methodBuilder.addStatement("\$L.\$L = null", targetVarName, targetFieldName)
        }
        methodBuilder.nextControlFlow("else")
        methodBuilder.addStatement("\$T<\$T> \$L = new \$T<>()", concreteListType, targetElementTypeName, tempListVarName, concreteListType)
        methodBuilder.beginControlFlow("for (int i = 0; i < \$L.size(); i++)", sourceExpression)
        methodBuilder.addStatement("\$L.add(\$T.\$L(\$L.get(i)))", tempListVarName, className, mapperMethodName, sourceExpression)
        methodBuilder.endControlFlow()
        when {
            targetSetterName != null -> methodBuilder.addStatement("\$L.\$L(\$L)", targetVarName, targetSetterName, tempListVarName)
            targetFieldName != null -> methodBuilder.addStatement("\$L.\$L = \$L", targetVarName, targetFieldName, tempListVarName)
        }
        methodBuilder.endControlFlow()
    }

    private fun generateCollectionConversionExpression(
        sourceExpression: String,
        sourceType: KSType?,
        targetType: KSType?
    ): String? {
        if (sourceType == null || targetType == null) return null
        if (isSameType(sourceType, targetType)) return null

        val targetTypeName = getCollectionTypeName(targetType) ?: return null
        return "$sourceExpression == null ? null : new $targetTypeName<>($sourceExpression)"
    }

    // ======================== 类型工具方法 ========================

    private fun resolveTargetContext(
        method: MapperMethodDescriptor,
        usedNames: MutableSet<String>
    ): TargetContext? {
        val isUpdate = method.mappingTarget != null
        val targetType = if (isUpdate) method.mappingTarget!!.type else method.returnType
        val targetDeclaration = propertyResolver.asClassDeclaration(targetType)

        if (targetDeclaration == null) {
            logger.error("Cannot resolve target type for method ${method.name}", method.element)
            return null
        }

        val varName = if (isUpdate) {
            method.mappingTarget!!.name
        } else {
            generateUniqueName("target", usedNames)
        }

        return TargetContext(
            type = targetType,
            typeDeclaration = targetDeclaration,
            varName = varName,
            isUpdate = isUpdate
        )
    }

    /**
     * 查找 Mapper 中是否有元素类型的映射方法
     */
    private fun findElementMapperMethod(sourceType: KSType?, targetType: KSType?): MapperMethodDescriptor? {
        if (sourceType == null || targetType == null) return null
        return descriptor.methods.firstOrNull { method ->
            val sourceParam = method.parameters.firstOrNull { !it.isMappingTarget }
            val hasMappingTarget = method.mappingTarget != null

            when {
                hasMappingTarget -> {
                    val mappingTargetType = method.mappingTarget?.type ?: return@firstOrNull false
                    if (sourceParam == null) return@firstOrNull false
                    isTypeCompatible(mappingTargetType, targetType) && isTypeCompatible(sourceParam.type, sourceType)
                }
                else -> {
                    if (sourceParam == null || method.parameters.size != 1) return@firstOrNull false
                    isTypeCompatible(method.returnType, targetType) && isTypeCompatible(sourceParam.type, sourceType)
                }
            }
        }
    }

    /**
     * 判断两个类型是否兼容
     */
    private fun isTypeCompatible(type1: KSType, type2: KSType): Boolean {
        val decl1 = type1.declaration.qualifiedName?.asString() ?: return false
        val decl2 = type2.declaration.qualifiedName?.asString() ?: return false
        return decl1 == decl2
    }

    /**
     * 判断 source 类型是否可赋值给 target 类型
     */
    private fun isAssignable(source: KSType?, target: KSType): Boolean {
        if (source == null) return true

        val sourceQName = source.declaration.qualifiedName?.asString() ?: return false
        val targetQName = target.declaration.qualifiedName?.asString() ?: return false

        // 相同类型（非集合时直接返回 true）
        if (sourceQName == targetQName && !isCollectionType(source)) return true

        // 基本类型与包装类型兼容
        if (isPrimitiveBoxingMatch(sourceQName, targetQName)) return true

        // 集合类型兼容性：必须比较元素类型
        if (isCollectionType(source) && isCollectionType(target)) {
            val sourceElement = getCollectionElementType(source)
            val targetElement = getCollectionElementType(target)
            if (sourceElement != null && targetElement != null) {
                return isAssignable(sourceElement, targetElement)
            }
            // 如果无法获取元素类型，认为集合类型相同即兼容
            return sourceQName == targetQName
        }

        // 相同类型（非集合已在上面处理，这里处理其他情况）
        if (sourceQName == targetQName) return true

        // 检查继承关系
        val sourceDecl = source.declaration as? KSClassDeclaration ?: return false
        return sourceDecl.getAllSuperTypes().any { superType ->
            superType.declaration.qualifiedName?.asString() == targetQName
        }
    }

    private fun isSameType(type1: KSType, type2: KSType): Boolean {
        return type1.declaration.qualifiedName?.asString() == type2.declaration.qualifiedName?.asString()
    }

    private fun isPrimitiveType(type: KSType): Boolean {
        val name = type.declaration.qualifiedName?.asString() ?: return false
        return name in PRIMITIVE_TYPES
    }

    private fun isPrimitiveNumericType(name: String): Boolean {
        return name in setOf(
            "kotlin.Byte", "kotlin.Short", "kotlin.Int", "kotlin.Long",
            "kotlin.Float", "kotlin.Double", "kotlin.Char",
            "java.lang.Byte", "java.lang.Short", "java.lang.Integer", "java.lang.Long",
            "java.lang.Float", "java.lang.Double", "java.lang.Character"
        )
    }

    private fun isPrimitiveBoxingMatch(name1: String, name2: String): Boolean {
        val mapping = mapOf(
            "kotlin.Int" to "java.lang.Integer",
            "kotlin.Long" to "java.lang.Long",
            "kotlin.Double" to "java.lang.Double",
            "kotlin.Float" to "java.lang.Float",
            "kotlin.Boolean" to "java.lang.Boolean",
            "kotlin.Byte" to "java.lang.Byte",
            "kotlin.Short" to "java.lang.Short",
            "kotlin.Char" to "java.lang.Character",
            "int" to "java.lang.Integer",
            "long" to "java.lang.Long",
            "double" to "java.lang.Double",
            "float" to "java.lang.Float",
            "boolean" to "java.lang.Boolean",
            "byte" to "java.lang.Byte",
            "short" to "java.lang.Short",
            "char" to "java.lang.Character"
        )
        return mapping[name1] == name2 || mapping[name2] == name1 ||
                mapping.entries.any { (k, v) -> (k == name1 && v == name2) || (k == name2 && v == name1) }
    }

    private fun isCollectionType(type: KSType?): Boolean {
        if (type == null) return false
        val name = type.declaration.qualifiedName?.asString() ?: return false
        return name in COLLECTION_TYPES
    }

    private fun getCollectionElementType(type: KSType?): KSType? {
        if (type == null) return null
        val typeArgs = type.arguments
        if (typeArgs.isEmpty()) return null
        return typeArgs.first().type?.resolve()
    }

    private fun getCollectionTypeName(type: KSType?): String? {
        if (type == null) return null
        val name = type.declaration.qualifiedName?.asString() ?: return null
        return when {
            name == "java.util.ArrayList" || name == "kotlin.collections.ArrayList" -> "java.util.ArrayList"
            name == "java.util.LinkedList" -> "java.util.LinkedList"
            name == "java.util.HashSet" || name == "kotlin.collections.HashSet" -> "java.util.HashSet"
            name == "java.util.LinkedHashSet" || name == "kotlin.collections.LinkedHashSet" -> "java.util.LinkedHashSet"
            name == "java.util.List" || name == "kotlin.collections.List" || name == "kotlin.collections.MutableList" -> "java.util.ArrayList"
            name == "java.util.Set" || name == "kotlin.collections.Set" || name == "kotlin.collections.MutableSet" -> "java.util.HashSet"
            name == "java.util.Collection" || name == "kotlin.collections.Collection" -> "java.util.ArrayList"
            else -> null
        }
    }

    /**
     * 将 KSType 转换为 JavaPoet TypeName
     */
    private fun resolveTypeName(type: KSType?): TypeName {
        if (type == null) return TypeName.VOID
        val qualifiedName = type.declaration.qualifiedName?.asString() ?: return TypeName.OBJECT

        // 处理基本类型
        val primitiveTypeName = KOTLIN_TO_JAVA_TYPE[qualifiedName]
        if (primitiveTypeName != null) return primitiveTypeName

        // 处理集合类型（带泛型）
        val typeArgs = type.arguments
        if (typeArgs.isNotEmpty() && isCollectionType(type)) {
            val javaCollectionName = when (qualifiedName) {
                "kotlin.collections.List", "kotlin.collections.MutableList" -> "java.util.List"
                "kotlin.collections.Set", "kotlin.collections.MutableSet" -> "java.util.Set"
                "kotlin.collections.ArrayList", "kotlin.collections.MutableList" -> "java.util.ArrayList"
                "kotlin.collections.Map", "kotlin.collections.MutableMap" -> "java.util.Map"
                else -> qualifiedName
            }
            val rawType = ClassName.bestGuess(javaCollectionName)
            val elementType = typeArgs.first().type?.resolve()
            if (elementType != null) {
                val elementTypeName = resolveTypeName(elementType)
                return com.squareup.javapoet.ParameterizedTypeName.get(rawType, elementTypeName)
            }
            return rawType
        }

        // 处理普通类型
        return ClassName.bestGuess(qualifiedName)
    }

    private fun generateUniqueName(base: String, used: MutableSet<String>): String {
        var candidate = base
        var index = 0
        while (used.contains(candidate)) {
            index++
            candidate = base + index
        }
        return candidate
    }

    // ======================== Javadoc 注释生成 ========================

    private fun generateMappingComment(method: MapperMethodDescriptor): String {
        val primarySource = method.primarySource ?: return ""
        val targetType = if (method.mappingTarget != null) method.mappingTarget.type else method.returnType
        val targetDeclaration = propertyResolver.asClassDeclaration(targetType) ?: return ""

        val sourceReadable = propertyResolver.readableProperties(primarySource.typeDeclaration)
        val targetWritable = propertyResolver.writeableProperties(targetDeclaration)
        val targetFields = propertyResolver.writableFields(targetDeclaration)

        val allSourceProperties = mutableSetOf<String>()
        allSourceProperties.addAll(sourceReadable.keys)

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
                    val sourceName = spec.source.split('.').first()
                    mappedSources += sourceName
                    explicitMappings += Pair(sourceName, targetName)
                } else {
                    mappedSources += targetName
                    autoMapped += targetName
                }
            }
        }

        // 自动映射
        val autoTargets = allTargetProperties - mappedTargets
        autoTargets.forEach { targetProp ->
            if (allSourceProperties.contains(targetProp)) {
                val sourceType = sourceReadable[targetProp]?.type
                val targetType2 = targetWritable[targetProp]?.paramType ?: targetFields[targetProp]?.type
                if (sourceType != null && targetType2 != null && isAssignable(sourceType, targetType2)) {
                    autoMapped += targetProp
                    mappedTargets += targetProp
                    mappedSources += targetProp
                }
            }
        }

        val unmappedSources = allSourceProperties - mappedSources
        val unmappedTargets = allTargetProperties - mappedTargets

        val comment = StringBuilder()
        comment.append("字段映射详情：\n")
        comment.append("源对象：${primarySource.typeDeclaration?.simpleName?.asString() ?: "Unknown"}\n")
        comment.append("目标对象：${targetDeclaration.simpleName.asString()}\n\n")

        if (explicitMappings.isNotEmpty()) {
            comment.append("显式映射（不同名）：\n")
            explicitMappings.forEach { (source, target) -> comment.append("  - $source -> $target\n") }
            comment.append("\n")
        }

        if (autoMapped.isNotEmpty()) {
            comment.append("自动映射（同名）：\n")
            autoMapped.forEach { prop -> comment.append("  - $prop\n") }
            comment.append("\n")
        }

        if (unmappedSources.isNotEmpty()) {
            comment.append("未映射的源字段：\n")
            unmappedSources.sorted().forEach { prop -> comment.append("  - $prop\n") }
            comment.append("\n")
        }

        if (unmappedTargets.isNotEmpty()) {
            comment.append("未映射的目标字段：\n")
            unmappedTargets.sorted().forEach { prop -> comment.append("  - $prop\n") }
            comment.append("\n")
        }

        return comment.toString()
    }

    // ======================== 常量 ========================

    companion object {
        private val PRIMITIVE_TYPES = setOf(
            "kotlin.Byte", "kotlin.Short", "kotlin.Int", "kotlin.Long",
            "kotlin.Float", "kotlin.Double", "kotlin.Char", "kotlin.Boolean",
            "java.lang.Byte", "java.lang.Short", "java.lang.Integer", "java.lang.Long",
            "java.lang.Float", "java.lang.Double", "java.lang.Character", "java.lang.Boolean",
            "int", "long", "double", "float", "boolean", "byte", "short", "char"
        )

        private val COLLECTION_TYPES = setOf(
            "java.util.List", "java.util.Set", "java.util.Collection",
            "java.util.ArrayList", "java.util.LinkedList",
            "java.util.HashSet", "java.util.LinkedHashSet",
            "kotlin.collections.List", "kotlin.collections.MutableList",
            "kotlin.collections.Set", "kotlin.collections.MutableSet",
            "kotlin.collections.ArrayList", "kotlin.collections.HashSet",
            "kotlin.collections.LinkedHashSet", "kotlin.collections.Collection"
        )

        private val KOTLIN_TO_JAVA_TYPE: Map<String, TypeName> = mapOf(
            "kotlin.Unit" to TypeName.VOID,
            "kotlin.Int" to TypeName.INT,
            "kotlin.Long" to TypeName.LONG,
            "kotlin.Double" to TypeName.DOUBLE,
            "kotlin.Float" to TypeName.FLOAT,
            "kotlin.Boolean" to TypeName.BOOLEAN,
            "kotlin.Byte" to TypeName.BYTE,
            "kotlin.Short" to TypeName.SHORT,
            "kotlin.Char" to TypeName.CHAR,
            "kotlin.String" to ClassName.get("java.lang", "String"),
            "java.lang.String" to ClassName.get("java.lang", "String"),
            "java.lang.Integer" to ClassName.get("java.lang", "Integer"),
            "java.lang.Long" to ClassName.get("java.lang", "Long"),
            "java.lang.Double" to ClassName.get("java.lang", "Double"),
            "java.lang.Float" to ClassName.get("java.lang", "Float"),
            "java.lang.Boolean" to ClassName.get("java.lang", "Boolean"),
            "java.lang.Byte" to ClassName.get("java.lang", "Byte"),
            "java.lang.Short" to ClassName.get("java.lang", "Short"),
            "java.lang.Character" to ClassName.get("java.lang", "Character"),
            "java.lang.Object" to TypeName.OBJECT
        )
    }
}

// ======================== 数据类 ========================

private data class TargetContext(
    val type: KSType,
    val typeDeclaration: KSClassDeclaration,
    val varName: String,
    val isUpdate: Boolean
)

private sealed class Assignment {
    abstract val expression: String
    abstract val expressionType: KSType?
}

private data class PropertyAssignment(
    val setterName: String,
    override val expression: String,
    override val expressionType: KSType?
) : Assignment()

private data class FieldAssignment(
    val fieldName: String,
    override val expression: String,
    override val expressionType: KSType?
) : Assignment()

private data class NestedAssignment(
    val rootProperty: String,
    val rootSetterName: String?,
    val rootFieldName: String?,
    val nestedPath: List<String>,
    val sourceExpression: ResolvedExpression,
    val intermediateType: KSType,
    val intermediateDeclaration: KSClassDeclaration
) : Assignment() {
    override val expression: String = sourceExpression.expression
    override val expressionType: KSType? = sourceExpression.type
}

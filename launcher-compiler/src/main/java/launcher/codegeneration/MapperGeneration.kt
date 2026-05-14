package launcher.codegeneration

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import launcher.error.error
import launcher.mapper.MapperDescriptor
import launcher.mapper.MapperMethodDescriptor
import launcher.mapper.MappingSpec
import launcher.mapper.ParameterDescriptor
import launcher.mapper.PropertyResolver
import launcher.mapper.ResolvedExpression
import mapper.MappingTarget
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror

class MapperGeneration(
    private val processingEnv: ProcessingEnvironment,
    private val propertyResolver: PropertyResolver,
    private val descriptor: MapperDescriptor
) {

    private val typeUtils = processingEnv.typeUtils
    private val elementUtils = processingEnv.elementUtils

    /**
     * 判断 Mapper 接口是否来自 Kotlin 源文件
     * 直接从 MapperDescriptor 中获取（在 ActivityLauncherProcessor 中已检测）
     */
    private fun isKotlinSource(): Boolean {
        return descriptor.isKotlinSource
    }

    /**
     * 判断类型是否为基本类型（primitive type）
     * 基本类型包括：byte, short, int, long, float, double, char, boolean
     * 基本类型不能为 null，因此不需要进行空值检查
     */
    private fun isPrimitiveType(type: TypeMirror?): Boolean {
        if (type == null) return false
        return when (type.kind) {
            javax.lang.model.type.TypeKind.BYTE,
            javax.lang.model.type.TypeKind.SHORT,
            javax.lang.model.type.TypeKind.INT,
            javax.lang.model.type.TypeKind.LONG,
            javax.lang.model.type.TypeKind.FLOAT,
            javax.lang.model.type.TypeKind.DOUBLE,
            javax.lang.model.type.TypeKind.CHAR,
            javax.lang.model.type.TypeKind.BOOLEAN -> true
            else -> false
        }
    }

    fun brewJava(): JavaFile = JavaFile.builder(descriptor.packageName, createTypeSpec())
        .addFileComment("Auto Generated code from Mapper. Do not modify!!!!!!!")
        .build()

    private fun createTypeSpec(): TypeSpec {
        val builder = TypeSpec.classBuilder(descriptor.implementationName)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        // 不再实现接口，所有方法都是静态的

        // 生成静态映射方法
        descriptor.methods.forEach { method ->
            builder.addMethod(createStaticMethod(method))
        }

        // 生成静态版本的 @BeforeMapping 和 @AfterMapping 方法
        // 所有生命周期方法都生成为静态方法
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
    /**
     * 为 @MappingIgnore 方法生成静态实现
     * 这些方法通常有方法体，需要调用接口的默认实现
     */
    private fun createStaticIgnoredMethod(ignoredMethod: ExecutableElement): MethodSpec {
        val methodBuilder = MethodSpec.methodBuilder(ignoredMethod.simpleName.toString())
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(TypeName.get(ignoredMethod.returnType))

        // 添加参数
        val paramNames = mutableListOf<String>()
        ignoredMethod.parameters.forEach { param ->
            val paramType = TypeName.get(param.asType())
            val paramName = param.simpleName.toString()
            methodBuilder.addParameter(paramType, paramName)
            paramNames.add(paramName)
        }

        // 检查是否有方法体（default 方法或 Kotlin 接口方法）
        val hasDefaultImpl = ignoredMethod.modifiers.contains(Modifier.DEFAULT) ||
                ignoredMethod.defaultValue != null

        if (hasDefaultImpl || descriptor.isKotlinSource) {
            // 如果有默认实现，调用接口的默认实现
            val isKotlin = descriptor.isKotlinSource
            if (isKotlin) {
                // Kotlin 源文件：调用 DefaultImpls
                val interfaceName = descriptor.mapperElement.simpleName.toString()
                val methodName = ignoredMethod.simpleName.toString()
                val paramList = paramNames.joinToString(", ")

                // 对于 Kotlin 接口方法，调用 DefaultImpls
                // 统一使用 null 作为第一个参数（与 @BeforeMapping/@AfterMapping 保持一致）
                // 如果方法体中使用了 this，可能会抛出 NPE，但通常 @MappingIgnore 方法不依赖实例状态
                methodBuilder.addComment("调用接口的默认实现")

                if (ignoredMethod.returnType.kind == TypeKind.VOID) {
                    // void 方法
                    methodBuilder.addStatement(
                        "\$L.DefaultImpls.\$L(null\$L)",
                        interfaceName,
                        methodName,
                        if (paramList.isEmpty()) "" else ", $paramList"
                    )
                } else {
                    // 有返回值的方法
                    methodBuilder.addStatement(
                        "return \$L.DefaultImpls.\$L(null\$L)",
                        interfaceName,
                        methodName,
                        if (paramList.isEmpty()) "" else ", $paramList"
                    )
                }
            } else {
                // Java default 方法：无法在静态方法中调用，生成空实现
                generateEmptyImplementation(methodBuilder, ignoredMethod)
            }
        } else {
            // 抽象方法：不应该出现（@MappingIgnore 方法应该有方法体）
            generateEmptyImplementation(methodBuilder, ignoredMethod)
        }

        return methodBuilder.build()
    }

    /**
     * 为生命周期方法生成静态实现
     *
     * 对于有方法体的接口方法（default 方法或 Kotlin 接口方法）：
     * - 调用接口的 DefaultImpls（Kotlin）或生成空实现（Java）
     *
     * 对于抽象方法：
     * - 生成空实现
     */
    private fun createStaticLifecycleMethod(lifecycleMethod: ExecutableElement): MethodSpec {
        val methodBuilder = MethodSpec.methodBuilder(lifecycleMethod.simpleName.toString())
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(TypeName.get(lifecycleMethod.returnType))

        // 添加参数
        val paramNames = mutableListOf<String>()
        lifecycleMethod.parameters.forEach { param ->
            val paramType = TypeName.get(param.asType())
            val paramName = param.simpleName.toString()
            methodBuilder.addParameter(paramType, paramName)
            paramNames.add(paramName)
        }

        // 检查是否有方法体（default 方法或 Kotlin 接口方法）
        val hasDefaultImpl = lifecycleMethod.modifiers.contains(javax.lang.model.element.Modifier.DEFAULT) ||
                lifecycleMethod.defaultValue != null

        if (hasDefaultImpl || descriptor.isKotlinSource) {
            // 如果有默认实现，尝试调用 Kotlin DefaultImpls
            val isKotlin = descriptor.isKotlinSource
            if (isKotlin) {
                // Kotlin 源文件：调用 DefaultImpls
                val interfaceName = descriptor.mapperElement.simpleName.toString()
                val paramList = paramNames.joinToString(", ")

                // 静态方法中调用 DefaultImpls，第一个参数传 null（不需要实例）
                // 但这可能导致 NPE，所以更好的方式是创建临时实例
                methodBuilder.addComment("调用接口的默认实现")
                methodBuilder.addStatement(
                    "\$L.DefaultImpls.\$L(null\$L)",
                    interfaceName,
                    lifecycleMethod.simpleName.toString(),
                    if (paramList.isEmpty()) "" else ", $paramList"
                )
            } else {
                // Java default 方法：生成空实现（无法在静态方法中调用）
                generateEmptyImplementation(methodBuilder, lifecycleMethod)
            }
        } else {
            // 抽象方法：生成空实现
            generateEmptyImplementation(methodBuilder, lifecycleMethod)
        }

        return methodBuilder.build()
    }

    /**
     * 生成空实现
     */
    private fun generateEmptyImplementation(methodBuilder: MethodSpec.Builder, method: ExecutableElement) {
        if (method.returnType.kind == TypeKind.VOID) {
            // void 方法，生成空实现
        } else {
            // 非 void 方法，必须返回一个值
            val returnType = method.returnType
            when (returnType.kind) {
                TypeKind.BOOLEAN -> methodBuilder.addStatement("return false")
                TypeKind.BYTE, TypeKind.SHORT, TypeKind.INT, TypeKind.LONG,
                TypeKind.CHAR, TypeKind.FLOAT, TypeKind.DOUBLE -> methodBuilder.addStatement("return 0")
                else -> methodBuilder.addStatement("return null")
            }
        }
    }

    /**
     * 创建静态映射方法
     */
    private fun createStaticMethod(method: MapperMethodDescriptor): MethodSpec {
        val methodBuilder = MethodSpec.methodBuilder(method.name)
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(TypeName.get(method.returnType))

        method.element.thrownTypes.forEach { methodBuilder.addException(TypeName.get(it)) }

        // 生成详细的字段映射注释
        val mappingComment = generateMappingComment(method)
        if (mappingComment.isNotEmpty()) {
            methodBuilder.addJavadoc(mappingComment)
        }

        val usedNames = mutableSetOf<String>()
        method.parameters.forEach { param ->
            val parameterSpec = ParameterSpec.builder(TypeName.get(param.type), param.name).build()
            methodBuilder.addParameter(parameterSpec)
            usedNames += param.name
        }

        // 检测是否是集合互转方法（如 List<A> -> List<B>）
        val isCollectionToCollection = detectCollectionToCollectionMapping(method)
        if (isCollectionToCollection) {
            return generateCollectionToCollectionMethod(method, methodBuilder, usedNames)
        }

        val targetContext = resolveTargetContext(method, usedNames) ?: run {
            methodBuilder.addStatement("throw new IllegalStateException(\$S)", "Unable to resolve target type")
            return methodBuilder.build()
        }

        // 对 source 参数进行空值检查
        // 如果 source 为 null，且方法返回类型不是 void，则返回 null
        if (method.returnType.kind != TypeKind.VOID) {
            val primarySource = method.primarySource
            if (primarySource != null && !isPrimitiveType(primarySource.type)) {
                // 对非基本类型的 source 参数进行空值检查
                methodBuilder.beginControlFlow("if (\$L == null)", primarySource.name)
                methodBuilder.addStatement("return null")
                methodBuilder.endControlFlow()
            }
        }

        if (!targetContext.isUpdate && method.returnType.kind == TypeKind.VOID) {
            error(method.element, "Mapper method ${method.name} must return a value when no @MappingTarget parameter is present.")
            methodBuilder.addStatement("return null")
            return methodBuilder.build()
        }

        if (!targetContext.isUpdate && !typeUtils.isSameType(method.returnType, targetContext.type)) {
            error(method.element, "Return type must match target type for method ${method.name}.")
        }

        if (targetContext.isUpdate && method.returnType.kind != TypeKind.VOID &&
            !typeUtils.isSameType(method.returnType, targetContext.type)
        ) {
            error(method.element, "Update method ${method.name} must return void or the same type as @MappingTarget.")
        }

        if (!targetContext.isUpdate) {
            val typeName = TypeName.get(targetContext.type)
            methodBuilder.addStatement("\$T \$L = new \$T()", typeName, targetContext.varName, typeName)
            usedNames += targetContext.varName
        } else {
            methodBuilder.beginControlFlow("if (\$L == null)", targetContext.varName)
                .addStatement("throw new IllegalArgumentException(\$S)", "@MappingTarget parameter ${targetContext.varName} must not be null")
                .endControlFlow()
        }

        // 调用 @BeforeMapping 方法（在字段映射之前）
        descriptor.beforeMappingMethods.forEach { beforeMethod ->
            callLifecycleMethod(methodBuilder, beforeMethod, method, targetContext, usedNames)
        }

        collectAssignments(method, targetContext).forEach { assignment ->
            when (assignment) {
                is PropertyAssignment -> {
                    // 检查是否是集合映射表达式（使用特殊标记）
                    if (assignment.expression.startsWith("COLLECTION_MAPPING:")) {
                        val parts = assignment.expression.split(":", limit = 3)
                        if (parts.size == 3) {
                            val sourceExpression = parts[1]
                            val mapperMethodName = parts[2]
                            generateCollectionMappingCodeBlock(
                                methodBuilder,
                                targetContext.varName,
                                assignment.setter.simpleName.toString(),
                                null,
                                sourceExpression,
                                mapperMethodName,
                                assignment.expressionType ?: assignment.setter.parameters.first().asType(),
                                usedNames
                            )
                        } else {
                            methodBuilder.addStatement("\$L.\$L(\$L)", targetContext.varName, assignment.setter.simpleName, assignment.expression)
                        }
                    } else {
                        // 优先使用方法级配置，如果没有则使用类级配置
                        val needNullCheck = method.needNullCheck ?: descriptor.needNullCheck
                        // 根据 needNullCheck 配置决定是否添加 null 检查
                        // 只对非基本类型进行空值检查（基本类型不能为 null）
                        if (needNullCheck && assignment.expressionType != null &&
                            !isPrimitiveType(assignment.expressionType) &&
                            !assignment.expression.contains("?")) {
                            // 表达式不包含三元运算符，说明是简单的属性访问，需要添加 null 检查
                            methodBuilder.beginControlFlow("if (\$L != null)", assignment.expression)
                            methodBuilder.addStatement("\$L.\$L(\$L)", targetContext.varName, assignment.setter.simpleName, assignment.expression)
                            methodBuilder.endControlFlow()
                        } else {
                            // 对于 expression，使用 addCode 确保正确解析复杂表达式
                            // 当 expressionType 为 null 时，说明是自定义 expression，需要直接插入代码
                            if (assignment.expressionType == null) {
                                // 自定义 expression，直接作为代码插入（不进行转义）
                                // 使用 CodeBlock.builder().add() 来直接插入代码字符串
                                methodBuilder.addCode(
                                    CodeBlock.builder()
                                        .add("\$L.\$L(", targetContext.varName, assignment.setter.simpleName)
                                        .add(assignment.expression)
                                        .add(");\n")
                                        .build()
                                )
                            } else {
                                // 普通表达式，使用 addStatement
                                methodBuilder.addStatement("\$L.\$L(\$L)", targetContext.varName, assignment.setter.simpleName, assignment.expression)
                            }
                        }
                    }
                }
                is FieldAssignment -> {
                    // 检查是否是集合映射表达式（使用特殊标记）
                    if (assignment.expression.startsWith("COLLECTION_MAPPING:")) {
                        val parts = assignment.expression.split(":", limit = 3)
                        if (parts.size == 3) {
                            val sourceExpression = parts[1]
                            val mapperMethodName = parts[2]
                            generateCollectionMappingCodeBlock(
                                methodBuilder,
                                targetContext.varName,
                                null,
                                assignment.field.simpleName.toString(),
                                sourceExpression,
                                mapperMethodName,
                                assignment.expressionType ?: assignment.field.asType(),
                                usedNames
                            )
                        } else {
                            methodBuilder.addStatement("\$L.\$L = \$L", targetContext.varName, assignment.field.simpleName, assignment.expression)
                        }
                    } else {
                        // 优先使用方法级配置，如果没有则使用类级配置
                        val needNullCheck = method.needNullCheck ?: descriptor.needNullCheck
                        // 根据 needNullCheck 配置决定是否添加 null 检查
                        // 只对非基本类型进行空值检查（基本类型不能为 null）
                        if (needNullCheck && assignment.expressionType != null &&
                            !isPrimitiveType(assignment.expressionType) &&
                            !assignment.expression.contains("?")) {
                            // 表达式不包含三元运算符，说明是简单的属性访问，需要添加 null 检查
                            methodBuilder.beginControlFlow("if (\$L != null)", assignment.expression)
                            methodBuilder.addStatement("\$L.\$L = \$L", targetContext.varName, assignment.field.simpleName, assignment.expression)
                            methodBuilder.endControlFlow()
                        } else {
                            // 对于 expression，使用 addCode 确保正确解析复杂表达式
                            // 当 expressionType 为 null 时，说明是自定义 expression，需要直接插入代码
                            if (assignment.expressionType == null) {
                                // 自定义 expression，直接作为代码插入（不进行转义）
                                // 使用 CodeBlock.builder().add() 来直接插入代码字符串
                                methodBuilder.addCode(
                                    CodeBlock.builder()
                                        .add("\$L.\$L = ", targetContext.varName, assignment.field.simpleName)
                                        .add(assignment.expression)
                                        .add(";\n")
                                        .build()
                                )
                            } else {
                                // 普通表达式，使用 addStatement
                                methodBuilder.addStatement("\$L.\$L = \$L", targetContext.varName, assignment.field.simpleName, assignment.expression)
                            }
                        }
                    }
                }
                is NestedAssignment -> {
                    generateNestedAssignment(methodBuilder, targetContext, assignment, usedNames, method)
                }
            }
        }

        // 调用 @AfterMapping 方法（在字段映射之后）
        descriptor.afterMappingMethods.forEach { afterMethod ->
            callLifecycleMethod(methodBuilder, afterMethod, method, targetContext, usedNames)
        }

        if (method.returnType.kind != TypeKind.VOID) {
            methodBuilder.addStatement("return \$L", targetContext.varName)
        }

        return methodBuilder.build()
    }

    /**
     * 调用生命周期方法（@BeforeMapping 或 @AfterMapping）
     *
     * 这些方法必须是 default 方法（接口中）或非抽象方法（抽象类中），
     * 参数必须包含源对象和目标对象（使用 @MappingTarget 标记）
     */
    /**
     * 调用生命周期方法（@BeforeMapping 或 @AfterMapping）
     *
     * 参数匹配规则：
     * 1. @MappingTarget 参数：总是匹配（使用目标对象）
     * 2. 源对象参数：必须类型完全匹配或源类型可以赋值给参数类型
     * 3. 如果任何参数无法匹配，则跳过这个生命周期方法
     *
     * 重要：如果生命周期方法有源对象参数，且源对象类型不匹配，则不会调用该方法
     */
    private fun callLifecycleMethod(
        methodBuilder: MethodSpec.Builder,
        lifecycleMethod: ExecutableElement,
        mappingMethod: MapperMethodDescriptor,
        targetContext: TargetContext,
        usedNames: MutableSet<String>
    ) {
        // 构建方法参数列表
        val paramNames = mutableListOf<String>()

        // 检查所有参数是否都能匹配
        var allParamsMatched = true

        // 先检查是否有非 @MappingTarget 的源对象参数
        val hasSourceParam = lifecycleMethod.parameters.any { param ->
            param.getAnnotation(MappingTarget::class.java) == null
        }

        // 如果有源对象参数，需要检查类型匹配
        if (hasSourceParam) {
            lifecycleMethod.parameters.forEach { param ->
                val paramName = param.simpleName.toString()
                val paramType = param.asType()

                // 检查是否是 @MappingTarget 参数
                val isMappingTarget = param.getAnnotation(MappingTarget::class.java) != null

                if (isMappingTarget) {
                    // @MappingTarget 参数总是匹配（使用目标对象）
                    paramNames.add(targetContext.varName)
                } else {
                    // 源对象参数：必须严格类型匹配
                    // 1. 先尝试从映射方法的参数中精确类型匹配（只检查类型，不检查名称）
                    val matchingParam = mappingMethod.parameters.firstOrNull {
                        typeUtils.isSameType(it.type, paramType)
                    }

                    if (matchingParam != null) {
                        // 找到精确类型匹配的参数
                        paramNames.add(matchingParam.name)
                    } else {
                        // 2. 如果没有精确类型匹配，尝试使用主要源参数
                        // 但必须检查类型兼容性：源类型必须可以赋值给参数类型
                        val primarySource = mappingMethod.primarySource
                        if (primarySource != null) {
                            // 检查类型兼容性：primarySource.type 必须可以赋值给 paramType
                            // 例如：如果 paramType 是 OrderDto，primarySource.type 也必须是 OrderDto 或其子类
                            // 注意：isAssignable(A, B) 检查 A 是否可以赋值给 B
                            // 所以如果 primarySource.type 是 OrderDto，paramType 也是 OrderDto，返回 true
                            // 如果 primarySource.type 是 OrderDto 的子类，paramType 是 OrderDto，也返回 true（子类可以赋值给父类）
                            // 如果 primarySource.type 是 OrderBean，paramType 是 OrderDto，返回 false（不兼容类型）

                            // 严格检查：必须类型相同或者是子类关系
                            // 注意：isAssignable(A, B) 检查 A 是否可以赋值给 B
                            // 如果 primarySource.type 是 TickerRealtimeV2，paramType 是 OptionOrderBean
                            // isAssignable(TickerRealtimeV2, OptionOrderBean) 应该返回 false
                            val isSameType = typeUtils.isSameType(primarySource.type, paramType)
                            val isAssignable = if (!isSameType) {
                                // 只有当类型不同时才检查 isAssignable
                                // 如果 primarySource.type 是 paramType 的子类，返回 true
                                // 例如：如果 primarySource.type 是 OrderDtoSubclass，paramType 是 OrderDto，返回 true
                                // 但如果 primarySource.type 是 TickerRealtimeV2，paramType 是 OptionOrderBean，返回 false
                                typeUtils.isAssignable(primarySource.type, paramType)
                            } else {
                                true  // 类型相同，已经匹配
                            }

                            if (isSameType || isAssignable) {
                                paramNames.add(primarySource.name)
                            } else {
                                // 类型不兼容，无法匹配
                                allParamsMatched = false
                                return@forEach  // 跳出 forEach 循环
                            }
                        } else {
                            // 没有主要源参数，且生命周期方法需要源对象参数，无法匹配
                            allParamsMatched = false
                            return@forEach
                        }
                    }
                }
            }
        } else {
            // 如果只有 @MappingTarget 参数，所有参数都匹配
            lifecycleMethod.parameters.forEach { param ->
                if (param.getAnnotation(MappingTarget::class.java) != null) {
                    paramNames.add(targetContext.varName)
                }
            }
        }

        // 只有当所有参数都能匹配时，才调用生命周期方法
        if (!allParamsMatched) {
            // 参数不匹配，跳过这个生命周期方法
            return
        }

        // 调用静态生命周期方法
        val methodName = lifecycleMethod.simpleName.toString()
        val paramList = paramNames.joinToString(", ")
        val className = ClassName.get(descriptor.packageName, descriptor.implementationName)
        methodBuilder.addStatement("\$T.\$L(\$L)", className, methodName, paramList)
    }

    /**
     * 生成字段映射的详细注释，列出所有映射情况和未映射的字段
     */
    private fun generateMappingComment(method: MapperMethodDescriptor): String {
        val primarySource = method.primarySource ?: return ""
        val targetType = if (method.mappingTarget != null) method.mappingTarget.type else method.returnType
        val targetElement = propertyResolver.asTypeElement(targetType) ?: return ""

        val sourceReadable = propertyResolver.readableProperties(primarySource.typeElement)
        val targetWritable = propertyResolver.writeableProperties(targetElement)
        val targetFields = propertyResolver.writableFields(targetElement)

        // 收集所有源字段（getter + public field）
        val allSourceProperties = mutableSetOf<String>()
        allSourceProperties.addAll(sourceReadable.keys)
        // 查找源对象的所有 public 字段
        primarySource.typeElement?.let { sourceElement ->
            processingEnv.elementUtils.getAllMembers(sourceElement)
                .filter { it.kind == javax.lang.model.element.ElementKind.FIELD }
                .map { it as javax.lang.model.element.VariableElement }
                .filter {
                    !it.modifiers.contains(javax.lang.model.element.Modifier.STATIC) &&
                            !it.modifiers.contains(javax.lang.model.element.Modifier.PRIVATE)
                }
                .forEach { field ->
                    allSourceProperties.add(field.simpleName.toString())
                }
        }
        // 收集所有目标字段（setter + field）
        val allTargetProperties = (targetWritable.keys + targetFields.keys).toSet()

        // 收集已映射的字段
        val mappedTargets = mutableSetOf<String>()
        val mappedSources = mutableSetOf<String>()
        val explicitMappings = mutableListOf<Pair<String, String>>() // (source, target)
        val autoMapped = mutableListOf<String>() // target names

        // 处理显式映射
        method.resolvedMappings.forEach { spec ->
            if (!spec.ignore) {
                val targetName = spec.target.split('.').first()
                mappedTargets += targetName
                if (!spec.source.isNullOrBlank()) {
                    val sourceName = spec.source.split('.').first()
                    mappedSources += sourceName
                    explicitMappings += Pair(sourceName, targetName)
                } else {
                    // 隐式映射（同名）
                    mappedSources += targetName
                    autoMapped += targetName
                }
            }
        }

        // 收集自动映射的字段（同名且类型兼容）
        val autoTargets = allTargetProperties - mappedTargets
        autoTargets.forEach { targetProp ->
            if (allSourceProperties.contains(targetProp)) {
                // 获取源字段类型（优先使用 getter，否则查找 field）
                val sourceType = sourceReadable[targetProp]?.returnType ?:
                propertyResolver.findField(primarySource.typeElement, targetProp)?.asType()
                val targetType = targetWritable[targetProp]?.parameters?.first()?.asType() ?:
                targetFields[targetProp]?.asType()
                if (sourceType != null && targetType != null && isAssignable(sourceType, targetType)) {
                    autoMapped += targetProp
                    mappedTargets += targetProp
                    mappedSources += targetProp
                }
            }
        }

        // 收集未映射的源字段
        val unmappedSources = allSourceProperties - mappedSources

        // 收集未映射的目标字段
        val unmappedTargets = allTargetProperties - mappedTargets

        // 收集类型不匹配的字段（源有但类型不兼容）
        val typeMismatched = mutableListOf<Pair<String, String>>() // (property, typeInfo)
        autoTargets.forEach { targetProp ->
            if (allSourceProperties.contains(targetProp)) {
                val sourceType = sourceReadable[targetProp]?.returnType ?:
                propertyResolver.findField(primarySource.typeElement, targetProp)?.asType()
                val targetType = targetWritable[targetProp]?.parameters?.first()?.asType() ?:
                targetFields[targetProp]?.asType()
                if (sourceType != null && targetType != null && !isAssignable(sourceType, targetType)) {
                    typeMismatched += Pair(targetProp, getTypeName(sourceType) + " -> " + getTypeName(targetType))
                }
            }
        }

        // 构建注释
        val comment = StringBuilder()
        comment.append("字段映射详情：\n")
        comment.append("源对象：${primarySource.typeElement?.simpleName ?: "Unknown"}\n")
        comment.append("目标对象：${targetElement.simpleName}\n\n")

        if (explicitMappings.isNotEmpty()) {
            comment.append("显式映射（不同名）：\n")
            explicitMappings.forEach { (source, target) ->
                comment.append("  - $source -> $target\n")
            }
            comment.append("\n")
        }

        if (autoMapped.isNotEmpty()) {
            comment.append("自动映射（同名）：\n")
            autoMapped.forEach { prop ->
                comment.append("  - $prop\n")
            }
            comment.append("\n")
        }

        if (unmappedSources.isNotEmpty()) {
            comment.append("未映射的源字段（源对象有但目标对象没有对应字段）：\n")
            unmappedSources.sorted().forEach { prop ->
                val sourceType = sourceReadable[prop]?.returnType ?:
                propertyResolver.findField(primarySource.typeElement, prop)?.asType()
                comment.append("  - $prop (${getTypeName(sourceType)})\n")
            }
            comment.append("\n")
        }

        if (unmappedTargets.isNotEmpty()) {
            comment.append("未映射的目标字段（目标对象有但源对象没有对应字段）：\n")
            unmappedTargets.sorted().forEach { prop ->
                val targetType = targetWritable[prop]?.parameters?.first()?.asType() ?: targetFields[prop]?.asType()
                comment.append("  - $prop (${getTypeName(targetType)})\n")
            }
            comment.append("\n")
        }

        if (typeMismatched.isNotEmpty()) {
            comment.append("类型不匹配的字段（需要特殊处理或添加映射方法）：\n")
            typeMismatched.forEach { (prop, typeInfo) ->
                comment.append("  - $prop: $typeInfo\n")
            }
            comment.append("\n")
        }

        return comment.toString()
    }

    private fun resolveTargetContext(
        method: MapperMethodDescriptor,
        usedNames: MutableSet<String>
    ): TargetContext? {
        val isUpdate = method.mappingTarget != null
        val targetType = if (isUpdate) method.mappingTarget!!.type else method.returnType
        val targetElement = propertyResolver.asTypeElement(targetType)

        if (targetElement == null) {
            error(method.element, "Cannot resolve target type for method ${method.name}")
            return null
        }

        val varName = if (isUpdate) {
            method.mappingTarget!!.name
        } else {
            generateUniqueName("target", usedNames)
        }

        if (!isUpdate) {
            usedNames += varName
        }

        return TargetContext(
            type = targetType,
            typeElement = targetElement,
            varName = varName,
            isUpdate = isUpdate
        )
    }

    private fun collectAssignments(
        method: MapperMethodDescriptor,
        targetContext: TargetContext
    ): List<Assignment> {
        val assignments = mutableListOf<Assignment>()
        val setterMap = propertyResolver.writeableProperties(targetContext.typeElement)
        val fieldMap = propertyResolver.writableFields(targetContext.typeElement)
        val handledTargets = mutableSetOf<String>()

        method.resolvedMappings.forEach { spec ->
            handledTargets += spec.target.split('.').first() // 只标记第一层，嵌套对象需要特殊处理
            if (spec.ignore) {
                return@forEach
            }
            // 检查是否是嵌套路径（如 "address.city"）
            val targetPath = spec.target.split('.').filter { it.isNotBlank() }
            if (targetPath.size > 1) {
                // 嵌套对象映射
                createNestedAssignment(method, targetContext, spec, targetPath)?.let { assignments += it }
            } else {
                // 单层映射
                val setter = setterMap[spec.target]
                val field = fieldMap[spec.target]
                when {
                    setter != null -> {
                        createAssignmentFromSpec(method, setter, spec)?.let { assignments += it }
                    }
                    field != null -> {
                        createFieldAssignmentFromSpec(method, field, spec)?.let { assignments += it }
                    }
                    else -> {
                        error(method.element, "No setter or writable field found on ${targetContext.typeElement.simpleName} for target '${spec.target}'.")
                    }
                }
            }
        }

        // 合并setter和field的keys
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
                            // 检查是否是类型不匹配（source有属性但类型不匹配）
                            checkAndReportTypeMismatch(method, primarySource, property, setter.parameters.first().asType())
                        }
                    }
                    field != null -> {
                        val assignment = createAutoFieldAssignment(method, field, primarySource, property)
                        if (assignment != null) {
                            assignments += assignment
                        } else {
                            // 检查是否是类型不匹配（source有属性但类型不匹配）
                            checkAndReportTypeMismatch(method, primarySource, property, field.asType())
                        }
                    }
                }
            }
        } else {
            // 如果没有自动映射的目标，且也没有显式映射，给出提示
            if (autoTargets.isEmpty() && method.resolvedMappings.isEmpty() && primarySource != null) {
                // 检查是否有任何可映射的字段
                val sourceReadable = propertyResolver.readableProperties(primarySource.typeElement)
                val hasAnyMappable = allWritableTargets.any { targetProp ->
                    sourceReadable.containsKey(targetProp)
                }
                if (!hasAnyMappable) {
                    error(method.element, "No mappings found. Please add @Mapping annotations or ensure source and target have matching property names with getter/setter methods or public fields.")
                }
            } else if (primarySource == null && method.resolvedMappings.isEmpty()) {
                error(method.element, "Cannot auto-map: No source parameter found.")
            }
        }
        return assignments
    }

    private fun createAssignmentFromSpec(
        method: MapperMethodDescriptor,
        setter: ExecutableElement,
        spec: MappingSpec
    ): PropertyAssignment? {
        val expression = when {
            // 优先处理 expression（自定义业务逻辑）
            !spec.expression.isNullOrBlank() -> {
                // 处理 expression，移除 "java(...)" 包装（如果存在）
                val expr = spec.expression.trim()
                val processedExpr = if (expr.startsWith("java(") && expr.endsWith(")")) {
                    expr.substring(5, expr.length - 1)  // 移除 "java(" 和 ")"
                } else {
                    expr
                }
                ResolvedExpression(processedExpr, null)  // expression 的类型由编译器推断
            }
            !spec.constant.isNullOrBlank() -> ResolvedExpression(spec.constant, null)
            !spec.source.isNullOrBlank() -> resolveSourceExpression(method, spec.source)
            else -> resolveImplicitExpression(method, spec.target)
        }
        if (expression == null) {
            error(method.element, "Unable to resolve source for mapping target '${spec.target}'.")
            return null
        }
        val targetType = setter.parameters.first().asType()

        // 如果是 expression（自定义表达式），直接使用，不进行类型检查
        // 因为 expression 的类型由编译器推断，我们在编译时无法确定
        if (!spec.expression.isNullOrBlank()) {
            // expression 直接使用，不进行类型转换
            return PropertyAssignment(setter, expression.expression, targetType)
        }

        // 如果类型不匹配，尝试查找映射方法
        if (!isAssignable(expression.type, targetType)) {
            // 1. 检查是否是集合类型，查找元素类型的映射方法
            if (isCollectionType(expression.type) && isCollectionType(targetType)) {
                val sourceElementType = getCollectionElementType(expression.type)
                val targetElementType = getCollectionElementType(targetType)
                if (sourceElementType != null && targetElementType != null) {
                    if (!isAssignable(sourceElementType, targetElementType)) {
                        // 元素类型不匹配，查找元素类型的映射方法
                        val elementMapperMethod = findElementMapperMethod(sourceElementType, targetElementType)
                        if (elementMapperMethod != null) {
                            // 生成集合映射表达式，使用元素映射方法
                            val collectionMappingExpression = generateCollectionMappingExpression(
                                expression.expression,
                                sourceElementType,
                                targetElementType,
                                elementMapperMethod
                            )
                            return PropertyAssignment(setter, collectionMappingExpression, targetType)
                        } else {
                            error(method.element,
                                "Type mismatch for property '${spec.target}'. " +
                                        "Source type: ${expression.type}, Target type: $targetType. " +
                                        "Please add a mapping method for element types: " +
                                        "${getTypeName(sourceElementType)} -> ${getTypeName(targetElementType)}")
                            return null
                        }
                    } else {
                        // 元素类型兼容，但集合类型可能不兼容（如 List vs ArrayList）
                        // 生成集合转换表达式
                        val collectionConversionExpression = generateCollectionConversionExpression(
                            expression.expression,
                            expression.type,
                            targetType
                        )
                        if (collectionConversionExpression != null) {
                            return PropertyAssignment(setter, collectionConversionExpression, targetType)
                        }
                    }
                }
            }

            // 2. 检查是否是普通类型，查找直接类型的映射方法
            val directMapperMethod = findElementMapperMethod(expression.type, targetType)
            if (directMapperMethod != null) {
                // 生成直接映射表达式，使用静态方法调用（类名.方法名），添加 null 检查
                // 在同一个类中，直接使用类名即可
                val className = descriptor.implementationName
                val mappingExpression = "${expression.expression} == null ? null : $className.${directMapperMethod.name}(${expression.expression})"
                return PropertyAssignment(setter, mappingExpression, targetType)
            }

            // 如果找不到映射方法，报错
            error(method.element,
                "Type mismatch for property '${spec.target}'. " +
                        "Source type: ${expression.type}, Target type: $targetType. " +
                        "Please add a mapping method: ${getTypeName(expression.type)} -> ${getTypeName(targetType)}")
            return null
        }

        // 类型兼容，但如果是集合类型且具体类型不同，也需要转换
        if (isCollectionType(expression.type) && isCollectionType(targetType)) {
            val collectionConversionExpression = generateCollectionConversionExpression(
                expression.expression,
                expression.type,
                targetType
            )
            if (collectionConversionExpression != null) {
                return PropertyAssignment(setter, collectionConversionExpression, targetType)
            }
        }

        return PropertyAssignment(setter, expression.expression, expression.type)
    }

    private fun createAutoAssignment(
        method: MapperMethodDescriptor,
        setter: ExecutableElement,
        sourceParam: ParameterDescriptor,
        property: String
    ): PropertyAssignment? {
        val expression = resolveImplicitExpression(method, property, sourceParam)
        if (expression == null) {
            return null
        }
        val targetType = setter.parameters.first().asType()

        // 如果类型不匹配，尝试查找映射方法
        if (!isAssignable(expression.type, targetType)) {
            // 1. 检查是否是集合类型，查找元素类型的映射方法
            if (isCollectionType(expression.type) && isCollectionType(targetType)) {
                val sourceElementType = getCollectionElementType(expression.type)
                val targetElementType = getCollectionElementType(targetType)
                if (sourceElementType != null && targetElementType != null) {
                    if (!isAssignable(sourceElementType, targetElementType)) {
                        // 元素类型不匹配，查找元素类型的映射方法
                        val elementMapperMethod = findElementMapperMethod(sourceElementType, targetElementType)
                        if (elementMapperMethod != null) {
                            // 生成集合映射表达式，使用元素映射方法
                            val collectionMappingExpression = generateCollectionMappingExpression(
                                expression.expression,
                                sourceElementType,
                                targetElementType,
                                elementMapperMethod
                            )
                            return PropertyAssignment(setter, collectionMappingExpression, targetType)
                        }
                    } else {
                        // 元素类型兼容，但集合类型可能不兼容（如 List vs ArrayList）
                        // 生成集合转换表达式
                        val collectionConversionExpression = generateCollectionConversionExpression(
                            expression.expression,
                            expression.type,
                            targetType
                        )
                        if (collectionConversionExpression != null) {
                            return PropertyAssignment(setter, collectionConversionExpression, targetType)
                        }
                    }
                }
            }

            // 2. 检查是否是普通类型，查找直接类型的映射方法
            val directMapperMethod = findElementMapperMethod(expression.type, targetType)
            if (directMapperMethod != null) {
                // 生成直接映射表达式，使用静态方法调用（类名.方法名），添加 null 检查
                // 在同一个类中，直接使用类名即可
                val className = descriptor.implementationName
                val mappingExpression = "${expression.expression} == null ? null : $className.${directMapperMethod.name}(${expression.expression})"
                return PropertyAssignment(setter, mappingExpression, targetType)
            }

            // 如果找不到映射方法，返回 null（会在 checkAndReportTypeMismatch 中处理）
            return null
        }

        // 类型兼容，但如果是集合类型且具体类型不同，也需要转换
        if (isCollectionType(expression.type) && isCollectionType(targetType)) {
            val collectionConversionExpression = generateCollectionConversionExpression(
                expression.expression,
                expression.type,
                targetType
            )
            if (collectionConversionExpression != null) {
                return PropertyAssignment(setter, collectionConversionExpression, targetType)
            }
        }

        return PropertyAssignment(setter, expression.expression, expression.type)
    }

    private fun createAutoFieldAssignment(
        method: MapperMethodDescriptor,
        field: javax.lang.model.element.VariableElement,
        sourceParam: ParameterDescriptor,
        property: String
    ): FieldAssignment? {
        val expression = resolveImplicitExpression(method, property, sourceParam)
        if (expression == null) {
            return null
        }
        val targetType = field.asType()

        // 如果类型不匹配，尝试查找映射方法
        if (!isAssignable(expression.type, targetType)) {
            // 1. 检查是否是集合类型，查找元素类型的映射方法
            if (isCollectionType(expression.type) && isCollectionType(targetType)) {
                val sourceElementType = getCollectionElementType(expression.type)
                val targetElementType = getCollectionElementType(targetType)
                if (sourceElementType != null && targetElementType != null) {
                    if (!isAssignable(sourceElementType, targetElementType)) {
                        // 元素类型不匹配，查找元素类型的映射方法
                        val elementMapperMethod = findElementMapperMethod(sourceElementType, targetElementType)
                        if (elementMapperMethod != null) {
                            // 生成集合映射表达式，使用元素映射方法
                            val collectionMappingExpression = generateCollectionMappingExpression(
                                expression.expression,
                                sourceElementType,
                                targetElementType,
                                elementMapperMethod
                            )
                            return FieldAssignment(field, collectionMappingExpression, targetType)
                        }
                    } else {
                        // 元素类型兼容，但集合类型可能不兼容（如 List vs ArrayList）
                        // 生成集合转换表达式
                        val collectionConversionExpression = generateCollectionConversionExpression(
                            expression.expression,
                            expression.type,
                            targetType
                        )
                        if (collectionConversionExpression != null) {
                            return FieldAssignment(field, collectionConversionExpression, targetType)
                        }
                    }
                }
            }

            // 2. 检查是否是普通类型，查找直接类型的映射方法
            val directMapperMethod = findElementMapperMethod(expression.type, targetType)
            if (directMapperMethod != null) {
                // 生成直接映射表达式，使用静态方法调用（类名.方法名），添加 null 检查
                // 在同一个类中，直接使用类名即可
                val className = descriptor.implementationName
                val mappingExpression = "${expression.expression} == null ? null : $className.${directMapperMethod.name}(${expression.expression})"
                return FieldAssignment(field, mappingExpression, targetType)
            }

            // 如果找不到映射方法，返回 null（会在 checkAndReportTypeMismatch 中处理）
            return null
        }

        // 类型兼容，但如果是集合类型且具体类型不同，也需要转换
        if (isCollectionType(expression.type) && isCollectionType(targetType)) {
            val collectionConversionExpression = generateCollectionConversionExpression(
                expression.expression,
                expression.type,
                targetType
            )
            if (collectionConversionExpression != null) {
                return FieldAssignment(field, collectionConversionExpression, targetType)
            }
        }

        return FieldAssignment(field, expression.expression, expression.type)
    }

    private fun checkAndReportTypeMismatch(
        method: MapperMethodDescriptor,
        primarySource: ParameterDescriptor,
        property: String,
        targetType: TypeMirror
    ) {
        // 检查source是否有这个属性
        val sourceReadable = propertyResolver.readableProperties(primarySource.typeElement)
        val sourceField = propertyResolver.findField(primarySource.typeElement, property)

        // 如果source有该属性（通过getter或field），但类型不匹配
        val sourceType = when {
            sourceReadable.containsKey(property) -> sourceReadable[property]!!.returnType
            sourceField != null -> sourceField.asType()
            else -> null
        }

        if (sourceType != null) {
            // 检查是否是集合类型不匹配，如果是，尝试查找元素类型的映射方法
            if (isCollectionType(sourceType) && isCollectionType(targetType)) {
                val sourceElementType = getCollectionElementType(sourceType)
                val targetElementType = getCollectionElementType(targetType)
                if (sourceElementType != null && targetElementType != null) {
                    // 查找是否有元素类型的映射方法
                    val elementMapperMethod = findElementMapperMethod(sourceElementType, targetElementType)
                    if (elementMapperMethod != null) {
                        // 找到了元素类型的映射方法，可以自动映射集合
                        // 这个会在生成代码时处理，这里不报错
                        return
                    } else {
                        // 没有找到元素类型的映射方法，给出明确的错误提示
                        error(method.element,
                            "Auto-mapping failed: Type mismatch for property '$property'. " +
                                    "Source type: $sourceType, Target type: $targetType. " +
                                    "Please add a mapping method for element types: " +
                                    "${getTypeName(sourceElementType)} -> ${getTypeName(targetElementType)}")
                        return
                    }
                }
            }

            // 检查是否是普通类型不匹配，如果是，尝试查找直接类型的映射方法
            val directMapperMethod = findElementMapperMethod(sourceType, targetType)
            if (directMapperMethod != null) {
                // 找到了直接类型的映射方法，可以自动映射
                // 这个会在生成代码时处理，这里不报错
                return
            }

            // 非集合类型的不匹配，且没有找到映射方法，直接报错
            error(method.element,
                "Auto-mapping failed: Type mismatch for property '$property'. " +
                        "Source type: $sourceType, Target type: $targetType. " +
                        "Please add a mapping method: ${getTypeName(sourceType)} -> ${getTypeName(targetType)}")
        }
        // 如果source没有该属性，静默跳过，不报错
    }

    /**
     * 提取通配符类型的实际类型
     * 例如：? extends CommonOrderBean -> CommonOrderBean
     */
    private fun extractWildcardType(type: TypeMirror): TypeMirror {
        if (type is javax.lang.model.type.WildcardType) {
            // 如果是通配符类型，提取上界（extends）或下界（super）
            // 对于 ? extends T，返回 T
            // 对于 ? super T，返回 T
            val extendsBound = type.extendsBound
            val superBound = type.superBound
            return when {
                extendsBound != null && extendsBound.kind != javax.lang.model.type.TypeKind.NULL -> extendsBound
                superBound != null && superBound.kind != javax.lang.model.type.TypeKind.NULL -> superBound
                else -> type
            }
        }
        return type
    }

    /**
     * 检查两个类型是否兼容（用于匹配映射方法）
     * 支持：
     * - 通配符类型：? extends T 与 T 兼容
     * - 可空类型：T? 与 T 兼容（Kotlin 可空类型在 Java 中可能显示为相同类型）
     * - 类型相同或可赋值
     */
    private fun isCompatibleType(source: TypeMirror, target: TypeMirror): Boolean {
        // 提取通配符类型的实际类型
        val actualSource = extractWildcardType(source)
        val actualTarget = extractWildcardType(target)

        // 先尝试精确匹配
        if (typeUtils.isSameType(actualSource, actualTarget)) {
            return true
        }

        // 尝试类型赋值兼容性（source 可以赋值给 target）
        if (typeUtils.isAssignable(actualSource, actualTarget)) {
            return true
        }

        // 尝试反向赋值（target 可以赋值给 source，处理可空类型等情况）
        if (typeUtils.isAssignable(actualTarget, actualSource)) {
            return true
        }

        // 如果都是 DeclaredType，比较原始类型（忽略泛型参数和可空性）
        val sourceElement = typeUtils.asElement(actualSource) as? TypeElement
        val targetElement = typeUtils.asElement(actualTarget) as? TypeElement
        if (sourceElement != null && targetElement != null) {
            // 比较原始类型名称（忽略泛型和可空性）
            if (sourceElement.qualifiedName == targetElement.qualifiedName) {
                return true
            }
        }

        return false
    }

    /**
     * 查找 Mapper 接口中是否有元素类型的映射方法
     * 例如：如果源类型是 List<OptionOrderBean>，目标类型是 List<CommonOrderBean>
     * 则查找是否有 OptionOrderBean -> CommonOrderBean 的映射方法
     *
     * 支持通配符类型匹配：
     * - ? extends CommonOrderBean 可以匹配 CommonOrderBean
     * - CommonOrderBean? 可以匹配 CommonOrderBean
     */
    private fun findElementMapperMethod(sourceElementType: TypeMirror?, targetElementType: TypeMirror?): MapperMethodDescriptor? {
        if (sourceElementType == null || targetElementType == null) return null
        return descriptor.methods.firstOrNull { method ->
            // 检查方法是否只有一个参数（非 @MappingTarget），且参数类型匹配源元素类型
            val sourceParam = method.parameters.firstOrNull { !it.isMappingTarget }
            val hasMappingTarget = method.mappingTarget != null

            // 方法必须：
            // 1. 有且仅有一个源参数（非 @MappingTarget）
            // 2. 源参数类型匹配 sourceElementType（支持通配符类型和可空类型）
            // 3. 返回类型匹配 targetElementType（如果是更新方法，则检查 @MappingTarget 的类型）
            when {
                hasMappingTarget -> {
                    // 更新方法：检查 @MappingTarget 的类型是否匹配 targetElementType
                    val mappingTargetType = method.mappingTarget?.type
                    if (mappingTargetType == null || sourceParam == null) return@firstOrNull false

                    // 检查目标类型匹配（支持通配符）
                    val targetMatches = isCompatibleType(mappingTargetType, targetElementType)
                    // 检查源类型匹配（支持通配符）
                    val sourceMatches = isCompatibleType(sourceParam.type, sourceElementType)

                    targetMatches && sourceMatches
                }
                else -> {
                    // 创建方法：检查返回类型是否匹配 targetElementType
                    if (sourceParam == null || method.parameters.size != 1) return@firstOrNull false

                    // 检查返回类型匹配（支持通配符）
                    val returnMatches = isCompatibleType(method.returnType, targetElementType)
                    // 检查源参数类型匹配（支持通配符）
                    val sourceMatches = isCompatibleType(sourceParam.type, sourceElementType)

                    returnMatches && sourceMatches
                }
            }
        }
    }

    /**
     * 获取类型的可读名称（用于错误提示）
     */
    private fun getTypeName(type: TypeMirror?): String {
        if (type == null) return "null"
        return when (val element = typeUtils.asElement(type)) {
            is TypeElement -> element.qualifiedName.toString()
            else -> type.toString()
        }
    }

    /**
     * 生成集合映射表达式
     * 例如：如果源是 List<OptionOrderBean>，目标是 List<CommonOrderBean>
     * 且存在 toCommonOrderBean(OptionOrderBean) 方法
     * 则生成使用传统 for 循环的代码，避免使用 Stream API（兼容低版本 Android）
     *
     * 注意：这个方法返回的表达式需要在方法体中生成多行代码，不能直接作为表达式使用
     * 所以需要特殊处理，在调用处生成代码块
     */
    private fun generateCollectionMappingExpression(
        sourceExpression: String,
        sourceElementType: TypeMirror?,
        targetElementType: TypeMirror?,
        elementMapperMethod: MapperMethodDescriptor
    ): String {
        // 这个方法现在返回一个标记，实际代码生成在调用处处理
        // 返回格式：COLLECTION_MAPPING:sourceExpression:mapperMethodName
        val mapperMethodName = elementMapperMethod.name
        return "COLLECTION_MAPPING:$sourceExpression:$mapperMethodName"
    }

    /**
     * 生成集合映射的代码块（不使用 Stream API，兼容低版本 Android）
     * 例如：
     * if (sourceList == null) {
     *     target.orders = null;
     * } else {
     *     java.util.ArrayList<CommonOrderBean> tempList = new java.util.ArrayList<>();
     *     for (int i = 0; i < sourceList.size(); i++) {
     *         tempList.add(toCommonOrderBean(sourceList.get(i)));
     *     }
     *     target.orders = tempList;
     * }
     */
    private fun generateCollectionMappingCodeBlock(
        methodBuilder: MethodSpec.Builder,
        targetVarName: String,
        targetSetterName: String?,
        targetFieldName: String?,
        sourceExpression: String,
        mapperMethodName: String,
        targetType: TypeMirror,
        usedNames: MutableSet<String>
    ) {
        val tempListVarName = generateUniqueName("tempList", usedNames)
        usedNames += tempListVarName

        // 获取目标集合类型的类名（必须是具体实现类，不能是接口）
        val targetTypeName = getCollectionTypeName(targetType) ?: "java.util.ArrayList"
        // 获取目标集合的元素类型
        val targetElementType = getCollectionElementType(targetType)
        val targetElementTypeName = if (targetElementType != null) {
            TypeName.get(targetElementType)
        } else {
            TypeName.OBJECT
        }

        // 使用具体实现类（ArrayList）而不是接口（List）
        val concreteListType = ClassName.get("java.util", "ArrayList")

        methodBuilder.beginControlFlow("if (\$L == null)", sourceExpression)
        when {
            targetSetterName != null -> {
                methodBuilder.addStatement("\$L.\$L(null)", targetVarName, targetSetterName)
            }
            targetFieldName != null -> {
                methodBuilder.addStatement("\$L.\$L = null", targetVarName, targetFieldName)
            }
        }
        methodBuilder.nextControlFlow("else")
        // 生成：ArrayList<ElementType> tempList = new ArrayList<>();
        methodBuilder.addStatement("\$T<\$T> \$L = new \$T<>()", concreteListType, targetElementTypeName, tempListVarName, concreteListType)
        methodBuilder.beginControlFlow("for (int i = 0; i < \$L.size(); i++)", sourceExpression)
        // 使用静态方法调用（类名.方法名）
        val className = ClassName.get(descriptor.packageName, descriptor.implementationName)
        methodBuilder.addStatement("\$L.add(\$T.\$L(\$L.get(i)))", tempListVarName, className, mapperMethodName, sourceExpression)
        methodBuilder.endControlFlow()
        when {
            targetSetterName != null -> {
                methodBuilder.addStatement("\$L.\$L(\$L)", targetVarName, targetSetterName, tempListVarName)
            }
            targetFieldName != null -> {
                methodBuilder.addStatement("\$L.\$L = \$L", targetVarName, targetFieldName, tempListVarName)
            }
        }
        methodBuilder.endControlFlow()
    }

    /**
     * 生成集合类型转换表达式
     * 例如：如果源是 List<StConditionResponse>，目标是 ArrayList<StConditionResponse>
     * 则生成：sourceList == null ? null : new ArrayList<>(sourceList)
     *
     * @param sourceExpression 源表达式
     * @param sourceType 源类型
     * @param targetType 目标类型
     * @return 转换表达式，如果不需要转换则返回 null
     */
    private fun generateCollectionConversionExpression(
        sourceExpression: String,
        sourceType: TypeMirror?,
        targetType: TypeMirror?
    ): String? {
        if (sourceType == null || targetType == null) return null

        // 如果源类型和目标类型相同，不需要转换
        if (typeUtils.isSameType(sourceType, targetType)) {
            return null
        }

        // 如果类型系统认为可以赋值，不需要转换
        if (typeUtils.isAssignable(sourceType, targetType)) {
            return null
        }

        // 获取目标集合类型的类名
        val targetTypeName = getCollectionTypeName(targetType) ?: return null

        // 生成：sourceList == null ? null : new TargetType<>(sourceList)
        return "$sourceExpression == null ? null : new $targetTypeName<>($sourceExpression)"
    }

    /**
     * 获取集合类型的类名
     * 例如：ArrayList<StConditionResponse> -> "java.util.ArrayList"
     */
    private fun getCollectionTypeName(type: TypeMirror?): String? {
        if (type == null) return null
        return when (val element = typeUtils.asElement(type)) {
            is TypeElement -> element.qualifiedName.toString()
            else -> {
                // 尝试从类型字符串中提取类名
                val typeString = type.toString()
                when {
                    typeString.startsWith("java.util.ArrayList") -> "java.util.ArrayList"
                    typeString.startsWith("java.util.LinkedList") -> "java.util.LinkedList"
                    typeString.startsWith("java.util.HashSet") -> "java.util.HashSet"
                    typeString.startsWith("java.util.LinkedHashSet") -> "java.util.LinkedHashSet"
                    typeString.startsWith("java.util.TreeSet") -> "java.util.TreeSet"
                    typeString.startsWith("java.util.Vector") -> "java.util.Vector"
                    typeString.startsWith("java.util.List") -> "java.util.ArrayList" // List 默认转换为 ArrayList
                    typeString.startsWith("java.util.Set") -> "java.util.HashSet" // Set 默认转换为 HashSet
                    typeString.startsWith("java.util.Collection") -> "java.util.ArrayList" // Collection 默认转换为 ArrayList
                    else -> null
                }
            }
        }
    }

    private fun createFieldAssignmentFromSpec(
        method: MapperMethodDescriptor,
        field: javax.lang.model.element.VariableElement,
        spec: MappingSpec
    ): FieldAssignment? {
        val expression = when {
            // 优先处理 expression（自定义业务逻辑）
            !spec.expression.isNullOrBlank() -> {
                // 处理 expression，移除 "java(...)" 包装（如果存在）
                val expr = spec.expression.trim()
                val processedExpr = if (expr.startsWith("java(") && expr.endsWith(")")) {
                    expr.substring(5, expr.length - 1)  // 移除 "java(" 和 ")"
                } else {
                    expr
                }
                ResolvedExpression(processedExpr, null)  // expression 的类型由编译器推断
            }
            !spec.constant.isNullOrBlank() -> ResolvedExpression(spec.constant, null)
            !spec.source.isNullOrBlank() -> resolveSourceExpression(method, spec.source)
            else -> resolveImplicitExpression(method, spec.target)
        }
        if (expression == null) {
            error(method.element, "Unable to resolve source for mapping target '${spec.target}'.")
            return null
        }
        val targetType = field.asType()

        // 如果是 expression（自定义表达式），直接使用，不进行类型检查
        // 因为 expression 的类型由编译器推断，我们在编译时无法确定
        if (!spec.expression.isNullOrBlank()) {
            // expression 直接使用，不进行类型转换
            return FieldAssignment(field, expression.expression, targetType)
        }

        // 如果类型不匹配，尝试查找映射方法
        if (!isAssignable(expression.type, targetType)) {
            // 1. 检查是否是集合类型，查找元素类型的映射方法
            if (isCollectionType(expression.type) && isCollectionType(targetType)) {
                val sourceElementType = getCollectionElementType(expression.type)
                val targetElementType = getCollectionElementType(targetType)
                if (sourceElementType != null && targetElementType != null) {
                    if (!isAssignable(sourceElementType, targetElementType)) {
                        // 元素类型不匹配，查找元素类型的映射方法
                        val elementMapperMethod = findElementMapperMethod(sourceElementType, targetElementType)
                        if (elementMapperMethod != null) {
                            // 生成集合映射表达式，使用元素映射方法
                            val collectionMappingExpression = generateCollectionMappingExpression(
                                expression.expression,
                                sourceElementType,
                                targetElementType,
                                elementMapperMethod
                            )
                            return FieldAssignment(field, collectionMappingExpression, targetType)
                        } else {
                            error(method.element,
                                "Type mismatch for property '${spec.target}'. " +
                                        "Source type: ${expression.type}, Target type: $targetType. " +
                                        "Please add a mapping method for element types: " +
                                        "${getTypeName(sourceElementType)} -> ${getTypeName(targetElementType)}")
                            return null
                        }
                    } else {
                        // 元素类型兼容，但集合类型可能不兼容（如 List vs ArrayList）
                        // 生成集合转换表达式
                        val collectionConversionExpression = generateCollectionConversionExpression(
                            expression.expression,
                            expression.type,
                            targetType
                        )
                        if (collectionConversionExpression != null) {
                            return FieldAssignment(field, collectionConversionExpression, targetType)
                        }
                    }
                }
            }

            // 2. 检查是否是普通类型，查找直接类型的映射方法
            val directMapperMethod = findElementMapperMethod(expression.type, targetType)
            if (directMapperMethod != null) {
                // 生成直接映射表达式，使用静态方法调用（类名.方法名），添加 null 检查
                // 在同一个类中，直接使用类名即可
                val className = descriptor.implementationName
                val mappingExpression = "${expression.expression} == null ? null : $className.${directMapperMethod.name}(${expression.expression})"
                return FieldAssignment(field, mappingExpression, targetType)
            }

            // 如果找不到映射方法，报错
            error(method.element,
                "Type mismatch for property '${spec.target}'. " +
                        "Source type: ${expression.type}, Target type: $targetType. " +
                        "Please add a mapping method: ${getTypeName(expression.type)} -> ${getTypeName(targetType)}")
            return null
        }

        // 类型兼容，但如果是集合类型且具体类型不同，也需要转换
        if (isCollectionType(expression.type) && isCollectionType(targetType)) {
            val collectionConversionExpression = generateCollectionConversionExpression(
                expression.expression,
                expression.type,
                targetType
            )
            if (collectionConversionExpression != null) {
                return FieldAssignment(field, collectionConversionExpression, targetType)
            }
        }

        return FieldAssignment(field, expression.expression, expression.type)
    }

    private fun resolveImplicitExpression(
        method: MapperMethodDescriptor,
        property: String,
        sourceParam: ParameterDescriptor? = null
    ): ResolvedExpression? {
        val param = sourceParam ?: method.primarySource ?: return null
        // 自动映射时，如果找不到属性应该静默返回null，不报错
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
        var currentElement: TypeElement? = parameter.typeElement
        var currentType: TypeMirror = parameter.type
        var expression = parameter.name

        path.forEach { segment ->
            if (currentElement == null) {
                // 如果silent=true（自动映射场景），静默返回null；否则报错（显式映射场景）
                if (!silent) {
                    error(method.element, "Cannot find type element for '$segment' on ${parameter.name}. Please add @Mapping annotation to specify the mapping.")
                }
                return null
            }

            val getters = propertyResolver.readableProperties(currentElement)
            val getter = getters[segment]
            if (getter != null) {
                // 使用getter方法
                expression += ".${getter.simpleName}()"
                currentType = getter.returnType
                currentElement = propertyResolver.asTypeElement(currentType)
            } else {
                // 尝试直接访问字段（Kotlin data class或public字段）
                val field = propertyResolver.findField(currentElement, segment)
                if (field != null) {
                    expression += ".${field.simpleName}"
                    currentType = field.asType()
                    currentElement = propertyResolver.asTypeElement(currentType)
                } else {
                    // 如果silent=true（自动映射场景），静默返回null；否则报错（显式映射场景）
                    if (!silent) {
                        val elementName = currentElement?.qualifiedName?.toString() ?: parameter.name
                        error(method.element, "Cannot find getter or field for '$segment' on $elementName. Please add @Mapping annotation to specify the mapping.")
                    }
                    return null
                }
            }
        }
        return ResolvedExpression(expression, currentType)
    }

    private fun isAssignable(source: TypeMirror?, target: TypeMirror): Boolean {
        if (source == null) return true

        // 基本类型检查
        if (typeUtils.isAssignable(source, target) || typeUtils.isSameType(source, target)) {
            return true
        }

        // 检查数组类型
        if (source.kind == javax.lang.model.type.TypeKind.ARRAY && target.kind == javax.lang.model.type.TypeKind.ARRAY) {
            val sourceArray = source as? javax.lang.model.type.ArrayType
            val targetArray = target as? javax.lang.model.type.ArrayType
            if (sourceArray != null && targetArray != null) {
                return isAssignable(sourceArray.componentType, targetArray.componentType)
            }
        }

        // 检查集合类型（List, Set等）
        if (isCollectionType(source) && isCollectionType(target)) {
            val sourceElementType = getCollectionElementType(source)
            val targetElementType = getCollectionElementType(target)
            if (sourceElementType != null && targetElementType != null) {
                return isAssignable(sourceElementType, targetElementType)
            }
            // 如果无法确定元素类型，检查集合类型本身是否兼容
            return typeUtils.isAssignable(source, target)
        }

        return false
    }

    private fun isCollectionType(type: TypeMirror?): Boolean {
        if (type == null) return false
        val typeString = type.toString()
        return typeString.startsWith("java.util.List") ||
                typeString.startsWith("java.util.Set") ||
                typeString.startsWith("java.util.Collection") ||
                typeString.startsWith("java.util.ArrayList") ||
                typeString.startsWith("java.util.LinkedList") ||
                typeString.startsWith("java.util.HashSet") ||
                typeString.startsWith("java.util.LinkedHashSet")
    }

    private fun getCollectionElementType(type: TypeMirror?): TypeMirror? {
        if (type == null) return null
        if (type is javax.lang.model.type.DeclaredType) {
            val typeArguments = type.typeArguments
            if (typeArguments.isNotEmpty()) {
                val elementType = typeArguments.first()
                // 如果是通配符类型（? extends T 或 ? super T），提取实际类型
                return extractWildcardType(elementType)
            }
        }
        return null
    }

    private fun createNestedAssignment(
        method: MapperMethodDescriptor,
        targetContext: TargetContext,
        spec: MappingSpec,
        targetPath: List<String>
    ): Assignment? {
        // 对于嵌套路径（如 "address.city"），我们需要：
        // 1. 获取或创建中间对象（address）
        // 2. 在中间对象上设置最终值（city）

        val rootProperty = targetPath.first()
        val nestedPath = targetPath.drop(1)

        // 查找根属性的setter或field
        val setterMap = propertyResolver.writeableProperties(targetContext.typeElement)
        val fieldMap = propertyResolver.writableFields(targetContext.typeElement)
        val rootSetter = setterMap[rootProperty]
        val rootField = fieldMap[rootProperty]

        if (rootSetter == null && rootField == null) {
            error(method.element, "Cannot find root property '$rootProperty' for nested target '${spec.target}'.")
            return null
        }

        // 解析源表达式
        val sourceExpression = when {
            !spec.constant.isNullOrBlank() -> ResolvedExpression(spec.constant, null)
            !spec.source.isNullOrBlank() -> resolveSourceExpression(method, spec.source)
            else -> {
                // 尝试从源对象中找到对应的嵌套路径
                val sourcePath = nestedPath.joinToString(".")
                resolveImplicitExpression(method, sourcePath)
            }
        }

        if (sourceExpression == null) {
            error(method.element, "Unable to resolve source for nested target '${spec.target}'.")
            return null
        }

        // 获取中间对象的类型
        val intermediateType = when {
            rootSetter != null -> rootSetter.parameters.first().asType()
            rootField != null -> rootField.asType()
            else -> return null
        }

        // 检查是否需要创建中间对象
        val intermediateElement = propertyResolver.asTypeElement(intermediateType)
        if (intermediateElement == null) {
            error(method.element, "Cannot resolve intermediate type for nested target '${spec.target}'.")
            return null
        }

        // 如果源类型和目标中间类型匹配，可以直接赋值
        if (isAssignable(sourceExpression.type, intermediateType)) {
            // 直接赋值给根属性
            return when {
                rootSetter != null -> PropertyAssignment(rootSetter, sourceExpression.expression, sourceExpression.type)
                rootField != null -> FieldAssignment(rootField, sourceExpression.expression, sourceExpression.type)
                else -> null
            }
        }

        // 对于真正的嵌套映射（如 source.address.city -> target.address.city），
        // 需要生成代码来：
        // 1. 获取或创建中间对象
        // 2. 设置嵌套属性
        // 这里我们创建一个特殊的嵌套赋值
        return NestedAssignment(
            rootProperty = rootProperty,
            rootSetter = rootSetter,
            rootField = rootField,
            nestedPath = nestedPath,
            sourceExpression = sourceExpression,
            intermediateType = intermediateType
        )
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

    private fun generateNestedAssignment(
        methodBuilder: MethodSpec.Builder,
        targetContext: TargetContext,
        assignment: NestedAssignment,
        usedNames: MutableSet<String>,
        method: MapperMethodDescriptor
    ) {
        // 生成嵌套对象访问代码
        // 例如：target.address.city = source.address.city
        // 需要：1. 获取或创建 target.address
        //       2. 设置 target.address.city

        val intermediateVarName = generateUniqueName("${assignment.rootProperty}Obj", usedNames)
        usedNames += intermediateVarName

        val intermediateTypeName = TypeName.get(assignment.intermediateType)
        val intermediateElement = propertyResolver.asTypeElement(assignment.intermediateType)

        // 获取中间对象（如果已存在）或创建新对象
        val getter = propertyResolver.readableProperties(targetContext.typeElement)[assignment.rootProperty]
        if (getter != null) {
            // 如果存在getter，先获取现有对象
            methodBuilder.addStatement("\$T \$L = \$L.\$L()", intermediateTypeName, intermediateVarName, targetContext.varName, getter.simpleName)
            methodBuilder.beginControlFlow("if (\$L == null)", intermediateVarName)
            methodBuilder.addStatement("\$L = new \$T()", intermediateVarName, intermediateTypeName)
            methodBuilder.endControlFlow()
        } else {
            // 直接创建新对象
            methodBuilder.addStatement("\$T \$L = new \$T()", intermediateTypeName, intermediateVarName, intermediateTypeName)
        }

        // 设置嵌套属性
        if (intermediateElement == null) {
            error(method.element, "Cannot resolve intermediate type for nested assignment '${assignment.rootProperty}'.")
            return
        }

        val nestedProperty = assignment.nestedPath.first()
        val nestedSetters = propertyResolver.writeableProperties(intermediateElement)
        val nestedFields = propertyResolver.writableFields(intermediateElement)
        val nestedSetter = nestedSetters[nestedProperty]
        val nestedField = nestedFields[nestedProperty]

        when {
            nestedSetter != null -> {
                methodBuilder.addStatement("\$L.\$L(\$L)", intermediateVarName, nestedSetter.simpleName, assignment.expression)
            }
            nestedField != null -> {
                methodBuilder.addStatement("\$L.\$L = \$L", intermediateVarName, nestedField.simpleName, assignment.expression)
            }
            else -> {
                error(method.element, "Cannot find setter or field '$nestedProperty' on intermediate type ${intermediateElement.simpleName}.")
            }
        }

        // 将中间对象设置回目标对象
        when {
            assignment.rootSetter != null -> {
                methodBuilder.addStatement("\$L.\$L(\$L)", targetContext.varName, assignment.rootSetter.simpleName, intermediateVarName)
            }
            assignment.rootField != null -> {
                methodBuilder.addStatement("\$L.\$L = \$L", targetContext.varName, assignment.rootField.simpleName, intermediateVarName)
            }
        }
    }

    /**
     * 检测是否是集合互转方法
     * 条件：
     * 1. 方法只有一个参数（非 @MappingTarget）
     * 2. 参数类型是集合类型
     * 3. 返回类型也是集合类型
     * 4. 没有 @MappingTarget 参数
     */
    private fun detectCollectionToCollectionMapping(method: MapperMethodDescriptor): Boolean {
        // 必须有 @MappingTarget 参数的方法不是集合互转方法
        if (method.mappingTarget != null) {
            return false
        }

        // 必须只有一个源参数
        val sourceParams = method.parameters.filter { !it.isMappingTarget }
        if (sourceParams.size != 1) {
            return false
        }

        val sourceType = sourceParams.first().type
        val returnType = method.returnType

        // 源类型和返回类型都必须是集合类型
        return isCollectionType(sourceType) && isCollectionType(returnType)
    }

    /**
     * 生成集合互转方法
     * 例如：List<CommonOrderBean> -> List<OptionOrderBean>
     */
    private fun generateCollectionToCollectionMethod(
        method: MapperMethodDescriptor,
        methodBuilder: MethodSpec.Builder,
        usedNames: MutableSet<String>
    ): MethodSpec {
        val primarySource = method.primarySource ?: run {
            error(method.element, "Collection mapping method must have a source parameter.")
            methodBuilder.addStatement("return null")
            return methodBuilder.build()
        }

        val sourceType = primarySource.type
        val returnType = method.returnType

        // 获取集合元素类型
        val sourceElementType = getCollectionElementType(sourceType)
        val targetElementType = getCollectionElementType(returnType)

        if (sourceElementType == null || targetElementType == null) {
            error(method.element, "Cannot determine element types for collection mapping method ${method.name}.")
            methodBuilder.addStatement("return null")
            return methodBuilder.build()
        }

        // 检查元素类型是否相同
        val isElementTypeSame = typeUtils.isSameType(sourceElementType, targetElementType)

        // 对 source 参数进行空值检查
        methodBuilder.beginControlFlow("if (\$L == null)", primarySource.name)
        methodBuilder.addStatement("return null")
        methodBuilder.endControlFlow()

        // 获取目标集合类型的类名
        val targetCollectionTypeName = getCollectionTypeName(returnType) ?: "java.util.ArrayList"
        val targetElementTypeName = TypeName.get(targetElementType)
        val concreteListType = ClassName.get("java.util", "ArrayList")

        // 生成临时列表变量名
        val tempListVarName = generateUniqueName("tempList", usedNames)
        usedNames += tempListVarName

        // 创建目标列表
        methodBuilder.addStatement(
            "\$T<\$T> \$L = new \$T<>()",
            concreteListType,
            targetElementTypeName,
            tempListVarName,
            concreteListType
        )

        // 生成循环代码
        methodBuilder.beginControlFlow("for (int i = 0; i < \$L.size(); i++)", primarySource.name)

        if (isElementTypeSame) {
            // 元素类型相同，直接添加
            methodBuilder.addStatement("\$L.add(\$L.get(i))", tempListVarName, primarySource.name)
        } else {
            // 元素类型不同，需要查找映射方法
            val elementMapperMethod = findElementMapperMethod(sourceElementType, targetElementType)
            if (elementMapperMethod != null) {
                // 找到了元素映射方法，调用它
                val className = descriptor.implementationName
                methodBuilder.addStatement(
                    "\$L.add(\$T.\$L(\$L.get(i)))",
                    tempListVarName,
                    ClassName.get(descriptor.packageName, className),
                    elementMapperMethod.name,
                    primarySource.name
                )
            } else {
                // 找不到映射方法，尝试类型转换（可能失败）
                error(
                    method.element,
                    "Cannot find mapping method for element types: ${sourceElementType} -> ${targetElementType}. " +
                            "Please add a mapping method like: ${targetElementType} map(${sourceElementType} source)"
                )
                methodBuilder.addStatement("// TODO: Add mapping method for element types")
                methodBuilder.addStatement("\$L.add(null)", tempListVarName)
            }
        }

        methodBuilder.endControlFlow()

        // 返回结果
        // 如果返回类型是接口（如 List），ArrayList 可以直接返回（ArrayList 实现了 List）
        // 如果返回类型是具体类型，需要检查是否需要转换
        val returnTypeString = returnType.toString()
        if (returnTypeString.startsWith("java.util.List") ||
            returnTypeString.startsWith("java.util.Collection")) {
            // 返回类型是 List 或 Collection 接口，ArrayList 可以直接返回
            methodBuilder.addStatement("return \$L", tempListVarName)
        } else if (targetCollectionTypeName == returnTypeString.substringBefore("<")) {
            // 返回类型是具体类型，且与生成的类型相同，直接返回
            methodBuilder.addStatement("return \$L", tempListVarName)
        } else {
            // 需要转换为目标类型（通过构造函数）
            val targetTypeClass = ClassName.bestGuess(targetCollectionTypeName)
            methodBuilder.addStatement("return new \$T<>(\$L)", targetTypeClass, tempListVarName)
        }

        return methodBuilder.build()
    }
}

private data class TargetContext(
    val type: TypeMirror,
    val typeElement: TypeElement,
    val varName: String,
    val isUpdate: Boolean
)

private sealed class Assignment {
    abstract val expression: String
    abstract val expressionType: TypeMirror?
}

private data class PropertyAssignment(
    val setter: ExecutableElement,
    override val expression: String,
    override val expressionType: TypeMirror?
) : Assignment()

private data class FieldAssignment(
    val field: javax.lang.model.element.VariableElement,
    override val expression: String,
    override val expressionType: TypeMirror?
) : Assignment()

private data class NestedAssignment(
    val rootProperty: String,
    val rootSetter: ExecutableElement?,
    val rootField: javax.lang.model.element.VariableElement?,
    val nestedPath: List<String>,
    val sourceExpression: ResolvedExpression,
    val intermediateType: TypeMirror
) : Assignment() {
    override val expression: String = sourceExpression.expression
    override val expressionType: TypeMirror? = sourceExpression.type
}


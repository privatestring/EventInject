package launcher.mapper

import launcher.codegeneration.MapperGeneration
import launcher.error.error
import launcher.error.messanger
import mapper.AfterMapping
import mapper.BeforeMapping
import mapper.InheritConfiguration
import mapper.Mapper
import mapper.Mapping
import mapper.MappingConfig
import mapper.MappingIgnore
import mapper.MappingTarget
import javax.annotation.processing.Filer
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement

object MapperUtils{
    /**
     * 处理单个 Mapper 接口/抽象类，生成对应的实现类
     *
     * 主要流程：
     * 1. 验证 Mapper 元素类型（必须是接口或抽象类）
     * 2. 构建 MapperDescriptor（包含所有映射方法的描述信息）
     * 3. 使用 MapperGeneration 生成 Java 代码
     * 4. 将生成的代码写入文件系统
     *
     * @param mapperElement 被 @Mapper 注解的接口或抽象类元素
     */
    fun handleMapper(mapperElement: TypeElement,processingEnv: ProcessingEnvironment,propertyResolver: PropertyResolver,filer: Filer) {
        // 验证：@Mapper 只能应用于接口或抽象类
        if (!mapperElement.mapperKind.isInterface && !mapperElement.modifiers.contains(javax.lang.model.element.Modifier.ABSTRACT)) {
            error(mapperElement, "@Mapper can only be applied to interface or abstract class.")
            return
        }

        // 构建 Mapper 描述符，包含所有映射方法的元数据
        val descriptor = buildMapperDescriptor(mapperElement,processingEnv,propertyResolver) ?: return

        // 使用 MapperGeneration 生成实现类代码，并写入文件
        // 例如：UserMapper -> UserMapperImpl.java
        MapperGeneration(processingEnv, propertyResolver, descriptor)
            .brewJava()  // 生成 Java 代码
            .writeTo(filer)  // 写入文件系统
    }

    /**
     * 构建 Mapper 描述符，包含 Mapper 接口的所有元数据信息
     *
     * 主要工作：
     * 1. 解析 @Mapper 注解，获取实现类后缀（默认 "Impl"）
     * 2. 确定实现类的包名和类名（如 com.example.UserMapper -> com.example.UserMapperImpl）
     * 3. 扫描所有抽象方法，构建方法描述符
     * 4. 解析继承的映射配置（@InheritConfiguration）
     *
     * @param mapperElement Mapper 接口元素
     * @return MapperDescriptor 如果构建成功，否则返回 null
     */
    private fun buildMapperDescriptor(
        mapperElement: TypeElement,
        processingEnv: ProcessingEnvironment,
        propertyResolver: PropertyResolver
    ): MapperDescriptor? {
        // 获取 @Mapper 注解，读取实现类后缀（默认 "Impl"）
        val mapperAnno = mapperElement.getAnnotation(Mapper::class.java)
        val suffix = mapperAnno?.implementationSuffix ?: "Impl"

        // 获取 @MappingConfig 注解，读取配置参数
        val mappingConfig = mapperElement.getAnnotation(MappingConfig::class.java)
        val needNullCheck = mappingConfig?.isNeedNullCheck ?: false

        // 确定实现类的包名（与 Mapper 接口在同一包下）
        val packageName = processingEnv.elementUtils.getPackageOf(mapperElement).qualifiedName.toString()

        // 生成实现类名称：接口名 + 后缀（如 UserMapper -> UserMapperImpl）
        val implName = mapperElement.simpleName.toString() + suffix

        // 扫描 Mapper 接口中的所有方法，过滤出需要处理的抽象方法
        val allMethods = mapperElement.enclosedElements
            .filter { it.kind == javax.lang.model.element.ElementKind.METHOD }  // 只处理方法元素
            .map { it as javax.lang.model.element.ExecutableElement }

        // 调试：记录所有方法
        messanger?.printMessage(
            javax.tools.Diagnostic.Kind.NOTE,
            "[Mapper] Found ${allMethods.size} methods in ${mapperElement.simpleName}"
        )

        val processableMethods = allMethods.filter { method ->
            val isProcessable = isProcessableMethod(mapperElement, method)
            if (!isProcessable) {
                messanger?.printMessage(
                    javax.tools.Diagnostic.Kind.NOTE,
                    "[Mapper] Method ${method.simpleName} is not processable (static=${method.modifiers.contains(javax.lang.model.element.Modifier.STATIC)}, default=${method.modifiers.contains(javax.lang.model.element.Modifier.DEFAULT)}, hasDefaultValue=${method.defaultValue != null})"
                )
            }
            isProcessable
        }

        val methodDescriptors = processableMethods.mapNotNull { method ->
            val descriptor = buildMethodDescriptor(method,propertyResolver)
            if (descriptor == null) {
                messanger?.printMessage(
                    javax.tools.Diagnostic.Kind.WARNING,
                    "[Mapper] Failed to build descriptor for method ${method.simpleName} in ${mapperElement.simpleName}"
                )
            }
            descriptor
        }

        // 验证：Mapper 接口必须至少有一个映射方法
        if (methodDescriptors.isEmpty()) {
            error(mapperElement, "No abstract mapping methods found inside @Mapper type.")
            return null
        }

        // 收集 @BeforeMapping 和 @AfterMapping 方法
        // 注意：在 Kotlin 中，接口方法有方法体时会被编译为 Java 的 default 方法
        // 但有时可能被识别为抽象方法（取决于 Kotlin 版本和编译选项）
        // 为了确保所有生命周期方法都能被正确处理，我们收集所有标记了 @BeforeMapping/@AfterMapping 的方法
        // 无论它们是否是 default 方法，我们都会为它们生成实现（如果需要）
        val beforeMappingMethods = allMethods.filter { method ->
            method.getAnnotation(BeforeMapping::class.java) != null
        }
        val afterMappingMethods = allMethods.filter { method ->
            method.getAnnotation(AfterMapping::class.java) != null
        }

        // 收集 @MappingIgnore 方法（这些方法用于 expression 中调用）
        val ignoredMethods = allMethods.filter { method ->
            method.getAnnotation(MappingIgnore::class.java) != null
        }

        // 检查哪些 @MappingIgnore 方法被 expression 引用
        val referencedIgnoredMethods = ignoredMethods.filter { ignoredMethod ->
            val methodName = ignoredMethod.simpleName.toString()
            // 检查所有映射方法中的 expression 是否引用了这个方法
            methodDescriptors.any { methodDesc ->
                methodDesc.ownMappings.any { mapping ->
                    val expr = mapping.expression?.trim() ?: ""
                    // 移除 "java(...)" 包装（如果存在）
                    val processedExpr = if (expr.startsWith("java(") && expr.endsWith(")")) {
                        expr.substring(5, expr.length - 1)
                    } else {
                        expr
                    }
                    // 检查 expression 中是否包含方法调用（如 getCallOrPut(...)）
                    processedExpr.contains("$methodName(")
                }
            }
        }

        // 检测源文件类型（Kotlin 还是 Java）
        // 注意：在 KAPT 过程中，Kotlin 文件会被编译为 Java stub 文件
        // 所以不能通过文件扩展名来判断，而应该检查 kotlin.Metadata 注解
        val isKotlinSource = try {
            // 方法1：检查是否有 kotlin.Metadata 注解（最可靠的方法）
            val hasKotlinMetadata = mapperElement.annotationMirrors.any { mirror ->
                mirror.annotationType.toString() == "kotlin.Metadata"
            }

            // 调试日志
            messanger?.printMessage(
                javax.tools.Diagnostic.Kind.NOTE,
                "[Mapper] Source file detection for ${mapperElement.simpleName}: hasKotlinMetadata=$hasKotlinMetadata, annotations=${mapperElement.annotationMirrors.map { it.annotationType }}"
            )

            hasKotlinMetadata
        } catch (e: Exception) {
            messanger?.printMessage(
                javax.tools.Diagnostic.Kind.WARNING,
                "[Mapper] Failed to detect source file type for ${mapperElement.simpleName}: ${e.message}"
            )
            false
        }

        // 创建 Mapper 描述符，包含配置信息
        val descriptor = MapperDescriptor(
            mapperElement,
            packageName,
            implName,
            methodDescriptors,
            needNullCheck = needNullCheck,
            beforeMappingMethods = beforeMappingMethods,
            afterMappingMethods = afterMappingMethods,
            ignoredMethods = referencedIgnoredMethods,
            isKotlinSource = isKotlinSource
        )

        // 解析继承的映射配置（处理 @InheritConfiguration 注解）
        resolveInheritedMappings(descriptor)

        return descriptor
    }

    /**
     * 判断一个方法是否可以被处理（是否需要生成实现）
     *
     * 对于接口：
     * - 必须是抽象方法（不能是 static、default 或有默认实现的方法）
     * - 不能标记 @MappingIgnore 注解
     *
     * 对于抽象类：
     * - 必须是 abstract 方法
     * - 不能标记 @MappingIgnore 注解
     *
     * @param mapperElement Mapper 接口或抽象类
     * @param method 要检查的方法
     * @return true 如果该方法需要生成实现，否则返回 false
     */
    private fun isProcessableMethod(mapperElement: TypeElement, method: javax.lang.model.element.ExecutableElement): Boolean {
        // 排除 @BeforeMapping 和 @AfterMapping 方法（这些是生命周期方法，单独处理）
        if (method.getAnnotation(BeforeMapping::class.java) != null ||
            method.getAnnotation(AfterMapping::class.java) != null) {
            return false
        }

        // 排除 @MappingIgnore 方法（这些方法用于编写 expression 中的业务代码，不生成实现）
        if (method.getAnnotation(MappingIgnore::class.java) != null) {
            return false
        }

        return when {
            // 如果是接口，必须是抽象方法（不能是 static、default 或有默认实现）
            mapperElement.mapperKind.isInterface -> !method.modifiers.contains(javax.lang.model.element.Modifier.STATIC) &&
                    method.defaultValue == null &&
                    !method.modifiers.contains(javax.lang.model.element.Modifier.DEFAULT)
            // 如果是抽象类，必须是 abstract 方法
            else -> method.modifiers.contains(javax.lang.model.element.Modifier.ABSTRACT)
        }
    }

    /**
     * 构建单个映射方法的描述符
     *
     * 主要工作：
     * 1. 解析方法参数，识别 @MappingTarget 参数和源参数
     * 2. 验证方法签名（必须有源参数，更新方法必须返回 void 或目标类型）
     * 3. 收集 @Mapping 注解定义的映射规则
     * 4. 检查是否有 @InheritConfiguration 注解
     *
     * 方法类型：
     * - 创建方法：无 @MappingTarget，返回目标类型（如 toEntity(dto: UserDto): UserEntity）
     * - 更新方法：有 @MappingTarget，返回 void 或目标类型（如 updateEntity(@MappingTarget entity: UserEntity, dto: UserDto)）
     *
     * @param method 映射方法元素
     * @return MapperMethodDescriptor 如果构建成功，否则返回 null
     */
    private fun buildMethodDescriptor(method: javax.lang.model.element.ExecutableElement,propertyResolver: PropertyResolver): MapperMethodDescriptor? {
        // 构建所有参数的描述符
        val params = method.parameters.map { buildParameterDescriptor(it,propertyResolver) }

        // 查找所有标记了 @MappingTarget 的参数（用于更新方法）
        val mappingTargets = params.filter { it.isMappingTarget }

        // 验证：最多只能有一个 @MappingTarget 参数
        if (mappingTargets.size > 1) {
            error(method, "Only one @MappingTarget parameter is supported.")
            return null
        }

        val mappingTarget = mappingTargets.firstOrNull()  // 更新目标参数（如果有）
        val primarySource = params.firstOrNull { !it.isMappingTarget }  // 主要的源参数（第一个非 @MappingTarget 的参数）

        // 验证：必须至少有一个源参数
        if (primarySource == null) {
            error(method, "Mapper method must declare at least one source parameter. Method: ${method.simpleName}, Parameters: ${params.map { "${it.name}(${it.type})" }.joinToString()}")
            return null
        }

        // 验证：非更新方法（没有 @MappingTarget）必须返回目标类型，不能返回 void
        if (mappingTarget == null && method.returnType.kind == javax.lang.model.type.TypeKind.VOID) {
            error(method, "Non update mapper method must return a target type. Method: ${method.simpleName}, ReturnType: void")
            return null
        }

        // 收集方法上所有的 @Mapping 注解定义的映射规则
        val ownMappings = collectMappings(method)

        // 检查是否有 @InheritConfiguration 注解，获取要继承的方法名
        val inheritFrom = method.getAnnotation(InheritConfiguration::class.java)?.name?.takeIf { it.isNotBlank() }

        // 检查方法上是否有 @MappingConfig 注解（方法级配置）
        val methodMappingConfig = method.getAnnotation(MappingConfig::class.java)
        val methodNeedNullCheck = methodMappingConfig?.isNeedNullCheck

        // 构建方法描述符
        return MapperMethodDescriptor(
            method,                    // 方法元素
            method.simpleName.toString(),  // 方法名
            method.returnType,         // 返回类型
            params,                    // 所有参数
            mappingTarget,             // @MappingTarget 参数（如果有）
            primarySource,             // 主要源参数
            ownMappings,              // 显式定义的映射规则
            inheritFrom,              // 要继承配置的方法名（如果有）
            methodNeedNullCheck        // 方法级 needNullCheck 配置（null 表示使用类级配置）
        )
    }

    /**
     * 构建方法参数的描述符
     *
     * 主要工作：
     * 1. 提取参数名称和类型
     * 2. 检查是否标记了 @MappingTarget 注解（用于更新方法）
     * 3. 解析参数类型对应的 TypeElement（用于后续属性解析）
     *
     * @param param 方法参数元素
     * @return ParameterDescriptor 参数描述符
     */
    private fun buildParameterDescriptor(param: VariableElement,propertyResolver: PropertyResolver): ParameterDescriptor {
        val name = param.simpleName.toString()  // 参数名
        val isTarget = param.getAnnotation(MappingTarget::class.java) != null  // 是否是更新目标参数
        val type = param.asType()  // 参数类型
        val typeElement = propertyResolver.asTypeElement(type)  // 类型对应的元素（用于解析属性）
        return ParameterDescriptor(param, name, type, typeElement, isTarget)
    }

    /**
     * 收集方法上所有的 @Mapping 注解，构建映射规则列表
     *
     * @Mapping 注解支持：
     * - source: 源属性路径（如 "userName" 或 "address.city"）
     * - target: 目标属性路径（必须指定）
     * - constant: 常量值（如果指定，会忽略 source）
     * - ignore: 是否忽略该字段映射
     *
     * 注意：使用 getAnnotationsByType 会自动处理 @Repeatable 注解，
     * 所以无论是多个 @Mapping 还是 @Mappings({@Mapping, @Mapping}) 都能正确获取
     *
     * @param method 映射方法元素
     * @return 映射规则列表
     */
    private fun collectMappings(method: javax.lang.model.element.ExecutableElement): List<MappingSpec> {
        // 获取方法上所有的 @Mapping 注解（支持 @Repeatable，自动展开）
        val annotations = method.getAnnotationsByType(Mapping::class.java)?.toList().orEmpty()

        // 将每个 @Mapping 注解转换为 MappingSpec
        return annotations.mapNotNull { mapping ->
            val target = mapping.target.takeIf { it.isNotBlank() }

            // 验证：target 不能为空
            if (target == null) {
                error(method, "@Mapping target cannot be empty.")
                return@mapNotNull null
            }

            // 验证：expression 和 source 不能同时使用
            val expression = mapping.expression.takeIf { it.isNotBlank() }
            val source = mapping.source.takeIf { it.isNotBlank() }
            if (expression != null && source != null) {
                error(method, "@Mapping cannot have both 'source' and 'expression' attributes. Use only one.")
                return@mapNotNull null
            }

            // 验证：expression 和 constant 不能同时使用
            val constant = mapping.constant.takeIf { it.isNotBlank() }
            if (expression != null && constant != null) {
                error(method, "@Mapping cannot have both 'constant' and 'expression' attributes. Use only one.")
                return@mapNotNull null
            }

            // 构建映射规则
            MappingSpec(
                target = target,                                    // 目标属性路径（必须）
                source = source,                                    // 源属性路径（可选，为空时自动匹配同名属性）
                constant = constant,                                // 常量值（可选，如果指定会覆盖 source）
                expression = expression,                            // Java 表达式（可选，支持自定义业务逻辑）
                ignore = mapping.ignore                            // 是否忽略该字段
            )
        }
    }

    /**
     * 解析所有方法的继承映射配置
     *
     * 处理 @InheritConfiguration 注解，将继承的映射规则与当前方法的映射规则合并。
     * 继承的映射规则会被当前方法的映射规则覆盖（后定义的优先级更高）。
     *
     * 例如：
     * ```kotlin
     * @Mapping(source = "userName", target = "name")
     * fun toEntity(dto: UserDto): UserEntity
     *
     * @InheritConfiguration
     * @Mapping(source = "email", target = "emailAddress")  // 会继承 userName->name，并添加 email->emailAddress
     * fun toEntityWithEmail(dto: UserDto): UserEntity
     * ```
     *
     * @param descriptor Mapper 描述符，包含所有方法
     */
    private fun resolveInheritedMappings(descriptor: MapperDescriptor) {
        // 构建方法名到方法的映射，用于快速查找被继承的方法
        val methodMap = descriptor.methods.associateBy { it.name }

        // 遍历所有方法，解析继承的映射配置
        descriptor.methods.forEach { method ->
            // 如果方法的映射规则还未解析（resolvedMappings 为空），则进行解析
            if (method.resolvedMappings.isEmpty()) {
                method.resolvedMappings = resolveMappingsForMethod(method, methodMap, mutableSetOf())
            }
        }
    }

    /**
     * 递归解析方法的映射配置，包括继承的配置
     *
     * 主要工作：
     * 1. 如果方法有 @InheritConfiguration，递归解析被继承方法的映射规则
     * 2. 检测循环继承（A 继承 B，B 继承 A）
     * 3. 合并继承的映射规则和当前方法的映射规则
     * 4. 对于同一个 target，后面的规则会覆盖前面的规则（ownMappings 优先级更高）
     *
     * 合并规则：
     * - 继承的映射规则在前
     * - 当前方法的映射规则在后
     * - 如果同一个 target 有多个规则，取最后一个（当前方法的规则优先）
     *
     * @param method 要解析映射配置的方法
     * @param methods 所有方法的映射表（方法名 -> 方法描述符）
     * @param visiting 正在访问的方法名集合（用于检测循环继承）
     * @return 合并后的映射规则列表
     */
    private fun resolveMappingsForMethod(
        method: MapperMethodDescriptor,
        methods: Map<String, MapperMethodDescriptor>,
        visiting: MutableSet<String>
    ): List<MappingSpec> {
        // 解析继承的映射规则
        val inherited = method.inheritFrom?.let { inheritName ->
            val inheritedMethod = methods[inheritName]

            // 检查被继承的方法是否存在
            if (inheritedMethod == null) {
                error(method.element, "@InheritConfiguration refers to unknown method $inheritName")
                emptyList()
            } else {
                // 检测循环继承（A 继承 B，B 继承 A）
                if (!visiting.add(method.name)) {
                    error(method.element, "Circular @InheritConfiguration detected at ${method.name}")
                    emptyList()
                } else {
                    // 递归解析被继承方法的映射规则（如果还未解析）
                    val resolved = if (inheritedMethod.resolvedMappings.isEmpty()) {
                        resolveMappingsForMethod(inheritedMethod, methods, visiting)
                    } else {
                        inheritedMethod.resolvedMappings
                    }
                    visiting.remove(method.name)  // 移除访问标记
                    resolved
                }
            }
        } ?: emptyList()  // 如果没有 @InheritConfiguration，继承的规则为空

        // 合并继承的映射规则和当前方法的映射规则
        // 继承的规则在前，当前方法的规则在后
        val merged = (inherited + method.ownMappings)
            .groupBy { it.target }  // 按 target 分组
            .map { (_, specs) -> specs.last() }  // 对于同一个 target，取最后一个规则（当前方法的规则优先）

        return merged
    }

    /**
     * TypeElement 的扩展属性：判断 Mapper 元素的类型
     *
     * 用于区分 @Mapper 注解是应用在接口上还是抽象类上，
     * 因为接口和抽象类在处理方法时的规则略有不同。
     *
     * @return MapperTypeKind.INTERFACE 如果是接口，否则返回 MapperTypeKind.CLASS
     */
    val TypeElement.mapperKind: MapperTypeKind
        get() = when (this.kind) {
            javax.lang.model.element.ElementKind.INTERFACE -> MapperTypeKind.INTERFACE
            else -> MapperTypeKind.CLASS
        }

    /**
     * Mapper 类型枚举
     *
     * 用于标识 @Mapper 注解应用的元素类型（接口或抽象类）
     */
    enum class MapperTypeKind {
        INTERFACE,  // 接口类型
        CLASS;      // 抽象类类型

        /**
         * 判断是否是接口类型
         */
        val isInterface: Boolean get() = this == INTERFACE
    }
}
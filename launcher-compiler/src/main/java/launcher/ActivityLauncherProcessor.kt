package launcher

import com.google.auto.service.AutoService
import com.google.common.collect.ImmutableSet
import launcher.classbinding.ClassBindingFactory
import launcher.codegeneration.FunctionGeneration
import launcher.codegeneration.MarketViewRouteGeneration
import launcher.codegeneration.MapperGeneration
import launcher.codegeneration.RouterGeneration
import launcher.codegeneration.TradeInterfaceGeneration
import launcher.codegeneration.TradeServiceAggregatorGeneration
import launcher.error.error
import launcher.error.messanger
import launcher.mapper.MapperDescriptor
import launcher.mapper.MapperMethodDescriptor
import launcher.mapper.MapperUtils
import launcher.mapper.MappingSpec
import launcher.mapper.ParameterDescriptor
import launcher.mapper.PropertyResolver
import launcher.service.ServiceUtil
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.MirroredTypesException
import javax.lang.model.type.TypeMirror
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType
import mapper.AfterMapping
import mapper.BeforeMapping
import mapper.InheritConfiguration
import mapper.Mapper
import mapper.Mapping
import mapper.MappingConfig
import mapper.MappingIgnore
import mapper.MappingTarget


// 添加计时工具类
class Timer(private val name: String) {
    private val startTime = System.currentTimeMillis()

    fun stop() {
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        messanger?.printMessage(
            javax.tools.Diagnostic.Kind.NOTE,
            "[Performance] $name took ${duration}ms"
        )
    }
}

@AutoService(Processor::class)
@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.AGGREGATING)
class ActivityLauncherProcessor :AbstractProcessor() {

    private lateinit var filer: Filer
    private lateinit var propertyResolver: PropertyResolver

    @Synchronized
    override fun init(env: ProcessingEnvironment) {
        super.init(env)
        filer = env.filer
        messanger = processingEnv.messager
        propertyResolver = PropertyResolver(processingEnv)
    }

    override fun getSupportedAnnotationTypes() = listOf<Class<*>>(
        Boom::class.java,
        MakeResult::class.java,
        MarketViewRoute::class.java,
        ParentCls::class.java,
        Router::class.java,
        Function::class.java,
        TradeInterface::class.java,
        Mapper::class.java,              // MapStruct风格的Mapper接口注解
        Mapping::class.java,              // 字段映射规则注解
        MappingTarget::class.java,        // 标记更新目标对象的参数注解
        InheritConfiguration::class.java,  // 继承其他方法映射配置的注解
        MappingConfig::class.java,        // Mapper配置注解
        MappingIgnore::class.java,        // 标记忽略生成的方法注解
        BeforeMapping::class.java,        // 映射前执行自定义逻辑的注解
        AfterMapping::class.java,         // 映射后执行自定义逻辑的注解
        TradeServiceMaker::class.java         //ITradeManagerService服务接口生成
    ).map { it.canonicalName }.toSet()

    override fun process(elements: Set<TypeElement>, env: RoundEnvironment): Boolean {
        val totalTimer = Timer("Total Processing")

        val findClassesTimer = Timer("Find Classes")
        val classesToProcess = findClassesToPrecess(env)
        findClassesTimer.stop()

        val processTargetsTimer = Timer("Process Targets")
        processTargets(classesToProcess)
        processTargetsTimer.stop()

        val processFunctionTimer = Timer("Process Function")
        processFunction(mutableSetOf<TypeElement>().apply {
            processAnnotation<Function>(env) { element ->
                add(element as TypeElement)
            }
            processAnnotation<MarketViewRoute>(env) { element ->
                add(element as TypeElement)
            }
        })
        processFunctionTimer.stop()

        val processTradeServiceTimer = Timer("Process Trade Service")
        processTradeService(env)
        processTradeServiceTimer.stop()

        val mapperTimer = Timer("Process Mapper")
        processMapper(env)
        mapperTimer.stop()

        val processTradeServiceAggregatorTimer = Timer("Process Trade Service Aggregator")
        ServiceUtil.processTradeServiceAggregator(env,processingEnv,filer)
        processTradeServiceAggregatorTimer.stop()

        totalTimer.stop()
        return true
    }

    override fun getSupportedSourceVersion() = SourceVersion.latestSupported()

    private fun findClassesToPrecess(env: RoundEnvironment): Set<TypeElement> {
        val classesToProcess = mutableSetOf<TypeElement>()
        val timer = Timer("Find Classes To Process")

        processAnnotation<Boom>(env) { element ->
            classesToProcess += element.enclosingElement as TypeElement
        }
        processAnnotation<MakeResult>(env) { element ->
            classesToProcess += element as TypeElement
        }
        processAnnotation<Router>(env) { element ->
            classesToProcess += element as TypeElement
        }
        processAnnotation<ParentCls>(env) { element ->
            classesToProcess += element as TypeElement
        }

        timer.stop()
        return classesToProcess
    }

    private inline fun <reified T : Annotation> processAnnotation(
        env: RoundEnvironment,
        process: (Element) -> Unit
    ) {
        val timer = Timer("Process ${T::class.java.simpleName} Annotation")
        for (element in env.getElementsAnnotatedWith(T::class.java)) {
            try {
                process(element)
            } catch (e: Exception) {
                logParsingError(element, T::class.java, e)
            }
        }
        timer.stop()
    }

    private fun processTargets(classesToProcess: Set<TypeElement>) {
        val timer = Timer("Process ${classesToProcess.size} Targets")
        for (classToPrecess in classesToProcess) {
            try {
                val classBinding = ClassBindingFactory(classToPrecess).create() ?: continue
                classBinding.getClasGeneration().brewJava().writeTo(filer)
                val bindingClassName =
                    launcher.codegeneration.getBindingClassName(classToPrecess, "_XXXxxx")
                if (classBinding.routerPath.isNotEmpty()) {
                    classBinding.bindingClassName = bindingClassName
                    RouterGeneration(classBinding).brewJava().writeTo(filer)
                }
            } catch (e: IOException) {
                error(
                    classToPrecess,
                    "Unable to write binding for typeName %s: %s",
                    classToPrecess,
                    e.message
                        ?: ""
                )
            }
        }
        timer.stop()
    }

    private fun processFunction(classesToProcess: Set<TypeElement>) {
        val timer = Timer("Process ${classesToProcess.size} Functions")
        val allFunction = mutableListOf<TypeElement>()
        val allView = mutableListOf<TypeElement>()
        val allFunctionGroup = mutableListOf<String>()
        val allFunctionCls = mutableMapOf<String,String>()

        for (classToPrecess in classesToProcess) {
            try {
                val allFunctionAno = classToPrecess.getAnnotation(Function::class.java)
                if(allFunctionAno!=null){
                    allFunction.add(classToPrecess)
                    allFunctionGroup.addAll(allFunctionAno.group)
                }
                val viewAno = classToPrecess.getAnnotation(MarketViewRoute::class.java)
                if (viewAno!=null){
                    allView.add(classToPrecess)
                }
            } catch (e: IOException) {
                error(classToPrecess, "Unable to write binding for typeName %s: %s", classToPrecess, e.message
                    ?: "")
            }
        }

        kotlin.runCatching {
            if (allFunction.isNotEmpty())
                FunctionGeneration(allFunction, allFunctionGroup.distinct().filter {
                    it.isNotEmpty()
                }.toMutableList()).apply {
                    throwError = {id,ele->
                        error(ele, "Found that the same FunctionId $id corresponds " +
                                "to multiple different implementation classes")
                    }
                }.brewJava().writeTo(filer)
        }

        kotlin.runCatching {
            if (allView.isNotEmpty())
                MarketViewRouteGeneration(allView).brewJava().writeTo(filer)
        }
        timer.stop()
    }

    /**
     * 处理TradeService注解，生成TradeInterfaceFactory
     */
    private fun processTradeService(env: RoundEnvironment) {
        val timer = Timer("Process Trade Service")
        val moduleName = processingEnv.options["module_name"] ?: return
        val interfaceMap = mutableMapOf<String, String>()
        val innerInterfaceMap = mutableMapOf<String, String>()

        for (element in env.getElementsAnnotatedWith(TradeInterface::class.java)) {
            try {
                if (element is TypeElement) {
                    val implClassName = element.qualifiedName.toString()
                    val interfaceType = ServiceUtil.getAnnotationInterfaceType(element)
                    if (interfaceType != null) {
                        val interfaceClassName = interfaceType.toString()
                        val isInner = element.getAnnotation(TradeInterface::class.java).isInner

                        if (isInner) {
                            innerInterfaceMap[interfaceClassName] = implClassName
                        } else {
                            interfaceMap[interfaceClassName] = implClassName
                        }
                    }
                }
            } catch (e: Exception) {
                error(element, "Error processing TradeService annotation: ${e.message}")
            }
        }

        if (interfaceMap.isNotEmpty() || innerInterfaceMap.isNotEmpty()) {
            try {
                TradeInterfaceGeneration(interfaceMap, innerInterfaceMap, moduleName)
                    .brewJava()
                    .writeTo(filer)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        interfaceMap.clear()
        innerInterfaceMap.clear()
        timer.stop()
    }

    /**
     * 处理所有标注了 @Mapper 注解的接口或抽象类
     *
     * 这是 Mapper 处理的入口方法，会扫描编译环境中的所有 @Mapper 注解，
     * 然后为每个 Mapper 接口生成对应的实现类（如 UserMapper -> UserMapperImpl）
     *
     * @param env 编译环境，包含所有被注解的元素
     */
    private fun processMapper(env: RoundEnvironment) {
        // 获取所有标注了 @Mapper 注解的元素（通常是接口或抽象类）
        val mapperElements = env.getElementsAnnotatedWith(Mapper::class.java)

        // 遍历每个 Mapper 元素，生成对应的实现类
        mapperElements.forEach { element ->
            if (element is TypeElement) {
                // 使用 runCatching 捕获异常，避免一个 Mapper 的错误影响其他 Mapper 的处理
                runCatching { MapperUtils.handleMapper(element,processingEnv,propertyResolver,filer) }
                    .onFailure { failure ->
                        error(element, failure.message ?: "Unknown error when generating mapper impl.")
                    }
            }
        }
    }



    private fun logParsingError(element: Element, annotation: Class<out Annotation>, e: Exception) {
        val stackTrace = StringWriter()
        e.printStackTrace(PrintWriter(stackTrace))
        error(element, "Unable to parse @%s binding.\n\n%s", annotation.simpleName, stackTrace)
    }

    override fun getSupportedOptions(): Set<String> {
        return ImmutableSet.of(
            "incremental",
            "incremental.apt",
            "incremental.apt.aggregation",
            "incremental.apt.isolating"
        )
    }
}


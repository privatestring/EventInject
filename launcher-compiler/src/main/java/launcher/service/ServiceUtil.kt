package launcher.service

import launcher.Timer
import launcher.TradeInterface
import launcher.TradeServiceMaker
import launcher.codegeneration.TradeServiceAggregatorGeneration
import launcher.error.error
import javax.annotation.processing.Filer
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.MirroredTypesException
import javax.lang.model.type.TypeMirror

object ServiceUtil {
    /**
     * 获取 TradeServiceAggregator 注解中的 baseInterface 类型
     */
    fun getBaseInterfaceType(element: TypeElement): TypeMirror? {
        try {
            element.getAnnotation(TradeServiceMaker::class.java).baseInterface
        } catch (mte: MirroredTypeException) {
            return mte.typeMirror
        }
        return null
    }

    /**
     * 获取 TradeServiceAggregator 注解中的 additionalInterfaces 类型列表
     */
    fun getAdditionalInterfaceTypes(element: TypeElement): List<TypeMirror> {
        try {
            element.getAnnotation(TradeServiceMaker::class.java).additionalInterfaces
        } catch (mte: MirroredTypesException) {
            return mte.typeMirrors
        }
        return emptyList()
    }

    /**
     * 获取TradeInterface注解中的接口类型
     */
    fun getAnnotationInterfaceType(element: TypeElement): TypeMirror? {
        try {
            // 这会抛出异常，所以需要在catch中处理
            element.getAnnotation(TradeInterface::class.java).value
        } catch (mte: MirroredTypeException) {
            return mte.typeMirror
        }
        return null
    }

    /**
     * 处理 TradeServiceAggregator 注解，生成聚合接口
     */
    fun processTradeServiceAggregator(env: RoundEnvironment,processingEnv: ProcessingEnvironment,filer: Filer) {
        val timer = Timer("Process Trade Service Aggregator")

        for (element in env.getElementsAnnotatedWith(TradeServiceMaker::class.java)) {
            try {
                if (element is TypeElement) {
                    val annotation = element.getAnnotation(TradeServiceMaker::class.java)

                    // 获取 baseInterface
                    val baseInterfaceType = ServiceUtil.getBaseInterfaceType(element)
                    if (baseInterfaceType == null) {
                        error(element, "TradeServiceAggregator: baseInterface is required")
                        continue
                    }

                    // 获取 scanPackages
                    val scanPackages = annotation.scanPackages.toList()

                    // 获取 additionalInterfaces
                    val additionalInterfaceTypes = ServiceUtil.getAdditionalInterfaceTypes(element)

                    // 获取包名和类名
                    val packageName = annotation.packageName.ifEmpty {
                        processingEnv.elementUtils.getPackageOf(element).qualifiedName.toString()
                    }
                    val className = annotation.className.ifEmpty {
                        element.simpleName.toString() + "Generated"
                    }

                    // 生成代码
                    TradeServiceAggregatorGeneration(
                        processingEnv,env,
                        baseInterfaceType,
                        scanPackages,
                        additionalInterfaceTypes,
                        packageName,
                        className
                    ).brewJava().writeTo(filer)
                }
            } catch (e: Exception) {
                error(element, "Error processing TradeServiceAggregator annotation: ${e.message}")
            }
        }

        timer.stop()
    }
}
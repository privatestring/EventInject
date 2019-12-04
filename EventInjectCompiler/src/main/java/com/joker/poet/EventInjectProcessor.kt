package com.joker.poet

import com.google.auto.service.AutoService
import com.joker.annotation.EventBridge
import com.joker.poet.utils.Logger
import com.squareup.kotlinpoet.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2019-11-20
 */
@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class EventInjectProcessor : AbstractProcessor() {

    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
        processingEnv?.messager?.apply { logger = Logger(this) }
        eventProcessingEnv = processingEnv
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return setOf(EventBridge::class.java.canonicalName).toMutableSet()
    }


    override fun process(
        annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment
    ): Boolean {
        val elements = roundEnv.getElementsAnnotatedWith(EventBridge::class.java).orEmpty()
        if (elements.isNotEmpty()) {
            val postFunc = FunSpec.builder("postEventInject")
                .addComment("this is kotlin doc")
                .addParameter("scheme", String::class.asTypeName())
                .addParameter("params", Any::class.asTypeName().copy(nullable = true))
                .returns(Any::class.asTypeName().copy(nullable = true))
            elements.forEachIndexed { index, element ->
                element.getAnnotation(EventBridge::class.java)?.let { event ->
                    var listStr = "arrayOf("
                    event.schemes.forEachIndexed { index, s ->
                        if (index > 0) listStr += ","
                        listStr += "\"$s\""
                    }
                    listStr += ")"
                    if (index == 0) {
                        postFunc.beginControlFlow("if ($listStr.any { it == scheme })")
                    } else {
                        postFunc.nextControlFlow("else if ($listStr.any { it == scheme })")
                    }
                    postFunc.addStatement("return %T().handleEvent(params)", element.asType())
                }
            }
            postFunc.endControlFlow()
            postFunc.addStatement("return null")
            val eventFile = FileSpec.builder("com.mei.models", "EventInject")
            val typeSpec = TypeSpec.classBuilder("EventInjectImpl")
            typeSpec.addFunction(postFunc.build())
            eventFile.addType(typeSpec.build())
            eventFile.build().writeFile()
        }
        return true
    }


}
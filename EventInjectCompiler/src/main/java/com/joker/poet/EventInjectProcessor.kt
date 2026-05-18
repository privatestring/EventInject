package com.joker.poet

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName

/**
 * EventBridge KSP 处理器。
 *
 * 扫描 @EventBridge 注解的类，生成 EventInjectImpl，
 * 包含 postEventInject(scheme, params) 方法，根据 schemes 分发到对应 handleEvent。
 */
class EventInjectProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    /** 防止多轮处理时重复生成 */
    private var generated = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (generated) return emptyList()

        val symbols = resolver.getSymbolsWithAnnotation("com.joker.annotation.EventBridge").toList()
        val (validSymbols, unprocessed) = symbols.partition { it.validate() }
        val elements = validSymbols.filterIsInstance<KSClassDeclaration>()

        if (elements.isEmpty()) return unprocessed

        // 过滤掉 schemes 为空的类（无法匹配任何 scheme）
        val validElements = elements.filter { classDecl ->
            val annotation = classDecl.annotations.first { ann ->
                ann.shortName.asString() == "EventBridge"
            }
            val schemes = annotation.arguments.first().value as? List<*> ?: emptyList<String>()
            schemes.isNotEmpty()
        }

        if (validElements.isEmpty()) return unprocessed

        val postFunc = FunSpec.builder("postEventInject")
            .addParameter("scheme", String::class.asTypeName())
            .addParameter("params", ANY.copy(nullable = true))
            .returns(ANY.copy(nullable = true))

        validElements.forEachIndexed { index, classDecl ->
            val annotation = classDecl.annotations.first { ann ->
                ann.shortName.asString() == "EventBridge"
            }
            val schemes = annotation.arguments.first().value as List<*>
            val listStr = schemes.joinToString(",") { "\"$it\"" }
            val qualifiedName = classDecl.qualifiedName?.asString() ?: return@forEachIndexed

            if (index == 0) {
                postFunc.beginControlFlow("if (arrayOf($listStr).any { it == scheme })")
            } else {
                postFunc.nextControlFlow("else if (arrayOf($listStr).any { it == scheme })")
            }
            postFunc.addStatement("return %L().handleEvent(params)", qualifiedName)
        }
        postFunc.endControlFlow()
        postFunc.addStatement("return null")

        val typeSpec = TypeSpec.classBuilder("EventInjectImpl")
            .addFunction(postFunc.build())
            .build()

        val fileSpec = FileSpec.builder("com.mei.models", "EventInject")
            .addType(typeSpec)
            .build()

        val containingFiles = validElements.mapNotNull { it.containingFile }
        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(aggregating = true, *containingFiles.toTypedArray()),
            packageName = "com.mei.models",
            fileName = "EventInject",
            extensionName = "kt"
        )
        file.writer().use { writer ->
            fileSpec.writeTo(writer)
        }

        generated = true
        return unprocessed
    }
}

package com.joker.poet

import com.joker.poet.utils.Logger
import com.squareup.kotlinpoet.FileSpec
import java.io.File
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2019-11-20
 */

var logger: Logger? = null
var eventProcessingEnv: ProcessingEnvironment? = null

fun Element.packageName(): String =
    eventProcessingEnv?.elementUtils?.getPackageOf(this)?.qualifiedName?.toString().orEmpty()

fun FileSpec.writeFile() {
    val kaptKotlinGeneratedDir = eventProcessingEnv?.options?.get("kapt.kotlin.generated").orEmpty()
    val outputFile = File(kaptKotlinGeneratedDir).apply {
        mkdirs()
    }
    if (outputFile.exists()) {
        writeTo(outputFile.toPath())
    }
}
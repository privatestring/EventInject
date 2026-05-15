package launcher

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.validate
import launcher.classbinding.ClassBindingFactory
import launcher.Boom
import launcher.MakeResult
import launcher.ParentCls

/**
 * launcher-compiler 的 KSP 版本处理器入口。
 *
 * 功能一：Activity/Fragment/BroadcastReceiver/Model 启动器
 * - @Boom → 生成 Launcher
 * - @MakeResult → 生成 startForResult 方法
 * - @ParentCls → 生成 addIntentParams/addBundleParams 方法
 */
class LauncherKspProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val unprocessed = mutableListOf<KSAnnotated>()

        // 收集需要处理的类
        val classesToProcess = mutableSetOf<KSClassDeclaration>()

        // 扫描 @Boom 字段 → 取所在类
        resolver.getSymbolsWithAnnotation(Boom::class.qualifiedName!!).forEach { symbol ->
            if (!symbol.validate()) {
                unprocessed += symbol
                return@forEach
            }
            when (symbol) {
                is KSPropertyDeclaration -> {
                    val parent = symbol.parentDeclaration as? KSClassDeclaration
                    if (parent != null) classesToProcess += parent
                }
            }
        }

        // 扫描 @MakeResult 类
        resolver.getSymbolsWithAnnotation(MakeResult::class.qualifiedName!!).forEach { symbol ->
            if (!symbol.validate()) {
                unprocessed += symbol
                return@forEach
            }
            if (symbol is KSClassDeclaration) classesToProcess += symbol
        }

        // 扫描 @ParentCls 类
        resolver.getSymbolsWithAnnotation(ParentCls::class.qualifiedName!!).forEach { symbol ->
            if (!symbol.validate()) {
                unprocessed += symbol
                return@forEach
            }
            if (symbol is KSClassDeclaration) classesToProcess += symbol
        }

        // 处理每个类
        classesToProcess.forEach { classDecl ->
            processTarget(classDecl)
        }

        return unprocessed
    }

    private fun processTarget(classDecl: KSClassDeclaration) {
        val classBinding = ClassBindingFactory(classDecl, logger).create() ?: return
        val javaFile = classBinding.getClassGeneration().brewJava()

        val containingFile = classDecl.containingFile ?: return
        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(aggregating = false, containingFile),
            packageName = javaFile.packageName,
            fileName = javaFile.typeSpec.name,
            extensionName = "java"
        )
        file.writer().use { writer ->
            javaFile.writeTo(writer)
        }
    }
}

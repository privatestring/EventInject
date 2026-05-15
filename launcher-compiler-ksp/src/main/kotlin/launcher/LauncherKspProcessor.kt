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
import com.squareup.javapoet.ClassName
import launcher.classbinding.ClassBindingFactory
import launcher.codegeneration.RouterGeneration
import launcher.Boom
import launcher.MakeResult
import launcher.ParentCls
import launcher.Router

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2025/5/15
 *
 * launcher-compiler 的 KSP 版本处理器入口。
 *
 * 功能一：Activity/Fragment/BroadcastReceiver/Model 启动器
 * - @Boom → 生成 Launcher
 * - @MakeResult → 生成 startForResult 方法
 * - @ParentCls → 生成 addIntentParams/addBundleParams 方法
 *
 * 功能二：Router 路由系统
 * - @Router → 生成 _XXXxxx 路由类（putRouter/jump/getActionScheme）
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

        // 扫描 @Router 类
        resolver.getSymbolsWithAnnotation(Router::class.qualifiedName!!).forEach { symbol ->
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

        // 如果有 @Router 注解且 routerPath 不为空，额外生成 _XXXxxx 路由文件
        if (classBinding.routerPath.isNotEmpty()) {
            val simpleName = classDecl.simpleName.asString()
            val routerBindingClassName = ClassName.get(classBinding.packageName, "${simpleName}_XXXxxx")
            classBinding.bindingClassName = routerBindingClassName
            val routerJavaFile = RouterGeneration(classBinding).brewJava()
            val routerFile = codeGenerator.createNewFile(
                dependencies = Dependencies(aggregating = false, containingFile),
                packageName = routerJavaFile.packageName,
                fileName = routerJavaFile.typeSpec.name,
                extensionName = "java"
            )
            routerFile.writer().use { writer ->
                routerJavaFile.writeTo(writer)
            }
        }
    }
}

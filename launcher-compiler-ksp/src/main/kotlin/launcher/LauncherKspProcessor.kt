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

        // 收集需要处理的类（合并扫描，减少 getSymbolsWithAnnotation 调用次数）
        val classesToProcess = mutableSetOf<KSClassDeclaration>()

        val targetAnnotations = listOf(
            Boom::class.qualifiedName!!,
            MakeResult::class.qualifiedName!!,
            ParentCls::class.qualifiedName!!,
            IncludeParentBoom::class.qualifiedName!!,
            Router::class.qualifiedName!!
        )

        for (annotationName in targetAnnotations) {
            resolver.getSymbolsWithAnnotation(annotationName).forEach { symbol ->
                if (!symbol.validate()) {
                    unprocessed += symbol
                    return@forEach
                }
                when (symbol) {
                    is KSPropertyDeclaration -> {
                        // @Boom 标注在属性上，取所在类
                        val parent = symbol.parentDeclaration as? KSClassDeclaration
                        if (parent != null) classesToProcess += parent
                    }
                    is KSClassDeclaration -> classesToProcess += symbol
                }
            }
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

        // 如果标注了 @IncludeParentBoom，将父类文件也加入 dependencies，确保增量编译正确
        val hasIncludeParentBoom = classDecl.annotations.any {
            it.shortName.asString() == IncludeParentBoom::class.simpleName
        }
        val sourceFiles = if (hasIncludeParentBoom) {
            val parentFiles = collectParentContainingFiles(classDecl)
            (listOf(containingFile) + parentFiles).distinct().toTypedArray()
        } else {
            arrayOf(containingFile)
        }

        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(aggregating = false, *sourceFiles),
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
                dependencies = Dependencies(aggregating = false, *sourceFiles),
                packageName = routerJavaFile.packageName,
                fileName = routerJavaFile.typeSpec.name,
                extensionName = "java"
            )
            routerFile.writer().use { writer ->
                routerJavaFile.writeTo(writer)
            }
        }
    }

    /**
     * 收集类继承链中所有父类的 containingFile（用于增量编译依赖追踪）
     */
    private fun collectParentContainingFiles(classDecl: KSClassDeclaration): List<com.google.devtools.ksp.symbol.KSFile> {
        val files = mutableListOf<com.google.devtools.ksp.symbol.KSFile>()
        var superClass = classDecl.superTypes.firstOrNull()?.resolve()?.declaration as? KSClassDeclaration
        while (superClass != null && superClass.qualifiedName?.asString() != "java.lang.Object") {
            superClass.containingFile?.let { files.add(it) }
            superClass = superClass.superTypes.firstOrNull()?.resolve()?.declaration as? KSClassDeclaration
        }
        return files
    }
}

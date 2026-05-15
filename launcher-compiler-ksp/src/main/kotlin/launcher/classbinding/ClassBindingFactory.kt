package launcher.classbinding

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.javapoet.ClassName
import launcher.Boom
import launcher.MakeResult
import launcher.ParentCls
import launcher.param.ArgumentFactory
import launcher.utils.CLASS_NAME_END
import launcher.utils.toTypeName

/**
 * KSP 版 ClassBindingFactory，解析注解类并构建 ClassBinding
 */
internal class ClassBindingFactory(
    private val classDeclaration: KSClassDeclaration,
    private val logger: KSPLogger
) {

    fun create(): ClassBinding? {
        try {
            val knownClassType = KnownClassType.getByType(classDeclaration)
            val targetTypeName = classDeclaration.asType(emptyList()).toTypeName()
            val packageName = classDeclaration.packageName.asString()
            val simpleName = classDeclaration.simpleName.asString()
            val bindingClassName = ClassName.get(packageName, simpleName + CLASS_NAME_END)

            val argumentFactory = ArgumentFactory(classDeclaration, logger)

            // 收集所有带 @Boom 注解的属性
            val argumentBindings = classDeclaration.getAllProperties()
                .filter { prop -> prop.annotations.any { it.shortName.asString() == Boom::class.simpleName } }
                .mapNotNull { prop -> argumentFactory.parseArgument(prop, packageName, knownClassType) }
                .sortedBy { it.index }
                .toList()

            // 检查 index 重复
            val indexGroups = argumentBindings.groupBy { it.index }
            val duplicateIndex = indexGroups.entries.firstOrNull { it.value.size > 1 }
            if (duplicateIndex != null) {
                logger.error("This $simpleName has index parameters are the same (index=${duplicateIndex.key})", classDeclaration)
                return null
            }

            // 读取 @MakeResult
            val makeResultAnnotation = classDeclaration.annotations.firstOrNull {
                it.shortName.asString() == MakeResult::class.simpleName
            }
            val includeStartForResult = makeResultAnnotation?.arguments
                ?.firstOrNull { it.name?.asString() == "includeStartForResult" }
                ?.value as? Boolean ?: false

            // 读取 @ParentCls
            val parentClsAnnotation = classDeclaration.annotations.firstOrNull {
                it.shortName.asString() == ParentCls::class.simpleName
            }
            val isParentClass = parentClsAnnotation?.arguments
                ?.firstOrNull { it.name?.asString() == "isParentClass" }
                ?.value as? Boolean ?: false

            return ClassBinding(
                knownClassType = knownClassType,
                targetTypeName = targetTypeName,
                bindingClassName = bindingClassName,
                packageName = packageName,
                argumentBindings = argumentBindings,
                includeStartForResult = includeStartForResult,
                isParentClass = isParentClass
            )
        } catch (e: Exception) {
            logger.error("Error creating ClassBinding for ${classDeclaration.simpleName.asString()}: ${e.message}", classDeclaration)
            return null
        }
    }
}

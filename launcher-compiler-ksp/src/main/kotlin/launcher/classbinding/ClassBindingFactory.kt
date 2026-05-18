package launcher.classbinding

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import launcher.Boom
import launcher.IncludeParentBoom
import launcher.MakeResult
import launcher.ParentCls
import launcher.Router
import launcher.param.ArgumentFactory
import launcher.utils.CLASS_NAME_END
import launcher.utils.toTypeName

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2025/5/15
 *
 * KSP 版 ClassBindingFactory。
 * 解析一个类上的所有 Launcher 相关注解（@Boom/@MakeResult/@ParentCls/@Router），
 * 构建 [ClassBinding] 供代码生成器使用。
 */
internal class ClassBindingFactory(
    private val classDeclaration: KSClassDeclaration,
    private val logger: KSPLogger
) {

    fun create(): ClassBinding? {
        try {
            val knownClassType = KnownClassType.getByType(classDeclaration)
            val packageName = classDeclaration.packageName.asString()
            val simpleName = classDeclaration.simpleName.asString()
            // 泛型类使用 raw type（ClassName），避免 K2 编译器对泛型参数的严格类型匹配报错
            val targetTypeName: TypeName = if (classDeclaration.typeParameters.isNotEmpty()) {
                ClassName.get(packageName, simpleName)
            } else {
                classDeclaration.asType(emptyList()).toTypeName()
            }
            val bindingClassName = ClassName.get(packageName, simpleName + CLASS_NAME_END)

            val argumentFactory = ArgumentFactory(classDeclaration, logger)

            // 检查是否标注了 @IncludeParentBoom（包含父类 @Boom 属性）
            val includeParentBoom = classDeclaration.annotations.any {
                it.shortName.asString() == IncludeParentBoom::class.simpleName
            }

            // 收集 @Boom 属性：有 @IncludeParentBoom 则包含父类，否则只收集当前类（与 KAPT 行为一致）
            val properties = if (includeParentBoom) {
                classDeclaration.getAllProperties()
            } else {
                classDeclaration.getDeclaredProperties()
            }

            val argumentBindings = properties
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

            // 读取 @Router
            val routerAnnotation = classDeclaration.annotations.firstOrNull {
                it.shortName.asString() == Router::class.simpleName
            }
            val routerPath = routerAnnotation?.arguments
                ?.firstOrNull { it.name?.asString() == "routerPath" }
                ?.value as? String ?: ""
            val cls: String? = routerAnnotation?.arguments
                ?.firstOrNull { it.name?.asString() == "cls" }
                ?.value?.let { value ->
                    val ksType = value as? KSType
                    val qualifiedName = ksType?.declaration?.qualifiedName?.asString()
                    // cls 默认为 Void，此时返回 null 表示使用当前类
                    if (qualifiedName == "java.lang.Void" || qualifiedName == "kotlin.Unit") null
                    else qualifiedName
                }

            return ClassBinding(
                knownClassType = knownClassType,
                targetTypeName = targetTypeName,
                bindingClassName = bindingClassName,
                packageName = packageName,
                argumentBindings = argumentBindings,
                includeStartForResult = includeStartForResult,
                routerPath = routerPath,
                isParentClass = isParentClass,
                cls = cls
            )
        } catch (e: Exception) {
            logger.error("Error creating ClassBinding for ${classDeclaration.simpleName.asString()}: ${e.message}", classDeclaration)
            return null
        }
    }
}

package launcher.codegeneration

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import launcher.Function
import launcher.MarketViewRoute
import launcher.utils.CONTEXT
import launcher.utils.NOTNULL
import launcher.utils.NULLABLE
import launcher.utils.STRING
import launcher.utils.VIEW
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement


internal class MarketViewRouteGeneration(var all:MutableList<TypeElement>){

    var throwError:((String, Element)->Unit)? = null

    fun brewJava() = JavaFile.builder("com.webull.market.common.base", createStarterSpec(all))
        .addFileComment("Generated code from market View!")
        .build()


    private fun createStarterSpec(all:List<TypeElement>) =  TypeSpec
        .classBuilder("MarketViewRouteFactory")
        .addJavadoc("市场 View 映射\n")
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addAllViewKeyFieldBuild(all)
        .addMethod(moduleCreateViewMethodBuilder(all))
        .build()


    private fun moduleCreateViewMethodBuilder(all:List<TypeElement>):MethodSpec{

        // 创建@NotNull注解的Spec
        val notNull = AnnotationSpec.builder(NOTNULL).build()
        // 创建@Nullable注解的Spec
        val nullable = AnnotationSpec.builder(NULLABLE).build()
        // 构造一个使用@NotNull注解的参数
        val keyParameterSpec = ParameterSpec.builder(STRING, "key")
            .addAnnotation(notNull)
            .build()
        val contextParameterSpec = ParameterSpec.builder(CONTEXT, "context")
            .addAnnotation(notNull)
            .build()


        return MethodSpec.methodBuilder("createView")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(VIEW)
            .addAnnotation(nullable)
            .addParameter(contextParameterSpec)
            .addParameter(keyParameterSpec)
            .addStatement("\$T view = null", VIEW)
            .apply {
                val switchBuilder: CodeBlock.Builder? = CodeBlock.builder()
                switchBuilder?.let {build->
                    build.beginControlFlow("switch (key)")
                    val allViewKeys = mutableListOf<String>()
                    all.forEach {
                        val anno = it.getAnnotation(MarketViewRoute::class.java)
                        val typeName = TypeName.get(it.asType())

                        if (allViewKeys.contains(anno.key)){
                            throwError?.invoke(anno.key,it)
                        }
                        allViewKeys.add(anno.key)
                        build.add("case \"${anno.key}\" :\n")
                            .addStatement("   view = new \$T(context)",typeName)
                            .addStatement("   break")
                    }
                    build.add("default:\n")
                        .addStatement("   break")
                    build.endControlFlow()
                    addCode(build.build())
                }
            }
            .addStatement("return view")
            .build()
    }



    /**
     * 生成所有View的Key 静态属性
     */
    private fun TypeSpec.Builder.addAllViewKeyFieldBuild(all:List<TypeElement>):TypeSpec.Builder {
        all.forEach {element->
            val clsObj = ClassName.get(element)
            val ani = element.getAnnotation(MarketViewRoute::class.java)
            val id = ani.key.takeIf {
                it.isNotEmpty()
            }?:clsObj.canonicalName()
            val fieldSpec = FieldSpec
                .builder(STRING, "VIEW_${id.uppercase()}", Modifier.STATIC, Modifier.FINAL, Modifier.PUBLIC)
                .addJavadoc(ani.desc)
                .initializer("\"$id\"")
                .build()
            addField(fieldSpec)
        }
        return this
    }

}
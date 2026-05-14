package launcher.codegeneration

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import launcher.Function
import launcher.utils.STRING
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement


class FunctionGeneration(var all:MutableList<TypeElement>,var allGroups:MutableList<String>) {

    var throwError:((String,Element)->Unit)? = null

    fun brewJava() = JavaFile.builder("com.webull.functionmap", createStarterSpec(all))
        .addFileComment("Generated code from Function!")
        .build()

    private fun createStarterSpec(all:List<TypeElement>) =  TypeSpec
        .classBuilder("FunctionFactory")
        .addJavadoc("功能地图 映射\n")
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addAllFunctionKeyFieldBuild(all)
        .addMethod(moduleInitAllFunctionMethodBuilder(all))
        .addMethod(GetFunctionIdMehtodBuild())
        .apply {
            allGroups.forEach {
                addMethod(moduleGetGroupFunctionsMethodBuilder(it,all))
            }
        }.build()


    /**
     * 生成所有功能的FunctionID 静态属性 按照FUNCTION_功能全类名_ID生成
     */
    private fun TypeSpec.Builder.addAllFunctionKeyFieldBuild(all:List<TypeElement>):TypeSpec.Builder {
        val allFunction = mutableListOf<String>()
        all.forEach {element->
            val clsObj = ClassName.get(element)
            val clazzName = clsObj.simpleName()
            val ani = element.getAnnotation(Function::class.java)
            val id = ani.functionId.ifEmpty { clazzName.plus("_function") }
            val fieldSpec = FieldSpec
                .builder(STRING, "FUNCTION_${clsObj.simpleName().uppercase()}_ID", Modifier.STATIC, Modifier.FINAL, Modifier.PUBLIC)
                .addJavadoc(ani.desc)
                .initializer("\"$id\"")
                .build()
            addField(fieldSpec)
            if (allFunction.contains(id)){
                throwError?.invoke(id,element)
            }
            allFunction.add(id)
        }
        val className = ClassName.get("java.lang", "Class")
        val list = ClassName.get("java.util", "Map")
        val keyStr = ClassName.get("java.lang", "String")
        val listOfHoverboards: TypeName = ParameterizedTypeName.get(list, keyStr,className)
        val fieldSpec = FieldSpec
            .builder(listOfHoverboards, "functionCacheMap", Modifier.STATIC, Modifier.FINAL, Modifier.PUBLIC)
            .initializer("new java.util.HashMap<>()")
            .build()
        addField(fieldSpec)
        return this
    }

    private fun GetFunctionIdMehtodBuild():MethodSpec {
        val methodSpec =  MethodSpec.methodBuilder("getFunctionId")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(STRING)
            .addParameter(STRING,"clsName")
            .addStatement("String functionId = \"\"")
            .beginControlFlow("if (functionCacheMap.isEmpty())")
            .addStatement("initFunction()")
            .endControlFlow()
            .beginControlFlow("for (String s : functionCacheMap.keySet())")
            .addStatement("java.lang.Class clszz = functionCacheMap.get(s)")
            .beginControlFlow("if (clszz.getCanonicalName().contains(clsName))")
            .addStatement("functionId = s")
            .addStatement("break")
            .endControlFlow()
            .endControlFlow()
            .addStatement("return functionId")
        return methodSpec.build()
    }


    /**
     * 生成 功能ID 对应class的属性关系
     */
    private fun moduleInitAllFunctionMethodBuilder(all:List<TypeElement>):MethodSpec {
        val methodSpec =  MethodSpec.methodBuilder("initFunction")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        methodSpec.beginControlFlow("if (functionCacheMap.isEmpty())")
        all.forEach {
            val clsObj = ClassName.get(it)
            val clazzName = clsObj.canonicalName()
            val id =  "FUNCTION_${clsObj.simpleName().uppercase()}_ID"
            methodSpec.addStatement("functionCacheMap.put($id,${clazzName}.class)")
        }
        methodSpec.endControlFlow()
        return methodSpec.build()
    }

    /**
     * 生成分组
     */
    private fun moduleGetGroupFunctionsMethodBuilder(group:String,all:List<TypeElement>): MethodSpec {
        val className = ClassName.get("java.lang", "Class")
        val list = ClassName.get("java.util", "List")
        val arrayList = ClassName.get("java.util", "ArrayList")
        val listOfHoverboards: TypeName = ParameterizedTypeName.get(list, className)
        return MethodSpec.methodBuilder("init${group}Function")
            .addModifiers(Modifier.PUBLIC).apply {
                addStatement("$listOfHoverboards result = new $arrayList<>()")
                all.forEach {
                    it.getAnnotation(Function::class.java)?.let {
                        if (it.group.contains(group)  && group.isNotEmpty())
                            addStatement("result.add(${it.functionId}})")
                    }
                }
            }.build()
    }
}
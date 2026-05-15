package launcher.wb.mapper.codegen

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSType
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import launcher.wb.mapper.MapperDescriptor
import launcher.wb.mapper.MapperMethodDescriptor
import launcher.wb.mapper.PropertyResolver

/**
 * 赋值代码生成器：负责将 Assignment 转换为 JavaPoet 代码
 */
class AssignmentGenerator(
    private val descriptor: MapperDescriptor,
    private val propertyResolver: PropertyResolver,
    private val logger: KSPLogger
) {

    /**
     * 生成单个赋值语句
     */
    fun generateAssignment(
        methodBuilder: MethodSpec.Builder,
        assignment: Assignment,
        targetContext: TargetContext,
        method: MapperMethodDescriptor,
        usedNames: MutableSet<String>
    ) {
        when (assignment) {
            is PropertyAssignment -> generatePropertyAssignment(methodBuilder, assignment, targetContext, method, usedNames)
            is FieldAssignment -> generateFieldAssignment(methodBuilder, assignment, targetContext, method, usedNames)
            is NestedAssignment -> generateNestedAssignment(methodBuilder, targetContext, assignment, usedNames)
        }
    }

    private fun generatePropertyAssignment(
        methodBuilder: MethodSpec.Builder,
        assignment: PropertyAssignment,
        targetContext: TargetContext,
        method: MapperMethodDescriptor,
        usedNames: MutableSet<String>
    ) {
        if (assignment.expression.startsWith("COLLECTION_MAPPING:")) {
            val parts = assignment.expression.split(":", limit = 3)
            if (parts.size == 3) {
                generateCollectionMappingCodeBlock(methodBuilder, targetContext.varName, assignment.setterName, null, parts[1], parts[2], assignment.expressionType, usedNames)
            }
        } else {
            val needNullCheck = method.needNullCheck ?: descriptor.needNullCheck
            if (needNullCheck && assignment.expressionType != null &&
                !TypeResolver.isPrimitiveType(assignment.expressionType) &&
                !assignment.expression.contains("?")
            ) {
                methodBuilder.beginControlFlow("if (\$L != null)", assignment.expression)
                methodBuilder.addStatement("\$L.\$L(\$L)", targetContext.varName, assignment.setterName, assignment.expression)
                methodBuilder.endControlFlow()
            } else if (assignment.expressionType == null) {
                // expression 映射：与 KAPT 一致，对非基本类型的方法调用结果做 null check
                // 排除：包含三元运算符的表达式（已自行处理 null）、常量
                val needNullCheck = method.needNullCheck ?: descriptor.needNullCheck
                if (needNullCheck && isNullCheckableExpression(assignment.expression)) {
                    methodBuilder.beginControlFlow("if (\$L != null)", assignment.expression)
                    methodBuilder.addStatement("\$L.\$L(\$L)", targetContext.varName, assignment.setterName, assignment.expression)
                    methodBuilder.endControlFlow()
                } else {
                    methodBuilder.addCode(
                        CodeBlock.builder()
                            .add("\$L.\$L(", targetContext.varName, assignment.setterName)
                            .add(assignment.expression)
                            .add(");\n")
                            .build()
                    )
                }
            } else {
                methodBuilder.addStatement("\$L.\$L(\$L)", targetContext.varName, assignment.setterName, assignment.expression)
            }
        }
    }

    private fun generateFieldAssignment(
        methodBuilder: MethodSpec.Builder,
        assignment: FieldAssignment,
        targetContext: TargetContext,
        method: MapperMethodDescriptor,
        usedNames: MutableSet<String>
    ) {
        if (assignment.expression.startsWith("COLLECTION_MAPPING:")) {
            val parts = assignment.expression.split(":", limit = 3)
            if (parts.size == 3) {
                generateCollectionMappingCodeBlock(methodBuilder, targetContext.varName, null, assignment.fieldName, parts[1], parts[2], assignment.expressionType, usedNames)
            }
        } else {
            val needNullCheck = method.needNullCheck ?: descriptor.needNullCheck
            if (needNullCheck && assignment.expressionType != null &&
                !TypeResolver.isPrimitiveType(assignment.expressionType) &&
                !assignment.expression.contains("?")
            ) {
                methodBuilder.beginControlFlow("if (\$L != null)", assignment.expression)
                methodBuilder.addStatement("\$L.\$L = \$L", targetContext.varName, assignment.fieldName, assignment.expression)
                methodBuilder.endControlFlow()
            } else if (assignment.expressionType == null) {
                // expression 映射：与 KAPT 一致，对非基本类型的方法调用结果做 null check
                val needNullCheck = method.needNullCheck ?: descriptor.needNullCheck
                if (needNullCheck && isNullCheckableExpression(assignment.expression)) {
                    methodBuilder.beginControlFlow("if (\$L != null)", assignment.expression)
                    methodBuilder.addStatement("\$L.\$L = \$L", targetContext.varName, assignment.fieldName, assignment.expression)
                    methodBuilder.endControlFlow()
                } else {
                    methodBuilder.addCode(
                        CodeBlock.builder()
                            .add("\$L.\$L = ", targetContext.varName, assignment.fieldName)
                            .add(assignment.expression)
                            .add(";\n")
                            .build()
                    )
                }
            } else {
                methodBuilder.addStatement("\$L.\$L = \$L", targetContext.varName, assignment.fieldName, assignment.expression)
            }
        }
    }

    private fun generateNestedAssignment(
        methodBuilder: MethodSpec.Builder,
        targetContext: TargetContext,
        assignment: NestedAssignment,
        usedNames: MutableSet<String>
    ) {
        val intermediateVarName = TypeResolver.generateUniqueName("${assignment.rootProperty}Obj", usedNames)
        usedNames += intermediateVarName

        val intermediateTypeName = TypeResolver.resolveTypeName(assignment.intermediateType)

        val getter = propertyResolver.readableProperties(targetContext.typeDeclaration)[assignment.rootProperty]
        if (getter != null) {
            val getExpr = if (getter.isFieldAccess) "${targetContext.varName}.${getter.getterName}" else "${targetContext.varName}.${getter.getterName}()"
            methodBuilder.addStatement("\$T \$L = \$L", intermediateTypeName, intermediateVarName, getExpr)
            methodBuilder.beginControlFlow("if (\$L == null)", intermediateVarName)
            methodBuilder.addStatement("\$L = new \$T()", intermediateVarName, intermediateTypeName)
            methodBuilder.endControlFlow()
        } else {
            methodBuilder.addStatement("\$T \$L = new \$T()", intermediateTypeName, intermediateVarName, intermediateTypeName)
        }

        val nestedProperty = assignment.nestedPath.first()
        val nestedSetters = propertyResolver.writeableProperties(assignment.intermediateDeclaration)
        val nestedFields = propertyResolver.writableFields(assignment.intermediateDeclaration)
        val nestedSetter = nestedSetters[nestedProperty]
        val nestedField = nestedFields[nestedProperty]

        when {
            nestedSetter != null -> methodBuilder.addStatement("\$L.\$L(\$L)", intermediateVarName, nestedSetter.setterName, assignment.expression)
            nestedField != null -> methodBuilder.addStatement("\$L.\$L = \$L", intermediateVarName, nestedField.name, assignment.expression)
            else -> logger.error("Cannot find setter or field '$nestedProperty' on intermediate type.", null)
        }

        when {
            assignment.rootSetterName != null -> methodBuilder.addStatement("\$L.\$L(\$L)", targetContext.varName, assignment.rootSetterName, intermediateVarName)
            assignment.rootFieldName != null -> methodBuilder.addStatement("\$L.\$L = \$L", targetContext.varName, assignment.rootFieldName, intermediateVarName)
        }
    }

    /**
     * 判断 expression 是否需要 null check（与 KAPT 行为一致）
     * 需要 null check：简单方法调用且返回引用类型（如 getXxx(source)）
     * 不需要 null check：三元表达式、常量、算术运算、返回基本类型的方法
     */
    private fun isNullCheckableExpression(expression: String): Boolean {
        // 三元表达式自行处理了 null
        if (expression.contains("?") && expression.contains(":")) return false
        // 字符串常量
        if (expression.startsWith("\"")) return false
        // 数字常量
        if (expression.firstOrNull()?.isDigit() == true) return false
        // 包含算术运算符的拼接表达式
        if (expression.contains(" + ") || expression.contains(" - ") || expression.contains(" * ")) return false
        // 静态字段访问（如 TickerBase.TICKER_TYPE_OPTION）
        if (!expression.contains("(") && expression.contains(".") && expression.first().isUpperCase()) return false
        // 方法调用 → 检查是否是返回基本类型的 @MappingIgnore 方法
        if (expression.contains("(") && expression.contains(")")) {
            // 提取方法名（如 "getWeekly(source.optionCycle)" → "getWeekly"）
            val methodName = expression.substringBefore("(").substringAfterLast(".")
            // 检查是否是 @MappingIgnore 方法且返回基本类型
            val ignoredMethod = descriptor.ignoredMethods.firstOrNull { it.simpleName.asString() == methodName }
            if (ignoredMethod != null) {
                val returnType = ignoredMethod.returnType?.resolve()
                val returnTypeName = returnType?.declaration?.qualifiedName?.asString()
                if (returnTypeName != null && TypeResolver.isPrimitiveType(returnType)) {
                    return false // 基本类型不能做 null check
                }
            }
            return true
        }
        return false
    }

    /**
     * 生成集合映射代码块（for 循环）
     */
    fun generateCollectionMappingCodeBlock(
        methodBuilder: MethodSpec.Builder,
        targetVarName: String,
        targetSetterName: String?,
        targetFieldName: String?,
        sourceExpression: String,
        mapperMethodName: String,
        targetType: KSType?,
        usedNames: MutableSet<String>
    ) {
        val tempListVarName = TypeResolver.generateUniqueName("tempList", usedNames)
        usedNames += tempListVarName

        val targetElementType = if (targetType != null) TypeResolver.getCollectionElementType(targetType) else null
        val targetElementTypeName = if (targetElementType != null) TypeResolver.resolveTypeName(targetElementType) else ClassName.OBJECT
        val concreteListType = ClassName.get("java.util", "ArrayList")
        val className = ClassName.get(descriptor.packageName, descriptor.implementationName)

        methodBuilder.beginControlFlow("if (\$L == null)", sourceExpression)
        when {
            targetSetterName != null -> methodBuilder.addStatement("\$L.\$L(null)", targetVarName, targetSetterName)
            targetFieldName != null -> methodBuilder.addStatement("\$L.\$L = null", targetVarName, targetFieldName)
        }
        methodBuilder.nextControlFlow("else")
        methodBuilder.addStatement("\$T<\$T> \$L = new \$T<>()", concreteListType, targetElementTypeName, tempListVarName, concreteListType)
        methodBuilder.beginControlFlow("for (int i = 0; i < \$L.size(); i++)", sourceExpression)
        methodBuilder.addStatement("\$L.add(\$T.\$L(\$L.get(i)))", tempListVarName, className, mapperMethodName, sourceExpression)
        methodBuilder.endControlFlow()
        when {
            targetSetterName != null -> methodBuilder.addStatement("\$L.\$L(\$L)", targetVarName, targetSetterName, tempListVarName)
            targetFieldName != null -> methodBuilder.addStatement("\$L.\$L = \$L", targetVarName, targetFieldName, tempListVarName)
        }
        methodBuilder.endControlFlow()
    }
}

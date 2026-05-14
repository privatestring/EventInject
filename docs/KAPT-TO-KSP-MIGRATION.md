# Launcher KAPT → KSP 迁移知识体系

## 一、项目现状总览

### 1.1 模块结构

| 模块 | 角色 | 技术栈 | 迁移影响 |
|------|------|--------|----------|
| `launcher-joke` | 注解定义 + 接口 | 纯 Java | **无需改动**（注解定义与处理器无关） |
| `launcher-compiler` | 注解处理器 | Kotlin + JavaPoet + AutoService + incap | **核心迁移对象** |
| `app` (业务模块) | 使用方 | Kotlin/Java | 仅改 `kapt` → `ksp` 依赖声明 |

### 1.2 当前处理器注册方式

- `@AutoService(Processor::class)` 自动生成 `META-INF/services/javax.annotation.processing.Processor`
- `@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.AGGREGATING)` 声明增量编译类型
- 手动维护 `META-INF/gradle/incremental.annotation.processors`

### 1.3 处理器入口

`ActivityLauncherProcessor` 是唯一的处理器，内部处理 5 个子系统：
1. Activity/Fragment/BroadcastReceiver/Model 启动器
2. Router 路由
3. Function 功能地图
4. MarketViewRoute 行情视图路由
5. TradeInterface 交易服务工厂 + TradeServiceMaker 聚合接口
6. Mapper 对象映射

---

## 二、KAPT vs KSP 核心差异

### 2.1 架构差异

| 维度 | KAPT | KSP |
|------|------|-----|
| 底层 | 基于 javac APT，Kotlin 先编译为 Java stub | 直接分析 Kotlin 编译器 IR |
| 性能 | 慢（需要生成 Java stub） | 快 2-3x |
| API | `javax.annotation.processing.*` + `javax.lang.model.*` | `com.google.devtools.ksp.*` |
| 类型系统 | `TypeMirror`, `TypeElement`, `Element` | `KSType`, `KSClassDeclaration`, `KSNode` |
| 代码生成 | 写入 `Filer` 或 kapt.kotlin.generated | `CodeGenerator` |
| 增量编译 | 需要 incap 库 | 内置支持 |
| Kotlin 特性 | 有限（通过 stub） | 完整（suspend, sealed, value class 等） |

### 2.2 API 映射表

| KAPT (javax.lang.model) | KSP (com.google.devtools.ksp) |
|--------------------------|-------------------------------|
| `ProcessingEnvironment` | `SymbolProcessorEnvironment` |
| `AbstractProcessor` | `SymbolProcessor` |
| `RoundEnvironment` | `Resolver` |
| `TypeElement` | `KSClassDeclaration` |
| `ExecutableElement` | `KSFunctionDeclaration` |
| `VariableElement` | `KSPropertyDeclaration` / `KSValueParameter` |
| `TypeMirror` | `KSType` |
| `DeclaredType` | `KSType` (with `declaration`) |
| `Element.getAnnotation(X::class.java)` | `KSAnnotated.getAnnotationsByType(X::class)` |
| `processingEnv.elementUtils` | `resolver` |
| `processingEnv.typeUtils` | `KSType` 自带方法 |
| `processingEnv.filer` | `environment.codeGenerator` |
| `processingEnv.messager` | `environment.logger` |
| `Messager.printMessage(Kind.ERROR, ...)` | `logger.error(...)` |
| `roundEnv.getElementsAnnotatedWith(X)` | `resolver.getSymbolsWithAnnotation("pkg.X")` |
| `element.enclosingElement` | `declaration.parentDeclaration` |
| `element.enclosedElements` | `declaration.declarations` |
| `typeElement.interfaces` | `classDeclaration.superTypes` |
| `typeElement.superclass` | `classDeclaration.superTypes` |
| `typeUtils.isSubtype(a, b)` | `a.isAssignableFrom(b)` (注意方向相反) |
| `typeUtils.isSameType(a, b)` | `a == b` 或 `a.isAssignableFrom(b) && b.isAssignableFrom(a)` |
| `typeUtils.asElement(type)` | `type.declaration` |
| `elementUtils.getPackageOf(e)` | `declaration.packageName` |
| `elementUtils.getAllMembers(type)` | `classDeclaration.getAllFunctions()` / `getAllProperties()` |
| `element.modifiers.contains(Modifier.PRIVATE)` | `declaration.modifiers.contains(Modifier.PRIVATE)` |
| `element.kind == ElementKind.INTERFACE` | `declaration.classKind == ClassKind.INTERFACE` |
| `MirroredTypeException` 技巧获取注解中的 Class | `annotation.arguments.find { it.name == "value" }?.value as KSType` |


### 2.3 注解中获取 Class 类型的差异

**KAPT 方式（当前代码中大量使用）：**
```kotlin
// 通过 MirroredTypeException 获取注解中的 Class 值
fun getAnnotationInterfaceType(element: TypeElement): TypeMirror? {
    try {
        element.getAnnotation(TradeInterface::class.java).value
    } catch (mte: MirroredTypeException) {
        return mte.typeMirror
    }
    return null
}
```

**KSP 方式：**
```kotlin
// 直接从注解参数中获取 KSType
fun getAnnotationInterfaceType(declaration: KSClassDeclaration): KSType? {
    val annotation = declaration.getAnnotationsByType(TradeInterface::class).firstOrNull()
    // 或者使用更底层的方式：
    val anno = declaration.annotations.firstOrNull {
        it.shortName.asString() == "TradeInterface"
    }
    return anno?.arguments?.firstOrNull { it.name?.asString() == "value" }?.value as? KSType
}
```

### 2.4 增量编译差异

**KAPT：**
- 需要 `net.ltgt.gradle.incap` 库
- `@IncrementalAnnotationProcessor(AGGREGATING)` 注解
- `META-INF/gradle/incremental.annotation.processors` 文件

**KSP：**
- 内置增量编译支持
- 通过 `Dependencies` 参数声明依赖关系
- `Dependencies(aggregating = true, ...)` 或 `Dependencies(aggregating = false, sources)`

---

## 三、当前代码中需要迁移的核心 API 使用点

### 3.1 ProcessingEnvironment 使用

当前代码中 `processingEnv` 的使用：
- `processingEnv.messager` → 日志输出
- `processingEnv.filer` → 代码写入
- `processingEnv.elementUtils` → 获取包名、获取所有成员
- `processingEnv.typeUtils` → 类型比较、类型擦除
- `processingEnv.options["module_name"]` → 获取编译参数

**KSP 对应：**
```kotlin
class LauncherProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return LauncherProcessor(
            logger = environment.logger,
            codeGenerator = environment.codeGenerator,
            options = environment.options  // 对应 processingEnv.options
        )
    }
}
```

### 3.2 RoundEnvironment 使用

当前代码：
```kotlin
roundEnv.getElementsAnnotatedWith(Boom::class.java)
roundEnv.getElementsAnnotatedWith(Router::class.java)
roundEnv.rootElements  // 用于 TradeServiceAggregator 扫描
```

**KSP 对应：**
```kotlin
override fun process(resolver: Resolver): List<KSAnnotated> {
    val boomSymbols = resolver.getSymbolsWithAnnotation("launcher.Boom")
    val routerSymbols = resolver.getSymbolsWithAnnotation("launcher.Router")
    // rootElements 对应：resolver.getAllFiles() 或 resolver.getNewFiles()
    
    // 返回未处理完的符号（下一轮继续处理）
    return unprocessed
}
```

### 3.3 TypeMirror / TypeElement 操作

当前代码中的高频操作：

```kotlin
// 1. 类型继承检查
fun TypeMirror.isSubtypeOfType(vararg otherType: String): Boolean

// 2. 获取泛型参数
(type as DeclaredType).typeArguments

// 3. 类型比较
typeUtils.isSameType(a, b)
typeUtils.isAssignable(a, b)
typeUtils.erasure(type)

// 4. 获取所有成员方法/字段
elementUtils.getAllMembers(typeElement)
```

**KSP 对应：**
```kotlin
// 1. 类型继承检查
fun KSClassDeclaration.isSubtypeOf(superType: String): Boolean {
    return superTypes.any { 
        it.resolve().declaration.qualifiedName?.asString() == superType ||
        (it.resolve().declaration as? KSClassDeclaration)?.isSubtypeOf(superType) == true
    }
}

// 2. 获取泛型参数
val typeArgs = ksType.arguments  // List<KSTypeArgument>
val elementType = typeArgs.first().type?.resolve()

// 3. 类型比较
typeA.isAssignableFrom(typeB)  // 注意：方向与 KAPT 相反！
typeA == typeB  // isSameType
ksType.starProjection()  // 类似 erasure

// 4. 获取所有成员
classDeclaration.getAllFunctions()
classDeclaration.getAllProperties()
```

### 3.4 代码生成（JavaPoet 保留）

当前使用 JavaPoet 生成 Java 代码。KSP 迁移后 **JavaPoet 可以继续使用**，只需改变写入方式：

**KAPT：**
```kotlin
javaFile.writeTo(filer)  // javax.annotation.processing.Filer
```

**KSP：**
```kotlin
val file = codeGenerator.createNewFile(
    dependencies = Dependencies(aggregating = true, *sourceFiles.toTypedArray()),
    packageName = "com.webull.functionmap",
    fileName = "FunctionFactory"
)
file.writer().use { writer ->
    javaFile.writeTo(writer)
}
```

也可以选择迁移到 KotlinPoet（生成 Kotlin 代码），但不是必须的。

---

## 四、各子系统迁移要点

### 4.1 Activity/Fragment 启动器

**涉及文件：**
- `ClassBindingFactory.kt` — 解析注解、判断类型
- `ClassBinding.kt` — 数据模型
- `ActivityGeneration.kt` / `FragmentGeneration.kt` / `IntentBinding.kt` 等 — 代码生成
- `ArgumentFactory.kt` / `ArgumentBinding.kt` — 参数解析
- `ParamType.kt` — 类型映射
- `FieldAccessor.kt` — 字段访问方式判断

**迁移关键点：**

1. **类型判断** — `KnownClassType.getByType()` 使用 `isSubtypeOfType` 判断是 Activity/Fragment/etc
   - KSP: 使用 `KSClassDeclaration.superTypes` 递归检查

2. **字段扫描** — `typeElement.enclosedElements.filter { it.getAnnotation(Boom::class.java) != null }`
   - KSP: `classDeclaration.getAllProperties().filter { it.hasAnnotation("launcher.Boom") }`

3. **字段类型判断** — `ParamType.fromType(typeMirror)` 通过 TypeKind 和类型名判断
   - KSP: `KSType.declaration.qualifiedName` + `KSType.nullability`

4. **字段可访问性** — `FieldAccessor` 检查 private/setter
   - KSP: `KSPropertyDeclaration.modifiers` + `KSPropertyDeclaration.setter`

5. **可选参数组合** — `createSublists` 生成重载方法
   - 逻辑不变，只是输入数据结构从 `ArgumentBinding` 适配

### 4.2 Router 路由

**迁移关键点：**

1. **注解值获取** — `@Router(routerPath, cls)`
   - `cls` 是 `Class<?>` 类型，KAPT 用 `MirroredTypeException` 获取
   - KSP: 直接从 `KSAnnotation.arguments` 获取 `KSType`

2. **参数类型限制** — 当前只支持 String，编译时报错
   - KSP 中同样可以通过 `logger.error()` 报错

3. **desc 必填校验** — 跨模块参数必须有描述
   - 逻辑不变

### 4.3 Function 功能地图

**迁移关键点：**

1. **收集所有 @Function 注解的类** — 简单的符号收集
2. **重复 ID 检测** — 逻辑不变
3. **分组生成** — 逻辑不变

### 4.4 MarketViewRoute

**迁移关键点：**

1. **收集所有 @MarketViewRoute 注解的类**
2. **生成 switch-case 工厂方法**
3. **重复 key 检测**

### 4.5 TradeInterface + TradeServiceMaker

**迁移关键点：**

1. **注解中的 Class 值获取** — `@TradeInterface(value = IXxx.class)`
   - KAPT: `MirroredTypeException`
   - KSP: `annotation.arguments["value"] as KSType`

2. **包扫描** — `TradeServiceAggregatorGeneration.findAllSubInterfaces()`
   - KAPT: 遍历 `roundEnv.rootElements` + `elementUtils.getPackageElement()`
   - KSP: `resolver.getAllFiles()` 遍历 + `resolver.getDeclarationsFromPackage(packageName)`

3. **继承关系分析** — `filterTopLevelInterfaces()` 判断接口继承层级
   - KSP: `KSClassDeclaration.superTypes` 递归

### 4.6 Mapper 对象映射

**这是最复杂的子系统，迁移工作量最大。**

**涉及文件：**
- `MapperUtils.kt` — 核心逻辑（构建描述符、解析映射规则）
- `MapperGeneration.kt` — 代码生成（1970 行）
- `PropertyResolver.kt` — 属性解析（getter/setter/field）
- `MapperModels.kt` — 数据模型

**迁移关键点：**

1. **属性解析** — `PropertyResolver` 通过 `getAllMembers()` 收集 getter/setter/field
   - KSP: `KSClassDeclaration.getAllFunctions()` + `getAllProperties()`
   - 注意：KSP 中 Kotlin 属性天然有 getter/setter，不需要通过方法名推断

2. **方法签名分析** — 参数类型、返回类型、@MappingTarget 检测
   - KSP: `KSFunctionDeclaration.parameters` / `returnType`

3. **类型兼容性检查** — `isAssignable()`, `isCollectionType()`, `getCollectionElementType()`
   - KSP: `KSType.isAssignableFrom()` (方向相反!)
   - 集合类型: `ksType.declaration.qualifiedName?.asString()?.startsWith("java.util.List")`

4. **Kotlin 源文件检测** — 当前通过 `kotlin.Metadata` 注解判断
   - KSP: `KSDeclaration.origin == Origin.KOTLIN` 直接判断

5. **DefaultImpls 调用** — Kotlin 接口默认方法
   - KSP 中可以直接知道方法是否有 body: `KSFunctionDeclaration.isAbstract`

6. **表达式解析** — `buildGetterChain()` 构建 `source.getXxx().getYyy()` 链
   - 逻辑不变，只是类型查询 API 变化


---

## 五、迁移步骤规划

### Phase 1: 基础设施搭建

1. **新建 `launcher-compiler-ksp` 模块**（与旧模块并存，渐进迁移）
2. **添加 KSP 依赖：**
   ```groovy
   // launcher-compiler-ksp/build.gradle
   plugins {
       id 'org.jetbrains.kotlin.jvm'
   }
   
   dependencies {
       implementation 'com.google.devtools.ksp:symbol-processing-api:1.9.22-1.0.17'
       implementation 'com.squareup:javapoet:1.13.0'  // 继续使用 JavaPoet
       implementation project(':launcher-joke')  // 注解定义
   }
   ```
3. **创建 `SymbolProcessorProvider` 入口**
4. **注册 SPI：** `META-INF/services/com.google.devtools.ksp.processing.SymbolProcessorProvider`

### Phase 2: 工具层迁移

1. **迁移 `Logger`** → 使用 `KSPLogger`
2. **迁移 `PropertyResolver`** → 基于 `KSClassDeclaration` 重写
3. **迁移类型工具** — `isSubtypeOfType`, `ParamType.fromType()`, `isAssignable()`
4. **迁移 `FieldAccessor`** → 基于 `KSPropertyDeclaration`

### Phase 3: 子系统逐个迁移（建议顺序）

| 顺序 | 子系统 | 复杂度 | 原因 |
|------|--------|--------|------|
| 1 | Function 功能地图 | ⭐ | 最简单，仅收集注解 + 生成工厂 |
| 2 | MarketViewRoute | ⭐ | 同上，简单收集 + switch-case |
| 3 | TradeInterface | ⭐⭐ | 涉及注解中 Class 值获取 |
| 4 | TradeServiceMaker | ⭐⭐⭐ | 涉及包扫描 + 继承分析 |
| 5 | Activity/Fragment 启动器 | ⭐⭐⭐ | 核心功能，类型判断 + 参数解析 |
| 6 | Router 路由 | ⭐⭐ | 依赖启动器的 ClassBinding |
| 7 | Mapper 对象映射 | ⭐⭐⭐⭐⭐ | 最复杂，1970 行代码生成 |

### Phase 4: 业务模块切换

1. 业务模块 `build.gradle` 中 `kapt project(':launcher-compiler')` → `ksp project(':launcher-compiler-ksp')`
2. 验证生成代码一致性
3. 移除旧 `launcher-compiler` 模块

---

## 六、KSP Processor 骨架代码

```kotlin
// LauncherSymbolProcessorProvider.kt
package launcher

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*

class LauncherSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return LauncherSymbolProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
            options = environment.options
        )
    }
}

// LauncherSymbolProcessor.kt
class LauncherSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val unprocessed = mutableListOf<KSAnnotated>()

        // 1. Activity/Fragment 启动器
        processLauncher(resolver, unprocessed)

        // 2. Function 功能地图
        processFunction(resolver, unprocessed)

        // 3. MarketViewRoute
        processMarketViewRoute(resolver, unprocessed)

        // 4. TradeInterface
        processTradeInterface(resolver, unprocessed)

        // 5. TradeServiceMaker
        processTradeServiceMaker(resolver, unprocessed)

        // 6. Mapper
        processMapper(resolver, unprocessed)

        return unprocessed
    }

    private fun processLauncher(resolver: Resolver, unprocessed: MutableList<KSAnnotated>) {
        val boomSymbols = resolver.getSymbolsWithAnnotation("launcher.Boom")
        val makeResultSymbols = resolver.getSymbolsWithAnnotation("launcher.MakeResult")
        val routerSymbols = resolver.getSymbolsWithAnnotation("launcher.Router")
        val parentClsSymbols = resolver.getSymbolsWithAnnotation("launcher.ParentCls")

        // 收集需要处理的类
        val classesToProcess = mutableSetOf<KSClassDeclaration>()

        boomSymbols.forEach { symbol ->
            if (symbol is KSPropertyDeclaration) {
                val parent = symbol.parentDeclaration as? KSClassDeclaration
                if (parent != null) classesToProcess += parent
                else unprocessed += symbol
            }
        }
        makeResultSymbols.forEach { symbol ->
            if (symbol is KSClassDeclaration) classesToProcess += symbol
            else unprocessed += symbol
        }
        // ... 类似处理 Router, ParentCls

        // 处理每个类
        classesToProcess.forEach { classDecl ->
            processTarget(classDecl)
        }
    }

    private fun processTarget(classDecl: KSClassDeclaration) {
        // 判断类型
        val classType = determineClassType(classDecl)
        // 收集 @Boom 字段
        val arguments = collectArguments(classDecl)
        // 生成代码
        val javaFile = generateLauncherCode(classDecl, classType, arguments)
        // 写入
        writeJavaFile(javaFile, classDecl)
    }

    private fun determineClassType(classDecl: KSClassDeclaration): KnownClassType {
        return when {
            classDecl.isSubtypeOf("android.app.Activity") -> KnownClassType.Activity
            classDecl.isSubtypeOf("androidx.fragment.app.Fragment") -> KnownClassType.Fragment
            classDecl.isSubtypeOf("android.app.Fragment") -> KnownClassType.Fragment
            classDecl.isSubtypeOf("android.content.BroadcastReceiver") -> KnownClassType.BroadcastReceiver
            else -> KnownClassType.Model
        }
    }

    // 辅助扩展函数
    private fun KSClassDeclaration.isSubtypeOf(superTypeName: String): Boolean {
        return superTypes.any { superTypeRef ->
            val resolved = superTypeRef.resolve()
            val qualifiedName = resolved.declaration.qualifiedName?.asString()
            qualifiedName == superTypeName ||
                (resolved.declaration as? KSClassDeclaration)?.isSubtypeOf(superTypeName) == true
        }
    }

    private fun writeJavaFile(javaFile: com.squareup.javapoet.JavaFile, originDecl: KSClassDeclaration) {
        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(aggregating = true, originDecl.containingFile!!),
            packageName = javaFile.packageName,
            fileName = javaFile.typeSpec.name,
            extensionName = "java"
        )
        file.writer().use { writer ->
            javaFile.writeTo(writer)
        }
    }
}
```

---

## 七、关键迁移难点与解决方案

### 7.1 `MirroredTypeException` 技巧替换

**问题：** 当前代码中有 3 处使用此技巧获取注解中的 `Class<?>` 值：
- `ServiceUtil.getBaseInterfaceType()` — `@TradeServiceMaker.baseInterface`
- `ServiceUtil.getAdditionalInterfaceTypes()` — `@TradeServiceMaker.additionalInterfaces`
- `ServiceUtil.getAnnotationInterfaceType()` — `@TradeInterface.value`
- `ClassBindingFactory.getAnnotationClassValue<Router> { cls }` — `@Router.cls`

**KSP 解决方案：**
```kotlin
fun KSClassDeclaration.getAnnotationClassValue(
    annotationName: String,
    argumentName: String
): KSType? {
    val annotation = annotations.firstOrNull {
        it.shortName.asString() == annotationName
    } ?: return null
    return annotation.arguments.firstOrNull {
        it.name?.asString() == argumentName
    }?.value as? KSType
}

// 使用
val interfaceType = classDecl.getAnnotationClassValue("TradeInterface", "value")
val baseInterface = classDecl.getAnnotationClassValue("TradeServiceMaker", "baseInterface")
```

### 7.2 包扫描（TradeServiceAggregator）

**问题：** 当前通过 `roundEnv.rootElements` + `elementUtils.getPackageElement()` 扫描指定包下的接口。

**KSP 解决方案：**
```kotlin
// KSP 提供了直接按包名获取声明的 API
fun findAllSubInterfaces(
    resolver: Resolver,
    baseInterfaceType: KSType,
    scanPackages: List<String>
): List<KSClassDeclaration> {
    return scanPackages.flatMap { packageName ->
        resolver.getDeclarationsFromPackage(packageName)
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.INTERFACE }
            .filter { it.isSubtypeOf(baseInterfaceType) }
    }.distinctBy { it.qualifiedName?.asString() }
}
```

**注意：** `getDeclarationsFromPackage` 只返回直接在该包下的声明，不包含子包。需要递归或列举所有子包。

### 7.3 PropertyResolver 重写

**问题：** 当前 `PropertyResolver` 通过 `getAllMembers()` 收集 getter/setter 方法，用方法名前缀（get/set/is）推断属性名。

**KSP 解决方案：**
```kotlin
class KspPropertyResolver(private val resolver: Resolver) {

    fun readableProperties(type: KSClassDeclaration?): Map<String, KSFunctionDeclaration> {
        if (type == null) return emptyMap()
        val map = linkedMapOf<String, KSFunctionDeclaration>()
        
        type.getAllFunctions().forEach { func ->
            if (func.parameters.isEmpty() && !func.modifiers.contains(Modifier.PRIVATE)) {
                val name = func.simpleName.asString()
                val property = when {
                    name.startsWith("get") && name.length > 3 -> decap(name.substring(3))
                    name.startsWith("is") && name.length > 2 -> decap(name.substring(2))
                    else -> null
                }
                if (property != null) map[property] = func
            }
        }
        return map
    }

    fun writableProperties(type: KSClassDeclaration?): Map<String, KSFunctionDeclaration> {
        if (type == null) return emptyMap()
        val map = linkedMapOf<String, KSFunctionDeclaration>()
        
        type.getAllFunctions().forEach { func ->
            val name = func.simpleName.asString()
            if (func.parameters.size == 1 && name.startsWith("set") && name.length > 3) {
                val property = decap(name.substring(3))
                map[property] = func
            }
        }
        return map
    }

    fun writableFields(type: KSClassDeclaration?): Map<String, KSPropertyDeclaration> {
        if (type == null) return emptyMap()
        return type.getAllProperties()
            .filter { !it.modifiers.contains(Modifier.PRIVATE) && it.isMutable }
            .associateBy { it.simpleName.asString() }
    }
}
```

### 7.4 类型系统方向差异

**关键注意：** `isAssignable` 方向相反！

```kotlin
// KAPT: typeUtils.isAssignable(source, target) → source 可以赋值给 target
// KSP:  target.isAssignableFrom(source) → source 可以赋值给 target

// 迁移时需要交换调用方向：
// KAPT: typeUtils.isAssignable(primarySource.type, paramType)
// KSP:  paramType.isAssignableFrom(primarySource.type)
```

### 7.5 代码生成输出

**问题：** 当前生成 Java 代码到 `filer`。KSP 的 `CodeGenerator` 默认生成到 `build/generated/ksp/` 目录。

**注意事项：**
- KSP 生成的文件扩展名需要显式指定 `extensionName = "java"`
- `Dependencies` 参数决定增量编译行为
- 如果生成的文件依赖多个源文件，使用 `Dependencies(aggregating = true, *files)`

### 7.6 多轮处理

**KAPT：** 每轮 `process()` 都会被调用，直到没有新的注解被生成。

**KSP：** `process()` 返回 `List<KSAnnotated>` 表示未处理完的符号，KSP 会在下一轮重新传入。如果返回空列表，处理结束。

```kotlin
override fun process(resolver: Resolver): List<KSAnnotated> {
    val symbols = resolver.getSymbolsWithAnnotation("launcher.Boom")
    val unprocessed = symbols.filter { !it.validate() }.toList()
    val valid = symbols.filter { it.validate() }
    
    // 处理 valid 符号...
    
    return unprocessed  // 下一轮继续处理
}
```

---

## 八、依赖变更清单

### 8.1 移除的依赖

```groovy
// 不再需要
implementation 'com.google.auto.service:auto-service:1.0-rc6'
kapt 'com.google.auto.service:auto-service:1.0-rc6'
implementation 'net.ltgt.gradle.incap:incap:1.0.0'
annotationProcessor 'net.ltgt.gradle.incap:incap-processor:1.0.0'
```

### 8.2 新增的依赖

```groovy
// KSP API
implementation 'com.google.devtools.ksp:symbol-processing-api:1.9.22-1.0.17'
// JavaPoet 保留
implementation 'com.squareup:javapoet:1.13.0'
// 注解定义保留
implementation project(':launcher-joke')
```

### 8.3 根 build.gradle 变更

```groovy
// 添加 KSP 插件
buildscript {
    dependencies {
        classpath 'com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:1.9.22-1.0.17'
    }
}
```

### 8.4 业务模块 build.gradle 变更

```groovy
// 旧
apply plugin: 'kotlin-kapt'
dependencies {
    kapt project(':launcher-compiler')
}

// 新
apply plugin: 'com.google.devtools.ksp'
dependencies {
    ksp project(':launcher-compiler-ksp')
}
```

---

## 九、移除的文件

迁移完成后可删除：
- `META-INF/services/javax.annotation.processing.Processor`
- `META-INF/gradle/incremental.annotation.processors`
- `@AutoService` 注解
- `@IncrementalAnnotationProcessor` 注解

新增：
- `META-INF/services/com.google.devtools.ksp.processing.SymbolProcessorProvider`

---

## 十、验证策略

1. **生成代码对比** — 对同一组注解，KAPT 和 KSP 生成的代码应完全一致
2. **编译通过** — 业务模块使用 KSP 生成的代码能正常编译
3. **运行时行为** — 启动器、路由、Mapper 等功能运行正常
4. **增量编译** — 修改单个文件后，只重新生成受影响的代码
5. **编译速度** — KSP 应比 KAPT 快 2-3x

---

## 十一、风险点

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| KSP 不支持 Java 源文件中的注解 | `Test2.java` 等 Java 文件的注解可能无法被 KSP 处理 | KSP 1.0+ 已支持 Java 源文件，但需验证 |
| `getAllMembers()` 行为差异 | 父类私有字段/方法的可见性不同 | 对比测试 |
| 泛型类型擦除行为差异 | 集合元素类型获取方式不同 | 单元测试覆盖 |
| 多模块增量编译 | 跨模块注解变更的传播 | 使用 `Dependencies(aggregating = true)` |
| `@Repeatable` 注解处理 | `@Mapping` 是 `@Repeatable`，KSP 处理方式不同 | 使用 `getAnnotationsByType` 或手动展开 |

---

## 十二、参考资源

- [KSP 官方文档](https://kotlinlang.org/docs/ksp-overview.html)
- [KSP API Reference](https://google.github.io/ksp/api/symbol-processing-api/)
- [KAPT → KSP 迁移指南](https://kotlinlang.org/docs/ksp-why-ksp.html)
- [JavaPoet + KSP 集成示例](https://github.com/google/ksp/tree/main/examples)

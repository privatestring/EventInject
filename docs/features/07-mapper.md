# 功能七：Mapper 对象映射

## 1. 功能概述

类似 MapStruct 的编译时对象映射框架。为标注 `@Mapper` 的接口生成静态映射方法实现类（`XxxImpl`），支持自动同名映射、显式映射、常量映射、表达式映射、集合映射、嵌套对象映射、继承配置、生命周期钩子等。生成的代码全部为静态方法，无需实例化。

---

## 2. 涉及源文件

### 注解定义（launcher-joke 模块）

| 文件 | 职责 |
|------|------|
| `mapper/Mapper.java` | Mapper 接口注解 |
| `mapper/Mapping.java` | 字段映射规则注解（@Repeatable） |
| `mapper/Mappings.java` | @Mapping 容器注解 |
| `mapper/MappingTarget.java` | 更新目标参数标记 |
| `mapper/MappingConfig.java` | 映射配置（空值检查等） |
| `mapper/InheritConfiguration.java` | 继承其他方法配置 |
| `mapper/BeforeMapping.java` | 映射前生命周期钩子 |
| `mapper/AfterMapping.java` | 映射后生命周期钩子 |
| `mapper/MappingIgnore.java` | 标记不生成实现的辅助方法 |

### 处理器（launcher-compiler 模块）

| 文件 | 职责 |
|------|------|
| `launcher/ActivityLauncherProcessor.kt` | 入口，`processMapper()` 方法 |
| `launcher/mapper/MapperUtils.kt` | 核心逻辑：构建描述符、解析映射规则 |
| `launcher/mapper/MapperModels.kt` | 数据模型定义 |
| `launcher/mapper/PropertyResolver.kt` | 属性解析（getter/setter/field） |
| `launcher/codegeneration/MapperGeneration.kt` | 代码生成（1970 行） |

---

## 3. 注解详细定义

### 3.1 `@Mapper`

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Mapper {
    Class<?>[] uses() default {};           // 预留兼容
    String componentModel() default "";     // 预留兼容
    String implementationSuffix() default "Impl";  // 生成类后缀
}
```

### 3.2 `@Mapping`

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
@Repeatable(Mappings.class)
public @interface Mapping {
    String source() default "";      // 源属性路径
    String target();                 // 目标属性路径（必填）
    boolean ignore() default false;  // 是否忽略
    String constant() default "";    // 常量值
    String expression() default "";  // Java 表达式，格式 "java(...)"
}
```

### 3.3 `@MappingTarget`

```java
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.CLASS)
public @interface MappingTarget {}
```

### 3.4 `@MappingConfig`

```java
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface MappingConfig {
    boolean isNeedNullCheck() default true;
}
```

### 3.5 `@InheritConfiguration`

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface InheritConfiguration {
    String name();  // 要继承配置的方法名
}
```

### 3.6 `@BeforeMapping` / `@AfterMapping`

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface BeforeMapping {}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface AfterMapping {}
```

### 3.7 `@MappingIgnore`

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface MappingIgnore {}
```

---

## 4. 处理流程详解

### 4.1 入口

```kotlin
private fun processMapper(env: RoundEnvironment) {
    val mapperElements = env.getElementsAnnotatedWith(Mapper::class.java)
    mapperElements.forEach { element ->
        if (element is TypeElement) {
            runCatching { MapperUtils.handleMapper(element, processingEnv, propertyResolver, filer) }
                .onFailure { error(element, it.message ?: "Unknown error") }
        }
    }
}
```

### 4.2 MapperUtils.handleMapper() 流程

```
1. 验证：必须是接口或抽象类
2. buildMapperDescriptor():
   a. 读取 @Mapper 注解，获取 implementationSuffix
   b. 读取 @MappingConfig 注解，获取 needNullCheck
   c. 确定包名和实现类名
   d. 扫描所有方法，过滤出可处理的抽象方法
   e. 对每个方法调用 buildMethodDescriptor()
   f. 收集 @BeforeMapping / @AfterMapping / @MappingIgnore 方法
   g. 检测 Kotlin 源文件（kotlin.Metadata 注解）
   h. 解析 @InheritConfiguration 继承关系
3. MapperGeneration(descriptor).brewJava().writeTo(filer)
```

### 4.3 方法过滤规则（isProcessableMethod）

排除以下方法：
- `@BeforeMapping` 方法
- `@AfterMapping` 方法
- `@MappingIgnore` 方法
- `static` 方法
- `default` 方法（Java 接口默认方法）
- 有 `defaultValue` 的方法

### 4.4 buildMethodDescriptor() 流程

```
1. 解析所有参数 → ParameterDescriptor
2. 查找 @MappingTarget 参数（最多一个）
3. 确定 primarySource（第一个非 @MappingTarget 参数）
4. 验证：必须有源参数
5. 验证：非更新方法不能返回 void
6. 收集 @Mapping 注解 → MappingSpec 列表
7. 检查 @InheritConfiguration
8. 检查方法级 @MappingConfig
```

### 4.5 映射规则解析（resolveInheritedMappings）

```
对每个方法：
  如果有 @InheritConfiguration(name = "otherMethod"):
    递归获取 otherMethod 的映射规则
    合并：继承规则 + 当前方法规则
    同一 target 取最后一个（当前方法优先）
  否则：
    直接使用当前方法的 @Mapping 规则
```

循环继承检测：使用 `visiting` Set 追踪正在解析的方法链。

---

## 5. 属性解析（PropertyResolver）

### 5.1 可读属性（getter）

```kotlin
fun readableProperties(type: TypeElement?): Map<String, ExecutableElement>
```

扫描 `getAllMembers()` 中的无参方法：
- `getXxx()` → 属性名 `xxx`
- `isXxx()` → 属性名 `xxx`
- 优先使用 `getXxx` 而非 `isXxx`

### 5.2 可写属性（setter）

```kotlin
fun writeableProperties(type: TypeElement?): Map<String, ExecutableElement>
```

扫描 `getAllMembers()` 中的单参数方法：
- `setXxx(value)` → 属性名 `xxx`

### 5.3 可写字段

```kotlin
fun writableFields(type: TypeElement?): Map<String, VariableElement>
```

条件：非 static、非 final、非 private、且没有对应的 setter 方法。

### 5.4 优先级

写入时：setter 优先于字段直接赋值。

---

## 6. 代码生成详解（MapperGeneration）

### 6.1 生成类结构

```java
public final class OrderMapperImpl {
    // 静态映射方法
    public static OrderEntity toEntity(OrderDto dto) { ... }
    public static void updateEntity(OrderDto dto, OrderEntity entity) { ... }
    public static List<OrderEntity> toEntityList(List<OrderDto> dtos) { ... }
    
    // 静态生命周期方法
    public static void beforeMapping(OrderDto source, OrderEntity target) { ... }
    public static void afterMapping(OrderDto source, OrderEntity target) { ... }
    
    // 静态辅助方法（@MappingIgnore 中被 expression 引用的）
    public static String formatPrice(double price) { ... }
}
```

### 6.2 创建方法生成模板

```java
public static OrderEntity toEntity(OrderDto dto) {
    // 1. Source 空值检查
    if (dto == null) {
        return null;
    }
    
    // 2. 创建目标对象
    OrderEntity target = new OrderEntity();
    
    // 3. @BeforeMapping
    OrderMapperImpl.beforeMapping(dto, target);
    
    // 4. 字段映射
    if (dto.getUserName() != null) {
        target.setName(dto.getUserName());
    }
    target.setStatus("PENDING");
    // ... 更多字段
    
    // 5. @AfterMapping
    OrderMapperImpl.afterMapping(dto, target);
    
    // 6. 返回
    return target;
}
```

### 6.3 更新方法生成模板

```java
public static void updateEntity(OrderDto dto, OrderEntity entity) {
    // 1. Target 非空校验
    if (entity == null) {
        throw new IllegalArgumentException("@MappingTarget parameter entity must not be null");
    }
    
    // 2. Source 空值检查（如果返回类型非 void）
    // ...
    
    // 3. @BeforeMapping
    // 4. 字段映射
    // 5. @AfterMapping
}
```

### 6.4 集合互转方法生成模板

```java
public static List<OrderEntity> toEntityList(List<OrderDto> dtos) {
    if (dtos == null) {
        return null;
    }
    ArrayList<OrderEntity> tempList = new ArrayList<>();
    for (int i = 0; i < dtos.size(); i++) {
        tempList.add(OrderMapperImpl.toEntity(dtos.get(i)));
    }
    return tempList;
}
```

---

## 7. 映射表达式解析

### 7.1 自动同名映射

源和目标有相同属性名且类型兼容时自动映射：
```java
// 源有 getName()，目标有 setName() → 自动映射
target.setName(dto.getName());
```

### 7.2 显式 source 映射

```java
// @Mapping(source = "userName", target = "name")
target.setName(dto.getUserName());

// @Mapping(source = "address.city", target = "city")
target.setCity(dto.getAddress().getCity());

// @Mapping(source = "dto.userName", target = "name") — 指定参数名
target.setName(dto.getUserName());
```

### 7.3 常量映射

```java
// @Mapping(target = "status", constant = "\"PENDING\"")
target.setStatus("PENDING");
```

### 7.4 Expression 映射

```java
// @Mapping(target = "fullName", expression = "java(source.getFirst() + \" \" + source.getLast())")
target.setFullName(source.getFirst() + " " + source.getLast());

// @Mapping(target = "time", expression = "java(System.currentTimeMillis())")
target.setTime(System.currentTimeMillis());

// @Mapping(target = "price", expression = "java(formatPrice(source.getPrice()))")
target.setPrice(OrderMapperImpl.formatPrice(source.getPrice()));
```

Expression 处理规则：
- 移除 `java(...)` 包装
- 直接作为代码插入（不进行类型检查）
- 可以引用方法参数名和 `@MappingIgnore` 方法

### 7.5 忽略映射

```java
// @Mapping(target = "createTime", ignore = true)
// 不生成任何代码
```

---

## 8. 类型不匹配处理

### 8.1 自动查找映射方法

当源类型和目标类型不兼容时，自动查找当前 Mapper 中是否有对应的映射方法：

```java
// 如果存在 AddressEntity toAddress(AddressDto dto) 方法
// 则自动调用：
target.setAddress(dto.getAddress() == null ? null : OrderMapperImpl.toAddress(dto.getAddress()));
```

### 8.2 集合元素类型不匹配

```java
// 源：List<OrderDto>，目标：List<OrderEntity>
// 如果存在 OrderEntity toEntity(OrderDto dto) 方法
// 生成循环转换代码（不使用 Stream API，兼容低版本 Android）
if (dto.getOrders() == null) {
    target.setOrders(null);
} else {
    ArrayList<OrderEntity> tempList = new ArrayList<>();
    for (int i = 0; i < dto.getOrders().size(); i++) {
        tempList.add(OrderMapperImpl.toEntity(dto.getOrders().get(i)));
    }
    target.setOrders(tempList);
}
```

### 8.3 集合类型转换

```java
// 源：List<Item>，目标：ArrayList<Item>
// 生成：
target.setItems(dto.getItems() == null ? null : new java.util.ArrayList<>(dto.getItems()));
```

---

## 9. 空值检查行为

### 9.1 配置方式

- 类级：`@MappingConfig(isNeedNullCheck = true)` 标注在 Mapper 接口上
- 方法级：`@MappingConfig(isNeedNullCheck = false)` 标注在方法上（覆盖类级）

### 9.2 生成代码差异

**needNullCheck = true（默认）：**
```java
if (dto.getName() != null) {
    target.setName(dto.getName());
}
```

**needNullCheck = false：**
```java
target.setName(dto.getName());
```

### 9.3 不检查的情况

- 基本类型（int, long, boolean 等）永远不检查
- 包含三元运算符的表达式不检查
- expression 表达式不检查

---

## 10. 嵌套对象映射

### 10.1 目标路径为嵌套

```java
// @Mapping(source = "city", target = "address.city")
AddressEntity addressObj = target.getAddress();
if (addressObj == null) {
    addressObj = new AddressEntity();
}
addressObj.setCity(dto.getCity());
target.setAddress(addressObj);
```

### 10.2 源路径为嵌套

```java
// @Mapping(source = "address.city", target = "city")
target.setCity(dto.getAddress().getCity());
```

---

## 11. 生命周期方法

### 11.1 执行顺序

```
@BeforeMapping → 字段映射 → @AfterMapping
```

### 11.2 参数匹配规则

| 参数类型 | 匹配方式 |
|----------|----------|
| `@MappingTarget` 参数 | 匹配目标对象 |
| 非 `@MappingTarget` 参数 | 按类型精确匹配映射方法的参数 |

如果类型不匹配，跳过该生命周期方法（不报错）。

### 11.3 Kotlin 接口默认方法调用

```java
// Kotlin 源文件：调用 DefaultImpls
OrderMapper.DefaultImpls.beforeMapping(null, dto, target);
```

### 11.4 Java default 方法

无法在静态方法中调用 Java default 方法，生成空实现。

---

## 12. @MappingIgnore 方法

### 12.1 用途

用于编写 `expression` 中调用的业务逻辑方法：

```kotlin
@Mapper
interface OrderMapper {
    @Mapping(target = "formattedPrice", expression = "java(formatPrice(source.getPrice()))")
    fun toEntity(source: OrderDto): OrderEntity

    @MappingIgnore
    fun formatPrice(price: Double): String {
        return String.format("%.2f", price)
    }
}
```

### 12.2 生成规则

只有被 expression 引用的 `@MappingIgnore` 方法才会生成静态版本。

---

## 13. 生成的 Javadoc

每个映射方法生成详细的字段映射注释：

```java
/**
 * 字段映射详情：
 * 源对象：OrderDto
 * 目标对象：OrderEntity
 *
 * 显式映射（不同名）：
 *   - userName -> name
 *
 * 自动映射（同名）：
 *   - orderId
 *   - amount
 *   - createTime
 *
 * 未映射的源字段（源对象有但目标对象没有对应字段）：
 *   - tempField (String)
 *
 * 未映射的目标字段（目标对象有但源对象没有对应字段）：
 *   - version (int)
 *
 * 类型不匹配的字段（需要特殊处理或添加映射方法）：
 *   - items: List<OrderItemDto> -> List<OrderItemEntity>
 */
public static OrderEntity toEntity(OrderDto dto) { ... }
```

---

## 14. 编译错误清单

| 错误 | 触发条件 |
|------|----------|
| `@Mapper can only be applied to interface or abstract class` | 标注在普通类上 |
| `No abstract mapping methods found inside @Mapper type` | 接口中没有可处理的抽象方法 |
| `Mapper method must declare at least one source parameter` | 方法没有非 @MappingTarget 参数 |
| `Non update mapper method must return a target type` | 无 @MappingTarget 且返回 void |
| `Only one @MappingTarget parameter is supported` | 多个 @MappingTarget 参数 |
| `@Mapping target cannot be empty` | target 属性为空 |
| `@Mapping cannot have both 'source' and 'expression'` | 同时指定 source 和 expression |
| `@Mapping cannot have both 'constant' and 'expression'` | 同时指定 constant 和 expression |
| `Type mismatch for property 'xxx'` | 类型不兼容且无映射方法 |
| `Cannot find getter or field for 'xxx'` | 指定的属性路径不存在 |
| `Circular @InheritConfiguration detected` | 循环继承 |
| `@InheritConfiguration refers to unknown method xxx` | 引用不存在的方法 |
| `No setter or writable field found for target 'xxx'` | 目标属性无法写入 |

---

## 15. 使用示例

```kotlin
@Mapper
@MappingConfig(isNeedNullCheck = true)
interface OrderMapper {

    @Mapping(source = "userName", target = "name")
    @Mapping(target = "status", constant = "\"PENDING\"")
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "fullName", expression = "java(dto.getFirstName() + \" \" + dto.getLastName())")
    fun toEntity(dto: OrderDto): OrderEntity

    @InheritConfiguration(name = "toEntity")
    @Mapping(target = "updateTime", expression = "java(System.currentTimeMillis())")
    @MappingConfig(isNeedNullCheck = false)
    fun updateEntity(dto: OrderDto, @MappingTarget entity: OrderEntity)

    fun toEntityList(dtos: List<OrderDto>): List<OrderEntity>

    @BeforeMapping
    fun beforeMapping(dto: OrderDto, @MappingTarget entity: OrderEntity) {
        entity.version = entity.version + 1
    }

    @AfterMapping
    fun afterMapping(dto: OrderDto, @MappingTarget entity: OrderEntity) {
        if (dto.amount > 10000) entity.isLargeOrder = true
    }

    @MappingIgnore
    fun formatPrice(price: Double): String {
        return String.format("%.2f", price)
    }
}
```

---

## 16. 生成文件位置

- 包名：与 Mapper 接口相同
- 文件名：`{MapperName}{suffix}.java`（如 `OrderMapperImpl.java`）

package mapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段映射注解，支持 source/target/ignore/constant/expression 等常用属性。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
@java.lang.annotation.Repeatable(Mappings.class)
public @interface Mapping {

    /**
     * 对应源对象属性路径，允许使用"参数名.属性"形式。
     * 例如："user.name" 表示参数 user 的 name 属性。
     */
    String source() default "";

    /**
     * 目标对象属性路径。
     */
    String target();

    /**
     * 是否忽略该字段映射。
     */
    boolean ignore() default false;

    /**
     * 使用常量填充值，常量内容会直接写入生成代码。
     * 例如：constant = "DEFAULT_VALUE"
     */
    String constant() default "";

    /**
     * 使用 Java 表达式进行映射，支持自定义业务逻辑。
     * 
     * 表达式可以使用：
     * - 方法参数名（如 source、target）
     * - 源对象的属性（如 source.getName()）
     * - 方法调用（如 formatDate(source.getDate())）
     * - 条件表达式（如 source.getAge() > 18 ? "ADULT" : "MINOR"）
     * 
     * 示例：
     * <pre>
     * {@code
     * @Mapping(target = "fullName", expression = "java(source.getFirstName() + \" \" + source.getLastName())")
     * @Mapping(target = "ageGroup", expression = "java(source.getAge() > 18 ? \"ADULT\" : \"MINOR\")")
     * @Mapping(target = "formattedDate", expression = "java(formatDate(source.getDate()))")
     * }
     * </pre>
     * 
     * 注意：
     * - expression 和 source 不能同时使用
     * - expression 中的代码会直接写入生成的实现类
     * - 表达式必须以 "java(...)" 开头
     */
    String expression() default "";
}


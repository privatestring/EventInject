package mapper

/**
 * 字段映射注解，支持 source/target/ignore/constant/expression 等常用属性。
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
@Repeatable
annotation class Mapping(
    /**
     * 对应源对象属性路径，允许使用"参数名.属性"形式。
     * 例如："user.name" 表示参数 user 的 name 属性。
     */
    val source: String = "",
    /**
     * 目标对象属性路径。
     */
    val target: String,
    /**
     * 是否忽略该字段映射。
     */
    val ignore: Boolean = false,
    /**
     * 使用常量填充值，常量内容会直接写入生成代码。
     */
    val constant: String = "",
    /**
     * 使用 Java 表达式进行映射，支持自定义业务逻辑。
     * 表达式必须以 "java(...)" 开头。
     * expression 和 source 不能同时使用。
     */
    val expression: String = ""
)

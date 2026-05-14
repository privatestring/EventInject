package launcher;

/**
 * 功能地图 注解
 */
public @interface Function {
    String functionId() default "";
    String desc();
    String[] group() default {};
}

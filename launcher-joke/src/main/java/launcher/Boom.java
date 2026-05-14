package launcher;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(CLASS)
@Target(FIELD)
public @interface Boom {
    int index();//当前参数所在的排序位置

    String key() default "";//是否自定义key

    boolean isOptional() default false;// 是否可选参数，默认是必传参数

    boolean useFieldKey() default false;// 是否使用属性名作为key

    String desc() default ""; // 支持跨模块时需要详细说明用途，方便其它人使用
}
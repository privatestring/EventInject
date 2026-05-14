package mapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link Mapping} 的容器注解，兼容早期 Java 版本不支持可重复注解的场景。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface Mappings {
    Mapping[] value();
}


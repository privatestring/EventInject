package mapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mapper 配置注解
 * 用于控制 Mapper 接口或方法的映射行为
 * 可以标注在类上（全局配置）或方法上（方法级配置）
 * 方法级配置会覆盖类级配置
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface MappingConfig {
    /**
     * 是否需要在字段赋值时进行空值检查
     * true: 在赋值前检查源值是否为 null，只有非 null 时才赋值
     * false: 直接赋值，不进行空值检查（默认行为）
     * 
     * 示例：
     * true 时生成：if (source.name != null) { target.name = source.name; }
     * false 时生成：target.name = source.name;
     */
    boolean isNeedNullCheck() default true;
}


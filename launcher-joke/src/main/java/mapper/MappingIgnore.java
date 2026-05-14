package mapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记在 Mapper 接口的方法上，表示该方法不需要生成实现。
 * 
 * 这些方法通常用于编写 expression 语句中的实际业务代码，
 * 而不是用于对象映射。
 * 
 * 示例：
 * <pre>
 * {@code
 * @Mapper
 * public interface UserMapper {
 *     UserEntity toEntity(UserDto dto);
 *     
 *     @MappingIgnore
 *     default String formatName(String firstName, String lastName) {
 *         return firstName + " " + lastName;
 *     }
 * }
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface MappingIgnore {
}


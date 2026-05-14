package mapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在映射完成后执行自定义逻辑的注解
 * 
 * 使用场景：
 * 1. 在字段映射完成后，需要执行额外的业务逻辑
 * 2. 需要根据映射结果进行条件判断和修改
 * 3. 需要调用其他方法进行数据转换或验证
 * 
 * 示例：
 * <pre>
 * {@code
 * @Mapper
 * interface UserMapper {
 *     UserEntity toEntity(UserDto dto);
 *     
 *     @AfterMapping
 *     default void afterMapping(UserDto source, @MappingTarget UserEntity target) {
 *         // 自定义业务逻辑
 *         if (source.getAge() > 18) {
 *             target.setAdult(true);
 *         }
 *         // 格式化处理
 *         target.setFormattedName(formatName(source.getName()));
 *     }
 * }
 * }
 * </pre>
 * 
 * 注意：
 * - 方法必须是 default 方法（接口中）或非抽象方法（抽象类中）
 * - 方法参数必须包含源对象和目标对象（使用 @MappingTarget 标记）
 * - 方法返回类型必须是 void
 * - 可以有多个 @AfterMapping 方法，按定义顺序执行
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface AfterMapping {
}


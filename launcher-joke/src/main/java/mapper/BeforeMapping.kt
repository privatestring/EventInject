package mapper

/**
 * 在映射开始前执行自定义逻辑的注解
 *
 * 使用场景：
 * 1. 在字段映射开始前，需要预处理源数据
 * 2. 需要根据源数据设置目标对象的初始状态
 * 3. 需要验证源数据的有效性
 * 
 * 示例：
 * <pre>
 * {@code
 * @Mapper
 * interface UserMapper {
 *     UserEntity toEntity(UserDto dto);
 *     
 *     @BeforeMapping
 *     default void beforeMapping(UserDto source, @MappingTarget UserEntity target) {
 *         // 预处理逻辑
 *         if (source == null) {
 *             return;
 *         }
 *         // 设置默认值
 *         target.setCreatedTime(System.currentTimeMillis());
 *     }
 * }
 * }
 * </pre>
 * 
 * 注意：
 * - 方法必须是 default 方法（接口中）或非抽象方法（抽象类中）
 * - 方法参数必须包含源对象和目标对象（使用 @MappingTarget 标记）
 * - 方法返回类型必须是 void
 * - 可以有多个 @BeforeMapping 方法，按定义顺序执行
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class BeforeMapping

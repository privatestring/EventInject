package launcher

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FIELD)
annotation class Boom(
    val index: Int, // 当前参数所在的排序位置
    val key: String = "", // 是否自定义key
    val isOptional: Boolean = false, // 是否可选参数，默认是必传参数
    val useFieldKey: Boolean = false, // 是否使用属性名作为key
    val desc: String = "" // 支持跨模块时需要详细说明用途，方便其它人使用
)


/**
 * 标注在子类上，表示生成的 Launcher 需要包含父类的 @Boom 属性。
 * 使用时子类的 @Boom index 不能与父类重复，否则编译报错。
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class IncludeParentBoom

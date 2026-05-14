package launcher

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class ParentCls(
    // This flag should be used only for Activities
    val isParentClass: Boolean = true
)

package launcher

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class MakeResult(
    // This flag should be used only for Activities
    val includeStartForResult: Boolean = false
)

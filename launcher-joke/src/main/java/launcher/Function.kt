package launcher

/**
 * 功能地图 注解
 */
annotation class Function(
    val functionId: String = "",
    val desc: String,
    val group: Array<String> = []
)

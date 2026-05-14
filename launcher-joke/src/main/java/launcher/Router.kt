package launcher

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Router(
    val routerPath: String, // 跨模块使用，需要指定路由地址，小心与其它冲突
    val cls: kotlin.reflect.KClass<*> = Void::class // 默认使用当前注解类的类名，也可以定制化
)

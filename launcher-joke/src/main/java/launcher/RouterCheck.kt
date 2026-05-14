package launcher

// 路由跳转检测类
// 该注解，会为当前Activity/Fragment类，生成一个RouterChecker类
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class RouterCheck(
    val checkers: Array<kotlin.reflect.KClass<out IRouterChecker>>, // 跨模块使用，需要指定路由地址，小心与其它冲突
    val cls: kotlin.reflect.KClass<*> = Void::class // 默认使用当前注解类的类名，也可以定制化
)

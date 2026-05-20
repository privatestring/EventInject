package wb.service

/**
 * 服务注册条目，支持按需实例化。
 *
 * 当 @ServiceGroup(lazy = true) 时，KSP 生成 ServiceEntry 列表而非直接实例列表。
 * 调用方可通过 [isType] 按类型匹配，仅对需要的条目调用 [instance] 触发实例化。
 *
 * @param implClass 实现类的 Class，用于类型匹配（如 isAssignableFrom）
 * @param factory 延迟创建工厂，首次访问 [instance] 时调用
 */
class ServiceEntry<T>(
    val implClass: Class<out T>,
    private val factory: () -> T
) {
    /** 首次访问时创建实例，后续复用（线程安全） */
    val instance: T by lazy { factory() }

    /** 判断实现类是否为指定类型（支持子接口匹配） */
    fun isType(targetClass: Class<*>): Boolean = targetClass.isAssignableFrom(implClass)
}


/**
 * 通用 Provider 标记接口。
 *
 * 业务层自定义的 Provider 接口继承此接口即可通过 @ServiceRegistry(IProvider::class) 注册，
 * 底层框架无需感知具体业务接口类型。
 * 调用方通过 ServiceEntry.isType() 按具体子接口类型匹配取用。
 */
interface IProvider {
    val pKey: String get() = ""
}

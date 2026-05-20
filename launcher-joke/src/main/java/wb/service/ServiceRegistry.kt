package wb.service

import kotlin.reflect.KClass

/**
 * 标记一个类需要注册到 ServiceAggregator。
 * 通过 value 参数显式指定归入哪个 SPI 接口组，与 @AutoService 语义一致。
 *
 * 迁移时只需：@AutoService(X::class) → @ServiceRegistry(X::class)
 *
 * 用法：
 * - @ServiceRegistry(IViewProvider::class)
 * - @ServiceRegistry(IService::class, priority = 100)
 * - @ServiceRegistry(IFragmentProvider::class)
 * - @ServiceRegistry(AbTestProvider::class)
 *
 * @param value 指定归入哪个 SPI 接口
 * @param priority 优先级，数值越大越靠前。默认 0，同优先级按类名字母序排列。
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ServiceRegistry(
    val value: KClass<*>,
    val priority: Int = 0
)

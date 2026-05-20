package wb.service

import kotlin.reflect.KClass

/**
 * 标记聚合器接口中的方法对应哪个 SPI 接口组。
 * KSP Processor 通过扫描此注解动态建立映射关系。
 *
 * 新增 service 类型时，只需在聚合接口中加一个带此注解的方法即可，
 * Processor 无需任何改动。
 *
 * 懒加载判断：Processor 通过方法返回类型自动推断：
 * - 返回 List<X> → eager 模式，直接实例化
 * - 返回 List<ServiceEntry<X>> → lazy 模式，生成工厂
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ServiceGroup(
    val value: KClass<*>
)

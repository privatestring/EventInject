package wb.service

import kotlin.reflect.KClass

/**
 * 标记聚合器接口中的方法对应哪个 SPI 接口组。
 * KSP Processor 通过扫描此注解动态建立映射关系。
 *
 * 新增 service 类型时，只需在聚合接口中加一个带此注解的方法即可，
 * Processor 无需任何改动。
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ServiceGroup(
    val value: KClass<*>
)

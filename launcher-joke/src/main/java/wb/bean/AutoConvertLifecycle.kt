package wb.bean

/**
 * 类转换生命周期接口。
 *
 * 实现此接口可在自动生成的转换代码执行前后插入自定义逻辑。
 *
 * @param S 源类类型
 * @param T 目标类类型
 */
interface AutoConvertLifecycle<S, T> {

    /**
     * 转换开始前调用。
     * 此时 target 已创建但尚未赋值，可用于初始化、校验等前置操作。
     */
    fun onStart(source: S, target: T) {}

    /**
     * 转换结束后调用。
     * 此时自动映射的属性已全部赋值完毕，可用于后处理、补充手动映射等。
     */
    fun onEnd(source: S, target: T) {}
}

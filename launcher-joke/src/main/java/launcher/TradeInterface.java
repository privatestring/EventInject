package launcher;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于标记Trade接口的实现类
 * 注解处理器会在编译时收集所有标记此注解的类，并自动生成TradeInterfaceFactory
 * 
 * 使用示例:
 * @TradeService(ITradeAccountInterface.class)
 * public class TradeAccountInterfaceImpl implements ITradeAccountInterface { ... }
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface TradeInterface {
    /**
     * 接口类型，实现类需要实现此接口
     */
    Class<?> value();
    
    /**
     * 是否为内部接口
     */
    boolean isInner() default false;
} 
package launcher;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

// 路由跳转检测类
// 该注解，会为当前Activity/Fragment类，生成一个RouterChecker类
@Retention(RUNTIME)
@Target(TYPE)
public @interface RouterCheck {
    Class<? extends IRouterChecker>[] checkers();//跨模块使用，需要指定路由地址，小心与其它冲突
    Class<?> cls() default Void.class;//默认使用当前注解类的类名，也可以定制化

}

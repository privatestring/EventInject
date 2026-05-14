package launcher;

import java.util.HashMap;
import java.util.Map;

/**
 * 路由拦截器，RouterCheck
 * 1、想继续下一个链式检测，用chain?.doCheck (大部分场景都用这个，如果是最后一个检测器，会执行默认跳转)
 * 2、想直接结束所有检测，且使用默认跳转，用proceedCallback
 * 3、想直接结束所有检测，且使用自定义跳转，用jumpCallback
 **/
public interface IRouterChecker {
    void doCheck(
            RouterCheckerChain chain,
            Object context,
            Map<String,String> params,
            HashMap<String, String> extras,
            IRouterProceedCallback proceedCallback,
            IRouterJumpCallback jumpCallback);

    // 优先级
    // 数字越大，优先级越高
    int priority();
}

package launcher;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RouterCheckerChain implements IRouterChecker {
    private List<IRouterChecker> checkers = new ArrayList<>();
    private int index = 0;

    public void addChecker(Class<IRouterChecker> checkerClass) {
        try {
            IRouterChecker checker = checkerClass.getConstructor().newInstance();
            checkers.add(checker);
            sortCheckers();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sortCheckers() {
        Collections.sort(checkers, (o1, o2) -> {
            if (o1.priority() > o2.priority()) {
                return 1;
            } else if (o1.priority() < o2.priority()) {
                return -1;
            } else {
                return 0;
            }
        });
    }

    @Override
    public void doCheck(RouterCheckerChain chain, Object context, Map<String, String> params, HashMap<String, String> extras, IRouterProceedCallback proceedCallback, IRouterJumpCallback callback) {
        //入参如果为空，不继续
        if (context == null
                || params == null
                || checkers == null || checkers.isEmpty()) {
            proceedCallback.proceed(false);
            return;
        }
        if (index >= checkers.size()) {
            //说明是最后一个检查类，如果前面都没返回结果，则这里返回成功
            proceedCallback.proceed(true);
            return;
        }
        IRouterChecker checker = checkers.get(index);
        index++;
        checker.doCheck(RouterCheckerChain.this, context, params, extras, proceedCallback, callback);
    }

    @Override
    public int priority() {
        return 0;
    }

}

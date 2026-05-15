package router;

import java.util.HashMap;

// 自动生成的路由注册,改动无效
public final class WBRouter {
    public static final HashMap<String, String> routerClsMap = new HashMap<>();

    public static void putRouter(String path, String cls) {
        routerClsMap.put(path, cls);
    }

    public static void bindRouter() {

    }
}
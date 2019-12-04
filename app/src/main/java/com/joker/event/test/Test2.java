package com.joker.event.test;

import android.util.Log;

import com.joker.annotation.EventBridge;
import com.joker.annotation.EventHandle;

import org.jetbrains.annotations.Nullable;

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2019-11-20
 */
@EventBridge(schemes = "haha://test2")
public class Test2 implements EventHandle {

    @Nullable
    @Override
    public Object handleEvent(@Nullable Object params) {
            Log.e("info-Test2", "handleEvent: " + params);
        return null;
    }
}

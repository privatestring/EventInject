package com.joker.event.test

import android.util.Log
import com.joker.annotation.EventBridge
import com.joker.annotation.EventHandle

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2019-11-20
 */
@EventBridge("haha://test1")
class Test1 :EventHandle{

    override fun handleEvent(params: Any?): Any? {
        Log.e("info-Test1", "$params: ");
        return null
    }
}
package com.joker.event

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.joker.annotation.EventBridge
import com.joker.annotation.EventHandle
import com.mei.models.EventInjectImpl
import kotlinx.android.synthetic.main.activity_main.*

@EventBridge("haha://tab_selected","haha://xxx")
class MainActivity : AppCompatActivity(), EventHandle {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val back: (String) -> Int = {
            Log.e("info-back", "$it: ");
            100
        }
        main_post.setOnClickListener { v ->

            EventInjectImpl().postEventInject("haha://tab_selected", back)
        }
        test1_post.setOnClickListener { v ->
            EventInjectImpl().postEventInject("haha://test1", Pair("xxxxx1", back))
        }
        test2_post.setOnClickListener { v ->
            EventInjectImpl().postEventInject("haha://test2", Pair("xxxxx2", back))
        }


    }

    @Suppress("UNCHECKED_CAST")
    override fun handleEvent(params: Any?): Any? {
        val func = (params as? ((String) -> Int))?.let {
            Log.e("info", "${it("this is main activity")}: ");
        }
        return null
    }
}

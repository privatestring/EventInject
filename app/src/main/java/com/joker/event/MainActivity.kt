package com.joker.event

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.joker.annotation.EventBridge
import com.joker.annotation.EventHandle
import com.joker.event.databinding.ActivityMainBinding
import com.mei.models.EventInjectImpl

@EventBridge("haha://tab_selected", "haha://xxx")
class MainActivity : AppCompatActivity(), EventHandle {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val back: (String) -> Int = {
            Log.e("info-back", "$it: ");
            100
        }
        binding.mainPost.setOnClickListener { v ->

            EventInjectImpl().postEventInject("haha://tab_selected", back)
        }
        binding.test1Post.setOnClickListener { v ->
            EventInjectImpl().postEventInject("haha://test1", Pair("xxxxx1", back))
        }
        binding.test2Post.setOnClickListener { v ->
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

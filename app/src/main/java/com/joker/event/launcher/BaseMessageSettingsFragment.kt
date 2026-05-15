package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import launcher.Boom

class BaseMessageSettingsFragment : Fragment() {
    @Boom(index = 0, key = "key_conversation_title")
    var roleName: String? = null

    @Boom(index = 1, key = "key_top_flag")
    var remindFlag: String? = null

    @Boom(index = 2, key = "key_remind_flag")
    var topFlag: String? = null

    @Boom(index = 3, key = "key_email_flag")
    var emailFlag: String? = null

    @Boom(index = 4, key = "key_message_type")
    var type: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        BaseMessageSettingsFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}

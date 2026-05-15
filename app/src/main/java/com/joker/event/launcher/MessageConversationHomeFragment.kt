package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import launcher.Boom

class MessageConversationHomeFragment : Fragment() {
    @Boom(index = 0, key = "key_message_type")
    var conversationType: String = ""

    @Boom(index = 1, isOptional = true, key = "key_conversation_title")
    var title: String ? = null

    @Boom(index = 2, isOptional = true, key = "key_catalog")
    var catalog: String = ""

    @Boom(index = 3, isOptional = true, key = "key_conversation_left_avatar_url")
    var leftAvatarUrl: String ? = null

    @Boom(index = 4, isOptional = true, key = "key_remind_flag")
    var mRemindFlag: String? = null

    @Boom(index = 5, isOptional = true, key = "key_email_flag")
    var mEmailFlag: String? = null

    @Boom(index = 6, isOptional = true, key = "key_reject_flag")
    var mRejectFlag: String? = null

    @Boom(index = 7, isOptional = true, key = "key_top_flag")
    var mTopFlag: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        MessageConversationHomeFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}

package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "message_page_conversation")
class MessageConversationHomeFragment : Fragment() {

    @Boom(index = 0, key = "key_message_type", desc = "消息会话类型")
    var conversationType: String = ""

    @Boom(index = 1, key = "key_conversation_title", isOptional = true, desc = "标题")
    var title: String? = null

    @Boom(index = 2, key = "key_catalog", isOptional = true, desc = "catalog")
    var catalog: String = ""

    @Boom(index = 3, key = "key_conversation_left_avatar_url", isOptional = true, desc = "左侧头像地址")
    var leftAvatarUrl: String? = null

    @Boom(index = 4, key = "key_remind_flag", isOptional = true, desc = "RemindFlag")
    var mRemindFlag: String? = null

    @Boom(index = 5, key = "key_email_flag", isOptional = true, desc = "EmailFlag")
    var mEmailFlag: String? = null

    @Boom(index = 6, key = "key_reject_flag", isOptional = true, desc = "RejectFlag")
    var mRejectFlag: String? = null

    @Boom(index = 7, key = "key_top_flag", isOptional = true, desc = "TopFlag")
    var mTopFlag: String? = null

}
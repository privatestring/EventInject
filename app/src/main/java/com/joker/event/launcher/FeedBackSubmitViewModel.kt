package com.joker.event.launcher

import launcher.Boom

class FeedBackSubmitViewModel {
    @Boom(index = 0, key = "intent_key_suggestion_id")
    var id: String = ""

    @Boom(index = 1, key = "feedback_casecode")
    var caseCode: String = ""

    @Boom(index = 2, key = "intent_key_feed_from_type")
    var fromType: String = ""

    @Boom(index = 3, isOptional = true, key = "intent_key_feed_input_default")
    var defaultInput: String = ""

    @Boom(index = 4, isOptional = true, key = "intent_key_feed_input_title")
    var inputTitle: String? = ""

    @Boom(index = 5, isOptional = true, key = "intent_key_biz_scenario")
    var bizScenario: String? = ""

    @Boom(index = 6, isOptional = true, key = "intent_key_feed_sec_account_id")
    var secAccountId: String? = ""

    @Boom(index = 7, isOptional = true, key = "intent_key_feed_suggestion_type")
    var suggestionType: String? = null

}

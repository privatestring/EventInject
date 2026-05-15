package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import launcher.Boom

class TradeHomeFragmentV10 : Fragment() {
    @Boom(index = 1, key = "intent_key_account_info", isOptional = true)
    var initAccountKey: String? = null

    @Boom(index = 2, key = "intent_key_single_broker_model")
    var mIsSingleBrokerModel: Boolean = false

    @Boom(index = 3, key = "intent_key_allow_switch")
    var mIsAllowSwitchBroker: Boolean = true

    @Boom(index = 4, key = "intent_key_currency")
    var currency: String? = null

    @Boom(index = 5, isOptional = true)
    var isInstSimulator: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TradeHomeFragmentV10Launcher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}

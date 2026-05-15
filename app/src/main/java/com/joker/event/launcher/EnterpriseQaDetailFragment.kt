package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import launcher.Boom

class EnterpriseQaDetailFragment : Fragment() {
    @Boom(index = 0, key = "enterprise_uuid")
    var enterpriseUuid: String = ""

    @Boom(index = 1, key = "enterprise_faq_uuid")
    var faqUuid: String = ""

    @Boom(index = 2, key = "enterprise_tickers")
    var tickers: String? = ""

    @Boom(index = 3, key = "enterprise_only_postion")
    var questionOnlyShareholder: String? = "true"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        EnterpriseQaDetailFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}

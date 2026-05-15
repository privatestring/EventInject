package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.webull.alert.common.viewdata.AlertTypeBean
import com.webull.alert.common.bean.BaseWarningRuleBean
import com.webull.commonmodule.bean.TickerKey
import launcher.Boom

open class BaseRuleContentEditFragment : Fragment() {
    @Boom(index = 0, desc = "当前标的")
    var tickerKey: TickerKey = TickerKey()
    @Boom(index = 1, desc = "当前编辑的盯盘类型")
    var alertType: AlertTypeBean = AlertTypeBean.Price()
    @Boom(index = 2, desc = "当前编辑的盯盘规则，创建盯盘是为空", isOptional = true)
    var rule: BaseWarningRuleBean? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        BaseRuleContentEditFragmentLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}

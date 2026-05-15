package com.joker.event.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.webull.commonmodule.networkinterface.socialapi.beans.common.FeedReportTypeItem
import com.webull.commonmodule.comment.report.ReportData
import launcher.Boom

class ReportSecondDialog : DialogFragment() {
    @Boom(index = 0)
    var reportTypeList: ArrayList<FeedReportTypeItem> = arrayListOf()

    @Boom(index = 1)
    var selectedItem: FeedReportTypeItem = FeedReportTypeItem()

    @Boom(index = 2)
    var reportData: ReportData = ReportData("", "")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        ReportSecondDialogLauncher.bind(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}

package com.joker.event.launcher

import android.app.Activity
import android.os.Bundle
import com.webull.library.tradenetwork.bean.AccountInfo
import java.util.Date
import launcher.Boom

class AUCSVStatementDownloadActivity : Activity() {
    @Boom(index = 0)
    var accountInfo: AccountInfo? = null

    @Boom(index = 1)
    var bizDate: String? = null

    @Boom(index = 2)
    var id: String? = null

    @Boom(index = 3)
    var statementType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AUCSVStatementDownloadActivityLauncher.bind(this)
    }
}

package com.joker.event.launcher

import android.app.Activity
import android.os.Bundle
import androidx.annotation.IntDef
import com.webull.library.tradenetwork.bean.AccountInfo
import launcher.Boom

class SelfHelpActivity : Activity() {
    @Boom(index = 0)
    var accountInfo: AccountInfo = AccountInfo()

    @Boom(index = 1)
    var relationshipId: String = ""

    @Boom(index = 2)
    @FrozenType
    var frozenType: Int = 0

    @Boom(index = 3)
    var accountNum: String = ""

    @Boom(index = 4)
    var frozenReason: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SelfHelpActivityLauncher.bind(this)
    }
}




/** ach 解冻  */
const val FROZEN_TYPE_ACH = 1

/** paypal 解冻  */
const val FROZEN_TYPE_PAYPAL = 2

@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
@IntDef(FROZEN_TYPE_ACH, FROZEN_TYPE_PAYPAL)
annotation class FrozenType


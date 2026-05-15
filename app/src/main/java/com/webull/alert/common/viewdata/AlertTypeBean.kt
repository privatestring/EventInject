package com.webull.alert.common.viewdata

import java.io.Serializable

sealed class AlertTypeBean : Serializable {
    class Price : AlertTypeBean()
}

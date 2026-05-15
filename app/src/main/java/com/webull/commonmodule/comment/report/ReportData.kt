package com.webull.commonmodule.comment.report

import java.io.Serializable

open class ReportData(
    var type: String = "",
    var id: String = ""
) : Serializable

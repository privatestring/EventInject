package com.webull.commonmodule.comment.report

import java.io.Serializable

class ReportData(var relatedType: String, var id: String, var nickName: String? = "") : Serializable

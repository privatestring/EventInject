package com.webull.commonmodule.datepick.bean

import java.io.Serializable
import java.util.Date

class PLRangeBean(var date: Date = Date(), var minSelectDate: Date = Date(), var limit: Int = 0, var tips: String = "") : Serializable

enum class DateType : Serializable { CN_DATE, US_DATE }

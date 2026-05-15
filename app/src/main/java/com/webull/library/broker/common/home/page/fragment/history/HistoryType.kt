package com.webull.library.broker.common.home.page.fragment.history

import java.io.Serializable

enum class HistoryType : Serializable {
    Order, Ipo, Dividends, OptionExercise, Funds, OrderFund, CryptoTransfer, PositionTransfer, Settlement, WarrantExercise
}

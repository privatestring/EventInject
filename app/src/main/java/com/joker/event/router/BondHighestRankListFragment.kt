package com.joker.event.router

import androidx.fragment.app.Fragment
import launcher.Boom
import launcher.Router

@Router(routerPath = "bond_highest_rank")
class BondHighestRankListFragment : Fragment() {

    @Boom(index = 0, key = "com.joker.event.router.isNeedActionBarIntentKey", desc = "是否需要显示导航条")
    var isNeedActionBar: String? = null

    @Boom(index = 1, key = "oddLotFlag", isOptional = true, desc = "碎债支持 1表示支持碎债 null 不支持 默认为不支持")
    var oddFlag: String? = null

    @Boom(index = 2, key = "treasuryType", isOptional = true, desc = "国债子品类 国债类型,1-bill；2-bond；3-note 4-strips")
    var treasuryType: String? = null

}
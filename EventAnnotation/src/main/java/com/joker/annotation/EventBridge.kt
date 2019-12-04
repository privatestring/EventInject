package com.joker.annotation



/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2019-11-20
 */

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class EventBridge(vararg val schemes: String)

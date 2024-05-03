package com.a10miaomiao.bilimiao.comm

import android.content.Intent
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.a10miaomiao.bilimiao.Bilimiao
import com.a10miaomiao.bilimiao.comm.navigation.tryPopBackStack
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import splitties.toast.toast

class BiliJsBridge(
    val fragment: Fragment,
    val webView: WebView,
//    val closeBrowser: () -> Unit,
) {
    private val activity get() = fragment.requireActivity()

    private val allSupportMethod = listOf<String>(
        "global.closeBrowser",
        "ui.setStatusBarMode",
//       "auth.checkBridgeEnable",
        "auth.getUserInfo",
//      "auth.getAccessToken",
//      "auth.getBaseInfo",
//      "auth.getAllBridge",
//      "auth.getTeenable",
//       "auth.getNetEnv",
        "auth.login",
        "ability.openScheme",
        "ability.currentThemeType",
        "view.goBack",
        "view.closeBrowser",
        "view.toast",
        "view.refresh",
        "view.setTitle",
//        "view.isLongScreen",
//        "route.login",
//        "route.editUserInfo",
//        "route.record",
//        "route.recommend",
//        "share.showShareWindow",
        "share.showShareMpcWindow",
//            "func.route",
//            "func.share",
//            "func.setShare",
//            "func.childrenOn",
//            "func.childrenOff",
//            "func.copy",
//            "func.cloud-editor.sync",
//            "func.creation-center.switchTabVisible",
//            "func.fixWindow",
//            "func.push.status",
//            "func.vipDraw.result",
//            "func.report.success",
    )

    fun findNavController() = fragment.findNavController()

    @JavascriptInterface
    fun postMessage(eventString: String) {
        DebugMiao.log("postMessage", eventString)
        val event = Gson().fromJson<MessageEventInfo>(eventString, MessageEventInfoType.type)
        var result = ""
        when (event.method) {
            "ui.setStatusBarMode" -> {

            }
            "auth.getUserInfo" -> {

            }
            "global.getAllSupport" -> {
                result = "[${allSupportMethod.joinToString(",") { "\"$it\"" }}]"
            }
            "global.closeBrowser",
            "view.closeBrowser",
            "view.goBack" -> {
//                closeBrowser.invoke()
                activity.runOnUiThread {
                    findNavController().tryPopBackStack()
                }
            }
            "view.refresh" -> {
                webView.reload()
            }
            "share.setShareContent" -> {
                activity.runOnUiThread {
                    toast("暂不支持分享操作")
                }
            }
            "share.showShareMpcWindow" -> {
                val defaultData = event.data["default"].asJsonObject
                val title = defaultData["title"].asString
                val text = defaultData["text"].asString
                val url = defaultData["url"].asString
                activity.runOnUiThread {
                    val sendIntent = Intent(Intent.ACTION_SEND)
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "$title $url $text");
                    sendIntent.setType("text/plain")
                    activity.startActivity(sendIntent)
                }
            }
            "ability.openScheme" -> {
                val url = event.data["url"].asString ?: return
                activity.runOnUiThread {
                    val re = BiliNavigation.navigationTo(
                        NavHosts.pointerNavController,
                        url
                    )
                    if (!re) {
                        toast("不支持打开的链接：$url")
                    }
                }
            }
            "ability.currentThemeType" -> {
                result = """
                    {
                        type: 1
                    }
                    """.trimIndent()
            }
            "auth.login" -> {
                val loginInfo = Bilimiao.commApp.loginInfo
                if (loginInfo != null) {
                    // TODO: 刷新登录cookie
                    val onLoginCallbackId = event.data["onLoginCallbackId"].asString
                    biliCallbackReceived(onLoginCallbackId, "{ state: 1 }")
                }
            }
        }
        activity.runOnUiThread {
            event.callback(result)
        }
    }

    fun MessageEventInfo.callback(
        result: String
    ) {
        val callbackId = data["callbackId"].asString
        callbackId?.let {
            biliCallbackReceived(it, result)
        }
    }

    fun biliCallbackReceived(
        callbackId: String,
        data: String,
    ) {
        val javascript = """(function() {
                window.BiliJsBridge.biliInject.biliCallbackReceived($callbackId, $data)
            })()
            """.trimIndent()
        activity.runOnUiThread {
            webView.evaluateJavascript(javascript){ }
        }
    }

    data class MessageEventInfo(
        val method: String,
        val data: JsonObject
    )
    object MessageEventInfoType : TypeToken<MessageEventInfo>()
}
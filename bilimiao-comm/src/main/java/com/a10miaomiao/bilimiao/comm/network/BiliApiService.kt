package com.a10miaomiao.bilimiao.comm.network

import com.a10miaomiao.bilimiao.comm.apis.RegionAPI
import com.a10miaomiao.bilimiao.comm.apis.VideoAPI

object BiliApiService {
    fun createUrl(url: String, vararg pairs: Pair<String, String>): String {
        val params = ApiHelper.createParams(*pairs)
        return url + "?" + ApiHelper.urlencode(params)
    }

    fun biliApi(path: String, vararg pairs: Pair<String, String>): String {
        return createUrl("https://api.bilibili.com/$path", *pairs)
    }

    fun biliApp(path: String, vararg pairs: Pair<String, String>): String {
        return createUrl("https://app.bilibili.com/$path", *pairs)
    }

    fun biliBangumi(path: String, vararg pairs: Pair<String, String>): String {
        return createUrl("https://bangumi.bilibili.com/$path", *pairs)
    }

    val regionAPI = RegionAPI()
    val videoAPI = VideoAPI()


}
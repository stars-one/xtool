package site.starsone.xtool.utils

import okhttp3.CacheControl
import okhttp3.Callback

import okhttp3.OkHttpClient
import okhttp3.Request


/**
 *
 * @author StarsOne
 * @url <a href="http://stars-one.site">http://stars-one.site</a>
 * @date Create in  2022/07/24 15:36
 *
 */
object OkhttpUtils {
    /**
     * 发送get请求
     *
     * @param address
     * @param callback
     */
    fun httpGet(address: String, callback: Callback) {
        val client = OkHttpClient()
        val control = CacheControl.Builder().build()
        println("请求地址:$address")
        val request: Request = Request.Builder()
                .cacheControl(control)
                .url(address)
                .build()
        client.newCall(request).enqueue(callback)
    }

    /**
     * 发送get请求
     *
     * @param address 地址
     * @param paramMap 参数
     * @param callback 接口返回
     */
    fun httpGet(address: String, paramMap: HashMap<String, Any>, callback: Callback) {
        val param = paramMap.map { "${it.key}=${it.value}" }.joinToString("&")
        val url = "$address?$param"
        httpGet(url,callback)
    }

}

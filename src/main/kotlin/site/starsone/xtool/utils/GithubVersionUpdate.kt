package site.starsone.xtool.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import site.starsone.xtool.model.DescData
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


object GithubVersionUpdate {

    fun checkVersion(descData: DescData, callback: (VersionUpdateMessage) -> Unit) {
        val url = descData.updateUrl
        OkhttpUtils.httpGet(url, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                body?.let {
                    val result = it.string()
                    println(result)
                    val list = Gson().fromJson<List<ReleaseVersionInfo>>(result, object : TypeToken<List<ReleaseVersionInfo>>() {}.type)
                    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                    simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
                    val item = list.first()
                    val tagName = item.tag_name
                    val createTime = simpleDateFormat.parse(item.created_at).toDateString()
                    val assets = item.assets
                    val jarDownloadUrl = if (assets.size > 0) {
                        assets.first().browser_download_url
                    } else {
                        ""
                    }
                    val jarFileSize = if (assets.size > 0) {
                        "${assets.first().size / (1024 * 1024)}MB"
                    } else {
                        "未知"
                    }
                    val jarFileName = if (assets.size > 0) {
                        assets.first().name
                    } else {
                        ""
                    }
                    val versionContent = item.body
                    val versionUpdateMessage = VersionUpdateMessage(tagName, createTime, jarDownloadUrl, jarFileSize, jarFileName, versionContent)
                    callback.invoke(versionUpdateMessage)
                }
            }

        })
    }
}


data class VersionUpdateMessage(val tagName: String, val createTime: String, val downloadUrl: String, val fileSize: String, val fileName: String, val content: String)

data class ReleaseVersionInfo(
        val assets: List<Asset>,
        val assets_url: String, // https://api.github.com/repos/stars-one/xtool/releases/71619326/assets
        val author: Author,
        val body: String, // ## Commits- 3fa2725: 更新文档说明 (starone)- 2f70b22: 新增Navicat解密工具菜单 (starone)- e8e7e07: 取消设置窗口在顶部 (starone)- 64b041c: NavicatPwdView.kt增加文件选择 (starone)- b794802: 新增端口占用查询功能 (starone)- 8027976: 完成md目录导航生成功能 (starone)- 13d5b09: 新增Navicat解密获取密码功能 (starone)- 31c542b: 加个标题 (starone)- 6ca4930: 更新文档 (starone)- 2826c1f: 更新文档 (starone)- 4b81ac1: 更新文档 (starone)
        val created_at: String, // 2022-07-09T10:46:34Z
        val draft: Boolean, // false
        val html_url: String, // https://github.com/stars-one/xtool/releases/tag/1.4.1
        val id: Int, // 71619326
        val name: String, // Release 1.4.1
        val node_id: String, // RE_kwDOHahjq84ERNL-
        val prerelease: Boolean, // false
        val published_at: String, // 2022-07-09T10:48:56Z
        val tag_name: String, // 1.4.1
        val tarball_url: String, // https://api.github.com/repos/stars-one/xtool/tarball/1.4.1
        val target_commitish: String, // main
        val upload_url: String, // https://uploads.github.com/repos/stars-one/xtool/releases/71619326/assets{?name,label}
        val url: String, // https://api.github.com/repos/stars-one/xtool/releases/71619326
        val zipball_url: String // https://api.github.com/repos/stars-one/xtool/zipball/1.4.1
) {
    data class Asset(
            val browser_download_url: String, // https://github.com/stars-one/xtool/releases/download/1.4.1/XTool-1.4.1.jar
            val content_type: String, // application/octet-stream
            val created_at: String, // 2022-07-09T10:48:56Z
            val download_count: Int, // 1
            val id: Int, // 71054032
            val label: String,
            val name: String, // XTool-1.4.1.jar
            val node_id: String, // RA_kwDOHahjq84EPDLQ
            val size: Int, // 25134839
            val state: String, // uploaded
            val updated_at: String, // 2022-07-09T10:48:58Z
            val uploader: Uploader,
            val url: String // https://api.github.com/repos/stars-one/xtool/releases/assets/71054032
    ) {
        data class Uploader(
                val avatar_url: String, // https://avatars.githubusercontent.com/in/15368?v=4
                val events_url: String, // https://api.github.com/users/github-actions%5Bbot%5D/events{/privacy}
                val followers_url: String, // https://api.github.com/users/github-actions%5Bbot%5D/followers
                val following_url: String, // https://api.github.com/users/github-actions%5Bbot%5D/following{/other_user}
                val gists_url: String, // https://api.github.com/users/github-actions%5Bbot%5D/gists{/gist_id}
                val gravatar_id: String,
                val html_url: String, // https://github.com/apps/github-actions
                val id: Int, // 41898282
                val login: String, // github-actions[bot]
                val node_id: String, // MDM6Qm90NDE4OTgyODI=
                val organizations_url: String, // https://api.github.com/users/github-actions%5Bbot%5D/orgs
                val received_events_url: String, // https://api.github.com/users/github-actions%5Bbot%5D/received_events
                val repos_url: String, // https://api.github.com/users/github-actions%5Bbot%5D/repos
                val site_admin: Boolean, // false
                val starred_url: String, // https://api.github.com/users/github-actions%5Bbot%5D/starred{/owner}{/repo}
                val subscriptions_url: String, // https://api.github.com/users/github-actions%5Bbot%5D/subscriptions
                val type: String, // Bot
                val url: String // https://api.github.com/users/github-actions%5Bbot%5D
        )
    }

    data class Author(
            val avatar_url: String, // https://avatars.githubusercontent.com/in/15368?v=4
            val events_url: String, // https://api.github.com/users/github-actions%5Bbot%5D/events{/privacy}
            val followers_url: String, // https://api.github.com/users/github-actions%5Bbot%5D/followers
            val following_url: String, // https://api.github.com/users/github-actions%5Bbot%5D/following{/other_user}
            val gists_url: String, // https://api.github.com/users/github-actions%5Bbot%5D/gists{/gist_id}
            val gravatar_id: String,
            val html_url: String, // https://github.com/apps/github-actions
            val id: Int, // 41898282
            val login: String, // github-actions[bot]
            val node_id: String, // MDM6Qm90NDE4OTgyODI=
            val organizations_url: String, // https://api.github.com/users/github-actions%5Bbot%5D/orgs
            val received_events_url: String, // https://api.github.com/users/github-actions%5Bbot%5D/received_events
            val repos_url: String, // https://api.github.com/users/github-actions%5Bbot%5D/repos
            val site_admin: Boolean, // false
            val starred_url: String, // https://api.github.com/users/github-actions%5Bbot%5D/starred{/owner}{/repo}
            val subscriptions_url: String, // https://api.github.com/users/github-actions%5Bbot%5D/subscriptions
            val type: String, // Bot
            val url: String // https://api.github.com/users/github-actions%5Bbot%5D
    )
}

package site.starsone.xtool.view

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javafx.beans.property.SimpleStringProperty
import tornadofx.*
import java.io.File
import java.lang.StringBuilder

/**
 *
 * @author StarsOne
 * @url <a href="http://stars-one.site">http://stars-one.site</a>
 * @date Create in  2022/07/10 14:21
 *
 */
class AndroidPermissionView : View("安装apk错误原因大全") {
    val controller by inject<AndroidPermissionViewController>()

    override val root = vbox {

        setPrefSize(1050.0, 500.0)
        form {
            fieldset {
                field("关键字搜索") {
                    textfield(controller.keyword) {
                        promptText = "输入关键字搜索"
                        controller.keyword.addListener { observable, oldValue, newValue ->
                            controller.startSearch()
                        }
                    }
                }
            }
        }

        tableview(controller.showListData) {
            readonlyColumn("权限", AndroidPermissionInfo::permission)
            readonlyColumn("名称", AndroidPermissionInfo::name)
            readonlyColumn("说明", AndroidPermissionInfo::desc) {
                isFillWidth = true
            }
        }

        controller.initData()
        //test()
    }
}

class AndroidPermissionViewController : Controller() {
    val keyword = SimpleStringProperty()

    //全部数据
    val allListData = observableListOf<AndroidPermissionInfo>()

    //展示数据
    val showListData = observableListOf<AndroidPermissionInfo>()

    fun initData() {
        val text = resources.text("/dbdata/AndroidPermission.json")
        val type = (object : TypeToken<List<AndroidPermissionInfo>>() {}).type
        val list = Gson().fromJson<List<AndroidPermissionInfo>>(text, type)
        allListData.addAll(list)
        showListData.addAll(list)
    }

    fun startSearch() {
        val newValue = keyword.value
        val list = allListData.filtered {
            it.desc.toLowerCase().contains(newValue.toLowerCase()) || it.permission.toLowerCase().contains(newValue.toLowerCase()) || it.name.toLowerCase().contains(newValue.toLowerCase())
        }
        showListData.clear()
        showListData.addAll(list)
    }
}

data class AndroidPermissionInfo(val permission: String, val name: String, val desc: String)

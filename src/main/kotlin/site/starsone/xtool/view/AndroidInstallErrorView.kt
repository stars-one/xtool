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
class AndroidInstallErrorView : View("My View") {
    val controller by inject<AndroidInstallErrorController>()

    override val root = vbox {

        setPrefSize(1050.0, 500.0)
        form {
            fieldset {
                field("关键字搜索"){
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
            readonlyColumn("错误码", AndroidInstallErrorInfo::errorCode)
            readonlyColumn("错误名称", AndroidInstallErrorInfo::errorName) {
                prefWidth = 420.0
            }
            readonlyColumn("说明", AndroidInstallErrorInfo::desc) {
                prefWidth = 540.0
            }
        }

        controller.initData()
        //test()
    }

    fun test() {
        val list = arrayListOf<AndroidInstallErrorInfo>()
        val path = "D:\\temp\\新建 文本文档.txt"
        val file = File(path)
        val lines = file.readLines()
        val sb = StringBuilder()
        lines.forEach {
            val arr = it.trim().replace("\t", "").split("|")
            val arr1 = arr[1].split("=")
            println(arr1.first())
            println(arr1.last())
            val item = AndroidInstallErrorInfo(arr1.last().trim().toInt(), arr1.first().trim(), arr[2].trim())
            list.add(item)
            sb.append("${arr1.first()}|${arr1.last()}|${arr[2]}")
            sb.appendln()
        }
        val outputFile = File("D:\\temp\\tt.txt")
        outputFile.writeText(Gson().toJson(list))
    }
}

class AndroidInstallErrorController : Controller() {
    val keyword = SimpleStringProperty()

    //全部数据
    val allListData = observableListOf<AndroidInstallErrorInfo>()

    //展示数据
    val showListData = observableListOf<AndroidInstallErrorInfo>()

    fun initData() {
        val text = resources.text("/dbdata/AndroidInstallErrorDb.json")
        val type = (object : TypeToken<List<AndroidInstallErrorInfo>>() {}).type
        val list = Gson().fromJson<List<AndroidInstallErrorInfo>>(text, type)
        allListData.addAll(list)
        showListData.addAll(list)
    }

    fun startSearch() {
        val newValue = keyword.value
        val list =allListData.filtered {
            it.desc.contains(newValue)||it.errorCode.toString().contains(newValue)||it.errorName.contains(newValue)
        }
        showListData.clear()
        showListData.addAll(list)
    }
}

data class AndroidInstallErrorInfo(val errorCode: Int, val errorName: String, val desc: String)

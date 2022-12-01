package site.starsone.xtool.view

import com.starsone.controls.common.remixIconButton
import com.starsone.controls.common.remixIconText
import com.starsone.controls.common.showToast
import com.starsone.controls.utils.TornadoFxUtil
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.TableCell
import javafx.scene.layout.VBox
import tornadofx.*
import java.io.*

/**
 * 查看本机保存wifi密码
 *
 * @constructor Create empty Cmd view
 */
class WifiInfoView : View("Wifi密码查看") {

    val controller by inject<WifiInfoViewController>()

    override val root = vbox {
        setPrefSize(600.0, 600.0)

        form {
            //tableview 使用 https://stars-one.gitee.io/tornadofx-guide-chinese/#/part1/5_Data_Controls?id=tableview
            tableview(controller.observableList) {

                readonlyColumn("Wifi名称", WifiInfo::name){
                    prefWidth = 300.0
                }
                readonlyColumn("密码", WifiInfo::pwd){
                    prefWidth = 250.0
                    cellFormat {
                        val hbox = hbox{
                            alignment = Pos.CENTER_LEFT
                            val lambda = {
                                TornadoFxUtil.copyTextToClipboard(it)
                                showToast(this@vbox,"复制成功")
                            }
                            remixIconText("file-copy-2-fill",fontColor = c("#1890ff")){
                                tooltip = tooltip("复制密码")
                                setOnMouseClicked {
                                    lambda.invoke()
                                }
                            }
                            label(it){
                                setOnMouseClicked {
                                    lambda.invoke()
                                }
                            }
                        }
                        graphic = hbox
                    }
                }
                placeholder = tablePlaceNode()
            }
        }
        controller.loadWifiList()
    }

    private fun tablePlaceNode(): VBox {
        return vbox{
            alignment  = Pos.CENTER
            imageview("/img/my_no_data.png"){
                fitWidth = 200.0
                fitHeight = 200.0
            }
            label("暂无数据,似乎你电脑是个新电脑呢~")
        }
    }
}

class WifiInfoViewController : Controller() {
    val observableList = observableListOf<WifiInfo>()

    val wifiInfoDirFile = File(TornadoFxUtil.getCurrentJarDirPath(resources.url("/desc.json")), "wifiInfo")

    fun loadWifiList() {
        val cmd = "cmd /c netsh wlan export profile key=clear"
        println("执行命令 $cmd")

        if (!wifiInfoDirFile.exists()) {
            wifiInfoDirFile.mkdirs()
        }
        val exec = Runtime.getRuntime().exec(cmd, null, wifiInfoDirFile)
        val inputStream = exec.inputStream

        val myBr = BufferedReader(InputStreamReader(inputStream, "gbk"))

        runAsync {
            var line: String? = null
            try {
                while (myBr.readLine().also({ line = it }) != null) {
                    line?.let {
                        println(line)
                    }
                }
                wifiInfoDirFile.listFiles().forEach {
                    val lines = it.readLines()
                    val wifiInfo = WifiInfo("", "")
                    lines.forEach { line ->
                        if (line.contains("name")) {
                            wifiInfo.name = line.subStringBetween("<name>", "</name>")
                        }
                        if (line.contains("keyMaterial")) {
                            wifiInfo.pwd = line.subStringBetween("<keyMaterial>", "</keyMaterial>")
                        }
                    }
                    runLater {
                        observableList.add(wifiInfo)
                    }
                }
            } catch (e: IOException) {

            }
        }
    }

}

/**
 * Wifi信息类
 *
 * @property name wifi名称
 * @property pwd 密码
 */
data class WifiInfo(var name: String, var pwd: String)

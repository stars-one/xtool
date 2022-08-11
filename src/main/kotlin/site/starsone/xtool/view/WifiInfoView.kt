package site.starsone.xtool.view

import com.starsone.controls.utils.TornadoFxUtil
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Button
import javafx.scene.control.TableCell
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
        setPrefSize(800.0, 600.0)

        form {
            //tableview 使用 https://stars-one.gitee.io/tornadofx-guide-chinese/#/part1/5_Data_Controls?id=tableview
            tableview(controller.observableList) {

                readonlyColumn("Wifi名称", WifiInfo::name){
                    prefWidth = 250.0
                }
                readonlyColumn("密码", WifiInfo::pwd){
                    prefWidth = 250.0
                }

                readonlyColumn("操作", WifiInfo::pwd) {
                    prefWidth = 300.0
                    setCellFactory {
                        object : TableCell<WifiInfo, String>() {
                            val button = Button("复制密码")

                            override fun updateItem(item: String?, empty: Boolean) {
                                button.action {
                                    item?.let {
                                        TornadoFxUtil.copyTextToClipboard(item)
                                    }
                                }
                                if (!empty) {
                                    graphic = button
                                }
                            }
                        }
                    }
                }
            }
        }
        controller.loadWifiList()
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

package site.starsone.xtool.view

import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Button
import javafx.scene.control.TableCell
import tornadofx.*
import java.io.*

/**
 * 进程管理页面
 *
 * @constructor Create empty Cmd view
 */
class CmdView : View("端口占用进程管理") {

    val controller by inject<CmdViewController>()

    override val root = vbox {
        setPrefSize(800.0, 600.0)

        form {
            fieldset {
                field("端口号") {

                    textfield(controller.port) {
                        isFocusTraversable = false
                        promptText = "输入端口号"
                    }
                    button("查找进程") {
                        addClass("cf-primary-but")
                        action {
                            controller.getList()
                        }
                    }
                }
            }

            //tableview 使用 https://stars-one.gitee.io/tornadofx-guide-chinese/#/part1/5_Data_Controls?id=tableview
            tableview(controller.observableList) {

                readonlyColumn("协议", CourseInfoItem::protocol)
                readonlyColumn("本地地址", CourseInfoItem::innerIpAddress) {
                    prefWidth = 180.0
                }
                readonlyColumn("外部地址", CourseInfoItem::outIpAddress) {
                    prefWidth = 180.0
                }
                readonlyColumn("状态", CourseInfoItem::status) {
                    prefWidth = 150.0
                }
                readonlyColumn("进程id", CourseInfoItem::pid)
                readonlyColumn("操作", CourseInfoItem::pid) {
                    prefWidth = 150.0
                    setCellFactory {
                        object : TableCell<CourseInfoItem, Int>() {
                            val button = Button("结束进程")

                            override fun updateItem(item: Int?, empty: Boolean) {
                                button.action {
                                    controller.taskKill(item.toString())
                                }
                                if (!empty) {
                                    graphic = button
                                }
                            }
                        }
                    }

                }
            }

            vbox(){
                prefWidth = 20.0
            }
            label("输出日志")
            textarea(controller.consoleLog) {
                prefHeight = 200.0
            }
        }
    }
}

class CmdViewController : Controller() {
    val port = SimpleStringProperty()
    val consoleLog = SimpleStringProperty()
    val observableList = observableListOf<CourseInfoItem>()

    //netstat 命令说明 https://blog.csdn.net/weixin_44299027/article/details/123741176
    fun getList() {
        val port = port.value

        val cmd = "cmd /c netstat -aon|findstr $port"

        println("执行命令 $cmd")

        val exec = Runtime.getRuntime().exec(cmd)
        val inputStream = exec.inputStream

        val myBr = BufferedReader(InputStreamReader(inputStream, "gbk"))
        val list = arrayListOf<String>()

        runAsync {
            var line: String? = null
            try {
                while (myBr.readLine().also({ line = it }) != null) {
                    line?.let {
                        list.add(it)
                        updateConsole(it)
                        //切割数据,拼接成数据
                        val arr = it.split(" ")
                        val result = arr.filter { it.trim().isNotBlank() }
                        if (result.size == 5) {
                            val item = CourseInfoItem(result[0], result[1], result[2], result[3], result[4].toInt())
                            runLater {
                                observableList.add(item)
                            }
                            println(item.toString())
                        }
                    }
                }
            } catch (e: IOException) {
            }
        }
    }

    /**
     * 强制结束进程
     *
     * @param port
     */
    fun taskKill(port: String) {
        val cmd = "taskkill /f /pid $port"
        val exec = Runtime.getRuntime().exec(cmd)
        val inputStream = exec.inputStream

        val myBr = BufferedReader(InputStreamReader(inputStream, "gbk"))

        runAsync {
            var line: String? = null
            try {
                while (myBr.readLine().also({ line = it }) != null) {
                    line?.let {
                        updateConsole(it)
                    }
                }
            } catch (e: IOException) {
            }
        }

    }

    private fun updateConsole(str: String) {
        val oldValue = consoleLog.value
        consoleLog.value = oldValue + "\n" + str
    }
}

/**
 * [protocol] 协议
 * [innerIpAddress] 本地地址
 * [outIpAddress] 外部地址
 * [status] 状态
 * [pid] pid
 */
data class CourseInfoItem(val protocol: String, val innerIpAddress: String, val outIpAddress: String, val status: String, val pid: Int)

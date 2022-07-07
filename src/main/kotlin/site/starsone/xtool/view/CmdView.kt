package site.starsone.xtool.view

import com.jfoenix.controls.JFXButton
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.image.Image
import tornadofx.*
import java.io.*



class MainView : View() {

    var dirFileTf: TextField by singleAssign()
    var jsonFileTf: TextField by singleAssign()

    var setSignButton: JFXButton by singleAssign()
    var settingSign: Field by singleAssign()

    var textarea by singleAssign<TextArea>()

    var option = 0
    var signVersion = 1 //默认v1+v2

    var dir = File("")

    init {
        title = "Ios打包测试"
        setStageIcon(Image("img/icon.png"))

        importStylesheet("/css/chenfei-core.css")
    }

    override val root = vbox {
        setPrefSize(500.0, 400.0)

        form {
            fieldset {
                field("命令") {

                    dirFileTf = textfield {
                        isFocusTraversable = false
                    }

                }
                field("字符集") {

                    jsonFileTf = textfield {
                        isFocusTraversable = false
                        val prop = System.getProperties()

                        val os = prop.getProperty("os.name")
                        text = if (os.contains("win", true)) {
                            "gbk"
                        } else {
                            "utf-8"
                        }
                    }

                }

                field {
                    button("执行"){
                        addClass("cf-primary-but")
                        action {
                            //todo 端口进程信息查询

                            //netstat -ano | findstr 6379

                            //taskkill /f /pid 5372

                            /*val cmd = dirFileTf.text
                            val charset = jsonFileTf.text
                            val controller = MainController()
                            outputText("执行命令 $cmd")
                            val inputStream = controller.executeCmd(cmd)

                            val tee = TeeInputStream(inputStream, FileOutputStream(File("D:\\temp\\test.log")))

                            val myBr = BufferedReader(InputStreamReader(tee, charset))
                            val list = arrayListOf<String>()

                            thread {
                                var line: String? = null
                                try {
                                    while (myBr.readLine().also({ line = it }) != null) {
                                        line?.let {
                                            list.add(it)
                                            outputText(it)
                                        }
                                    }
                                } catch (e: IOException) {

                                }
                                val index = list.indexOf("pgyerdata")
                                if (index >= 0) {
                                    val jsonData = list[index + 1]
                                    outputText("获取的json数据为：\n")
                                    outputText(jsonData)
                                }
                            }*/

                        }
                    }
                }

                field {
                    textarea = textarea {
                        isEditable = false
                        isWrapText = true

                    }
                }

            }
        }
    }


    fun outputText(text: String?) {

        runLater {
            textarea.appendText("\n")
            textarea.appendText(text)
        }

        val arr = text?.split(" ")
        val result = arr?.filter { it.trim().isNotBlank() }
        println(result.toString())
    }


}

class ConsolePrint(var console: TextArea) : PrintStream(ByteArrayOutputStream()) {
    //可以正常解析utf8和gbk码
    override fun write(buf: ByteArray, off: Int, len: Int) {
        print(String(buf, off, len))
    }

    override fun print(s: String) {
        console.appendText(s)
    }
}

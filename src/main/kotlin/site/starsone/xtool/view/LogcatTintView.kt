package site.starsone.xtool.view

import javafx.beans.property.SimpleStringProperty
import tornadofx.*
import java.io.File

/**
 * Logcat日志代码着色功能
 * @author StarsOne
 * @url <a href="http://stars-one.site">http://stars-one.site</a>
 * @date Create in  2022/10/18 23:48
 *
 */
class LogcatTintView : View("Logcat日志代码着色功能") {

    val viewModel by inject<LogcatTintViewModel>()

    override val root = vbox {

        setPrefSize(1000.0,800.0)
        //todo 增加日志等级过滤,搜索,设置各等级日志颜色功能,文本可复制功能


        form {
            fieldset("Logcat文本文件") {
                field {
                    xChooseFile(viewModel.logcatFilePath,"txt","文本文件")
                }
            }
        }
        button("刷新"){
            action{
                val tempText = viewModel.logcatFilePath.value
                viewModel.logcatFilePath.set(tempText+" ")
                viewModel.logcatFilePath.set(tempText)
            }
        }

        scrollpane {
            whenVisible {
                viewModel.logcatFilePath.isNotEmpty
            }

            vbox {
                viewModel.logcatFilePath.addListener { observable, oldValue, newValue ->
                    val file = File(newValue)
                    clear()
                    if (file.exists()) {
                        val lines = file.readLines()
                        lines.forEach {
                            val flag = it.substring(19, 20)
                            if (flag.isNotBlank()) {
                                val color = viewModel.map[flag]
                                text(it) {
                                    style {
                                        fill = color!!
                                        fontSize = 16.px
                                    }
                                }
                            }
                        }
                    }
                }


            }
        }

    }
}

class LogcatTintViewModel : ViewModel() {
    val logcatFilePath = SimpleStringProperty("")

    //颜色定义
    val map = hashMapOf(
            "V" to c("black"),
            "I" to c("#6a875a"),
            "D" to c("#6a875a"),
            "W" to c("#bbb529"),
            "E" to c("#9a174f")
    )

}

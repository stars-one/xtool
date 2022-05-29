package site.starsone.xtool.view

import com.github.houbb.markdown.toc.util.MdTocTextHelper
import com.melloware.jintellitype.JIntellitype
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.text.FontWeight
import javafx.stage.Screen
import site.starsone.xtool.app.Styles
import site.starsone.xtool.utils.ScreenProperties
import tornadofx.*
import java.awt.Rectangle
import java.awt.Robot
import java.io.File
import javax.imageio.ImageIO

class MdTocView : BaseView() {

    val viewModel by inject<MdTocViewModel>()

    override var root = borderpane {

        setPrefSize(500.0, 300.0)

        left {

            vbox {
                importStylesheet(Styles::class)
                progressbar {
                    progress = -1.0
                }

                textfield(viewModel.mdFilePath) {
                    promptText = "md文件路径"
                }

                button("生成目录代码") {
                    action {
                        viewModel.generateToc()
                    }
                }
            }
        }

        right {
            vbox {
                style {
                    padding = box(10.px)
                }
                textarea(viewModel.result) {
                    fitToParentHeight()
                    isWrapText = true
                }
            }
        }

    }

}

class MdTocViewModel : ViewModel() {
    var mdFilePath = SimpleStringProperty("")
    var result = SimpleStringProperty("")

    fun generateToc() {
        val file = File(mdFilePath.value)
        val readLines = file.readLines()
        val tocList = MdTocTextHelper.getTocList(readLines)
        val bf = StringBuffer()
        tocList.forEach {
            bf.append(it)
            bf.appendln()
        }
        result.value = bf.toString()
    }

}




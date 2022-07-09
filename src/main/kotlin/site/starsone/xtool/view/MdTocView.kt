package site.starsone.xtool.view

import com.github.houbb.markdown.toc.util.MdTocTextHelper
import com.melloware.jintellitype.JIntellitype
import com.starsone.controls.common.xCircleJfxButton
import com.starsone.controls.common.xJfxButton
import com.starsone.controls.common.xSelectText
import com.starsone.controls.utils.TornadoFxUtil
import com.starsone.controls.utils.setMargin
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.control.TextField
import javafx.scene.input.ClipboardContent
import javafx.scene.input.TransferMode
import javafx.scene.layout.HBox
import javafx.scene.text.FontWeight
import javafx.stage.FileChooser
import javafx.stage.Screen
import kfoenix.jfxbutton
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
        padding = insets(10.0)

        setPrefSize(500.0, 550.0)
        importStylesheet(Styles::class)

        top {
            label("此工具用来快速生成md文件的目录导航,选择md文件或复制md文件内容,之后即可自动生成目录并复制到粘贴板中,直接粘贴即可使用")
        }

        center {
            vbox {
                xChooseFile(viewModel.mdFilePath, "md,markdown", "markdown文件") {
                    viewModel.mdFilePath.addListener { observable, oldValue, newValue ->
                        viewModel.generateToc()
                    }
                }
                textarea(viewModel.inputContent) {
                    setMargin(insets(top=10))
                    prefColumnCount = 20
                    isWrapText = true
                    promptText="输入md文件内容"
                    viewModel.inputContent.addListener { observable, oldValue, newValue ->
                        val dirFile = TornadoFxUtil.getCurrentJarDirPath(resources.url("/desc.json"))
                        val tempFile = File(dirFile, "temp.md")
                        tempFile.writeText(newValue)
                        viewModel.mdFilePath.value = tempFile.path
                        viewModel.generateToc()
                        tempFile.delete()
                        viewModel.mdFilePath.value = ""
                    }
                }
            }
        }

        bottom {
            vbox {
                button("一键复制输出结果") {
                    action{
                        TornadoFxUtil.copyTextToClipboard(viewModel.result.value)
                    }
                }

                textarea(viewModel.result) {
                    prefColumnCount = 20
                    isWrapText = true

                    viewModel.result.addListener { observable, oldValue, newValue ->
                        TornadoFxUtil.copyTextToClipboard(newValue)
                    }
                }
            }
        }


    }

}

class MdTocViewModel : ViewModel() {
    var mdFilePath = SimpleStringProperty("")
    var inputContent = SimpleStringProperty("")
    var result = SimpleStringProperty("")

    fun generateToc() {
        if (mdFilePath.value.isNotBlank()) {
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
}


fun EventTarget.xChooseFile(myfilepath: SimpleStringProperty, fileTypes: String, fileDesc: String, imgPath: String = "", imgWidth: Int = 0, imgHeight: Int = 0, op: (HBox.() -> Unit) = {}): HBox {
    val hbox = hbox {
        textfield(myfilepath) {
            prefWidth=400.0
            setOnDragOver {
                it.acceptTransferModes(*TransferMode.ANY)
            }
            setOnDragExited {
                val dragboard = it.dragboard
                val flag = dragboard.hasFiles()
                if (flag) {
                    val files = dragboard.files
                    myfilepath.set(files.first().path)
                }
            }
            promptText="输入文件路径或拖放文件到此处"
        }

        if (imgPath.isBlank()) {
            //普通按钮
            jfxbutton {
                graphic = text {
                    text = "\ueac5"
                    style {
                        font = loadFont("/ttf/iconfont.ttf", 18.0)!!
                        fill = c("#ffad42")
                    }
                }
                action {
                    val split = fileTypes.split(",")
                    val fileTypeList = split.map { "*.$it" }
                    val files = chooseFile("选择文件", arrayOf(FileChooser.ExtensionFilter(fileDesc, fileTypeList)))
                    if (files.isNotEmpty()) {
                        myfilepath.set(files.first().path)
                    }
                }
            }
        } else {
            //图片按钮
            xJfxButton(imgPath, imgWidth, imgHeight) {
                action {
                    val split = fileTypes.split(",")
                    val fileTypeList = split.map { "*.$it" }
                    val files = chooseFile("选择文件", arrayOf(FileChooser.ExtensionFilter(fileDesc, fileTypeList)))
                    if (files.isNotEmpty()) {
                        myfilepath.set(files.first().path)
                    }
                }
            }
        }

    }
    return opcr(this, hbox, op)
}

package site.starsone.xtool.view

import com.google.gson.Gson
import com.starsone.controls.common.showDialog
import com.starsone.controls.common.showDialogPopup
import com.starsone.controls.common.showLoadingDialog
import com.starsone.controls.common.xJfxButton
import javafx.beans.property.SimpleStringProperty
import javafx.concurrent.Task
import javafx.event.EventTarget
import javafx.scene.input.TransferMode
import javafx.scene.layout.HBox
import javafx.stage.FileChooser
import kfoenix.jfxbutton
import org.jsoup.Jsoup
import tornadofx.*
import java.io.*
import javax.naming.directory.SearchResult
import kotlin.experimental.xor

/**
 *
 * @author StarsOne
 * @url <a href="http://stars-one.site">http://stars-one.site</a>
 * @date Create in  2022/08/15 22:32
 *
 */
class Uc2Mp3View : View("网易云uc缓存文件(uc)转mp3文件") {
    val controller by inject<Uc2Mp3ViewController>()

    lateinit var task: Task<Unit>

    override val root = vbox {
        setPrefSize(500.0, 300.0)

        xChooseFileDirectory("选择或输入网易云cache文件夹", controller.cacheFilePath)
        xChooseFileDirectory("选择或输入生成mp3存放文件夹", controller.outputFilePath)

        button("开始转换") {
            action {
                showLoadingDialog(currentStage, "提示", "转换文件中,请稍候...", "取消", { task.cancel(true) }) { alert ->
                    task = runAsync {
                        controller.startConvert()
                    } ui {
                        alert.hideWithAnimation()
                        showDialog(currentStage, "提示", "转换成功")
                    }
                }
            }
        }
    }
}

class Uc2Mp3ViewController : Controller() {
    val cacheFilePath = SimpleStringProperty("D:\\temp\\歌曲\\cache")

    //val cacheFilePath = SimpleStringProperty("")
    val outputFilePath = SimpleStringProperty("D:\\temp\\歌曲\\output")

    fun startConvert() {
        val dirFile = File(cacheFilePath.value)
        if (dirFile.exists()) {
            val tempFiles = dirFile.listFiles()
            tempFiles?.apply {
                val ucFiles = filter { it.extension.toLowerCase() == "uc" }
                //todo 开启多线程进行(有调用网页)
                for (ucFile in ucFiles) {
                    ucConvertMp3File(ucFile)
                }
            }
        }
    }

    private fun ucConvertMp3File(inFile: File): File {
        val musicId = inFile.nameWithoutExtension.substringBefore("-")
        //获取网页的信息，然后改名
        val song = getSongDetail(musicId)

        val outFile = File(outputFilePath.value, "${song.name}.mp3")

        val dataInputStream = DataInputStream(FileInputStream(inFile))
        val dataOutputStream = DataOutputStream(FileOutputStream(outFile))

        val data = ByteArray(1024 * 4)
        var len: Int = 0
        while (dataInputStream.read(data).also({ len = it }) != -1) {
            for (i in 0 until len) {
                data[i] = data[i] xor 0xa3.toByte()
            }
            dataOutputStream.write(data, 0, len)
        }

        dataOutputStream.close()
        dataInputStream.close()
        return outFile
    }

    private fun getSongDetail(id: String): Song {
        //val id = "1476496806"
        val pageUrl = "https://music.163.com/song?id=$id"
        val doc = Jsoup.connect(pageUrl).get()
        val name = doc.select(".m-lycifo .f-cb .cnt .hd .tit .f-ff2")[0].text()
        val detail = doc.select(".m-lycifo .f-cb .cnt .des")
        if (detail.size > 1) {
            val singer = detail[0].getElementsByAttribute("title")[0].attr("title")
            val album = detail[1].select(".s-fc7")[0].text()
            val url = "http://music.163.com/song/media/outer/url?id=${id}.mp3"
            return Song(name, singer, album, url)
        } else {
            val docHtml = doc.outerHtml()
            println(docHtml)
        }
        return Song("", "", "", "")
    }
}

data class Song(val name: String, val singer: String, val album: String, val fileUrl: String, var file: File? = null)

fun EventTarget.xChooseFileDirectory(tip: String, myfilepath: SimpleStringProperty, imgPath: String = "", imgWidth: Int = 0, imgHeight: Int = 0, op: (HBox.() -> Unit) = {}): HBox {
    val hbox = hbox {
        textfield(myfilepath) {
            prefWidth = 400.0
            promptText = tip
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
                    val file = chooseDirectory("选择目录", File(myfilepath.value))
                    file?.let {
                        myfilepath.set(it.path)
                    }
                }
            }
        } else {
            //图片按钮
            xJfxButton(imgPath, imgWidth, imgHeight) {
                action {
                    val file = chooseDirectory("选择目录", File(myfilepath.value))
                    file?.let {
                        myfilepath.set(it.path)
                    }
                }
            }
        }

    }
    return opcr(this, hbox, op)
}

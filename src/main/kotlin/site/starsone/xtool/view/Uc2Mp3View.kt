package site.starsone.xtool.view

import cn.hutool.core.io.FileUtil
import cn.hutool.core.io.file.FileNameUtil
import com.starsone.controls.common.*
import com.starsone.controls.utils.GlobalDataConfig
import com.starsone.controls.utils.GlobalDataConfigUtil
import com.starsone.controls.utils.TornadoFxUtil
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.concurrent.Task
import javafx.geometry.Pos
import javafx.scene.input.TransferMode
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import kfoenix.jfxbutton
import kfoenix.jfxradiobutton
import org.jsoup.Jsoup
import site.starsone.xtool.utils.toUnitString
import tornadofx.*
import java.io.*
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

    override val root = vbox(10) {

        setPrefSize(820.0, 500.0)

        style {
            backgroundColor += c("white")
        }
        padding = insets(10)

        hbox(10.0) {
            jfxbutton {
                graphic = remixIconText("add-circle-line", fontColor = c("white"))
                text = "添加文件"
                style {
                    backgroundColor += c("#366fff")
                    textFill = c("white")
                }
                action {
                    val files = chooseFile("选择ncm文件", arrayOf(FileChooser.ExtensionFilter("ncm文件", "*.uc")), mode = FileChooserMode.Multi)
                    controller.addFiles(files)
                }
            }

            jfxbutton {
                graphic = remixIconText("folder-2-fill", fontColor = c("white"))
                text = "添加文件夹"
                style {
                    backgroundColor += c("#366fff")
                    textFill = c("white")
                }
                action {
                    val dirFile = chooseDirectory("选择ncm文件夹")
                    val files = dirFile?.listFiles()?.filter { it.extension.toLowerCase() == "uc" }
                    files?.let {
                        controller.addFiles(it.toList())
                    }
                }
            }

            jfxbutton {
                graphic = remixIconText("delete-bin-fill", fontColor = c("white"))
                text = "清空列表"
                style {
                    backgroundColor += c("#875a1a")
                    textFill = c("white")
                }
                action {
                    controller.observableList.clear()
                }
            }

        }


        tableview(controller.observableList) {
            //设置文件拖动功能
            setOnDragOver {
                it.acceptTransferModes(*TransferMode.ANY)
            }
            setOnDragExited {
                val dragboard = it.dragboard
                val flag = dragboard.hasFiles()
                if (flag) {
                    val files = dragboard.files
                    //如果是文件夹,遍历所有子目录
                    val dirFiles = files.filter { it.isDirectory }
                    dirFiles.forEach {
                        val tempFiles = FileUtil.loopFiles(it) {
                            it.extension.toLowerCase() == "uc"
                        }
                        controller.addFiles(tempFiles)
                    }

                    val newFiles = files.filter { it.isFile }.filter {
                        it.extension.toLowerCase() == "uc"
                    }
                    controller.addFiles(newFiles)
                }
            }
            fitToParentSize()
            readonlyColumn("文件名", UcFileInfo::ucFile) {
                cellFormat {
                    text = it.name
                }
                prefWidth = 200.0
            }
            readonlyColumn("文件大小", UcFileInfo::fileSize) {
                prefWidth = 100.0
            }
            readonlyColumn("状态", UcFileInfo::status) {
                prefWidth = 98.0
                cellFormat {
                    val str = when (it) {
                        1 -> "转换成功"
                        2 -> "转换失败"
                        else -> "待开始"
                    }
                    text = str

                    style {
                        textFill = when (it) {
                            1 -> c("green")
                            2 -> c("red")
                            else -> c("black")
                        }
                    }
                }
            }
            readonlyColumn("输出文件", UcFileInfo::outputFile) {
                prefWidth = 400.0
                cellFormat {
                    it?.apply {
                        text = name
                    }
                }
            }

            //设置占位符
            placeholder = tablePlaceNode()
        }

        hbox {
            label("输出文件名格式：")

            togglegroup {
                selectedToggleProperty().addListener { _, _, newValue ->
                    controller.fileNamePatternType.set(newValue.userData.toString().toInt())
                }

                spacing = 10.0
                controller.fileNamePatternMap.forEach { (key, value) ->

                    jfxradiobutton(value) {
                        userData = key
                        selectedProperty().set(controller.fileNamePatternType.value == key)
                        selectedProperty().addListener { _, _, newValue ->
                            if (newValue) {
                                Uc2Mp3ViewController.GlobalSetting.fileNamePatternType.setValue(key)
                            }
                        }
                        controller.chooseItemListPropertyList.add(selectedProperty())
                    }
                }
            }
        }

        hbox(10) {
            alignment = Pos.CENTER_LEFT

            label("文件保存目录：")



            xChooseFileDirectory("文件保存目录", controller.outputFilePath, node = jfxbutton("更改目录") {
                style {
                    backgroundColor += c("#2b3245")
                    textFill = c("white")
                }
            })

            jfxbutton {
                text = "打开目录"
                style {
                    backgroundColor += c("#2b3245")
                    textFill = c("white")
                }
                action {
                    val dirFile = File(controller.outputFilePath.value)
                    dirFile.apply {
                        if (exists()) {
                            TornadoFxUtil.openFile(this)
                        }
                    }
                }
            }

            //设置左右对齐
            hbox {
                hgrow = Priority.ALWAYS
            }

            jfxbutton("开始转换") {
                alignment = Pos.CENTER_RIGHT
                style {
                    backgroundColor += c("#366fff")
                    textFill = c("white")
                }
                action {
                    //验证拦截
                    if (controller.observableList.isEmpty()) {
                        showToast(this@vbox, "还未添加uc文件呢!")
                        return@action
                    }
                    showLoadingDialog(currentStage, "提示", "转换文件中,请稍候...", "取消", { task.cancel(true) }) { alert ->
                        task = runAsync {
                            controller.startConvert()
                        } ui {
                            alert.hideWithAnimation()
                        }
                    }
                }
            }
            //如果默认下载目录未选择,则使用默认
            if (controller.outputFilePath.value.isBlank()) {
                val path = File(TornadoFxUtil.getCurrentJarDirPath(), "歌曲").apply {
                    //如果不存在,则创建
                    if (!exists()) {
                        mkdirs()
                    }
                }.path
                controller.outputFilePath.set(path)
            }
        }


    }

    private fun tablePlaceNode(): VBox {
        return vbox {
            alignment = Pos.CENTER
            imageview("/img/my_no_data.png") {
                fitWidth = 200.0
                fitHeight = 200.0
            }
            label("可以拖动uc文件或文件夹到此处")
        }
    }
}

/**
 * Uc file info
 *
 * @property ucFile
 * @property fileSize
 * @property status 状态 0:未开始 1:转换成功 2:转换失败
 * @property outputFile 输出的mp3文件
 * @constructor Create empty Uc file info
 */
data class UcFileInfo(var ucFile: File, var fileSize: String, var status: Int, var outputFile: File? = null)

class Uc2Mp3ViewController : Controller() {

    val fileNamePatternMap = hashMapOf(0 to "歌曲名", 1 to "歌曲名 - 歌手", 2 to "歌手 - 歌曲名")

    val fileNamePatternType = GlobalDataConfigUtil.getSimpleIntegerProperty(GlobalSetting.fileNamePatternType)

    val chooseItemListPropertyList = arrayListOf<BooleanProperty>()

    val selectOutputNameType = SimpleIntegerProperty(1)

    val observableList = observableListOf<UcFileInfo>()

    val outputFilePath = GlobalDataConfigUtil.getSimpleStringProperty(GlobalSetting.dirFile)

    fun addFiles(files: List<File>) {
        files.forEach {
            addFile(it)
        }
    }

    private fun addFile(file: File) {
        val info = UcFileInfo(file, file.length().toUnitString(), 0)
        observableList.add(info)
    }

    fun startConvert() {
        val newList = observableList.map {
            val ucFile = it.ucFile
            //todo 开启多线程进行(有调用网页)
            val outputFile = ucConvertMp3File(ucFile)
            val temp = it.copy()
            if (outputFile.exists() && outputFile.length() > 0) {
                temp.outputFile = outputFile
                temp.status = 1
            } else {
                temp.status = 2
            }
            temp
        }

        observableList.clear()
        observableList.addAll(newList)
    }

    private fun ucConvertMp3File(inFile: File): File {
        val selectOutputNameType = selectOutputNameType.value
        println(selectOutputNameType)

        val musicId = inFile.nameWithoutExtension.substringBefore("-")
        //获取网页的信息，然后改名
        val song = getSongDetail(musicId)

        //读取设置,是要 音乐名-歌手还是 歌手-音乐名
        val name = when (GlobalSetting.fileNamePatternType.currentValue) {
            0 -> "${song.name}.mp3"
            1 -> "${song.name} - ${song.singer}.mp3"
            2 -> "${song.singer} - ${song.name}.mp3"
            else -> "${song.name} - ${song.singer}.mp3"
        }
        //注意文件名可能存在非法字符,需要处理
        val outFile = File(outputFilePath.value, FileNameUtil.cleanInvalid(name))

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

    object GlobalConstant {
        const val DIR_PATH = "Uc2Mp3View_dirPath"
        const val FILE_NAME_PATTERN = "Uc2Mp3View_fileNamePattern"
    }

    /**
     * 全局的设置
     */
    object GlobalSetting {
        //歌曲保存目录
        var dirFile = GlobalDataConfig(GlobalConstant.DIR_PATH, "")

        //下载文件格式类型
        var fileNamePatternType = GlobalDataConfig(GlobalConstant.FILE_NAME_PATTERN, 1)
    }
}

data class Song(val name: String, val singer: String, val album: String, val fileUrl: String, var file: File? = null)


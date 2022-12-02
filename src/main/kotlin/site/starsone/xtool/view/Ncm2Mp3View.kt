package site.starsone.xtool.view

import cn.hutool.core.io.FileUtil
import com.starsone.controls.common.*
import com.starsone.controls.utils.GlobalDataConfig
import com.starsone.controls.utils.GlobalDataConfigUtil
import com.starsone.controls.utils.TornadoFxUtil
import javafx.beans.property.SimpleStringProperty
import javafx.concurrent.Task
import javafx.geometry.Pos
import javafx.scene.input.TransferMode
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import kfoenix.jfxbutton
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.flac.metadatablock.MetadataBlockDataPicture
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.images.ArtworkFactory
import site.starsone.xtool.utils.parseJsonToObject
import site.starsone.xtool.utils.toUnitString
import site.starsone.xtool.view.ncm2mp3.Core
import site.starsone.xtool.view.ncm2mp3.Utils
import site.starsone.xtool.view.ncm2mp3.mime.Mata
import site.starsone.xtool.view.ncm2mp3.mime.Ncm
import tornadofx.*
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileFilter
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.imageio.ImageIO

/**
 *
 * @author StarsOne
 * @url <a href="http://stars-one.site">http://stars-one.site</a>
 * @date Create in  2022/08/15 22:32
 *
 */
class Ncm2Mp3View : View("网易云ncm文件转mp3文件") {
    val controller by inject<Ncm2Mp3ViewController>()

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
                    val files = chooseFile("选择ncm文件", arrayOf(FileChooser.ExtensionFilter("ncm文件", "*.ncm")), mode = FileChooserMode.Multi)
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
                    val files = dirFile?.listFiles()?.filter { it.extension.toLowerCase() == "ncm" }
                    files?.let {
                        controller.addFiles(it.toList())
                    }
                }
            }

            jfxbutton {
                graphic = remixIconText("delete-btn-fill", fontColor = c("white"))
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
                    val dirFiles =files.filter { it.isDirectory }
                    dirFiles.forEach {
                       val tempFiles =  FileUtil.loopFiles(it) {
                           it.extension.toLowerCase() == "ncm"
                       }
                        controller.addFiles(tempFiles)
                    }

                    val newFiles = files.filter { it.isFile }.filter {
                        it.extension.toLowerCase() == "ncm"
                    }
                    controller.addFiles(newFiles)
                }
            }

            fitToParentSize()

            readonlyColumn("文件名", NcmFileInfo::fileName) {
                prefWidth = 200.0
            }
            readonlyColumn("文件路径", NcmFileInfo::fileName) {
                prefWidth = 400.0
            }
            readonlyColumn("文件大小", NcmFileInfo::fileSize) {
                prefWidth = 100.0
            }
            readonlyColumn("状态", NcmFileInfo::status) {
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

            //设置占位符
            placeholder = tablePlaceNode()
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
                    showLoadingDialog(currentStage, "提示", "转换文件中,请稍候...", "取消", { task.cancel(true) }) { alert ->
                        task = runAsync {
                            controller.startConvert()
                        } ui {
                            alert.hideWithAnimation()
                        }
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

   private fun tablePlaceNode(): VBox {
        return vbox{
            alignment  = Pos.CENTER
            imageview("/img/my_no_data.png"){
                fitWidth = 200.0
                fitHeight = 200.0
            }
            label("可以拖动ncm文件或文件夹到此处")
        }
    }
}

class Ncm2Mp3ViewController : Controller() {
    val observableList = observableListOf<NcmFileInfo>()

    val outputFilePath = GlobalDataConfigUtil.getSimpleStringProperty(GlobalSetting.dirFile)

    fun addFiles(files: List<File>) {
        files.forEach {
            addFile(it)
        }
    }

    private fun addFile(file: File) {
        val info = NcmFileInfo(file.name, file.path, file.length().toUnitString(), 0)
        observableList.add(info)
    }

    /**
     * 文件转换
     *
     */
    fun startConvert() {
        val newList = observableList.map {
            val ncmFilePath = it.filePath
            val file = File(ncmFilePath)
            val outputFile = File(outputFilePath.value, "${file.nameWithoutExtension}.mp3")

            val flag = Ncm2Mp3Util.ncm2Mp3(file.path, outputFile.path)

            //1是成功,2是失败
            it.status = if (flag) 1 else 2
            it.copy()
        }

        observableList.clear()
        observableList.addAll(newList)
    }

    object GlobalConstant {
        const val DIR_PATH = "Ncm2Mp3View_dirPath"
    }

    /**
     * 全局的设置
     */
    object GlobalSetting {
        //歌曲保存目录
        var dirFile = GlobalDataConfig(GlobalConstant.DIR_PATH, "")

    }
}

data class NcmFileInfo(var fileName: String, var filePath: String, var fileSize: String, var status: Int)
object Ncm2Mp3Util {
    /**
     * NCM转换MP3
     * 功能:将NCM音乐转换为MP3
     *
     * @param ncmFilePath NCM文件路径
     * @param outFilePath MP3文件路径
     * @return 转换成功与否
     */
    fun ncm2Mp3(ncmFilePath: String, outFilePath: String): Boolean {
        return try {
            val ncm = Ncm()
            ncm.ncmFile = ncmFilePath
            val inputStream = FileInputStream(ncm.ncmFile)
            Core.appendMagicHeader(inputStream)
            val key = Core.cr4Key(inputStream)
            val jsonText = Core.mataData(inputStream)
            val mata = jsonText.parseJsonToObject<Mata>()
            ncm.mata = mata
            val image = Core.getAlbumImage(inputStream)
            ncm.image = image

            ncm.outFile = outFilePath
            val outputStream = FileOutputStream(ncm.outFile)
            Core.musicData(inputStream, outputStream, key)
            System.out.format("转换成功文件：%s\n", outFilePath)
            val flag = combineFile(ncm)
            flag
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    private fun combineFile(ncm: Ncm): Boolean {
        return try {
            val audioFile = AudioFileIO.read(File(ncm.outFile))
            val tag = audioFile.tag
            tag.setField(FieldKey.ALBUM, ncm.mata.album)
            tag.setField(FieldKey.TITLE, ncm.mata.musicName)
            tag.setField(FieldKey.ARTIST, *ncm.mata.artist[0])
            val image = ImageIO.read(ByteArrayInputStream(ncm.image))
            if (image != null) {
                val coverArt = MetadataBlockDataPicture(ncm.image, 0, Utils.albumImageMimeType(ncm.image), "", image.width, image.height, if (image.colorModel.hasAlpha()) 32 else 24, 0)
                val artwork = ArtworkFactory.createArtworkFromMetadataBlockDataPicture(coverArt)
                tag.setField(tag.createField(artwork))
            }
            AudioFileIO.write(audioFile)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

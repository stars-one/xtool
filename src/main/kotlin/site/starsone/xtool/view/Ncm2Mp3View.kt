package site.starsone.xtool.view

import com.starsone.controls.common.*
import javafx.beans.property.SimpleStringProperty
import javafx.concurrent.Task
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import kfoenix.jfxbutton
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.flac.metadatablock.MetadataBlockDataPicture
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.images.ArtworkFactory
import site.starsone.xtool.utils.parseJsonToObject
import site.starsone.xtool.view.ncm2mp3.Core
import site.starsone.xtool.view.ncm2mp3.Utils
import site.starsone.xtool.view.ncm2mp3.mime.Mata
import site.starsone.xtool.view.ncm2mp3.mime.Ncm
import tornadofx.*
import java.io.ByteArrayInputStream
import java.io.File
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

                }
            }

        }



        tableview(controller.observableList) {
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
                action {

                }
            })
            jfxbutton {
                text = "打开目录"
                style {
                    backgroundColor += c("#2b3245")
                    textFill = c("white")
                }
                action {

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
                            showDialog(currentStage, "提示", "转换成功")
                        }
                    }
                }
            }
        }

        //todo 测试
        val tempNcmFileInfo = NcmFileInfo("文件保存目录文件保存目录文件保存目录.ncm", "D:\\temp\\test.ncm", "14.56MB", 1)
        controller.observableList.add(tempNcmFileInfo)
    }
}

class Ncm2Mp3ViewController : Controller() {
    val cacheFilePath = SimpleStringProperty("D:\\temp\\歌曲\\cache")

    val observableList = observableListOf<NcmFileInfo>()

    //val cacheFilePath = SimpleStringProperty("")
    val outputFilePath = SimpleStringProperty("D:\\temp\\歌曲\\output")

    fun startConvert() {
        /*val dirFile = File(cacheFilePath.value)
        if (dirFile.exists()) {

        }*/
        val flag = Ncm2Mp3Util.ncm2Mp3("D:\\temp\\test.ncm", "D:\\temp\\test_output.mp3")
        println(flag)
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
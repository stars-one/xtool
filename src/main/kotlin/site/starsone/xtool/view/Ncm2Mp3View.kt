package site.starsone.xtool.view

import com.starsone.controls.common.showDialog
import com.starsone.controls.common.showLoadingDialog
import com.starsone.controls.common.xChooseFileDirectory
import javafx.beans.property.SimpleStringProperty
import javafx.concurrent.Task
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.flac.metadatablock.MetadataBlockDataPicture
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.images.ArtworkFactory
import site.starsone.xtool.utils.parseJsonToObject
import site.starsone.xtool.view.ncm2mp3.Combine
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
    val controller by inject<Ncm2Mp4ViewController>()

    lateinit var task: Task<Unit>

    override val root = vbox {
        setPrefSize(500.0, 300.0)

        xChooseFileDirectory("选择或输入文件夹", controller.cacheFilePath)
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

class Ncm2Mp4ViewController : Controller() {
    val cacheFilePath = SimpleStringProperty("D:\\temp\\歌曲\\cache")

    //val cacheFilePath = SimpleStringProperty("")
    val outputFilePath = SimpleStringProperty("D:\\temp\\歌曲\\output")

    fun startConvert() {
        /*val dirFile = File(cacheFilePath.value)
        if (dirFile.exists()) {

        }*/
        val flag = Ncm2Mp3Util.ncm2Mp3("D:\\temp\\test.ncm","D:\\temp\\test_output.mp3")
        println(flag)
    }



}

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

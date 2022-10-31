package site.starsone.xtool.view

import com.starsone.controls.common.xUrlLink
import com.starsone.controls.utils.TornadoFxUtil
import javafx.geometry.Pos
import javafx.scene.control.ScrollPane
import javafx.scene.text.FontWeight
import tornadofx.*
import java.awt.Desktop
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.URI

class OCRView : BaseView() {

    val ocrController by inject<OcrController>()
    override val root = vbox {

        hbox {
            text("参考地址")
            xUrlLink("https://github.com/zh-h/Windows.Media.Ocr.Cli")
        }
        ocrController.getTextFormImage(File("D:\\temp\\Snipaste_2022-10-31_22-40-46.png"))
    }

}

class OcrController : Controller() {

    fun getTextFormImage(imgFile: File): Unit {

        val exePath = "D:\\project\\web\\XTool\\src\\main\\resources\\Windows.Media.Ocr.Cli.exe"
        val imgPath = imgFile.path
        val cmd = "$exePath $imgPath"

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
                        println(it)
                    }
                }
            } catch (e: IOException) {
            }
        }

    }
}

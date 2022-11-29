package site.starsone.xtool.view

import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.PageSize
import com.itextpdf.text.pdf.PdfWriter
import com.starsone.controls.common.showDialog
import com.starsone.controls.common.showLoadingDialog
import com.starsone.controls.common.xChooseFileDirectory
import javafx.beans.property.SimpleStringProperty
import javafx.concurrent.Task
import tornadofx.*
import java.io.*
import kotlin.math.roundToInt

/**
 * 多张图片合并为pdf文件
 * @author StarsOne
 * @url <a href="http://stars-one.site">http://stars-one.site</a>
 * @date Create in  2022/08/15 22:32
 *
 */
class Img2PdfView : View("多张图片转pdf文件") {
    val controller by inject<Img2PdfViewController>()

    lateinit var task: Task<File>

    override val root = vbox(10) {
        setPrefSize(500.0, 300.0)

        text("支持jpg和png两种格式图片,注意,图片文件夹图片需要以数字来进行命名,否则会出错"){
            style {
                fill = c("red")
            }
        }

        form {
            fieldset("图片文件夹") {
                field {
                    xChooseFileDirectory("选择或输入图片文件夹", controller.imgsDir)

                }
            }
            fieldset("pdf生成文件夹") {
                xChooseFileDirectory("选择或输入生成pdf文件夹", controller.outputFilePath)
            }
        }

        button("开始转换") {
            action {
                showLoadingDialog(currentStage, "提示", "合并中,请稍候...", "取消", { task.cancel(true) }) { alert ->
                    task = runAsync {
                        controller.startConvert()
                    } ui {
                        alert.hideWithAnimation()

                        showDialog(currentStage, "提示", "合并成功,文件输出路径为", it.path, false)
                    }
                }
            }
        }

        requestFocus()
    }
}

class Img2PdfViewController : Controller() {
    val imgsDir = SimpleStringProperty("")

    //val cacheFilePath = SimpleStringProperty("")
    val outputFilePath = SimpleStringProperty("")

    fun startConvert(): File {
        //多个图片合成pdf
        val file = File(imgsDir.value)

        val doc = Document(PageSize.A4, 0f, 0f, 0f, 0f) //new一个pdf文档
        val dirFile = File(outputFilePath.value)
        if (!dirFile.exists()) {
            dirFile.mkdirs()
        }

        val outputFile = File(dirFile, "output.pdf")
        PdfWriter.getInstance(doc, FileOutputStream(outputFile))
        //排序
        val list = file.listFiles().filter {
            it.extension.toLowerCase() == "png" || it.extension.toLowerCase() == "jpg"
        }.sortedBy {
            it.nameWithoutExtension.toInt()
        }

        doc.open()
        list.forEach {
            val imgFile = it
            val png1 = Image.getInstance(imgFile.toURI().toURL())

            val heigth: Float = png1.height
            val width: Float = png1.width
            val percent: Int = getPercent2(heigth, width)
            png1.alignment = Image.MIDDLE
            png1.scalePercent(percent.plus(3).toFloat()) // 表示是原来图像的比例;
            doc.add(png1)
        }

        doc.close()
        return outputFile
    }

    fun getPercent(h: Float, w: Float): Int {
        var p = 0
        var p2 = 0.0f
        p2 = if (h > w) {
            297 / h * 100
        } else {
            210 / w * 100
        }
        p = p2.roundToInt()
        return p
    }

    fun getPercent2(h: Float, w: Float): Int {
        var p = 0
        var p2 = 0.0f
        p2 = 530 / w * 100
        p = p2.roundToInt()
        return p
    }


}

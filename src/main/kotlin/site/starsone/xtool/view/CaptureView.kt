package site.starsone.xtool.view

import com.github.hui.quick.plugin.qrcode.constants.QuickQrUtil
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.layout.Background
import javafx.scene.paint.Paint
import javafx.stage.Screen
import javafx.stage.StageStyle
import site.starsone.xtool.utils.QrCodeUtils
import site.starsone.xtool.utils.ScreenProperties
import tornadofx.*
import java.awt.Rectangle
import java.awt.Robot

/**
 * 截图的遮罩View
 * @author StarsOne
 * @url <a href="http://stars-one.site">http://stars-one.site</a>
 * @date Create in  2022/01/02 17:19
 *
 */
class CaptureView : View("My View") {
    val viewModel by inject<CaptureViewModel>()

    override val root = anchorpane {


        style {
            backgroundColor += c("#B5B5B522")
        }

        setOnMousePressed {
            println("外部的press事件")
            //重置数据
            viewModel.reset()

            val y = it.sceneY
            val x = it.sceneX
            viewModel.xProperty.set(x)
            viewModel.yProperty.set(y)
        }

        setOnMouseReleased {
            val endX = it.sceneX
            val endY = it.sceneY
            val x = viewModel.xProperty.value
            val y = viewModel.yProperty.value
            val height = endY - y
            val width = endX - x

            viewModel.widthProperty.set(width)
            viewModel.heightProperty.set(height)
        }


        rectangle {
            xProperty().bind(viewModel.xProperty)
            yProperty().bind(viewModel.yProperty)
            widthProperty().bind(viewModel.widthProperty)
            heightProperty().bind(viewModel.heightProperty)
            style {
                strokeWidth = 1.px
                stroke = c("red")
                fill = c("#000000", 0.0)
            }

            setOnMousePressed {
                println("矩形内部press事件")
            }
        }
        hbox {
            button("关闭").action {
                currentStage?.hide()
                primaryStage.isIconified = false
            }

            button("确定").action {
                currentStage?.hide()
                primaryStage.isIconified = false
                //具体逻辑 图片
                val robot = Robot()
                val image = robot.createScreenCapture(viewModel.createRectangle())
                val result = QrCodeUtils.getContentByQr(image)
                println(result)
            }
        }

    }

    override fun onBeforeShow() {
        super.onBeforeShow()
        primaryStage.isIconified = true

        currentStage?.apply {
            scene.fill = Paint.valueOf("#ffffff03")
            fullScreenExitHint = ""
            initStyle(StageStyle.TRANSPARENT)
            isFullScreen = true
            val screenProperties = ScreenProperties(currentStage, ScreenProperties.DEFAULT)

            x = screenProperties.fullScreenX
            y = screenProperties.fullScreenY
            show()
        }

    }
}

class CaptureViewModel : ViewModel() {
    var xProperty = SimpleDoubleProperty(0.0)
    var yProperty = SimpleDoubleProperty(0.0)
    var widthProperty = SimpleDoubleProperty(0.0)
    var heightProperty = SimpleDoubleProperty(0.0)

    fun reset() {
        xProperty.set(0.0)
        yProperty.set(0.0)
        widthProperty.set(0.0)
        heightProperty.set(0.0)
    }

    fun createRectangle(): Rectangle {
        return Rectangle(xProperty.intValue(), yProperty.intValue(), widthProperty.intValue(), heightProperty.intValue())
    }
}

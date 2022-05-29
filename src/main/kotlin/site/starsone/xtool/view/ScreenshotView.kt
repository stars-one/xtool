package site.starsone.xtool.view

import com.melloware.jintellitype.JIntellitype
import com.starsone.controls.common.TornadoFxUtil
import javafx.stage.Screen
import site.starsone.xtool.utils.ScreenProperties
import tornadofx.*
import java.awt.Rectangle
import java.awt.Robot
import java.io.File
import javax.imageio.ImageIO

class ScreenshotView : BaseView() {

    override var root = vbox {

        setPrefSize(500.0, 300.0)

        menubar {
            menu("关于") {
                item("软件说明") {
                    action {
                        find(AboutView::class).openModal()
                    }
                }
            }
        }

        button("截图") {
            action {

                val robot = Robot()
                //右键
                //robot.mousePress(InputEvent.BUTTON3_DOWN_MASK)
                //robot.mouseMove(500,600)
                //robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK)


                //robot.keyPress(KeyEvent.VK_CONTROL)
                //robot.keyPress(KeyEvent.VK_V)
                //robot.keyRelease(KeyEvent.VK_V)
                //robot.keyRelease(KeyEvent.VK_CONTROL)
                val tangle = Rectangle(400, 400, 200, 200)
                val img = robot.createScreenCapture(tangle)
                ImageIO.write(img, "png", File("D:\\temp\\ss.png"))
            }
        }
        shortCut()
        button("截图识别二维码").action {
            //find<CaptureView>().openWindow()
            //注意:使用此方法,可以保证窗口是位于顶层

            find<CaptureView>().openModal()

        }
        button("测试屏幕").action {
            val screenProperties1 = ScreenProperties(currentStage, ScreenProperties.DEFAULT)
            println(screenProperties1.toString())
            println("-------")
            var screenProperties = ScreenProperties(currentStage, ScreenProperties.FIRST_SCREEN)
            println(screenProperties.toString())
            screenProperties = ScreenProperties(currentStage, ScreenProperties.SECOND_SCREEN)
            println(screenProperties.toString())
        }
    }

    fun shortCut() {
        val keyCode = 'I'.toInt()
        //println(keyCode) ctrl+shift+I快捷键
        JIntellitype.getInstance().registerHotKey(1, JIntellitype.MOD_CONTROL + JIntellitype.MOD_SHIFT, keyCode)

        JIntellitype.getInstance().addHotKeyListener {
            if (it == 1) {
                val screenRectangle = Screen.getPrimary().bounds
                val width = screenRectangle.width
                val height = screenRectangle.height
                println("width:$width,height:$height")
            }
        }
    }

}

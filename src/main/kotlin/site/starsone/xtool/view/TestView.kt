package site.starsone.xtool.view

import com.jfoenix.controls.JFXBadge
import com.starsone.controls.common.setActionHank
import javafx.beans.property.SimpleIntegerProperty
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.text.Text
import kfoenix.jfxbadge
import kfoenix.jfxbutton
import kfoenix.jfxtextfield
import site.starsone.xtool.app.Styles
import tornadofx.*

class TestView : View("Hello TornadoFX") {
    val model by inject<MainViewModel>()

    override val root = vbox() {
        prefWidth = 400.0
        prefHeight = 200.0

        val result = -5
        val r = result.takeIf { result >= 0 } ?: 0
        println(r)


        val badge = JFXBadge(Label("hello"))
        badge.setPosition(Pos.TOP_RIGHT);
        badge.setPrefHeight(100.0)
        badge.setMaxWidth(110.0)
        this+=badge
        button("aa").action{
            badge.refreshBadge()
        }

        vbox {
            val list = listOf(LeftMenuItem("首页"), LeftMenuItem("下载列表"), LeftMenuItem("资源搜索"))
            list.forEachIndexed { index, leftMenuItem ->
                button(leftMenuItem.title) {
                    userData = System.currentTimeMillis()
                    addClass(Styles.leftMenu)
                    toggleClass(Styles.leftMenuSelect, model.selectIndex.eq(index))
                    setActionHank {
                        model.selectIndex.set(index)
                    }
                }

            }
        }

        hbox {
            text(model.selectIndex.asString())
        }

    }
}

class MainViewModel : ViewModel() {
    val selectIndex = SimpleIntegerProperty(0)
}

data class LeftMenuItem(val title: String)

package site.starsone.xtool.view

import com.google.gson.Gson
import com.jfoenix.controls.JFXBadge
import javafx.beans.property.SimpleIntegerProperty
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.paint.Color
import javafx.scene.text.Text
import javafx.stage.Modality
import kfoenix.jfxbadge
import kfoenix.jfxbutton
import kfoenix.jfxtextfield
import site.starsone.xtool.app.Styles
import tornadofx.*

class HomeView : BaseView() {

    override val root = vbox() {
        prefWidth = 600.0
        prefHeight = 400.0

        menubar {
            menu("关于") {
                item("软件说明"){
                    action {
                        find(AboutView::class).openModal()
                    }
                }
            }
        }

        val pluginConfig = Gson().fromJson(resources.text("/plugins.json"), PluginConfig::class.java)
        val pluginCategory = pluginConfig.plugins
        pluginCategory.forEach {
            vbox {
                label(it.name)
                flowpane{
                    it.list.forEach { plugin->
                        button(plugin.name) {
                            addClass(Styles.optionMenu)
                            action{
                                val viewClass = Class.forName(plugin.mainClass)
                                val method = viewClass.getMethod("openModal")
                                val myObject = viewClass.newInstance()
                                method.invoke(myObject)

                                //todo 使用openModal导致文件选择对话框会置于底层...
                            }
                        }
                    }
                }
            }
        }
        /*vbox {
            label("开发")
            flowpane{
                repeat(5) {
                    button("依赖互转") {
                        addClass(Styles.optionMenu)
                        action{
                            val viewClass = Class.forName("site.starsone.xtool.view.DependencyView")
                            val method = viewClass.getMethod("openWindow")
                            val myObject = viewClass.newInstance()
                            method.invoke(myObject)
                        }
                    }
                }
            }
        }*/
    }
}


data class PluginConfig(
    val plugins: List<PluginCategory>
) {
    data class PluginCategory(
        val icon: String,
        val list: List<Plugin>,
        val name: String
    ) {
        data class Plugin(
            val mainClass: String,
            val name: String
        )
    }
}

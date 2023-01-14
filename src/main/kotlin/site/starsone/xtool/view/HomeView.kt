package site.starsone.xtool.view

import com.google.gson.Gson
import com.jfoenix.controls.JFXBadge
import com.starsone.controls.common.DialogBuilder
import com.starsone.controls.common.DownloadDialogView
import com.starsone.controls.common.showToast
import com.starsone.controls.model.UpdateInfo
import com.starsone.controls.utils.TornadoFxUtil
import javafx.beans.property.SimpleIntegerProperty
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import javafx.scene.text.Text
import javafx.stage.Modality
import kfoenix.jfxbadge
import kfoenix.jfxbutton
import kfoenix.jfxtextfield
import org.jsoup.Jsoup
import site.starsone.xtool.app.Styles
import site.starsone.xtool.utils.GithubVersionUpdate
import tornadofx.*

class HomeView : BaseView() {

    override val root = vbox() {
        prefWidth = 800.0
        prefHeight = 600.0

        menubar {
            menu("关于") {
                item("检测更新") {
                    action {
                        DialogBuilder(currentStage)
                                .setTitle("检测更新")
                                .setLoadingMessage("新版本检测中") { alert ->
                                    runAsync {
                                        GithubVersionUpdate.checkVersion(descData) {
                                            alert.hideWithAnimation()
                                            if (it.tagName == descData.version.substringAfter("v")) {
                                                runLater {
                                                    showToast(this@vbox, "当前已是最新版本")
                                                    alert.hideWithAnimation()
                                                }
                                            } else {
                                                runLater {
                                                    val dialogBuilder = DialogBuilder(currentStage)
                                                            .setTitle("发现新版本")
                                                            .setMessage(it.content)
                                                            .setPositiveBtn("升级") {
                                                                DownloadDialogView(currentStage, it.downloadUrl, it.fileName).show()
                                                            }
                                                    dialogBuilder.setNegativeBtn("取消"){
                                                        alert.hideWithAnimation()
                                                    }.create()
                                                }
                                            }
                                        }
                                    }
                                }
                                .setPositiveBtn("取消")
                                .create()
                    }
                }
                item("软件说明") {
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
                hbox(5) {
                    padding = insets(5)
                    alignment = Pos.CENTER_LEFT
                    imageview("/img/title-head.png") {
                        fitHeight = 30.0
                        fitWidth = 30.0
                    }
                    label(it.name) {
                        style {
                            fontWeight = FontWeight.BOLD
                            fontSize = 18.px
                        }
                    }
                }

                flowpane {
                    it.list.forEach { plugin ->
                        vbox {
                            padding = insets(5)
                            button(plugin.name).apply {
                                addClass(Styles.optionMenu)
                                action {
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

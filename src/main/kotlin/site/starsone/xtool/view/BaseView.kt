package site.starsone.xtool.view


import com.starsone.controls.common.TornadoFxUtil
import javafx.scene.layout.Pane
import javafx.stage.Stage
import site.starsone.xtool.app.AppModel
import tornadofx.*

/**
 * 抽象类,需要被继承(有参数设置标题,无参数则默认appName+version+author信息拼接
 */
abstract class BaseView : View {
    val appModel: AppModel by inject()
    val descData = appModel.descData

    constructor(title: String) {
        this.title = title
    }

    constructor()

    override fun onBeforeShow() {
        //根据情况不同设置窗口标题
        if (title.isBlank()) {
            val data = appModel.descData
            title = data.appName + " " + data.version + " " + "by " + data.author
        }

    }

    fun checkVersion(stage: Stage?,pane:Pane) {
        TornadoFxUtil.checkVersion(stage,descData.updateUrl, descData.appName,descData.versionCode,descData.version,pane)
    }
}
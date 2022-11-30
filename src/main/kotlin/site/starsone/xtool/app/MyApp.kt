package site.starsone.xtool.app


import com.google.gson.Gson
import javafx.scene.image.Image
import javafx.stage.Stage
import site.starsone.xtool.model.DescData

import site.starsone.xtool.view.HomeView
import tornadofx.*


class MyApp : App(HomeView::class, Styles::class) {
    //全局消息存放
    private val model = AppModel()

    override fun start(stage: Stage) {
        super.start(stage)
        //存放model
        setInScope(model, scope)
        //设置图标
        stage.icons += Image(model.descData.icon)

        importStylesheet("/css/tableview.css")
    }
}

/**
 * 全局的消息存放
 */
class AppModel : ViewModel() {
    val descData = Gson().fromJson(resources.text("/desc.json"), DescData::class.java)

}

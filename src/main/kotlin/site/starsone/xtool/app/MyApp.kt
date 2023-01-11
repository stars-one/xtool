package site.starsone.xtool.app


import com.google.gson.Gson
import com.starsone.controls.utils.TornadoFxUtil
import javafx.scene.image.Image
import javafx.stage.Stage
import site.starsone.kxorm.db.KxDb
import site.starsone.kxorm.db.KxDbConnConfig
import site.starsone.xtool.model.DescData
import site.starsone.xtool.view.CnblogImgInfo

import site.starsone.xtool.view.HomeView
import tornadofx.*
import java.io.File


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

        //初始化数据库
         val kclass = CnblogImgInfo::class
        val dbDirFile = File(TornadoFxUtil.getCurrentJarDirPath(), "db")
        if (!dbDirFile.exists()) {
            dbDirFile.mkdirs()
        }
        val dbUrl = "jdbc:h2:${dbDirFile.path}/test"
        val user = ""
        val pwd = ""

        println("初始化h2database:$dbUrl")
        val kxDbConnConfig = KxDbConnConfig(dbUrl, user, pwd).registerClass(kclass)
        KxDb.init(kxDbConnConfig)
    }
}

/**
 * 全局的消息存放
 */
class AppModel : ViewModel() {
    val descData = Gson().fromJson(resources.text("/desc.json"), DescData::class.java)

}

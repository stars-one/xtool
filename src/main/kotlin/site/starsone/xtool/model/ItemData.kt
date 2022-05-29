package site.starsone.xtool.model

data class DescData(
        val appName: String,
        val desc: String,
        val icon:String,
        val author: String,
        val blogUrl: String,
        val updateUrl:String,
        val githubUrl: String,
        val qq: String,
        val versionCode:Int,
        val qqGroup: String,
        val updateMessage: List<UpdateMessage>,
        val version: String
)

data class UpdateMessage(
        val message: List<String>,
        val version: String
)
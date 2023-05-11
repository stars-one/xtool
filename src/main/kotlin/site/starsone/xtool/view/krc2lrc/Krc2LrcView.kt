package site.starsone.xtool.view.krc2lrc

import tornadofx.*
import java.io.File

/**
 *
 * @author StarsOne
 * @Date Create in  2023/05/12 01:55
 *
 */
class Krc2LrcView : View("My View") {
    override val root = borderpane {
        //todo
        //转换歌曲文件夹

        //转换歌曲文件夹
        val krcFile = File("D:\\temp\\krc\\鈴華ゆう子 - 永世のクレイドル (永世的摇篮)-86c08c794e4fd454bde7a7d868ef28e2-50060051-00000000.krc")
        val lrcFile = File(krcFile.parent, "output.lrc")
        Krc2LrcUtil.krc2lrc(krcFile, lrcFile);

    }
}

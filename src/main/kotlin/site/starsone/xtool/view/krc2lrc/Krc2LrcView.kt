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

        text("开发中...")

        //转换歌曲文件夹

        //test()

    }
    fun test() {
        val krcFile = File("D:\\Download\\其他杂\\Across the Destiny - 仲村芽衣子-594c1357724245cb2b21d5ffa054198f-16026930-00000000.krc")
        val lrcFile = File(krcFile.parent, "output.lrc")
        Krc2LrcUtil.krc2lrc(krcFile, lrcFile);
    }
}

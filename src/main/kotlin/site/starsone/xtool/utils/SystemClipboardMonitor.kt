package site.starsone.xtool.utils

import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.ClipboardOwner
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable

/**
 *
 * @author StarsOne
 * @url <a href="http://stars-one.site">http://stars-one.site</a>
 * @date Create in  2021/10/10 15:38
 *
 */
class SystemClipboardMonitor : ClipboardOwner {
    private val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    private var isOpenListener = true

    var listener: GlobalClipBoardListener? = null

    init {
        clipboard.setContents(clipboard.getContents(null), this)
    }

    override fun lostOwnership(clipboard: Clipboard?, contents: Transferable?) {
        try {
            Thread.sleep(1)
            var text = ""
            if (clipboard?.isDataFlavorAvailable(DataFlavor.stringFlavor) == true) {
                text = clipboard.getData(DataFlavor.stringFlavor).toString()
            }
            clipboard?.setContents(clipboard.getContents(null), this)
            if (isOpenListener) {
                listener?.onCopy(text, clipboard, contents)
            }
        } catch (e: IllegalStateException) {
            //有时候会出现剪切板被占用就会出现此异常
        }
    }

    /**
     * 移除监听
     *
     */
    fun stopListen() {
        isOpenListener = false
    }

    /**
     * 开启监听
     *
     */
    fun startListen() {
        isOpenListener = true
    }

    fun addClipboardListener(listener: GlobalClipBoardListener) {
        this.listener = listener
    }
}

interface GlobalClipBoardListener {
    fun onCopy(text: String?, clipboard: Clipboard?, contents: Transferable?)
}


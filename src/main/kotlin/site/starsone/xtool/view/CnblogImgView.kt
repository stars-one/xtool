package site.starsone.xtool.view


import cn.hutool.core.codec.Base64
import cn.hutool.core.codec.Base64Encoder
import cn.hutool.core.io.FileUtil
import com.starsone.controls.utils.GlobalDataConfig
import com.starsone.controls.utils.GlobalDataConfigUtil
import de.timroes.axmlrpc.XMLRPCClient
import de.timroes.axmlrpc.XMLRPCException
import javafx.embed.swing.SwingFXUtils
import javafx.geometry.Orientation
import javafx.scene.control.TextArea
import javafx.scene.input.*
import javafx.stage.Window
import kfoenix.jfxtextfield
import tornadofx.*
import java.awt.Image
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URL
import javax.imageio.ImageIO


/**
 * 博客园上传图片
 */
class CnblogImgView() : BaseView("博客园图片上传") {

    val cnblogImgViewModel by inject<CnblogImgViewModel>()
    var textArea by singleAssign<TextArea>()

    val imgFileType = arrayOf("png", "jpg", "jpeg", "gif")

    override val root = vbox {

        padding = insets(10)

        form {
            fieldset(labelPosition = Orientation.HORIZONTAL) {
                hbox {
                    field("用户名") {
                        jfxtextfield(cnblogImgViewModel.username) { }
                    }
                    field("密码") {
                        jfxtextfield(cnblogImgViewModel.pwd) { }
                    }
                }
            }
            fieldset {
                field() {
                    button("登录") {
                        fitToParentWidth()
                        action {
                            val resultPair = CnblogImgUtil.login(cnblogImgViewModel.username.value, cnblogImgViewModel.pwd.value)
                            println("登录状态: " + resultPair.first)
                        }
                    }
                }
            }
        }

        text("拖动图片文件到下面输入框或在输入框里按ctrl+v快捷键粘贴图片")
        textArea = textarea {
            prefWidth = 200.0
            prefHeight = 200.0

            setOnDragOver {
                it.acceptTransferModes(*TransferMode.ANY)
            }

            setOnDragExited {
                val dragboard = it.dragboard
                val flag = dragboard.hasFiles()
                val fileList = arrayListOf<File>()
                if (flag) {
                    val files = dragboard.files
                    //如果是文件夹,遍历所有子目录
                    val dirFiles = files.filter { it.isDirectory }
                    dirFiles.forEach {
                        val tempFiles = FileUtil.loopFiles(it) {
                            val extension = it.extension.toLowerCase()
                            imgFileType.contains(extension)
                        }
                        fileList.addAll(tempFiles)
                    }

                    val newFiles = files.filter { it.isFile }.filter {
                        val extension = it.extension.toLowerCase()
                        imgFileType.contains(extension)
                    }
                    fileList.addAll(newFiles)

                    runAsync {
                        CnblogImgUtil.login(cnblogImgViewModel.username.value, cnblogImgViewModel.pwd.value, false)
                        fileList.forEach {
                            val result = CnblogImgUtil.uploadImgToCnblog(it)
                            //todo 成功后的提示效果
                            println(result)
                        }
                    }
                }
            }
        }

    }

    override fun onBeforeShow() {
        addShortcut(KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN), currentWindow) {
            if (textArea.isFocused) {
                //用awt的包,兼容截图软件出来的图片
                val sysc = Toolkit.getDefaultToolkit().systemClipboard
                val cc = sysc.getContents(null)
                if (cc == null) {
                    return@addShortcut
                }
                when {
                    //图片
                    cc.isDataFlavorSupported(DataFlavor.imageFlavor) -> {
                        val image = cc.getTransferData(DataFlavor.imageFlavor) as Image
                        image.let {
                            runAsync {
                                val bufferedImage = BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB)
                                val g = bufferedImage.createGraphics()
                                g.drawImage(image, null, null)

                                //转换为字节流
                                val byteOs = ByteArrayOutputStream()
                                ImageIO.write(bufferedImage, "png", byteOs)

                                CnblogImgUtil.login(cnblogImgViewModel.username.value, cnblogImgViewModel.pwd.value)
                                val result = CnblogImgUtil.uploadImgToCnblog(byteOs.toByteArray())
                                result
                            } ui {
                                //todo 成功后的提示效果
                                println(it)
                            }
                        }
                    }
                    //文件的处理方式
                    cc.isDataFlavorSupported(DataFlavor.javaFileListFlavor) -> {
                        val transferData = cc.getTransferData(DataFlavor.javaFileListFlavor)
                        val files = transferData as List<File>
                        //筛选文件列表里的图片文件
                        val imgFiles = files.filter { it.isFile && imgFileType.contains(it.extension) }

                        runAsync {
                            //所有图片上传
                            CnblogImgUtil.login(cnblogImgViewModel.username.value, cnblogImgViewModel.pwd.value)
                            val resultList = imgFiles.map {
                                CnblogImgUtil.uploadImgToCnblog(it)
                            }
                            resultList
                        } ui {
                            //todo 成功后的提示效果
                            println(it.toString())
                        }
                    }
                }

            }
        }
    }


    /**
     * 设置页面全局快捷键（焦点在页面才会触发) **（此方法需要在`onBeforeShow()`方法中调用）**
     * @param keyCodeCombination 快捷键，如 ctrl+alt+c快捷键为`KeyCodeCombination(KeyCode.C, KeyCombination.ALT_DOWN, KeyCombination.CONTROL_DOWN)`
     * @param currentWindow 当前窗口
     */
    fun addShortcut(keyCodeCombination: KeyCombination, currentWindow: Window?, lambda: () -> Unit) {
        val kc1 = keyCodeCombination
        currentWindow?.addEventFilter(KeyEvent.KEY_PRESSED) { event ->
            if (kc1.match(event)) {
                lambda.invoke()
            }
        }
    }

    object GlobalConstant {
        const val CNBLOG_USER_NAME = "userName"
        const val CNBLOG_USER_PWD = "pwd"
    }

    /**
     * 全局的设置
     */
    object GlobalSetting {
        //cnblog的账号和密码
        var userName = GlobalDataConfig(GlobalConstant.CNBLOG_USER_NAME, "")
        var pwd = GlobalDataConfig(GlobalConstant.CNBLOG_USER_PWD, "")

    }
}

class CnblogImgViewModel : ViewModel() {
    //val username = SimpleStringProperty("")
    //val pwd = SimpleStringProperty("")
    val username = GlobalDataConfigUtil.getSimpleStringProperty(CnblogImgView.GlobalSetting.userName)
    val pwd = GlobalDataConfigUtil.getSimpleStringProperty(CnblogImgView.GlobalSetting.pwd)

}

object CnblogImgUtil {


    private var username = ""
    private var pwd = ""
    private var url = ""

    var isLogin = false

    /**
     * 先进行登录验证!!
     *
     * @return Pair<Boolean, String> true,登录成功;false,登录失败,并给回失败原因
     */
    fun login(userName: String, pwd: String, needCheck: Boolean = false): Pair<Boolean, String> {
        if (!isLogin) {
            //判断是否研究登录,没有则重新进行登录
            this.username = userName
            this.pwd = pwd
            this.url = "https://rpc.cnblogs.com/metaweblog/$username"
        }
        if (needCheck) {
            return try {
                val client = XMLRPCClient(URL(url))
                val result = client.call("blogger.getUsersBlogs", "", username, pwd) as Array<*>
                isLogin = true
                Pair(true, "")
            } catch (e: XMLRPCException) {
                isLogin = false
                Pair(false, e.message ?: "")
            }
        } else {
            return Pair(true, "")
        }

    }

    /**
     * @param type 1:输出为md图片链接 0：普通图片链接
     */
    fun uploadImgToCnblog(bytes: ByteArray, type: Int = 1): String {
        val base64 = bytes
        val client = XMLRPCClient(URL(url))
        val imgName = "name.png"
        val imgType = "png"

        val file = hashMapOf<String, Any>("bits" to base64, "name" to imgName, "type" to imgType)

        try {
            val result = client.call("metaWeblog.newMediaObject", "", username, pwd, file) as HashMap<String, String>
            val url = result["url"] ?: return ""
            if (type == 1) {
                return "![]($url)"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
        return url
    }

    /**
     * @param type 1:输出为md图片链接 0：普通图片链接
     */
    fun uploadImgToCnblog(imgFile: File, type: Int = 1): String {
        val base64 = imgFile.readBytes()

        val client = XMLRPCClient(URL(url))
        val imgName = imgFile.name
        val imgType = imgFile.extension

        val file = hashMapOf<String, Any>("bits" to base64, "name" to imgName, "type" to imgType)

        try {
            val result = client.call("metaWeblog.newMediaObject", "", username, pwd, file) as HashMap<String, String>
            val url = result["url"] ?: return ""
            if (type == 1) {
                return "![]($url)"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
        return url
    }
}


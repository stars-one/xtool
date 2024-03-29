package site.starsone.xtool.view


import cn.hutool.core.date.DateTime
import cn.hutool.core.io.FileUtil
import com.jfoenix.controls.JFXButton
import com.starsone.controls.common.remixIconButton
import com.starsone.controls.common.remixIconText
import com.starsone.controls.common.xUrlLink
import com.starsone.controls.utils.GlobalDataConfig
import com.starsone.controls.utils.GlobalDataConfigUtil
import com.starsone.controls.utils.TornadoFxUtil
import com.starsone.controls.view.AlertLevel
import com.starsone.controls.view.XMessage
import de.timroes.axmlrpc.XMLRPCClient
import de.timroes.axmlrpc.XMLRPCException
import javafx.geometry.Orientation
import javafx.scene.control.TextArea
import javafx.scene.input.*
import javafx.stage.Window
import kfoenix.jfxbutton
import kfoenix.jfxtextfield
import site.starsone.kxorm.annotation.TableColumnPk
import site.starsone.kxorm.db.KxDb
import tornadofx.*
import java.awt.Image
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URL
import java.util.*
import javax.imageio.ImageIO
import kotlin.collections.HashMap


/**
 * 博客园上传图片
 */
class CnblogImgView() : BaseView("博客园图片上传") {

    val cnblogImgViewModel by inject<CnblogImgViewModel>()
    var textArea by singleAssign<TextArea>()

    var xMessage by singleAssign<XMessage>()

    val imgFileType = arrayOf("png", "jpg", "jpeg", "gif")

    override val root = stackpane {
        style{
            backgroundColor+=c("white")
        }

        vbox(10.0) {

            padding = insets(10)

            form {
                fieldset(labelPosition = Orientation.HORIZONTAL) {
                    hbox {
                        field("MetaWeblog登录名") {
                            jfxtextfield(cnblogImgViewModel.username) { }
                        }
                        field("MetaWeblog访问令牌") {
                            jfxtextfield(cnblogImgViewModel.pwd) { }
                        }
                    }
                }
                fieldset {
                    field() {
                        jfxbutton("核验登录名和访问令牌是否正确",btnType = JFXButton.ButtonType.RAISED) {
                            style{
                                backgroundColor+=c("#1890ff")
                                textFill =c("white")
                            }
                            fitToParentWidth()
                            action {
                                runAsync {
                                    CnblogImgUtil.login(cnblogImgViewModel.username.value, cnblogImgViewModel.pwd.value, true)
                                } ui {
                                    if (it.first) {
                                        xMessage.create("核验成功", AlertLevel.SUCCESS)
                                    } else {
                                        xMessage.create(it.second, AlertLevel.DANGER)
                                    }
                                }
                            }
                        }
                    }
                    field{
                        textflow {
                            text("具体可前往")
                            xUrlLink("https://i.cnblogs.com/settings")
                            text("登录博客园后台,在设置中的最后可以看到MetaBlog的相关参数")
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
                            runLater {
                                xMessage.create("图片上传中,请稍候...", AlertLevel.INFO)
                            }

                            CnblogImgUtil.login(cnblogImgViewModel.username.value, cnblogImgViewModel.pwd.value, false)
                            val resultList = fileList.map {
                                CnblogImgUtil.uploadImgToCnblog(it)
                            }
                            resultList
                        } ui {
                            imgUploadTip(it)
                        }
                    }
                }
                requestFocus()
            }

            jfxbutton {
                graphic = remixIconText("delete-bin-fill", fontColor = c("white"))
                text = "清空本地记录"
                style {
                    backgroundColor += c("#875a1a")
                    textFill = c("white")
                }
                action {
                    KxDb.deleteAll(CnblogImgInfo::class)
                    cnblogImgViewModel.myList.clear()
                }
            }


            tableview(cnblogImgViewModel.myList) {
                prefHeight = 300.0
                readonlyColumn("图片地址", CnblogImgInfo::url) {
                    prefWidth = 550.0
                    cellFormat {
                        val url = it
                        val hyperlink = cache {
                            hbox(10.0) {
                                remixIconText("file-copy-2-fill", fontColor = c("#1890ff")) {
                                    tooltip = tooltip("复制MD格式图片链接")
                                    setOnMouseClicked {
                                        TornadoFxUtil.copyTextToClipboard("![]($url)")
                                        xMessage.create("复制成功", AlertLevel.SUCCESS)
                                    }
                                }
                                xUrlLink(it)
                            }
                        }
                        graphic = hyperlink
                    }
                }
                readonlyColumn("创建时间", CnblogImgInfo::createTime) {
                    prefWidth = 200.0
                    cellFormat {
                        text = DateTime.of(it).toString()
                    }
                }
            }

            runAsync {
                val list = KxDb.getQueryList(CnblogImgInfo::class)
                list
            } ui {
                cnblogImgViewModel.addListData(it)
            }
        }

        //这个必须放在最后
        xMessage = XMessage.bindingContainer(this)
    }

    /**
     * 图片上传成功后的提示
     *
     * @param result
     */
    private fun imgUploadTip(result: List<String>) {
        val it = result
        if (it.isNotEmpty()) {
            //保存到数据库
            saveBeanList(it)
            //复制图片MD格式的链接
            TornadoFxUtil.copyTextToClipboard(it.first())
            xMessage.create("上传成功,已复制图片链接", AlertLevel.SUCCESS)
        } else {
            xMessage.create("上传失败,请重新操作", AlertLevel.DANGER)
        }
    }

    /**
     * 图片上传成功后的提示
     *
     * @param result
     */
    private fun imgUploadTip(result: String) {
        val it = result
        if (it.isNotEmpty()) {
            //保存在数据库
            saveBean(it)
            //复制图片MD格式的链接
            TornadoFxUtil.copyTextToClipboard(it)
            xMessage.create("上传成功,已复制图片链接", AlertLevel.SUCCESS)
        } else {
            xMessage.create("上传失败,请重新操作", AlertLevel.DANGER)
        }
    }

    private fun saveBean(imgUrl: String) {
        val str = imgUrl.subStringBetween("(", ")")
        val info = CnblogImgInfo(UUID.randomUUID().toString(), str)
        KxDb.insert(info)
        cnblogImgViewModel.addData(info)
    }

    private fun saveBeanList(list: List<String>) {
        val infoList = list.map {
            println(it)
            val str = it.subStringBetween("(", ")")
            val info = CnblogImgInfo(UUID.randomUUID().toString(), str)
            info
        }
        KxDb.insert(infoList)
        cnblogImgViewModel.addListData(infoList)
    }

    override fun onBeforeShow() {


        //设置监听ctrl+v的快捷键
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
                                runLater {
                                    xMessage.create("图片上传中,请稍候...", AlertLevel.INFO)
                                }

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
                                imgUploadTip(it)

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
                            runLater {
                                xMessage.create("图片上传中,请稍候...", AlertLevel.INFO)
                            }

                            //所有图片上传
                            CnblogImgUtil.login(cnblogImgViewModel.username.value, cnblogImgViewModel.pwd.value)
                            val resultList = imgFiles.map {
                                CnblogImgUtil.uploadImgToCnblog(it)
                            }
                            resultList
                        } ui {
                            imgUploadTip(it)
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

    var myList = observableListOf<CnblogImgInfo>()

    fun addData(info: CnblogImgInfo) {
        myList.add(info)
    }

    fun addListData(infoList: List<CnblogImgInfo>) {
        myList.addAll(infoList)
    }
}

data class CnblogImgInfo(@TableColumnPk var id: String, var url: String, var createTime: Date = Date())

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
            //判断是否登录,没有则重新进行登录
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


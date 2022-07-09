package site.starsone.xtool.view

import com.starsone.controls.common.xUrlLink
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.ToggleGroup
import kfoenix.jfxradiobutton
import site.starsone.xtool.utils.NavicatDecryptUtil
import tornadofx.*
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory


/**
 *  Navicat解密获取密码
 * @author StarsOne
 * @href <a href="http://stars-one.site">http://stars-one.site</a>
 * @date Create in  2022/07/06 22:25
 *
 */
class NavicatPwdView : BaseView("Navicat数据库密码查看") {
    val controller by inject<NacicatPwdViewController>()

    var toggleGroup by singleAssign<ToggleGroup>()


    override val root = vbox {
        //参考 https://gitee.com/lzy549876/navicat_password_decrypt

        padding = insets(10)

        prefWidth = 800.0
        hbox(10.0) {
            label("选择版本")
            toggleGroup = togglegroup() {
                val defaultType = 2
                jfxradiobutton("navicat12+") {
                    isSelected = 2 == defaultType
                    setOnAction {
                        controller.type.set(2)
                    }
                }
                jfxradiobutton("navicat11") {
                    isSelected = 1 == defaultType
                    setOnAction {
                        controller.type.set(1)
                    }
                }
            }
        }

        hbox {
            textfield(controller.inputPwd) {
                promptText = "请输入加密密码"
                controller.inputPwd.addListener { observable, oldValue, newValue ->
                    controller.getPwd()
                }
            }

            button("查看密码") {
                action {
                    controller.getPwd()
                }
            }

            textfield(controller.result)
        }


        hbox(10) {
            label("选择ncx文件,查看保存的数据库密码")
            xUrlLink("如何导出ncx文件?请点击此查看文章教程", "https://blog.csdn.net/kkk123445/article/details/122514124?spm=1001.2014.3001.5502")
        }

        xChooseFile(controller.ncxFilePath, "ncx", "ncx文件") {
            controller.ncxFilePath.addListener { observable, oldValue, newValue ->
                controller.getPwdList()
            }
        }

        tableview(controller.observableList) {
            readonlyColumn("连接名", DbInfo::dbName)
            readonlyColumn("IP地址", DbInfo::host) {
                prefWidth = 200.0
            }
            readonlyColumn("连接名", DbInfo::dbName)
            readonlyColumn("端口号", DbInfo::port)
            readonlyColumn("用户名", DbInfo::userName)
            readonlyColumn("密码", DbInfo::pwd)
        }
    }
}

class NacicatPwdViewController() : Controller() {
    val inputPwd = SimpleStringProperty()
    val type = SimpleIntegerProperty()
    val result = SimpleStringProperty()

    val ncxFilePath = SimpleStringProperty()

    val observableList = observableListOf<DbInfo>()

    fun getPwd() {
        val inputPwd = inputPwd.value
        val type = type.value
        val pwd = NavicatDecryptUtil.decryptString(inputPwd, type)
        println("解密密码:$pwd")
        result.set(pwd)
    }

    fun getPwd(inputPwd: String): String {
        val type = type.value
        return NavicatDecryptUtil.decryptString(inputPwd, type)
    }

    fun getPwdList() {
        // List<Map <连接名，Map<属性名，值>>> 要导入的连接
        //1、创建一个DocumentBuilderFactory的对象
        val dbf = DocumentBuilderFactory.newInstance()
        //2、创建一个DocumentBuilder的对象
        //创建DocumentBuilder对象
        val db = dbf.newDocumentBuilder()
        //3、通过DocumentBuilder对象的parser方法加载xml文件到当前项目下

        val document = db.parse(File(ncxFilePath.value))
        //获取所有Connections节点的集合

        val connectList = document.getElementsByTagName("Connection")

        val dbInfoList = arrayListOf<DbInfo>()
        for (i in 0 until connectList.length) {
            val tempMap = hashMapOf<String, String>()
            //通过 item(i)方法 获取一个Connection节点，nodelist的索引值从0开始

            val connect = connectList.item(i)
            //获取Connection节点的所有属性集合
            val attrs = connect.attributes
            for (j in 0 until attrs.length) {
                //通过item(index)方法获取connect节点的某一个属性
                val attr = attrs.item(j)
                tempMap[attr.nodeName] = attr.nodeValue
                println("${attr.nodeName}:${attr.nodeValue}")
            }

            val dbInfo = DbInfo().apply {
                dbName = tempMap["ConnectionName"].toString()
                host = tempMap["Host"].toString()
                port = tempMap["Port"].toString()
                userName = tempMap["UserName"].toString()
                pwd = getPwd(tempMap["Password"].toString())
            }
            dbInfoList.add(dbInfo)
        }
        observableList.clear()
        observableList.addAll(dbInfoList)
    }
}

class DbInfo() {
    var dbName: String = ""
    var host: String = ""
    var port = ""
    var userName = ""
    var pwd = ""

    override fun toString(): String {
        return "DbInfo(dbName='$dbName', host='$host', port='$port', userName='$userName', pwd='$pwd')"
    }


}

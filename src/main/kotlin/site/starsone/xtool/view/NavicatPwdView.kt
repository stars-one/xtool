package site.starsone.xtool.view

import com.starsone.controls.common.xUrlLink
import javafx.stage.FileChooser
import kfoenix.jfxradiobutton
import tornadofx.*

/**
 *  Navicat解密获取密码
 * @author StarsOne
 * @href <a href="http://stars-one.site">http://stars-one.site</a>
 * @date Create in  2022/07/06 22:25
 *
 */
class NavicatPwdView : View("My View") {
    override val root = vbox {
        //参考 https://gitee.com/lzy549876/navicat_password_decrypt

        hbox {
            togglegroup {
                jfxradiobutton("navicat11") {
                    isSelected = true
                }
                jfxradiobutton("navicat12+") { }
            }
        }


        hbox {

            textfield {
                promptText = "请输入加密密码"
            }
            button("查看密码") {
                action {

                }
            }
        }

        xUrlLink("操作说明", "https://gitee.com/lzy549876/navicat_password_decrypt")

        hbox {
            text("导入ncx文件,请选择")
            button("选择文件") {
                action {
                    val files = chooseFile("选择ncx文件", arrayOf(FileChooser.ExtensionFilter("ncx文件", "*.ncx")))
                    if (files.isNotEmpty()) {
                        println(files.first().path)
                    }
                }
            }
        }

        textarea {
            fitToParentWidth()
            prefHeight = 200.0
        }

    }
}

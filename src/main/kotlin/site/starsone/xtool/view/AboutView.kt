package site.starsone.xtool.view

import javafx.geometry.Pos
import javafx.scene.control.ScrollPane
import javafx.scene.text.FontWeight
import tornadofx.*
import java.awt.Desktop
import java.net.URI

class AboutView : BaseView() {

    override val root = scrollpane {
        //不显示水平滚动条
        hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER

        vbox {
            paddingTop = 10.0
            spacing = 10.0
            setPrefSize(800.0, 500.0)

            text(descData.appName+descData.version) {
                alignment = Pos.TOP_CENTER

                style {
                    fontWeight = FontWeight.BOLD
                    //字体大小，第二个参数是单位，一个枚举类型
                    fontSize = Dimension(18.0, Dimension.LinearUnits.px)
                }
            }
            //软件描述
            text(descData.desc) {
                alignment = Pos.TOP_CENTER
            }
            form {
                hbox(20) {
                    fieldset {
                        alignment = Pos.CENTER
                        field("软件作者：") {
                            text(descData.author)
                        }
                        field("项目地址：") {
                            hyperlink(descData.githubUrl) {
                                setOnMouseClicked {
                                    Desktop.getDesktop().browse(URI(this.text.toString()))
                                }
                            }
                        }

                        field("博客地址：") {
                            hyperlink(descData.blogUrl) {
                                tooltip(this.text.toString())
                                maxWidth = 300.0
                                setOnMouseClicked {
                                    Desktop.getDesktop().browse(URI(this.text.toString()))
                                }
                            }
                        }
                        field("联系QQ：") {
                            text(descData.qq)
                        }
                        field("软件交流群：") {
                            text(descData.qqGroup)
                        }
                    }
                    fieldset {
                        vbox(20) {
                            text("开发不易，希望得到你打赏支持") {
                                alignment = Pos.TOP_CENTER
                                style {
                                    fontWeight = FontWeight.BOLD
                                    //字体大小，第二个参数是单位，一个枚举类型
                                    fontSize = Dimension(18.0, Dimension.LinearUnits.px)
                                }
                            }
                            hbox(20) {
                                vbox(15) {
                                    text("微信") {
                                        alignment = Pos.TOP_CENTER
                                    }
                                    imageview(url = "img/weixin.jpg") {
                                        alignment = Pos.TOP_CENTER
                                        fitHeight = 160.0
                                        fitWidth = 160.0
                                        isPreserveRatio = true
                                    }
                                }
                                vbox(15) {
                                    text("支付宝") {
                                        alignment = Pos.TOP_CENTER
                                    }
                                    imageview(url = "img/zhifubao.jpg") {
                                        alignment = Pos.TOP_CENTER
                                        fitHeight = 160.0
                                        fitWidth = 160.0
                                        isPreserveRatio = true
                                    }
                                }
                            }
                        }
                    }
                }

            }
            form {

                fieldset("版本更新说明") {
                    val messageList = descData.updateMessage
                    vbox(20) {
                        for (updateMessage in messageList) {
                            field(updateMessage.version) {
                                vbox {
                                    paddingLeft = 30.0
                                    val messages = updateMessage.message
                                    for (message in messages) {
                                        text(message)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

}
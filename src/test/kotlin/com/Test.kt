package kfoenix

import com.jfoenix.controls.JFXBadge

import javafx.geometry.Pos
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import tornadofx.*

class JFXPopupTestApp : App(Main::class, MyStyles::class) {

    class Main: View() {

        val user = UserModel(User("John Doe", "johnd", "oe", "jdoe@fake.io"))

        override val root = stackpane {

            addClass(MyStyles.box)
            form {
                fieldset("User Information") {
                    field("Name") {
                        jfxtextfield(user.name)
                        val jfxBadge = JFXBadge(jfxbutton(user.name))
                        jfxBadge.text = 50.toString()

                         this+=jfxBadge
                        //jfxbutton(user.name)
                        //add(JFXBadge(jfxbutton(user.name)))
                    }

                }
            }
        }
    }

    class MyStyles: Stylesheet() {

        companion object {
            val bar by cssclass()
            val box by cssclass()
            val customerBtn by cssclass()
            val jfxButton by cssclass()
            val jfxRippler by cssclass()
            val jfxRipplerFill by cssproperty<Paint>("-jfx-rippler-fill")

            val defaultColor = Color.web("#4059a9")
        }

        init {
            /* Must place default styling first. Will overwrite more specific styling */
            jfxButton {
                backgroundColor += defaultColor
                textFill = Color.WHITE
            }

            bar {
                alignment = Pos.CENTER_RIGHT
                spacing = 10.px
            }

            box {
                prefHeight = 600.px
                prefWidth = 800.px
            }

            customerBtn {
                backgroundColor += Color.TRANSPARENT

                jfxRippler {
                    jfxRipplerFill.value = Color.GRAY
                }
            }
        }
    }


}

package site.starsone.xtool.app

import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.Stop
import javafx.scene.text.FontWeight
import tornadofx.*

class Styles : Stylesheet() {
    companion object {
        val heading by cssclass()
        val iconBtn by cssclass()

        val optionMenu by cssclass()
        val optionMenuSelect by cssclass()

        val progressStyle by cssclass("progress-bar")
    }

    init {
        progressStyle {
            indeterminateBarLength = 60.px
            indeterminateBarEscape = true
            indeterminateBarFlip = true
            indeterminateBarAnimationTime = 2

            select(".bar") {
                backgroundInsets+=box(3.px,3.px,4.px,3.px)
                backgroundRadius+= box(2.px)
                padding= box((0.75).em)
            }
            and(indeterminate){
                select(".bar"){
                    backgroundColor+= LinearGradient(0.0,0.0,1.0,0.0,true, CycleMethod.NO_CYCLE, Stop(0.0,c("black")),Stop(1.0,c("red")))
                }
            }
            select(".track"){
                backgroundColor+=c("#292E48")
            }

        }

        label and heading {
            padding = box(10.px)
            fontSize = 20.px
            fontWeight = FontWeight.BOLD
        }
        iconBtn{
            textFill = c("blue")
            and(hover){
                backgroundColor += c(0, 0, 0, 0.1)
                backgroundRadius += box(50.percent)
            }

        }

        optionMenu {
            minWidth = 200.px
            maxWidth = 300.px
            startMargin = 10.px
            fontSize = 14.px
            padding = box(15.px)
            backgroundColor += c("white")

            and(hover) {
                textFill = c("#1890ff")
            }
        }

        optionMenuSelect{
            prefWidth = 200.px
            startMargin = 10.px
            fontSize = 14.px
            padding = box(15.px)

            backgroundColor +=c("#e6f7ff")
            textFill = c("#1890ff")
            borderColor += box(null,c("#1890ff"),null,null)
            borderWidth += box(0.px,2.px,0.px,0.px)
            borderStyle += BorderStrokeStyle.SOLID
        }
    }
}

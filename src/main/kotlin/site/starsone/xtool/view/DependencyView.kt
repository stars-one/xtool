package site.starsone.xtool.view

import com.melloware.jintellitype.JIntellitype
import com.starsone.controls.common.TornadoFxUtil
import com.starsone.icontext.icontext
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.text.FontWeight
import javafx.stage.Screen
import site.starsone.xtool.utils.ScreenProperties
import tornadofx.*
import java.awt.Rectangle
import java.awt.Robot
import java.io.File
import javax.imageio.ImageIO

class DependencyView : BaseView() {

    val viewModel by inject<DependencyViewModel>()

    override var root = borderpane {

        setPrefSize(500.0, 300.0)

        left {
            vbox {
                style {
                    padding = box(10.px)
                }
                label("Maven依赖") {
                    style {
                        fontWeight = FontWeight.BOLD
                        fontSize = 16.px
                    }
                }
                textarea(viewModel.mavenStr) {
                    fitToParentHeight()
                    isWrapText = true
                }
            }
        }

        right {
            vbox {
                style {
                    padding = box(10.px)
                }
                label("Gradle依赖") {
                    style {
                        fontWeight = FontWeight.BOLD
                        fontSize = 16.px
                    }
                }
                textarea(viewModel.gradleStr) {
                    fitToParentHeight()
                    isWrapText = true
                }
            }
        }
        center {
            vbox {
                spacing = 10.0
                minWidth = 80.0
                alignment = Pos.CENTER

                button() {
                    icon = icontext("arrow-right")
                    action {
                        viewModel.mavenToGradle()
                    }
                }
                button() {
                    icon = icontext("arrow-left")
                    action {
                        viewModel.gradleToMaven()
                    }
                }
            }
        }
    }

}

class DependencyViewModel : ViewModel() {
    var mavenStr = SimpleStringProperty("")
    var gradleStr = SimpleStringProperty("")

    val mavenKeyWordList = listOf("dependency","groupId","artifactId","version")
    var mavenTemplateList = arrayListOf<String>()

    init {
        if (mavenTemplateList.isEmpty()) {
           mavenKeyWordList.forEach {
               mavenTemplateList.add("<$it>")
               mavenTemplateList.add("</$it>")
           }
        }
    }

    fun reset() {
        mavenStr.set("")
        gradleStr.set("")
    }

    fun mavenToGradle() {
        val mavenStr = mavenStr.value
        val list = arrayListOf<String>()
        for (i in 1..3) {
            val keyWord = mavenKeyWordList[i]
            val result =mavenStr.subStringBetween("<$keyWord>","</$keyWord>")
            list.add(result)
        }
        // implementation 'androidx.legacy:legacy-support-v4:1.0.0'
        val result = "implementation '"+list.joinToString(":")+"'"
        gradleStr.setValue(result)
    }

    fun gradleToMaven() {
        val gradleStr = gradleStr.value
        val message = gradleStr.subStringBetween("'","'")
        val array = message.split(":")

        val lastIndex = mavenTemplateList.lastIndex
        val sb = StringBuffer()
        sb.append(mavenTemplateList.first())
        sb.append("\n")
        var index = 0
        for (i in 2..lastIndex) {
            sb.append(mavenTemplateList[i])
            if (i % 2 == 1) {
                index++
                sb.append("\n")
            } else {
                sb.append(array[index])
            }
        }
        sb.append(mavenTemplateList[1])
        mavenStr.set(sb.toString())
    }
}

fun String.subStringBetween(startKeyWord: String, endKeyWord: String): String {
    if (contains(startKeyWord) && contains(endKeyWord)) {
        val startIndex = indexOf(startKeyWord) + startKeyWord.length
        val endIndex = lastIndexOf(endKeyWord)
        return substring(startIndex, endIndex)
    }
    return ""

}


package site.starsone.xtool.view

import com.github.houbb.markdown.toc.util.MdTocTextHelper
import com.melloware.jintellitype.JIntellitype
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.layout.Background
import javafx.scene.layout.Priority
import javafx.scene.text.FontWeight
import javafx.stage.Screen
import net.dankito.richtexteditor.java.fx.RichTextEditor
import net.dankito.richtexteditor.java.fx.toolbar.GroupedCommandsEditorToolbar
import site.starsone.xtool.app.Styles
import site.starsone.xtool.utils.ScreenProperties
import tornadofx.*
import java.awt.Rectangle
import java.awt.Robot
import java.io.File
import javax.imageio.ImageIO

class MdEditorView : BaseView() {


    override var root = vbox {

        setPrefSize(500.0, 300.0)

        val toolbar = GroupedCommandsEditorToolbar()
        toolbar.root.background = Background.EMPTY
        this+=toolbar

        val editor = RichTextEditor()
        toolbar.editor = editor
        editor.vboxConstraints {
            vGrow = Priority.ALWAYS
        }

        this+=editor


    }

}





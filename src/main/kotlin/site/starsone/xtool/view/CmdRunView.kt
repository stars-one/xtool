package site.starsone.xtool.view

import site.starsone.xtool.app.Styles
import tornadofx.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 *
 * @author StarsOne
 * @Date Create in  2023/07/22 21:38
 *
 */
class CmdRunView : View("My View") {
    override val root = vbox {
        setPrefSize(600.0, 300.0)
        button("取消搜狗输入法占用ctrl+space") {
            addClass(Styles.optionMenu)
            action {
                val cmd = """
        reg add "HKEY_CURRENT_USER\Control Panel\Input Method\Hot Keys\00000010" /v "Key Modifiers" /t REG_BINARY /d 00c00000 /f 
        reg add "HKEY_CURRENT_USER\Control Panel\Input Method\Hot Keys\00000010" /v "Virtual Key" /t REG_BINARY /d ff000000 /f 
        reg add "HKEY_USERS\.DEFAULT\Control Panel\Input Method\Hot Keys\00000010" /v "Key Modifiers" /t REG_BINARY /d 00c00000 /f 
        reg add "HKEY_USERS\.DEFAULT\Control Panel\Input Method\Hot Keys\00000010" /v "Virtual Key" /t REG_BINARY /d ff000000 /f 
    """.trimIndent()

                cmd.lines().forEach {
                    runCmd("cmd /c $it")
                }
            }
        }
    }

    fun runCmd(cmd: String) {
        val exec = Runtime.getRuntime().exec(cmd, null, null)
        val inputStream = exec.inputStream

        val myBr = BufferedReader(InputStreamReader(inputStream, "gbk"))

        runAsync {
            var line: String? = null
            try {
                while (myBr.readLine().also({ line = it }) != null) {
                    line?.let {
                        println(line)
                    }
                }

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

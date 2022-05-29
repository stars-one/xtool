package site.starsone.xtool.utils

import com.github.hui.quick.plugin.qrcode.wrapper.QrCodeDeWrapper
import com.github.hui.quick.plugin.qrcode.wrapper.QrCodeGenWrapper
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import javax.imageio.ImageIO

/**
 *
 * @author StarsOne
 * @url <a href="http://stars-one.site">http://stars-one.site</a>
 * @date Create in  2021/07/18 15:00
 *
 */
fun main() {
    var result = QrCodeDeWrapper.decode("D:\\temp\\qrcode1.gif")
    println(result)
}

class QrCodeUtils {
    companion object {
        fun createGifQrCode(content: String, logo: File, imageType: ImageType, outputFile: File) {
            val msg = "https://stars-one.site"
            //动图gif
            val bg = "https://img2018.cnblogs.com/blog/1210268/201905/1210268-20190502211445963-1641313648.gif"

            val steam = QrCodeGenWrapper
                    .of(msg)
                    .setW(500)
                    .setBgImg(bg)
                    .setBgOpacity(0.6f)
                    .setPicType("gif")
                    .asStream()
            File("D:\\temp\\qrcode.gif").writeBytes(steam.toByteArray())
        }

        fun getContentByQr(imgFilePath: String): String {
            return QrCodeDeWrapper.decode(imgFilePath)
        }

        fun getContentByQr(imgFile: File): String {
            return getContentByQr(imgFile.path)
        }

        fun getContentByQr(img: BufferedImage): String {
            return QrCodeDeWrapper.decode(img)
        }
    }
}

enum class ImageType {
    PNG, JPG, GIF
}

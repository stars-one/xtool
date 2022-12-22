package site.starsone.xtool.view.lpk

import cn.hutool.core.util.ArrayUtil
import com.google.gson.JsonParser
import org.apache.commons.lang3.StringUtils
import org.apache.tika.Tika
import site.starsone.xtool.view.lpk.mime.MimeMatcher
import site.starsone.xtool.view.lpk.mime.MimeMoc
import site.starsone.xtool.view.lpk.mime.MimeMoc3
import site.starsone.xtool.view.lpk.mime.MimeTypes
import java.util.*
import java.util.regex.Pattern
import kotlin.math.min

object LpkUtils {
    val match_rule = Pattern.compile("^[0-9a-f]{32}.bin3?$")
    fun isEncryptedFile(s: String?): Boolean {
        return match_rule.matcher(s).find()
    }

    fun genKey(s: String): Long {
        var ret = 0L
        for (element in s) {
            ret = (ret * 31 + element.toInt()) and 0xffffffffL
        }
        if ((ret and 0x80000000L) != 0L) {
            ret = ret or -0x100000000L
        }
        return ret
    }

    fun decrypt(key: Long, data: ByteArray): ByteArray {
        val list = arrayListOf<Byte>()
        for (i in data.indices step 1024) {
            val end = min(i + 1024, data.size)
            val slice = ArrayUtil.sub(data, i, end)
            var tmpkey = key
            for (b in slice) {
                tmpkey = (65535L and ((2531011 + (214013L * tmpkey)) shr 16)) and 0xffffffffL
                list.add(((tmpkey and 0xff) xor b.toLong()).toByte())
            }
        }
        val size = list.size
        val bytes = ByteArray(size)
        for (i in 0 until size) {
            bytes[i] = list[i]
        }
        return bytes
    }

    fun travelsDict(map: Map<String?, *>): List<Pair<String?, Any?>> {
        val items: MutableList<Pair<String?, Any?>> = ArrayList()
        for ((key, value) in map) {
            val o = value!!
            //根据数值分类
            if (o is Map<*, *>) {
                for ((first, second) in travelsDict(o as Map<String?, *>)) {
                    items.add(Pair<String?, Any?>(String.format("%s_%s", key, first), second))
                }
            } else if (o is List<*>) {
                for ((first, second) in travelsList(o)) {
                    items.add(Pair<String?, Any?>(String.format("%s_%s", key, first), second))
                }
            } else {
                items.add(Pair<String?, Any?>(key, o))
            }
        }
        return items
    }

    fun travelsList(list: List<*>): List<Pair<String?, Any?>> {
        val items: MutableList<Pair<String?, Any?>> = ArrayList()
        for (i in list.indices) {
            val o = list[i]!!
            if (o is Map<*, *>) {
                for ((first, second) in travelsDict(o as Map<String?, *>)) {
                    items.add(Pair<String?, Any?>(String.format("%d_%s", i, first), second))
                }
            } else if (o is List<*>) {
                for ((first, second) in travelsList(o)) {
                    items.add(Pair<String?, Any?>(String.format("%d_%s", i, first), second))
                }
            } else {
                items.add(Pair<String?, Any?>(i.toString(), o))
            }
        }
        return items
    }

    // MIME
    val customMimeTypes: MutableList<MimeMatcher> = ArrayList()
    val TIKA = Tika()
    fun guessType(data: ByteArray?): String {
        // 使用自定义的类型匹配器
        for (matcher in customMimeTypes) {
            if (matcher.match(data!!)) {
                return "." + matcher.getExt()
            }
        }
        // 使用 tika 检查内容类型
        val mime = TIKA.detect(data)
        if (StringUtils.isNoneEmpty(mime) && "text/plain" != mime) {
            // https://stackoverflow.com/questions/48053127/get-the-extension-from-a-mimetype
            val ext = MimeTypes.lookupExt(mime)
            if (StringUtils.isNoneEmpty(ext)) {
                return ".$ext"
            }
        }
        return try {
            //尝试转换为json格式
            val str = String(data!!)
            val parse = JsonParser().parse(str)
            ".json"
        } catch (ignored: Exception) {
            //如果转换失败，则返回空白字符串
            ""
        }
    }

    init {
        customMimeTypes.add(MimeMoc())
        customMimeTypes.add(MimeMoc3())
    }
}

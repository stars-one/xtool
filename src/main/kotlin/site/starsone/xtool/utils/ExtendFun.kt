package site.starsone.xtool.utils

import com.google.gson.Gson
import com.google.gson.internal.`$Gson$Types`
import com.google.gson.reflect.TypeToken
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.KClass


fun Long.toDateString(format: String = "yyyy-MM-dd HH:mm:ss"): String {
    return SimpleDateFormat(format).format(Date(this))
}

fun Date.toDateString(format: String = "yyyy-MM-dd HH:mm:ss"): String {
    return SimpleDateFormat(format).format(this)
}

/**
 * 将字节(B)转为对应的单位
 */
fun Long.toUnitString(): String {
    val df = DecimalFormat("#.00")
    val bytes = this
    return when {
        bytes < 1024 -> return df.format(bytes.toDouble()) + "B"
        bytes < 1048576 -> df.format(bytes.toDouble() / 1024) + "KB"
        bytes < 1073741824 -> df.format(bytes.toDouble() / 1048576) + "MB"
        else -> df.format(bytes.toDouble() / 1073741824) + "GB"
    }
}

/**
 * 将json数据转为List<T>
 *
 * @param T 数据类型
 * @return
 */
fun <T> String.parseJsonToList(clazz:Class<T>): List<T> {
    val gson = Gson()
    val type = `$Gson$Types`.newParameterizedTypeWithOwner(null,ArrayList::class.java,clazz)
    val data: List<T> = gson.fromJson(this, type)
    return data
}

/**
 * 将json数据转为一个T类型对象
 *
 * @param T
 * @return
 */
inline fun <reified T> String.parseJsonToObject(): T {
    val gson = Gson()
    val result = gson.fromJson(this, T::class.java)
    return result
}



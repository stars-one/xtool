package site.starsone.xtool.utils

import java.text.SimpleDateFormat
import java.util.*


fun Long.toDateString(format:String="yyyy-MM-dd HH:mm:ss"): String {
    return SimpleDateFormat(format).format(Date(this))
}

fun Date.toDateString(format:String="yyyy-MM-dd HH:mm:ss"): String {
    return SimpleDateFormat(format).format(this)
}


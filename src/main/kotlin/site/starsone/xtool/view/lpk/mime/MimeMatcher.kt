package site.starsone.xtool.view.lpk.mime

abstract class MimeMatcher {
    abstract fun match(data: ByteArray): Boolean

    abstract fun getMimeType(): String
    abstract fun getExt(): String

    /**
     * 检测文件头是否相同
     *
     * @param data
     * @param head
     * @return
     */
    fun checkSame(data: ByteArray, head: List<Byte>) :Boolean{
        return if (data.size >= 3) {
            val temp = data.toList().subList(0,3)
            var flag = true

            temp.forEachIndexed { index, byte ->
                if (head[index]!=byte) {
                    flag = false
                }
            }
            return flag
        } else false
    }
}

class MimeMoc : MimeMatcher() {
    val head = "moc".toByteArray().toList()

    override fun match(data: ByteArray): Boolean {
        return checkSame(data,head)
    }

    override fun getMimeType(): String {
        return "application/moc"
    }

    override fun getExt(): String {
        return "moc"
    }
}

class MimeMoc3 : MimeMatcher() {
    var head = "MOC3".toByteArray().toList()

    override fun match(data: ByteArray): Boolean {
        return checkSame(data,head)
    }

    override fun getMimeType(): String {
        return "application/moc3"
    }

    override fun getExt(): String {
        return "moc3"
    }

}

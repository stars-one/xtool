package site.starsone.xtool.view.lpk.mime


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

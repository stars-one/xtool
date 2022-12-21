package site.starsone.xtool.view.lpk.mime

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

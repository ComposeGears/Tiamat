package composegears.tiamat.example.platform

expect object Platform {
    fun start()
    fun name(): String
    fun features(): List<PlatformFeature>
}
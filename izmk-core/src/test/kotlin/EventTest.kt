import ovo.xsvf.izmk.event.*

class PlayerJoinEvent(val playerName: String) : Event()

class ExampleListener {
    @EventListener
    fun onPlayerJoin(event: PlayerJoinEvent) {
        println("Player joined: ${event.playerName}")
    }
}

fun main() {
    val eventBus = EventBus
    val listener = ExampleListener()

    eventBus.register(listener)
    eventBus.post(PlayerJoinEvent("LangYa"))
}

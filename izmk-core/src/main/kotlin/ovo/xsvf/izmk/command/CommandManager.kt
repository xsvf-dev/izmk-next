package ovo.xsvf.izmk.command

import ovo.xsvf.izmk.command.impl.*
import ovo.xsvf.izmk.event.annotations.EventTarget
import ovo.xsvf.izmk.event.impl.ChatMessageEvent

object CommandManager {
    private val commands = listOf(
        BindCommand(),
        ToggleCommand()
    )

    private fun runCommand(message: String): Boolean {
        val args = message.split(' ')
        return commands.find { it.name == args[0].removePrefix(".") }
            ?.apply { run(args.toTypedArray()) } != null
    }

    @EventTarget
    fun onChat(event: ChatMessageEvent) {
        event.message.string.takeIf { it.startsWith(".") && runCommand(it) }?.let {
            event.isCancelled = true
        }
    }
}

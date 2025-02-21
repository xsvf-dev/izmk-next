package ovo.xsvf.izmk.command

import ovo.xsvf.izmk.command.impl.BindCommand
import ovo.xsvf.izmk.command.impl.ToggleCommand
import ovo.xsvf.izmk.event.EventListener
import ovo.xsvf.izmk.event.SendMessageEvent

object CommandManager {
    private val commands by lazy {
        listOf(BindCommand(), ToggleCommand())
    }

    private fun runCommand(message: String): Boolean {
        val args = message.split(' ')
        val commandName = args[0].removePrefix(".")
        return commands.firstOrNull { it.name == commandName }?.let {
            it.run(args.toTypedArray())
            true
        } ?: false
    }

    @EventListener
    fun onChat(event: SendMessageEvent) {
        if (event.component.string.startsWith(".") && runCommand(event.component.string)) {
            event.isCancelled = true
        }
    }
}

package ovo.xsvf.izmk.command

import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.command.impl.BindCommand
import ovo.xsvf.izmk.command.impl.ToggleCommand
import ovo.xsvf.izmk.event.EventTarget
import ovo.xsvf.izmk.event.impl.SendMessageEvent

object CommandManager {
    private val commands by lazy {
        listOf(BindCommand(), ToggleCommand())
    }

    private fun runCommand(message: String): Boolean {
        val args = message.split(' ')
        IZMK.logger.debug(args.toString())
        val commandName = args[0].removePrefix(".")
        return commands.firstOrNull { it.name == commandName }?.let {
            it.run(args.toTypedArray())
            true
        } ?: false
    }

    @EventTarget
    fun onChat(event: SendMessageEvent) {
        if (event.message.startsWith(".") && runCommand(event.message)) {
            event.isCancelled = true
        }
    }
}

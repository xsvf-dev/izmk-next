package ovo.xsvf.izmk.command

import ovo.xsvf.izmk.command.impl.BindCommand
import ovo.xsvf.izmk.command.impl.ToggleCommand
import ovo.xsvf.izmk.event.EventListener
import ovo.xsvf.izmk.event.impl.SendMessageEvent

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

    @EventListener
    fun onChat(event: SendMessageEvent) {
        event.component.string.takeIf { it.startsWith(".") && runCommand(it) }?.let {
            event.isCancelled = true
        }
    }
}

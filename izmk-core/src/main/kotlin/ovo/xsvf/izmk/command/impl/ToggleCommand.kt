package ovo.xsvf.izmk.command.impl

import ovo.xsvf.izmk.command.Command
import ovo.xsvf.izmk.module.ModuleManager
import ovo.xsvf.izmk.util.ChatUtil

/**
 * @author LangYa466
 * @since 2025/1/11
 */
class ToggleCommand : Command("t", ".t 模块名称") {

    override fun run(args: Array<String>) {
        if (args.size != 2) {
            return ChatUtil.addMessageWithClient(usage)
        }

        val moduleName = args[1]
        val module = ModuleManager.modulesMap[moduleName] ?: return ChatUtil.addMessageWithClient("找不到有这个名字的模块")

        module.toggle()
    }
}

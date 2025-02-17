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
            ChatUtil.addMessageWithClient(usage)
            return
        }

        val moduleName = args[1]

        val module = ModuleManager.modulesMap.values.find { it.name == moduleName }

        if (module != null) {
            module.toggle()
        } else {
            ChatUtil.addMessageWithClient("找不到有这个名字的模块")
        }
    }
}

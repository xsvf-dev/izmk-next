package ovo.xsvf.izmk.command.impl

import com.mojang.blaze3d.platform.InputConstants
import ovo.xsvf.izmk.command.Command
import ovo.xsvf.izmk.module.ModuleManager
import ovo.xsvf.izmk.util.ChatUtil

class BindCommand : Command("bind", ".bind 模块名称 按键名称") {

    override fun run(args: Array<String>) {
        if (args.size != 3) {
            ChatUtil.addMessageWithClient(usage)
            return
        }

        val moduleName = args[1]
        val keyName = args[2].uppercase()

        val module = ModuleManager.modulesMap.values.find { it.name == moduleName }

        if (module != null) {
            val key = InputConstants.getKey(keyName)

            if (key != InputConstants.UNKNOWN) {
                module.keyCode = key.value
                ChatUtil.addMessageWithClient("绑定按键成功: $keyName")
            } else {
                ChatUtil.addMessageWithClient("找不到有这个名字的按键")
            }
        } else {
            ChatUtil.addMessageWithClient("找不到有这个名字的模块")
        }
    }
}

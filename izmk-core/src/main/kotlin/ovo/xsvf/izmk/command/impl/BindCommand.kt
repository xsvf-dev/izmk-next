package ovo.xsvf.izmk.command.impl

import com.mojang.blaze3d.platform.InputConstants
import ovo.xsvf.izmk.command.Command
import ovo.xsvf.izmk.module.ModuleManager
import ovo.xsvf.izmk.util.ChatUtil

class BindCommand : Command("bind", ".bind 模块名称 按键名称") {

    override fun run(args: Array<String>) {
        if (args.size != 3) {
            return ChatUtil.addMessageWithClient(usage)
        }

        val (moduleName, keyName) = args[1] to args[2].uppercase()
        val module = ModuleManager.get(moduleName) ?: return ChatUtil.addMessageWithClient("找不到有这个名字的模块")

        val key = InputConstants.getKey(keyName)
        if (key == InputConstants.UNKNOWN) {
            return ChatUtil.addMessageWithClient("找不到有这个名字的按键")
        }

        module.keyCode = key.value
        ChatUtil.addMessageWithClient("绑定按键成功: $keyName")
    }
}

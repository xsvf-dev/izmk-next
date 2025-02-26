package ovo.xsvf.izmk.command.impl

import ovo.xsvf.izmk.command.Command
import ovo.xsvf.izmk.module.ModuleManager
import ovo.xsvf.izmk.util.ChatUtil
import org.lwjgl.glfw.GLFW

class BindCommand : Command("bind", ".bind 模块名称 按键名称") {

    override fun run(args: Array<String>) {
        if (args.size != 3) {
            return ChatUtil.addMessageWithClient(usage)
        }

        val (moduleName, keyName) = args[1] to args[2].uppercase()
        val module = ModuleManager[moduleName] ?: return ChatUtil.addMessageWithClient("找不到有这个名字的模块")

        val keyCode = keyNameToGLFWKeyCode(keyName)
        if (keyCode == -1) {
            return ChatUtil.addMessageWithClient("找不到有这个名字的按键: $keyName")
        }

        module.keyCode = keyCode
        ChatUtil.addMessageWithClient("绑定按键成功: $keyName")
    }

    private fun keyNameToGLFWKeyCode(name: String): Int {
        return when (name) {
            "A" -> GLFW.GLFW_KEY_A
            "B" -> GLFW.GLFW_KEY_B
            "C" -> GLFW.GLFW_KEY_C
            "D" -> GLFW.GLFW_KEY_D
            "E" -> GLFW.GLFW_KEY_E
            "F" -> GLFW.GLFW_KEY_F
            "G" -> GLFW.GLFW_KEY_G
            "H" -> GLFW.GLFW_KEY_H
            "I" -> GLFW.GLFW_KEY_I
            "J" -> GLFW.GLFW_KEY_J
            "K" -> GLFW.GLFW_KEY_K
            "L" -> GLFW.GLFW_KEY_L
            "M" -> GLFW.GLFW_KEY_M
            "N" -> GLFW.GLFW_KEY_N
            "O" -> GLFW.GLFW_KEY_O
            "P" -> GLFW.GLFW_KEY_P
            "Q" -> GLFW.GLFW_KEY_Q
            "R" -> GLFW.GLFW_KEY_R
            "S" -> GLFW.GLFW_KEY_S
            "T" -> GLFW.GLFW_KEY_T
            "U" -> GLFW.GLFW_KEY_U
            "V" -> GLFW.GLFW_KEY_V
            "W" -> GLFW.GLFW_KEY_W
            "X" -> GLFW.GLFW_KEY_X
            "Y" -> GLFW.GLFW_KEY_Y
            "Z" -> GLFW.GLFW_KEY_Z
            "1" -> GLFW.GLFW_KEY_1
            "2" -> GLFW.GLFW_KEY_2
            "3" -> GLFW.GLFW_KEY_3
            "4" -> GLFW.GLFW_KEY_4
            "5" -> GLFW.GLFW_KEY_5
            "6" -> GLFW.GLFW_KEY_6
            "7" -> GLFW.GLFW_KEY_7
            "8" -> GLFW.GLFW_KEY_8
            "9" -> GLFW.GLFW_KEY_9
            "0" -> GLFW.GLFW_KEY_0
            "SPACE" -> GLFW.GLFW_KEY_SPACE
            "ENTER" -> GLFW.GLFW_KEY_ENTER
            "ESCAPE" -> GLFW.GLFW_KEY_ESCAPE
            "TAB" -> GLFW.GLFW_KEY_TAB
            "BACKSPACE" -> GLFW.GLFW_KEY_BACKSPACE
            "INSERT" -> GLFW.GLFW_KEY_INSERT
            "DELETE" -> GLFW.GLFW_KEY_DELETE
            "RIGHT" -> GLFW.GLFW_KEY_RIGHT
            "LEFT" -> GLFW.GLFW_KEY_LEFT
            "DOWN" -> GLFW.GLFW_KEY_DOWN
            "UP" -> GLFW.GLFW_KEY_UP
            "PAGEUP" -> GLFW.GLFW_KEY_PAGE_UP
            "PAGEDOWN" -> GLFW.GLFW_KEY_PAGE_DOWN
            "HOME" -> GLFW.GLFW_KEY_HOME
            "END" -> GLFW.GLFW_KEY_END
            "CAPSLOCK" -> GLFW.GLFW_KEY_CAPS_LOCK
            "SCROLLLOCK" -> GLFW.GLFW_KEY_SCROLL_LOCK
            "NUMLOCK" -> GLFW.GLFW_KEY_NUM_LOCK
            "PRINTSCREEN" -> GLFW.GLFW_KEY_PRINT_SCREEN
            "PAUSE" -> GLFW.GLFW_KEY_PAUSE
            "F1" -> GLFW.GLFW_KEY_F1
            "F2" -> GLFW.GLFW_KEY_F2
            "F3" -> GLFW.GLFW_KEY_F3
            "F4" -> GLFW.GLFW_KEY_F4
            "F5" -> GLFW.GLFW_KEY_F5
            "F6" -> GLFW.GLFW_KEY_F6
            "F7" -> GLFW.GLFW_KEY_F7
            "F8" -> GLFW.GLFW_KEY_F8
            "F9" -> GLFW.GLFW_KEY_F9
            "F10" -> GLFW.GLFW_KEY_F10
            "F11" -> GLFW.GLFW_KEY_F11
            "F12" -> GLFW.GLFW_KEY_F12
            "F13" -> GLFW.GLFW_KEY_F13
            "F14" -> GLFW.GLFW_KEY_F14
            "F15" -> GLFW.GLFW_KEY_F15
            "F16" -> GLFW.GLFW_KEY_F16
            "F17" -> GLFW.GLFW_KEY_F17
            "F18" -> GLFW.GLFW_KEY_F18
            "F19" -> GLFW.GLFW_KEY_F19
            "F20" -> GLFW.GLFW_KEY_F20
            "F21" -> GLFW.GLFW_KEY_F21
            "F22" -> GLFW.GLFW_KEY_F22
            "F23" -> GLFW.GLFW_KEY_F23
            "F24" -> GLFW.GLFW_KEY_F24
            "LSHIFT" -> GLFW.GLFW_KEY_LEFT_SHIFT
            "RSHIFT" -> GLFW.GLFW_KEY_RIGHT_SHIFT
            "LCTRL" -> GLFW.GLFW_KEY_LEFT_CONTROL
            "RCTRL" -> GLFW.GLFW_KEY_RIGHT_CONTROL
            "LALT" -> GLFW.GLFW_KEY_LEFT_ALT
            "RALT" -> GLFW.GLFW_KEY_RIGHT_ALT
            "LWIN" -> GLFW.GLFW_KEY_LEFT_SUPER
            "RWIN" -> GLFW.GLFW_KEY_RIGHT_SUPER
            "KP0" -> GLFW.GLFW_KEY_KP_0
            "KP1" -> GLFW.GLFW_KEY_KP_1
            "KP2" -> GLFW.GLFW_KEY_KP_2
            "KP3" -> GLFW.GLFW_KEY_KP_3
            "KP4" -> GLFW.GLFW_KEY_KP_4
            "KP5" -> GLFW.GLFW_KEY_KP_5
            "KP6" -> GLFW.GLFW_KEY_KP_6
            "KP7" -> GLFW.GLFW_KEY_KP_7
            "KP8" -> GLFW.GLFW_KEY_KP_8
            "KP9" -> GLFW.GLFW_KEY_KP_9
            "KPDOT" -> GLFW.GLFW_KEY_KP_DECIMAL
            "KPENTER" -> GLFW.GLFW_KEY_KP_ENTER
            "KPMINUS" -> GLFW.GLFW_KEY_KP_SUBTRACT
            "KPPLUS" -> GLFW.GLFW_KEY_KP_ADD
            "KPMULTIPLY" -> GLFW.GLFW_KEY_KP_MULTIPLY
            "KPDIVIDE" -> GLFW.GLFW_KEY_KP_DIVIDE
            else -> -1
        }
    }
}

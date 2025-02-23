package ovo.xsvf.izmk.setting.impl

import ovo.xsvf.izmk.setting.AbstractSetting

class BoolSetting(
    name: String,
    value: Boolean = false,
    visibility: () -> Boolean = { true }
) : AbstractSetting<Boolean>(name, value, visibility)
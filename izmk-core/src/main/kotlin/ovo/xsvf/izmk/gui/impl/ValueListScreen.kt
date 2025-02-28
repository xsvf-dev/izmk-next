package ovo.xsvf.izmk.gui.impl

import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.multidraw.FontMultiDraw
import ovo.xsvf.izmk.graphics.multidraw.PosColor2DMultiDraw
import ovo.xsvf.izmk.graphics.utils.RenderUtils2D
import ovo.xsvf.izmk.gui.GuiScreen
import ovo.xsvf.izmk.module.Module
import ovo.xsvf.izmk.settings.*

// 需要引入 Java AWT 用于 HSBtoRGB
import java.awt.Color

class ValueListScreen(val module: Module) : GuiScreen("value-list") {
    private val rectMulti = PosColor2DMultiDraw()
    private val fontMulti = FontMultiDraw()

    private val listX = 50f
    private val listY = 50f
    private val listWidth = 300f
    private val listHeight = 400f
    private val entryHeight = 20f
    private val padding = 5f

    // 存放 ColorSetting 的展开状态 + HSB
    private val colorPickerStates = mutableMapOf<ColorSetting, ColorPickerData>()

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        rectMulti.clear()
        fontMulti.clear()

        // 绘制整体背景
        rectMulti.addRect(listX, listY, listWidth, listHeight, ColorRGB(0.15f, 0.15f, 0.15f))

        var offsetY = listY + padding

        module.settings.forEach { setting ->
            if (!setting.visibility.invoke()) return@forEach

            val settingX = listX + padding
            val settingY = offsetY

            // 每个设置项的“行”高度先固定 entryHeight
            var rowHeight = entryHeight

            // 绘制行背景
            rectMulti.addRectGradientHorizontal(
                settingX, settingY,
                listWidth - 2 * padding, rowHeight,
                ColorRGB(0.2f, 0.2f, 0.2f),
                ColorRGB(0.25f, 0.25f, 0.25f)
            )

            // 根据不同类型绘制文本
            when (setting) {
                is BooleanSetting -> {
                    fontMulti.addText(
                        "${setting.name.translation} ${if (setting.value) "(ON)" else "(OFF)"}",
                        settingX + padding + 2f,
                        settingY + padding - 3f,
                        if (setting.value) ColorRGB.WHITE else ColorRGB.GRAY
                    )
                }
                is NumberSetting<*> -> {
                    fontMulti.addText(
                        String.format("${setting.name.translation}: %.2f",setting.value),
                        settingX + padding + 2f,
                        settingY + padding - 3f,
                        ColorRGB.WHITE
                    )
                }
                is ColorSetting -> {
                    // 显示名称 + 当前颜色预览
                    fontMulti.addText(
                        setting.name.translation,
                        settingX + padding + 2f,
                        settingY + padding - 3f,
                        ColorRGB.WHITE
                    )
                    // 在右侧显示一个小方块，展示当前颜色
                    val colorBoxSize = rowHeight - 6
                    val colorBoxX = settingX + listWidth - 2 * padding - colorBoxSize - 3
                    val colorBoxY = settingY + 3
                    rectMulti.addRect(colorBoxX, colorBoxY, colorBoxSize, colorBoxSize, setting.value)

                    // 如果 colorPickerStates 中还没初始化，就初始化
                    if (!colorPickerStates.containsKey(setting)) {
                        val (h, s, b, a) = colorRGBToHSB(setting.value)
                        colorPickerStates[setting] = ColorPickerData(false, h, s, b, a)
                    }

                    // 若 ColorPickerData.isOpen = true，则在下方绘制可视化 ColorPicker
                    val cpData = colorPickerStates[setting]!!
                    if (cpData.isOpen) {
                        val pickerY = settingY + rowHeight // 紧接在本行下面
                        val pickerWidth = listWidth - 2 * padding
                        val pickerHeight = 80f  // 可以自定义

                        // 绘制 ColorPicker 背景
                        rectMulti.addRect(
                            settingX, pickerY,
                            pickerWidth, pickerHeight,
                            ColorRGB(0.17f, 0.17f, 0.17f)
                        )

                        // 分为三块：S-B 区域、Hue 条、Alpha 条
                        // 1) S-B 区域
                        val sbWidth = pickerWidth - 30f // 给 Hue/Alpha 留点空间
                        val sbHeight = pickerHeight - 10f
                        val sbX = settingX + 5f
                        val sbY = pickerY + 5f

                        // 第一次：水平渐变(左：纯黑->右：与 hue 相同的亮色)
                        val leftColor = hsbToColorRGB(cpData.hue, 0f, 1f)   // 饱和度=0 -> 白灰
                        val rightColor = hsbToColorRGB(cpData.hue, 1f, 1f)  // 饱和度=1 -> 色相色
                        rectMulti.addRectGradientHorizontal(
                            sbX, sbY, sbWidth, sbHeight,
                            leftColor, rightColor
                        )
                        // 第二次：垂直渐变(上：透明->下：黑)
                        rectMulti.addRectGradientVertical(
                            sbX, sbY, sbWidth, sbHeight,
                            ColorRGB(1f, 1f, 1f, 0f),  // 透明白
                            ColorRGB(0f, 0f, 0f, 1f)   // 黑
                        )

                        // 绘制当前 (saturation, brightness) 位置的小圆点(这里简单用 addRect 代替)
                        val sbCursorX = sbX + cpData.saturation * sbWidth
                        val sbCursorY = sbY + (1f - cpData.brightness) * sbHeight
                        rectMulti.addRect(
                            sbCursorX - 2, sbCursorY - 2,
                            4f, 4f,
                            ColorRGB.WHITE
                        )

                        // 2) Hue 条
                        val hueX = sbX + sbWidth + 2f
                        val hueWidth = 10f
                        val hueHeight = sbHeight / 6f  // 我们分6段
                        // 这里做 6 段矩形拼接
                        for (i in 0..5) {
                            val segY = sbY + i * hueHeight
                            val c1 = hsbToColorRGB(i / 6f, 1f, 1f)
                            val c2 = hsbToColorRGB((i + 1) / 6f, 1f, 1f)
                            rectMulti.addRectGradientVertical(hueX, segY, hueWidth, hueHeight, c1, c2)
                        }
                        // 绘制 hue 光标
                        val hueCursorY = sbY + cpData.hue * sbHeight
                        rectMulti.addRect(hueX - 2, hueCursorY - 1, hueWidth + 4, 2f, ColorRGB.WHITE)

                        // 3) Alpha 条
                        val alphaX = hueX + hueWidth + 3f
                        val alphaWidth = 10f
                        // 背板(先画白色，再画当前颜色->透明渐变)
                        rectMulti.addRect(alphaX, sbY, alphaWidth, sbHeight, ColorRGB.WHITE)
                        // 计算基色时固定 alpha 为 1
                        val baseColor = hsbToColorRGB(cpData.hue, cpData.saturation, cpData.brightness, 1f)
                        val topAlphaColor = baseColor  // 全不透明
                        // 构造全透明颜色（RGB保持不变）
                        val bottomAlphaColor = ColorRGB(baseColor.r.toFloat(), baseColor.g.toFloat(), baseColor.b.toFloat(), 0f)

                        rectMulti.addRectGradientVertical(
                            alphaX,
                            sbY,
                            alphaWidth,
                            sbHeight,
                            topAlphaColor,
                            bottomAlphaColor
                        )


                        // 绘制 alpha 光标
                        val alphaCursorY = sbY + (1f - cpData.alpha) * sbHeight
                        rectMulti.addRect(alphaX - 2, alphaCursorY - 1, alphaWidth + 4, 2f, ColorRGB.WHITE)

                        // 最终行高度 = 原 entryHeight + pickerHeight
                        rowHeight += pickerHeight
                    }
                }
                else -> {
                    fontMulti.addText(
                        setting.name.translation,
                        settingX + padding + 2f,
                        settingY + padding - 3f,
                        ColorRGB.WHITE
                    )
                }
            }

            offsetY += rowHeight + padding
        }

        // 绘制
        rectMulti.draw()
        fontMulti.draw()
    }

    override fun mouseClicked(buttonID: Int, mouseX: Double, mouseY: Double) {
        var offsetY = listY + padding

        module.settings.forEach { setting ->
            if (!setting.visibility.invoke()) return@forEach

            val settingX = listX + padding
            val settingY = offsetY
            var rowHeight = entryHeight

            // 如果是 ColorSetting 并且处于展开，则行高度要加上 picker
            if (setting is ColorSetting && colorPickerStates.containsKey(setting)) {
                val cpData = colorPickerStates[setting]!!
                if (cpData.isOpen) {
                    rowHeight += 80f // 你在上面绘制了 80f 的 picker
                }
            }

            val isMouseOverRow = RenderUtils2D.isMouseOver(
                mouseX.toFloat(), mouseY.toFloat(),
                settingX, settingY,
                settingX + listWidth - 2 * padding,
                settingY + entryHeight
            )

            if (isMouseOverRow) {
                when (setting) {
                    is BooleanSetting -> {
                        // 左/右键都 toggle
                        setting.toggle()
                    }
                    is NumberSetting<*> -> {
                        if (buttonID == 0) {
                            incrementNumber(setting)
                        } else if (buttonID == 1) {
                            decrementNumber(setting)
                        }
                    }
                    is ColorSetting -> {
                        //切换展开/收起
                        val cpData = colorPickerStates.getOrPut(setting) {
                            val (h, s, b, a) = colorRGBToHSB(setting.value)
                            ColorPickerData(false, h, s, b, a)
                        }
                        cpData.isOpen = !cpData.isOpen
                    }
                    else -> {}
                }
            }

            // 如果行是 ColorSetting & 已展开，就要判断点击是否在 Hue/SB/Alpha 区域
            if (setting is ColorSetting && colorPickerStates.containsKey(setting)) {
                val cpData = colorPickerStates[setting]!!
                if (cpData.isOpen) {
                    // 计算 picker 各区域的坐标
                    val pickerX = settingX
                    val pickerY = settingY + entryHeight
                    val pickerWidth = listWidth - 2 * padding
                    val pickerHeight = 80f

                    // SB 区域
                    val sbX = pickerX + 5f
                    val sbY = pickerY + 5f
                    val sbWidth = pickerWidth - 30f
                    val sbHeight = pickerHeight - 10f

                    // Hue 条
                    val hueX = sbX + sbWidth + 2f
                    val hueWidth = 10f

                    // Alpha 条
                    val alphaX = hueX + hueWidth + 3f
                    val alphaWidth = 10f

                    // 左键按下时，如果点击在这三块区域里，就更新颜色
                    if (buttonID == 0) {
                        // Saturation-Brightness
                        if (RenderUtils2D.isMouseOver(
                                mouseX.toFloat(),
                                mouseY.toFloat(),
                                sbX,
                                sbY,
                                sbX + sbWidth,
                                sbY + sbHeight
                            )
                        ) {
                            updateSB(cpData, mouseX.toFloat(), mouseY.toFloat(), sbX, sbY, sbWidth, sbHeight, setting)
                        }
                        // Hue
                        else if (RenderUtils2D.isMouseOver(
                                mouseX.toFloat(),
                                mouseY.toFloat(),
                                hueX,
                                sbY,
                                hueX + hueWidth,
                                sbY + sbHeight
                            )
                        ) {
                            updateHue(cpData, mouseY.toFloat(), sbY, sbHeight, setting)
                        }
                        // Alpha
                        else if (RenderUtils2D.isMouseOver(
                                mouseX.toFloat(),
                                mouseY.toFloat(),
                                alphaX,
                                sbY,
                                alphaX + alphaWidth,
                                sbY + sbHeight
                            )
                        ) {
                            updateAlpha(cpData, mouseY.toFloat(), sbY, sbHeight, setting)
                        }
                    }
                }
            }

            offsetY += rowHeight + padding
        }
    }

    /**
     * 更新饱和度与亮度
     */
    private fun updateSB(cpData: ColorPickerData, mouseX: Float, mouseY: Float, sbX: Float, sbY: Float, sbW: Float, sbH: Float, setting: ColorSetting) {
        val relX = (mouseX - sbX).coerceIn(0f, sbW)
        val relY = (mouseY - sbY).coerceIn(0f, sbH)

        cpData.saturation = relX / sbW
        cpData.brightness = 1f - (relY / sbH)

        // 应用到 ColorSetting
        setting.value = hsbToColorRGB(cpData.hue, cpData.saturation, cpData.brightness, cpData.alpha)
    }

    /**
     * 更新 Hue
     */
    private fun updateHue(cpData: ColorPickerData, mouseY: Float, hueY: Float, hueH: Float, setting: ColorSetting) {
        val relY = (mouseY - hueY).coerceIn(0f, hueH)
        cpData.hue = relY / hueH

        // 应用到 ColorSetting
        setting.value = hsbToColorRGB(cpData.hue, cpData.saturation, cpData.brightness, cpData.alpha)
    }

    /**
     * 更新 Alpha
     */
    private fun updateAlpha(cpData: ColorPickerData, mouseY: Float, alphaY: Float, alphaH: Float, setting: ColorSetting) {
        val relY = (mouseY - alphaY).coerceIn(0f, alphaH)
        cpData.alpha = 1f - (relY / alphaH)

        // 应用到 ColorSetting
        setting.value = hsbToColorRGB(cpData.hue, cpData.saturation, cpData.brightness, cpData.alpha)
    }

    /**
     * NumberSetting 增加 step
     */
    private fun incrementNumber(setting: NumberSetting<*>) {
        when (setting) {
            is IntSetting -> {
                setting.value = (setting.value + setting.step).coerceIn(setting.minValue, setting.maxValue)
            }
            is FloatSetting -> {
                setting.value = (setting.value + setting.step).coerceIn(setting.minValue, setting.maxValue)
            }
            is DoubleSetting -> {
                setting.value = (setting.value + setting.step).coerceIn(setting.minValue, setting.maxValue)
            }
            is LongSetting -> {
                setting.value = (setting.value + setting.step).coerceIn(setting.minValue, setting.maxValue)
            }
        }
    }

    /**
     * NumberSetting 减少 step
     */
    private fun decrementNumber(setting: NumberSetting<*>) {
        when (setting) {
            is IntSetting -> {
                setting.value = (setting.value - setting.step).coerceIn(setting.minValue, setting.maxValue)
            }
            is FloatSetting -> {
                setting.value = (setting.value - setting.step).coerceIn(setting.minValue, setting.maxValue)
            }
            is DoubleSetting -> {
                setting.value = (setting.value - setting.step).coerceIn(setting.minValue, setting.maxValue)
            }
            is LongSetting -> {
                setting.value = (setting.value - setting.step).coerceIn(setting.minValue, setting.maxValue)
            }
        }
    }
}

/**
 * 用来存储某个 ColorSetting 的展开状态 & HSB 参数
 */
data class ColorPickerData(
    var isOpen: Boolean = true,
    var hue: Float,
    var saturation: Float,
    var brightness: Float,
    var alpha: Float
) {
    // 方便取当前颜色
    fun value(): ColorRGB = hsbToColorRGB(hue, saturation, brightness, alpha)
}

/**
 * 将 ColorRGB -> (H, S, B, A)
 */
fun colorRGBToHSB(color: ColorRGB): FloatArray {
    val rInt = (color.r * 255).coerceIn(0, 255)
    val gInt = (color.g * 255).coerceIn(0, 255)
    val bInt = (color.b * 255).coerceIn(0, 255)
    val hsb = Color.RGBtoHSB(rInt, gInt, bInt, null)
    return floatArrayOf(hsb[0], hsb[1], hsb[2], color.a.toFloat())
}

/**
 * (H, S, B, A) -> ColorRGB
 */
fun hsbToColorRGB(hue: Float, saturation: Float, brightness: Float, alpha: Float = 1f): ColorRGB {
    val rgbInt = Color.HSBtoRGB(hue, saturation, brightness)
    val rInt = (rgbInt shr 16) and 0xFF
    val gInt = (rgbInt shr 8) and 0xFF
    val bInt = (rgbInt) and 0xFF
    return ColorRGB(
        rInt / 255f,
        gInt / 255f,
        bInt / 255f,
        alpha
    )
}
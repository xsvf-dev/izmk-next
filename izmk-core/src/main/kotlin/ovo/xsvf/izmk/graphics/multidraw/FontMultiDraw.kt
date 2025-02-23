package ovo.xsvf.izmk.graphics.multidraw

import org.lwjgl.opengl.GL11.GL_TRIANGLES
import ovo.xsvf.izmk.graphics.buffer.VertexBufferObjects
import ovo.xsvf.izmk.graphics.buffer.multiDrawArrays
import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.font.FontAdapter
import ovo.xsvf.izmk.graphics.font.FontMode
import ovo.xsvf.izmk.graphics.font.FontRenderer.Companion.FONT_SIZE
import ovo.xsvf.izmk.graphics.font.FontRenderers
import ovo.xsvf.izmk.graphics.shader.impl.FontShader

class FontMultiDraw {

    private val texts = mutableListOf<Text>()
    private var vertexSize = 0

    private data class Text(
        val text: String,
        val x: Float,
        val y: Float,
        val color: ColorRGB,
        val shadow: Boolean,
        val scale: Float,
    )

    fun addText(text: String, x: Float, y: Float, color: ColorRGB, shadow: Boolean = false, scale: Float = 1.0f) {
        texts.add(Text(text, x, y, color, shadow, scale))
        vertexSize += if (shadow) 12 else 6
    }

    fun draw() {
        when (FontRenderers.fontMode) {
            FontMode.SPARSE -> {
                VertexBufferObjects.RenderFont.multiDrawArrays(GL_TRIANGLES, IntArray(texts.size) {
                    if (texts[it].shadow) 12 else 6
                }) {
                    texts.forEach { text ->
                        val font = FontRenderers.default.font

                        var continueIndex = -1
                        var color = text.color

                        val scale = text.scale / 40f * FONT_SIZE

                        font.sparse.tex.bind()
                        FontShader.textureUnit = font.sparse.tex.handle

                        var width = 0f
                        text.text.forEachIndexed { index, ch ->
                            if (index == continueIndex) {
                                continueIndex = -1
                                color = FontRenderers.default.getColor(ch)
                                return@forEachIndexed
                            }

                            if (ch == '§') {
                                continueIndex = index + 1
                                return@forEachIndexed
                            }

                            if (!font.canDisplay(ch)) return@forEachIndexed

                            width += drawCharSparse(font, ch, text.x + width, text.y, color, text.shadow, scale)
                        }

                        font.sparse.tex.unbind()
                        FontShader.textureUnit = null
                    }
                }
            }

            else -> {
                texts.forEach {
                    FontRenderers.drawString(it.text, it.x, it.y, it.color, it.shadow, it.scale)
                }
            }
        }

        texts.clear()
    }

    fun clear() {
        texts.clear()
    }

    private fun VertexBufferObjects.RenderFont.drawCharSparse(
        font: FontAdapter, ch: Char, x: Float, y: Float,
        color: ColorRGB, shadow: Boolean, scale: Float
    ): Float {
        val charData = font.getCharData(ch) ?: return 0f

        val width = charData.width * scale
        val height = charData.height * scale

        val startX = x
        val startY = y
        val endX = x + width
        val endY = y + height

        val startU = charData.uStart
        val startV = charData.vStart
        val endU = charData.uEnd
        val endV = charData.vEnd

        val chunk = font.sparse.getChunk(ch).toFloat()

        if (shadow) {
            val startX = x + 1
            val startY = y + 1
            val endX = x + width + 1
            val endY = y + height + 1

            texture(startX, startY, startU, startV, chunk, ColorRGB(0, 0, 0))
            texture(endX, startY, endU, startV, chunk, ColorRGB(0, 0, 0))
            texture(endX, endY, endU, endV, chunk, ColorRGB(0, 0, 0))
            texture(startX, startY, startU, startV, chunk, ColorRGB(0, 0, 0))
            texture(startX, endY, startU, endV, chunk, ColorRGB(0, 0, 0))
            texture(endX, endY, endU, endV, chunk, ColorRGB(0, 0, 0))
        }

        // Triangles mode
        texture(startX, startY, startU, startV, chunk, color)
        texture(endX, startY, endU, startV, chunk, color)
        texture(endX, endY, endU, endV, chunk, color)
        texture(startX, startY, startU, startV, chunk, color)
        texture(startX, endY, startU, endV, chunk, color)
        texture(endX, endY, endU, endV, chunk, color)

        return width
    }

}
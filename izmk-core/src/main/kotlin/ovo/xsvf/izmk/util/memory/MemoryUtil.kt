package ovo.xsvf.izmk.util.memory

import java.nio.ByteBuffer
import java.nio.ByteOrder

object MemoryUtil {
    fun createDirectByteBuffer(capacity: Int): ByteBuffer {
        return ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder())
    }
}
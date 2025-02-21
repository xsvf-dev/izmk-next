package ovo.xsvf.izmk.util.memory

import java.nio.ByteBuffer
import java.nio.ByteOrder

fun createDirectByteBuffer(capacity: Int): ByteBuffer {
    return ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder())
}
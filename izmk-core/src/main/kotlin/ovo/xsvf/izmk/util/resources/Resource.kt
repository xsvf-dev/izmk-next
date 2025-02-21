package ovo.xsvf.izmk.util.resources

import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.util.ResourceUtil

class Resource(
    path: String
) {
    val byteArr: ByteArray

    val data: String get() = String(byteArr, Charsets.UTF_8)

    init {
        val stream = ResourceUtil.getAsStream("${IZMK.ASSETS_DIRECTORY}/${path}")
            ?: throw IllegalArgumentException("Resource not found: ${IZMK.ASSETS_DIRECTORY}/${path}")

        byteArr = stream.readBytes()

        stream.close()
    }

}

package ovo.xsvf.izmk.util.extensions

import java.io.File

fun File.createIfNotExist() {
    kotlin.runCatching {
        if (!this.exists()) {
            this.parentFile.mkdirs()
            this.createNewFile()
        }
    }
}
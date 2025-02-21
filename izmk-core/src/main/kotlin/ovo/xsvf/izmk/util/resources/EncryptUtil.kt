package ovo.xsvf.izmk.util.resources

import java.io.File
import java.util.jar.JarInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.math.min

object EncryptUtil {
    private fun encodeInt(number: Int): ByteArray {
        val encryptedInt = (number xor 0x1A2B3C4D).let { (it shl 5) or (it ushr 27) }

        return ByteArray(4) {
            when (it) {
                0 -> (encryptedInt ushr 24).toByte()
                1 -> (encryptedInt ushr 16).toByte()
                2 -> (encryptedInt ushr 8).toByte()
                3 -> encryptedInt.toByte()
                else -> throw IllegalArgumentException("Invalid index: $it")
            }
        }
    }

    private fun decodeInt(encodedBytes: ByteArray, start: Int): Int {
        require(!(start < 0 || start >= encodedBytes.size)) { "start index out of range: $start" }

        val length = min(4.0, (encodedBytes.size - start).toDouble()).toInt()
        var decryptedInt = 0

        for (i in 0..<length) {
            decryptedInt = decryptedInt or ((encodedBytes[start + i].toInt() and 0xFF) shl (24 - i * 8))
        }

        return ((decryptedInt ushr 5) or (decryptedInt shl 27)) xor 0x1A2B3C4D
    }

    fun getBinaryFilesEncrypted(file: File, func: (String, ByteArray) -> Unit) {
        // fist, get the file bytes
        val fileBytes = file.readBytes()

        var index = 0
        // then, get the trash data offset
        val trashDataOffset = decodeInt(fileBytes, 0)
        index += 4 + trashDataOffset
        // get encrypted file size
        val encryptedFileSize = decodeInt(fileBytes, index)
        index += 4

        // get encrypted file bytes
        val encryptedFileBytes = ByteArray(encryptedFileSize)
        System.arraycopy(fileBytes, index, encryptedFileBytes, 0, encryptedFileSize)
        for (i in encryptedFileBytes.indices) encryptedFileBytes[i] =
            (encryptedFileBytes[i].toInt() xor -0x35214111).toByte()

        JarInputStream(encryptedFileBytes.inputStream()).use { zis ->
            var entry: ZipEntry?
            while ((zis.nextEntry.also { entry = it }) != null) {
                if (entry!!.isDirectory) continue
                val entryBytes = zis.readAllBytes()
                if (entryBytes.size > 4 && entryBytes[0] == 0xCA.toByte() &&
                    entryBytes[1] == 0xFE.toByte() &&
                    entryBytes[2] == 0xBA.toByte() &&
                    entryBytes[3] == 0xBE.toByte()) continue
                func(entry!!.name, entryBytes)
            }
        }
    }

    fun getBinaryFiles(file: File, func: (String, ByteArray) -> Unit) {
        // fist, get the file bytes
        val fileBytes: ByteArray = file.readBytes()
        // get the core file size
        val coreFileSize: Int = decodeInt(fileBytes, 0)
        // get the core file bytes
        val coreFileBytes = ByteArray(coreFileSize)
        System.arraycopy(fileBytes, 4, coreFileBytes, 0, coreFileSize)

        ZipInputStream(coreFileBytes.inputStream()).use { zis ->
            var entry: ZipEntry?
            while ((zis.nextEntry.also { entry = it }) != null) {
                if (entry!!.isDirectory) continue
                val entryBytes = zis.readAllBytes()
                if (entryBytes.size > 4 && entryBytes[0] == 0xCA.toByte() &&
                    entryBytes[1] == 0xFE.toByte() &&
                    entryBytes[2] == 0xBA.toByte() &&
                    entryBytes[3] == 0xBE.toByte()) continue
                func(entry!!.name, entryBytes)
            }
        }
    }
}

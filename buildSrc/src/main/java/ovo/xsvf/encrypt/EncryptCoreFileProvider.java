package ovo.xsvf.encrypt;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class EncryptCoreFileProvider {
    public static boolean DEV = false;

    public static List<byte[]> getClasses(String filePath) throws IOException {
        List<byte[]> binaryFiles = new ArrayList<>();
        // fist, get the file bytes
        byte[] fileBytes;
        int index = 0;
        try (FileInputStream fis = new FileInputStream(filePath)) {
            fileBytes = fis.readAllBytes();
        }
        // then, get the trash data offset
        int trashDataOffset = decodeInt(fileBytes, 0);
        index += 4 + trashDataOffset;
        // get encrypted file size
        int encryptedFileSize = decodeInt(fileBytes, index);
        index += 4;
        // get encrypted file bytes
        byte[] encryptedFileBytes = new byte[encryptedFileSize];
        System.arraycopy(fileBytes, index, encryptedFileBytes, 0, encryptedFileSize);
        for (int i = 0; i < encryptedFileBytes.length; i++) encryptedFileBytes[i] ^= (byte) 0xCADEBEEF;
        // open it as a zip file
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(encryptedFileBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    byte[] bytes = zis.readAllBytes();
                    // CA FE BA BE
                    if (bytes.length > 4 && bytes[0] == (byte) 0xCA && bytes[1] == (byte) 0xFE && bytes[2] == (byte) 0xBA && bytes[3] == (byte) 0xBE) {
                        // vaild class file, decrypt it
                        List<Byte> decryptedBytes = new ArrayList<>();
                        byte[] toDecrypt = new byte[bytes.length - 4];
                        int index2 = 0;
                        System.arraycopy(bytes, 4, toDecrypt, 0, toDecrypt.length);
                        while (index2 < toDecrypt.length) {
                            // read trash data length and skip it
                            int trashDataLength = decodeInt(toDecrypt, index2);
                            index2 += 4 + trashDataLength;
                            // read real data length
                            int realDataLength = decodeInt(toDecrypt, index2);
                            index2 += 4;
                            // add the real data to the decrypted bytes
                            for (int j = 0; j < realDataLength; j++) {
                                decryptedBytes.add((byte) (toDecrypt[index2 + j] ^ (byte) 0xCADEBEEF));
                            }
                            index2 += realDataLength;
                        }
                        // convert the decrypted bytes to byte array
                        byte[] decryptedBytesArray = new byte[decryptedBytes.size()];
                        for (int j = 0; j < decryptedBytes.size(); j++) {
                            decryptedBytesArray[j] = decryptedBytes.get(j);
                        }
                        binaryFiles.add(decryptedBytesArray);
                    }
                }
            }
        }
        return binaryFiles;
    }

    public static Map<String, byte[]> getResources(String filePath) throws IOException {
        Map<String, byte[]> binaryFiles = new HashMap<>();
        // fist, get the file bytes
        byte[] fileBytes;
        int index = 0;
        try (FileInputStream fis = new FileInputStream(filePath)) {
            fileBytes = fis.readAllBytes();
        }
        // then, get the trash data offset
        int trashDataOffset = decodeInt(fileBytes, 0);
        index += 4 + trashDataOffset;
        // get encrypted file size
        int encryptedFileSize = decodeInt(fileBytes, index);
        index += 4;
        // get encrypted file bytes
        byte[] encryptedFileBytes = new byte[encryptedFileSize];
        System.arraycopy(fileBytes, index, encryptedFileBytes, 0, encryptedFileSize);
        for (int i = 0; i < encryptedFileBytes.length; i++) encryptedFileBytes[i] ^= (byte) 0xCADEBEEF;
        // open it as a zip file
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(encryptedFileBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    byte[] bytes = zis.readAllBytes();
                    // CA FE BA BE
                    if (bytes.length <= 4 || bytes[0] != (byte) 0xCA || bytes[1] != (byte) 0xFE || bytes[2] != (byte) 0xBA || bytes[3] != (byte) 0xBE) {
                        binaryFiles.put(entry.getName(), bytes);
                    }
                }
            }
        }
        return binaryFiles;
    }

    private static int decodeInt(byte[] encodedBytes, int start) {
        if (start < 0 || start >= encodedBytes.length) {
            throw new IllegalArgumentException("start index out of range: " + start);
        }

        int length = Math.min(4, encodedBytes.length - start);
        int decryptedInt = 0;

        for (int i = 0; i < length; i++) {
            decryptedInt |= (encodedBytes[start + i] & 0xFF) << (24 - i * 8);
        }

        decryptedInt = (decryptedInt >>> 5) | (decryptedInt << 27); // 循环右移
        decryptedInt ^= 0x1A2B3C4D; // XOR操作，使用相同的掩码

        return decryptedInt;
    }
}

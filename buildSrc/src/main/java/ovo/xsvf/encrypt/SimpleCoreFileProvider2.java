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

public class SimpleCoreFileProvider2 {
    public static boolean DEV = false;

    public static List<byte[]> getClasses(String filePath) throws IOException {
        List<byte[]> binaryFiles = new ArrayList<>();
        // fist, get the file bytes
        byte[] fileBytes;
        try (FileInputStream fis = new FileInputStream(filePath)) {
            fileBytes = fis.readAllBytes();
        }
        // get the core file size
        int coreFileSize = decodeInt(fileBytes, 0);
        // get the core file bytes
        byte[] coreFileBytes = new byte[coreFileSize];
        System.arraycopy(fileBytes, 4, coreFileBytes, 0, coreFileSize);
        // open it as zip file
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(coreFileBytes))) {
            while (zis.getNextEntry() != null) {
                byte[] entryBytes = zis.readAllBytes();
                // if it starts with 0xca, 0xfe, 0xba, 0xbe, it's a class file
                if (entryBytes.length > 4 && entryBytes[0] == (byte) 0xCA && entryBytes[1] == (byte) 0xFE && entryBytes[2] == (byte) 0xBA && entryBytes[3] == (byte) 0xBE) {
                    binaryFiles.add(entryBytes);
                }
            }
        }
        return binaryFiles;
    }

    public static Map<String, byte[]> getResources(String jarPath) throws IOException {
        Map<String, byte[]> binaryFiles = new HashMap<>();
        // fist, get the file bytes
        byte[] fileBytes;
        try (FileInputStream fis = new FileInputStream(jarPath)) {
            fileBytes = fis.readAllBytes();
        }
        // get the core file size
        int coreFileSize = decodeInt(fileBytes, 0);
        // get the core file bytes
        byte[] coreFileBytes = new byte[coreFileSize];
        System.arraycopy(fileBytes, 4, coreFileBytes, 0, coreFileSize);
        // open it as zip file
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(coreFileBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                byte[] entryBytes = zis.readAllBytes();
                // if it starts with 0xca, 0xfe, 0xba, 0xbe, it's a class file
                if (entryBytes.length <= 4 || entryBytes[0] != (byte) 0xCA || entryBytes[1] != (byte) 0xFE || entryBytes[2] != (byte) 0xBA || entryBytes[3] != (byte) 0xBE) {
                    if (!entry.isDirectory()) {
                        binaryFiles.put(entry.getName().replace("\\", "/"), entryBytes);
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
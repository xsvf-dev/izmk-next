package ovo.xsvf.encrypt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class EncryptUtil {
    private static final Random random = new Random();

    public static byte[] encodeInt(int number) {
        int encryptedInt = number ^ 0x1A2B3C4D; // XOR操作，使用一个固定的掩码
        encryptedInt = (encryptedInt << 5) | (encryptedInt >>> 27); // 循环左移

        byte[] result = new byte[4];
        result[0] = (byte) (encryptedInt >>> 24); // 取最高8位
        result[1] = (byte) (encryptedInt >>> 16); // 取次高8位
        result[2] = (byte) (encryptedInt >>> 8);  // 取次低8位
        result[3] = (byte) encryptedInt;          // 取最低8位

        return result;
    }

    public static byte[] encodeClassBytes(byte[] bytes) {
        List<Byte> encryptedBytes = new ArrayList<>();
        int index = 0;
        // put some bytes for reconizing
        encryptedBytes.add((byte) 0xCA);
        encryptedBytes.add((byte) 0xFE);
        encryptedBytes.add((byte) 0xBA);
        encryptedBytes.add((byte) 0xBE);
        while (index < bytes.length) {
            // random trash bytes
            int trashLength = random.nextInt(256, 4096);
            byte[] trashBytes = getRandomBytes(trashLength);
            for (byte b : encodeInt(trashLength)) encryptedBytes.add(b);
            for (byte b : trashBytes) encryptedBytes.add(b);

            int dataLength = random.nextInt(bytes.length / 5, bytes.length / 2);
            // check if dataLength is too large
            if (index + dataLength > bytes.length) dataLength = bytes.length - index;
            byte[] dataBytes = new byte[dataLength];
            System.arraycopy(bytes, index, dataBytes, 0, dataLength);
            for (byte b : encodeInt(dataLength)) encryptedBytes.add(b);
            for (byte b : dataBytes) encryptedBytes.add((byte) (b ^ 0xCADEBEEF));
            index += dataLength;
        }
        // the final file look like:
        // [CA FE BA BE] [length1] [trash1] [dataLength1] [data1] [length2] [trash2] [dataLength2] [data2]...
        return toByteArray(encryptedBytes);
    }

    private static byte[] toByteArray(List<Byte> list) {
        byte[] result = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    public static byte[] getRandomBytes(int length) {
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) (random.nextInt(-256, 256));
        }
        return bytes;
    }
}

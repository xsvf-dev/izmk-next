package ovo.xsvf.encrypt;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

public class RandomUtil {
    private static final SecureRandom secureRandom = new SecureRandom();

    // 生成指定范围内的随机数字（不唯一）
    public static int generateRandomNumber(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("min must be less than max");
        }
        return secureRandom.nextInt((max - min) + 1) + min;
    }

    // 其他方法保持不变
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final Set<String> existingStrings = new HashSet<>();

    public static String generateRandomString(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be greater than 0");
        }
        StringBuilder result = new StringBuilder(length);

        while (result.length() < length) {
            int index = secureRandom.nextInt(CHARACTERS.length());
            char randomChar = CHARACTERS.charAt(index);
            result.append(randomChar);
        }

        // 确保生成的字符串不重复
        if (!existingStrings.add(result.toString())) {
            // 如果添加失败，说明生成的字符串已经存在，重新生成
            return generateRandomString(length);
        }

        return result.toString();
    }

    public static String generateUniqueRandomString(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be greater than 0");
        }
        StringBuilder result = new StringBuilder(length);

        while (result.length() < length) {
            int index = secureRandom.nextInt(CHARACTERS.length());
            char randomChar = CHARACTERS.charAt(index);
            result.append(randomChar);
        }

        // 确保生成的字符串不重复
        if (!existingStrings.add(result.toString())) {
            // 如果添加失败，说明生成的字符串已经存在，重新生成
            return generateUniqueRandomString(length);
        }

        return result.toString();
    }

    public static int generateUniqueRandomNumber(int min, int max, Set<Integer> existingNumbers) {
        if (min >= max) {
            throw new IllegalArgumentException("min must be less than max");
        }
        int randomNumber = secureRandom.nextInt((max - min) + 1) + min;

        // 确保生成的数字不重复
        if (!existingNumbers.add(randomNumber)) {
            // 如果添加失败，说明生成的数字已经存在，重新生成
            return generateUniqueRandomNumber(min, max, existingNumbers);
        }

        return randomNumber;
    }

    public static String generateRandomFileName(int folders) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < folders; i++) {
            result.append(generateRandomString(generateRandomNumber(5, 15)));
            result.append("/");
        }
        result.append(generateRandomString(generateRandomNumber(10, 20)));
        return result.toString();
    }
}

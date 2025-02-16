package ovo.xsvf.task;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class UploadTask extends DefaultTask {
    public File sourceFile;
    public String changelog;
    public boolean force = false;
    private final String uploadURL = getProject().getExtensions().getExtraProperties().get("publishUrl").toString();
    private final String timestamp = getProject().getExtensions().getExtraProperties().get("publishVersion").toString();

    @TaskAction
    public void run() {
        assert sourceFile != null || changelog != null;

        try {
            byte[] fileContent = Files.readAllBytes(Paths.get(sourceFile.getAbsolutePath()));
            String sha256 = calculateSHA256(fileContent);
            JsonObject jsonData = new JsonObject();
            jsonData.addProperty("timestamp", Long.parseLong(timestamp));
            jsonData.addProperty("sha256", sha256);
            jsonData.addProperty("force", force);
            jsonData.addProperty("changelog",
                    Base64.getEncoder().encodeToString(changelog.getBytes(StandardCharsets.UTF_8)));

            getLogger().info(jsonData.toString());
            JsonObject jsonResponse = sendHttpRequest(uploadURL, jsonData);
            if (jsonResponse != null && jsonResponse.get("message").getAsString().equalsIgnoreCase("ok!")) {
                getLogger().info("uploaded succcefully");
            } else {
                throw new RuntimeException("Failed to upload file");
            }
        } catch (IOException e) {
            getLogger().error("Failed to read file: {}", sourceFile.getAbsolutePath(), e);
        }
    }

    private @NotNull String calculateSHA256(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private @Nullable JsonObject sendHttpRequest(@NotNull String url, @NotNull JsonObject jsonObject) {
        String jsonStr = jsonObject.toString();
        try {
            String encodedJsonStr = URLEncoder.encode(jsonStr, StandardCharsets.UTF_8);
            String requestUrl = url.formatted(encodedJsonStr);
            URL obj = new URL(requestUrl);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // 将响应内容解析为JsonObject
                return JsonParser.parseString(response.toString()).getAsJsonObject();
            } else {
                getLogger().error("Failed to send HTTP request. Response code: {}", responseCode);
            }
        } catch (IOException e) {
            getLogger().error("Failed to send HTTP request to URL: {}", url, e);
        }
        return null;
    }
}
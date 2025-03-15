package ovo.xsvf;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class Mapping {
    public final HashMap<String, String> fieldMapping = new HashMap<>();
    public final HashMap<Pair<String, String>, Pair<String, String>> methodsMapping = new HashMap<>(); // name,desc
    public final HashMap<String, String> classesMapping = new HashMap<>();

    public final HashMap<String, String> revFieldMapping = new HashMap<>();
    public final HashMap<Pair<String, String>, Pair<String, String>> revMethodsMapping = new HashMap<>(); // name,desc
    public final HashMap<String, String> revClassesMapping = new HashMap<>();

    private final byte[] mapBytes;

    public Mapping(byte[] bytes) {
        mapBytes = bytes;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String type = line.split(": ")[0];
                String[] data = line.split(": ")[1].split(" ");
                switch (type) {
                    case "CL": {
                        classesMapping.put(data[0], data[1]);
                        revClassesMapping.put(data[1], data[0]);
                        break;
                    }
                    case "FD": {
                        fieldMapping.put(data[0], data[1]);
                        revFieldMapping.put(data[1], data[0]);
                        break;
                    }
                    case "MD": {
                        methodsMapping.put(Pair.of(data[0], data[1]), Pair.of(data[2], data[3]));
                        revMethodsMapping.put(Pair.of(data[2], data[3]), Pair.of(data[0], data[1]));
                        break;
                    }
                }
            }
        } catch (IOException ignored) { }
    }

    public Mapping(String map) {
        this(map.getBytes(StandardCharsets.UTF_8));
    }

    public byte[] getBytes() {
        return mapBytes;
    }
}

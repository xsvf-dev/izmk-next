package ovo.xsvf.patchify;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public final class Mapping {
    public final Object2ObjectMap<String, String> fieldMapping =
            new Object2ObjectOpenHashMap<>(5000);
    public final Object2ObjectMap<Pair<String, String>, Pair<String, String>> methodsMapping =
            new Object2ObjectOpenHashMap<>(5000);
    public final Object2ObjectMap<String, String> classesMapping =
            new Object2ObjectOpenHashMap<>(5000);

    public final Object2ObjectMap<String, String> revFieldMapping =
            new Object2ObjectOpenHashMap<>(5000);
    public final Object2ObjectMap<Pair<String, String>, Pair<String, String>> revMethodsMapping =
            new Object2ObjectOpenHashMap<>(5000);
    public final Object2ObjectMap<String, String> revClassesMapping =
            new Object2ObjectOpenHashMap<>(5000);

    public Mapping(byte[] bytes) {
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
        } catch (IOException ignored) {
        }
    }
}

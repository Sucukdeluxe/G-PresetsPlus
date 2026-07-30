package utils;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class SettingsCache {

    private static final String FILENAME = "cache.json";
    private static String cacheDir = "Cache";

    public static void setCacheDir(String dir) {
        cacheDir = dir;
    }

    public static String getCacheDir() {
        return cacheDir;
    }

    public static synchronized JSONObject getCacheContents() {
        File f = new File(cacheDir, FILENAME);
        if (f.exists() && !f.isDirectory()) {
            try {
                return new JSONObject(String.join("\n", Files.readAllLines(f.toPath())));
            } catch (IOException | RuntimeException e) {
                e.printStackTrace();
            }
        }
        return new JSONObject();
    }

    public static synchronized void put(String key, Object value) {
        JSONObject contents = getCacheContents();
        contents.remove(key);
        contents.put(key, value);
        write(contents);
    }

    public static synchronized Object get(String key) {
        JSONObject contents = getCacheContents();
        return contents.has(key) ? contents.get(key) : null;
    }

    private static void write(JSONObject contents) {
        new File(cacheDir).mkdirs();
        try (FileWriter file = new FileWriter(new File(cacheDir, FILENAME))) {
            file.write(contents.toString(2));
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

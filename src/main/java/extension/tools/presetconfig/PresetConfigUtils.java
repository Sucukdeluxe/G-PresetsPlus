package extension.tools.presetconfig;

import extension.GRoomCloner;
import org.json.JSONObject;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PresetConfigUtils {
    public static final String PRESET_EXT = ".json";
    public static final String ROOM_EXT = ".roomJson";

    public static String presetPath() {
        try {
            String path = (new File(GRoomCloner.class.getProtectionDomain().getCodeSource().getLocation().toURI()))
                    .getParentFile().toString();
            return Paths.get(path, "presets").toString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return "";
    }


    public static List<String> listPresets() {
        List<String> presets = new ArrayList<>();
        File presetsDir = new File(presetPath());

        if (!presetsDir.isDirectory()) {
            return presets;
        }

        File[] files = presetsDir.listFiles();
        if (files != null) {
            for (File presetFile : files) {
                if (presetFile.isFile()) {
                    String name = presetFile.getName();
                    if (name.endsWith(PRESET_EXT)) {
                        name = name.substring(0, name.length() - PRESET_EXT.length());
                        presets.add(name);
                    }
                }
            }
        }
        return presets;
    }

    public static boolean presetExists(String name) {
        return new File(presetPath(), name + PRESET_EXT).isFile();
    }

    public static boolean isValidPresetName(String name) {
        if (name == null) {
            return false;
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty() || trimmed.length() > 120) {
            return false;
        }
        if (trimmed.startsWith(".")) {
            return false;
        }
        for (char forbidden : "/\\:*?\"<>|".toCharArray()) {
            if (trimmed.indexOf(forbidden) >= 0) {
                return false;
            }
        }
        return true;
    }

    public static class Counts {
        public final int furni;
        public final int wired;

        Counts(int furni, int wired) {
            this.furni = furni;
            this.wired = wired;
        }
    }

    private static final java.util.Map<String, String> COUNT_STAMPS = new java.util.HashMap<>();
    private static final java.util.Map<String, Counts> COUNT_CACHE = new java.util.HashMap<>();

    public static synchronized Counts counts(String name) {
        File file = new File(presetPath(), name + PRESET_EXT);
        if (!file.isFile()) {
            COUNT_STAMPS.remove(name);
            COUNT_CACHE.remove(name);
            return null;
        }

        String stamp = file.length() + "/" + file.lastModified();
        if (stamp.equals(COUNT_STAMPS.get(name))) {
            return COUNT_CACHE.get(name);
        }

        Counts counts = read(file);
        if (counts != null) {
            COUNT_STAMPS.put(name, stamp);
            COUNT_CACHE.put(name, counts);
        }
        return counts;
    }

    private static Counts read(File file) {
        try {
            String contents = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            JSONObject json = new JSONObject(contents);
            int furni = json.optJSONArray("furni") == null ? 0 : json.getJSONArray("furni").length();

            int wired = 0;
            JSONObject wiredJson = json.optJSONObject("wired");
            if (wiredJson != null) {
                for (String key : new String[] { "triggers", "conditions", "effects",
                        "selectors", "addons", "variables" }) {
                    if (wiredJson.optJSONArray(key) != null) {
                        wired += wiredJson.getJSONArray(key).length();
                    }
                }
            }
            return new Counts(furni, wired);
        } catch (Throwable t) {
            return null;
        }
    }

    public static boolean nameTaken(String name) {
        File dir = new File(presetPath());
        return new File(dir, name + PRESET_EXT).isFile()
                || new File(dir, name + ROOM_EXT).isFile();
    }

    public static String uniqueName(String base) {
        return numberedName(base, PresetConfigUtils::nameTaken);
    }

    public static String numberedName(String base, java.util.function.Predicate<String> taken) {
        if (!taken.test(base)) {
            return base;
        }
        for (int copy = 1; copy <= 999; copy++) {
            String candidate = base + " (" + copy + ")";
            if (!taken.test(candidate)) {
                return candidate;
            }
        }
        return base;
    }

    public static boolean renamePreset(String from, String to) {
        File dir = new File(presetPath());
        File source = new File(dir, from + PRESET_EXT);
        File target = new File(dir, to + PRESET_EXT);
        if (!source.isFile() || target.exists()) {
            return false;
        }
        if (!source.renameTo(target)) {
            return false;
        }
        File roomSource = new File(dir, from + ROOM_EXT);
        if (roomSource.isFile()) {
            roomSource.renameTo(new File(dir, to + ROOM_EXT));
        }
        return true;
    }

    public static boolean deletePreset(String name) {
        File dir = new File(presetPath());
        File room = new File(dir, name + ROOM_EXT);
        if (room.isFile()) {
            room.delete();
        }
        File config = new File(dir, name + PRESET_EXT);
        return !config.isFile() || config.delete();
    }

    public static boolean savePreset(String name, PresetConfig config) {
        File presetPath = new File(presetPath());
        presetPath.mkdirs();

        try (Writer file = new OutputStreamWriter(Files.newOutputStream(new File(presetPath(), name + PRESET_EXT).toPath()), StandardCharsets.UTF_8)) {
            file.write(config.toJsonObject().toString(4));
            file.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static PresetConfig loadPreset(String name) {
        File file = new File(presetPath(), name + PRESET_EXT);
        if (file.exists() && file.isFile()) {
            try {
                String contents = String.join("\n", Files.readAllLines(file.toPath(), StandardCharsets.UTF_8));
                return new PresetConfig(new JSONObject(contents));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

}

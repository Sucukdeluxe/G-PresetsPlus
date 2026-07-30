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

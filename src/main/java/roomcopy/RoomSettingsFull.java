package roomcopy;

import utils.Messages;
import extension.logger.Logger;
import gearth.protocol.HMessage;
import gearth.protocol.HPacket;
import org.json.JSONArray;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RoomSettingsFull {

    public final int roomId;
    public final String name;
    public final String description;
    public final int doorMode;
    public final int categoryId;
    public final int maximumVisitors;
    public final List<String> tags = new ArrayList<>();
    public final int tradeMode;
    public final boolean allowPets;
    public final boolean allowFoodConsume;
    public final boolean allowWalkThrough;
    public final boolean hideWalls;
    public final int wallThickness;
    public final int floorThickness;
    public final int chatFloodSensitivity;
    public final boolean leaveOnDoorTile;
    public final boolean idleSleepEnabled;
    public final int idleSleepTimeoutSeconds;
    public final boolean idleAutokickEnabled;
    public final int idleAutokickTimeoutSeconds;
    public final boolean muteAllPets;
    public final int whoCanMute;
    public final int whoCanKick;
    public final int whoCanBan;
    public final String doorPassword;

    private RoomSettingsFull(HPacket packet) {
        packet.resetReadIndex();
        this.roomId = packet.readInteger();
        this.name = packet.readString(StandardCharsets.UTF_8);
        this.description = packet.readString(StandardCharsets.UTF_8);
        this.doorMode = packet.readInteger();
        this.categoryId = packet.readInteger();
        this.maximumVisitors = packet.readInteger();
        packet.readInteger();

        int tagCount = packet.readInteger();
        if (tagCount < 0 || tagCount > 64) {
            throw new IllegalStateException("implausible tag count " + tagCount);
        }
        for (int i = 0; i < tagCount; i++) {
            tags.add(packet.readString(StandardCharsets.UTF_8));
        }

        this.tradeMode = packet.readInteger();
        this.allowPets = packet.readInteger() != 0;
        this.allowFoodConsume = packet.readInteger() != 0;
        this.allowWalkThrough = packet.readInteger() != 0;
        this.hideWalls = packet.readInteger() != 0;
        this.wallThickness = packet.readInteger();
        this.floorThickness = packet.readInteger();
        this.chatFloodSensitivity = packet.readInteger();
        this.leaveOnDoorTile = packet.readBoolean();
        this.idleSleepEnabled = packet.readBoolean();
        this.idleSleepTimeoutSeconds = packet.readInteger();
        this.idleAutokickEnabled = packet.readBoolean();
        this.idleAutokickTimeoutSeconds = packet.readInteger();
        this.muteAllPets = packet.readBoolean();
        this.whoCanMute = packet.readInteger();
        this.whoCanKick = packet.readInteger();
        this.whoCanBan = packet.readInteger();
        this.doorPassword = "";
    }

    public static RoomSettingsFull request(Executor executor, Logger logger, int roomId) {
        return request(executor, logger, roomId, false);
    }

    public static RoomSettingsFull request(Executor executor, Logger logger, int roomId, boolean quiet) {
        if (!executor.isKnownName(HMessage.Direction.TOSERVER, "GetRoomSettings")) {
            logger.log(Messages.get("settings.header_unresolvable"), "orange");
            return null;
        }

        Executor.AwaitingPacket answer =
                new Executor.AwaitingPacket("RoomSettingsData", HMessage.Direction.TOCLIENT, 6000)
                        .addConditions(packet -> packet.readInteger() == roomId);
        executor.register(answer);

        if (!executor.sendToServer("GetRoomSettings", roomId)) {
            logger.log(Messages.get("settings.get.send_failed"), "orange");
            return null;
        }

        HPacket response = executor.awaitPacket(answer);
        if (response == null) {
            logger.log(Messages.get("settings.no_response"), "orange");
            return null;
        }

        try {
            RoomSettingsFull settings = new RoomSettingsFull(response);
            if (!quiet) {
                logger.log(Messages.get("settings.full_read",
                        settings.chatFloodSensitivity,
                        settings.idleSleepTimeoutSeconds, settings.idleAutokickTimeoutSeconds,
                        settings.whoCanMute, settings.whoCanKick, settings.whoCanBan), "green");
            }
            return settings;
        } catch (Throwable t) {
            logger.log(Messages.get("settings.read_failed", t), "orange");
            return null;
        }
    }

    public boolean applyTo(Executor executor, int targetRoomId, String password, int doorModeOverride,
                           String nameOverride) {
        return applyTo(executor, targetRoomId, password, doorModeOverride, nameOverride, false);
    }

    public boolean applyTo(Executor executor, int targetRoomId, String password, int doorModeOverride,
                           String nameOverride, boolean rawStrings) {
        Object[] args = args(targetRoomId,
                password == null || password.isEmpty() ? doorPassword : password,
                doorModeOverride, nameOverride);
        return rawStrings
                ? executor.sendToServerRaw("SaveRoomSettings", args)
                : executor.sendToServer("SaveRoomSettings", args);
    }

    private Object[] args(int targetRoomId, String password, int doorModeOverride, String nameOverride) {
        return new Object[] {
                targetRoomId, nameOverride == null ? name : nameOverride, description,
                doorModeOverride, password == null ? "" : password,
                maximumVisitors, categoryId, 0, tradeMode,
                allowPets, allowFoodConsume, allowWalkThrough, hideWalls,
                wallThickness, floorThickness,
                whoCanMute, whoCanKick, whoCanBan,
                chatFloodSensitivity,
                leaveOnDoorTile, idleSleepEnabled, idleSleepTimeoutSeconds,
                idleAutokickEnabled, idleAutokickTimeoutSeconds, muteAllPets };
    }

    public RoomSettingsFull(JSONObject json) {
        this.roomId = json.optInt("roomId", 0);
        this.name = json.optString("name", "");
        this.description = json.optString("description", "");
        this.doorMode = json.optInt("doorMode", 0);
        this.categoryId = json.optInt("categoryId", 0);
        this.maximumVisitors = json.optInt("maximumVisitors", 25);
        JSONArray storedTags = json.optJSONArray("tags");
        if (storedTags != null) {
            for (int i = 0; i < storedTags.length() && i < 2; i++) {
                String tag = storedTags.optString(i, "").trim();
                if (!tag.isEmpty()) {
                    tags.add(tag);
                }
            }
        }
        this.tradeMode = json.optInt("tradeMode", 0);
        this.allowPets = json.optBoolean("allowPets", false);
        this.allowFoodConsume = json.optBoolean("allowFoodConsume", false);
        this.allowWalkThrough = json.optBoolean("allowWalkThrough", true);
        this.hideWalls = json.optBoolean("hideWalls", false);
        this.wallThickness = json.optInt("wallThickness", 0);
        this.floorThickness = json.optInt("floorThickness", 0);
        this.chatFloodSensitivity = json.optInt("chatFloodSensitivity", 1);
        this.leaveOnDoorTile = json.optBoolean("leaveOnDoorTile", true);
        this.idleSleepEnabled = json.optBoolean("idleSleepEnabled", true);
        this.idleSleepTimeoutSeconds = json.optInt("idleSleepTimeoutSeconds", 300);
        this.idleAutokickEnabled = json.optBoolean("idleAutokickEnabled", true);
        this.idleAutokickTimeoutSeconds = json.optInt("idleAutokickTimeoutSeconds", 900);
        this.muteAllPets = json.optBoolean("muteAllPets", false);
        this.whoCanMute = json.optInt("whoCanMute", 0);
        this.whoCanKick = json.optInt("whoCanKick", 0);
        this.whoCanBan = json.optInt("whoCanBan", 0);
        this.doorPassword = json.optString("doorPassword", "");
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("roomId", roomId);
        json.put("name", name);
        json.put("description", description);
        json.put("doorMode", doorMode);
        json.put("categoryId", categoryId);
        json.put("maximumVisitors", maximumVisitors);
        json.put("tags", new JSONArray(tags));
        json.put("tradeMode", tradeMode);
        json.put("allowPets", allowPets);
        json.put("allowFoodConsume", allowFoodConsume);
        json.put("allowWalkThrough", allowWalkThrough);
        json.put("hideWalls", hideWalls);
        json.put("wallThickness", wallThickness);
        json.put("floorThickness", floorThickness);
        json.put("chatFloodSensitivity", chatFloodSensitivity);
        json.put("leaveOnDoorTile", leaveOnDoorTile);
        json.put("idleSleepEnabled", idleSleepEnabled);
        json.put("idleSleepTimeoutSeconds", idleSleepTimeoutSeconds);
        json.put("idleAutokickEnabled", idleAutokickEnabled);
        json.put("idleAutokickTimeoutSeconds", idleAutokickTimeoutSeconds);
        json.put("muteAllPets", muteAllPets);
        json.put("whoCanMute", whoCanMute);
        json.put("whoCanKick", whoCanKick);
        json.put("whoCanBan", whoCanBan);
        json.put("doorPassword", doorPassword);
        return json;
    }
}

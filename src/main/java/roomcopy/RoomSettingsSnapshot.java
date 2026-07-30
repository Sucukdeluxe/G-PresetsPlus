package roomcopy;

import gearth.protocol.HPacket;

import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

public class RoomSettingsSnapshot {

    public final int id;
    public final String name;
    public final String description;
    public final String ownerName;
    public final int ownerId;
    public final int lockType;
    public final int maxUsers;
    public final int tradingMode;
    public final int category;
    public final int freeflow;
    public final int chatSize;
    public final int scrollMethod;
    public final int chatDistance;
    public final int floodMode;
    public final int muteRights;
    public final int kickRights;
    public final int banRights;
    public final int wallThickness;
    public final int floorThickness;
    public final boolean wallsHidden;
    public final boolean allowPets;

    public final int missingFields;

    private static final class SafeReader {
        private final HPacket packet;
        private int failures = 0;

        SafeReader(HPacket packet) {
            this.packet = packet;
        }

        int readInt(int fallback) {
            if (packet == null) return fallback;
            try {
                return packet.readInteger();
            } catch (Throwable t) {
                failures++;
                return fallback;
            }
        }

        String readString(String fallback) {
            if (packet == null) return fallback;
            try {
                return packet.readString(StandardCharsets.UTF_8);
            } catch (Throwable t) {
                failures++;
                return fallback;
            }
        }

        boolean readBoolean(boolean fallback) {
            if (packet == null) return fallback;
            try {
                return packet.readBoolean();
            } catch (Throwable t) {
                failures++;
                return fallback;
            }
        }
    }

    public RoomSettingsSnapshot(HPacket roomPacket, HPacket visualizationPacket) {
        roomPacket.setReadIndex(7);
        SafeReader room = new SafeReader(roomPacket);

        this.id = room.readInt(0);
        this.name = room.readString("Unknown");
        this.ownerId = room.readInt(0);
        this.ownerName = room.readString("");
        this.lockType = room.readInt(0);

        room.readInt(0);

        this.maxUsers = room.readInt(0);
        this.description = room.readString("");
        this.tradingMode = room.readInt(0);

        room.readInt(0);
        room.readInt(0);

        this.category = room.readInt(0);

        int tagCount = room.readInt(0);
        for (int i = 0; i < tagCount && i < 64; i++) {
            room.readString("");
        }

        final int THUMBNAIL_BITMASK = 1;
        final int GROUPDATA_BITMASK = 2;
        final int ROOMAD_BITMASK = 4;
        final int ALLOWPETS_BITMASK = 16;

        int multiUse = room.readInt(0);

        if ((multiUse & THUMBNAIL_BITMASK) > 0) {
            room.readString("");
        }
        if ((multiUse & GROUPDATA_BITMASK) > 0) {
            room.readInt(0);
            room.readString("");
            room.readString("");
        }
        if ((multiUse & ROOMAD_BITMASK) > 0) {
            room.readString("");
            room.readString("");
            room.readInt(0);
        }

        this.allowPets = (multiUse & ALLOWPETS_BITMASK) > 0;

        room.readInt(0);

        this.muteRights = room.readInt(0);
        this.kickRights = room.readInt(0);
        this.banRights = room.readInt(0);

        room.readBoolean(false);

        this.freeflow = room.readInt(0);
        this.chatSize = room.readInt(0);
        this.scrollMethod = room.readInt(0);
        this.chatDistance = room.readInt(50);
        this.floodMode = room.readInt(0);

        if (visualizationPacket != null) {
            visualizationPacket.resetReadIndex();
        }
        SafeReader visualization = new SafeReader(visualizationPacket);
        this.wallsHidden = visualization.readBoolean(false);
        this.wallThickness = visualization.readInt(0);
        this.floorThickness = visualization.readInt(0);

        this.missingFields = room.failures + visualization.failures;
    }

    public RoomSettingsSnapshot(JSONObject json) {
        this.id = json.optInt("id", 0);
        this.name = json.optString("name", "Unknown");
        this.description = json.optString("description", "");
        this.ownerName = json.optString("ownerName", "");
        this.ownerId = json.optInt("ownerId", 0);
        this.lockType = json.optInt("lockType", 0);
        this.maxUsers = json.optInt("maxUsers", 25);
        this.tradingMode = json.optInt("tradingMode", 0);
        this.category = json.optInt("category", 0);
        this.freeflow = json.optInt("freeflow", 0);
        this.chatSize = json.optInt("chatSize", 0);
        this.scrollMethod = json.optInt("scrollMethod", 0);
        this.chatDistance = json.optInt("chatDistance", 50);
        this.floodMode = json.optInt("floodMode", 0);
        this.muteRights = json.optInt("muteRights", 0);
        this.kickRights = json.optInt("kickRights", 0);
        this.banRights = json.optInt("banRights", 0);
        this.wallThickness = json.optInt("wallThickness", 0);
        this.floorThickness = json.optInt("floorThickness", 0);
        this.wallsHidden = json.optBoolean("wallsHidden", false);
        this.allowPets = json.optBoolean("allowPets", false);
        this.missingFields = 0;
    }

    public static RoomSettingsSnapshot defaults(String name) {
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("maxUsers", 25);
        json.put("chatDistance", 50);
        return new RoomSettingsSnapshot(json);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        json.put("description", description);
        json.put("ownerName", ownerName);
        json.put("ownerId", ownerId);
        json.put("lockType", lockType);
        json.put("maxUsers", maxUsers);
        json.put("tradingMode", tradingMode);
        json.put("category", category);
        json.put("freeflow", freeflow);
        json.put("chatSize", chatSize);
        json.put("scrollMethod", scrollMethod);
        json.put("chatDistance", chatDistance);
        json.put("floodMode", floodMode);
        json.put("muteRights", muteRights);
        json.put("kickRights", kickRights);
        json.put("banRights", banRights);
        json.put("wallThickness", wallThickness);
        json.put("floorThickness", floorThickness);
        json.put("wallsHidden", wallsHidden);
        json.put("allowPets", allowPets);
        return json;
    }

    public boolean isPasswordLocked() {
        return lockType == 2;
    }

    public boolean applyTo(Executor executor, int targetRoomId, String password, int lockTypeOverride,
                           String nameOverride) {
        return applyTo(executor, targetRoomId, password, lockTypeOverride, nameOverride, false);
    }

    public boolean applyTo(Executor executor, int targetRoomId, String password, int lockTypeOverride,
                           String nameOverride, boolean rawStrings) {
        int floodSensitivity = (floodMode >= 0 && floodMode <= 2) ? floodMode : 1;
        String effectiveName = nameOverride == null ? name : nameOverride;

        Object[] args = {
                targetRoomId, effectiveName, description, lockTypeOverride, password == null ? "" : password,
                maxUsers, category, 0, tradingMode,
                allowPets, false, true, wallsHidden,
                wallThickness, floorThickness,
                muteRights, kickRights, banRights,
                floodSensitivity,
                true, true, 300, true, 900, false };
        return rawStrings
                ? executor.sendToServerRaw("SaveRoomSettings", args)
                : executor.sendToServer("SaveRoomSettings", args);
    }
}

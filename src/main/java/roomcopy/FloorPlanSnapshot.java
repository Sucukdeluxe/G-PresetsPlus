package roomcopy;

import gearth.protocol.HPacket;
import org.json.JSONObject;

public class FloorPlanSnapshot {

    public final String floorPlan;
    public final int doorX;
    public final int doorY;
    public final int doorDir;
    public final int wallHeight;

    public FloorPlanSnapshot(HPacket floorPlanPacket, HPacket doorPacket) {
        this.wallHeight = floorPlanPacket.readInteger(7);
        this.floorPlan = floorPlanPacket.readString(11);
        this.doorX = doorPacket.readInteger(6);
        this.doorY = doorPacket.readInteger(10);
        this.doorDir = doorPacket.readInteger(14);

        if (this.floorPlan == null || this.floorPlan.isEmpty()) {
            throw new IllegalStateException("floorplan packet has an unexpected layout");
        }
    }

    public FloorPlanSnapshot(JSONObject json) {
        this.floorPlan = json.getString("floorPlan");
        this.doorX = json.getInt("doorX");
        this.doorY = json.getInt("doorY");
        this.doorDir = json.getInt("doorDir");
        this.wallHeight = json.optInt("wallHeight", -1);
    }

    public static final int PRESET_ORIGIN = 1;

    public static final int MAX_PLAN_TILES = 3025;

    public static final int MAX_PLAN_SIDE = 64;

    public static FloorPlanSnapshot forPreset(int contentWidth, int contentHeight) {
        return forPreset(contentWidth, contentHeight, PRESET_ORIGIN);
    }

    public static FloorPlanSnapshot forPreset(int contentWidth, int contentHeight, int shift) {
        int width = Math.max(1, contentWidth) + shift;
        int height = Math.max(1, contentHeight) + shift;

        StringBuilder plan = new StringBuilder();
        for (int y = 0; y < height; y++) {
            if (y > 0) plan.append('\r');
            for (int x = 0; x < width; x++) {
                plan.append(y < PRESET_ORIGIN || x < PRESET_ORIGIN ? 'x' : '0');
            }
        }

        JSONObject json = new JSONObject();
        json.put("floorPlan", plan.toString());
        json.put("doorX", PRESET_ORIGIN);
        json.put("doorY", PRESET_ORIGIN);
        json.put("doorDir", 2);
        json.put("wallHeight", 0);
        return new FloorPlanSnapshot(json);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("floorPlan", floorPlan);
        json.put("doorX", doorX);
        json.put("doorY", doorY);
        json.put("doorDir", doorDir);
        json.put("wallHeight", wallHeight);
        return json;
    }

    public int writableWallHeight() {
        return wallHeight < 0 ? 0 : wallHeight;
    }

    public String[] rows() {
        return floorPlan.split("\r");
    }

    public int width() {
        int max = 0;
        for (String row : rows()) {
            max = Math.max(max, row.length());
        }
        return max;
    }

    public int height() {
        return rows().length;
    }

    public int usableTiles() {
        int count = 0;
        for (String row : rows()) {
            for (int i = 0; i < row.length(); i++) {
                if (row.charAt(i) != 'x' && row.charAt(i) != 'X') {
                    count++;
                }
            }
        }
        return count;
    }

    public boolean isWalkable(int x, int y) {
        String[] rows = rows();
        if (y < 0 || y >= rows.length) return false;
        String row = rows[y];
        if (x < 0 || x >= row.length()) return false;
        char c = row.charAt(x);
        return c != 'x' && c != 'X';
    }

    public int[] findFlatSquare(int size, java.util.Set<Long> blocked) {
        String[] rows = rows();
        int wanted = Math.max(1, size);

        for (int y = 0; y + wanted <= rows.length; y++) {
            for (int x = 0; x + wanted <= rows[y].length(); x++) {
                if (!isWalkable(x, y)) continue;
                char height = rows[y].charAt(x);
                boolean possible = true;
                for (int dy = 0; dy < wanted && possible; dy++) {
                    for (int dx = 0; dx < wanted; dx++) {
                        int tx = x + dx;
                        int ty = y + dy;
                        if (!isWalkable(tx, ty) || rows[ty].charAt(tx) != height
                                || (blocked != null && blocked.contains(tileKey(tx, ty)))) {
                            possible = false;
                            break;
                        }
                    }
                }
                if (possible) {
                    return new int[]{x, y};
                }
            }
        }
        return null;
    }

    public static Long tileKey(int x, int y) {
        return ((long) x << 32) | (y & 0xffffffffL);
    }

    public boolean applyTo(Executor executor, int wallThickness, int floorThickness) {
        return executor.sendToServer("UpdateFloorProperties",
                floorPlan, doorX, doorY, doorDir, wallThickness, floorThickness, writableWallHeight());
    }
}

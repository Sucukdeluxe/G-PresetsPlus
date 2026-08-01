package roomcopy;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FloorPlanText {

    public enum DoorSource {
        ISOLATED_TILE,
        DEAD_END,
        FIRST_WALKABLE
    }

    public final String plan;
    public final int width;
    public final int height;
    public final int walkableTiles;
    public final int doorX;
    public final int doorY;
    public final int doorDir;
    public final DoorSource doorSource;
    public final int firstRowWalkable;
    public final int firstColumnWalkable;

    private FloorPlanText(String plan, int width, int height, int walkableTiles,
                          int doorX, int doorY, int doorDir, DoorSource doorSource,
                          int firstRowWalkable, int firstColumnWalkable) {
        this.plan = plan;
        this.width = width;
        this.height = height;
        this.walkableTiles = walkableTiles;
        this.doorX = doorX;
        this.doorY = doorY;
        this.doorDir = doorDir;
        this.doorSource = doorSource;
        this.firstRowWalkable = firstRowWalkable;
        this.firstColumnWalkable = firstColumnWalkable;
    }

    public boolean violatesDoorRule() {
        return firstRowWalkable > 1 || firstColumnWalkable > 1;
    }

    public JSONObject toFloorPlanJson() {
        JSONObject json = new JSONObject();
        json.put("floorPlan", plan);
        json.put("doorX", doorX);
        json.put("doorY", doorY);
        json.put("doorDir", doorDir);
        json.put("wallHeight", 0);
        return json;
    }

    private static boolean blocked(char c) {
        return c == 'x' || c == 'X';
    }

    public static FloorPlanText parse(String raw) {
        if (raw == null) {
            return null;
        }

        List<String> rows = new ArrayList<>();
        for (String line : raw.replace("\r\n", "\n").replace('\r', '\n').split("\n")) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                rows.add(trimmed);
            }
        }
        if (rows.isEmpty()) {
            return null;
        }

        int width = 0;
        for (String row : rows) {
            width = Math.max(width, row.length());
        }
        if (width < 2 || rows.size() < 2) {
            return null;
        }

        List<String> padded = new ArrayList<>();
        int walkable = 0;
        for (String row : rows) {
            StringBuilder sb = new StringBuilder(row);
            while (sb.length() < width) {
                sb.append('x');
            }
            String full = sb.toString();
            for (int i = 0; i < full.length(); i++) {
                if (!blocked(full.charAt(i))) {
                    walkable++;
                }
            }
            padded.add(full);
        }
        if (walkable == 0) {
            return null;
        }

        int height = padded.size();
        int firstRow = 0;
        for (int x = 0; x < width; x++) {
            if (!blocked(padded.get(0).charAt(x))) {
                firstRow++;
            }
        }
        int firstColumn = 0;
        for (int y = 0; y < height; y++) {
            if (!blocked(padded.get(y).charAt(0))) {
                firstColumn++;
            }
        }

        List<int[]> isolated = new ArrayList<>();
        List<int[]> deadEnds = new ArrayList<>();
        int[] firstWalkable = null;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (blocked(padded.get(y).charAt(x))) {
                    continue;
                }
                if (firstWalkable == null) {
                    firstWalkable = new int[] { x, y };
                }
                int neighbours = 0;
                if (walkableAt(padded, x + 1, y, width, height)) neighbours++;
                if (walkableAt(padded, x - 1, y, width, height)) neighbours++;
                if (walkableAt(padded, x, y + 1, width, height)) neighbours++;
                if (walkableAt(padded, x, y - 1, width, height)) neighbours++;
                if (neighbours == 0) {
                    isolated.add(new int[] { x, y });
                } else if (neighbours == 1) {
                    deadEnds.add(new int[] { x, y });
                }
            }
        }

        int[] door;
        DoorSource source;
        if (isolated.size() == 1) {
            door = isolated.get(0);
            source = DoorSource.ISOLATED_TILE;
        } else if (isolated.isEmpty() && deadEnds.size() == 1) {
            door = deadEnds.get(0);
            source = DoorSource.DEAD_END;
        } else {
            door = firstWalkable;
            source = DoorSource.FIRST_WALKABLE;
        }

        return new FloorPlanText(String.join("\r", padded), width, height, walkable,
                door[0], door[1], 2, source, firstRow, firstColumn);
    }

    private static boolean walkableAt(List<String> rows, int x, int y, int width, int height) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return false;
        }
        return !blocked(rows.get(y).charAt(x));
    }
}

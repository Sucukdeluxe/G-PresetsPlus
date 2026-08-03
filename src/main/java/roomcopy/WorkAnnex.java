package roomcopy;

import utils.Messages;
import gearth.extensions.parsers.HPoint;

import java.util.HashSet;
import java.util.Set;

public class WorkAnnex {

    private final String plan;
    private final int doorX;
    private final int doorY;
    private final int doorDir;
    private final HPoint stackTileSpot;
    private final HPoint reservedSpot;
    private HPoint mediumStackSpot;
    private HPoint smallStackSpot;
    private final Set<Long> annexTiles = new HashSet<>();
    private final int width;
    private final int height;

    private WorkAnnex(String plan, int doorX, int doorY, int doorDir,
                      HPoint stackTileSpot, HPoint reservedSpot,
                      Set<Long> annexTiles, int width, int height) {
        this.plan = plan;
        this.doorX = doorX;
        this.doorY = doorY;
        this.doorDir = doorDir;
        this.stackTileSpot = stackTileSpot;
        this.reservedSpot = reservedSpot;
        this.annexTiles.addAll(annexTiles);
        this.width = width;
        this.height = height;
    }

    public String getPlan() {
        return plan;
    }

    public int getDoorX() {
        return doorX;
    }

    public int getDoorY() {
        return doorY;
    }

    public int getDoorDir() {
        return doorDir;
    }

    public HPoint getStackTileSpot() {
        return stackTileSpot;
    }

    public HPoint getReservedSpot() {
        return reservedSpot;
    }

    public HPoint getMediumStackSpot() {
        return mediumStackSpot;
    }

    public HPoint getSmallStackSpot() {
        return smallStackSpot;
    }

    public Set<Long> getAnnexTiles() {
        return annexTiles;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getSide() {
        return annexSide;
    }

    public int getOriginX() {
        return annexOriginX;
    }

    public int getOriginY() {
        return annexOriginY;
    }

    public String describe(int stackTileDimension) {
        return Messages.get("annex.describe.full",
                annexSide, annexSide, annexOriginX, annexOriginY,
                doorX, doorY,
                stackTileDimension, stackTileDimension, stackTileSpot.getX(), stackTileSpot.getY(),
                reservedSpot.getX(), reservedSpot.getY(),
                width, height);
    }

    private int annexSide;
    private int annexOriginX;
    private int annexOriginY;

    private static boolean planFits(int width, int height) {
        return width <= FloorPlanSnapshot.MAX_PLAN_SIDE
                && height <= FloorPlanSnapshot.MAX_PLAN_SIDE
                && width * height <= FloorPlanSnapshot.MAX_PLAN_TILES;
    }

    public static WorkAnnex build(FloorPlanSnapshot source, int stackTileDimension) {
        String[] sourceRows = source.rows();
        int oldWidth = 0;
        for (String row : sourceRows) {
            oldWidth = Math.max(oldWidth, row.length());
        }
        int oldHeight = sourceRows.length;

        int maxWalkableX = -1;
        int anchorY = -1;
        char lowestHeight = 'z';
        for (int y = 0; y < oldHeight; y++) {
            for (int x = 0; x < sourceRows[y].length(); x++) {
                if (!source.isWalkable(x, y)) continue;
                char c = sourceRows[y].charAt(x);
                if (c < lowestHeight) lowestHeight = c;
                if (x > maxWalkableX) {
                    maxWalkableX = x;
                    anchorY = y;
                }
            }
        }
        if (maxWalkableX < 0) {
            return null;
        }
        if (lowestHeight == 'z') {
            lowestHeight = '0';
        }

        int maxWalkableY = -1;
        int anchorX = -1;
        for (int y = 0; y < oldHeight; y++) {
            for (int x = 0; x < sourceRows[y].length(); x++) {
                if (!source.isWalkable(x, y)) continue;
                if (y > maxWalkableY) {
                    maxWalkableY = y;
                    anchorX = x;
                }
            }
        }

        int side = Math.max(4, Math.max(1, stackTileDimension) + 2);

        int annexX = maxWalkableX + 1;
        int annexY = anchorY;
        int newWidth = Math.max(oldWidth, annexX + side);
        int newHeight = Math.max(oldHeight, annexY + side);

        if (!planFits(newWidth, newHeight)) {
            annexY = maxWalkableY + 1;
            annexX = Math.max(1, Math.min(anchorX, oldWidth - side));
            newWidth = Math.max(oldWidth, annexX + side);
            newHeight = Math.max(oldHeight, annexY + side);
            if (!planFits(newWidth, newHeight)) {
                return null;
            }
        }

        char[][] grid = new char[newHeight][newWidth];
        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                grid[y][x] = 'x';
            }
        }
        for (int y = 0; y < oldHeight; y++) {
            for (int x = 0; x < sourceRows[y].length(); x++) {
                grid[y][x] = sourceRows[y].charAt(x);
            }
        }

        Set<Long> tiles = new HashSet<>();
        for (int dy = 0; dy < side; dy++) {
            for (int dx = 0; dx < side; dx++) {
                int tx = annexX + dx;
                int ty = annexY + dy;
                grid[ty][tx] = lowestHeight;
                tiles.add(FloorPlanSnapshot.tileKey(tx, ty));
            }
        }

        StringBuilder planBuilder = new StringBuilder();
        for (int y = 0; y < newHeight; y++) {
            if (y > 0) planBuilder.append('\r');
            planBuilder.append(new String(grid[y]));
        }

        HPoint stackSpot = new HPoint(annexX, annexY + 1);
        HPoint reserved = new HPoint(annexX + 1, annexY);

        WorkAnnex annex = new WorkAnnex(planBuilder.toString(),
                annexX, annexY, source.doorDir,
                stackSpot, reserved, tiles, newWidth, newHeight);
        annex.mediumStackSpot = new HPoint(annexX + Math.max(1, stackTileDimension), annexY + 1);
        annex.smallStackSpot = new HPoint(annexX + Math.max(1, stackTileDimension), annexY + 3);
        annex.annexSide = side;
        annex.annexOriginX = annexX;
        annex.annexOriginY = annexY;
        return annex;
    }

    public boolean applyTo(Executor executor, int wallThickness, int floorThickness, int wallHeight) {
        return executor.sendToServer("UpdateFloorProperties",
                plan, doorX, doorY, doorDir, wallThickness, floorThickness, wallHeight < 0 ? 0 : wallHeight);
    }
}

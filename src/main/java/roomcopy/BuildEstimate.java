package roomcopy;

public class BuildEstimate {

    private static final int DROP_BC_MS = 230;
    private static final int DROP_INVENTORY_MS = 150;
    private static final int MOVE_OBJECT_MS = 60;
    private static final int MOVE_STACK_TILE_MS = 60;
    private static final int WIRED_SAVE_MS = 300;

    public final int furniCount;
    public final int wiredCount;
    public final long dropMs;
    public final long moveMs;
    public final long wiredMs;
    public final long totalMs;

    private BuildEstimate(int furniCount, int wiredCount, long dropMs, long moveMs, long wiredMs) {
        this.furniCount = furniCount;
        this.wiredCount = wiredCount;
        this.dropMs = dropMs;
        this.moveMs = moveMs;
        this.wiredMs = wiredMs;
        this.totalMs = dropMs + moveMs + wiredMs;
    }

    public static BuildEstimate of(int furniCount, int wiredCount, boolean fromBuildersClub, int rateLimitMs) {
        int rate = Math.max(0, rateLimitMs);
        long drop = (long) furniCount * ((fromBuildersClub ? DROP_BC_MS : DROP_INVENTORY_MS) + rate);
        long move = (long) furniCount * (MOVE_STACK_TILE_MS + MOVE_OBJECT_MS + 2L * rate);
        long wired = (long) wiredCount * (WIRED_SAVE_MS + rate);
        return new BuildEstimate(furniCount, wiredCount, drop, move, wired);
    }

    public static String format(long millis) {
        long seconds = Math.max(0, millis) / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long rest = seconds % 60;

        if (hours > 0) {
            return String.format("%d h %02d min", hours, minutes);
        }
        if (minutes > 0) {
            return String.format("%d min %02d s", minutes, rest);
        }
        return String.format("%d s", rest);
    }
}

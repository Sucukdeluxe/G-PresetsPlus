package extension.tools.importutils;

import extension.tools.presetconfig.furni.PresetFurni;
import furnidata.FurniDataTools;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FlatFurniOrder {

    public static class Conflict {
        public final int x;
        public final int y;
        public final String below;
        public final String above;

        Conflict(int x, int y, String below, String above) {
            this.x = x;
            this.y = y;
            this.below = below;
            this.above = above;
        }
    }

    public static void sort(List<PresetFurni> flatFurni, FurniDataTools furniData) {
        flatFurni.sort(Comparator
                .comparingDouble((PresetFurni f) -> f.getLocation().getZ())
                .thenComparingInt(f -> furniData.canSupportFurniOnTop(f.getClassName()) ? 0 : 1));
    }

    public static List<Conflict> conflicts(List<PresetFurni> flatFurni, FurniDataTools furniData) {
        Map<String, List<PresetFurni>> byTile = new LinkedHashMap<>();
        for (PresetFurni furni : flatFurni) {
            String tile = furni.getLocation().getX() + "|" + furni.getLocation().getY();
            byTile.computeIfAbsent(tile, key -> new ArrayList<>()).add(furni);
        }

        List<Conflict> conflicts = new ArrayList<>();
        for (List<PresetFurni> onTile : byTile.values()) {
            for (int i = 0; i + 1 < onTile.size(); i++) {
                PresetFurni below = onTile.get(i);
                if (!furniData.canSupportFurniOnTop(below.getClassName())) {
                    conflicts.add(new Conflict(below.getLocation().getX(), below.getLocation().getY(),
                            below.getClassName(), onTile.get(i + 1).getClassName()));
                }
            }
        }
        return conflicts;
    }
}

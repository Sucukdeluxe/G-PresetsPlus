package roomcopy;

import gearth.protocol.HMessage;
import gearth.protocol.HPacket;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FlatCategories {

    public static class Category {
        public final int id;
        public final String name;
        public final String serverName;
        public final boolean selectable;

        Category(int id, String serverName, boolean selectable) {
            this.id = id;
            this.serverName = serverName;
            String clientName = CLIENT_NAMES.get(id);
            this.name = clientName == null ? serverName : clientName;
            this.selectable = selectable;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static final Map<Integer, Category> byId = new LinkedHashMap<>();
    private static final Map<Integer, String> CLIENT_NAMES = new LinkedHashMap<>();

    static {
        CLIENT_NAMES.put(10, "Personal Space");
        CLIENT_NAMES.put(3, "Habbo Games");
        CLIENT_NAMES.put(14, "Trading");
        CLIENT_NAMES.put(16, "Agencies");
        CLIENT_NAMES.put(17, "Role Playing");
        CLIENT_NAMES.put(12, "Chat and discussion");
        CLIENT_NAMES.put(11, "Building and decoration");
        CLIENT_NAMES.put(2, "Party");
        CLIENT_NAMES.put(5, "Fansite Square");
        CLIENT_NAMES.put(6, "Help Centers");
    }

    public static synchronized List<Category> selectable() {
        List<Category> out = new ArrayList<>();
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (Category category : byId.values()) {
            if (!category.selectable) {
                continue;
            }
            if (!seen.add(category.serverName.toLowerCase(java.util.Locale.ROOT))) {
                continue;
            }
            out.add(category);
        }
        return out;
    }

    public static synchronized Category get(int id) {
        return byId.get(id);
    }

    public static synchronized boolean isEmpty() {
        return byId.isEmpty();
    }

    public static synchronized void clear() {
        byId.clear();
    }

    public static boolean request(Executor executor) {
        if (!executor.isKnownName(HMessage.Direction.TOSERVER, "GetUserFlatCats")) {
            return false;
        }

        Executor.AwaitingPacket answer =
                new Executor.AwaitingPacket("UserFlatCats", HMessage.Direction.TOCLIENT, 6000);
        executor.register(answer);

        if (!executor.sendToServer("GetUserFlatCats")) {
            return false;
        }

        HPacket response = executor.awaitPacket(answer);
        if (response == null) {
            return false;
        }
        return parse(response);
    }

    public static synchronized boolean parse(HPacket packet) {
        packet.resetReadIndex();
        Map<Integer, Category> parsed = new LinkedHashMap<>();
        try {
            int count = packet.readInteger();
            if (count < 0 || count > 200) {
                return false;
            }
            for (int i = 0; i < count; i++) {
                int id = packet.readInteger();
                String name = packet.readString(StandardCharsets.UTF_8);
                boolean selectable = packet.readBoolean();
                packet.readBoolean();
                packet.readString(StandardCharsets.UTF_8);
                packet.readString(StandardCharsets.UTF_8);
                packet.readBoolean();
                parsed.put(id, new Category(id, name, selectable));
            }
        } catch (Throwable t) {
            if (parsed.isEmpty()) {
                return false;
            }
        }
        byId.clear();
        byId.putAll(parsed);
        return !byId.isEmpty();
    }
}

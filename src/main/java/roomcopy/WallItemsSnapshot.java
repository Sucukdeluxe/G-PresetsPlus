package roomcopy;

import utils.Messages;
import extension.logger.Logger;
import furnidata.FurniDataTools;
import game.Inventory;
import gearth.extensions.parsers.HInventoryItem;
import gearth.extensions.parsers.HWallItem;
import gearth.protocol.HMessage;
import gearth.protocol.HPacket;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.Utils;

import java.util.*;
import java.util.function.BooleanSupplier;

public class WallItemsSnapshot {

    public static class WallItem {
        public final String className;
        public final String position;
        public final String state;

        WallItem(String className, String position, String state) {
            this.className = className;
            this.position = position;
            this.state = state;
        }

        JSONObject toJson() {
            JSONObject json = new JSONObject();
            json.put("className", className == null ? JSONObject.NULL : className);
            json.put("position", position);
            json.put("state", state == null ? JSONObject.NULL : state);
            return json;
        }

        static WallItem fromJson(JSONObject json) {
            return new WallItem(
                    json.isNull("className") ? null : json.getString("className"),
                    json.getString("position"),
                    json.isNull("state") ? null : json.getString("state"));
        }
    }

    private final List<WallItem> wallItems = new ArrayList<>();

    public WallItemsSnapshot(HPacket itemsPacket, FurniDataTools furniData) {
        itemsPacket.resetReadIndex();
        for (HWallItem item : HWallItem.parse(itemsPacket)) {
            String className = furniData == null ? null : furniData.getWallItemName(item.getTypeId());
            wallItems.add(new WallItem(className, item.getLocation(), item.getState()));
        }
    }

    public WallItemsSnapshot(JSONArray json) {
        for (Object o : json) {
            wallItems.add(WallItem.fromJson((JSONObject) o));
        }
    }

    public JSONArray toJson() {
        JSONArray json = new JSONArray();
        wallItems.forEach(item -> json.put(item.toJson()));
        return json;
    }

    public int size() {
        return wallItems.size();
    }

    public List<WallItem> getWallItems() {
        return Collections.unmodifiableList(wallItems);
    }

    public int applyTo(Executor executor, Inventory inventory, FurniDataTools furniData,
                       Logger logger, BooleanSupplier keepRunning) {
        Map<Integer, LinkedList<HInventoryItem>> byType = new HashMap<>();
        int placed = 0;
        int skipped = 0;

        for (WallItem item : wallItems) {
            if (!keepRunning.getAsBoolean()) {
                logger.logKey("wallitems.aborted", "orange");
                return placed;
            }
            if (item.className == null) {
                skipped++;
                continue;
            }
            Integer typeId = furniData.getWallTypeId(item.className);
            if (typeId == null) {
                logger.logKey("wallitems.not_in_furnidata", "orange", item.className);
                skipped++;
                continue;
            }
            if (!byType.containsKey(typeId)) {
                byType.put(typeId, new LinkedList<>(inventory.getWallItemsByType(typeId)));
            }
            HInventoryItem inventoryItem = byType.get(typeId).pollFirst();
            if (inventoryItem == null) {
                logger.logKey("wallitems.not_in_inventory", "orange", item.className);
                skipped++;
                continue;
            }

            HPacket added = placeWallItem(executor, inventoryItem.getId(), item.position);
            if (added != null) {
                placed++;
                lastKnownStates.put(inventoryItem.getId(), readState(added));
                applyState(executor, inventoryItem.getId(), item.state);
            } else {
                logger.logKey("wallitems.place_failed", "orange", item.className);
                skipped++;
            }
        }

        if (skipped > 0) {
            logger.logKey("wallitems.summary_with_skipped", "orange", placed, skipped);
        } else {
            logger.logKey("wallitems.summary", "green", placed);
        }
        return placed;
    }

    private HPacket placeWallItem(Executor executor, int itemId, String position) {
        String payload = String.format("%d %s", itemId, position);
        for (int tries = 0; tries < 10; tries++) {
            if (!executor.sendToServer("PlaceObject", payload)) {
                return null;
            }
            HPacket response = executor.awaitPacket(
                    new Executor.AwaitingPacket("ItemAdd", HMessage.Direction.TOCLIENT, 400)
                            .addConditions(packet -> Integer.parseInt(packet.readString()) == itemId));
            if (response != null) {
                return response;
            }
            Utils.sleep(60);
        }
        return null;
    }

    private void applyState(Executor executor, int itemId, String wantedState) {
        if (wantedState == null || wantedState.isEmpty()) {
            return;
        }
        for (int tries = 0; tries < 12; tries++) {
            String current = lastKnownStates.get(itemId);
            if (wantedState.equals(current)) {
                return;
            }
            if (!executor.sendToServer("UseWallItem", itemId, 0)) {
                return;
            }
            HPacket update = executor.awaitPacket(
                    new Executor.AwaitingPacket("ItemUpdate", HMessage.Direction.TOCLIENT, 300)
                            .addConditions(packet -> Integer.parseInt(packet.readString()) == itemId)
                            .setMinWaitingTime(40));
            if (update == null) {
                return;
            }
            lastKnownStates.put(itemId, readState(update));
        }
    }

    private final Map<Integer, String> lastKnownStates = new HashMap<>();

    private static String readState(HPacket packet) {
        try {
            packet.resetReadIndex();
            return new HWallItem(packet).getState();
        } catch (Throwable t) {
            return null;
        }
    }
}

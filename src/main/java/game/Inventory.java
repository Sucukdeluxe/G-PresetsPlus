package game;

import utils.Messages;
import extension.logger.Logger;
import gearth.extensions.IExtension;
import gearth.extensions.parsers.HInventoryItem;
import gearth.extensions.parsers.HProductType;
import gearth.protocol.HMessage;
import gearth.protocol.HPacket;
import utils.Callback;

import java.util.*;

public class Inventory {

    public enum InventoryState {
        UNAVAILABLE,
        LOADING,
        LOADED
    }

    private final Callback onInventoryStateChange;
    private final IExtension extension;
    private final Logger logger;

    private List<HInventoryItem> buffer = null;

    private Map<Integer, HInventoryItem> itemPlacements = new HashMap<>();
    private Map<Integer, Map<Integer, HInventoryItem>> wallItemsByType = new HashMap<>();
    private Map<Integer, Map<Integer, HInventoryItem>> floorItemsByType = new HashMap<>();

    private static final int NO_ANSWER_WARN_MS = 8000;
    private static final long BLOCK_MAX_MS = 30000;

    private InventoryState state = InventoryState.UNAVAILABLE;
    private volatile boolean virtualRequest = false;
    private volatile long lastRequestAt = 0;
    private volatile long loadedAt = 0;

    private static int chunkLogStep(int total) {
        if (total <= 20) return 5;
        if (total <= 100) return 10;
        return 25;
    }

    public Inventory(IExtension extension, Logger logger, Callback onInventoryStateChange) {
        this.extension = extension;
        this.logger = logger;
        this.onInventoryStateChange = onInventoryStateChange;

        extension.intercept(HMessage.Direction.TOCLIENT, "FurniList", this::loadItems);
        extension.intercept(HMessage.Direction.TOCLIENT, "FurniListAddOrUpdate", (m) -> {
            HPacket packet = m.getPacket();
            int count = packet.readInteger();
            for (int i = 0; i < count; i++) {
                HInventoryItem item = new HInventoryItem(m.getPacket());
                updateOrAddItem(item);
            }
        });
        extension.intercept(HMessage.Direction.TOCLIENT, "FurniListRemove", (m) ->
                removeItem(m.getPacket().readInteger()));
        extension.intercept(HMessage.Direction.TOCLIENT, "FurniListInvalidate", (m) -> {
            logger.log(Messages.get("inventory.invalidated"), "orange");
            virtualRequest = false;
        });
    }

    public InventoryState getState() {
        return state;
    }

    private void loadItems(HMessage hMessage) {
        if (virtualRequest && System.currentTimeMillis() - lastRequestAt > BLOCK_MAX_MS) {
            virtualRequest = false;
            logger.log(Messages.get("inventory.block_released"), "orange");
        }
        if (virtualRequest) {
            hMessage.setBlocked(true);
        }
        
        boolean stateChanged = false;

        HPacket inventoryLoadPacket = hMessage.getPacket();
        int total = inventoryLoadPacket.readInteger();
        int i = inventoryLoadPacket.readInteger();

        if (i == 0) {
            clear();
            logger.log(itemPlacements.isEmpty()
                    ? Messages.get("inventory.loading")
                    : Messages.get("inventory.updating"), "blue");
            buffer = new ArrayList<>();
            stateChanged = true;
            state = InventoryState.LOADING;
        }

        if (buffer == null) {
            buffer = new ArrayList<>();
            state = InventoryState.LOADING;
            stateChanged = true;
        }

        inventoryLoadPacket.resetReadIndex();
        HInventoryItem[] items = HInventoryItem.parse(inventoryLoadPacket);
        buffer.addAll(Arrays.asList(items));

        if (total > 1 && (i + 1) % chunkLogStep(total) == 0 && i != total - 1) {
            logger.log(Messages.get("inventory.progress", i + 1, total, buffer.size()), "blue");
        }

        boolean inventoryComplete = i == total - 1;
        if (inventoryComplete) {
            buffer.forEach(this::updateOrAddItem);
            logger.log(Messages.get("inventory.loaded", itemPlacements.size()), "blue");
            buffer = null;
            stateChanged = true;
            virtualRequest = false;
            state = InventoryState.LOADED;
            loadedAt = System.currentTimeMillis();
        }

        if (stateChanged) {
            onInventoryStateChange.call();
        }
    }

    private void updateOrAddItem(HInventoryItem item) {
        if (state != InventoryState.UNAVAILABLE) {
            itemPlacements.put(item.getPlacementId(), item);
            Map<Integer, Map<Integer, HInventoryItem>> map = item.getType() == HProductType.FloorItem ? floorItemsByType : wallItemsByType;
            if (!map.containsKey(item.getTypeId()))
                map.put(item.getTypeId(), new HashMap<>());
            map.get(item.getTypeId()).put(item.getId(), item);
        }
    }

    private void removeItem(int id) {
        if (state == InventoryState.LOADED) {
            HInventoryItem item = itemPlacements.remove(id);
            if (item != null) {
                if (item.getType() == HProductType.FloorItem) {
                    if (floorItemsByType.containsKey(item.getTypeId()))
                        floorItemsByType.get(item.getTypeId()).remove(item.getId());
                }
                else {
                    if (wallItemsByType.containsKey(item.getTypeId()))
                        wallItemsByType.get(item.getTypeId()).remove(item.getId());
                }
            }
        }
    }

    public List<HInventoryItem> getInventoryItems() {
        return new ArrayList<>(itemPlacements.values());
    }
    public List<HInventoryItem> getFloorItemsByType(int typeId) {
        Map<Integer, HInventoryItem> items = floorItemsByType.get(typeId);
        if (items == null)
            return Collections.emptyList();
        return new ArrayList<>(items.values());
    }

    public List<HInventoryItem> getWallItemsByType(int typeId) {
        Map<Integer, HInventoryItem> items = wallItemsByType.get(typeId);
        if (items == null)
            return Collections.emptyList();
        return new ArrayList<>(items.values());
    }

    public void clear() {
        virtualRequest = false;
        buffer = null;
        itemPlacements.clear();
        floorItemsByType.clear();
        wallItemsByType.clear();

        state = InventoryState.UNAVAILABLE;
        onInventoryStateChange.call();
    }

    public void requestInventory() {
        virtualRequest = true;
        logger.log(Messages.get("inventory.requested"), "blue");
        onInventoryStateChange.call();

        long requestedAt = System.currentTimeMillis();
        lastRequestAt = requestedAt;
        extension.sendToServer(new HPacket("RequestFurniInventory", HMessage.Direction.TOSERVER));

        Thread watchdog = new Thread(() -> {
            try {
                Thread.sleep(NO_ANSWER_WARN_MS);
            } catch (InterruptedException e) {
                return;
            }
            if (lastRequestAt != requestedAt) {
                return;
            }
            if (state == InventoryState.LOADING || loadedAt > requestedAt) {
                return;
            }
            virtualRequest = false;
            if (state == InventoryState.LOADED) {
                logger.log(Messages.get("inventory.kept_cached", itemPlacements.size()), "orange");
            } else {
                state = InventoryState.UNAVAILABLE;
                logger.log(Messages.get("inventory.no_answer", NO_ANSWER_WARN_MS / 1000), "red");
                onInventoryStateChange.call();
            }
        }, "inventory-watchdog");
        watchdog.setDaemon(true);
        watchdog.start();
    }
}

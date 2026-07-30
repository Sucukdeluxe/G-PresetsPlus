package roomcopy;

import utils.Messages;
import extension.logger.Logger;
import furnidata.FurniDataTools;
import gearth.protocol.HMessage;
import gearth.protocol.HPacket;

import java.util.*;

public class RoomCapture {

    private static final String[] ROOM_ENTRY_PACKETS = {
            "GetGuestRoomResult", "RoomEntryTile", "FloorHeightMap",
            "Items", "Objects", "RoomVisualizationSettings"
    };

    private final Executor executor;
    private final Logger logger;

    public RoomCapture(Executor executor, Logger logger) {
        this.executor = executor;
        this.logger = logger;
    }

    public Map<String, HPacket> collectRoomPackets() {
        Map<String, HPacket> packets = new HashMap<>();
        for (String name : ROOM_ENTRY_PACKETS) {
            packets.put(name, executor.getCachedRoomPacket(name));
        }
        if (packets.values().stream().allMatch(Objects::nonNull)) {
            return packets;
        }

        Executor.AwaitingPacket[] awaiting = Arrays.stream(ROOM_ENTRY_PACKETS)
                .map(name -> new Executor.AwaitingPacket(name, HMessage.Direction.TOCLIENT, 4000))
                .toArray(Executor.AwaitingPacket[]::new);
        executor.register(awaiting);

        if (executor.sendFirstKnown("GetHeightMap", "GetRoomEntryTile") == null) {
            logger.logKey("capture.no_refresh_request", "orange");
        }
        executor.awaitPacketList(awaiting);

        for (Executor.AwaitingPacket packet : awaiting) {
            if (packet.getPacket() != null) {
                packets.put(packet.headerName, packet.getPacket());
            }
        }
        for (String name : ROOM_ENTRY_PACKETS) {
            if (packets.get(name) == null) {
                packets.put(name, executor.getCachedRoomPacket(name));
            }
        }
        return packets;
    }

    public RoomSnapshot capture(FurniDataTools furniData) {
        Map<String, HPacket> packets = collectRoomPackets();

        List<String> missing = new ArrayList<>();
        for (String name : ROOM_ENTRY_PACKETS) {
            if (packets.get(name) == null) {
                missing.add(name);
            }
        }
        if (packets.get("GetGuestRoomResult") == null) {
            logger.logKey("capture.roomdata.missing", "red");
            return null;
        }

        RoomSettingsSnapshot settings = new RoomSettingsSnapshot(
                packets.get("GetGuestRoomResult"), packets.get("RoomVisualizationSettings"));
        FloorPlanSnapshot floorPlan = null;
        if (packets.get("FloorHeightMap") != null && packets.get("RoomEntryTile") != null) {
            try {
                floorPlan = new FloorPlanSnapshot(packets.get("FloorHeightMap"), packets.get("RoomEntryTile"));
            } catch (Throwable t) {
                logger.logKey("capture.floorplan.read_failed", "red", t);
            }
        }

        WallItemsSnapshot wallItems = null;
        if (packets.get("Items") != null) {
            try {
                wallItems = new WallItemsSnapshot(packets.get("Items"), furniData);
            } catch (Throwable t) {
                logger.logKey("capture.wallitems.read_failed", "orange", t);
            }
        }

        if (!missing.isEmpty()) {
            logger.logKey("capture.not_recorded", "orange", String.join(", ", missing));
        }

        logger.logKey("capture.summary", "green", settings.name, settings.id, floorPlan == null
                        ? Messages.get("capture.floorplan.missing")
                        : Messages.get("capture.floorplan.dimensions",
                                floorPlan.width(), floorPlan.height(), floorPlan.usableTiles()), wallItems == null ? 0 : wallItems.size());

        return new RoomSnapshot(settings, floorPlan, wallItems);
    }
}

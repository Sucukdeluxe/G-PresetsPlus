package roomcopy;

import utils.Messages;
import extension.logger.Logger;
import extension.tools.StackTileSetting;
import extension.tools.postconfig.ItemSource;
import furnidata.FurniDataTools;
import furnidata.details.FloorItemDetails;
import game.FloorState;
import game.Inventory;
import gearth.extensions.parsers.HFloorItem;
import gearth.extensions.parsers.HInventoryItem;
import gearth.extensions.parsers.HPoint;
import gearth.protocol.HMessage;
import gearth.protocol.HPacket;
import utils.Utils;

import java.util.List;

public class StackTileBootstrap {

    public static final int FAILED = -1;
    public static final int ALREADY_PRESENT = 0;

    private final Executor executor;
    private final Logger logger;

    public StackTileBootstrap(Executor executor, Logger logger) {
        this.executor = executor;
        this.logger = logger;
    }

    public int ensureStackTile(StackTileSetting setting, ItemSource itemSource,
                              FloorState floorState, Inventory inventory,
                              FurniDataTools furniData, HPoint location) {
        String className = setting.getClassName();

        List<HFloorItem> existing = floorState.getItemsFromType(furniData, className);
        if (!existing.isEmpty()) {
            logger.logKey("stacktile.already_present", "green");
            return ALREADY_PRESENT;
        }

        Integer typeId = furniData.getFloorTypeId(className);
        if (typeId == null) {
            logger.logKey("stacktile.not_in_furnidata", "red", furniData.displayName(className));
            return FAILED;
        }

        FloorItemDetails details = furniData.getFloorItemDetails(className);
        int bcOfferId = details == null ? -1 : details.bcOfferId;
        List<HInventoryItem> inventoryItems = inventory.getFloorItemsByType(typeId);

        boolean useBc = itemSource == ItemSource.ONLY_BC
                || (itemSource == ItemSource.PREFER_BC && bcOfferId != -1)
                || (itemSource == ItemSource.PREFER_INVENTORY && inventoryItems.isEmpty() && bcOfferId != -1);

        if (!useBc && inventoryItems.isEmpty()) {
            if (bcOfferId != -1 && itemSource != ItemSource.ONLY_INVENTORY) {
                useBc = true;
            } else {
                logger.logKey("stacktile.none_available", "red", furniData.displayName(className));
                return FAILED;
            }
        }

        logger.logKey("stacktile.placing", "blue", furniData.displayName(className), location.getX(), location.getY(), useBc ? Messages.get("stacktile.source.bc") : Messages.get("stacktile.source.inventory"));

        Executor.AwaitingPacket added =
                new Executor.AwaitingPacket("ObjectAdd", HMessage.Direction.TOCLIENT, 5000)
                        .addConditions(packet -> {
                            packet.readInteger();
                            return packet.readInteger() == typeId;
                        });
        executor.register(added);

        boolean sent;
        if (useBc) {
            sent = executor.sendToServer("BuildersClubPlaceRoomItem",
                    -1, bcOfferId, "", location.getX(), location.getY(), 0);
        } else {
            sent = executor.sendToServer("PlaceObject",
                    String.format("-%d %d %d %d", inventoryItems.get(0).getId(),
                            location.getX(), location.getY(), 0));
        }

        if (!sent) {
            logger.logKey("stacktile.send_failed", "red");
            return FAILED;
        }

        HPacket response = executor.awaitPacket(added);
        if (response == null) {
            logger.logKey("stacktile.not_confirmed", "red");
            return FAILED;
        }

        Utils.sleep(250);

        List<HFloorItem> placed = floorState.getItemsFromType(furniData, className);
        if (placed.isEmpty()) {
            logger.logKey("stacktile.missing_in_state", "orange");
            return FAILED;
        }

        int placedId = placed.get(0).getId();
        logger.logKey("stacktile.in_room", "green", placedId);
        return placedId;
    }
}

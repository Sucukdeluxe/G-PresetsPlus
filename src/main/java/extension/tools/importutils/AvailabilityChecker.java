package extension.tools.importutils;

import utils.Messages;
import extension.logger.Logger;
import extension.tools.postconfig.ItemSource;
import furnidata.FurniDataTools;
import furnidata.details.FloorItemDetails;
//import game.BCCatalog;
import game.Inventory;
import gearth.extensions.parsers.HInventoryItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AvailabilityChecker {

    // returns null if invalid resources to check availability
    // return map of counts of missing furniture
    // map is empty if you can start importing
    public static Map<String, Integer> missingItems(List<FurniDropInfo> furniDrops, Inventory inventory, FurniDataTools furniDataTools) {

        if (inventory.getState() == Inventory.InventoryState.LOADED
                && furniDataTools.isReady()) {

            Map<Integer, Integer> usedInventorySpots = new HashMap<>();
            Map<Integer, Integer> missingItemCounts = new HashMap<>();
            for (FurniDropInfo furniDropInfo : furniDrops) {
                int typeId = furniDropInfo.getTypeId();
                usedInventorySpots.putIfAbsent(typeId, 0);
                ItemSource src = furniDropInfo.getItemSource();

                List<HInventoryItem> invItems = inventory.getFloorItemsByType(typeId);
                FloorItemDetails floorItemDetails = furniDataTools.getFloorItemDetails(furniDataTools.getFloorItemName(typeId));
                boolean isMissing = false;
                boolean useInventorySpace = false;

                if (src == ItemSource.ONLY_BC) {
                    if (floorItemDetails.bcOfferId == -1) isMissing = true;
                }
                else if (src == ItemSource.PREFER_BC) {
                    if (floorItemDetails.bcOfferId == -1) {
                        if (usedInventorySpots.get(typeId) < invItems.size()) useInventorySpace = true;
                        else isMissing = true;
                    }
                }
                else if (src == ItemSource.PREFER_INVENTORY) {
                    if (usedInventorySpots.get(typeId) < invItems.size()) useInventorySpace = true;
                    else isMissing = floorItemDetails.bcOfferId == -1;
                }
                else if (src == ItemSource.ONLY_INVENTORY) {
                    if (usedInventorySpots.get(typeId) < invItems.size()) useInventorySpace = true;
                    else isMissing = true;
                }

                if (isMissing) {
                    missingItemCounts.putIfAbsent(typeId, 0);
                    missingItemCounts.put(typeId, missingItemCounts.get(typeId) + 1);
                }
                if (useInventorySpace) {
                    usedInventorySpots.put(typeId, usedInventorySpots.get(typeId) + 1);
                }
            }

            Map<String, Integer> missingItemCountsByName = new HashMap<>();

            for (int typeId : missingItemCounts.keySet()) {
                String className = furniDataTools.getFloorItemName(typeId);
                missingItemCountsByName.put(className, missingItemCounts.get(typeId));
            }

            return missingItemCountsByName;
        }
        else return null;
    }

    public static void printAvailability(Logger logger, List<FurniDropInfo> furniDrops, Inventory inventory, FurniDataTools furniDataTools) {

        Map<String, Integer> missing = missingItems(furniDrops, inventory, furniDataTools);
        if (inventory.getState() == Inventory.InventoryState.LOADED
                && furniDataTools.isReady() && missing != null) {

            List<String> allItems = furniDrops.stream()
                    .map(i -> furniDataTools.getFloorItemName(i.getTypeId()))
                    .distinct()
                    .sorted(java.util.Comparator.comparing(
                            name -> furniDataTools.displayName(name), String.CASE_INSENSITIVE_ORDER))
                    .collect(Collectors.toList());

            logger.logKey("inventory.availability.header", "black");
            for (String className : allItems) {
                boolean isMissing = missing.containsKey(className) && missing.get(className) > 0;
                int totalNeeded = (int)(furniDrops.stream().filter(i -> furniDataTools.getFloorItemName(i.getTypeId()).equals(className)).count());
                int available = isMissing ? totalNeeded - missing.get(className) : totalNeeded;

                logger.logNoNewline(Messages.get("inventory.availability.item.name", furniDataTools.displayName(className)), "black");
                logger.logKey("inventory.availability.item.count", isMissing ? "red" : "green", available, totalNeeded);
            }

        }
        else {
            logger.logKey("inventory.availability.failed", "red");
        }
    }

}

package extension.tools;

import utils.Messages;
import extension.GRoomCloner;
import extension.tools.presetconfig.PresetConfig;
import extension.tools.presetconfig.furni.PresetFurni;
import furnidata.FurniDataTools;
import game.Inventory;
import gearth.protocol.HMessage;
import gearth.protocol.HPacket;
import utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AutoDonator {
    private final GRoomCloner extension;

    public enum AutoDonateState {
        NONE,
        SELF_DONATING,
    }

    private volatile AutoDonateState state = AutoDonateState.NONE;

    public AutoDonator(GRoomCloner extension) {
        this.extension = extension;

        extension.intercept(HMessage.Direction.TOSERVER, "Chat", this::onChat);

        extension.intercept(HMessage.Direction.TOSERVER, "SelfDonateItem", this::onSelfDonateItem);
        extension.intercept(HMessage.Direction.TOCLIENT, "SelfDonationResult", this::onSelfDonationResult);
    }

    private void onChat(HMessage hMessage) {
        String text = hMessage.getPacket().readString();

        if (text.equals(":abort") || text.equals(":a")) {
            hMessage.setBlocked(true);

            if (state != AutoDonateState.NONE) {
                state = AutoDonateState.NONE;
                extension.sendVisualChatInfo(Messages.get("inventory.donate.aborted"));
            }
        }
    }

    private void onSelfDonateItem(HMessage hMessage) {
        if (state == AutoDonateState.SELF_DONATING) {
            extension.sendVisualChatInfo(Messages.get("inventory.donate.wait_before_manual"));
            hMessage.setBlocked(true);
        }
    }

    private void onSelfDonationResult(HMessage hMessage) {
        if (state == AutoDonateState.SELF_DONATING) {
            hMessage.setBlocked(true);
        }
    }

    public boolean isDonating() {
        return this.state == AutoDonateState.SELF_DONATING;
    }

    public void donateAll(boolean missingOnly) {
        if (isDonating()) {
            extension.getLogger().log(Messages.get("inventory.donate.already_running"), "red");
            return;
        }

        PresetConfig preset = this.extension.getImporter().getPresetConfig();
        if (preset == null) {
            extension.getLogger().log(Messages.get("inventory.donate.no_preset"), "red");
            return;
        }

        state = AutoDonateState.SELF_DONATING;

        Map<Integer, Integer> countByTypeId = countFloorItemsByTypeId(preset.getFurniture());
        if (missingOnly) {
            Inventory inventory = extension.getInventory();
            if (inventory == null) {
                extension.getLogger().log(Messages.get("inventory.donate.inventory_not_loaded"), "red");
                return;
            }

            countByTypeId = countByTypeId.entrySet().stream().peek(entry ->
                            entry.setValue(entry.getValue() - inventory.getFloorItemsByType(entry.getKey()).size())
                    )
                    .filter(entry -> entry.getValue() > 0)
                    .collect(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue));

            extension.getLogger().log(Messages.get("inventory.donate.missing_started"), "orange");
        } else {
            extension.getLogger().log(Messages.get("inventory.donate.all_started"), "orange");
        }

        donateFloorFurni(countByTypeId);
    }

    public Map<Integer, Integer> countFloorItemsByTypeId(List<PresetFurni> presetFurniList) {
        Map<Integer, Integer> map = new HashMap<>();
        FurniDataTools furniDataTools = extension.getFurniDataTools();
        for (PresetFurni presetFurni : presetFurniList) {
            int classId = furniDataTools.getFloorTypeId(presetFurni.getClassName());
            map.putIfAbsent(classId, 0);
            map.compute(classId, (k, v) -> v + 1);
        }
        return map;
    }

    private void donateFloorFurni(Map<Integer, Integer> countsByTypeId) {
        new Thread(() -> {
            int counted = 0;
            int totalCount = countsByTypeId.values().stream().reduce(0, Integer::sum);
            int lastReportedCount = 0;
            for (Map.Entry<Integer, Integer> entry : countsByTypeId.entrySet()) {
                if (state == AutoDonateState.NONE) {
                    extension.getLogger().log(Messages.get("inventory.donate.aborted"), "red");
                    return;
                }
                donateFurni(false, entry.getKey(), "", entry.getValue());
                counted += entry.getValue();
                if (counted - lastReportedCount > 50) {
                    lastReportedCount = counted;
                    extension.getLogger().log(Messages.get("inventory.donate.progress", counted, totalCount), "orange");
                }
                Utils.sleep(500);
            }
            extension.getLogger().log(Messages.get("inventory.donate.finished"), "green");
            state = AutoDonateState.NONE;
        }).start();
    }

    private void donateFurni(boolean isWallItem, int typeId, String special, int amount) {
        extension.sendToServer(new HPacket("SelfDonateItem", HMessage.Direction.TOSERVER, isWallItem, typeId, special, amount));
    }
}

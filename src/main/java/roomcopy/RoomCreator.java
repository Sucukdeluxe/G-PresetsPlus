package roomcopy;

import utils.Messages;
import utils.Utils;
import extension.logger.Logger;
import gearth.protocol.HMessage;
import gearth.protocol.HPacket;

public class RoomCreator {

    private static final int CREATE_ATTEMPTS = 4;
    private static final int CREATE_RETRY_WAIT_MS = 15000;

    public static class CreatedRoom {
        public final int roomId;
        public final String reportedName;

        CreatedRoom(int roomId, String reportedName) {
            this.roomId = roomId;
            this.reportedName = reportedName;
        }
    }

    private final Executor executor;
    private final Logger logger;

    public RoomCreator(Executor executor, Logger logger) {
        this.executor = executor;
        this.logger = logger;
    }

    public void logRoomQuota() {
        Executor.AwaitingPacket answer =
                new Executor.AwaitingPacket("CanCreateRoom", HMessage.Direction.TOCLIENT, 2500);
        executor.register(answer);

        if (!executor.sendToServer("CanCreateRoom")) {
            logger.log(Messages.get("room.quota.header_unresolvable"), "orange");
            return;
        }

        HPacket response = executor.awaitPacket(answer);
        if (response == null) {
            logger.log(Messages.get("room.quota.no_response"), "orange");
            return;
        }
        logger.log(Messages.get("room.quota.response", response.toExpression()), "blue");
    }

    public CreatedRoom createRoom(String name, String description, String model,
                                  int category, int maxUsers, int tradeMode) {
        return createRoom(name, description, model, category, maxUsers, tradeMode, 0);
    }

    public CreatedRoom createRoom(String name, String description, String model,
                                  int category, int maxUsers, int tradeMode, int sourceRoomId) {
        if (!executor.isKnownName(HMessage.Direction.TOSERVER, "CreateFlat")) {
            logger.log(Messages.get("room.create.header_unresolvable"), "red");
            return null;
        }

        logger.log(Messages.get("room.create.started",
                name, model, category, maxUsers, tradeMode), "blue");


        for (int attempt = 1; attempt <= CREATE_ATTEMPTS; attempt++) {
            Executor.AwaitingPacket created =
                    new Executor.AwaitingPacket("FlatCreated", HMessage.Direction.TOCLIENT, 12000);
            Executor.AwaitingPacket entered =
                    new Executor.AwaitingPacket("RoomEntryInfo", HMessage.Direction.TOCLIENT, 12000)
                            .addConditions(packet -> {
                                int id = packet.readInteger();
                                return id > 0 && id != sourceRoomId;
                            });
            executor.register(created, entered);

            if (!executor.sendToServer("CreateFlat", name, description, model, category, maxUsers, tradeMode)) {
                logger.log(Messages.get("room.create.send_failed"), "red");
                return null;
            }

            HPacket response = executor.awaitPacket(created, entered);
            if (created.getPacket() != null) {
                return readCreatedRoom(created.getPacket(), name);
            }
            if (entered.getPacket() != null) {
                HPacket entry = entered.getPacket();
                entry.resetReadIndex();
                int newId = entry.readInteger();
                logger.log(Messages.get("room.create.detected_via_entry", newId), "orange");
                return new CreatedRoom(newId, null);
            }

            if (attempt == 1) {
                logDiagnostics(name, description, model, category, maxUsers, tradeMode);
            }
            if (attempt < CREATE_ATTEMPTS) {
                logger.log(Messages.get("room.create.throttled_retry",
                        attempt, CREATE_ATTEMPTS, CREATE_RETRY_WAIT_MS / 1000), "orange");
                Utils.sleep(CREATE_RETRY_WAIT_MS);
            }
        }

        logger.log(Messages.get("room.create.no_confirmation"), "red");
        return null;
    }

    private void logDiagnostics(String name, String description, String model,
                                int category, int maxUsers, int tradeMode) {
        logger.log(Messages.get("room.create.header_info",
                executor.describeHeader(HMessage.Direction.TOSERVER, "CreateFlat")), "grey");
        logger.log(Messages.get("room.create.header_info",
                executor.describeHeader(HMessage.Direction.TOCLIENT, "FlatCreated")), "grey");
        HPacket outgoing = new HPacket("CreateFlat", HMessage.Direction.TOSERVER,
                name, description, model, category, maxUsers, tradeMode);
        logger.log(Messages.get("room.create.outgoing_packet", outgoing.toExpression()), "grey");
        logger.log(Messages.get("room.create.outgoing_bytes", hex(outgoing)), "grey");
    }

    private static String hex(HPacket packet) {
        byte[] bytes = packet.toBytes();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length && i < 120; i++) {
            sb.append(String.format("%02X ", bytes[i]));
        }
        if (bytes.length > 120) sb.append("... (").append(bytes.length).append(" bytes)");
        return sb.toString().trim();
    }

    private CreatedRoom readCreatedRoom(HPacket packet, String expectedName) {
        packet.resetReadIndex();
        int roomId;
        try {
            roomId = packet.readInteger();
        } catch (Throwable t) {
            logger.log(Messages.get("room.create.read_failed", t), "red");
            return null;
        }

        String reportedName = null;
        try {
            reportedName = packet.readString();
        } catch (Throwable ignored) {
        }

        if (roomId <= 0) {
            logger.log(Messages.get("room.create.implausible_id", roomId), "red");
            return null;
        }

        if (reportedName != null && !reportedName.equals(expectedName)) {
            logger.log(Messages.get("room.create.name_mismatch",
                    reportedName, expectedName, roomId), "orange");
        }

        logger.log(Messages.get("room.create.new_id", roomId), "green");
        return new CreatedRoom(roomId, reportedName);
    }

    public boolean enterRoom(int roomId, String password) {
        if (!executor.isKnownName(HMessage.Direction.TOSERVER, "OpenFlatConnection")) {
            logger.log(Messages.get("room.enter.header_unresolvable"), "red");
            return false;
        }

        Executor.AwaitingPacket roomReady =
                new Executor.AwaitingPacket("RoomReady", HMessage.Direction.TOCLIENT, 20000);
        Executor.AwaitingPacket floorPlan =
                new Executor.AwaitingPacket("FloorHeightMap", HMessage.Direction.TOCLIENT, 20000);
        Executor.AwaitingPacket objects =
                new Executor.AwaitingPacket("Objects", HMessage.Direction.TOCLIENT, 20000);
        executor.register(roomReady, floorPlan, objects);

        executor.clearRoomPacketCache();

        if (!executor.sendToServer("OpenFlatConnection", roomId, password == null ? "" : password, -1)) {
            logger.log(Messages.get("room.enter.send_failed"), "red");
            return false;
        }

        executor.awaitPacketList(roomReady, floorPlan, objects);

        if (roomReady.getPacket() == null) {
            logger.log(Messages.get("room.enter.no_room_ready"), "red");
            return false;
        }
        if (floorPlan.getPacket() == null || objects.getPacket() == null) {
            logger.log(Messages.get("room.enter.packets_missing"), "orange");
            return false;
        }

        logger.log(Messages.get("room.enter.success"), "green");
        return true;
    }
}

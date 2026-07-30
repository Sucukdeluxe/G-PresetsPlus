package roomcopy;

import gearth.extensions.ExtensionBase;
import gearth.protocol.HMessage;
import gearth.protocol.HPacket;
import gearth.services.packet_info.PacketInfo;
import gearth.services.packet_info.PacketInfoManager;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Executor {

    private static final Set<String> ROOM_PACKET_NAMES = new HashSet<>(Arrays.asList(
            "getguestroomresult", "roomentrytile", "floorheightmap", "items", "objects", "roomvisualizationsettings"));
    private static final String[] NAME_SUFFIXES = { "messagecomposer", "messageevent", "composer", "event" };
    private static final String[] OUTGOING_SUFFIXES = { "MessageComposer", "Composer" };

    private final ExtensionBase extension;
    private final List<AwaitingPacket> awaitingPackets = new ArrayList<>();
    private final Map<String, HPacket> roomPacketCache = new HashMap<>();
    private final Object lock = new Object();

    public Executor(ExtensionBase extension) {
        this.extension = extension;

        extension.intercept(HMessage.Direction.TOSERVER, this::onMessageToServer);
        extension.intercept(HMessage.Direction.TOCLIENT, this::onMessageToClient);
    }

    private PacketInfoManager packetInfoManager() {
        return extension.getPacketInfoManager();
    }

    public boolean isKnownName(HMessage.Direction direction, String name) {
        return packetInfoManager().getPacketInfoFromName(direction, name) != null;
    }

    public String firstKnownName(HMessage.Direction direction, String... names) {
        for (String name : names) {
            if (isKnownName(direction, name)) {
                return name;
            }
        }
        return null;
    }

    public String describeHeader(HMessage.Direction direction, String name) {
        java.util.List<PacketInfo> all = packetInfoManager().getAllPacketInfoFromName(direction, name);
        if (all.isEmpty()) {
            return name + " = NICHT AUFLOESBAR";
        }
        StringBuilder sb = new StringBuilder(name + " -> ");
        for (int i = 0; i < all.size(); i++) {
            PacketInfo info = all.get(i);
            if (i > 0) sb.append(" | ");
            sb.append("id=").append(info.getHeaderId())
              .append(" src=").append(info.getSource());
            if (i == 0) sb.append(" (BENUTZT)");
        }
        if (all.size() > 1) {
            sb.append("  << MEHRFACH BELEGT");
        }
        return sb.toString();
    }

    private volatile String lastSentDescription = "";

    public String lastSentDescription() {
        return lastSentDescription;
    }

    public String resolveOutgoingName(String name) {
        for (String suffix : OUTGOING_SUFFIXES) {
            if (isKnownName(HMessage.Direction.TOSERVER, name + suffix)) {
                return name + suffix;
            }
        }
        return name;
    }

    public boolean sendToServer(String hashOrName, Object... objects) {
        return send(hashOrName, true, objects);
    }

    public boolean sendToServerRaw(String hashOrName, Object... objects) {
        return send(hashOrName, false, objects);
    }

    private boolean send(String hashOrName, boolean utf8Strings, Object[] objects) {
        String resolved = resolveOutgoingName(hashOrName);
        if (!isKnownName(HMessage.Direction.TOSERVER, resolved)) {
            lastSentDescription = hashOrName + " = NICHT AUFLOESBAR";
            return false;
        }
        HPacket packet = new HPacket(resolved, HMessage.Direction.TOSERVER,
                utf8Strings ? asUtf8Bytes(objects) : objects);
        lastSentDescription = describeHeader(HMessage.Direction.TOSERVER, resolved)
                + " | " + packet.toExpression()
                + (utf8Strings ? " | strings=UTF-8" : " | strings=roh")
                + " | len=" + packet.getBytesLength() + " hex=" + hex(packet);
        return extension.sendToServer(packet);
    }

    private static Object[] asUtf8Bytes(Object[] objects) {
        Object[] encoded = new Object[objects.length];
        for (int i = 0; i < objects.length; i++) {
            Object value = objects[i];
            encoded[i] = value instanceof String
                    ? new String(((String) value).getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1)
                    : value;
        }
        return encoded;
    }

    private static String hex(HPacket packet) {
        byte[] bytes = packet.toBytes();
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public String sendFirstKnown(String... names) {
        for (String name : names) {
            if (sendToServer(name)) {
                return name;
            }
        }
        return null;
    }

    public void sendToClient(String hashOrName, Object... objects) {
        extension.sendToClient(new HPacket(hashOrName, HMessage.Direction.TOCLIENT, asUtf8Bytes(objects)));
    }

    public static String normalizeName(String name) {
        if (name == null) return "";
        String normalized = name.toLowerCase(Locale.ROOT);
        for (String suffix : NAME_SUFFIXES) {
            if (normalized.endsWith(suffix)) {
                return normalized.substring(0, normalized.length() - suffix.length());
            }
        }
        return normalized;
    }

    private static boolean namesMatch(String expected, String actual) {
        return normalizeName(expected).equals(normalizeName(actual));
    }

    public HPacket getCachedRoomPacket(String headerName) {
        synchronized (lock) {
            HPacket cached = roomPacketCache.get(normalizeName(headerName));
            if (cached == null) return null;
            HPacket copy = new HPacket(cached);
            copy.resetReadIndex();
            return copy;
        }
    }

    public int cachedRoomPacketCount() {
        synchronized (lock) {
            return roomPacketCache.size();
        }
    }

    public void clearRoomPacketCache() {
        synchronized (lock) {
            roomPacketCache.clear();
        }
    }

    private void cacheRoomPacket(String name, HPacket packet) {
        String normalized = normalizeName(name);
        if (!ROOM_PACKET_NAMES.contains(normalized)) return;

        HPacket copy = new HPacket(packet);
        copy.resetReadIndex();
        if (normalized.equals("getguestroomresult") && !readsEnteredRoomFlag(copy)) return;
        copy.resetReadIndex();

        synchronized (lock) {
            roomPacketCache.put(normalized, copy);
        }
    }

    private static boolean readsEnteredRoomFlag(HPacket packet) {
        try {
            return packet.readBoolean();
        } catch (Throwable t) {
            return true;
        }
    }

    private void onMessageToServer(HMessage hMessage) {
        matchAwaiting(HMessage.Direction.TOSERVER, hMessage);
    }

    private void onMessageToClient(HMessage hMessage) {
        PacketInfo info = packetInfoManager().getPacketInfoFromHeaderId(
                HMessage.Direction.TOCLIENT, hMessage.getPacket().headerId());
        if (info == null) {
            return;
        }
        cacheRoomPacket(info.getName(), hMessage.getPacket());
        matchAwaiting(HMessage.Direction.TOCLIENT, hMessage, info);
    }

    private void matchAwaiting(HMessage.Direction direction, HMessage hMessage) {
        PacketInfo info = packetInfoManager().getPacketInfoFromHeaderId(direction, hMessage.getPacket().headerId());
        if (info == null) {
            return;
        }
        matchAwaiting(direction, hMessage, info);
    }

    private void matchAwaiting(HMessage.Direction direction, HMessage hMessage, PacketInfo info) {
        List<AwaitingPacket> candidates;
        synchronized (lock) {
            candidates = awaitingPackets.stream()
                    .filter(packet -> packet.direction.equals(direction))
                    .filter(packet -> namesMatch(packet.headerName, info.getName()))
                    .collect(Collectors.toList());
        }
        for (AwaitingPacket packet : candidates) {
            if (packet.test(hMessage)) {
                packet.setPacket(hMessage.getPacket());
            }
        }
    }

    public void register(AwaitingPacket... packets) {
        synchronized (lock) {
            for (AwaitingPacket packet : packets) {
                if (!awaitingPackets.contains(packet)) {
                    awaitingPackets.add(packet);
                }
            }
        }
    }

    public HPacket awaitPacket(AwaitingPacket... packets) {
        register(packets);

        while (true) {
            for (AwaitingPacket awaitingPacket : packets) {
                if (awaitingPacket.isReady()) {
                    unregister(packets);
                    return awaitingPacket.getPacket();
                }
            }
            idle();
        }
    }

    public List<HPacket> awaitPacketList(AwaitingPacket... packets) {
        register(packets);

        while (true) {
            if (Arrays.stream(packets).allMatch(AwaitingPacket::isReady)) {
                unregister(packets);
                return Arrays.stream(packets).map(AwaitingPacket::getPacket).collect(Collectors.toList());
            }
            idle();
        }
    }

    private void unregister(AwaitingPacket... packets) {
        synchronized (lock) {
            awaitingPackets.removeAll(Arrays.asList(packets));
        }
    }

    public void clear() {
        synchronized (lock) {
            this.awaitingPackets.clear();
        }
    }

    private static void idle() {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static class AwaitingPacket {

        private static final ScheduledExecutorService TIMEOUTS = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "awaiting-packet-timeouts");
            thread.setDaemon(true);
            return thread;
        });

        public final String headerName;
        public final HMessage.Direction direction;
        private volatile HPacket packet = null;
        private volatile boolean received = false;
        private final List<Predicate<? super HPacket>> conditions = new ArrayList<>();
        private final long start;
        private long minWait = 0;

        public AwaitingPacket(String headerName, HMessage.Direction direction, int maxWaitingTimeMillis) {
            this.headerName = headerName;
            this.direction = direction;

            if (maxWaitingTimeMillis < 50) {
                maxWaitingTimeMillis = 50;
            }

            TIMEOUTS.schedule(() -> received = true, maxWaitingTimeMillis, TimeUnit.MILLISECONDS);

            this.start = System.currentTimeMillis();
        }

        public AwaitingPacket setMinWaitingTime(int millis) {
            this.minWait = millis;
            return this;
        }

        @SafeVarargs
        public final AwaitingPacket addConditions(Predicate<? super HPacket>... conditions) {
            this.conditions.addAll(Arrays.asList(conditions));
            return this;
        }

        private void setPacket(HPacket packet) {
            HPacket copy = new HPacket(packet);
            copy.resetReadIndex();
            this.packet = copy;
            this.received = true;
        }

        public HPacket getPacket() {
            if (packet != null) {
                this.packet.resetReadIndex();
            }
            return this.packet;
        }

        private boolean test(HMessage hMessage) {
            for (Predicate<? super HPacket> condition : conditions) {
                HPacket packet = hMessage.getPacket();
                packet.resetReadIndex();
                try {
                    if (condition.test(packet)) {
                        packet.resetReadIndex();
                        return true;
                    }
                } catch (Throwable ignored) {
                }
                packet.resetReadIndex();
            }
            return conditions.isEmpty();
        }

        private boolean isReady() {
            return received && start + minWait < System.currentTimeMillis();
        }
    }
}

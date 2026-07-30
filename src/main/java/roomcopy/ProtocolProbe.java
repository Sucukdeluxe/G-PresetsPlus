package roomcopy;

import extension.logger.Logger;
import gearth.extensions.ExtensionBase;
import gearth.protocol.HMessage;

public class ProtocolProbe {

    private static final boolean ENABLED = false;

    public static void attachIfEnabled(ExtensionBase extension, Logger logger) {
        if (ENABLED) {
            new ProtocolProbe(extension, logger);
        }
    }

    private static final String[] WATCHED_OUTGOING = {
            "CreateFlat", "OpenFlatConnection", "UpdateFloorProperties",
            "SaveRoomSettings", "BuildersClubPlaceWallItem"
    };

    private static final String[] WATCHED_INCOMING = {
            "FlatCreated", "UpdateFloorPropertiesResponse", "NoSuchFlat"
    };

    private final Logger logger;

    private ProtocolProbe(ExtensionBase extension, Logger logger) {
        this.logger = logger;

        for (String name : WATCHED_OUTGOING) {
            extension.intercept(HMessage.Direction.TOSERVER, name,
                    hMessage -> log("out", name, hMessage));
        }
        for (String name : WATCHED_INCOMING) {
            extension.intercept(HMessage.Direction.TOCLIENT, name,
                    hMessage -> log("in", name, hMessage));
        }
    }

    private void log(String direction, String name, HMessage hMessage) {
        hMessage.getPacket().resetReadIndex();
        logger.log(String.format("[Protokoll] %s %s %s", direction, name,
                hMessage.getPacket().toExpression()), "grey");
    }
}

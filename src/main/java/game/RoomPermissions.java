package game;

import extension.logger.Logger;
import gearth.extensions.IExtension;
import gearth.protocol.HMessage;
import utils.Callback;

public class RoomPermissions {
    private final IExtension extension;
    private final Logger logger;
    private final Callback stateChangeCallback;

    private boolean canModifyWired;
    public enum Grant { UNKNOWN, ALLOWED, DENIED }

    private Grant furniGrant = Grant.UNKNOWN;

    public RoomPermissions(IExtension extension, Logger logger, Callback stateChangeCallback) {
        this.extension = extension;
        this.logger = logger;
        this.stateChangeCallback = stateChangeCallback;

        extension.intercept(HMessage.Direction.TOCLIENT, "WiredPermissions", this::onWiredPermissions);
        extension.intercept(HMessage.Direction.TOCLIENT, "YouAreController", this::onYouAreController);
        extension.intercept(HMessage.Direction.TOCLIENT, "YouAreNotController", this::onYouAreNotController);

        extension.intercept(HMessage.Direction.TOCLIENT, "CloseConnection", m -> clear());
        extension.intercept(HMessage.Direction.TOSERVER, "Quit", m -> clear());
        extension.intercept(HMessage.Direction.TOCLIENT, "RoomReady", m -> clear());
    }

    private void onWiredPermissions(HMessage msg) {
        canModifyWired = msg.getPacket().readBoolean();
        stateChangeCallback.call();
    }

    private void onYouAreController(HMessage msg) {
        furniGrant = Grant.ALLOWED;
        stateChangeCallback.call();
    }

    private void onYouAreNotController(HMessage msg) {
        furniGrant = Grant.DENIED;
        stateChangeCallback.call();
    }

    public boolean canModifyWired() {
        return canModifyWired;
    }

    public Grant furniGrant() {
        return furniGrant;
    }

    public boolean furniExplicitlyDenied() {
        return furniGrant == Grant.DENIED;
    }

    public boolean canMoveFurni() {
        return furniGrant == Grant.ALLOWED;
    }

    public void clear() {
        canModifyWired = false;
        furniGrant = Grant.UNKNOWN;
        stateChangeCallback.call();
    }
}

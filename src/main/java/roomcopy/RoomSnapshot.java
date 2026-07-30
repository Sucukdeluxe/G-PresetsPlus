package roomcopy;

import org.json.JSONObject;

public class RoomSnapshot {

    public final RoomSettingsSnapshot settings;
    public final FloorPlanSnapshot floorPlan;
    public final WallItemsSnapshot wallItems;
    public final JSONObject fullSettings;

    public RoomSnapshot(RoomSettingsSnapshot settings, FloorPlanSnapshot floorPlan, WallItemsSnapshot wallItems) {
        this(settings, floorPlan, wallItems, null);
    }

    public RoomSnapshot(RoomSettingsSnapshot settings, FloorPlanSnapshot floorPlan, WallItemsSnapshot wallItems,
                        JSONObject fullSettings) {
        this.settings = settings;
        this.floorPlan = floorPlan;
        this.wallItems = wallItems;
        this.fullSettings = fullSettings;
    }

    public RoomSnapshot(JSONObject json) {
        this.settings = json.has("roomData") ? new RoomSettingsSnapshot(json.getJSONObject("roomData")) : null;
        this.floorPlan = json.has("floorPlan") ? new FloorPlanSnapshot(json.getJSONObject("floorPlan")) : null;
        this.wallItems = json.has("wallItems") ? new WallItemsSnapshot(json.getJSONArray("wallItems")) : null;
        this.fullSettings = json.optJSONObject("roomSettings");
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        if (settings != null) json.put("roomData", settings.toJson());
        if (floorPlan != null) json.put("floorPlan", floorPlan.toJson());
        if (wallItems != null) json.put("wallItems", wallItems.toJson());
        if (fullSettings != null) json.put("roomSettings", fullSettings);
        return json;
    }
}

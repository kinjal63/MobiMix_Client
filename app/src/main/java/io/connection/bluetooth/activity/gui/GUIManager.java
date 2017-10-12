package io.connection.bluetooth.activity.gui;

import java.util.List;

import io.connection.bluetooth.Database.DatabaseManager;
import io.connection.bluetooth.Database.entity.MBNearbyPlayers;

/**
 * Created by Kinjal on 10/13/2017.
 */

public class GUIManager {
    private DatabaseManager dbManager_;

    GUIManager() {
        dbManager_ = new DatabaseManager();
    }

    public List<MBNearbyPlayers> getNearbyPlayers() {
        return dbManager_.findPlayers();
    }
}

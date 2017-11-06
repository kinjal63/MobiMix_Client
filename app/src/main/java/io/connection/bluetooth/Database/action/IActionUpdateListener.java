package io.connection.bluetooth.Database.action;

/**
 * Created by Kinjal on 10/15/2017.
 */

public interface IActionUpdateListener extends IActionCRUD {
    public void onUpdateAction(int error);
}

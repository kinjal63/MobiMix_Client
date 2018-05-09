package io.connection.bluetooth.Database.action;

import java.util.List;

/**
 * Created by Kinjal on 10/15/2017.
 */

public interface IDatabaseActionListener {
    void onDataReceived(List<?> data);
    void onDataUpdated();
    void onDataError();
}

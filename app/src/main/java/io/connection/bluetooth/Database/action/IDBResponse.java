package io.connection.bluetooth.Database.action;

import java.util.List;

/**
 * Created by Kinjal on 10/15/2017.
 */

public interface IDBResponse {
    void onDataAvailable(int responseCode, List<?> data);
    void onDataFailure();
}

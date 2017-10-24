package io.connection.bluetooth.Database.action;

import java.util.List;

/**
 * Created by Kinjal on 10/15/2017.
 */

public interface IActionReadListener extends IActionCRUD {
    public void onReadOperation(int error, List<?> objects) ;
}

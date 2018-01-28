package io.connection.bluetooth.Database;

/**
 * Created by Kinjal on 1/20/2018.
 */

public enum DBError {
    GAME_NOT_FOUND(2002),
    INCORRECT_INPUT(2001);

    int errorCode;
    DBError(int errorCode) {
        this.errorCode = errorCode;
    }
}

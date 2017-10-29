package io.connection.bluetooth.enums;

import io.connection.bluetooth.utils.Constants;

/**
 * Created by KP49107 on 27-03-2017.
 */
public enum Modules {
    FILE_SHARING(Constants.FILESHARING_MODULE),
    CHAT(Constants.CHAT_MODULE),
    BUSINESS_CARD(Constants.BUSINESSCARD_MODULE),
    GAME(Constants.GAME_MODULE),
    NONE(Constants.NO_MODULE);

    private String moduleName;

    Modules(String moduleName) {
        this.moduleName = moduleName
    }

    public String getModuleName() {
        return moduleName;
    }
}

package io.connection.bluetooth.core;

import org.json.JSONObject;

import io.connection.bluetooth.enums.RadioType;

/**
 * Created by Kinjal on 11/6/2017.
 */

public class EventData {
    public int event_ = 0;
    public String userId_ = null;
    public String socketAddr_ = null;
    public RadioType radioType_ = null;
    public JSONObject object_ = null;

    public EventData(int event) {
        this.event_ = event;
    }

    public EventData() {

    }
}

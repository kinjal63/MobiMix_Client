package io.connection.bluetooth.Domain;

/**
 * Created by Kinjal on 11/5/2016.
 */
public class Calllog {
    private String number;
    private String name;
    private String duration;
    private String type;
    private int simNo;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getSimNo() {
        return simNo;
    }

    public void setSimNo(int simNo) {
        this.simNo = simNo;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}

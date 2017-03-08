package io.connection.bluetooth.activity;

import android.graphics.Bitmap;

/**
 * Created by songline on 19/08/16.
 */
public class Video {

    private String title,memory,duration,path;
    private Bitmap bitmap;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Video(String title, String memory, String duration, Bitmap bitmap) {
        this.title = title;
        this.memory = memory;
        this.bitmap = bitmap;

    }
    public Video(){

    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

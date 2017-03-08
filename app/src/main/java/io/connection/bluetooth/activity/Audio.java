package io.connection.bluetooth.activity;

import android.graphics.Bitmap;

/**
 * Created by songline on 19/08/16.
 */
public class Audio {
        private String title,singer,duration,size,path;
    private Bitmap bitmap;
    private int thumbId;

    public int getThumbId() {
        return thumbId;
    }

    public void setThumbId(int thumbId) {
        this.thumbId = thumbId;
    }

    public Audio(String title, String singer, String duration, Bitmap bitmap) {
        this.title = title;
        this.singer = singer;
        this.duration = duration;
        this.bitmap = bitmap;

    }
    public Audio(){

    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
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

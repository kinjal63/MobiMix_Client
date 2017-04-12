package io.connection.bluetooth.Domain;

import android.net.Uri;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Created by KP49107 on 12-04-2017.
 */
public class QueueManager {
    private static LinkedList<Object> filesToSend = new LinkedList<>();

    public static void addFilesToSend(List<Uri> files) {
        filesToSend.push(files);
    }

    public static List<Uri> getFilesToSend() {
        return  (List<Uri>)filesToSend.pop();
    }
}

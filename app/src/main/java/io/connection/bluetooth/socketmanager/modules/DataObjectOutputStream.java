package io.connection.bluetooth.socketmanager.modules;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * Created by Kinjal on 5/1/2018.
 */

public class DataObjectOutputStream extends ObjectOutputStream {
    public DataObjectOutputStream(OutputStream out) throws IOException {
        super(out);
    }

    @Override
    protected void writeStreamHeader() throws IOException {
        // do not write a header, but reset:
        // this line added after another question
        // showed a problem with the original
        reset();
    }
}

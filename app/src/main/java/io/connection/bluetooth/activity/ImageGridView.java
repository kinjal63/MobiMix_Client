package io.connection.bluetooth.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by songline on 04/08/16.
 */
public class ImageGridView extends AsyncTask<String, View, Bitmap> {

    ImageView imageViewGrid;
    String uri;

    ImageGridView(String uri, ImageView imageView) {

        this.uri = uri;
        this.imageViewGrid = imageView;

    }

    @Override
    protected Bitmap doInBackground(String... params) {

        int h = 200; // height in pixels
        int w = 200; // width in pixels
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap largeBitmap = BitmapFactory.decodeFile(uri, options);
        if (largeBitmap != null) {
            Bitmap scaled = Bitmap.createScaledBitmap(largeBitmap, h, w, true);
            return scaled;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if (bitmap != null) {
            imageViewGrid.setImageBitmap(bitmap);

        }
    }
}

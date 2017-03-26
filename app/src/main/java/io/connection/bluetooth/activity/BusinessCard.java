package io.connection.bluetooth.activity;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import io.connection.bluetooth.R;
import io.connection.imagecrop.CropImageIntentBuilder;

/**
 * Created by songline on 06/09/16.
 */
public class BusinessCard extends AppCompatActivity implements View.OnClickListener {

    public class ReceivingError extends Exception {
    }

    public class ResizeError extends Exception {
    }

    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    io.connection.imagecrop.CropImageIntentBuilder cropImage;

    public static final int AVATAR_REQUEST_CODE = 21;
    public static final int PIC_CROP = 31;
    public static final int IMAGE_QUALITY = 50;
    public static final int AVATAR_SIZE = 512;
    private static final String TAG = "MainActivity";
    String file_Path = "";
    private File tempFile;
    Uri picUri;
    File tempFileForGalleryPicture;
    ImageView card_avatar;
    ImageView card_avatar_edit_overlay;
    EditText card_name;
    EditText card_email;
    EditText card_phone;
    Button button_done;
    Button button_send;
    Button button_edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.businesscardlayout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        preferences = getSharedPreferences("businesscard", MODE_PRIVATE);
        editor = preferences.edit();
        ImageCache.setContext(this);

        card_avatar = (ImageView) findViewById(R.id.card_avatar);
        card_avatar_edit_overlay = (ImageView) findViewById(R.id.card_avatar_edit_overlay);
        card_name = (EditText) findViewById(R.id.card_name);
        card_email = (EditText) findViewById(R.id.card_email);
        card_phone = (EditText) findViewById(R.id.card_phone);
        button_done = (Button) findViewById(R.id.button_done);
        button_send = (Button) findViewById(R.id.button_send);
        button_edit = (Button) findViewById(R.id.button_edit);
        button_edit.setOnClickListener(this);
        button_send.setOnClickListener(this);
        button_done.setOnClickListener(this);

        imageClickEvent();

        card_name.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
        card_email.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
        card_phone.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
        //AcceptBusinessThread thread = new AcceptBusinessThread(BluetoothAdapter.getDefaultAdapter(), this);
        //thread.start();

        if (!new File(Environment.getExternalStorageDirectory() + "/TransferBluetooth").exists()) {
            new File(Environment.getExternalStorageDirectory() + "/TransferBluetooth").mkdir();
            new File(Environment.getExternalStorageDirectory() + "/TransferBluetooth/BusinessCard").mkdir();
        }
        card_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (!card_name.getText().toString().trim().equals("")) {
                    button_done.setVisibility(View.VISIBLE);
                } else {
                    button_done.setVisibility(View.GONE);
                }
            }
        });

        if (isBusinessCardCreated()) {

            card_avatar.setImageURI(Uri.parse(preferences.getString("picture", "")));
            card_avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
            card_name.setText(preferences.getString("name", ""));
            card_email.setText(preferences.getString("email", ""));
            card_phone.setText(preferences.getString("phone", ""));
            tempFileForGalleryPicture = new File(Uri.parse(preferences.getString("picture", "")).getPath());
            setDisableMode(preferences.getString("picture", ""));


        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_list_card:
                Intent intent = new Intent(this, BusinessCardReceivedList.class);
                this.startActivity(intent);
                break;

            case android.R.id.home:
                Intent intent1 = new Intent(this, io.connection.bluetooth.activity.Home_Master.class);
                startActivity(intent1);
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_done:
                Log.d(TAG, "onClick: done clicked");
                setDisableMode(tempFileForGalleryPicture != null ? tempFileForGalleryPicture.getPath() : "");
                break;
            case R.id.button_send:
                Log.d(TAG, "onClick: send clicked");
                Intent intent = new Intent(BusinessCard.this, BusinessCardListActivityUser.class);
                //intent.putExtra("connectThread", connectedThread);
                this.startActivity(intent);
                break;
            case R.id.button_edit:
                Log.d(TAG, "onClick: edit clickedd");
                setEnableMode();
                break;


        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == AVATAR_REQUEST_CODE) {
//               Bitmap bitmap =  data.getExtras().getParcelable("data");
                Log.d(TAG, "onActivityResult: " + tempFile.toString());
                //Log.d(TAG, "onActivityResult: "+data.getData());
                if (data != null && data.getData() != null) {
                    tempFile = new File(data.getData().getPath());

                }

                tempFileForGalleryPicture = new File(Environment.getExternalStorageDirectory() + "/TransferBluetooth/BusinessCard", tempFile.getName());
                Log.d(TAG, "onActivityResult: " + tempFile.toString()
                );

                // Log.d(TAG, "onActivityResult: "+picUri + "     "+bitmap.getByteCount());
                performCrop();
                // ImageView picView = (ImageView) findViewById(R.id.card_avatar);
                //picView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                //picView.setImageBitmap(bitmap);

            } else if (requestCode == PIC_CROP) {
                ImageView picView = (ImageView) findViewById(R.id.card_avatar);
                // picView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                picView.setImageBitmap(BitmapFactory.decodeFile(tempFileForGalleryPicture.getAbsolutePath()));

            }
        }
    }


    private void performCrop() {
        try {


            Uri croppedImage = Uri.fromFile(tempFile);

            cropImage = new CropImageIntentBuilder(200, 200, Uri.fromFile(tempFileForGalleryPicture));
            cropImage.setOutlineColor(0xFF03A9F4);
            cropImage.setSourceImage(croppedImage);
            if (Build.VERSION.SDK_INT >= 21)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1111);
            else
                startActivityForResult(cropImage.getIntent(this), PIC_CROP);
        } catch (ActivityNotFoundException anfe) {
            //display an error message
            anfe.printStackTrace();
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1111:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(cropImage.getIntent(this), PIC_CROP);
                }
        }
    }

    public void setDisableMode(String path) {
        card_avatar_edit_overlay.setVisibility(View.GONE);
        card_name.setEnabled(false);
        card_phone.setEnabled(false);
        card_email.setEnabled(false);
        button_done.setVisibility(View.GONE);
        if (!path.equals("")) {
            button_send.setVisibility(View.VISIBLE);
        }
        button_edit.setVisibility(View.VISIBLE);
        card_name.setTextColor(Color.BLACK);
        card_email.setTextColor(Color.BLACK);
        card_phone.setTextColor(Color.BLACK);
//        card_avatar.setOnClickListener(null);

        if (card_phone.getText().toString().trim().equals(""))
            card_phone.setVisibility(View.GONE);
        if (card_email.getText().toString().trim().equals(""))
            card_email.setVisibility(View.GONE);

        Log.d(TAG, "setDisableMode:  path " + path);
        editor.putString("picture", path);
        editor.putString("name", card_name.getText().toString());
        editor.putString("email", card_email.getText().toString());
        editor.putString("phone", card_phone.getText().toString());
        editor.putString("device_id", Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID));
        editor.putBoolean("isdataAvailable", true);
        editor.commit();


    }

    public void setEnableMode() {

        card_avatar_edit_overlay.setVisibility(View.VISIBLE);
        card_name.setEnabled(true);
        card_phone.setEnabled(true);
        card_email.setEnabled(true);
        button_done.setVisibility(View.VISIBLE);
        button_send.setVisibility(View.GONE);
        button_edit.setVisibility(View.GONE);
        card_email.setVisibility(View.VISIBLE);
        card_phone.setVisibility(View.VISIBLE);
        imageClickEvent();

    }


    public void imageClickEvent() {

        card_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickIntent = new Intent();
                pickIntent.setType("image/*");
                pickIntent.setAction(Intent.ACTION_GET_CONTENT);
                // pickIntent.addCategory(Intent.CATEGORY_OPENABLE);
                pickIntent.putExtra(MediaStore.EXTRA_OUTPUT, Environment.getExternalStorageDirectory());
                tempFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), System.nanoTime() + ".jpg");
                Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                takePhotoIntent.putExtra("android.intent.extras.CAMERA_FACING",
                        Camera.CameraInfo.CAMERA_FACING_FRONT);
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
                takePhotoIntent.addFlags(
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

                String pickTitle = "where from ?";
                Intent chooserIntent = Intent.createChooser(pickIntent, pickTitle);
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takePhotoIntent});
                chooserIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));

                startActivityForResult(chooserIntent, AVATAR_REQUEST_CODE);

            }
        });

    }


    public boolean isBusinessCardCreated() {

        return preferences.getBoolean("isdataAvailable", false);

    }

}

package io.connection.bluetooth.activity;

/**
 * Created by songline on 06/08/16.
 */

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.connection.bluetooth.Api.ApiCall;
import io.connection.bluetooth.Api.ApiClient;
import io.connection.bluetooth.R;

public class ImagesFragment extends Fragment {
    private static final String TAG = "ImagesFragment";
    RecyclerView gv;
    private String[] mNames = null;
    public Context context;
    ArrayList<File> files = new ArrayList<>();
    List<String> imageExtension = new ArrayList<>();
    private Cursor cc = null;
    private ProgressDialog myProgressDialog = null;
    private static Uri[] mUrls = null;
    private static String[] strUrls = null;
    private static int[] imageId = null;
    static GridAdapter gridAdapter;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    private BluetoothSocket bluetoothSocket;
    private static final int FILE_SELECT_CODE = 0;
    private ListView bluetoothDeviceList;
    BluetoothSocket bluetoothSocketForSharingData;
    public static int count = 0;
    // Declare variables
    ProgressDialog mProgressDialog;
    public static BottomSheetBehavior mBottomSheetBehavior;
    public static BottomSheetBehavior mBottomSheetBehaviorforFooter;
    public static TextView countText;


    ApiCall apiCall;

    GridLayoutManager layoutManager;
    int h, w;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        context = getActivity();
        View view = inflater.inflate(R.layout.fragment_home_fragment_, container, false);
        RelativeLayout bottomSheet = (RelativeLayout) getActivity().findViewById(R.id.footerSend);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        RelativeLayout recyclerView = (RelativeLayout) getActivity().findViewById(R.id.footer_device);
        mBottomSheetBehaviorforFooter = BottomSheetBehavior.from(recyclerView);

        countText = (TextView) getActivity().findViewById(R.id.countselectednumber);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        apiCall = ApiClient.getClient().create(ApiCall.class);
        if (bluetoothAdapter == null) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Not compatible")
                    .setMessage("Your phone does not support Bluetooth")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        gv = (RecyclerView) view.findViewById(R.id.gridView);
            initialize();
        return view;
    }

    public void initialize() {

        if (ImageCache.getURIs() == null) {
            cc = getActivity().getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null,
                    MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

            if (cc != null) {

                myProgressDialog = new ProgressDialog(getContext());
                myProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                myProgressDialog.setMessage("Loading");
                myProgressDialog.show();


            }
            new AsyncUriLoader().execute();
        } else {
            strUrls = ImageCache.getURIs();
            setRecyclerView();
        }
    }


    void setRecyclerView() {
        if (getActivity() != null)
            gv.setHasFixedSize(true);
        layoutManager = (GridLayoutManager) gv.getLayoutManager();

        // gv.setLayoutManager(layoutManager);'
        List<String> arraListString = new ArrayList<>();
        arraListString.addAll(Arrays.asList(strUrls));
        gridAdapter = new GridAdapter(getContext(), arraListString, layoutManager, imageId);
        gv.setAdapter(gridAdapter);
    }


    class AsyncUriLoader extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            getActivity().setRequestedOrientation(
//                    ActivityInfo.SCREEN_ORIENTATION_LOCKED
//            );
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                cc.moveToFirst();
                imageId = new int[cc.getCount()];
                mUrls = new Uri[cc.getCount()];
                strUrls = new String[cc.getCount()];
                mNames = new String[cc.getCount()];
                int ImageIdIndex = cc.getColumnIndex(MediaStore.Images.Media._ID);
                Log.d(TAG, "run: " + cc.getCount());
                for (int i = 0; i < cc.getCount(); i++) {
                    cc.moveToPosition(i);
                    mUrls[i] = Uri.parse(cc.getString(1));
                    imageId[i] = Integer.parseInt(cc.getString(ImageIdIndex));
                    strUrls[i] = cc.getString(1);
                    Log.d(TAG, "doInBackground: " + mUrls[i].getPath() + "   " + cc.getString(0));
                    mNames[i] = cc.getString(3);
                    Log.d(TAG, "mNames[i] : " + mNames[i] + ":" + cc.getColumnCount() + " : " + cc.getString(3));
                }

            } catch (Exception e) {
                Log.d(TAG, "run: " + e.getMessage());
            } finally {
                cc.close();
            }
//            ImageCache.setURIs(strUrls);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (myProgressDialog != null && myProgressDialog.isShowing())
                myProgressDialog.dismiss();
            setRecyclerView();
        }
    }

    public static void removeImages(int position, String key) {
        gridAdapter.remove(position, key);
    }

    public static void updateChekBox() {
        gridAdapter.notifyDataSetChanged();
    }

    static class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> {

        private static Context mContext;
        private List<String> data = new ArrayList<>();
        GridLayoutManager layoutManager;
        boolean[] checkBoxValue;
        int[] imageId;

        public GridAdapter(Context c, List<String> urls, GridLayoutManager layoutManager, int[] imageId) {
            mContext = c;
            data = urls;
            this.layoutManager = layoutManager;
            checkBoxValue = new boolean[data.size()];
            this.imageId = imageId;
        }

        public void remove(int position, String key) {

            data.remove(key);
            ImageCache.remove(key);
            notifyDataSetChanged();

        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.single_grid, parent, false), layoutManager);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.itemView.setTag(data.get(position));
            holder.imageView.setImageBitmap(null);
            holder.imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            holder.checkbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;

                    if (ImageCache.getImageCheckBox(holder.itemView.getTag().toString())) {
                        cb.setChecked(false);
                        ImageCache.setImageCheckBoxValue(holder.itemView.getTag().toString(), false);
                        --count;
                        if (countText.getText().equals("1")) {

                            countText.setText("");
                            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                        } else {
                            countText.setText(count + "");
                            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        }
                        holder.imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

                    } else {
                        cb.setChecked(true);
                        holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        ImageCache.setImageCheckBoxValue(holder.itemView.getTag().toString(), true);
                        ++count;
                        countText.setText(count + "");
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                    }
                    mBottomSheetBehaviorforFooter.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            });

            if (ImageCache.getImageCheckBox(holder.itemView.getTag().toString()) == null) {
                holder.checkbox.setChecked(false);
                ImageCache.setImageCheckBoxValue(holder.itemView.getTag().toString(), false);
            } else {
                holder.checkbox.setChecked(ImageCache.getImageCheckBox(holder.itemView.getTag().toString()));
            }

            if (ImageCache.get(data.get(position)) != null) {
                holder.imageView.setImageBitmap(ImageCache.get(data.get(position)));
                holder.progressBar.setVisibility(View.GONE);
            } else {
                holder.progressBar.setVisibility(View.VISIBLE);
                new AsyncImgLoader(holder.imageView, data.get(position), holder.progressBar, holder, position, imageId[position]).execute();
            }
        }

        public static Bitmap decodeURI(int filePath) {
            try {
                int h = 150; // height in pixels
                int w = 150; // width in pixels

                Bitmap largeBitmap = MediaStore.Images.Thumbnails.getThumbnail(mContext.getContentResolver(), filePath, MediaStore.Images.Thumbnails.MINI_KIND, null);
                if (largeBitmap != null) {
                    Bitmap scaled = ThumbnailUtils.extractThumbnail(largeBitmap, h, w);
                    // = Bitmap.createScaledBitmap(largeBitmap, h, w, true);
                    return scaled;
                }
            } catch (OutOfMemoryError e) {
                Log.e("oom", e.toString());
            } catch (Exception e) {
                Log.e("Exception ", e.getMessage());
            }
            return null;
        }

         static class AsyncImgLoader extends AsyncTask<Void, Void, Void> {
            ImageView view;
            String path;
            Bitmap bmp = null;
            View progress;
            ViewHolder holder;
            int position;
            int imageIdPosition;

            public AsyncImgLoader(ImageView view, String path, View progress, ViewHolder holder, int position, int imageIdPosition) {
                this.view = view;
                this.path = path;
                this.progress = progress;
                this.holder = holder;
                this.position = position;
                this.imageIdPosition = imageIdPosition;
            }

            @Override
            protected Void doInBackground(Void... params) {
                bmp = decodeURI(imageIdPosition);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (bmp != null) {
                    ImageCache.put(path, bmp);
                    holder.setImageView(bmp, position);
                }
            }
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            ProgressBar progressBar;
            CheckBox checkbox;
            GridLayoutManager layoutManager;

            public ViewHolder(View itemView, GridLayoutManager layoutManager) {
                super(itemView);
                this.layoutManager = layoutManager;
                imageView = (ImageView) itemView.findViewById(R.id.imageView);
                progressBar = (ProgressBar) itemView.findViewById(R.id.progress);
                checkbox = (CheckBox) itemView.findViewById(R.id.itemCheckBox);
                // itemView.setOnLongClickListener(this);

            }

            void setImageView(Bitmap bmp, int pos) {

                if (layoutManager.findFirstVisibleItemPosition() <= pos && layoutManager.findLastVisibleItemPosition() >= pos) {
                    imageView.setImageBitmap(bmp);
                    progressBar.setVisibility(View.GONE);
                }
            }

        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();


    }


}

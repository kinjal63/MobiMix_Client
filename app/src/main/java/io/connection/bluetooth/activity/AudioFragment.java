package io.connection.bluetooth.activity;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.connection.bluetooth.R;

/**
 * Created by songline on 18/08/16.
 */
public class AudioFragment extends Fragment {

    private static final String TAG = "AudioFragment";
    static Bitmap[] thumbPicture = null;
    private Cursor cc = null;
    private Cursor ccAlbum = null;
    List<Audio> audioList = new ArrayList<>();
    static AudioAdapter audioAdapter;


    public AudioFragment() {

    }

    RecyclerView recycleListView;
    LinearLayoutManager layoutManager = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        audioList.removeAll(audioList);
        View view = inflater.inflate(R.layout.audio_layout, container, false);
        recycleListView = (RecyclerView) view.findViewById(R.id.list_audio);
        recycleListView.setHasFixedSize(true);

        cc = getActivity().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.SIZE, MediaStore.Audio.Media.DATA}, null, null,
                MediaStore.Audio.Media.DATE_ADDED + " DESC");

        setRecycleView();

        if (audioList.isEmpty()) {
            new asyncAudioLoader().execute();
        }
        return view;
    }

    public static void updateCheckbox() {
        audioAdapter.notifyDataSetChanged();
    }


    public static void removeAudio(String str) {
        audioAdapter.remove(str);
    }


    public static class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.AudioViewHolder> {
        private static final String TAG = "AudioAdapter";
        private Context mContext;
        LinearLayoutManager layoutManager;
        List<Audio> audioListAdapter;

        public AudioAdapter(Context c, List<Audio> listAudios, LinearLayoutManager layoutManager) {
            mContext = c;
            this.layoutManager = layoutManager;
            audioListAdapter = listAudios;


        }

        public void remove(String str) {

            Audio removeAudio = null;
            for (Audio audio : audioListAdapter) {
                Log.d(TAG, "remove: " + audio.getPath() + "\n   file path " + str);
                if (audio.getPath().equals(str)) {
                    removeAudio = audio;
                    break;
                }
            }
            boolean abc = audioListAdapter.remove(removeAudio);

            Log.d(TAG, "remove: or not  " + abc);
            //notifyItemRemoved(position);
            notifyDataSetChanged();
        }

        @Override
        public AudioViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.d(TAG, "onCreateViewHolder: in create view Holder");
            View v = LayoutInflater.from(mContext).inflate(R.layout.audio_layout_grid, parent, false);
            AudioViewHolder holder = new AudioViewHolder(v, layoutManager);
            return holder;
        }

        @Override
        public void onBindViewHolder(final AudioViewHolder holder, int position) {

            Audio audio = audioListAdapter.get(position);
            holder.itemView.setTag(audio.getPath());
            Log.d(TAG, "onBindViewHolder: " + audio.getTitle() + "    " + audio.getPath());
            if (thumbPicture[audio.getThumbId()] != null) {
                holder.imageView.setImageBitmap(thumbPicture[audio.getThumbId()]);
            } else {
                holder.imageView.setImageResource(R.drawable.music_icon);
            }
            holder.titleString.setText(audio.getTitle());
            holder.singer.setText(audio.getSinger());
            holder.duration.setText(audio.getDuration());
            holder.size.setText(audio.getSize());

            holder.checkbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;

                    if (ImageCache.getAudioCheckBox(holder.itemView.getTag().toString())) {
                        cb.setChecked(false);
                        ImageCache.setAudioCheckBoxValue(holder.itemView.getTag().toString(), false);
                        --ImagesFragment.count;
                        if (ImagesFragment.countText.getText().equals("1")) {

                            ImagesFragment.countText.setText("");
                            ImagesFragment.mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                        } else {
                            ImagesFragment.countText.setText(ImagesFragment.count + "");
                            ImagesFragment.mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        }

                    } else {
                        cb.setChecked(true);
                        ImageCache.setAudioCheckBoxValue(holder.itemView.getTag().toString(), true);
                        ++ImagesFragment.count;
                        ImagesFragment.countText.setText(ImagesFragment.count + "");
                        ImagesFragment.mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                    }
                    ImagesFragment.mBottomSheetBehaviorforFooter.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            });

            if (ImageCache.getAudioCheckBox(holder.itemView.getTag().toString()) == null) {
                holder.checkbox.setChecked(false);
                ImageCache.setAudioCheckBoxValue(holder.itemView.getTag().toString(), false);
            } else {
                holder.checkbox.setChecked(ImageCache.getAudioCheckBox(holder.itemView.getTag().toString()));
            }


        }

        @Override
        public int getItemCount() {
            Log.d(TAG, "getItemCount:  item COunt");
            return audioListAdapter.size();
        }

        public class AudioViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public TextView titleString;
            public TextView duration;
            public TextView singer;
            public TextView size;
            LinearLayoutManager linearLayoutManager;
            CheckBox checkbox;

            public AudioViewHolder(View w, LinearLayoutManager layoutManager) {
                super(w);
                imageView = (ImageView) w.findViewById(R.id.list_image);
                titleString = (TextView) w.findViewById(R.id.titleSong);
                duration = (TextView) w.findViewById(R.id.duration);
                singer = (TextView) w.findViewById(R.id.artist);
                size = (TextView) w.findViewById(R.id.size_audio);
                linearLayoutManager = layoutManager;
                checkbox = (CheckBox) w.findViewById(R.id.itemCheckBox_audio);
            }

        }


    }


    class asyncAudioLoader extends AsyncTask<Void, Void, Void> {
        private static final String TAG = "asyncAudioLoader";

        @Override
        protected Void doInBackground(Void... params) {
            try {

                int thumbCount = 0;
                thumbPicture = new Bitmap[cc.getCount()];

                ArrayList<String> checkBitmap = new ArrayList<>();
                cc.moveToFirst();

                Log.d(TAG, "run: " + cc.getCount());
                for (int i = 0; i < cc.getCount(); i++) {
                    cc.moveToPosition(i);
                    Audio audio = new Audio();
                    audio.setTitle(cc.getString(0));
                    audio.setSinger(cc.getString(1));


                    long milliSeconds = Long.parseLong(cc.getString(2));
                    long hours = TimeUnit.MILLISECONDS.toHours(milliSeconds);
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(milliSeconds) % 60;
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(milliSeconds) % 60;

                    String Time = "";
                    if (hours >= 1) {
                        Time += hours + ":";
                    }
                    if (minutes < 10) {
                        Time += "0" + minutes + ":";
                    } else {
                        Time += minutes + ":";
                    }
                    if (seconds < 10) {
                        Time += "0" + seconds;
                    } else {
                        Time += seconds;
                    }
                    audio.setDuration(Time);
                    audio.setSize(readableFileSize(Long.parseLong(cc.getString(4))));
                    audio.setPath(cc.getString(5));
                    Log.d(TAG, "doInBackground: 555555 " + cc.getString(3));
                    ccAlbum = getActivity().getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Audio.Albums._ID
                                    , MediaStore.Audio.Albums.ALBUM_ART}, MediaStore.Audio.Albums._ID + "=?",
                            new String[]{String.valueOf(cc.getString(3))}, null);
                    if (ccAlbum.moveToFirst() && !checkBitmap.contains(cc.getString(3))) {
                        String path = ccAlbum.getString(ccAlbum.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                        if (path != null) {
                            Bitmap bitmap = BitmapFactory.decodeFile(path);
                            checkBitmap.add(cc.getString(3));
                            thumbPicture[checkBitmap.size()] = Bitmap.createScaledBitmap(bitmap, 100, 100, true);
                            Log.d(TAG, "doInBackground:  " + path);
                            audio.setThumbId(checkBitmap.size());

                        } else {
                            checkBitmap.add(null);
                            thumbPicture[checkBitmap.size()] = null;
                            audio.setThumbId(checkBitmap.size());
                        }
                    } else {
                        if (checkBitmap.contains((cc.getString(3))))
                            audio.setThumbId(checkBitmap.indexOf(cc.getString(3)) + 1);
                    }
                    audioList.add(audio);
                    ccAlbum.close();

                }

            } catch (Exception e) {
                Log.d(TAG, "run: " + e.getMessage());
            } finally {

                cc.close();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            audioAdapter.notifyDataSetChanged();
        }
    }

    public void setRecycleView() {
        //getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        audioAdapter = new AudioAdapter(getContext(), audioList, layoutManager);
        recycleListView.setLayoutManager(layoutManager);
        //   recycleListView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        recycleListView.setAdapter(audioAdapter);
        Log.d(TAG, "setRecycleView: here in end ");
        // recycleListView.setAdapter(new AudioAdapter(getContext(),audioList, layoutManager));

    }

    public static String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}

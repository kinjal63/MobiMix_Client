package io.connection.bluetooth.activity;

import android.content.Context;
import android.database.Cursor;
import android.media.ThumbnailUtils;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.connection.bluetooth.R;

/**
 * Created by songline on 18/08/16.
 */
public class VideosFragment extends Fragment {

    private static final String TAG = "VideosFragment";
    private Cursor cc = null;
    List<Video> videoList = new ArrayList<>();
    static VideoAdapter videoAdapter;
    ProgressBar progressBar = null;


    public VideosFragment() {

    }

    RecyclerView recycleListView;
    LinearLayoutManager layoutManager = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.video_layout, container, false);
        recycleListView = (RecyclerView) view.findViewById(R.id.list_video);
        recycleListView.setHasFixedSize(true);
        progressBar = (ProgressBar)view.findViewById(R.id.progress_video);

        cc = getActivity().getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Video.Media.DATE_ADDED + " DESC");

        if (videoList.isEmpty()) {
            progressBar.setIndeterminate(true);
            progressBar.setVisibility(View.VISIBLE);
            new asyncAudioLoader().execute();
        } else {
            setRecycleView();
        }

        return view;
    }

    public static void removeVideo(String str) {
        videoAdapter.remove(str);
    }


    public static void updateCheckbox() {
        if (videoAdapter != null)
            videoAdapter.notifyDataSetChanged();
    }


    static class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {
        private static final String TAG = "VideoAdapter";
        private Context mContext;
        LinearLayoutManager layoutManager;
        List<Video> videoListAdapter;

        public VideoAdapter(Context c, List<Video> listVideo, LinearLayoutManager layoutManager) {
            mContext = c;
            this.layoutManager = layoutManager;
            videoListAdapter = listVideo;

        }

        public void remove(String str) {

            Video videoQ = null;
            for (Video video : videoListAdapter) {
                if (video.getPath().equals(str)) {
                    videoQ = video;
                    break;
                }
            }
            boolean abc = videoListAdapter.remove(videoQ);
            Log.d(TAG, "remove: is video removed  " + abc);
            notifyDataSetChanged();

        }

        @Override
        public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.d(TAG, "onCreateViewHolder: in create view Holder");
            View v = LayoutInflater.from(mContext).inflate(viewType==0?R.layout.video_layout_grid :R.layout.searching_devices, parent, false);
            VideoViewHolder holder = new VideoViewHolder(v, layoutManager,viewType);
            return holder;
        }

        @Override
        public void onBindViewHolder(final VideoViewHolder holder, int position) {

            Video video = videoListAdapter.get(position);
            holder.itemView.setTag(video.getPath());
            Log.d(TAG, "onBindViewHolder: " + video.getTitle() + "    " + video.getPath());
            holder.imageView.setImageBitmap(video.getBitmap());
            holder.titleString.setText(video.getTitle());
            holder.duration.setText(video.getDuration());
            holder.size.setText(video.getMemory());

            holder.checkbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;

                    if (ImageCache.getVideoCheckBox(holder.itemView.getTag().toString())) {
                        cb.setChecked(false);
                        ImageCache.setVideoCheckBoxValue(holder.itemView.getTag().toString(), false);
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
                        ImageCache.setVideoCheckBoxValue(holder.itemView.getTag().toString(), true);
                        ++ImagesFragment.count;
                        ImagesFragment.countText.setText(ImagesFragment.count + "");
                        ImagesFragment.mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                    }
                    ImagesFragment.mBottomSheetBehaviorforFooter.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            });

            if (ImageCache.getVideoCheckBox(holder.itemView.getTag().toString()) == null) {
                holder.checkbox.setChecked(false);
                ImageCache.setVideoCheckBoxValue(holder.itemView.getTag().toString(), false);
            } else {
                holder.checkbox.setChecked(ImageCache.getVideoCheckBox(holder.itemView.getTag().toString()));
            }


        }

        @Override
        public int getItemCount() {
            Log.d(TAG, "getItemCount:  item COunt");
            return videoListAdapter.size();
        }

        public class VideoViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public TextView titleString;
            public TextView duration;
            public TextView size;
            LinearLayoutManager linearLayoutManager;
            CheckBox checkbox;

            public VideoViewHolder(View w, LinearLayoutManager layoutManager,int type) {
                super(w);
                if(type==0) {
                    imageView = (ImageView) w.findViewById(R.id.list_video);
                    titleString = (TextView) w.findViewById(R.id.titleVideo);
                    duration = (TextView) w.findViewById(R.id.duration_video);
                    size = (TextView) w.findViewById(R.id.size_video);
                    checkbox = (CheckBox) w.findViewById(R.id.itemCheckBox_video);
                    linearLayoutManager = layoutManager;
                }
            }

        }


    }


    class asyncAudioLoader extends AsyncTask<Void, Void, Void> {
        private static final String TAG = "asyncAudioLoader";

        @Override
        protected Void doInBackground(Void... params) {
            try {
                cc.moveToFirst();
                int titleIndex = cc.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
                int sizeIndex = cc.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
                int dataIndex = cc.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                int durationIndex = cc.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);


                Log.d(TAG, "run: " + cc.getCount());
                for (int i = 0; i < cc.getCount(); i++) {
                    cc.moveToPosition(i);
                    Video video = new Video();
                    video.setTitle(cc.getString(titleIndex));
                    video.setMemory(readableFileSize(Long.parseLong(cc.getString(sizeIndex))));
                    video.setBitmap(ThumbnailUtils.createVideoThumbnail(cc.getString(dataIndex), MediaStore.Images.Thumbnails.MICRO_KIND));
                    video.setPath(cc.getString(dataIndex));
                    long milliSeconds = Long.parseLong(cc.getString(durationIndex));
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

                    video.setDuration(Time);
                    videoList.add(video);
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
            setRecycleView();
        }
    }

    public void setRecycleView() {
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        videoAdapter = new VideoAdapter(getContext(), videoList, layoutManager);
        recycleListView.setLayoutManager(layoutManager);
//       recycleListView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        recycleListView.setAdapter(videoAdapter);
        Log.d(TAG, "setRecycleView: here in end ");
        progressBar.setVisibility(View.GONE);

    }

    public static String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}

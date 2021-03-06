package io.connection.bluetooth.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.connection.bluetooth.Domain.GameInfo;
import io.connection.bluetooth.request.ReqGameInvite;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Kinjal on 11/24/2016.
 */
public class GameAdapter extends RecyclerView.Adapter<GameAdapter.MyViewHolder> {

    private List<GameInfo> gameList;
    private ImageLoader imageLoader;
    private ArrayList<Long> remoteUserIds;
    private Context context;
    public static String gamePackageName = "";
    public static String gameName = "";

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public ImageView imageView, imgWifi, imgBluetooth;

        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.txtName);
            imageView = (ImageView) view.findViewById(R.id.imgView);
            imgWifi = (ImageView) view.findViewById(R.id.imgWifi);
            imgBluetooth = (ImageView) view.findViewById(R.id.imgBluetooth);
        }
    }

    public GameAdapter(Context context, List<GameInfo> gameList, Activity activity) {
        this.context = context;
        this.gameList = gameList;
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(activity));
    }

    public void setRemoteUserIds(ArrayList<Long> userIds) {
        this.remoteUserIds = userIds;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final GameInfo userInfo = gameList.get(position);

        holder.name.setText(userInfo.getGamneName());
        holder.imgWifi.setTag(position);
        holder.imgBluetooth.setTag(position);

        holder.imgBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendConnectionInvites();
                gameName = gameList.get((int)view.getTag()).getGamneName();
                gamePackageName = gameList.get((int)view.getTag()).getGamePackageName();

                Intent LaunchIntent = MobileMeasurementApplication.getInstance().getContext().getPackageManager().
                        getLaunchIntentForPackage(GameAdapter.gamePackageName);
                MobileMeasurementApplication.getInstance().getContext().startActivity(LaunchIntent);
            }
        });
        holder.imgWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendWifiConnectionInvite();
                gameName = gameList.get((int)view.getTag()).getGamneName();
                gamePackageName = gameList.get((int)view.getTag()).getGamePackageName();

//                Intent LaunchIntent = MobileMeasurementApplication.getInstance().getContext().getPackageManager().
//                        getLaunchIntentForPackage(GameAdapter.gamePackageName);
//                MobileMeasurementApplication.getInstance().getContext().startActivity(LaunchIntent);
            }
        });
        imageLoader.displayImage(userInfo.getGameImagePath(), holder.imageView);
    }

    @Override
    public int getItemCount() {
        return gameList.size();
    }

    private void sendConnectionInvites() {
        String wifiAddress = ApplicationSharedPreferences.getInstance(context).getValue("wifi_p2p_address");
        ReqGameInvite gameInvite = new ReqGameInvite(ApplicationSharedPreferences.getInstance(context).getLongValue("user_id"), remoteUserIds, wifiAddress);
        retrofit2.Call<okhttp3.ResponseBody> req1 = MobileMeasurementApplication.getInstance().getService().sendConnectionInvite(gameInvite);

        req1.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String data = response.body().string();
                    System.out.println(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void sendWifiConnectionInvite() {
        String wifiAddress = ApplicationSharedPreferences.getInstance(context).getValue("wifi_p2p_address");
        ReqGameInvite gameInvite = new ReqGameInvite(ApplicationSharedPreferences.getInstance(context).getLongValue("user_id"), remoteUserIds, wifiAddress);
        retrofit2.Call<okhttp3.ResponseBody> req1 = MobileMeasurementApplication.getInstance().getService().sendConnectionInvite(gameInvite);

        req1.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String data = response.body().string();
                    System.out.println(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
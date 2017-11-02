package io.connection.bluetooth.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.connection.bluetooth.Database.entity.MBGameInfo;
import io.connection.bluetooth.Database.entity.MBNearbyPlayer;
import io.connection.bluetooth.MobiMixApplication;
import io.connection.bluetooth.R;
import io.connection.bluetooth.core.WifiDirectService;
import io.connection.bluetooth.request.ReqGameInvite;
import io.connection.bluetooth.utils.ApplicationSharedPreferences;
import io.connection.bluetooth.utils.Utils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Kinjal on 11/24/2016.
 */
public class GameAdapter extends RecyclerView.Adapter<GameAdapter.MyViewHolder> {

    private List<MBGameInfo> gameList;
    private ImageLoader imageLoader;
    private Context context;
    private List<MBNearbyPlayer> selectedPlayers;
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

    public GameAdapter(Context context, List<MBGameInfo> gameList, Activity activity) {
        this.context = context;
        this.gameList = gameList;
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(activity));
    }

    public void setGamePlayers(List<MBNearbyPlayer> players) {
        this.selectedPlayers = players;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final MBGameInfo gameInfo = gameList.get(position);

        holder.name.setText(gameInfo.getGameName());
        holder.imgWifi.setTag(position);
        holder.imgBluetooth.setTag(position);

        holder.imgBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gameName = gameList.get((int)view.getTag()).getGameName();
                gamePackageName = gameList.get((int)view.getTag()).getGamePackageName();

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Do you want to send Bluetooth connection for game " + gameName);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sendBluetoothConnectionInvite(gamePackageName);
                        dialogInterface.dismiss();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();
            }
        });
        holder.imgWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gameName = gameList.get((int)view.getTag()).getGameName();
                gamePackageName = gameList.get((int)view.getTag()).getGamePackageName();

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Do you want to send wifi-direct connection to selected users to play game " + gameName);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sendWifiConnectionInvite(gamePackageName);
                        dialogInterface.dismiss();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();
            }
        });
        imageLoader.displayImage(gameInfo.getGameImagePath(), holder.imageView);
    }

    @Override
    public int getItemCount() {
        return gameList.size();
    }

    private void sendBluetoothConnectionInvite(String gamePackageName) {
        Utils.makeDeviceDiscoverable(context);

        ReqGameInvite gameInvite = new ReqGameInvite(ApplicationSharedPreferences.getInstance(context).getValue("user_id"),
                remoteUserIds, 1);
        gameInvite.setGamePackageName(gamePackageName);
        retrofit2.Call<okhttp3.ResponseBody> req1 = MobiMixApplication.getInstance().getService().sendConnectionInvite(gameInvite);

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

    private void sendWifiConnectionInvite(MBGameInfo gameInfo) {
        WifiDirectService.getInstance(context).sendWifiDirectRequestToUser(selectedPlayers, gameInfo);
//        WifiDirectService.getInstance(context).initiateDiscovery();
////
//        ReqGameInvite gameInvite = new ReqGameInvite(ApplicationSharedPreferences.getInstance(context).getValue("user_id"),
//                remoteUserIds, 2);
//        gameInvite.setGamePackageName(gamePackageName);
//        retrofit2.Call<okhttp3.ResponseBody> req1 = MobiMixApplication.getInstance().getService().sendConnectionInvite(gameInvite);
//
//        req1.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                try {
//                    String data = response.body().string();
//                    System.out.println(data);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                t.printStackTrace();
//            }
//        });
    }
}
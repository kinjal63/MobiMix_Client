package io.connection.bluetooth.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.List;

import io.connection.bluetooth.Database.entity.MBNearbyPlayer;
import io.connection.bluetooth.R;
import io.connection.bluetooth.adapter.model.MyGameInfo;

/**
 * Created by Kinjal on 11/24/2016.
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyViewHolder> {

    private Context context;
    private List<MBNearbyPlayer> userList;
    private ImageLoader imageLoader;
    private MyGameInfo myGameInfo;
    private ArrayList<String> toUserIds = new ArrayList<>();

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, txtActiveGame;
        public ImageView imageView;
        private CheckBox checkBox;
        private View viewIsEngaged;

        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.txtName);
            imageView = (ImageView) view.findViewById(R.id.imgView);
            checkBox = (CheckBox) view.findViewById(R.id.chkBox);
            viewIsEngaged = (View) view.findViewById(R.id.view_is_engaged);
            txtActiveGame = (TextView) view.findViewById(R.id.txt_active_game);
        }
    }

    public UserAdapter(Context context, List<MBNearbyPlayer> userList, Activity activity) {
        this.context = context;
        this.userList = userList;
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(activity));
    }

    public void setMyInfo(MyGameInfo info) {
        this.myGameInfo = info;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        MBNearbyPlayer userInfo = userList.get(position);

//        int drawable = 0;
//        if(userInfo.isEngaged() == 1 && userInfo.getAllowedPlayersCount() <= 0
//                && myGameInfo.getIsEngaged() == 1 &&
//                !myGameInfo.getGroupOwnerId().equalsIgnoreCase(userInfo.getGroupOwnerUserId())) {
//
//        }
//        else if(userInfo.isEngaged() == 1 && userInfo.getAllowedPlayersCount() <= 0
//                && myGameInfo.getIsEngaged() == 1 &&
//                myGameInfo.getGroupOwnerId().equalsIgnoreCase(userInfo.getGroupOwnerUserId())) {
//
//        }
        int drawable = (userInfo.getIsEngaged() == 1 && userInfo.getMaxPlayers() <= 0)
                        || (userInfo.getIsEngaged() == 1 &&
                            myGameInfo.getIsEngaged() == 1 &&
                            !myGameInfo.getGroupOwnerId().equalsIgnoreCase(userInfo.getGroupOwnerUserId()))
                            ? R.drawable.view_deactive_background : R.drawable.view_active_background;

        if(userInfo.getIsEngaged() == 1) {
            holder.viewIsEngaged.setBackground(context.getResources().getDrawable(drawable));
        }
        else {
            holder.viewIsEngaged.setBackground(context.getResources().getDrawable(drawable));
        }

        int chckBoxVisibility =
                (myGameInfo.getIsEngaged() == 1 && myGameInfo.getAllowedPlayersCount() <= 0) ||
                (userInfo.getIsEngaged() == 1 && userInfo.getIsGroupOwner() == 0) ||
                (userInfo.getIsEngaged() == 1 && userInfo.getIsGroupOwner() == 1 && userInfo.getMaxPlayers() <= 0) ||
                        (userInfo.getIsEngaged() == 1 && userInfo.getIsGroupOwner() == 1 &&
                        (myGameInfo.getIsEngaged() == 1 && !myGameInfo.getGroupOwnerId().equalsIgnoreCase(userInfo.getGroupOwnerUserId())))
                ? View.INVISIBLE : View.VISIBLE;

        if(userInfo.getActiveGameName() != null && !userInfo.getActiveGameName().equalsIgnoreCase("null")) {
            holder.txtActiveGame.setVisibility(View.VISIBLE);
            holder.txtActiveGame.setText(userInfo.getActiveGameName());
        }
        else {
            holder.txtActiveGame.setVisibility(View.GONE);
        }
        holder.name.setText(userInfo.getPlayerName());
        imageLoader.displayImage(userInfo.getPlayerImagePath(), holder.imageView);

        holder.checkBox.setVisibility(chckBoxVisibility);
        holder.checkBox.setTag(position);
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                toUserIds.add(userList.get((int)compoundButton.getTag()).getPlayerId());
            }
        });
    }

    public ArrayList<String> getUserIds() {
        return toUserIds;
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}
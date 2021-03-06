package io.connection.bluetooth.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.connection.bluetooth.Domain.NearbyUserInfo;

/**
 * Created by Kinjal on 11/24/2016.
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyViewHolder> {

    private List<NearbyUserInfo> userList;
    private ImageLoader imageLoader;
    private ArrayList<Long> toUserIds = new ArrayList<>();

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public ImageView imageView;
        private CheckBox checkBox;

        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.txtName);
            imageView = (ImageView) view.findViewById(R.id.imgView);
            checkBox = (CheckBox) view.findViewById(R.id.chkBox);
        }
    }


    public UserAdapter(List<NearbyUserInfo> userList, Activity activity) {
        this.userList = userList;
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(activity));
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        NearbyUserInfo userInfo = userList.get(position);

        holder.name.setText(userInfo.getUserFirstName() + " " + userInfo.getUserLastName());
        imageLoader.displayImage(userInfo.getUserImagePath(), holder.imageView);

        holder.checkBox.setTag(position);
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                toUserIds.add(userList.get((int)compoundButton.getTag()).getUserId());
            }
        });
    }

    public ArrayList<Long> getUserIds() {
        return toUserIds;
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}
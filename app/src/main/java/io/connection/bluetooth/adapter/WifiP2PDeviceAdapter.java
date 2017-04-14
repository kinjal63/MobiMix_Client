package io.connection.bluetooth.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import io.connection.bluetooth.R;
import io.connection.bluetooth.Services.WifiDirectService;
import io.connection.bluetooth.activity.BusinessCardListActivityUser;
import io.connection.bluetooth.activity.DeviceListActivityChat;
import io.connection.bluetooth.activity.ImageCache;
import io.connection.bluetooth.activity.WifiDirectMainActivity;
import io.connection.bluetooth.activity.WifiP2PChatActivity;
import io.connection.bluetooth.adapter.model.WifiP2PRemoteDevice;
import io.connection.bluetooth.enums.Modules;
import io.connection.bluetooth.enums.NetworkType;

/**
 * Created by KP49107 on 28-03-2017.
 */
public class WifiP2PDeviceAdapter extends RecyclerView.Adapter<WifiP2PDeviceAdapter.ViewHolder>  {
    private Context mContext;
    private List<WifiP2PRemoteDevice> devices;
    private View.OnClickListener clickListener;

    public WifiP2PDeviceAdapter(Context mContext, List<WifiP2PRemoteDevice> devices) {
        this.mContext = mContext;
        this.devices = devices;
    }

    public void setDeviceClickListener(View.OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Context context;
        private WifiP2PRemoteDevice device;
        private TextView nameTV;

        ViewHolder(View itemView, Context context, int type) {
            super(itemView);
            if (type == 0) {
                nameTV = (TextView) itemView.findViewById(R.id.chat_user_name);
                this.context = context;
                itemView.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View v) {
            device = (WifiP2PRemoteDevice) v.getTag();
            ImageCache.setContext(context);

            Intent intent = new Intent();

            if(WifiDirectService.getInstance(mContext).getClassName().equalsIgnoreCase(BusinessCardListActivityUser.class.getSimpleName())) {
                WifiDirectService.getInstance(context).getMessageHandler().setModule(Modules.BUSINESS_CARD);

                intent.setClass(mContext, BusinessCardListActivityUser.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra("device", device);
                intent.putExtra("networkType", NetworkType.WIFI_DIRECT.name());
                context.startActivity(intent);
            }
            else if(WifiDirectService.getInstance(mContext).getClassName().equalsIgnoreCase(DeviceListActivityChat.class.getSimpleName())) {
                WifiDirectService.getInstance(context).getMessageHandler().setModule(Modules.CHAT);

                intent.setClass(mContext, WifiP2PChatActivity.class);
                intent.putExtra("device", device);
                intent.putExtra("networkType", NetworkType.WIFI_DIRECT.name());
                context.startActivity(intent);
            }
            else if(WifiDirectService.getInstance(mContext).getClassName().equalsIgnoreCase(WifiDirectMainActivity.class.getSimpleName())) {
                WifiDirectService.getInstance(context).getMessageHandler().setModule(Modules.FILE_SHARING);
                clickListener.onClick(v);
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).
                inflate(viewType == 0 ? R.layout.device_layout_chat : R.layout.searching_devices, parent, false), mContext, viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (holder.getItemViewType() == 0) {
            WifiP2PRemoteDevice device = devices.get(position);
            holder.nameTV.setText(device.getName());
            holder.itemView.setTag(device);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 && devices.isEmpty() ? 1 : 0;
    }

    @Override
    public int getItemCount() {
        return devices.isEmpty() ? 1: devices.size();
    }
}

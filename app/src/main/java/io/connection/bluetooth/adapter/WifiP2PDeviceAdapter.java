package io.connection.bluetooth.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import io.connection.bluetooth.Database.entity.MBNearbyPlayer;
import io.connection.bluetooth.R;
import io.connection.bluetooth.core.WifiDirectService;
import io.connection.bluetooth.actionlisteners.DeviceClickListener;
import io.connection.bluetooth.activity.BusinessCardListActivityUser;
import io.connection.bluetooth.activity.DeviceListActivityChat;
import io.connection.bluetooth.activity.ImageCache;
import io.connection.bluetooth.activity.WifiDirectMainActivity;
import io.connection.bluetooth.adapter.model.WifiP2PRemoteDevice;
import io.connection.bluetooth.enums.Modules;

/**
 * Created by KP49107 on 28-03-2017.
 */
public class WifiP2PDeviceAdapter extends RecyclerView.Adapter<WifiP2PDeviceAdapter.ViewHolder>  {
    private Context mContext;
    private List<MBNearbyPlayer> devices;
    private DeviceClickListener clickListener;

    public WifiP2PDeviceAdapter(Context mContext, List<MBNearbyPlayer> devices) {
        this.mContext = mContext;
        this.devices = devices;
    }

    public void setDeviceClickListener(DeviceClickListener clickListener) {
        this.clickListener = clickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Context context;
        private MBNearbyPlayer device;
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
            device = (MBNearbyPlayer) v.getTag();
            ImageCache.setContext(context);

            WifiDirectService wifiP2PService = WifiDirectService.getInstance(context);

            Modules module = wifiP2PService.getModule();

            if(wifiP2PService.getClassName().equalsIgnoreCase(BusinessCardListActivityUser.class.getSimpleName())) {
                if(module != Modules.BUSINESS_CARD) {
//                    wifiP2PService.closeSocket();
                    wifiP2PService.setModule(Modules.BUSINESS_CARD);
                }
            }
            else if(WifiDirectService.getInstance(mContext).getClassName().equalsIgnoreCase(DeviceListActivityChat.class.getSimpleName())) {
                if(module != Modules.CHAT) {
//                    wifiP2PService.closeSocket();
                    wifiP2PService.setModule(Modules.CHAT);
                }
            }
            else if(wifiP2PService.getClassName().equalsIgnoreCase(WifiDirectMainActivity.class.getSimpleName())) {
                if(module != Modules.FILE_SHARING) {
//                    wifiP2PService.closeSocket();
                    wifiP2PService.setModule(Modules.FILE_SHARING);
                }
            }
            clickListener.onWifiDeviceClick(device);
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
            MBNearbyPlayer device = devices.get(position);
            holder.nameTV.setText(device.getPlayerName());
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

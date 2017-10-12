package io.connection.bluetooth.adapter;

import android.content.Context;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.connection.bluetooth.R;
import io.connection.bluetooth.core.BluetoothService;
import io.connection.bluetooth.Thread.ConnectedThread;
import io.connection.bluetooth.actionlisteners.DeviceClickListener;
import io.connection.bluetooth.activity.BusinessCardListActivityUser;
import io.connection.bluetooth.activity.DeviceListActivityChat;
import io.connection.bluetooth.activity.ImageCache;
import io.connection.bluetooth.activity.MainActivity;
import io.connection.bluetooth.adapter.model.BluetoothRemoteDevice;
import io.connection.bluetooth.enums.Modules;

/**
 * Created by KP49107 on 17-04-2017.
 */
public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder> implements Filterable {
    private Context mContext;
    List<BluetoothRemoteDevice> devices = new ArrayList<>();
    List<BluetoothRemoteDevice> selectedDevices = new ArrayList<>();
    FriendFilter friendFilter;
    private ConnectedThread connectedThread;
    private DeviceClickListener clickListener;

    public BluetoothDeviceAdapter(Context mContext, List<BluetoothRemoteDevice> devices) {
        this.mContext = mContext;
        this.devices = devices;
    }

    public void setDeviceClickListener(DeviceClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(viewType == 0 ? R.layout.device_layout_chat : R.layout.searching_devices, parent, false), mContext, viewType);
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 && devices.isEmpty() ? 1 : 0;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (holder.getItemViewType() == 0) {
            holder.nameTV.setText(devices.get(position).getName());
            holder.chkSelectUser.setTag(devices.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return devices.isEmpty() ? 1 : devices.size();
    }

    @Override
    public Filter getFilter() {
        if (friendFilter == null) {
            friendFilter = new FriendFilter();
        }
        return friendFilter;
    }

    public List<BluetoothRemoteDevice> getBluetoothDevices() {
        return selectedDevices;
    }


    private class FriendFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults filterResults = new FilterResults();
            Map<BluetoothRemoteDevice, String> map = new HashMap<>();
            if (constraint != null && constraint.length() > 0 && constraint.toString().trim().length() > 0) {
                ArrayList<String> tempList = new ArrayList<String>();
                int i = 0;
                // search content in friend list
                for (BluetoothRemoteDevice device : devices) {
                    if (device.getName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        //tempList.add(user);
                        map.put(device, device.getName());
                        i++;
                    } else {
                        i++;
                    }
                }
                filterResults.count = map.size();
                filterResults.values = map;
            } else {
                int i = 0;
                for (BluetoothRemoteDevice device : devices) {
                    map.put(devices.get(i++), device.getName());
                }
                filterResults.count = map.size();
                filterResults.values = map;
            }
            return filterResults;
        }

        /**
         * Notify about filtered list to ui
         *
         * @param constraint text
         * @param results    filtered result
         */
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            Map<BluetoothRemoteDevice, String> objDeviceMap = (Map) results.values;
            devices.clear();
            devices.addAll(objDeviceMap.keySet());
            notifyDataSetChanged();
        }
    }


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView nameTV;
        ImageView imageView;
        CheckBox chkSelectUser;
        private Context context;
        private BluetoothRemoteDevice device;

        public ViewHolder(View itemView, Context context, int type) {
            super(itemView);
            if (type == 0) {
                nameTV = (TextView) itemView.findViewById(R.id.chat_user_name);
                chkSelectUser = (CheckBox) itemView.findViewById(R.id.chk_select_user);
                this.context = context;
                chkSelectUser.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View v) {
            device = (BluetoothRemoteDevice) v.getTag();
            ImageCache.setContext(context);

            BluetoothService bluetoothService = BluetoothService.getInstance();

            if(bluetoothService.getClassName().equalsIgnoreCase(BusinessCardListActivityUser.class.getSimpleName())) {
                bluetoothService.setModule(Modules.BUSINESS_CARD);
            }
            else if(bluetoothService.getClassName().equalsIgnoreCase(DeviceListActivityChat.class.getSimpleName())) {
                bluetoothService.setModule(Modules.CHAT);
            }
            else if(bluetoothService.getClassName().equalsIgnoreCase(MainActivity.class.getSimpleName())) {
                bluetoothService.setModule(Modules.FILE_SHARING);
            }

            selectedDevices.add(device);

//            if(BluetoothDeviceAdapter.this.clickListener != null) {
//                BluetoothDeviceAdapter.this.clickListener.onBluetoothDeviceClick(device);
//            }

            NotificationManagerCompat.from(context).cancelAll();

        }
    }

    public List<BluetoothRemoteDevice> getSelectedDevices() {
        return selectedDevices;
    }

}

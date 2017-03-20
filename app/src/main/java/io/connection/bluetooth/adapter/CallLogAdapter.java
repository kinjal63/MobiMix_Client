package io.connection.bluetooth.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import io.connection.bluetooth.Domain.Calllog;
import io.connection.bluetooth.R;

public class CallLogAdapter extends RecyclerView.Adapter<CallLogAdapter.MyViewHolder> {

    private List<Calllog> callLogsList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, number, duration, type;

        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.name);
            number = (TextView) view.findViewById(R.id.number);
            duration = (TextView) view.findViewById(R.id.duration);
            type = (TextView) view.findViewById(R.id.type);
        }
    }


    public CallLogAdapter(List<Calllog> callLogsList) {
        this.callLogsList = callLogsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.calllog_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Calllog calllog = callLogsList.get(position);
        if(calllog.getName() == null || calllog.getName().length() == 0) {
            holder.name.setText("(No name)");
        }
        else {
            holder.name.setText(calllog.getName());
        }
        holder.number.setText(calllog.getNumber());
        holder.duration.setText(calllog.getDuration());
        holder.type.setText(calllog.getType());
    }

    @Override
    public int getItemCount() {
        return callLogsList.size();
    }
}
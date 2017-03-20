package io.connection.bluetooth.activity;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.connection.bluetooth.Domain.Calllog;
import io.connection.bluetooth.R;
import io.connection.bluetooth.adapter.CallLogAdapter;
import io.connection.bluetooth.utils.ApplicationSharedPreferences;

/**
 * Created by Kinjal on 11/5/2016.
 */
public class CallLogActivity extends Activity {

    private RecyclerView recyclerView;
    private List<Calllog> callLogList1 = new ArrayList<Calllog>();
    private List<Calllog> callLogList2 = new ArrayList<Calllog>();
    private CallLogAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_calllogs);
        recyclerView = (RecyclerView) this.findViewById(R.id.recycler_view);

        if(getIntent().getIntExtra("simno", -1) == 1) {
            adapter = new CallLogAdapter(callLogList1);
        }else {
            adapter = new CallLogAdapter(callLogList2);
        }
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        getCallLogs();
    }

    private void getCallLogs() {
        Uri allCalls = Uri.parse("content://call_log/calls");
        Cursor c = managedQuery(allCalls, null, null, null, CallLog.Calls.DATE + " DESC");
        while (c.moveToNext()) {
            String num = c.getString(c.getColumnIndex(CallLog.Calls.NUMBER));// for  number
            String name = c.getString(c.getColumnIndex(CallLog.Calls.CACHED_NAME));// for name
            String duration = c.getString(c.getColumnIndex(CallLog.Calls.DURATION));// for duration
            String type = String.valueOf(Integer.parseInt(c.getString(c.getColumnIndex(CallLog.Calls.TYPE))));
            int idSimId = getSimIdColumn(c);// f

            Calllog log = new Calllog();
            log.setName(name);
            log.setNumber(num);
            log.setDuration(duration);
            log.setType(type);
            log.setSimNo(idSimId + 1);

            if( idSimId == 34 ) {
                callLogList2.add(log);
                int callLogDuration = (int)ApplicationSharedPreferences.getInstance(this).getLongValue("callLog2Duration");
                ApplicationSharedPreferences.getInstance(this).addLongValue("callLog2Duration", callLogDuration + Integer.parseInt(duration));
            }
            else {
                callLogList1.add(log);
                int callLogDuration = (int)ApplicationSharedPreferences.getInstance(this).getLongValue("callLog1Duration");
                ApplicationSharedPreferences.getInstance(this).addLongValue("callLog1Duration", callLogDuration + Integer.parseInt(duration));
            }
        }

        adapter.notifyDataSetChanged();
    }

    public static int getSimIdColumn(final Cursor c) {

        for (String s : new String[] { "sim_id", "simid", "sub_id" }) {
            int id = c.getColumnIndex(s);
            if (id >= 0) {
                Log.d("Calllog", "sim_id column found: " + s);
                return id;
            }
        }
        Log.d("Calllog", "no sim_id column found");
        return -1;
    }
}

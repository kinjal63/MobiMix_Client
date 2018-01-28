package io.connection.bluetooth.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.connection.bluetooth.Database.DBParams;
import io.connection.bluetooth.Database.entity.MBNearbyPlayer;
import io.connection.bluetooth.MobiMixApplication;
import io.connection.bluetooth.R;
import io.connection.bluetooth.activity.gui.GUIManager;
import io.connection.bluetooth.adapter.UserAdapter;
import io.connection.bluetooth.adapter.model.MyGameInfo;
import io.connection.bluetooth.core.MobiMix;
import io.connection.bluetooth.core.WifiDirectService;
import io.connection.bluetooth.enums.Modules;
import io.connection.bluetooth.utils.ApplicationSharedPreferences;
import io.connection.bluetooth.utils.Utils;

/**
 * Created by Kinjal on 11/24/2016.
 */
public class PlayerListActivity extends Activity implements IDBResponse {
    private RecyclerView recyclerView;
    private Button btnSubmit;
    private List<MBNearbyPlayer> userList = new ArrayList<MBNearbyPlayer>();

    private UserAdapter adapter;
    private LinearLayout llCurrentGame;
    private TextView txtCurrentGame;

    final String PROVIDER_NAME = "com.mobimix.provider";
    final String URI = "content://" + PROVIDER_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_player_info);
        MobiMixApplication.getInstance().registerActivity(this);

        // Set Game Module
        WifiDirectService.getInstance(this).setModule(Modules.GAME);
        recyclerView = (RecyclerView) this.findViewById(R.id.recycler_view1);
        btnSubmit = (Button) this.findViewById(R.id.btnSubmit);

        llCurrentGame = (LinearLayout) this.findViewById(R.id.ll_my_current_game);
        txtCurrentGame = (TextView) this.findViewById(R.id.txt_current_game);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        adapter = new UserAdapter(PlayerListActivity.this, userList, this);
        recyclerView.setAdapter(adapter);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMutualGames();
            }
        });

        getNearByPlayers();

        Uri contentUri = Uri.parse(URI + "/mb_game_participants");
        String[] selectionArgs = new String[]{"redmi@gmail.com", "srk.syracuse.gameofcards"};

        Cursor c = getContentResolver().query(contentUri, null, null, selectionArgs, null);
        if(c.getCount() > 0) {
            c.moveToFirst();
            String groupOwnerDeviceName = c.getString(c.getColumnIndex("group_owner_device_name"));
            int connectionType = c.getInt(c.getColumnIndex("connection_type"));
            String gameParticipants = c.getString(c.getColumnIndex("game_participants"));
            String userName = c.getString(c.getColumnIndex("user_name"));
            System.out.println("End");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void updateView(MyGameInfo myInfo) {
        if (myInfo.getIsEngaged() == 0) {
            llCurrentGame.setVisibility(View.GONE);
        } else {
            txtCurrentGame.setText(myInfo.getCurrentActiveGame());
        }
    }

    private void getNearByPlayers() {
        DBParams params = new DBParams();
        params.event_ = MobiMix.DBRequest.DB_FIND_NEARBY_PLAYERS;
        params.userId_ = ApplicationSharedPreferences.getInstance(this).getValue("user_id");
        GUIManager.getObject().getNearbyPlayers(params, this);
    }

    @Override
    public void onDataAvailable(int resCode, List<?> data) {
        if (resCode == MobiMix.DBResponse.DB_RES_FIND_NEARBY_PLAYERS) {
            List<MBNearbyPlayer> players = (List<MBNearbyPlayer>) data;
            MyGameInfo myGameInfo = null;

            this.userList.clear();
            for (MBNearbyPlayer player : players) {
                if (player.getPlayerId().equalsIgnoreCase(
                        ApplicationSharedPreferences.getInstance(PlayerListActivity.this).getValue("user_id"))) {
                    myGameInfo = new MyGameInfo();
                    myGameInfo.setUserId(player.getPlayerId());
                    myGameInfo.setCurrentActiveGame(player.getActiveGameName());
                    myGameInfo.setIsEngaged(player.getIsEngaged());
                    myGameInfo.setGroupOwnerId(player.getGroupOwnerUserId());
                    myGameInfo.setAllowedPlayersCount(player.getMaxPlayers());
                    continue;
                }
                this.userList.add(player);
            }
            adapter.notifyDataSetChanged();
            adapter.setMyInfo(myGameInfo);
            if(myGameInfo != null){
                updateView(myGameInfo);
            }
        }
    }

    @Override
    public void onDataFailure() {
        Utils.showErrorDialog(this, "Players could not be retrived, Please try after some time.");
    }

    private void showMutualGames() {
        ArrayList<MBNearbyPlayer> selectedPlayers = adapter.getSelectedPlayers();
        Intent intent = new Intent(PlayerListActivity.this, GameListActivity.class);
        intent.putExtra("selected_players", selectedPlayers);
        startActivity(intent);
    }
}

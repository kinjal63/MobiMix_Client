package io.connection.bluetooth.activity;

import android.app.Activity;
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
import io.connection.bluetooth.Database.entity.MBGameInfo;
import io.connection.bluetooth.Database.entity.MBNearbyPlayer;
import io.connection.bluetooth.MobiMixApplication;
import io.connection.bluetooth.R;
import io.connection.bluetooth.actionlisteners.DialogActionListener;
import io.connection.bluetooth.activity.gui.GUIManager;
import io.connection.bluetooth.adapter.GameAdapter;
import io.connection.bluetooth.adapter.RecyclerItemClickListener;
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

    private Object adapter;
    private boolean isGame = false;
    private LinearLayout llCurrentGame;
    private TextView txtCurrentGame;

    RecyclerItemClickListener itemClickListener;
    MyGameInfo gameInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_info);

        // Set Game Module
        WifiDirectService.getInstance(this).setModule(Modules.GAME);
        recyclerView = (RecyclerView) this.findViewById(R.id.recycler_view1);
        btnSubmit = (Button) this.findViewById(R.id.btnSubmit);

        llCurrentGame = (LinearLayout)this.findViewById(R.id.ll_my_current_game);
        txtCurrentGame = (TextView)this.findViewById(R.id.txt_current_game);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        itemClickListener = new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                isGame = true;
                List<MBGameInfo> gameInfoList = userList.get(position).getPlayerGames();
                adapter = new GameAdapter(PlayerListActivity.this, gameInfoList, PlayerListActivity.this);
                recyclerView.setAdapter((GameAdapter) adapter);
                recyclerView.removeOnItemTouchListener(itemClickListener);
            }
        });

        MobiMixApplication.getInstance().registerActivity(this);
        addOnItemTouchListener();
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getMutualGames(ApplicationSharedPreferences.getInstance(PlayerListActivity.this).getValue("user_id"));
            }
        });

        getNearByGames();
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

    private void addOnItemTouchListener() {
        isGame = false;
        adapter = new UserAdapter(PlayerListActivity.this, userList, this);
        recyclerView.setAdapter((UserAdapter) adapter);

        btnSubmit.setVisibility(View.VISIBLE);
    }

    private void updateView(MyGameInfo myInfo) {
        if(myInfo.getIsEngaged() == 0) {
            llCurrentGame.setVisibility(View.GONE);
        }
        else {
            txtCurrentGame.setText(myInfo.getCurrentActiveGame());
        }
    }

    private void getMutualGames(final String userId) {
        final ArrayList<String> remoteUserIds = ((UserAdapter)adapter).getUserIds();

//        if(remoteUsers.size() == 1 && remoteUsers.get(0).getIsEngaged() == 0) {
//            Utils.showAlertMessage(this, "Game Invitation", "Would you like to involve any other player in this game?",
//                    new DialogActionListener() {
//                        @Override
//                        public void dialogPositiveButtonPerformed() {
//                            sendRequestToGetMutualGames(0, userId, remoteUserIds);
//                        }
//
//                        @Override
//                        public void dialogNegativeButtonPerformed() {
//                            sendRequestToGetMutualGames(1, userId, remoteUserIds);
//                        }
//                    });
//        }
//        else {
            sendRequestToGetMutualGames(0, userId, remoteUserIds);
//        }
    }

    private void getNearByGames() {
        DBParams params = new DBParams();
        params.event_ = MobiMix.DBRequest.DB_FIND_NEARBY_PLAYERS;
        params.userId_ = ApplicationSharedPreferences.getInstance(this).getValue("user_id");
        GUIManager.getObject().getNearbyPlayers(params, this);
    }

    private void sendRequestToGetMutualGames(int isTwoPlayerOnly, String userId, final ArrayList<String> remoteUserIds) {
        remoteUserIds.add(userId);

        DBParams params = new DBParams();
        params.event_ = MobiMix.DBRequest.DB_FIND_MUTUAL_GAMES;
        params.userId_ = ApplicationSharedPreferences.getInstance(this).getValue("user_id");
        params.remoteUserIds_ = remoteUserIds;
        GUIManager.getObject().getMutualGames(params, this);
    }

    @Override
    public void onBackPressed() {
        if (isGame) {
            addOnItemTouchListener();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onDataAvailable(int resCode, List<?> data) {
        if(resCode == MobiMix.DBResponse.DB_RES_FIND_NEARBY_PLAYERS) {
            isGame = false;

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
            if (adapter != null && adapter instanceof UserAdapter) {
                ((UserAdapter) adapter).notifyDataSetChanged();
                ((UserAdapter) adapter).setMyInfo(myGameInfo);
            }
            updateView(myGameInfo);
        }
        else if(resCode == MobiMix.DBResponse.DB_RES_FIND_MUTUAL_GAMES) {
            isGame = true;
            List<MBGameInfo> gameInfoList = (List<MBGameInfo>)data;

            adapter = new GameAdapter(PlayerListActivity.this, gameInfoList, PlayerListActivity.this);
            recyclerView.setAdapter((GameAdapter) adapter);
        }
    }

    @Override
    public void onDataFailure() {
        Utils.showErrorDialog(this, "Players could not be retrived, Please try after some time.");
    }
}

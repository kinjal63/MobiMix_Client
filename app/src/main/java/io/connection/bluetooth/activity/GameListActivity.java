package io.connection.bluetooth.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.connection.bluetooth.Database.DBParams;
import io.connection.bluetooth.Database.entity.MBGameInfo;
import io.connection.bluetooth.Database.entity.MBNearbyPlayer;
import io.connection.bluetooth.MobiMixApplication;
import io.connection.bluetooth.R;
import io.connection.bluetooth.activity.gui.GUIManager;
import io.connection.bluetooth.adapter.GameAdapter;
import io.connection.bluetooth.core.MobiMix;
import io.connection.bluetooth.utils.ApplicationSharedPreferences;
import io.connection.bluetooth.utils.Utils;

/**
 * Created by Kinjal on 11/3/2017.
 */
public class GameListActivity extends Activity implements IDBResponse {
    private RecyclerView recyclerView;
    private GameAdapter gameAdapter;

    private List<MBGameInfo> gameList = new ArrayList<MBGameInfo>();
    private List<MBNearbyPlayer> selectedPlayers = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game_info);
        MobiMixApplication.getInstance().registerActivity(this);

        selectedPlayers = (ArrayList<MBNearbyPlayer>)getIntent().getSerializableExtra("selected_players");

        recyclerView = (RecyclerView) this.findViewById(R.id.recycler_view1);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        gameAdapter = new GameAdapter(this, gameList, GameListActivity.this);
        recyclerView.setAdapter(gameAdapter);

        getMutualGames();
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

    private void getMutualGames() {
        String userId = ApplicationSharedPreferences.getInstance(GameListActivity.this).getValue("user_id");
        sendRequestToGetMutualGames(userId, selectedPlayers);
    }

    private void sendRequestToGetMutualGames(String userId, final List<MBNearbyPlayer> selectedPlayers) {
        List<String> remoteUserIds = new ArrayList<>();
        remoteUserIds.add(userId);

        for (MBNearbyPlayer player : selectedPlayers) {
            remoteUserIds.add(player.getPlayerId());
        }

        DBParams params = new DBParams();
        params.event_ = MobiMix.DBRequest.DB_FIND_MUTUAL_GAMES;
        params.userId_ = ApplicationSharedPreferences.getInstance(this).getValue("user_id");
        params.remoteUserIds_ = remoteUserIds;
        GUIManager.getObject().getMutualGames(params, this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onDataAvailable(int resCode, List<?> data) {
        if (resCode == MobiMix.DBResponse.DB_RES_FIND_MUTUAL_GAMES) {
            List<MBGameInfo> gameInfoList = (List<MBGameInfo>) data;

            gameAdapter.setGamePlayers(selectedPlayers);
            gameList.addAll(gameInfoList);
            gameAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDataFailure() {
        Utils.showErrorDialog(this, "Players could not be retrived, Please try after some time.");
    }
}

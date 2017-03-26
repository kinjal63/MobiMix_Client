package io.connection.bluetooth.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.connection.bluetooth.Domain.GameInfo;
import io.connection.bluetooth.Domain.NearbyUserInfo;
import io.connection.bluetooth.MobileMeasurementApplication;
import io.connection.bluetooth.R;
import io.connection.bluetooth.adapter.GameAdapter;
import io.connection.bluetooth.adapter.RecyclerItemClickListener;
import io.connection.bluetooth.adapter.UserAdapter;
import io.connection.bluetooth.request.ReqGameInvite;
import io.connection.bluetooth.utils.ApplicationSharedPreferences;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Kinjal on 11/24/2016.
 */
public class UserList extends Activity {
    private RecyclerView recyclerView;
    private Button btnSubmit;
    private List<NearbyUserInfo> userList = new ArrayList<NearbyUserInfo>();

    private Object adapter;
    private boolean isGame = false;

    RecyclerItemClickListener itemClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_info);
        recyclerView = (RecyclerView) this.findViewById(R.id.recycler_view1);
        btnSubmit = (Button) this.findViewById(R.id.btnSubmit);

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
                List<GameInfo> gameInfoList = userList.get(position).getGameInfoList();
                adapter = new GameAdapter(UserList.this, gameInfoList, UserList.this);
                recyclerView.setAdapter((GameAdapter) adapter);
                recyclerView.removeOnItemTouchListener(itemClickListener);
            }
        });

        MobileMeasurementApplication.getInstance().registerActivity(this);
        addOnItemTouchListener();
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getMutualGames(ApplicationSharedPreferences.getInstance(UserList.this).getValue("user_id"));
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
        adapter = new UserAdapter(userList, this);
        recyclerView.setAdapter((UserAdapter) adapter);

        btnSubmit.setVisibility(View.VISIBLE);
    }

    private void getNearByGames() {
        retrofit2.Call<okhttp3.ResponseBody> req1 = MobileMeasurementApplication.getInstance().
                getService().getNearByGameList(ApplicationSharedPreferences.getInstance(UserList.this).getValue("user_id"));

        req1.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String data = response.body().string();
                    System.out.println(data);

                    JSONObject parentJsonObject = new JSONObject(data);
                    JSONArray usrArray = parentJsonObject.getJSONArray("userDetails");

                    for (int ii = 0; ii < usrArray.length(); ii++) {

                        JSONObject jsonObject = (JSONObject) usrArray.get(ii);
                        NearbyUserInfo userInfo = new NearbyUserInfo();

                        userInfo.setUserId(jsonObject.getString("userId"));
                        userInfo.setUserImagePath(jsonObject.getString("userImagePath"));
                        userInfo.setUserFirstName(jsonObject.getString("userFirstName"));
                        userInfo.setUserLastName(jsonObject.getString("userLastName"));

                        JSONArray jsonGameIdArray = jsonObject.getJSONArray("gameId");
                        JSONArray jsonGameNameArray = jsonObject.getJSONArray("gameName");
                        JSONArray jsonGameImagePathArray = jsonObject.getJSONArray("gameImagePath");

                        GameInfo[] gameInfo = new GameInfo[jsonGameIdArray.length()];

                        for (int i = 0; i < gameInfo.length; i++) {
                            gameInfo[i] = new GameInfo();
                            gameInfo[i].setGameId((Integer) jsonGameIdArray.get(i));
                        }
                        for (int j = 0; j < jsonGameNameArray.length(); j++) {
                            gameInfo[j].setGamneName((String) jsonGameNameArray.get(j));
                        }
                        for (int k = 0; k < jsonGameImagePathArray.length(); k++) {
                            gameInfo[k].setGameImagePath((String) jsonGameImagePathArray.get(k));
                        }
                        userInfo.setGameInfoList(Arrays.asList(gameInfo));
                        userList.add(userInfo);
                    }
                    ((UserAdapter) adapter).notifyDataSetChanged();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void getMutualGames(final String userId) {
        final ArrayList<String> remoteUserIds = ((UserAdapter)adapter).getUserIds();

        ReqGameInvite req = new ReqGameInvite(userId, remoteUserIds, 0 );

        Call<ResponseBody> call = MobileMeasurementApplication.getInstance().getService().getMutualGames(req);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String data = response.body().string();
                    System.out.println(data);

                    List<GameInfo> lstGameInfo = new ArrayList<GameInfo>();

                    JSONObject parentJsonObject = new JSONObject(data);
                    JSONArray gameArray = parentJsonObject.getJSONArray("userDetails");

                    for (int ii = 0; ii < gameArray.length(); ii++) {

                        JSONObject jsonObject = (JSONObject) gameArray.get(ii);
                        GameInfo gameInfo = new GameInfo();

                        gameInfo.setGameId(jsonObject.getLong("gameId"));
                        gameInfo.setGameImagePath(jsonObject.getString("gameImagePath"));
                        gameInfo.setGamneName(jsonObject.getString("gameName"));
                        gameInfo.setNetworkType(jsonObject.getInt("gameNetworkType"));
                        gameInfo.setGamePackageName(jsonObject.getString("gamePackageName"));

                        lstGameInfo.add(gameInfo);
                    }

                    isGame = true;

                    adapter = new GameAdapter(UserList.this, lstGameInfo, UserList.this);
                    ((GameAdapter)adapter).setRemoteUserIds(remoteUserIds);
                    recyclerView.setAdapter((GameAdapter) adapter);
                    recyclerView.removeOnItemTouchListener(itemClickListener);

                    btnSubmit.setVisibility(View.GONE);

                } catch (JSONException je) {
                    je.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }

        });
    }

    @Override
    public void onBackPressed() {
        if (isGame) {
            addOnItemTouchListener();
        } else {
            super.onBackPressed();
        }
    }
}

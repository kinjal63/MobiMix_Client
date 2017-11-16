package io.connection.bluetooth.activity.gui;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.connection.bluetooth.Database.DBParams;
import io.connection.bluetooth.Database.DatabaseManager;
import io.connection.bluetooth.Database.action.IDatabaseActionListener;
import io.connection.bluetooth.Domain.GameRequest;
import io.connection.bluetooth.MobiMixApplication;
import io.connection.bluetooth.R;
import io.connection.bluetooth.activity.DialogActivity;
import io.connection.bluetooth.activity.IDBResponse;
import io.connection.bluetooth.core.CoreEngine;
import io.connection.bluetooth.core.EventData;
import io.connection.bluetooth.core.MobiMix;
import io.connection.bluetooth.core.NetworkManager;
import io.connection.bluetooth.core.WifiDirectService;
import io.connection.bluetooth.utils.GameConstants;
import io.connection.bluetooth.utils.NotificationUtil;
import io.connection.bluetooth.utils.UtilsHandler;
import io.connection.bluetooth.utils.cache.MobiMixCache;

/**
 * Created by Kinjal on 10/13/2017.
 */

public class GUIManager {
    private NetworkManager networkManager_;
    private Context context_ = null;
    private static GUIManager guiManager_ = null;

    GUIManager() {
        context_ = MobiMixApplication.getInstance().getContext();
        networkManager_ = NetworkManager.getInstance();
    }

    public static GUIManager getObject() {
        if (guiManager_ == null) {
            guiManager_ = new GUIManager();
        }
        return guiManager_;
    }

    public Handler getHandler() {
        return handler_;
    }

    public void getNearbyPlayers(DBParams params, final IDBResponse IDBResponseListener) {
        networkManager_.getNearByPlayersFromDB(params, new IDatabaseActionListener() {
            @Override
            public void onDataReceived(final List<?> data) {
                MobiMixApplication.getInstance().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        IDBResponseListener.onDataAvailable(MobiMix.DBResponse.DB_RES_FIND_NEARBY_PLAYERS, data);
                    }
                });
            }

            @Override
            public void onDataError() {
                IDBResponseListener.onDataFailure();
            }
        });
    }

    public void getMutualGames(DBParams params, final IDBResponse IDBResponseListener) {
        networkManager_.getMutualGames(params, new IDatabaseActionListener() {
            @Override
            public void onDataReceived(final List<?> data) {
                MobiMixApplication.getInstance().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        IDBResponseListener.onDataAvailable(MobiMix.DBResponse.DB_RES_FIND_MUTUAL_GAMES, data);
                    }
                });
            }

            @Override
            public void onDataError() {
                IDBResponseListener.onDataFailure();
            }
        });
    }

    private Handler handler_ = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.obj != null) {
                JSONObject jsonObject = (JSONObject) msg.obj;
                switch (msg.what) {
                    case MobiMix.GameEvent.EVENT_GAME_INFO_REQUEST:
                        if (jsonObject != null) {
                            GameRequest gameRequest = new GameRequest();
                            gameRequest.setGameName(jsonObject.optString(GameConstants.GAME_NAME));
                            gameRequest.setGameId(jsonObject.optLong(GameConstants.GAME_ID));
                            gameRequest.setGamePackageName(jsonObject.optString(GameConstants.GAME_PACKAGE_NAME));
                            gameRequest.setRemoteUserName(jsonObject.optString(GameConstants.GAME_REQUEST_SENDER_NAME));
                            gameRequest.setRemoteUserId(jsonObject.optString(GameConstants.GAME_REQUEST_SENDER_ID));
                            gameRequest.setConnectionType(jsonObject.optInt(GameConstants.GAME_REQUEST_CONNECTION_TYPE));

                            NotificationUtil.generateNotificationForGameRequest(gameRequest);
                        }
                        break;
                    case MobiMix.GameEvent.EVENT_GAME_UPDATE_TABLE_DATA:
                        if (jsonObject != null) {
                            DBParams params = new DBParams();
                            params.object_ = jsonObject;
                            NetworkManager.getInstance().updateGameTable(params);
                        }
                        break;
                    case MobiMix.GameEvent.EVENT_GAME_LAUNCHED:
                        if (jsonObject != null) {
                            GameRequest game = MobiMixCache.getGameFromCache(jsonObject.optString(GameConstants.USER_ID));
                            if (game != null) {
                                UtilsHandler.launchGame(game.getGamePackageName());

                                // Send Ack for game launch
                                EventData eventData = new EventData(MobiMix.GameEvent.EVENT_GAME_LAUNCHED_ACK);
                                CoreEngine.sendEventToHandler(eventData);
                            }
                        }
                        break;
                    default:
                        break;
                }
            }

            super.handleMessage(msg);
        }
    };

    public void sendEvent(EventData eventData) {
        switch (eventData.event_) {
            case MobiMix.GameEvent.EVENT_GAME_LAUNCHED:
                CoreEngine.sendEventToHandler(eventData);
                break;
            default:
                break;
        }
    }
}

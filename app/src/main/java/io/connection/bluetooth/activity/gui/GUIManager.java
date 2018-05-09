package io.connection.bluetooth.activity.gui;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.Socket;
import java.util.List;

import io.connection.bluetooth.Database.DBParams;
import io.connection.bluetooth.Database.action.IDatabaseActionListener;
import io.connection.bluetooth.Database.entity.MBGameInfo;
import io.connection.bluetooth.Database.entity.MBNearbyPlayer;
import io.connection.bluetooth.Domain.GameRequest;
import io.connection.bluetooth.MobiMixApplication;
import io.connection.bluetooth.actionlisteners.ISocketEventListener;
import io.connection.bluetooth.activity.IDBResponse;
import io.connection.bluetooth.core.CoreEngine;
import io.connection.bluetooth.core.EventData;
import io.connection.bluetooth.core.MobiMix;
import io.connection.bluetooth.core.NetworkManager;
import io.connection.bluetooth.core.WifiDirectService;
import io.connection.bluetooth.utils.ApplicationSharedPreferences;
import io.connection.bluetooth.utils.GameConstants;
import io.connection.bluetooth.utils.NotificationUtil;
import io.connection.bluetooth.utils.Utils;
import io.connection.bluetooth.utils.UtilsHandler;
import io.connection.bluetooth.utils.cache.CacheConstants;
import io.connection.bluetooth.utils.cache.MobiMixCache;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;

/**
 * Created by Kinjal on 10/13/2017.
 */

public class GUIManager {
    private NetworkManager networkManager_;
    private Context context_ = null;
    private static GUIManager guiManager_ = null;
    private ISocketEventListener socketEventListener;

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

    public void setSocketEventListener(ISocketEventListener socketEventListener) {
        this.socketEventListener = socketEventListener;
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
            public void onDataUpdated() {

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
            public void onDataUpdated() {

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
                final JSONObject jsonObject = (JSONObject) msg.obj;
                switch (msg.what) {
                    // Socket initilization and diconnection events
                    case MobiMix.GameEvent.EVENT_SOCKET_INITIALIZED:
                        String sockAddr = jsonObject.optString(GameConstants.CLIENT_SOCKET_ADDRESS);
                        if (socketEventListener != null) {
                            socketEventListener.socketInitialized(sockAddr);
                        }
                        break;
                    case MobiMix.GameEvent.EVENT_SOCKET_DISCONNECTED:
                        if (socketEventListener != null) {
                            socketEventListener.socketDiconnected();
                        }
                        break;
                    case MobiMix.GameEvent.EVENT_GAME_INFO_REQUEST:
                        if (jsonObject != null) {
                            GameRequest gameRequest = new GameRequest();
                            gameRequest.setGameName(jsonObject.optString(GameConstants.GAME_NAME));
                            gameRequest.setGameId(jsonObject.optLong(GameConstants.GAME_ID));
                            gameRequest.setGamePackageName(jsonObject.optString(GameConstants.GAME_PACKAGE_NAME));
                            gameRequest.setRequesterUserName(jsonObject.optString(GameConstants.GAME_REQUEST_SENDER_NAME));
                            gameRequest.setRequesterUserId(jsonObject.optString(GameConstants.GAME_REQUEST_SENDER_ID));
                            gameRequest.setConnectionType(jsonObject.optInt(GameConstants.GAME_REQUEST_CONNECTION_TYPE));
                            gameRequest.setWifiAddress(jsonObject.optString(GameConstants.GAME_REQUEST_DEVICE_NAME));
                            gameRequest.setBluetoothAddress(jsonObject.optString(GameConstants.GAME_REQUEST_DEVICE_NAME));

                            NotificationUtil.generateNotificationForGameRequest(gameRequest);
                        }
                        break;
                    case MobiMix.GameEvent.EVENT_GAME_LAUNCHED:
                        GameRequest game = MobiMixCache.getCurrentGameRequestFromCache();
                        if (game != null) {
                            boolean isForeGround = false;
                            try {
                                isForeGround = new ForegroundCheckTask(context_).execute(game.getGamePackageName()).get();
                                if (!isForeGround) {
                                    UtilsHandler.launchGame(game.getGamePackageName());
                                }

                                if (jsonObject != null && Utils.isGroupOwner()) {
                                    String userId = ApplicationSharedPreferences.getInstance(
                                            MobiMixApplication.getInstance().getContext()).getValue("user_id");

                                    jsonObject.put(GameConstants.GROUP_OWNER_USER_ID, userId);
                                    jsonObject.put(GameConstants.CONNECTED_USER_ID, jsonObject.optString(GameConstants.USER_ID));
                                    jsonObject.put(GameConstants.GAME_CONNECTION_TYPE, game.getConnectionType());
                                    jsonObject.put(GameConstants.GAME_MAX_PLAYERS, 3);

                                    DBParams params = new DBParams();
                                    params.object_ = jsonObject;
                                    params.event_ = MobiMix.DBRequest.DB_UPDATE_GAME_TABLE;
                                    NetworkManager.getInstance().updateGameTable(params);
                                }

//                                List<Socket> clients = MobiMixCache.getClientSockets();
//                                for(Socket client : clients) {
//                                    EventData eventData2 = new EventData(MobiMix.GameEvent.EVENT_GAME_UPDATE_TABLE_DATA);
//                                    CoreEngine.sendEventToHandler(eventData2);
//                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case MobiMix.GameEvent.EVENT_GAME_LAUNCHED_ACK:
                        if (Utils.isGroupOwner() && jsonObject != null) {
                            DBParams params = new DBParams();
                            params.object_ = jsonObject;
                            params.event_ = MobiMix.DBRequest.DB_UPDATE_GAME_TABLE;
                            NetworkManager.getInstance().updateGameTable(params);

                            if(Utils.isGroupOwner()) {
                                GameRequest gameRequest = MobiMixCache.getCurrentGameRequestFromCache();
                                UtilsHandler.launchGame(gameRequest.getGamePackageName());
                            }
                        }
                        break;
                    case MobiMix.GameEvent.EVENT_GAME_READ_TABLE_DATA:
                        DBParams dbParams = new DBParams();
                        dbParams.event_ = MobiMix.DBRequest.DB_READ_GAME_TABLES;
                        dbParams.object_ = jsonObject;

                        NetworkManager.getInstance().getGameTableData(dbParams, new IDatabaseActionListener() {
                            @Override
                            public void onDataReceived(List<?> data) {
                                EventData eventObj = new EventData();

                                JSONObject jsonObj = new JSONObject();
                                try {
                                    jsonObj.put(GameConstants.GAME_UPDATE_TABLE_DATA, data);

                                    eventObj.event_ = MobiMix.GameEvent.EVENT_GAME_UPDATE_TABLE_DATA;
                                    eventObj.object_ = jsonObj;

                                    WifiDirectService.getInstance(context_).sendEvent(eventObj);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onDataUpdated() {

                            }

                            @Override
                            public void onDataError() {

                            }
                        });
                        break;
                    case MobiMix.GameEvent.EVENT_GAME_UPDATE_TABLE_DATA:
                        DBParams params = new DBParams();
                        params.event_ = MobiMix.DBRequest.DB_UPDATE_GAME_TABLE_BATCH;
                        params.object_ = jsonObject;

                        NetworkManager.getInstance().updateGameTableInBatch(params, new IDatabaseActionListener() {
                            @Override
                            public void onDataUpdated() {
                                WifiDirectService.getInstance(context_).sendEvent(new EventData(MobiMix.GameEvent.EVENT_GAME_UPDATE_TABLE_ACK));

                                String gamePackageName = jsonObject.optString(GameConstants.GAME_PACKAGE_NAME);
                                UtilsHandler.launchGame(gamePackageName);
                            }

                            @Override
                            public void onDataReceived(List<?> data) {

                            }

                            @Override
                            public void onDataError() {

                            }
                        });


                        break;
                    case MobiMix.GameEvent.EVENT_GAME_CONNECTION_CLOSED:
                        DBParams params1 = new DBParams();
                        params1.event_ = MobiMix.DBRequest.DB_DELETE_GAME_PARTICIPANTS;
                        NetworkManager.getInstance().deleteGameParticipants(params1);
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
            // Game related events
            case MobiMix.GameEvent.EVENT_GAME_LAUNCHED:
                CoreEngine.sendEventToHandler(eventData);
                break;
            case MobiMix.GameEvent.EVENT_GAME_REQUEST_TO_USERS:
            case MobiMix.GameEvent.EVENT_GAME_REQUEST_TO_QUEUED_USERS:
                CoreEngine.sendEventToRadioService(eventData);
                break;
            default:
                break;
        }
    }
}

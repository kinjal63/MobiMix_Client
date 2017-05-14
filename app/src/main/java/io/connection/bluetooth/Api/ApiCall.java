package io.connection.bluetooth.Api;

import java.util.HashSet;
import java.util.List;

import io.connection.bluetooth.Domain.DeviceDetails;
import io.connection.bluetooth.Domain.GameConnectionInfo;
import io.connection.bluetooth.Domain.GameProfile;
import io.connection.bluetooth.Domain.User;
import io.connection.bluetooth.request.ReqGameInvite;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by songline on 31/07/16.
 */
public interface ApiCall {


    @POST("login")
    Call<User> login(@Body User userLogin);

    @POST("signup")
    Call<User> signup(@Body User userSignup);

    @POST("search")
    Call<User> isAvailable(@Body User userAvailable);

    @POST("pushRegister")
    Call<Object> sendPushTokenToServer(@Body DeviceDetails deviceDetails);

    @POST("addGameToProfile")
    Call<Object> addGameToProfileByUser(@Body GameProfile gameProfile);

    @POST("addGameRequestToLibrary")
    Call<Object> addGameProfileRequestToLibrary(@Body GameProfile gameProfile);

    @GET("getGamesForAddToProfile")
    Call<List<String>> getGamesForAddToProfile(@Query("userId") String userId, @Query("packageList") List<String> packageList);

    @GET("getGameProfileByUser")
    Call<List<GameProfile>> getGameProfileByUserId(@Query("userId") String userId);

    @GET("getNearByUserGames")
    Call<User> getNearByUserGames(@Query("userId") String userId, @Query("bluetoothAddress") String deviceId, @Query("packageList") List<String> packageList);

    @POST("updateGamePlayingTime")
    Call<User> updateGamePlayingTime(@Body User userData);

    @POST("updateUserLocation")
    Call<HashSet> updateUserLocation(@Body User userData);

    @POST("updateUserLocation")
    Call<ResponseBody> updateUserLocation(@Query("userId") String userId, @Query("latitude") double latitude, @Query("longitude") double longitude);

    @GET("updateGameProfiles")
    Call<User> updateGameProfiles(@Query("userId") String userId, @Query("installedPackageName") List<String> installedPackageName);

    @POST("getNearByGameList")
    Call<ResponseBody> getNearByGameList(@Query("user_id") String userId);

    @POST("getMutualGames")
    Call<ResponseBody> getMutualGames(@Body ReqGameInvite reqGameInvite);

    @POST("sendRemoteUserInput")
    Call<ResponseBody> sendRemoteUserInput(@Query("userId") String userId, @Query("toUserId") String toUserId,
                                           @Query("bluetoothAddress") String bluetoothAddress,
                                           @Query("accept") boolean accept);

    @POST("sendConnectionInvite")
    Call<ResponseBody> sendConnectionInvite(@Body ReqGameInvite reqGameInvite);

    @POST("addUserTime")
    Call<ResponseBody> addUserTime(@Query("userId") String userId, @Query("fromTime") String fromTime,
                                   @Query("toTime") String toTime);

    @POST("updateGameConnectionInfo")
    Call<ResponseBody> updateConnectionInfo(@Body GameConnectionInfo connectionInfo);
}


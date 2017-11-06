package io.connection.bluetooth.activity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.connection.bluetooth.Api.ApiCall;
import io.connection.bluetooth.Api.ApiClient;
import io.connection.bluetooth.Domain.DeviceDetails;
import io.connection.bluetooth.Domain.User;
import io.connection.bluetooth.MobiMixApplication;
import io.connection.bluetooth.R;
import io.connection.bluetooth.utils.ApplicationSharedPreferences;
import io.connection.bluetooth.utils.Constants;
import io.connection.bluetooth.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by songline on 31/07/16.
 */
public class LoginActivity extends Fragment {
    @InjectView(R.id.login_email)
    EditText email;
    @InjectView(R.id.login_password)
    EditText password;
    @InjectView(R.id.login_login)
    Button login;
    ApiCall apiCall;
    private static final String TAG = "LoginActivity";
    final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public Context context;
    SharedPreferences preferences;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        context = getActivity();
        View view = inflater.inflate(R.layout.activity_login, container, false);
        preferences = getActivity().getSharedPreferences(Constants.LOGIN, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        ButterKnife.inject(getActivity());
        password = (EditText) view.findViewById(R.id.login_password);
        email = (EditText) view.findViewById(R.id.login_email);
        apiCall = ApiClient.getClient().create(ApiCall.class);
        login = (Button) view.findViewById(R.id.login_login);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String macAddress = bluetoothAdapter.getAddress();
                if (!validate()) {
                    login.setEnabled(true);
                    return;
                }

                User user = new User();
                user.setEmail(email.getText().toString());
                user.setPassword(password.getText().toString());
                DeviceDetails deviceDetails = new DeviceDetails();
                deviceDetails.setDeviceId(Utils.getDeviceId(context));
                user.setMacAddress(macAddress);
                List<DeviceDetails> deviceDetailsList = new ArrayList<>();
                deviceDetailsList.add(deviceDetails);
                user.setUserDeviceDetailses(deviceDetailsList);
                Toast.makeText(context, "Click Login " + user.getUserDeviceDetailses().get(0).getDeviceId(), Toast.LENGTH_LONG).show();

                login.setEnabled(false);
                final ProgressDialog progressDialog = new ProgressDialog(getActivity(),
                        R.style.AppTheme_Dark_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Authenticating...");
                progressDialog.show();

                Log.d(TAG, "onClick: " + macAddress + "   " + password.getText().toString());

                if (Utils.isConnected(context)) {

                    Call<User> result = apiCall.login(user);
                    result.enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {

                            if (response.code() == 200) {
                                User userObject = response.body();
                                System.out.println(userObject.getId());
                                editor.putString(Constants.LOGIN_KEY, userObject.getId());
                                editor.putString(Constants.NAME_KEY, userObject.getName());
                                editor.putString(Constants.EMAIL_KEY, userObject.getEmail());
                                editor.putLong(Constants.DOB_KEY, userObject.getDob());
                                bluetoothAdapter.setName(userObject.getName());

                                editor.commit();
                                editor.apply();
                                preferences = getActivity().getSharedPreferences(Constants.GAME, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editorGameTime = preferences.edit();
//                                for (GameProfileTimeDetails gameProfileTimeDetails : userObject.getGameProfileTime()) {
//
//                                    long startTime = gameProfileTimeDetails.getGameProfileStartTime();
//                                    Date date = new Date();
//                                    date.setTime(startTime);
//                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//                                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
//                                    Date gmt = new Date(sdf.format(date));
//                                    System.out.println(" db  start time " + startTime);
//                                    startTime += Math.abs(gmt.getTime() - startTime);
//                                    System.out.println(" device  start time " + startTime);
//                                    long startHour = TimeUnit.MILLISECONDS.toHours(startTime) % 24;
//                                    long startMinute = TimeUnit.MILLISECONDS.toMinutes(startTime) % 60;
//                                    long endTime = gameProfileTimeDetails.getGameProfileEndTime();
//                                    date.setTime(endTime);
//                                    gmt = new Date(sdf.format(date));
//                                    System.out.println(" db  end time " + endTime);
//                                    endTime += Math.abs(gmt.getTime() - endTime);
//                                    System.out.println(" device  end time " + endTime);
//                                    long endHour = TimeUnit.MILLISECONDS.toHours(endTime) % 24;
//                                    long endMinute = TimeUnit.MILLISECONDS.toMinutes(endTime) % 60;
//                                    String gameTime = startHour + ":" + startMinute + " - " + endHour + ":" + endMinute;
//                                    editorGameTime.putString("game_time_" + gameProfileTimeDetails.getGameProfileTimeSchedule().toLowerCase(), gameTime);
//                                }
//
                                editorGameTime.commit();
                                editorGameTime.apply();
                                getActivity().finish();

                                ApplicationSharedPreferences.getInstance(MobiMixApplication.getInstance().getContext()).
                                        addValue("user_id", userObject.getId());
                                ApplicationSharedPreferences.getInstance(getActivity()).addValue("email", userObject.getEmail());
                                ApplicationSharedPreferences.getInstance(getActivity()).addValue("user_name", userObject.getName());
                                ApplicationSharedPreferences.getInstance(getActivity()).addBooleanValue("is_login", true);

                                preferences = context.getSharedPreferences("myPref", Context.MODE_PRIVATE);
                                preferences.edit().putBoolean("is_login", true).commit();


                                Intent intent = new Intent(getActivity(), Home_Master.class);
                                startActivity(intent);
                            } else if (response.code() == 204) {
                                // no credintial found
                                Toast toast = Toast.makeText(context, "Email or Password are Incorrect", Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                                password.setText("");
                                toast.show();

                            } else if (response.code() == 500) {
                                // Internal server error
                                Toast.makeText(context, "Internal Server Error", Toast.LENGTH_LONG).show();


                            }
                            progressDialog.dismiss();
                            login.setEnabled(true);

                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {


                            Log.d(TAG, "onFailure: " + t.getMessage());
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Login Failed", Toast.LENGTH_SHORT).show();
                            login.setEnabled(true);

                        }
                    });

                } else {
                    Toast.makeText(context, Constants.INTERNET_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
                }
            }
        });


        return view;
    }


    public boolean validate() {
        boolean valid = true;


        String user_password = password.getText().toString();
        if (user_password.isEmpty() || user_password.length() < 3) {
            password.setError("at least 3 characters");
            valid = false;
        } else {
            password.setError(null);
        }

        return valid;
    }


}

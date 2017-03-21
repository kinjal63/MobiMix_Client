package io.connection.bluetooth.activity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import butterknife.InjectView;
import io.connection.bluetooth.Api.ApiCall;
import io.connection.bluetooth.Api.ApiClient;
import io.connection.bluetooth.Domain.DeviceDetails;
import io.connection.bluetooth.Domain.User;
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
public class SignupActivity extends Fragment implements DatePickerDialog.OnDateSetListener {
    @InjectView(R.id.input_name)
    EditText name;
    @InjectView(R.id.input_password)
    EditText password;
    @InjectView(R.id.input_email)
    EditText email;
    @InjectView(R.id.input_dob)
    EditText dob;
    ApiCall apiCall;
    Button _signup;
    Date dobObj;
    DatePickerDialog dpd;
    private static final String TAG = "SignupActivity";
    public Context context;
    private long dateOfBirth;
    private SharedPreferences sharedPref;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        context = getActivity();
        View view = inflater.inflate(R.layout.activity_signup, container, false);
        apiCall = ApiClient.getClient().create(ApiCall.class);
        name = (EditText) view.findViewById(R.id.input_name);
        password = (EditText) view.findViewById(R.id.input_password);
        email = (EditText) view.findViewById(R.id.input_email);
        dob = (EditText) view.findViewById(R.id.input_dob);
        dob.setInputType(InputType.TYPE_NULL);
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
            Intent enableBlueTooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBlueTooth, 1);
        }


        _signup = (Button) view.findViewById(R.id.register_register);
        _signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "signUp");
                if (!validate()) {
                    onSignupFailed();
                    return;
                }

                String macAddress = android.provider.Settings.Secure.getString(getActivity().getContentResolver(), "bluetooth_address");

                final User user = new User();
                user.setName(name.getText().toString());
                user.setPassword(password.getText().toString());
                user.setMacAddress(macAddress);
                user.setEmail(email.getText().toString());
                user.setDob(dateOfBirth);
                user.setGender("male");
                user.setEmailVerified(false);
                DeviceDetails deviceDetails = new DeviceDetails();
                deviceDetails.setDeviceId(Utils.getDeviceId(context));
                List<DeviceDetails> deviceDetailsList = new ArrayList<DeviceDetails>();
                deviceDetailsList.add(deviceDetails);
                user.setUserDeviceDetailses(deviceDetailsList);
//                _signup.setEnabled(false);

                final ProgressDialog progressDialog = new ProgressDialog(getActivity(),
                        R.style.AppTheme_Dark_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Registration...");
                progressDialog.show();
                if (Utils.isConnected(context)) {
                    final Call<User> result = apiCall.signup(user);
                    result.enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            if (response.isSuccessful()) {
                                sharedPref = context.getSharedPreferences("myPref", Context.MODE_PRIVATE);
                                ApplicationSharedPreferences.getInstance(getActivity()).addValue("user_id", response.body().getId());
                                ApplicationSharedPreferences.getInstance(getActivity()).addValue("email", response.body().getEmail());

                                sharedPref.edit().putBoolean("is_login", true).commit();

                                onSignupSuccess();
                                Toast.makeText(getActivity(), "Registered Successfully", Toast.LENGTH_SHORT).show();
                            } else if (response.code() == 409) {
                                conflictEmail();
                                _signup.setEnabled(true);
                                Toast.makeText(getActivity(), "Email-Id Already Exist", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), "Registration Failed ! Try Again ", Toast.LENGTH_SHORT).show();
                            }

                            progressDialog.dismiss();
                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
                            t.printStackTrace();
                            Log.d(TAG, "onFailure: " + t.getMessage());
                            progressDialog.dismiss();
                            //  Toast.makeText(getActivity(), "Server Error", Toast.LENGTH_SHORT).show();

                        }
                    });
                }else{
                    Toast.makeText(context, Constants.INTERNET_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
                }


            }
        });

        Calendar now = Calendar.getInstance();
        dpd = DatePickerDialog.newInstance(
                SignupActivity.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );

        dob.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dpd.show(getActivity().getFragmentManager(), "Date Of Birth");
                }
            }
        });
        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dpd.show(getActivity().getFragmentManager(), "Date Of Birth");
            }
        });

        dpd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Log.d(TAG, "onCancel: Dialog Cancel");
            }
        });


        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: " + resultCode);
        if (requestCode == 1 && resultCode == -1) {
            Toast.makeText(context, "Bluetooth Turned on Successfully ", Toast.LENGTH_LONG).show();
        } else {

            Toast.makeText(context, "Please Turn On Bluetooth ", Toast.LENGTH_LONG).show();

        }

    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String dateOfbirth = (monthOfYear + 1) + "/" + dayOfMonth + "/" + year;
        java.text.DateFormat
                df = new SimpleDateFormat("MM/dd/yyyy");

        try {
            dobObj = df.parse(dateOfbirth);
            String newDateString = df.format(dobObj);
            Calendar calendar = new GregorianCalendar(TimeZone.getDefault());

            calendar.setTime(dobObj);
            dob.setText(newDateString);
            dateOfBirth = calendar.getTime().getTime();
            System.out.println(newDateString + "  " + calendar.getTime().getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void onSignupSuccess() {
//        _signup.setEnabled(true);
//        email.setText("");
//        name.setText("");
//        password.setText("");
//        dob.setText("");
//        System.out.println(getActivity().getParent() == null);
//        ((TabLayout) getActivity().findViewById(R.id.tabs_login_register)).getTabAt(0).select();

        startActivity(new Intent(getActivity(), Home_Master.class));
    }

    public void onSignupFailed() {
        _signup.setEnabled(true);
    }

    public void conflictEmail() {
        email.setText("");
        email.setError("Email-Id Already Exist");
        email.setFocusable(true);
        _signup.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String user_name = name.getText().toString();
        String user_password = password.getText().toString();
        String user_email = email.getText().toString();
        String user_dob = dob.getText().toString();
        Log.d(TAG, "validate:  " + user_dob);

        if (user_name.isEmpty() || user_name.length() < 3) {
            name.setError(" Name Must Be Atleast 3 Characters");
            valid = false;
        } else {
            name.setError(null);
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(user_email).matches()) {
            email.setError("Invalid Email");
            valid = false;
        } else {
            email.setError(null);
        }

        if (user_password.isEmpty() || user_password.length() < 6) {
            password.setError("Password Must Be Atleast 6 Characters");
            valid = false;
        } else {
            password.setError(null);
        }

        if (user_dob.isEmpty()) {
            dob.setError("Date Of Birth Cannot be Empty");
            valid = false;
        } else {
            dob.setError(null);
        }

        return valid;

    }


}

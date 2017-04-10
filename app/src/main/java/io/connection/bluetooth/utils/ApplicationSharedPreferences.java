package io.connection.bluetooth.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Kinjal on 12/28/2016.
 */

public class ApplicationSharedPreferences {
    private Context context;
    private static ApplicationSharedPreferences instance;
    private SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private static String MyPREFERENCES = "Preferences_Mobile";

    ApplicationSharedPreferences(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static ApplicationSharedPreferences getInstance(Context context) {
        if( instance == null ) {
            instance = new ApplicationSharedPreferences(context);
        }
        return instance;
    }

    public void addValue( String name, String value ) {
        editor.putString(name, value);
        editor.commit();
    }

    public void addLongValue( String name, long value ) {
        editor.putLong(name, value);
        editor.commit();
    }

    public void addBooleanValue( String name, boolean value ) {
        editor.putBoolean(name, value);
        editor.commit();
    }

    public String getValue(String name) {
        return sharedPreferences.getString(name, "");
    }

    public long getLongValue(String name) {
        return sharedPreferences.getLong(name, 0);
    }

    public boolean getBooleanValue(String name) {
        return sharedPreferences.getBoolean(name, false);
    }
}

package com.game.dhanraj.myownalexa.sharedpref;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.util.UUID;

import okhttp3.OkHttpClient;

/**
 * Created by Dhanraj on 30-05-2017.
 */

public class Util {

    private static SharedPreferences mPreferences;
    private static final String IDENTIFIER = "identifier";
    private static OkHttpClient client;

    public static SharedPreferences getPrefernces(Context context){
        if(mPreferences==null)
        {
            mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
        return mPreferences;
    }

    private static String getIdentifier() {
        return (mPreferences!=null)?mPreferences.getString(IDENTIFIER,""):"";
    }

    public static String getUuid() {
        String prefix=(TextUtils.isEmpty(getIdentifier()))?"":getIdentifier()+".";
        return prefix + UUID.randomUUID().toString();
    }

    public static OkHttpClient getOkhttp() {
        if (client == null)
            client = new OkHttpClient();
        return client;
    }


}

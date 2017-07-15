package com.game.dhanraj.myownalexa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Dhanraj on 01-06-2017.
 */

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        //start our service in the background
        Intent stickyIntent = new Intent(context, DownChannel.class);
        context.startService(stickyIntent);
        Log.i(TAG, "Started down channel service.");
    }
}

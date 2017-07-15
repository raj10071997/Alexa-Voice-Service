package com.game.dhanraj.myownalexa.Alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Dhanraj on 10-06-2017.
 */

public class AlarmReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("we are in the receiver","hope ki chal jaye");

        String alrm = intent.getExtras().getString("alarm");

        Intent i = new Intent(context,AlarmService.class);
        i.putExtra("extra",alrm);
        context.startService(i);

    }
}

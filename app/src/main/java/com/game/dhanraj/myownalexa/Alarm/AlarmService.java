package com.game.dhanraj.myownalexa.Alarm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.game.dhanraj.myownalexa.R;
import com.game.dhanraj.myownalexa.SendingAudio;

/**
 * Created by Dhanraj on 10-06-2017.
 */

public class AlarmService extends Service {

    private MediaPlayer mediaPlayer;
    private int startId;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {



       // this.startId=0;
        String state = null;
        if(intent.getExtras().getString("extra")!=null)
         state = intent.getExtras().getString("extra");



      //  assert state !=null;
        switch(state){
            case "alarm off":
                this.startId = 0;
                break;
            case "alarm on":
                this.startId = 1;
            default:
                this.startId = 0;
                break;
        }

        if( this.startId==0)
        {
            mediaPlayer.stop();
            mediaPlayer.reset();

            this.startId=0;
        }else if( this.startId==1){
            mediaPlayer = MediaPlayer.create(this, R.raw.alarm);
            mediaPlayer.start();

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Intent i = new Intent(this.getApplicationContext(), SendingAudio.class);
            //FLAG_UPDATE_CURRENT use kia tha
            PendingIntent pendingIntent = PendingIntent.getActivity(AlarmService.this,0,i,0);

            Notification notification_popu = new Notification.Builder(this)
                    .setContentTitle("Turn off the alarm!!")
                    .setContentText("Click me !")
                    .setSmallIcon(R.drawable.ic_alarm_off_black_24dp)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();

            notificationManager.notify(0,notification_popu);
        }

        return START_NOT_STICKY;
    }
}

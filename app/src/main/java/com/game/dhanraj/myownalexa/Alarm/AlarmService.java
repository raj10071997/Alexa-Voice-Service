package com.game.dhanraj.myownalexa.Alarm;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.game.dhanraj.myownalexa.DatabaseForAlarmAndTimer.DataBase;
import com.game.dhanraj.myownalexa.R;
import com.game.dhanraj.myownalexa.sharedpref.Util;

/**
 * Created by Dhanraj on 10-06-2017.
 */

public class AlarmService extends Service {

    private MediaPlayer mediaPlayer;
    private int startId;
    private DataBase db;
    private AlarmManager alarmManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int fakeId=0;
        db = new DataBase(AlarmService.this);
        alarmManager =  (AlarmManager) getSystemService(ALARM_SERVICE);
        String state = null;
        if(intent.getExtras().getString("extra")!=null)
         state = intent.getExtras().getString("extra");

        if(state.equals("alarm on"))
            fakeId=1;
        else
            fakeId=0;

        if(fakeId==0) {
            if(mediaPlayer!=null) {
                mediaPlayer.stop();
                mediaPlayer.reset();
            }

            final int idtocancelled = db.getTime();
            int lastId = db.getLastRowID();
            Intent i = new Intent(AlarmService.this, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(AlarmService.this,idtocancelled,i,PendingIntent.FLAG_CANCEL_CURRENT);

            alarmManager.cancel(pendingIntent);

            db.deleteTimeRow(idtocancelled);

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AlarmService.this, "Alarm has been cancelled !" +" "+String.valueOf(idtocancelled), Toast.LENGTH_SHORT).show();
                }
            });
        }
        if (fakeId==1) {
            String ringName = Util.getPrefernces(getApplicationContext()).getString("SelectedRingtone", "alarm.mp3");
            int iend = ringName.indexOf(".");
            if(iend!=-1)
                ringName = ringName.substring(0,iend);

            int ringtoneName = getResources().getIdentifier(ringName, "raw", getPackageName());
            mediaPlayer = MediaPlayer.create(this, ringtoneName);
            mediaPlayer.start();

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Intent i = new Intent(AlarmService.this, AlarmReceiver.class);
            i.putExtra("alarm","alarm off");
            int idtocancelled = db.getTime();
            PendingIntent pendingIntent = PendingIntent.getBroadcast(AlarmService.this,idtocancelled,i,PendingIntent.FLAG_CANCEL_CURRENT);

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

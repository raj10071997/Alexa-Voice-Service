package com.game.dhanraj.myownalexa;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.game.dhanraj.myownalexa.Alarm.AlarmReceiver;
import com.game.dhanraj.myownalexa.Alarm.MyAlarm;
import com.game.dhanraj.myownalexa.DatabaseForAlarmAndTimer.DataBase;
import com.game.dhanraj.myownalexa.sharedpref.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSource;

import static com.game.dhanraj.myownalexa.sharedpref.Util.getOkhttp;


/**
 * Created by Dhanraj on 01-06-2017.
 */

public class DownChannel extends Service {

    private String DchannelURL = "https://avs-alexa-eu.amazon.com/v20160207/directives";
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    public static Boolean bool ;
    public static MyAlarm myAlarm;
    public AlarmManager alarmManager;
    public Calendar calendar;
    public SendingAudio sendingAudio;
    public PendingIntent pending;
    public Calendar calendar2;
    private DataBase db;
    public TokenHandler tokenHandler;

    IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public DownChannel getServerInstance() {
            return DownChannel.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("gat", "Launched");
        tokenHandler.getAccessToken(TokenHandler.DownChannelCase1);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        alarmManager =  (AlarmManager) getSystemService(ALARM_SERVICE);
        calendar = Calendar.getInstance();
        sendingAudio  = new SendingAudio();
        calendar2 = Calendar.getInstance();

        EventBus.getDefault().register(this);

        tokenHandler = new TokenHandler(this);
        db = new DataBase(DownChannel.this);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(MessageEvent event){
        switch (event.event){
            case TokenHandler.DownChannelCase1:
                openDownChannel(event.message);
                break;
            case TokenHandler.DownChannelCase2:
                SendSynchronizeEvent(event.message);
                break;
            case TokenHandler.DownChannelCase3:
                SendPingRequest(event.message);
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);

    }

    public void openDownChannel(final String accessToken) {
        OkHttpClient client2 = getOkhttp();
        OkHttpClient client=  client2.newBuilder()
               .connectTimeout(0, TimeUnit.MILLISECONDS)
               .readTimeout(0, TimeUnit.MILLISECONDS)
               .writeTimeout(0, TimeUnit.MILLISECONDS)
               .build();
       // String accesstoken = MainActivity.getAccessToken(DownChannel.this);

        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(accessToken==null) {
            Log.d("openDownChannel","failed");
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(DownChannel.this, "You Login has been failed because accessToken string is null",
                            Toast.LENGTH_SHORT).show();
                }
            });

            Intent i = new Intent(DownChannel.this,MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            stopSelf();

           // tokenHandler.getAccessToken(TokenHandler.DownChannelCase1);

           /*openDownChannel();
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(DownChannel.this, "Loginfailed", Toast.LENGTH_SHORT).show();
                }
            });
            openDownChannel(accessToken);


            MainActivity main = new MainActivity();
            main.intiLogi();*/
        } else {
            final Request request = new Request.Builder()
                    .url(DchannelURL)
                    .get()
                    .addHeader("authorization","Bearer "+accessToken)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("downchannelfailed","failureagain");
                    openDownChannel(accessToken);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                     new AsyncTask<Void, Void, Void>(){
                        @Override
                        protected Void doInBackground(Void... params) {
                          //  SendSynchronizeEvent();
                            tokenHandler.getAccessToken(TokenHandler.DownChannelCase2);
                            return null;
                        }

                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DownChannel.this, "DownChannel has been established", Toast.LENGTH_SHORT).show();
                        }
                    });
                    BufferedSource bufferedSource = response.body().source();
                    try {
                        while (!bufferedSource.exhausted()) {
                        String line = bufferedSource.readUtf8Line();
                       // bufferedSource.read(buffer, 8192);
                         //   stringBuilder.append(line);
                             boolean checkJson = isJSONValid(line);
                                    if(checkJson) {
                                        try {
                                            JSONObject objectDirective = new JSONObject(line);
                                            JSONObject directive = (JSONObject) objectDirective.get("directive");
                                            JSONObject header = (JSONObject) directive.get("header");
                                            JSONObject payload = (JSONObject) directive.get("payload");
                                            String namespace = header.getString("namespace");
                                            String name = header.getString("name");
                                            if(name.equals("StopCapture") && namespace.equals("SpeechRecognizer")) {
                                                sendMessage();
                                            } else if (name.equals("SetAlert") && namespace.equals("Alerts")) {
                                                String type =  payload.getString("type");
                                                if(type.equals("ALARM")) {
                                                    Intent intet = new Intent(DownChannel.this, AlarmReceiver.class);
                                                    String scheduledTime = payload.getString("scheduledTime");
                                                    String TYPE = payload.getString("type");
                                                    Log.d("time",scheduledTime);
                                                    DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
                                                    try {
                                                        Date dhanraj = fmt.parse(scheduledTime);
                                                        Long time =  dhanraj.getTime();
                                                        Log.d("Mytime",time.toString());
                                                        calendar.setTimeInMillis(time);
                                                        //for setting new alarm
                                                        int request = (int) (time/1000);
                                                        int hour = calendar.get(Calendar.HOUR);
                                                        int minute = calendar.get(Calendar.MINUTE);
                                                        int year = calendar.get(Calendar.YEAR);
                                                        int AMorPM = calendar.get(Calendar.AM_PM);
                                                        String amOrpm = AMorPM==1? "pm":"am";
                                                        String min = String.valueOf(minute);
                                                        if(minute/10==0) {
                                                          min = "0"+min;
                                                        }
                                                        String myTime = String.valueOf(hour)+" : "+min + "  "+ amOrpm;
                                                        intet.putExtra("alarm","alarm on");
                                                        db.addAlarm(myTime,TYPE,calendar.getTimeInMillis());
                                                        int lastId = db.getLastRowID();
                                                        Log.d("lastrowID", String.valueOf(lastId));
                                                        Log.d("wholeTime",hour+","+minute+","+year+","+calendar.get(Calendar.AM_PM));
                                                        pending = PendingIntent.getBroadcast(DownChannel.this,lastId,intet,PendingIntent.FLAG_UPDATE_CURRENT);
                                                        alarmManager.setExact(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pending);
                                                    } catch (ParseException e) {
                                                        e.printStackTrace();
                                                        Log.d("ParsingException","DownnChannelException");
                                                    }
                                                } else if(type.equals("TIMER")) {

                                                }
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                            Log.d("alarm",line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        //code has been changed here
                        tokenHandler.getAccessToken(TokenHandler.DownChannelCase1);
                    }
                }
            });
        }
    }

    public boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            // edited, to include @Arthur's comment
            // e.g. in case JSONArray is valid as well...
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    private void SendSynchronizeEvent(final String accessToken) {

        JSONObject p = new JSONObject();
        JSONObject event = new JSONObject();
        JSONObject header = new JSONObject();
        JSONObject payload = new JSONObject();
        JSONArray context = new JSONArray();
        JSONObject AudioPlayer = new JSONObject();
        JSONObject audioHeader = new JSONObject();
        JSONObject audioPayload = new JSONObject();
        JSONObject AlertState = new JSONObject();
        JSONObject alertHeader = new JSONObject();
        JSONObject alertPayload = new JSONObject();
        JSONArray  acitveAlerts = new JSONArray();
        JSONArray  allAlerts = new JSONArray();
        JSONObject SpeakerState = new JSONObject();
        JSONObject speakerHeader = new JSONObject();
        JSONObject speakerPayload = new JSONObject();
        JSONObject SpeechSynthesizer = new JSONObject();
        JSONObject speechSynthesizerHeader = new JSONObject();
        JSONObject speechSynthesizerPayload = new JSONObject();
        JSONObject SpeechRecognizer = new JSONObject();
        JSONObject speechRecognizerHeader = new JSONObject();
        JSONObject speechRecognizerPayload = new JSONObject();

        try {
//            context.put("Alerts.AlertsState");
//            context.put("AudioPlayer.PlaybackState");
//            context.put("Speaker.VolumeState");
//            context.put("SpeechSynthesizer.SpeechState");
//            context.put("SpeechRecognizer.RecognizerState");
            AudioPlayer.put("header",audioHeader);
            AudioPlayer.put("payload",audioPayload);
            audioHeader.put("namespace","AudioPlayer");
            audioHeader.put("name","PlaybackState");
            audioPayload.put("offsetInMilliseconds",0);
            audioPayload.put("playerActivity","IDLE");
            audioPayload.put("token","");

            AlertState.put("header",alertHeader);
            AlertState.put("payload",alertPayload);
            alertHeader.put("name","AlertsState");
            alertHeader.put("namespace","Alerts");
            alertPayload.put("activeAlerts",acitveAlerts);
            alertPayload.put("allAlerts",allAlerts);

            SpeakerState.put("header",speakerHeader);
            SpeakerState.put("payload",speakerPayload);
            speakerHeader.put("name","VolumeState");
            speakerHeader.put("namespace","Speaker");
            speakerPayload.put("muted",false);
            speakerPayload.put("volume",100);

            SpeechSynthesizer.put("header",speechSynthesizerHeader);
            SpeechSynthesizer.put("payload",speechSynthesizerPayload);
            speechSynthesizerHeader.put("name","SpeechState");
            speechSynthesizerHeader.put("namespace","SpeechSynthesizer");
            speechSynthesizerPayload.put("offsetInMilliseconds",0);
            speechSynthesizerPayload.put("playerActivity","FINISHED");
            speechSynthesizerPayload.put("token","");

//            SpeechRecognizer.put("header",speechRecognizerHeader);
//            SpeechRecognizer.put("payload",speechRecognizerPayload);
//            speechRecognizerHeader.put("name","Recognize");
//            speechRecognizerHeader.put("namespace","SpeechRecognizer");

            context.put(AudioPlayer);
            context.put(AlertState);
            context.put(SpeakerState);
            context.put(SpeechSynthesizer);
          //  context.put(SpeechRecognizer);

            header.put("namespace", "System");
            header.put("name","SynchronizeState");
            header.put("messageId", Util.getUuid());
            event.put("header",header);
            event.put("payload",payload);
            p.put("event",event);
       //     p.put("context",context);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(accessToken==null)
            SendSynchronizeEvent(accessToken);
        else {
            OkHttpClient okclient = getOkhttp();
            MultipartBody.Builder requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("metadata", "metadata", RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), String.valueOf(p)));

            Request request = new Request.Builder()
                    .url("https://avs-alexa-eu.amazon.com/v20160207/events")
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("content-type","multipart/form-data")
                    .addHeader("boundary","--gc0p4Jq0M2Yt08jU534c0p--")
                    .post(requestBody.build())
                    .build();

            okclient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    call.cancel();
                    Log.d("synchronizefailed","mda");
                    SendSynchronizeEvent(accessToken);
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    tokenHandler.getAccessToken(TokenHandler.DownChannelCase3);
                    Intent dialogIntent = new Intent(DownChannel.this, SendingAudio.class);
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(dialogIntent);
                 //   EventBus.getDefault().post(new MessageEvent(TokenHandler.SplashActivity,"finishSplashActivity"));
                }
        });
       }
    }
    private void SendPingRequest(final String accessToken) {
        Log.d("ping request","method");
        OkHttpClient client = getOkhttp();

        if(accessToken==null)
            SendPingRequest(accessToken);
        else {
            final Request request = new Request.Builder()
                    .url("https://avs-alexa-eu.amazon.com/v20160207/ping")
                    .get()
                    .addHeader("authorization","Bearer "+accessToken)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    SendPingRequest(accessToken);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DownChannel.this, "PingRequest is successfully sent.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    SendPingRequest(accessToken);
                }
            }, 4 * 60 * 1000);
        }
    }

    private void sendMessage() {
        Intent intent = new Intent("my-event");
        intent.putExtra("message", "stop");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void cancelPendingIntent(int id) {
        Intent i = new Intent(DownChannel.this, AlarmReceiver.class);
        i.putExtra("alarm","alarm on");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(DownChannel.this,id,i,PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(pendingIntent);
    }

}

package com.game.dhanraj.myownalexa;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.game.dhanraj.myownalexa.Alarm.MyAlarm;
import com.game.dhanraj.myownalexa.sharedpref.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSource;

import static com.game.dhanraj.myownalexa.MainActivity.getOkhttp;

/**
 * Created by Dhanraj on 01-06-2017.
 */

public class DownChannel extends Service {


    //changed it from na to eu
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



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("gat", "Launched");
        //ek baar dekh lena poorana code bhi aur naye ko compare karke


        //ye isliye dala hai ki execute kar paye na ki enqueue

                openDownChannel();


       // return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        alarmManager =  (AlarmManager) getSystemService(ALARM_SERVICE);
        calendar = Calendar.getInstance();
        sendingAudio  = new SendingAudio();
        calendar2 = Calendar.getInstance();

    }

   /* @Override
    public void onDestroy() {
        super.onDestroy();
    }*/

    public void openDownChannel() {



        OkHttpClient client2 = getOkhttp();

        OkHttpClient client=  client2.newBuilder()
               .connectTimeout(0, TimeUnit.MILLISECONDS)
               .readTimeout(0, TimeUnit.MILLISECONDS)
               .writeTimeout(0, TimeUnit.MILLISECONDS)
               .build();
        String accesstoken = MainActivity.getAccessToken(DownChannel.this);

        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(accesstoken==null)
        {
            Log.d("openDownChannel","failed");
           // openDownChannel();
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(DownChannel.this, "Loginfailed", Toast.LENGTH_SHORT).show();
                }
            });
            openDownChannel();

           /* MainActivity main = new MainActivity();
            main.intiLogi();*/
        }

        else{
            final Request request = new Request.Builder()
                    .url(DchannelURL)
                    .get()
                    .addHeader("authorization","Bearer "+accesstoken)
                    .build();

              /*  Response response = null;
                try {
                    response = client.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
             */

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("downchannelfailed","failureagain");
                    openDownChannel();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    new AsyncTask<Void, Void, Void>(){
                        @Override
                        protected Void doInBackground(Void... params) {
                            SendSynchronizeEvent();
                            return null;
                        }

                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DownChannel.this, "DownChannel pe Response aya hai", Toast.LENGTH_SHORT).show();
                        }
                    });


                    // Log.d("Downchannel",response.body().string());
                    Headers headers = response.headers();
                    Log.d("CheckTheResponseHere", "startDownchannel: " + response.code() + ", " + response.protocol().name() + ", length:" + response.body().contentLength());
                    for (int i = 0; i < headers.size(); i++) {
                        Log.d("HeaderResponse", " " + headers.name(i) + ": " + headers.value(i));
                    }
                  //  StringBuilder stringBuilder = new StringBuilder();


                    BufferedSource bufferedSource = response.body().source();
                    Buffer buffer = new Buffer();

                    try {

                        while (!bufferedSource.exhausted()) {
                        // String line = bufferedSource.readUtf8Line();
                       // bufferedSource.read(buffer, 8192);
                            String line = bufferedSource.readUtf8Line();
                         //   stringBuilder.append(line);
                             boolean checkJson = isJSONValid(line);

//                            new AsyncTask<Void,Void,Void>(){
//                                @Override
//                                protected Void doInBackground(Void... params) {
                                    if(checkJson==true)
                                    {
                                        try {
                                            JSONObject objectDirective = new JSONObject(line);
                                            JSONObject directive = (JSONObject) objectDirective.get("directive");
                                            JSONObject header = (JSONObject) directive.get("header");
                                            JSONObject payload = (JSONObject) directive.get("payload");
                                            String namespace = header.getString("namespace");
                                            String name = header.getString("name");
                                            if(name.equals("StopCapture") && namespace.equals("SpeechRecognizer"))
                                            {
                                              //  sendingAudio.stopListening();
                                            }else if(name.equals("SetAlert") && namespace.equals("Alerts"))
                                            {
                                                String type =  payload.getString("type");
                                                if(type.equals("ALARM"))
                                                {
                                                    /*Intent intet = new Intent(DownChannel.this, AlarmReceiver.class);
                                                    String scheduledTime = payload.getString("scheduledTime");
                                                    // Calendar calendar = javax.xml.bind.DatatypeConverter.parseDateTime(scheduledTime);
                                                    //"yyyy-MM-dd'T'HH:mm:ss.SSSZ",yyyy-MM-dd'T'HH:mm:ss'

                                                    DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                                                  //  DateFormat fmt2 = new SimpleDateFormat("HH:mm:ss", Locale.US);

                                                    //  DateFormat fmt = DateFormat.getTimeInstance(DateFormat.LONG, Locale.US);

                                                    try {
                                                        Date dhanraj = fmt.parse(scheduledTime);
                                                        //String dhanraj2 = fmt.format(scheduledTime);
                                                        Long time =  dhanraj.getTime();
                                                        Log.d("Mytime",time.toString());
                                                        calendar.setTimeInMillis(time);
                                                        //for setting new alarm
                                                        int request = (int) (time/1000);
                                                        int hour = calendar.get(Calendar.HOUR)-7;
                                                        int minute = calendar.get(Calendar.MINUTE);
                                                        int year = calendar.get(Calendar.YEAR);
                                                        int AMorPM = calendar.get(Calendar.AM_PM);
                                                        if(hour<=0)
                                                        {
                                                            hour = 12+hour;
                                                            if(AMorPM==1)
                                                                AMorPM =0;
                                                            else
                                                                AMorPM = 1;
                                                        }

                                                        calendar2.set(Calendar.HOUR,hour);
                                                        calendar2.set(Calendar.MINUTE,minute);
                                                        calendar2.set(Calendar.AM_PM,AMorPM);

                                                        intet.putExtra("alarm","alarm on");

                                                        Log.d("mywholeTime",hour+","+minute+","+year+","+calendar.get(Calendar.AM_PM));
                                                        pending = PendingIntent.getBroadcast(DownChannel.this,0,intet,0);
                                                        //calendar2 use karna tha lekin calendar use kar raha hu
                                                        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pending);

                                                     //   Log.d("dhanrajtime",dhanraj2);
                                                    } catch (ParseException e) {
                                                        e.printStackTrace();
                                                        Log.d("ParsingException","DownnChannelException");
                                                    }
                                                    //  Log.d("shashank",dhanraj);*/

                                                }
                                            }

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                //    return null;
                            //    }
                        //    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            Log.d("alarm",line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
             //       Log.d("stringBuilder",stringBuilder.toString());
                   //  Log.d("dhanrajDownChannel", "Response: " + buffer.toString());
                  /*  new AsyncTask<Void, Void, Void>(){
                        @Override
                        protected Void doInBackground(Void... params) {
                            SendSynchronizeEvent();
                            return null;
                        }
                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
*/
                      /*try {
                        Headers headers = response.headers();
                        Log.d("CheckTheResponseHere", "startDownchannel: " + response.code() + ", " + response.protocol().name() + ", length:" + response.body().contentLength());
                        for (int i = 0; i < headers.size(); i++) {
                            Log.d("HeaderResponse", " " + headers.name(i) + ": " + headers.value(i));
                        }
                        Buffer buffer = new Buffer();
                        while (!response.body().source().exhausted()) {
                            long count = response.body().source().read(buffer, 8192);
                            Log.d("count ", count + " " + buffer.toString());
                        }
                        // listener.onConnected(response.isSuccessful());
                    } catch (IOException e) {
                        e.printStackTrace();
                        // downchannelThread.interrupt();
                    }*/

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


    private void SendSynchronizeEvent() {

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
        }catch (JSONException e)
        {
            e.printStackTrace();
        }
        p.toString();
        String accesstoken = MainActivity.getAccessToken(DownChannel.this);

        if(accesstoken==null)
            SendSynchronizeEvent();
        else
        {
            OkHttpClient okclient = getOkhttp();

            MultipartBody.Builder requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("metadata", "metadata", RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), String.valueOf(p)));


            //changed the endpoint here also us-eu
            Request request = new Request.Builder()
                    .url("https://avs-alexa-eu.amazon.com/v20160207/events")
                    .addHeader("Authorization", "Bearer " + accesstoken)
                    .addHeader("content-type","multipart/form-data")
                    .addHeader("boundary","--gc0p4Jq0M2Yt08jU534c0p--")
                    .post(requestBody.build())
                    .build();


            okclient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    call.cancel();
                    Log.d("synchronizefailed","mda");
                    SendSynchronizeEvent();
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {

                    //not necessary ki yahi pe first time sendpingrequest ko call kia jaye
                    SendPingRequest();
                }
        });

       }
    }
    private void SendPingRequest() {
        OkHttpClient client = getOkhttp();
         String accesstoken = MainActivity.getAccessToken(DownChannel.this);
        if(accesstoken==null)
            SendPingRequest();
        else{

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(DownChannel.this, "PingRequest has been sent", Toast.LENGTH_SHORT).show();
                }
            });

            final Request request = new Request.Builder()
                    .url("https://avs-alexa-eu.amazon.com/v20160207/ping")
                    .get()
                    .addHeader("authorization","Bearer "+accesstoken)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    SendPingRequest();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                }
            });

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    SendPingRequest();
                }
            }, 4 * 60 * 1000);
        }


    }


}
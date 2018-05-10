package com.game.dhanraj.myownalexa;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.game.dhanraj.myownalexa.Alarm.MyAlarm;
import com.game.dhanraj.myownalexa.RecordAudio.RecordAudioinBytes;
import com.game.dhanraj.myownalexa.RecordAudio.RecorderConstants;
import com.game.dhanraj.myownalexa.RecorderView.recorderView;
import com.game.dhanraj.myownalexa.sharedpref.Util;

import org.apache.commons.io.IOUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

import static com.game.dhanraj.myownalexa.Constants.BASE_THEME;
import static com.game.dhanraj.myownalexa.Constants.BASE_THEME_INTEGER;
import static com.game.dhanraj.myownalexa.Constants.DARK_THEME;
import static com.game.dhanraj.myownalexa.sharedpref.Util.getOkhttp;

/**
 * Created by Dhanraj on 05-06-2017.
 */

public class SendingAudio extends AppCompatActivity {

    private Button btn;
    private recorderView record;
    private RecorderConstants recorderConstants;
    public  RecordAudioinBytes recordAudioinBytes;
    private static final int AUDIO_RATE = 16000;
    private String myresponse;
    public  String tokenfrompayload;
    private MediaPlayer mediaPlayer;
    public boolean PressTheButton = false ;
    public  ProgressDialog asyncDialog;
    public boolean checkRecordButton = true;
    public DownChannel down;
    private PendingIntent pendingIntent;
    private TextView stateofbtn;
    private AlarmManager alarmManager;
    public TokenHandler tokenHandler;
    private Toolbar toolbar;
    private int theme, themeInteger;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_audio_drawer);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Talk Box");
        stateofbtn = (TextView) findViewById(R.id.stateofbutton);
        record = (recorderView) findViewById(R.id.sendAudiobtn);

        SharedPreferences preferences = Util.getPrefernces(SendingAudio.this);
        theme = preferences.getInt(BASE_THEME, ContextCompat.getColor(SendingAudio.this, R.color.light_background));
        themeInteger = preferences.getInt(BASE_THEME_INTEGER, 1);
        setUpLayout();

        tokenHandler = new TokenHandler(this);

        down = new DownChannel();
        asyncDialog = new ProgressDialog(SendingAudio.this);
        mediaPlayer = new MediaPlayer();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        record.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN ) {
                    if ( ContextCompat.checkSelfPermission(SendingAudio.this, Manifest.permission.RECORD_AUDIO  )
                            != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(SendingAudio.this,
                                new String[]{Manifest.permission.RECORD_AUDIO},
                                2);
                    }
                    else {
                        ConnectivityManager connectivityManager
                                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                            if (recordAudioinBytes == null) {
                                //if mediaplayer is not playing
                                if (!mediaPlayer.isPlaying() && checkRecordButton) {
                                    send();
                                    Toast.makeText(SendingAudio.this, "Speak", Toast.LENGTH_SHORT).show();
                                    checkRecordButton = false;
                                }
                            }
                        } else {
                            Snackbar.make(v, "No Internet Connectivity", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    }
                    return true;
                } else if(event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    stopListening();
                    checkRecordButton=true;
                    return true;
                }
                return false;
            }
        });
        EventBus.getDefault().post(new MessageEvent(TokenHandler.SplashActivity,"finishSplashActivity"));
        EventBus.getDefault().post(new MessageEvent(TokenHandler.FinishMainActivity,""));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_alarm:
                Intent i = new Intent(SendingAudio.this, MyAlarm.class);
                startActivity(i);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(SendingAudio.this, DownChannel.class);
        SendingAudio.this.stopService(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(SendingAudio.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 2: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (recordAudioinBytes == null) {

                        if (!mediaPlayer.isPlaying() && checkRecordButton){
                            send();
                            Toast.makeText(SendingAudio.this, "Speak", Toast.LENGTH_SHORT).show();
                            checkRecordButton = false;
                        }
                    } else {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        stopListening();
                        checkRecordButton=true;
                    }
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.RECORD_AUDIO)) {
                        new AlertDialog.Builder(this).
                                setTitle("Record Audio Permission").
                                setMessage("You need to grant this permission in order to record audio");
                    } else {
                        new AlertDialog.Builder(this)
                                .setTitle("Record Audio Permission")
                                .setMessage("You have denied this permission.Now you have to go to settings to enable it");
                    }
                }
                return;
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("my-event"));
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(MessageEvent event){
        switch (event.event){
            case TokenHandler.SendAudioRequest:
                sendAudioRequest(requestBody,event.message);
                break;
            case TokenHandler.SendSpeechStartedEvent:
                SendSpeechStartedEvent(tokenfrompayload,event.message);
                break;
            case TokenHandler.SendSpeechFinishedEvent:
                SendSpeechFinishedEvent(tokenfrompayload,event.message);
                break;
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            if(checkRecordButton)
            stopListening();
            Log.d("receiver", "Got message " );
        }
    };

    @Override
    protected void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    private void send() {
        recorderConstants = new RecorderConstants(AUDIO_RATE);
        if(recordAudioinBytes==null){
            recordAudioinBytes = new RecordAudioinBytes();
        }
        recordAudioinBytes.start();
        tokenHandler.getAccessToken(TokenHandler.SendAudioRequest);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void sendAudioRequest(RequestBody requestBody,final String accessToken) {
        JSONObject p = new JSONObject();
        JSONObject event = new JSONObject();
        JSONObject header = new JSONObject();
        JSONObject payload = new JSONObject();
        Log.d("sendAduioReqeust","sentit");

        try {
            header.put("namespace", "SpeechRecognizer");
            header.put("name","Recognize");
            header.put("messageId", Util.getUuid());
            header.put("dialogRequestId","dialogRequest-321");
            event.put("header",header);
            event.put("payload",payload);
            payload.put("format","AUDIO_L16_RATE_16000_CHANNELS_1");
 //           payload.put("profile","NEAR_FIELD");
            payload.put("profile","FAR_FIELD");
            p.put("event",event);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(accessToken==null) {
            Log.d("sendforrefreshtoken","refreshtoken");
            Toast.makeText(this, "First take authorization token for getting access token", Toast.LENGTH_SHORT).show();
        } else {
            OkHttpClient okclient = getOkhttp();
            MultipartBody.Builder requestBdy = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("metadata","metadata", RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), String.valueOf(p)))
                    .addFormDataPart("audio","application/octet-stream",requestBody);

            Request request = new Request.Builder()
                    .url("https://avs-alexa-eu.amazon.com/v20160207/events")
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("content-type","multipart/form-data")
                    .addHeader("boundary","--gc0p4JppM2Yt08jU534c0p--")
                    .post(requestBdy.build())
                    .build();

            final RequestBody rqstBody = requestBody;
            okclient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("failurehaibhai","failed");
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            stateofbtn.setText("");
                        }
                    });
                    checkRecordButton=true;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SendingAudio.this, "Request Failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if(response.isSuccessful()) {
                        Log.d("speechrecognizer","success");
                    }
                    try {
                      //  if (getBoundary(response) != null) {
                        if(true){
                          //  System.setProperty("mail.mime.multipart.ignoreexistingboundaryparameter", "true");
                            ByteArrayDataSource ds = new ByteArrayDataSource(response.body().byteStream(), "multipart/form-data");
                            response.body().close();
                            MimeMultipart multipart = null;
                            Boolean checkforExpectSpeech = false;
                            try {
                                multipart = new MimeMultipart(ds);

                                Log.d("MainContentType",multipart.getContentType());

                                for (int i = 0; i < multipart.getCount(); i++) {
                             /*   if(multipart.getBodyPart(i)==null)
                                    break;*/
                                    BodyPart bodypart = multipart.getBodyPart(i);
                                    String contentType = bodypart.getContentType();
                                    Log.d("contentType: ", bodypart.getContentType());
                                    String forSendingToken = null;
                                    if (contentType.equals("application/json; charset=UTF-8")) {
                                        InputStream inputStreamObject = bodypart.getInputStream();
                                        BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStreamObject, "UTF-8"));
                                        StringBuilder responseStrBuilder = new StringBuilder();
                                        String inputStr;
                                        String token = null;
                                        String namespace = null;
                                        String name = null;
                                        String scheduledTime = null;
                                        String type = null;

                                        while ((inputStr = streamReader.readLine()) != null)
                                            responseStrBuilder.append(inputStr);

                                        try {
                                            //  Log.d("body",responseStrBuilder.toString());
                                            JSONObject dhanraj = new JSONObject(responseStrBuilder.toString());
                                            JSONObject directive = (JSONObject) dhanraj.get("directive");
                                            JSONObject header = (JSONObject) directive.get("header");
                                            JSONObject payload = (JSONObject) directive.get("payload");
                                            namespace = header.getString("namespace");
                                            name = header.getString("name");
                                            if (name.equals("StopCapture") && namespace.equals("SpeechRecognizer"))
                                                stopListening();
                                            else if (name.equals("ExpectSpeech") && namespace.equals("SpeechRecognizer")) {
                                                long timeoutInMilliseconds = payload.getLong("timeoutInMilliseconds");
                                                Log.d("timeoutmsec", String.valueOf((timeoutInMilliseconds)));
                                                checkforExpectSpeech = true;
                                            } else if (name.equals("Speak") && namespace.equals("SpeechSynthesizer")) {
                                                Log.d("token", "SpeakDirective");
                                                if (payload.getString("token") != null) {
                                                    token = payload.getString("token");
                                                    tokenfrompayload = token;
                                                }
                                            } else if (name.equals("SetAlerts") && namespace.equals("Alerts")) {
                                                String scheduledAlertTime = payload.getString("scheduledTime");
                                                Log.d("AlertTimeis ", scheduledAlertTime);
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        Log.d("nameofdirective", name + " , " + "i=" + i);
                                    } else if (contentType.equals("application/json")) {
                                        Log.d("alert", bodypart.getContentType());

                                    } else if (contentType.equals("application/octet-stream") ){
                                        byte[] b = IOUtils.toByteArray(bodypart.getInputStream());
                                        playByteArray(b, null);
                                    }
                                }
                                if(checkforExpectSpeech) {
                                    send();
                                    checkforExpectSpeech=false;
                                }
                            } catch (MessagingException e) {
                                e.printStackTrace();
                                Log.d("pooriAudioreceivenhihoi","ResponseFailed");
                            }
                        } else {

                        }
                    }catch (Exception e){
                        //nothing specified
                    } finally {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                stateofbtn.setText("");
                            }
                        });
                    }
                }
            });
        }
    }

    private void sendExpectSpeechTimedOutEvent() {
        JSONObject p = new JSONObject();
        JSONObject event = new JSONObject();
        JSONObject header = new JSONObject();
        JSONObject payload = new JSONObject();
        try {
            header.put("namespace", "SpeechRecognizer");
            header.put("name", "ExpectSpeechTimedOut");
            header.put("messageId", Util.getUuid());
            event.put("header", header);
            event.put("payload", payload);
            p.put("event", event);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        p.toString();
      //  String accesstoken = MainActivity.getAccessToken(SendingAudio.this);
        String accesstoken =null;
        if (accesstoken == null)
            sendExpectSpeechTimedOutEvent();
        else {
            OkHttpClient okclient = getOkhttp();
            MultipartBody.Builder requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("metadata", "metadata", RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), String.valueOf(p)));

            Request request = new Request.Builder()
                    .url("https://avs-alexa-eu.amazon.com/v20160207/events")
                    .addHeader("Authorization", "Bearer " + accesstoken)
                    .addHeader("content-type", "multipart/form-data")
                    .addHeader("boundary", "--gc0p4Jq0M2Yt08jU534c0p--")
                    .post(requestBody.build())
                    .build();

            okclient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    call.cancel();
                    Log.d("sendSpeechExpectTimOut","failed");
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    if(response.isSuccessful())
                        Log.d("sendSpeechExpectTimOut","successful");

                    Log.d("sendSpeechExpectTimOut",response.body().string());
                }
            });
        }
    }

    private void SendSpeechFinishedEvent(String token,String accessToken) {
        JSONObject p = new JSONObject();
        JSONObject event = new JSONObject();
        JSONObject header = new JSONObject();
        JSONObject payload = new JSONObject();

        try {
            header.put("namespace", "SpeechSynthesizer");
            header.put("name", "SpeechFinished");
            header.put("messageId", Util.getUuid());
            event.put("header", header);
            event.put("payload", payload);
            payload.put("token", token);
            p.put("event", event);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //might not need the below statement
       // p.toString();
        if (accessToken == null)
           tokenHandler.getAccessToken(TokenHandler.SendSpeechFinishedEvent);
        else {
            OkHttpClient okclient = getOkhttp();
            MultipartBody.Builder requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("metadata", "metadata", RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), String.valueOf(p)));

            Request request = new Request.Builder()
                    .url("https://avs-alexa-eu.amazon.com/v20160207/events")
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("content-type", "multipart/form-data")
                    .addHeader("boundary", "--gc0p4Jq0M2Yt08jU534c0p--")
                    .post(requestBody.build())
                    .build();

            okclient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    call.cancel();
                    Log.d("SendSpeechFinishedEvent","failed");

                }
                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    if(response.isSuccessful()) {
                        Log.d("speechfinishedrequest", "successful");
                        if(response.header("name")!=null)
                        Log.d("headeroffinishedRequest",response.header("name"));
                    }
                }
            });
        }
    }

    private void SendSpeechStartedEvent(String token,String accessToken) {
        JSONObject p = new JSONObject();
        JSONObject event = new JSONObject();
        JSONObject header = new JSONObject();
        JSONObject payload = new JSONObject();

        try {
            header.put("namespace", "SpeechSynthesizer");
            header.put("name", "SpeechStarted");
            header.put("messageId", Util.getUuid());
            event.put("header", header);
            event.put("payload", payload);
            payload.put("token", token);
            p.put("event", event);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //p.toString();
        //String accesstoken = MainActivity.getAccessToken(SendingAudio.this);

        if (accessToken == null)
            tokenHandler.getAccessToken(TokenHandler.SendSpeechStartedEvent);
        else {
            OkHttpClient okclient = getOkhttp();
            MultipartBody.Builder requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("metadata", "metadata", RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), String.valueOf(p)));

            Request request = new Request.Builder()
                    .url("https://avs-alexa-eu.amazon.com/v20160207/events")
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("content-type", "multipart/form-data")
                    .addHeader("boundary", "--gc0p4Jq0M2Yt08jU534c0p--")
                    .post(requestBody.build())
                    .build();

            final String TOKEN = token;
            okclient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    call.cancel();
                    Log.d("SendSpeechStartedEvent","failed");

                }
                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    if(response.isSuccessful())
                        Log.d("speechstartedrequest","successful");
                    if(response.header("name")!=null)
                        Log.d("speechstartedrequest",response.header("name"));
                }
            });

        }
    }

    private MediaPlayer.OnCompletionListener mCompletion = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
           /* if(tokenfrompayload!=null)
                SendSpeechFinishedEvent(tokenfrompayload);*/
        }
    };

    private MediaPlayer.OnPreparedListener mPreparation = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
//            if(tokenfrompayload!=null)
//                SendSpeechStartedEvent(tokenfrompayload);
        }
    };

    private void playByteArray(byte[] mp3SoundByteArray,String token) {
        try {
            File Mytemp = new File(getApplicationContext().getCacheDir(),System.currentTimeMillis()+".mp3");
            FileOutputStream fos = new FileOutputStream(Mytemp);
            fos.write(mp3SoundByteArray);
            fos.close();

            mediaPlayer.reset();
            mediaPlayer.setOnCompletionListener(mCompletion);
            mediaPlayer.setOnPreparedListener(mPreparation);
            mediaPlayer.setDataSource(Mytemp.getPath());

            if(tokenfrompayload!=null)
                tokenHandler.getAccessToken(TokenHandler.SendSpeechStartedEvent);

            mediaPlayer.prepare();

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    stateofbtn.setText("");
                    asyncDialog.setMessage("playing");
                    asyncDialog.show();
                }
            });

            mediaPlayer.start();

            while(mediaPlayer.isPlaying());

            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            mediaPlayer.stop();

            if(tokenfrompayload!=null)
                tokenHandler.getAccessToken(TokenHandler.SendSpeechFinishedEvent);

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
               asyncDialog.dismiss();

                }
            });

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(SendingAudio.this, "You can send another request or can send the previous request if you didn't get any response",
                            Toast.LENGTH_SHORT).show();
                }
            });

           /* new AsyncTask<Void, Void, Void>(){
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        boolean playbackstartbool = false;
                        boolean playbacknearlyfinished = false;
                        while (mediaPlayer != null && mediaPlayer.isPlaying()) {
                            int pos = mediaPlayer.getCurrentPosition();
                            final float percent = (float) pos / (float) mediaPlayer.getDuration();
                         //   postProgress(percent);
                            if(!playbackstartbool)
                            {
                                playbackstartbool = true;
                                sendPlaybackStartedEvent(percent);
                                playbacknearlyfinished=true;
                            }
                            if(playbacknearlyfinished && percent> .8f)
                            {
                                sendPlaybackNearlyFinishedEvent(percent);
                                playbackstartbool = false;
                                playbacknearlyfinished = false;
                                break;
                            }
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }catch (NullPointerException|IllegalStateException e){
                        e.printStackTrace();
                    }
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);*/


          //  mediaPlayer.getCurrentPosition();


            /*if(!mediaPlayer.isPlaying()){
                SendSpeechFinishedEvent(tokenfrompayload);
            }*/
        } catch (IOException ex) {
            String s = ex.toString();
            ex.printStackTrace();
        }
    }

   /* private void sendPlaybackStartedEvent(float percent) {
        JSONObject p = new JSONObject();
        JSONObject event = new JSONObject();
        JSONObject header = new JSONObject();
        JSONObject payload = new JSONObject();

        try {

            header.put("namespace", "SpeechSynthesizer");
            header.put("name", "SpeechFinished");
            header.put("messageId", Util.getUuid());
            event.put("header", header);
            event.put("payload", payload);
          //  payload.put("token", token);
            p.put("event", event);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        p.toString();
        String accesstoken = MainActivity.getAccessToken(SendingAudio.this);

        if (accesstoken == null){
            sendPlaybackStartedEvent(percent);
        }
        else {
            //  Log.d("speechfinishedrequest","successful");
            OkHttpClient okclient = getOkhttp();

            MultipartBody.Builder requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("metadata", "metadata", RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), String.valueOf(p)));


            Request request = new Request.Builder()
                    .url("https://avs-alexa-na.amazon.com/v20160207/events")
                    .addHeader("Authorization", "Bearer " + accesstoken)
                    .addHeader("content-type", "multipart/form-data")
                    .addHeader("boundary", "--gc0p4Jq0M2Yt08jU534c0p--")
                    .post(requestBody.build())
                    .build();


            okclient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    call.cancel();
                    Log.d("sendPybkStartedEvent","failed");

                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    if(response.isSuccessful()) {
                        Log.d("sendPybkStartedEvent", "successful");
                    }

                }
            });

        }
    }*/

    public String getBoundary(Response response) throws IOException {
        String header = response.header("content-type");
        String boundary = "";

        if (header != null) {
            Pattern pattern = Pattern.compile("boundary=(.*?);");
            Matcher matcher = pattern.matcher(header);
            if (matcher.find()) {
                boundary = matcher.group(1);
            }
        } else {
            Log.i("noboundary", "Body");
            String printmy =  System.setProperty("mail.mime.multipart.ignoreexistingboundaryparameter", "true");
            Log.d("system",printmy);
        }
        return boundary;
    }

    private RequestBody requestBody = new RequestBody() {
        @Override
        public MediaType contentType() {
            return MediaType.parse("application/octet-stream");
        }
        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            //recordAudioinBytes.isPausing() changing this
           // while(recordAudioinBytes!=null && !recordAudioinBytes.isPausing())
            while(recordAudioinBytes!=null) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        stateofbtn.setText("Listening...");
                    }
                });
                if(recordAudioinBytes!=null) {
                    final float rmsdb = recordAudioinBytes.getRmsdb();
                    if(record != null) {
                        record.post(new Runnable() {
                            @Override
                            public void run() {
                                record.setRmsdbLevel(rmsdb);
                            }
                        });
                    } if(sink!=null && recordAudioinBytes!=null) {
                        sink.write(recordAudioinBytes.consumeRecording());
                    }
                }

                try {
                    //changed from 25 to 250
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public void stopListening(){

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                stateofbtn.setText("wait...");
            }
        });

        if(recordAudioinBytes != null) {
            recordAudioinBytes.stop();
            recordAudioinBytes.release();
            recordAudioinBytes = null;
        }
    }

    @SuppressLint("ResourceAsColor")
    private void setUpLayout() {
        findViewById(R.id.send_audio_layout).setBackgroundColor(theme);
        int oppositeTheme;
        if (themeInteger == DARK_THEME)
            oppositeTheme = ContextCompat.getColor(SendingAudio.this, R.color.light_background);
        else
            oppositeTheme = ContextCompat.getColor(SendingAudio.this, R.color.dark_background);
        stateofbtn.setTextColor(oppositeTheme);
    }

}

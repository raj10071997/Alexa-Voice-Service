package com.game.dhanraj.myownalexa;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.api.authorization.AuthCancellation;
import com.amazon.identity.auth.device.api.authorization.AuthorizationManager;
import com.amazon.identity.auth.device.api.authorization.AuthorizeListener;
import com.amazon.identity.auth.device.api.authorization.AuthorizeRequest;
import com.amazon.identity.auth.device.api.authorization.AuthorizeResult;
import com.amazon.identity.auth.device.api.authorization.ScopeFactory;
import com.amazon.identity.auth.device.api.workflow.RequestContext;
import com.game.dhanraj.myownalexa.AccessConstant.CodeVerifierandChallengeMethods;
import com.game.dhanraj.myownalexa.Alarm.MyAlarm;
import com.game.dhanraj.myownalexa.sharedpref.Util;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.game.dhanraj.myownalexa.AccessConstant.CodeVerifierandChallengeMethods.generateCodeChallenge;


//The authorization code changes everytime you open the app for the first time so you have to get the authorization code everytime you login till your access
public class MainActivity extends AppCompatActivity {

    private RequestContext mRequestContext;
    public  static Context mContext;
    public static Context myContext;
    private static final String PRODUCT_ID = "DhanrajCompanionProduct";
    private static String PRODUCT_DSN;
    private static String CODE_CHALLENGE_METHOD = "S256";
    private String codeVerifier;
    private static String authCode, redirectURI, clientID;
    private static String codeChallenge;
    private static OkHttpClient client;
    private TextView txt;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private Button btn, pst,nxtaudio;
    private CircleImageView Loginbtn;

    private static String REFRESH_TOKEN;
    private static String ACCESS_TOKEN;

    private static String myresponse;
    public static String CLIENTID;

    public static DownChannel dwn;

    public final static String PREF_ACCESS_TOKEN = "access_token_042017";
    public final static String PREF_REFRESH_TOKEN = "refresh_token_042017";
    public final static String PREF_TOKEN_EXPIRES = "token_expires_042017";

    private ProgressDialog progressDialog;
    private static boolean forDownChannel,refreshToken;
    public static boolean CheckInternetConnection,DownChannelestablished;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//meine getApplicationContext() use karke dekha tha lekin error aa raha tha, abhi activity ka context pass kia hai
        mContext = MainActivity.this;
        myContext = MainActivity.this;
        mRequestContext = RequestContext.create(this);
        mRequestContext.registerListener(new AuthorizeListenerImpl());

        txt = (TextView) findViewById(R.id.text);
        PRODUCT_DSN = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        try {
            codeVerifier = CodeVerifierandChallengeMethods.generateCodeVerifier();
            codeChallenge = generateCodeChallenge(codeVerifier, "S256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Your JRE does not support the required "
                    + "SHA-256" + " algorithm.", e);
        }

        refreshToken= true;
        DownChannelestablished = true;
        dwn = new DownChannel();
        //SharedPreferences perf = Util.getPrefernces(mContext);

        Loginbtn = (CircleImageView) findViewById(R.id.btn);
        Loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager connectivityManager
                        = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if(activeNetworkInfo != null && activeNetworkInfo.isConnected())
                intiLogi();
                else
                    Toast.makeText(MainActivity.this, "No Internet Connectivity", Toast.LENGTH_SHORT).show();

            }
        });


        }


    @Override
    protected void onStart() {
        super.onStart();

        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (!(activeNetworkInfo != null && activeNetworkInfo.isConnected())) {
            Toast.makeText(MainActivity.this, "No Internet Connectivity", Toast.LENGTH_SHORT).show();
        }


       /* String token=null;
        try {
            token = getAccessToken();
        }catch(Exception e)
        {

        }finally {
            if(token==null)
                Toast.makeText(MainActivity.this, "Login to get the access token", Toast.LENGTH_SHORT).show();
        }
        if(token != null)
        {
            if(!isMyServiceRunning(DownChannel.class))
            {
                // intiLogi();
                Log.d("downchannelStarted","dhanraj");
                Intent stickyIntent = new Intent(MainActivity.this, DownChannel.class);
                MainActivity.this.startService(stickyIntent);
            }
        }*/


    }


    @Override
    protected void onResume() {
        super.onResume();
        mRequestContext.onResume();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
       // Toast.makeText(<Context>, "Stopping service", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, DownChannel.class);
        MainActivity.this.stopService(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent i = new Intent(MainActivity.this, MyAlarm.class);
                startActivity(i);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private static void doPostRequest(final RequestBody form,final String checkRefreshToken) {

        OkHttpClient okclient = getOkhttp();
        Request request = new Request.Builder()
                .url("https://api.amazon.com/auth/O2/token")
                .post(form)
                .build();
        Response response=null;


        okclient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                Log.d("accessToken","failed");
                doPostRequest(form,checkRefreshToken);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

               myresponse = response.body().string();

                TokenResponse tokenResponse = new Gson().fromJson(myresponse, TokenResponse.class);
                saveToken(tokenResponse);

                Intent st = new Intent(myContext,DownChannel.class);
                myContext.startService(st);

                if(checkRefreshToken!=null)
                    refreshToken = false;

            }
        });

        /*if(DownChannelestablished){
            Intent st = new Intent(myContext,DownChannel.class);
            myContext.startService(st);

            DownChannelestablished=false;
        }*/

    }

    private static void saveToken(TokenResponse tokenResponse) {

        REFRESH_TOKEN = tokenResponse.refresh_token;
        ACCESS_TOKEN = tokenResponse.access_token;
        SharedPreferences.Editor preferences = Util.getPrefernces(mContext).edit();
        preferences.putString(PREF_ACCESS_TOKEN, ACCESS_TOKEN);
        preferences.putString(PREF_REFRESH_TOKEN, REFRESH_TOKEN);
        //comes back in seconds, needs to be milis
        preferences.putLong(PREF_TOKEN_EXPIRES, (System.currentTimeMillis() + tokenResponse.expires_in * 1000));
        preferences.apply();
    }


    public void intiLogi() {

        final JSONObject scopeData = new JSONObject();
        final JSONObject productInstanceAttributes = new JSONObject();

        try {
            productInstanceAttributes.put("deviceSerialNumber", PRODUCT_DSN);
            scopeData.put("productInstanceAttributes", productInstanceAttributes);
            scopeData.put("productID", PRODUCT_ID);

            AuthorizationManager.authorize(new AuthorizeRequest.Builder(mRequestContext)
                    .addScope(ScopeFactory.scopeNamed("alexa:all", scopeData))
                    .forGrantType(AuthorizeRequest.GrantType.AUTHORIZATION_CODE)
                    .withProofKeyParameters(codeChallenge, CODE_CHALLENGE_METHOD)
                    .build());


        } catch (JSONException e) {
            // handle exception here
        }
    }


    private class AuthorizeListenerImpl extends AuthorizeListener {

        /* Authorization was completed successfully. */
        @Override
        public void onSuccess(final AuthorizeResult authorizeResult) {
            final String authorizationCode = authorizeResult.getAuthorizationCode();
            final String redirectUri = authorizeResult.getRedirectURI();
            final String clientId = authorizeResult.getClientId();
            CLIENTID = clientId;

                    SharedPreferences.Editor  preferences = Util.getPrefernces(mContext).edit();
                    preferences.putString("clientID",clientId);
                    preferences.apply();
                    RequestBody formBody = new FormBody.Builder()
                            .add("grant_type", "authorization_code")
                            .add("code", authorizationCode)
                            .add("redirect_uri", redirectUri)
                            .add("client_id", clientId)
                            .add("code_verifier", codeVerifier)
                            .build();
                    doPostRequest(formBody,null);
                  //  SaveToken();

                   // stopService(DownChannel.class);

            //checking if there is no response or not
//            Intent st = new Intent(MainActivity.this,DownChannel.class);
//            MainActivity.this.stopService(st);

             /*Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent st = new Intent(MainActivity.this,DownChannel.class);
                    MainActivity.this.startService(st);
                }
            }, 5000);*/
                   /* if(!isMyServiceRunning(DownChannel.class))
                    {

                        Log.d("downchannelStarted","dhanraj");
                        Intent stickyIntent = new Intent(MainActivity.this, DownChannel.class);
                        MainActivity.this.startService(stickyIntent);
                    }*/
        }

        /* There was an error during the attempt to authorize the application. */
        @Override
        public void onError(final AuthError authError) {
            intiLogi();
        }

        /* Authorization was cancelled before it could be completed. */
        @Override
        public void onCancel(final AuthCancellation authCancellation) {
            Toast.makeText(MainActivity.this, "Your authorization has been cancelled. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }





    public static OkHttpClient getOkhttp() {
        if (client == null)
            client = new OkHttpClient();


        return client;
    }


    public static String getAccessToken(Context context) {
        SharedPreferences preferences = Util.getPrefernces(context);
        //if we have an access token
        if (preferences.contains(PREF_ACCESS_TOKEN)) {
            Log.d("containAccessToken","contains");
            if (preferences.getLong(PREF_TOKEN_EXPIRES, 0) > System.currentTimeMillis()) {
                //if it's not expired, return the existing token
                Log.d("notexpired","contains");
                return preferences.getString(PREF_ACCESS_TOKEN, null);

            } else {
                //if it is expired but we have a refresh token, get a new token

                if (preferences.contains(PREF_REFRESH_TOKEN)) {
                    Log.d("expired","doesnotcontains");
                    getRefreshToken(context, preferences.getString(PREF_REFRESH_TOKEN, ""));
                    while(refreshToken);
                    refreshToken=true;
                    return preferences.getString(PREF_ACCESS_TOKEN, null);
                }
            }
        }

        return null;
    }




    private static void getRefreshToken(Context context, String string) {
        RequestBody formBody = new FormBody.Builder()
                .add("grant_type", "refresh_token")
                .add("refresh_token", string)
                .add("client_id", Util.getPrefernces(context).getString("clientId",""))
                .build();
        doPostRequest(formBody,"RefreshToken");

    }

    public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    //for JSON parsing of our token responses
    public static class TokenResponse{
        public String access_token;
        public String refresh_token;
        public String token_type;
        public long expires_in;
    }
}
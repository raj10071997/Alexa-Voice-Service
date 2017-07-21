package com.game.dhanraj.myownalexa;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class LoginWebViewActivity extends Activity {

    private WebView webView;

    private static final int RESULT_LOGIN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_web_view);

        webView = (WebView) findViewById(R.id.webview);
        webView.setWebViewClient(mWebClient);

        Intent i =  getIntent();
        Uri data = i.getData();

        if(data!=null){
            webView.loadUrl(data.toString());
        }else{
            finish();
        }
    }


    WebViewClient mWebClient = new WebViewClient(){
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if(request.getUrl().toString().startsWith("http") || request.getUrl().toString().startsWith("https")){
                return super.shouldOverrideUrlLoading(view,request);
            }

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(request.getUrl().toString()));
            startActivityForResult(i,RESULT_LOGIN);

            return true;
        }
    };
}

package com.example.vidio;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TermsandConditions extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_termsand_conditions);

        WebView web=findViewById(R.id.termswebview);
        web.setWebViewClient(new WebViewClient());
        web.loadUrl("https://www.termsandconditionsgenerator.com/live.php?token=snFX7korCto7Ka6CMmyHQocKhc1kzbcJ");

    }
}
package com.shiv.testapp1;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

public class Main3Activity extends AppCompatActivity {

    WebView w;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        w= (WebView) findViewById(R.id.web);
    }
    public void navigate(View v){
        w.loadUrl("file:///android_asset/aboutt.html");
    }
}

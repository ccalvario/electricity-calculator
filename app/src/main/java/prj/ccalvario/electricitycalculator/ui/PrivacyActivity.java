package prj.ccalvario.electricitycalculator.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

import prj.ccalvario.electricitycalculator.R;

public class PrivacyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);

        WebView myWebView = findViewById(R.id.webview_privacy);
        myWebView.loadUrl("https://ccalvario.github.io/electricity-calculator/privacy/");
    }
}

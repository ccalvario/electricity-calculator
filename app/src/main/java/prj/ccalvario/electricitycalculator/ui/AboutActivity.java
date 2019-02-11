package prj.ccalvario.electricitycalculator.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import prj.ccalvario.electricitycalculator.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView versionTextview = findViewById(R.id.textview_about_version);
        String version = getResources().getString(R.string.app_version);
        versionTextview.setText("Version: " + version);
    }
}

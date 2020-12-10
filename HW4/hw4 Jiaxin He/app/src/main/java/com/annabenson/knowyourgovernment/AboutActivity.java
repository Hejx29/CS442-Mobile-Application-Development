package com.annabenson.knowyourgovernment;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;


public class AboutActivity extends AppCompatActivity {

    //SET PARENT ACTIVITY IN THE MANIFEST

    private static final String TAG = "About_Activity";

    private TextView titleView;
    private TextView copyrightView;
    private TextView versionView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        titleView = findViewById(R.id.titleID);
        copyrightView = findViewById(R.id.copyrightID);
        versionView = findViewById(R.id.versionID);

        titleView.setText("Know Your Government");
        copyrightView.setText("© 2020, Jiaxin He");
        versionView.setText("Version 1.0");

        TextView googleApi = (TextView)findViewById(R.id.googleApi);
        googleApi.setMovementMethod(LinkMovementMethod.getInstance());
    }
}

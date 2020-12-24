package com.android.launcher2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import com.android.launcher.R;

public class ActivityWarm extends Activity {
    private static final int GO_HOME = 1000;
    private static final long SPLASH_DELAY_MILLIS = 1000;
    private Button btn_yes;
    int i = 0;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ActivityWarm.GO_HOME /*1000*/:
                    ActivityWarm.this.goHome();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_warm);
        this.btn_yes = (Button) findViewById(R.id.btn_yes);
        this.btn_yes.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ActivityWarm.this.goHome();
            }
        });
    }

    /* access modifiers changed from: private */
    public void goHome() {
        sendBroadcast(new Intent("ActivityWarm_Closed"));
        startActivity(new Intent(this, Launcher.class));
        finish();
    }
}

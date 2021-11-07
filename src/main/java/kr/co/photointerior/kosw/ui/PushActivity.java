package kr.co.photointerior.kosw.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import kr.co.photointerior.kosw.R;

public class PushActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push);

        Intent intent = getIntent();

        String msg = intent.getStringExtra("message");
        Log.v("test", "test start ");
        if (msg != null) {
            Log.v("test", msg);

            /*
            LogUtils.err(TAG, "push receive : " + push.string());
            String topActivity = AUtil.getTopActivity(context);

            KsEvent.Type type = getEventtype(push.getStringData("push_type"));
            KoswApp app = (KoswApp) context.getApplicationContext() ;
            app.push = push ;
            */


        }
    }
}

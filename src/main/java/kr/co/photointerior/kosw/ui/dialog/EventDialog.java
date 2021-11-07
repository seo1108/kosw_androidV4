package kr.co.photointerior.kosw.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.Gravity;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.text.SimpleDateFormat;
import java.util.Date;

import kr.co.photointerior.kosw.R;
import kr.co.photointerior.kosw.widget.KoswButton;

import static android.content.Context.MODE_PRIVATE;


public class EventDialog extends Dialog {
    private String mUrl;

    public EventDialog(@NonNull Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.dialog_event);
        //getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        Window window = getWindow();
        Point size = new Point();

        Display display = window.getWindowManager().getDefaultDisplay();
        display.getSize(size);

        int width = size.x;
        int height = size.y;

        window.setLayout((int) (width * 0.90), (int) (height * 0.90));
        window.setGravity(Gravity.CENTER);
        initializeview();
    }

    private void initializeview() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Date time = new Date();
        String curdate = format.format(time);

        WebView webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

        webView.loadUrl(mUrl);
        webView.setWebViewClient(webViewClient);

        KoswButton btn_close_today = findViewById(R.id.btn_close_today);
        KoswButton btn_close = findViewById(R.id.btn_close);


        btn_close.setOnClickListener(v -> {
            dismiss();
        });

        btn_close_today.setOnClickListener(v -> {
            SharedPreferences prefr = getContext().getSharedPreferences("event", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefr.edit();
            editor.putString("lastopendate", curdate);
            editor.commit();

            dismiss();
        });
    }

    WebViewClient webViewClient = new WebViewClient() {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }
    };
}


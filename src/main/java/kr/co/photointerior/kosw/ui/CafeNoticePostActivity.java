package kr.co.photointerior.kosw.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import java.util.Map;

import kr.co.photointerior.kosw.R;
import kr.co.photointerior.kosw.rest.model.AppUserBase;
import kr.co.photointerior.kosw.rest.model.DataHolder;
import kr.co.photointerior.kosw.rest.model.ResponseBase;
import kr.co.photointerior.kosw.utils.KUtil;
import kr.co.photointerior.kosw.utils.LogUtils;
import kr.co.photointerior.kosw.widget.KoswEditText;
import kr.co.photointerior.kosw.widget.KoswTextView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CafeNoticePostActivity extends BaseActivity {
    private String TAG = LogUtils.makeLogTag(CafeNoticePostActivity.class);
    private KoswEditText et_content;
    private KoswTextView title_done;
    private ImageView btn_back;

    private String mCafeseq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cafe_notice_post);

        mCafeseq = getIntent().getStringExtra("cafeseq");

        findViews();
        attachEvents();
        setInitialData();
    }

    @Override
    protected void findViews() {
        et_content = findViewById(R.id.et_content);
        title_done = findViewById(R.id.title_done);
        btn_back = findViewById(R.id.btn_back);
    }

    @Override
    protected void attachEvents() {
        btn_back.setOnClickListener(v -> {
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
        });

        title_done.setOnClickListener(v -> {
            // 포스팅
            writeNotice();
        });
    }

    @Override
    protected void setInitialData() {
        et_content.setHint("새로운 소식을 남겨보세요.");
    }

    private void writeNotice() {
        showSpinner("");

        String content = et_content.getText().toString();

        if (null == content || "".equals(content)) {
            closeSpinner();
            toast(R.string.warn_cafe_bbs_content_empty);
            return;
        } else if (content.length() > 1000) {
            closeSpinner();
            toast("글쓰기는 1000자를 넘길 수 없습니다.");
            et_content.setText(content.substring(0, 1000));
            return;
        }

        AppUserBase user = DataHolder.instance().getAppUserBase();
        Map<String, Object> query = KUtil.getDefaultQueryMap();
        query.put("user_seq", user.getUser_seq());
        query.put("cafeseq", mCafeseq);
        query.put("contents", content);

        Call<ResponseBase> call = getCafeService().writeNotice(query);

        call.enqueue(new Callback<ResponseBase>() {
            @Override
            public void onResponse(Call<ResponseBase> call, Response<ResponseBase> response) {
                closeSpinner();
                LogUtils.err(TAG, response.raw().toString());
                if (response.isSuccessful()) {
                    ResponseBase base = response.body();
                    if (base.isSuccess()) {
                        toast(R.string.cafe_notice_post_success);
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();
                    } else {
                        toast(R.string.warn_cafe_fail_notice_write);
                    }
                } else {
                    toast(R.string.warn_cafe_fail_notice_write);
                }
            }

            @Override
            public void onFailure(Call<ResponseBase> call, Throwable t) {
                closeSpinner();
                LogUtils.err(TAG, t);
                toast(R.string.warn_server_not_smooth);
            }
        });
    }

    @Override
    protected void onStart() {
        measureStart();
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        measureStop();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        measureStop();
        super.onPause();
    }


    @Override
    protected void onResume() {
        measureStart();
        super.onResume();
    }

}

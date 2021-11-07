package kr.co.photointerior.kosw.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import java.util.Map;

import kr.co.photointerior.kosw.R;
import kr.co.photointerior.kosw.rest.model.AppUserBase;
import kr.co.photointerior.kosw.rest.model.DataHolder;
import kr.co.photointerior.kosw.rest.model.ResponseBase;
import kr.co.photointerior.kosw.ui.dialog.DialogCommon;
import kr.co.photointerior.kosw.utils.AbstractAcceptor;
import kr.co.photointerior.kosw.utils.Acceptor;
import kr.co.photointerior.kosw.utils.KUtil;
import kr.co.photointerior.kosw.utils.LogUtils;
import kr.co.photointerior.kosw.widget.KoswButton;
import kr.co.photointerior.kosw.widget.KoswEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BbsEditActivity extends BaseActivity {
    private String TAG = LogUtils.makeLogTag(BbsDetailActivity.class);
    private Dialog mDialog;
    private KoswEditText et_content;
    private KoswButton btn_edit, btn_cancel;
    private ImageView btn_back;

    private String mBbsseq, mContent;
    private int mBbsCreateResultCode = 1000;
    private int mBbsCreateCancelResultCode = 2000;

    Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        setContentView(R.layout.activity_bbs_edit);

        mBbsseq = getIntent().getStringExtra("bbsseq");
        mContent = getIntent().getStringExtra("content");

        findViews();
        attachEvents();
        setInitialData();
    }

    @Override
    protected void findViews() {
        et_content = findViewById(R.id.et_content);

        btn_edit = findViewById(R.id.btn_edit);
        btn_cancel = findViewById(R.id.btn_cancel);

        btn_back = findViewById(R.id.btn_back);
    }

    @Override
    protected void attachEvents() {
        btn_edit.setOnClickListener(v -> {
            showConfirmPopup();
        });

        btn_cancel.setOnClickListener(v -> {
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            mActivity.finish();
        });

        btn_back.setOnClickListener(v -> {
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            mActivity.finish();
        });
    }

    private void showConfirmPopup() {
        if (!isFinishing()) {
            if (mDialog != null) {
                mDialog.dismiss();
            }
            Acceptor acceptor = new AbstractAcceptor() {
                @Override
                public void accept() {
                    modifyBbs();
                }
            };
            String msg = getString(R.string.txt_confirm_message);
            mDialog =
                    new DialogCommon(this,
                            acceptor,
                            getString(R.string.txt_warn),
                            msg,
                            new String[]{getString(R.string.txt_cancel), null, getString(R.string.txt_confirm)});
            mDialog.setCancelable(false);
            mDialog.show();
        }
    }

    private void modifyBbs() {
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
        query.put("bbsseq", mBbsseq);
        query.put("content", content);

        Call<ResponseBase> call = getCafeService().modifyBbs(query);

        call.enqueue(new Callback<ResponseBase>() {
            @Override
            public void onResponse(Call<ResponseBase> call, Response<ResponseBase> response) {
                LogUtils.err(TAG, response.raw().toString());
                if (response.isSuccessful()) {
                    ResponseBase base = response.body();
                    if (base.isSuccess()) {
                        toast(R.string.cafe_bbs_edit_success);
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();
                    } else {
                        toast(R.string.warn_cafe_fail_bbs_edit);
                    }
                } else {
                    toast(R.string.warn_cafe_fail_bbs_edit);
                }
            }

            @Override
            public void onFailure(Call<ResponseBase> call, Throwable t) {
                LogUtils.err(TAG, t);
                toast(R.string.warn_server_not_smooth);
            }
        });
    }

    @Override
    protected void setInitialData() {
        et_content.setText(mContent);
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

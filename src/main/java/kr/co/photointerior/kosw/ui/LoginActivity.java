package kr.co.photointerior.kosw.ui;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import kr.co.photointerior.kosw.R;
import kr.co.photointerior.kosw.listener.ClickListener;
import kr.co.photointerior.kosw.rest.model.AppUser;
import kr.co.photointerior.kosw.rest.model.DataHolder;
import kr.co.photointerior.kosw.utils.AUtil;
import kr.co.photointerior.kosw.utils.LogUtils;
import kr.co.photointerior.kosw.utils.StringUtil;

/**
 * 계단왕 로그인 화면.
 * 저장된 상용자 토큰이 존재할 경우 메인으로 이동.
 */
public class LoginActivity extends BaseUserActivity {
    private String TAG = LogUtils.makeLogTag(LoginActivity.class);

    private com.kakao.usermgmt.LoginButton mBtnKakao;

    private SessionCallback callback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        changeStatusBarColor(getCompanyColor());
        findViews();
        attachEvents();
        //getAppKeyHash();
        initKakao();

    }

    @Override
    protected void findViews() {
    }

    @Override
    protected void attachEvents() {
        ClickListener clickListener = new ClickListener(this);
        getView(R.id.btn_login).setOnClickListener(clickListener);
        getView(R.id.btn_signup).setOnClickListener(clickListener);
        getView(R.id.btn_find_pwd).setOnClickListener(clickListener);
        getEditText(R.id.input_name).setOnEditorActionListener(mEditorAtionListener);
        getEditText(R.id.input_pwd).setOnEditorActionListener(mEditorAtionListener);

        findViewById(R.id.ac_login_fl_kakao_login).setOnClickListener(clickListener);
    }

    @Override
    public void performClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btn_login:
                checkInputData();
                break;
            case R.id.btn_signup:
                //callActivity(SignUpExtraInfoActivity.class, false);
                callActivity(SignUpActivity.class, false);
                break;
            case R.id.btn_find_pwd:
                callActivity(FindPasswordActivity.class, false);
                break;

            case R.id.ac_login_fl_kakao_login:
                mBtnKakao.performClick();
                break;
        }
    }

    // 카카오톡 초기화
    private void initKakao() {
        mBtnKakao = findViewById(R.id.com_kakao_login);
        callback = new SessionCallback();
        Session.getCurrentSession().addCallback(callback);
        //Session.getCurrentSession().checkAndImplicitOpen();
    }

    /**
     * 로그인 입력 아이디/패스워드 유효성 검사.
     */
    private void checkInputData() {
        String email = getEditText(R.id.input_name).getText().toString();
        if (!AUtil.isValidEmail(email)) {
            showWarn(R.id.input_warn, R.string.warn_email);
            return;
        }
        String pwd = getEditText(R.id.input_pwd).getText().toString();
        if (StringUtil.isEmptyOrWhiteSpace(pwd) || pwd.length() < 8) {
            showWarn(R.id.input_warn, R.string.warn_password);
            return;
        }
        AUtil.toggleSoftKeyboard(getBaseContext(), getEditText(R.id.input_pwd), false);
        tryLogin(email, pwd);
    }


    /*
     * 앱에 저장된 회사코드를 기준으로 회사의 로고를 적용.
     */
    @Override
    protected void setInitialData() {

    }

    @Override
    protected void onResume() {
        super.onResume();

        AppUser user = DataHolder.instance().getAppUser();

        if (user == null) {
            return;
        }

        if (user.getUserId() == null || user.getUserPwd() == null) {
            return;
        }

        if (!user.getUserId().isEmpty() && !user.getUserPwd().isEmpty()) {

            getEditText(R.id.input_name).setText(user.getUserId().toString());
            getEditText(R.id.input_pwd).setText(user.getUserPwd().toString());

            tryLogin(user.getUserId().toString(), user.getUserPwd().toString());
        }

        //Toast.makeText(this, "resume", Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            // 카카오톡
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Session.getCurrentSession().removeCallback(callback);
    }

    // 카카오톡
    private class SessionCallback implements ISessionCallback {

        @Override
        public void onSessionOpened() {
            List<String> keys = new ArrayList<>();
            keys.add("properties.nickname");
            keys.add("properties.profile_image");
            keys.add("kakao_account.email");

            UserManagement.getInstance().me(keys, new MeV2ResponseCallback() {
                @Override
                public void onFailure(ErrorResult errorResult) {
                    String message = "failed to get user info. msg=" + errorResult;
                    Logger.d(message);
                }

                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                    redirectLoginActivity();
                }

                @Override
                public void onSuccess(MeV2Response response) {
                /*Logger.d("user id : " + response.getId());
                Logger.d("email: " + response.getKakaoAccount().getEmail());
                Logger.d("profile image: " + response.getKakaoAccount()
                        .getProfileImagePath());
                redirectMainActivity();*/
                    try {
                        Long l_id = response.getId();
                        if (null == response) {
                            showWarn(R.id.input_warn, R.string.warn_kakao_insufficient);
                        } else if (null == l_id) {
                            showWarn(R.id.input_warn, R.string.warn_kakao_insufficient);
                        } else if (null == response.getNickname()) {
                            showWarn(R.id.input_warn, R.string.warn_kakao_insufficient);
                        } else if (null == response.getKakaoAccount().getEmail()) {
                            showWarn(R.id.input_warn, R.string.warn_kakao_insufficient);
                        } else {
                            String openid = String.valueOf(response.getId());
                            String nickname = response.getNickname();
                            String email = response.getKakaoAccount().getEmail();

                            tryKakaoLogin(openid, email, nickname);
                        }
                    } catch (Exception ex) {
                        //toast("카카오에서 획득한 정보가 부족하여, 로그인할 수 없습니다.");
                        showWarn(R.id.input_warn, R.string.warn_kakao_insufficient);
                    }
                }

            /*@Override
            public void onNotSignedUp() {
                showSignup();
            }*/
            });
        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            if (exception != null) {
                Logger.e(exception);
                //toast("카카오 로그인에 실패하였습니다.");
                //toast("카카오 로그인에 실패하였습니다.  " + exception.toString());
                showWarn(R.id.input_warn, R.string.warn_kakao);
                //Toast.makeText(getApplicationContext(), exception.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void getAppKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                Log.d("keyHash", something);
                Toast.makeText(getApplicationContext(), something, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e("name not found", e.toString());
        }
    }
}

package kr.co.photointerior.kosw.ui;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Application;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.drawable.GradientDrawable;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.Utils;
import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;
import com.squareup.otto.Subscribe;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.photointerior.kosw.R;
import kr.co.photointerior.kosw.conf.AppConst;
import kr.co.photointerior.kosw.db.KsDbWorker;
import kr.co.photointerior.kosw.global.DefaultCode;
import kr.co.photointerior.kosw.global.Env;
import kr.co.photointerior.kosw.listener.ClickListener;
import kr.co.photointerior.kosw.pref.Pref;
import kr.co.photointerior.kosw.pref.PrefKey;
import kr.co.photointerior.kosw.rest.model.AppUserBase;
import kr.co.photointerior.kosw.rest.model.BeaconUuid;
import kr.co.photointerior.kosw.rest.model.Cafe;
import kr.co.photointerior.kosw.rest.model.CafeDetail;
import kr.co.photointerior.kosw.rest.model.DataHolder;
import kr.co.photointerior.kosw.rest.model.PageResponseBase;
import kr.co.photointerior.kosw.rest.model.Profile;
import kr.co.photointerior.kosw.rest.model.ResponseBase;
import kr.co.photointerior.kosw.service.beacon.AltitudeManager;
import kr.co.photointerior.kosw.service.beacon.BLocationManager;
import kr.co.photointerior.kosw.service.beacon.BeaconRagingInRegionService;
import kr.co.photointerior.kosw.service.beacon.DirectionManager;
import kr.co.photointerior.kosw.service.beacon.MeasureObj;
import kr.co.photointerior.kosw.service.beacon.StepManager;
import kr.co.photointerior.kosw.service.beacon.StepSensorService;
import kr.co.photointerior.kosw.service.fcm.Push;
import kr.co.photointerior.kosw.service.net.NetworkConnectivityReceiver;
import kr.co.photointerior.kosw.service.net.NetworkSchedulerService;
import kr.co.photointerior.kosw.service.noti.NotiService;
import kr.co.photointerior.kosw.service.noti.RestartService;
import kr.co.photointerior.kosw.ui.fragment.BaseFragment;
import kr.co.photointerior.kosw.ui.fragment.CityRankFragment;
import kr.co.photointerior.kosw.ui.fragment.FragmentActivityAnalysis;
import kr.co.photointerior.kosw.ui.fragment.FragmentActivityRecord;
import kr.co.photointerior.kosw.ui.fragment.FragmentRankingGroup;
import kr.co.photointerior.kosw.ui.fragment.FragmentRankingIndividual;
import kr.co.photointerior.kosw.ui.fragment.FragmentRankingWalkIndividual;
import kr.co.photointerior.kosw.ui.fragment.GGRFragment;
import kr.co.photointerior.kosw.ui.fragment.MainFragment;
import kr.co.photointerior.kosw.utils.AUtil;
import kr.co.photointerior.kosw.utils.AbstractAcceptor;
import kr.co.photointerior.kosw.utils.Acceptor;
import kr.co.photointerior.kosw.utils.DateUtil;
import kr.co.photointerior.kosw.utils.KUtil;
import kr.co.photointerior.kosw.utils.LogUtils;
import kr.co.photointerior.kosw.utils.StringUtil;
import kr.co.photointerior.kosw.utils.event.BusProvider;
import kr.co.photointerior.kosw.utils.event.KsEvent;
import kr.co.photointerior.kosw.widget.CircleImageView;
import kr.co.photointerior.kosw.widget.MenuRow;
import me.leolin.shortcutbadger.ShortcutBadger;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity class form app main screen.
 */
public class MainActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks, BLocationManager.DelegateFindLocation {
    private String TAG = LogUtils.makeLogTag(MainActivity.class);

    /**
     * current fragment instance.
     */
    private static boolean isTest = false;
    private Location mLocation;
    private Address mAddr;
    private boolean mStarted;
    private double mAltitude;
    private double mAltitude2;
    private double mOrientation;
    private double mX;
    private double mY;
    private double mZ;
    private double mStep;
    private int mFloor;

    private int mSleepCnt = 0;
    static private int mMaxSleepCnt = 10;
    static private String sleepMsg[] = {
            "걷기중 측정시간(30초)이 지났습니다.",
            "계단이용 측정시간(5분)이 지났습니다."
    };
    private int sleepMode = 0;


    private double mSaveStep = 0;
    private Boolean isSleep = false;
    private Boolean isSleepBtn = false;

    private Boolean isActivity = false;

    private long startTime = 0;
    private long endTime = 0;
    private Boolean isRedDot = false;

    private BaseFragment mFragmentCurrent;
    private int[] mMenuIds = {
            R.id.menu_activity_gps,
            R.id.menu_cafe_name,
            R.id.menu_cafe_board,
            R.id.menu_cafe_daily,
            R.id.menu_cafe_weekly,
            R.id.menu_cafe_monthly,
            R.id.menu_activity_record,
            R.id.menu_analysis,
            R.id.menu_ranking_private,
            R.id.menu_ranking_walk_private,
            R.id.menu_ranking_group,
            R.id.menu_ggr,
            R.id.menu_city_ranking,
            R.id.menu_cafe,
            /*R.id.menu_my_cafe,
            R.id.menu_cafe_list,
            R.id.menu_make_cafe,
            R.id.menu_join_cafe,*/
            R.id.menu_signcafe,
            R.id.menu_help,
            R.id.menu_altitude,
            R.id.menu_plus_friend

    };
    private String mHomeTitle;

    private ImageButton mPauseBtn;

    private BroadcastReceiver mExitReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Env.Action.EXIT_ACTION.isMatch(intent.getAction())) {
                callActivity(LoginActivity.class, true);
            }
        }
    };

    protected BroadcastReceiver mIsAppBackgroundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isActivity) {
                if (Env.Action.APP_IS_BACKGROUND_ACTION.isMatch(intent.getAction())) {
                    displayFragment(Env.FragmentType.HOME);
                }
            }
        }
    };

    private BeaconUuid mTodayActivity;

    private AltitudeManager mAltiManager;
    private StepManager mStepManager;
    private DirectionManager mDirectionManager;

    private boolean isStart = false;
    private boolean isTurn = false;
    private boolean isContinue = false;
    // Create the Handler
    private Handler handler = new Handler();
    private ArrayList<MeasureObj> mStartList = new ArrayList<>();
    private ArrayList<MeasureObj> mUpList = new ArrayList<>();
    private int cnt = 0;
    private int checkStartTimeOut = 60;
    private int checkRotationTimeOut = 60;
    private int checkUpTimeOut = 60;
    private Boolean is315 = false;
    private int cnt315 = 0;
    private Boolean isCount = false;

    private long goupTime = 0;

    private TextView mTv_log;

    // 건물 층 카운트
    private int mBuildCount = 0;
    private int mCurBuildCount = 0;

    // 건물 / 등산 모드
    private String mIsBuild = ""; // Y :건물계단    N : 등산계단
    private int mClimbCount = 0;  // 등산 카운트 (4미터 체크)
    private int mLogicCount = 0;  // 로직 카운트 (회전 )

    private RestartService restartService;

    private ViewGroup mRootView;

    private boolean isCheck = false;


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Application myApp = Utils.getApp();

        mContentRootView = getWindow().getDecorView().findViewById(android.R.id.content);
        mRootView = (ViewGroup) ((ViewGroup) this
                .findViewById(android.R.id.content)).getChildAt(0);

        //chkGpsService();

        AppUserBase user = DataHolder.instance().getAppUserBase();
        if (null != user) {
            mCurBuildCount = user.getBuild_floor_amt();
            mIsBuild = user.getIsbuild();
        } else {
            callActivity(SplashActivity.class, false);
            mFinishFlag = true;
            finish();
        }

        if (null == mIsBuild || mIsBuild.equals("")) { // 초기 셋팅은 등산 모드
            mIsBuild = "N";
        }

        showStartPopup();

        getApp().isPop = false;
        getApp().isInit = false;

        mTv_log = getTextView(R.id.txt_log);

        if (!isTest) {

            getTextView(R.id.txt_m).setVisibility(View.INVISIBLE);
            getTextView(R.id.txt_height).setVisibility(View.INVISIBLE);
            getTextView(R.id.txt_dir).setVisibility(View.INVISIBLE);
            getTextView(R.id.txt_step).setVisibility(View.INVISIBLE);
            getTextView(R.id.txt_time).setVisibility(View.INVISIBLE);
            getTextView(R.id.txt_count).setVisibility(View.INVISIBLE);
            getTextView(R.id.txt_addr).setVisibility(View.INVISIBLE);
            getTextView(R.id.txt_gps).setVisibility(View.INVISIBLE);
            getTextView(R.id.txt_log).setVisibility(View.INVISIBLE);

        }

        if (!checkLogin()) {
            callActivity(LoginActivity.class, true);
            return;
        }

        /*if(!EasyPermissions.hasPermissions(this, Env.PERMISSIONS)){
            EasyPermissions.requestPermissions(this, getString(R.string.txt_permission_desc), Env.REQ_CODE[3],
                    Env.PERMISSIONS);
        }else {
            //Gps start
            app.getmBLocationManager().registerLocationUpdates();
            app.getmBLocationManager().mDelegateFindLocation = this ;
        }*/

        Crashlytics.setUserIdentifier(Pref.instance().getStringValue(PrefKey.USER_TOKEN, ""));
        clearNotificationBadge();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mTodayActivity = (BeaconUuid) bundle.getSerializable("_TODAY_ACTIVITY_");
        }

        //startService(new Intent(getBaseContext(), BeaconRagingInRegionService.class));
        //checkBluetoothOn();

        LogUtils.err(TAG, "MainActivity#onCreate()");
        //BusProvider.instance().register(this);
        mContentFrame = getView(R.id.content_frame);

        initActionBarAndDrawer();

        findViewById(R.id.LayoutPause).setVisibility(View.INVISIBLE);

        //changeColors();
        findViews();
        displayFragment(Env.FragmentType.HOME);
        attachEvents();
        sendFcmToken();
        setInitialData();

        try {
            registerReceiver(mExitReceiver, new IntentFilter(Env.Action.EXIT_ACTION.action()));
            registerReceiver(mIsAppBackgroundReceiver, new IntentFilter(Env.Action.APP_IS_BACKGROUND_ACTION.action()));

            LocalBroadcastManager.getInstance(this).registerReceiver(
                    mCharacterChangeReceiver,
                    new IntentFilter(Env.Action.CHARACTER_CHANGED_ACTION.action())
            );
        } catch (Exception e) {
        }
        /*mStartList.clear();
        for (int i = 0; i < 600; i++) {
            mStartList.add(new MeasureObj());
        }*/

        scheduleJob();//network state monitoring

        /*mAltiManager = new AltitudeManager(this);
        mStepManager = new StepManager(this) ;
        mDirectionManager = new DirectionManager(this) ;

        mSleepCnt = 0 ;*/


/*

        /////==================== 계단 업 테스트 =================/////
        //
        //sendDataToServer(1) ;

        //Thread mMainTread = new Thread(runnable) ;
        //mMainTread.start();





*/

        //getAppKeyHash();


        // 계단 측정 서비스 실행
        //Intent walk_intent = new Intent(this, StepCounterService.class);
        //startService(walk_intent);

        /*if(!isMyServiceRunning(NotiService.class)) {
            // 노티피케이션 서비스 실행
            Intent noti_intent = new Intent(this, NotiService.class);
            startService(noti_intent);
        }


        SharedPreferences prefr = getSharedPreferences("background", MODE_PRIVATE);
        String background = prefr.getString("background", "auto");

        if (null != background && "auto".equals(background)) {
            findViewById(R.id.LayoutPause).setVisibility(View.VISIBLE);
            TextView tv =  findViewById(R.id.tvPauseMent);
            tv.setText("자동측정중입니다.");

            if (!isMyServiceRunning(StepCounterService.class)) {
                // 자동측정 서비스 실행
                Intent intent = new Intent(this, StepCounterService.class);
                startService(intent);
            }
        }*/

        /******** 임시주석 ********/
        //if(!isMyServiceRunning(StepCounterService.class)) {
        //    startMeasure(true);
        //    handler.post(runnable);
        //}
        /**************************/

        /*startMeasure(true);
        handler.post(runnable);*/

        //startCalcStairs();

        try {
            String gocafemain = getIntent().getStringExtra("_CAFEMAIN_ACTIVITY_");

            if (null != gocafemain && "GOCAFEMAIN".equals(gocafemain)) {
                callActivity(CafeMainActivity.class, false);
            }


            SharedPreferences prefr = getSharedPreferences("userInfo", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefr.edit();
            if (null != user) {
                editor.putString("token", user.getUserToken());
                editor.putInt("beacon_seq", user.getBeacon_seq());
                editor.putInt("stair_seq", user.getStair_seq());
                editor.putString("build_seq", user.getBuild_seq());
                editor.putString("build_name", user.getBuild_name());
                editor.putInt("cust_seq", user.getCust_seq());
                editor.putString("cust_name", user.getCust_name());
                editor.putString("build_code", user.getBuildingCode());
                editor.putString("country", user.getCountry());
                editor.putString("city", user.getCity());
                editor.putInt("curBuildCount", user.getBuild_floor_amt());
                editor.putInt("user_seq", Pref.instance().getIntValue(PrefKey.USER_SEQ, -1));
                editor.commit();
            } else {
                editor.putString("token", "");
                editor.putInt("beacon_seq", 0);
                editor.putInt("stair_seq", 0);
                editor.putString("build_seq", "");
                editor.putString("build_name", "");
                editor.putInt("cust_seq", 0);
                editor.putString("cust_name", "");
                editor.putString("build_code", "");
                editor.putString("country", "");
                editor.putString("city", "");
                editor.putInt("curBuildCount", 1000);
                editor.putInt("user_seq", -1);
                editor.commit();
            }

            // 죽지않는 서비스 구현
            restartService = new RestartService();
            Intent intent = new Intent(MainActivity.this, NotiService.class);

            IntentFilter intentFilter = new IntentFilter("kr.co.photointerior.kosw.service.noti.NotiService");
            //브로드 캐스트에 등록
            registerReceiver(restartService, intentFilter);
            // 서비스 시작
            startService(intent);

            AppConst.IS_MAIN_RUNNED = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // Insert custom code here
            if (mStarted) {
                checkFloor();
            }
            // Repeat every 2 seconds
            handler.postDelayed(runnable, 100);
        }
    };

    public void checkFloor() {
        checkStart();
        /*if (!isMyServiceRunning(StepCounterService.class)) {
            checkStart();
        } else {
            handler.removeCallbacks(runnable);
            startMeasure(false);
        }*/
    }

    private void checkStart() {
        if (mAltitude == 0) {
            //return ;
        }
        Log.d("999999999999", cnt + "__");
        // 30초 이상 걷기 없으면 잠금
        if (cnt >= 30 * 10) {
            if (mSaveStep <= 0) { // 걷기중이아니면
                mSleepCnt = mMaxSleepCnt;
                sleepMode = 0;
                initMeasure();
                return;
            } else {  // 120초이상 측정이 없으면 측정 잠금  mSleepCnt >= 4
                sleepMode = 1;
                mSleepCnt++;
                initMeasure();
                return;
            }
        }

        mSaveStep = 0;

        MeasureObj obj = mStartList.get(cnt);
        obj.altitude = mAltitude;
        obj.orientation = mOrientation;
        obj.step = mStep;
        obj.x = mX;
        obj.y = mY;
        obj.z = mZ;

        MeasureObj obj_b = mStartList.get(0);
        obj.altitudeGap = obj.altitude - obj_b.altitude;
        obj.orientationGap = Math.abs(obj.orientation - obj_b.orientation);
        obj.stepGap = Math.abs(obj.step - obj_b.step);
        obj.xGap = Math.abs(obj.x - obj_b.x);
        obj.yGap = Math.abs(obj.y - obj_b.y);
        obj.zGap = Math.abs(obj.z - obj_b.z);

        // 고도 합계
        double gapAlitude = 0;
        gapAlitude = obj.altitude - obj_b.altitude;

        // 스텝 합계
        double gapStep = 0;
        gapStep = obj.step - obj_b.step;
        if (cnt > 50) {
            MeasureObj obj_c;
            obj_c = mStartList.get(cnt - 50);
            obj.stepGap = Math.abs(obj.step - obj_c.step);
            gapStep = obj.step - obj_c.step;
        }

        double step = gapStep; // 초당 걸음수
        mSaveStep = step;

        List<Double> list = Arrays.asList(obj.xGap, obj.yGap, obj.zGap);
        Double mDir = Collections.max(list);

        //Log.v("kmj",String.format("count %d ,높이 :  %.2f , 걸음수 : %.2f , 방향 : %.2f",cnt,gapAlitude,step,mDir)) ;
        //tvStep.setText(String.format("스텝 : %.2f" ,step));
        //mValue.setText(String.format("높이 : %.2fm \n 방향  %.2f " , gapAlitude ,mDir)  );
        //getTextView(R.id.txt_m).setText("");
        String m = String.format("높이 : %.2f , 방향 : %.2f , 걷기 : %.2f , 시간 : %d", gapAlitude, mDir, step, cnt);
        if (isTest) {
            // ==========================   test    ==============================
            getTextView(R.id.txt_height).setText(String.format("높이 : %.2f", gapAlitude));
            getTextView(R.id.txt_dir).setText(String.format("방향 : %.2f", mDir));
            getTextView(R.id.txt_step).setText(String.format("걷기 : %.2f", step));
            getTextView(R.id.txt_time).setText(String.format("시간 : %d (%d)", 300 - cnt, mSleepCnt));
            getTextView(R.id.txt_count).setText(String.format("%d  (B:%d,M:%d)", mFloor, mLogicCount, mClimbCount));
            if (mAddr != null && mAddr.getAddressLine(0) != null) {
                getTextView(R.id.txt_addr).setText(String.format("%s", mAddr.getAddressLine(0)));
            }
            if (mLocation != null) {
                getTextView(R.id.txt_gps).setText(String.format("%s,%s", String.valueOf(mLocation.getLatitude()), String.valueOf(mLocation.getLongitude())));
            }
        }

        if (!isContinue) {
            if (mDir > 135 && Math.abs(gapAlitude) > 1.5) {
                // 2초 이내 측정이면  카운트 하지 않음 엘리베이터 사용자 걸름
                long curTime = System.currentTimeMillis();
                if (cnt < 30 || (curTime - goupTime) < 3000) {
                    mSleepCnt++;
                    initMeasure();
                    return;
                }

                if (step > 1) { // 걷기중이면

                    AppUserBase user = DataHolder.instance().getAppUserBase();
                    mCurBuildCount = user.getBuild_floor_amt();
                    mIsBuild = user.getIsbuild();

                    if (gapAlitude > 0) {
                        mFloor++;
                        mLogicCount++;
                        mClimbCount = 0;

                        if (mIsBuild.equals("Y")) {
                            mBuildCount++;
                        } else {
                            // 건물 모드 :건물높이의 1⁄2 이상이면 건물로 전환
                            if (mLogicCount >= Math.ceil(mCurBuildCount / 2)) {
                                mIsBuild = "Y";
                                mBuildCount = mLogicCount;
                            }
                        }

                        if (mBuildCount == 1) {
                            startTime = System.currentTimeMillis();
                        }

                        if (mBuildCount == Math.ceil(mCurBuildCount / 2) && mBuildCount >= 4) {
                            endTime = System.currentTimeMillis();
                            isRedDot = true;
                        } else {
                            isRedDot = false;
                        }
                        isCount = true;
                        if (isTest) {
                            getTextView(R.id.txt_m).setText(m);
                        }

                        goupTime = System.currentTimeMillis();
                        sendDataToServer(1);
                    } else {
                        mBuildCount = 0;
                        mClimbCount = 0;
                        mLogicCount = 0;

                        mFloor++;
                        isCount = true;
                        if (isTest) {
                            getTextView(R.id.txt_m).setText(m);
                        }
                        goupTime = System.currentTimeMillis();
                        sendDataToServer(1);
                    }

                    // 90이상이면 높이는 클리어  방향은 보존
                    MeasureObj obj_bb = mStartList.get(0);
                    isContinue = true;
                    mStartList.clear();
                    for (int i = 0; i < 600; i++) {
                        mStartList.add(new MeasureObj());
                    }
                    cnt = 0;
                    mStartList.get(0).x = obj_bb.x;
                    mStartList.get(0).y = obj_bb.y;
                    mStartList.get(0).z = obj_bb.z;
                    mStartList.get(0).xGap = obj_bb.xGap;
                    mStartList.get(0).yGap = obj_bb.yGap;
                    mStartList.get(0).zGap = obj_bb.zGap;
                    mStartList.get(0).step = obj_bb.step;
                    mStartList.get(0).altitude = mAltitude;

                    cnt++;
                    return;

                }
            }

        }

        if (mDir > 315 && isContinue) {  //
            // 315 이상이면 높이는 클리어  방향은 보존
            MeasureObj obj_bb = mStartList.get(0);
            isContinue = false;
            for (int i = 0; i < cnt; i++) {
                mStartList.get(i).x = obj_bb.x;
                mStartList.get(i).y = obj_bb.y;
                mStartList.get(i).z = obj_bb.z;
                mStartList.get(i).xGap = obj_bb.xGap;
                mStartList.get(i).yGap = obj_bb.yGap;
                mStartList.get(i).zGap = obj_bb.zGap;
                mStartList.get(i).step = obj_bb.step;
                mStartList.get(i).altitude = mAltitude;
            }
            mStartList.get(cnt).x = obj_bb.x;
            mStartList.get(cnt).y = obj_bb.y;
            mStartList.get(cnt).z = obj_bb.z;
            mStartList.get(cnt).xGap = obj_bb.xGap;
            mStartList.get(cnt).yGap = obj_bb.yGap;
            mStartList.get(cnt).zGap = obj_bb.zGap;
            mStartList.get(cnt).step = obj_bb.step;
            mStartList.get(cnt).altitude = mAltitude;

            is315 = true;
            cnt315 = cnt;

            cnt++;
            return;

        }

        // 1초뒤에 방향만 클리어
        if (is315 && cnt > (cnt315 + 10)) {  //
            MeasureObj obj_bb = mStartList.get(0);

            for (int i = 0; i < cnt; i++) {
                mStartList.get(i).x = mX;
                mStartList.get(i).y = mY;
                mStartList.get(i).z = mZ;
                mStartList.get(i).step = obj_bb.step;
                mStartList.get(i).altitude = obj_bb.altitude;
            }
            mStartList.get(cnt).x = mX;
            mStartList.get(cnt).y = mY;
            mStartList.get(cnt).z = mZ;
            mStartList.get(cnt).step = obj_bb.step;
            mStartList.get(cnt).altitude = obj_bb.altitude;
            is315 = false;
            cnt315 = 0;

            cnt++;
            return;
        }

        //=================================
        // 회전이 없고 3미터 이상이면 1층 측정
        //=================================

        if (Math.abs(gapAlitude) > 3) {
            long curTime = System.currentTimeMillis();
            if (cnt < 20 || (curTime - goupTime) < 2000) {
                //mSleepCnt++;
                initMeasure();
                return;
            } else {
                if (step > 1) { // 걷기중이면
                    if (gapAlitude > 0) {

                        AppUserBase user = DataHolder.instance().getAppUserBase();
                        mCurBuildCount = user.getBuild_floor_amt();
                        mIsBuild = user.getIsbuild();
                        mClimbCount++;
                        mLogicCount = 0;

                        if (mIsBuild.equals("Y")) {
                            if (mClimbCount > mCurBuildCount) {
                                mIsBuild = "N";
                                mFloor++;
                                mBuildCount = mClimbCount;

                                if (mBuildCount == 1) {
                                    startTime = System.currentTimeMillis();
                                }
                                if (mBuildCount == Math.ceil(mCurBuildCount / 2) && mBuildCount >= 3) {
                                    endTime = System.currentTimeMillis();
                                    isRedDot = true;
                                } else {
                                    isRedDot = false;
                                }
                                if (isTest) {
                                    getTextView(R.id.txt_m).setText(m);
                                }
                                goupTime = System.currentTimeMillis();
                                sendDataToServer(1);
                            }
                        } else {
                            mBuildCount++;
                            mFloor++;
                            if (mBuildCount == 1) {
                                startTime = System.currentTimeMillis();
                            }
                            if (mBuildCount == Math.ceil(mCurBuildCount / 2) && mBuildCount >= 3) {
                                endTime = System.currentTimeMillis();
                                isRedDot = true;
                            } else {
                                isRedDot = false;
                            }
                            if (isTest) {
                                getTextView(R.id.txt_m).setText(m);
                            }
                            goupTime = System.currentTimeMillis();
                            sendDataToServer(1);
                        }
                    } else {
                        mBuildCount = 0;
                        mClimbCount = 0;
                        mLogicCount = 0;
                        if (mIsBuild.equals("Y")) {
                            //mClimbCount++;
                        } else {
                            mFloor++;
                            if (isTest) {
                                getTextView(R.id.txt_m).setText(m);
                            }
                            goupTime = System.currentTimeMillis();
                            sendDataToServer(1);
                        }
                    }
                    initMeasure();
                    return;


                }
            }

        }

        if (mDir > 400) {
            initMeasure();
            return;
        }

        cnt++;
    }


    private void clearMeasur() {

        MeasureObj obj_bb = mStartList.get(cnt);

        for (int i = 0; i < cnt; i++) {
            mStartList.get(i).x = obj_bb.x;
            mStartList.get(i).y = obj_bb.y;
            mStartList.get(i).z = obj_bb.z;
            mStartList.get(i).xGap = obj_bb.xGap;
            mStartList.get(i).yGap = obj_bb.yGap;
            mStartList.get(i).zGap = obj_bb.zGap;
            mStartList.get(i).altitude = obj_bb.altitude;
            mStartList.get(i).step = obj_bb.step;
        }

        cnt++;

        isStart = false;
        //isTurn = false ;
        isContinue = false;
        is315 = false;
        cnt315 = 0;
        goupTime = System.currentTimeMillis();

    }

    private void initMeasure() {

        /*
        if ( cnt > 0 ) {
            cnt = cnt - 1;
            MeasureObj obj_bb = mStartList.get(cnt);

            mStartList.clear();

            if (mStartList.size() == 0) {
                for (int i = 0; i < 600; i++) {
                    mStartList.add(new MeasureObj());
                }
            }

            for (int i = 0; i < 600; i++) {
                mStartList.get(i).x = obj_bb.x;
                mStartList.get(i).y = obj_bb.y;
                mStartList.get(i).z = obj_bb.z;
                mStartList.get(i).xGap = obj_bb.xGap;
                mStartList.get(i).yGap = obj_bb.yGap;
                mStartList.get(i).zGap = obj_bb.zGap;
                mStartList.get(i).step = obj_bb.step;
                mStartList.get(i).altitude = obj_bb.altitude;
            }

            cnt = 1;
        } else {
        }
        */
        mStartList.clear();

        if (mStartList.size() == 0) {
            for (int i = 0; i < 600; i++) {
                mStartList.add(new MeasureObj());
            }
        }

        cnt = 0;

        isStart = false;
        isTurn = false;
        isContinue = false;
        is315 = false;
        cnt315 = 0;

        isCount = false;
        goupTime = System.currentTimeMillis();

        // 5분 지나면 잠금
        if (mSleepCnt >= mMaxSleepCnt) {
            isSleep = true;
            ;
            mSleepCnt = 0;
            mStarted = false;
            getTextView(R.id.tvPauseMent).setText(sleepMsg[sleepMode]);
            startMeasure(false);
        }

    }

    // 빌딩 층수 카운트 (빌딩 높이 )
    private void saveBuildCount(int count) {

    }

    // 측정 시작 ,멈춤
    public void startMeasure(boolean started) {
        if (!started) {

            saveBuildCount(mBuildCount);


            goupTime = System.currentTimeMillis();

            mAltitude = 0;
            mOrientation = 0;
            mStep = 0;
            mX = 0;
            mY = 0;
            mZ = 0;

            for (int i = 0; i < 600; i++) {
                MeasureObj obj = mStartList.get(i);
                obj.altitude = 0.0;
                obj.orientation = 0.0;
                obj.step = 0.0;
                obj.x = 0.0;
                obj.y = 0.0;
                obj.z = 0.0;
                obj.xGap = 0.0;
                obj.yGap = 0.0;
                obj.zGap = 0.0;
            }
            cnt = 0;

            mAltiManager.stopMeasure();
            mDirectionManager.stopMeasure();
            mStepManager.stopMeasure();
            findViewById(R.id.LayoutPause).setVisibility(View.INVISIBLE);
           /*if(isMyServiceRunning(StepCounterService.class)) {
               // 자동측정중이면
               TextView tv =  findViewById(R.id.tvPauseMent);
               tv.setText("자동측정중입니다.");
           }*/


        } else {


            AppUserBase user = DataHolder.instance().getAppUserBase();

            if (user != null) {
                mCurBuildCount = user.getBuild_floor_amt(); // 현재 빌딩층수 (높이)
            }

            if (!isSleep) {
                initMeasure();
            }

            goupTime = System.currentTimeMillis();

            mAltitude = 0;
            mOrientation = 0;
            mStep = 0;
            mX = 0;
            mY = 0;
            mZ = 0;

            for (int i = 0; i < 600; i++) {
                MeasureObj obj = mStartList.get(i);
                obj.altitude = 0.0;
                obj.orientation = 0.0;
                obj.step = 0.0;
                obj.x = 0.0;
                obj.y = 0.0;
                obj.z = 0.0;
                obj.xGap = 0.0;
                obj.yGap = 0.0;
                obj.zGap = 0.0;

            }
            cnt = 0;

            mAltiManager = new AltitudeManager(this);
            mStepManager = new StepManager(this);
            mDirectionManager = new DirectionManager(this);

            isSleep = false;

            mAltiManager.startMeasure();
            mDirectionManager.startMeasure();
            mStepManager.startMeasure();
            /*if (null != mStepManager) {
                mStepManager.restartMeasure();
            } else {
                mStepManager.startMeasure();
            }*/
            findViewById(R.id.LayoutPause).setVisibility(View.INVISIBLE);
            //mValue.setText("wait...");
        }
        mStarted = started;
    }

    private void restartMeasure() {
        if (!mStarted) {
            Toast.makeText(this, "측정이 시작되지 않았습니다.", Toast.LENGTH_SHORT).show();
        } else {
            mAltiManager.restartMeasure();
            //mValue.setText("wait...");
        }
    }

    public void setCurrentAltitude(double altitude) {
        mAltitude = altitude;
        displayAlti();
    }

    public void setCurrentAltitude2(double altitude) {
        mAltitude2 = altitude;
        displayAlti();
    }

    public void setCurrentOrientation(double val) {
        mOrientation = val;
        displayAlti();
    }

    public void setCurrentOrientation2(double x, double y, double z) {
        mX = x;
        mY = y;
        mZ = z;
        //displayAlti();
    }

    public void setCurrentStep(double val) {
        mStep += val;
        displayAlti();
    }

    private void displayAlti() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    private String getDirectionFromDegrees(float degrees) {
        if (degrees >= -22.5 && degrees < 22.5) {
            return "N";
        }
        if (degrees >= 22.5 && degrees < 67.5) {
            return "NE";
        }
        if (degrees >= 67.5 && degrees < 112.5) {
            return "E";
        }
        if (degrees >= 112.5 && degrees < 157.5) {
            return "SE";
        }
        if (degrees >= 157.5 || degrees < -157.5) {
            return "S";
        }
        if (degrees >= -157.5 && degrees < -112.5) {
            return "SW";
        }
        if (degrees >= -112.5 && degrees < -67.5) {
            return "W";
        }
        if (degrees >= -67.5 && degrees < -22.5) {
            return "NW";
        }

        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void scheduleJob() {
        JobInfo myJob = new JobInfo.Builder(0, new ComponentName(this, NetworkSchedulerService.class))
                .setRequiresCharging(true)
                .setMinimumLatency(1000)
                .setOverrideDeadline(2000)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .build();

        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(myJob);
    }


    /**
     * 계단을 올라간 데이터를 서버로 전송
     */

    private void sendDataToServer(int goupAmt) {
        //Toast.makeText(this, "Send to Server2", Toast.LENGTH_SHORT).show();
        mSleepCnt = 0;
        mTv_log.setText("save start");

        String token = KUtil.getUserToken();
        String buildCode = KUtil.getBuildingCode();
        if (StringUtil.isEmptyOrWhiteSpace(token) || StringUtil.isEmptyOrWhiteSpace(buildCode)) {
            mTv_log.setText("save fail - none buildcode ");
            return;
        }

        AppUserBase user = DataHolder.instance().getAppUserBase();
        //return;
        try {
            Map<String, Object> query = new HashMap<>();
            query.put("token", user.getUserToken());
            query.put("beacon_uuid", "");
            query.put("major_value", "");
            query.put("minor_value", "");
            query.put("install_floor", "");
            //map.put("beacon_seq", getBeaconSeq());
            query.put("beacon_seq", user.getBeacon_seq());
            if (user.getBeacon_seq() == 0) {
                DefaultCode.BEACON_SEQ.getValue();
            }
            query.put("stair_seq", user.getStair_seq());
            query.put("build_seq", user.getBuild_seq());
            query.put("build_name", user.getBuild_name());
            query.put("floor_amount", 0);
            query.put("stair_amount", 0);
            query.put("cust_seq", user.getCust_seq());
            query.put("cust_name", user.getCust_name());
            query.put("build_code", user.getBuildingCode());
            query.put("godo", mAltitude);
            query.put("goup_amt", goupAmt);
            query.put("curBuildCount", mCurBuildCount);
            query.put("buildCount", mBuildCount);
            query.put("isbuild", mIsBuild);
            query.put("country", user.getCountry());
            query.put("city", user.getCity());

            if (isRedDot) {
                query.put("start_time", startTime);
                query.put("end_time", endTime);
            }

            final String localTime = DateUtil.currentDate("yyyyMMddHHmmss");
            if (NetworkConnectivityReceiver.isConnected(getApplicationContext())) {
                Call<BeaconUuid> call = getAppService().sendStairGoUpAmountToServer(query);
                final String goupSentTime = DateUtil.currentDate("yyyyMMdd HHmmss");

                call.enqueue(new Callback<BeaconUuid>() {
                    @Override
                    public void onResponse(Call<BeaconUuid> call, Response<BeaconUuid> response) {
                        if (response.isSuccessful()) {
                            BeaconUuid uuid = response.body();
                            if (uuid != null && uuid.isSuccess()) {
                                broadcastServerResult(uuid);

                                SharedPreferences pref = getSharedPreferences("unsent", MODE_PRIVATE);
                                SharedPreferences.Editor editor = pref.edit();
                                editor.putInt("unsent", 0);
                                editor.commit();
                            } else {
                            }
                        } else {
                        }
                    }

                    @Override
                    public void onFailure(Call<BeaconUuid> call, Throwable t) {
                    }
                });
            } else {
            }
        } catch (Exception e) {
        }
    }

    /**
     * 서버로 로그 전송
     */
    public synchronized void sendBeaconLog(BeaconUuid bu, int goupAmt, String desc, String logTime) {
        if (bu == null) {
            return;
        }
        Map<String, Object> query = KUtil.getDefaultQueryMap();
        query.put("alti", mAltitude);
        query.put("device_model", AUtil.getDeviceModel() + "(" + AUtil.getOsReleaseVersion() + ")");
        query.put("app_ver", AUtil.getVersionCode(this, getPackageName()));
        query.put("app_desc", "(" + bu.getOriginalBeaconSeq() + ") " + desc + " notscancnt=" +
                " ble=" + KUtil.isBluetoothOn() + " gps=" + AUtil.isLocationEnabled(this) +
                " app_time=" + (logTime == null ? DateUtil.currentDate("yyyyMMdd HHmmss") : logTime));
        if (bu != null) {
            query.put("beacon_seq", bu.getBeaconSeq());
            query.put("minor_value", bu.getMinorValue());
            query.put("build_seq", bu.getBuildSeq());
            query.put("goup_amt", goupAmt);
            //query.put("godo_list", Arrays.toString(mGodoList.toArray(new String[mGodoList.size()])));
            //query.put("godo_index", mGodoIndex);
        } else {
            query.put("beacon_seq", "0");
            query.put("minor_value", "0");
            query.put("build_seq", "0");
            query.put("goup_amt", "-100");
            query.put("godo_list", "0");
            query.put("godo_index", "0");
        }

        Call<ResponseBase> call = getUserService().sendBeaconLog(query);
        call.enqueue(new Callback<ResponseBase>() {
            @Override
            public void onResponse(Call<ResponseBase> call, Response<ResponseBase> response) {
                LogUtils.err(TAG, "service log response:" + response.raw().toString());
            }

            @Override
            public void onFailure(Call<ResponseBase> call, Throwable t) {
                LogUtils.err(TAG, "service log fail:" + Log.getStackTraceString(t));
            }
        });
    }

    /**
     * 서버로 로그 전송
     */
    public void sendServiceLog(String msg) {
        /*if(!Env.Bool.SERVER_LOG_ENABLE.getValue()){
            return;
        }

        BeaconLog log = new BeaconLog(getApplicationContext());
        log.setUserId(KUtil.getStringPref(PrefKey.USER_ID, "none"));
        log.setBuildingCode(KUtil.getBuildingCode());
        log.setMessage(msg);
        //log.setStackData(takeStackPosition());
        String sourcePosition = takeStackPosition();
        log.setMessage(sourcePosition + ":" + log.string());
        LogUtils.err(TAG, "server log:"+ log.string());

        Map<String, Object> query = log.createQueryMap();

        Call<ResponseBase> call = getAppService().sendServiceLog(query);
        call.enqueue(new Callback<ResponseBase>() {
            @Override
            public void onResponse(Call<ResponseBase> call, Response<ResponseBase> response) {
                LogUtils.err(TAG, "service log response:" +response.raw().toString());
            }

            @Override
            public void onFailure(Call<ResponseBase> call, Throwable t) {
                LogUtils.err(TAG, "service log fail:" + Log.getStackTraceString(t));
            }
        });*/
    }


    /**
     * 올라간 층수 데이터 서버전송 성공시 데이터 전파
     *
     * @param beaconUuid
     */
    public static void broadcastServerResult(BeaconUuid beaconUuid) {

        Pref pref = Pref.instance();

        //if (!StringUtil.isEmptyOrWhiteSpace(beaconUuid.getMainCharFilename()) ) {
        pref.saveStringValue(PrefKey.CHARACTER_MAIN, beaconUuid.getMainCharFilename());
        //}
        //if(!StringUtil.isEmptyOrWhiteSpace(beaconUuid.getSubCharFilenane())) {
        pref.saveStringValue(PrefKey.CHARACTER_SUB, beaconUuid.getSubCharFilenane());
        //}

        //LogUtils.err(TAG, beaconUuid.string());
        //String prevChar = KUtil.getStringPref(PrefKey.CHARACTER_MAIN, "");
        //boolean isMainChracterChanged = !StringUtil.isEquals(prevChar, beaconUuid.getMainCharFilename());
        boolean isMainChracterChanged = false;

        String charImageFile = pref.getStringValue(PrefKey.CHARACTER_MAIN, "");

        // 기존 이미지와 동일 하면 스킵
        if (!charImageFile.equals(beaconUuid.getMainCharFilename())) {
            isMainChracterChanged = true;
        }

        if (isMainChracterChanged) {
            KUtil.saveStringPref(PrefKey.CHARACTER_MAIN, beaconUuid.getMainCharFilename());
        }

        if (!StringUtil.isEmptyOrWhiteSpace(beaconUuid.getSubCharFilenane())) {
            KUtil.saveStringPref(PrefKey.CHARACTER_SUB, beaconUuid.getSubCharFilenane());
        }

        BusProvider.instance().post(
                new KsEvent<BeaconUuid>()
                        .setType(KsEvent.Type.UPDATE_FLOOR_AMOUNT)
                        .setValue(beaconUuid)
                        .setMainCharacterChanged(isMainChracterChanged)
        );
    }

    /**
     * 계단 올라간 데이터 서버전송 실패하면 로컬에 저장 후 네트워크가 안정화 되면 전송
     *
     * @param goupData
     */
    private void saveGoUpDataToLocalDb(Map<String, Object> goupData, String localTime) {
        goupData.put("app_time", localTime);
        if (KsDbWorker.insertFailData(getApplicationContext(), goupData)) {
            BusProvider.instance().post(
                    new KsEvent<Map<String, Object>>()
                            .setType(KsEvent.Type.UPDATE_FLOOR_AMOUNT_FAIL)
                            .setValue(goupData)
                            .setMainCharacterChanged(false));
        }
    }


    @Override
    protected void onStart() {
        LogUtils.err(TAG, "MainActivity#onStart()");
        //callActivity(DummyActivity.class, false);
        //startService(new Intent(getBaseContext(), BeaconRagingInRegionService.class));
        //startService(new Intent(getBaseContext(), StepSensorService.class));

        Intent startServiceIntent = new Intent(this, NetworkSchedulerService.class);
        startService(startServiceIntent);

        /*if(getIntent().getStringExtra("_FROM_") != null){
            finish();
        }*/
        super.onStart();
    }

    @Override
    protected void onStop() {
        stopService(new Intent(this, NetworkSchedulerService.class));
        super.onStop();
    }

    /**
     * 배터리 최적화 대상 확인
     */
    private void checkBatteryWhiteList() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean isWhiteListing = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            isWhiteListing = pm.isIgnoringBatteryOptimizations(getPackageName());
            LogUtils.err(TAG, "white list=" + isWhiteListing);
            if (!isWhiteListing) {
                //startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));

                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }

        }
    }

    /**
     * 로그인 상태인가 검사
     *
     * @return
     */
    private boolean checkLogin() {
        return KUtil.hasUserToken() && KUtil.hasUserId();
    }

    /**
     * 블루투스 ON 확인
     */
    private void checkBluetoothOn() {
        if (!KUtil.isBluetoothOn()) {
            Acceptor acceptor = new AbstractAcceptor() {
                @Override
                public void accept() {
                    Intent intentOpenBluetoothSettings = new Intent();
                    intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                    startActivity(intentOpenBluetoothSettings);
                }
            };
            showWarnPopup(acceptor, getString(R.string.txt_bluetooth_desc));
        }
    }

    @Override
    protected void findViews() {
        TextView nick = getView(R.id.txt_name);
        nick.setIncludeFontPadding(false);
        TextView email = getView(R.id.txt_email);
        email.setIncludeFontPadding(false);
    }

    @Override
    protected void attachEvents() {
        View.OnClickListener clickListener = new ClickListener(this);
        getView(R.id.btn_navi_close).setOnClickListener(clickListener);
        getView(R.id.btn_setting).setOnClickListener(clickListener);
        for (int va : mMenuIds) {
            getView(va).setOnClickListener(clickListener);
        }

        //getView(R.id.btn_pause).setOnClickListener(clickListener);
    }

    @Override
    protected void setInitialData() {
        //updateCharacter();
        /*Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            BeaconUuid info = (BeaconUuid)bundle.getSerializable("_TODAY_ACTIVITY_");
            LogUtils.err(TAG, "today info is null?=" + (info == null));
            if(info != null) {
                BusProvider.instance().post(
                        new KsEvent<BeaconUuid>()
                                .setType(KsEvent.Type.UPDATE_FLOOR_AMOUNT).setValue(info)
                );
            }
        }*/
    }

    @Override
    public void performClick(View view) {
        int id = view.getId();
        SharedPreferences prefr = getSharedPreferences("lastSelectedCafe", MODE_PRIVATE);
        switch (id) {
            case R.id.btn_navi_close://drawer close
                onBackPressed();
                break;
            /*case R.id.btn_pause://측정 중지 상태

               // callActivity(GPSAcceptActivity.class, false);
                startMeasure(false);
                mFinishFlag = true ;
                app.push = null ;
                //finish();


                mStarted = true;

                startMeasure(true);
                mStartList.clear();
                for (int i = 0; i < 600; i++) {
                    mStartList.add(new MeasureObj());
                }
                cnt = 0;

                displayFragment(Env.FragmentType.HOME);
                showStartPopup();

                //

                break;*/
                /*
                String s_lat = Pref.instance().getStringValue(PrefKey.BUILDING_LAT, "");
                String s_lng = Pref.instance().getStringValue(PrefKey.BUILDING_LNG, "");
                if ( StringUtil.isEmptyOrWhiteSpace(s_lat) || StringUtil.isEmptyOrWhiteSpace(s_lng)  || s_lat.equals("null")  || s_lng.equals("null")  ) {
                    callActivity(GPSAcceptActivity.class, false);
                    mFinishFlag = true ;
                    finish();
                }  else {
                    Location s_loc = new Location("dbplace");
                    s_loc.setLatitude(Double.valueOf(s_lat));
                    s_loc.setLongitude(Double.valueOf(s_lng));
                    if (mLocation.distanceTo(s_loc)  > DefaultCode.GPS_RANGE.getValue())  {
                        callActivity(GPSAcceptActivity.class, false);
                        mFinishFlag = true ;
                        finish();
                    } else {
                        mStarted = true;
                        startMeasure(true);
                        mStartList.clear();
                        for (int i = 0; i < 600; i++) {
                            mStartList.add(new MeasureObj());
                        }
                        cnt = 0;
                        //initMeasure();
                    }
                }
                break;
                */
            case R.id.menu_activity_gps://건물 선택
                //displayFragment(Env.FragmentType.INFO_SETTING);
                onBackPressed();

                Bundle data = new Bundle();
                data.putBoolean("selectmode", true);
                callActivity(GPSAcceptActivity.class, data, false);
                startMeasure(false);
                getApp().push = null;
                mFinishFlag = true;
                finish();
                break;
            case R.id.menu_cafe_name:
                onBackPressed();

                Bundle bu4 = new Bundle();
                bu4.putString("TYPE", "");
                bu4.putString("cafeseq", prefr.getString("cafeseq", ""));

                callActivity(CafeDetailActivity.class, bu4, false);

                break;
            case R.id.menu_cafe_board:
                onBackPressed();

                Bundle bu = new Bundle();
                bu.putString("TYPE", "BOARD");
                bu.putString("cafeseq", prefr.getString("cafeseq", ""));

                callActivity(CafeDetailActivity.class, bu, false);

                break;
            case R.id.menu_cafe_daily:
                onBackPressed();

                Bundle bu1 = new Bundle();
                bu1.putString("TYPE", "DAILY");
                bu1.putString("cafeseq", prefr.getString("cafeseq", ""));

                callActivity(CafeDetailActivity.class, bu1, false);

                break;
            case R.id.menu_cafe_weekly:
                onBackPressed();

                Bundle bu2 = new Bundle();
                bu2.putString("TYPE", "WEEKLY");
                bu2.putString("cafeseq", prefr.getString("cafeseq", ""));

                callActivity(CafeDetailActivity.class, bu2, false);

                break;
            case R.id.menu_cafe_monthly:
                onBackPressed();

                Bundle bu3 = new Bundle();
                bu3.putString("TYPE", "MONTHLY");
                bu3.putString("cafeseq", prefr.getString("cafeseq", ""));

                callActivity(CafeDetailActivity.class, bu3, false);

                break;
            case R.id.btn_setting://개인정보 설정
                //displayFragment(Env.FragmentType.INFO_SETTING);
                onBackPressed();

                callActivity(InfoSettingActivity.class, false);
                break;
            case R.id.menu_activity_record://활동기록
                displayFragment(Env.FragmentType.ACTIVITY_RECORD);
                break;
            case R.id.menu_analysis://주.월별 분석
                displayFragment(Env.FragmentType.ACTIVITY_ANALYSIS);
                break;
            case R.id.menu_ranking_private://개인랭킹
                //toast("랭킹-" + Env.FragmentType.RANKING_INDIVIDUAL);
                displayFragment(Env.FragmentType.RANKING_INDIVIDUAL);
                break;
            case R.id.menu_ranking_walk_private://개인랭킹
                //toast("랭킹-" + Env.FragmentType.RANKING_WALK_INDIVIDUAL);
                displayFragment(Env.FragmentType.RANKING_WALK_INDIVIDUAL);
                break;
            case R.id.menu_ranking_group://그룹랭킹
                //toast("랭킹-" + Env.FragmentType.RANKING_GROUP);
                displayFragment(Env.FragmentType.RANKING_GROUP);
                break;
            case R.id.menu_ggr://GGR 히스토리
                displayFragment(Env.FragmentType.GGR_HISTORY);
                break;
            case R.id.menu_city_ranking://명예의 전당
                displayFragment(Env.FragmentType.CITY_RANKING);
                break;
            /*case R.id.menu_notice://공지.이벤트
                callActivity(NoticeEventActivity.class, false);
                break;*/

            case R.id.menu_cafe: //카페
                onBackPressed();

                callActivity(CafeMainActivity.class, false);
                /*if (getLinearLayout(R.id.ll_cafe).isShown()) {
                    getLinearLayout(R.id.ll_cafe).setVisibility(View.GONE);
                } else {
                    getLinearLayout(R.id.ll_cafe).setVisibility(View.VISIBLE);
                }*/
                break;
            case R.id.menu_plus_friend:
                onBackPressed();

                /*Bundle bu = new Bundle();
                bu.putSerializable("url", "https://pf.kakao.com/_xhYYJT");
                callActivity(WebviewActivity.class, bu,false);*/
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://pf.kakao.com/_xhYYJT"));
                startActivity(intent);
                break;
           /* case R.id.menu_make_cafe://카페 개설하기
                //callActivity(MakeCafeActivity.class, false);
                callActivity(CafeCreateActivity.class, false);
                break;
            case R.id.menu_signcafe://카페 가입 탈퇴
                callActivity(InfoSignupCafeActivity.class, false);
                break;
            case R.id.menu_help://도움말
                //openWebBrowser(Env.UrlPath.HELP.url(), false);
                callActivity(HelpActivity.class, false);
                break;*/
/*
            case R.id.menu_provision://이용약관 보기
                //openWebBrowser(Env.UrlPath.PROVISION.url(), false);
                callActivity(ProvisionActivity.class, false);
                break;

            case R.id.menu_beacon://비콘신호 모니터링
                callActivity(MonitoringActivity.class, false);
                break;
*/

        }

    }

    @Override
    public void displayFragment(Env.FragmentType type) {
        LogUtils.d(TAG, "menu position=" + type.name());
        BaseFragment fragment = null;
        String title = getString(R.string.app_name);
        int code = type.code();

        switch (code) {
            case 100://HOME
                fragment = MainFragment.newInstance(this, mTodayActivity);
                title = DateUtil.currentDate("yyyy.MM.dd");
                break;
            case 400://ACTIVITY_RECORD
                fragment = FragmentActivityRecord.newInstance(this);
                title = getString(R.string.txt_activity_record);
                break;
            case 500://ACTIVITY_ANALYSIS
                fragment = FragmentActivityAnalysis.newInstance(this);
                title = getString(R.string.txt_analysis);
                break;
            case 600://RANKING_INDIVIDUAL
                fragment = FragmentRankingIndividual.newInstance(this);
                title = getString(R.string.txt_ranking_private);
                break;
            case 602://RANKING_INDIVIDUAL
                fragment = FragmentRankingWalkIndividual.newInstance(this);
                title = getString(R.string.txt_ranking_walk_private);
                break;
            case 601://RANKING_GROUP
                AppUserBase user = DataHolder.instance().getAppUserBase();
                if (!user.getCust_build_seq().equals(user.getBuild_seq())) {
                    showWarnPopup("카페가입후 사용 가능합니다.");
                    break;
                }
                /*if (null == user || null == user.getCust_build_seq()) {
                    showWarnPopup("카페가입후 사용 가능합니다.");
                    break;
                } else if (!user.getCust_build_seq().equals(user.getBuild_seq())) {
                    showWarnPopup("카페가입후 사용 가능합니다.");
                    break;
                }*/
                fragment = FragmentRankingGroup.newInstance(this);
                title = getString(R.string.txt_ranking_group);
                break;
            case 700://GGR History
                fragment = GGRFragment.newInstance(this);
                title = getString(R.string.txt_ggr);
                break;
            case 710://명예의 전당
                fragment = CityRankFragment.newInstance(this);
                title = getString(R.string.txt_city_ranking);
                break;
        }

        if (fragment != null) {
            mFragmentCurrent = fragment;

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            //transaction.setCustomAnimations(R.anim.slide_in_right, 0, 0, R.anim.slide_out_right);
            if (!isHomeScreen()) {
                /*transaction.setCustomAnimations(
                        R.anim.slide_in_right,//enter
                        R.anim.fade_out,//exit
                        R.anim.fade_in_full,//popEnter
                        R.anim.slide_out_right);//popExit*/
                //transaction.setCustomAnimations(0,0,0,0);
                /*transaction.setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left, R.anim.slide_out_right);*/
                findViewById(R.id.LayoutPause).setVisibility(View.INVISIBLE);
            } else {
                /*if (isSleep)
                findViewById(R.id.LayoutPause).setVisibility(View.VISIBLE);

                if(isMyServiceRunning(StepCounterService.class)) {
                    // 자동측정중이면
                    TextView tv =  findViewById(R.id.tvPauseMent);
                    tv.setText("자동측정중입니다.");
                }
*/
            }
            transaction.replace(R.id.content_frame, fragment);
            transaction.addToBackStack(title + "-" + fragment.getClass().getSimpleName());
            transaction.commit();

            setNavigationTitle(title);
            if (isHomeScreen()) {
                // 백그라운드 색상 변경
                SharedPreferences prefr = getSharedPreferences("backgroundColor", MODE_PRIVATE);
                int initColor = prefr.getInt("backgroundColor", -99);

                if (initColor != -99) {
                    int bglist[] = DataHolder.instance().getBgColors();
                    Pref.instance().saveIntValue(PrefKey.COMPANY_COLOR_NUM, initColor);
                    selectBgColor(initColor);
                    changeColors();
                    mToolBar.setBackgroundColor(getCompanyColor());
                } else {
                    changeColors();
                    mToolBar.setBackgroundColor(getCompanyColor());
                }

                /*mToolBar.setBackgroundColor(getCompanyColor());
                toggleDrawerIcon(true);*/
            } else {
                toggleDrawerIcon(false);
                lockDrawerLayout(true);
            }

        }
    }

    private boolean isHomeScreen() {
        return mFragmentCurrent != null &&
                mFragmentCurrent instanceof MainFragment;
    }

    public boolean mFinishFlag;

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(Gravity.START)) {
            mDrawer.closeDrawer(Gravity.START);
        } else {
            //Log.d("backstack count=" + getSupportFragmentManager().getBackStackEntryCount());
            int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
            LogUtils.err(TAG, "back stack count=" + backStackCount);
            if (isHomeScreen() || backStackCount == 1) {
                if (mFinishFlag) {
                    finish();
                } else {
                    //toast("한번 더 누르시면 종료됩니다.");
                    mFinishFlag = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mFinishFlag = false;
                        }
                    }, 3000);
                }
            } else {
                FragmentManager.BackStackEntry
                        entry = getSupportFragmentManager().getBackStackEntryAt(backStackCount - 1);
                String title = entry.getName().split("-")[0];
                setNavigationTitle(title);
                toggleDrawerIcon(true);
                lockDrawerLayout(false);

                getSupportFragmentManager().popBackStackImmediate();

                //if (isSleep)
                //   findViewById(R.id.LayoutPause).setVisibility(View.VISIBLE);

                /*if(isMyServiceRunning(StepCounterService.class)) {
                    // 자동측정중이면
                    TextView tv =  findViewById(R.id.tvPauseMent);
                    tv.setText("자동측정중입니다.");
                }*/

                LogUtils.err(TAG, entry.getName());
                if (backStackCount <= 2) {
                    setNavigationTitle(DateUtil.currentDate("yyyy.MM.dd"));
                    toggleActionBarResources(true);
                    //fireMainRefreshEvent();
                }
            }
        }
    }

    private void showStartPopup() {
        Toast.makeText(this, "건강한 습관, 계단왕\n지금 시작하십시오.", Toast.LENGTH_LONG).show();

    }


    private void fireMainRefreshEvent() {
        new Handler().postDelayed(() -> {
            BusProvider.instance().post(new KsEvent<>().setType(KsEvent.Type.MAIN_REFRESH));
        }, 100);
    }


    @Override
    protected void onDestroy() {
        LogUtils.err(TAG, "MainActivity#onDestroy()");
//        stopService(new Intent(getBaseContext(), BeaconRagingInRegionService.class));
//        stopService(new Intent(getBaseContext(), StepSensorService.class));
        try {
            unregisterReceiver(mExitReceiver);
            unregisterReceiver(mIsAppBackgroundReceiver);
            unregisterReceiver(restartService);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mCharacterChangeReceiver);
        } catch (Exception e) {
        }

 /*       mStarted = false;
        startMeasure(mStarted);
        handler.removeCallbacks(runnable);*/

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        LogUtils.err(TAG, "MainActivity#onPause()");
        isActivity = false;

        stopService(new Intent(getBaseContext(), BeaconRagingInRegionService.class));
        stopService(new Intent(getBaseContext(), StepSensorService.class));
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mCharacterChangeReceiver);

        measureStop();

        super.onPause();
    }

    private void selectBgColor(int n) {
        int bglist[] = DataHolder.instance().getBgColors();
        for (int i = 0; i < 10; i++) {
            Button tv = (Button) mRootView.findViewWithTag("21" + String.format("%02d", i + 1));
            if (tv == null) {
                continue;
            }
            if (i == n) {
                GradientDrawable gd = new GradientDrawable();
                gd.setColor(getResources().getColor(bglist[i]));
                gd.setCornerRadius(0);
                gd.setStroke(8, 0xFF28B8F5);
                tv.setBackground(gd);

            } else {
                tv.setBackgroundColor(getResources().getColor(bglist[i]));
            }
        }

    }

    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mCharacterChangeReceiver,
                new IntentFilter(Env.Action.CHARACTER_CHANGED_ACTION.action())
        );

        isActivity = true;
        //BusProvider.instance().register(this);
        LogUtils.err(TAG, "MainActivity#onResume()");
        //startService(new Intent(getBaseContext(), BeaconRagingInRegionService.class));


        SharedPreferences prefr = getSharedPreferences("backgroundColor", MODE_PRIVATE);
        int initColor = prefr.getInt("backgroundColor", -99);

        if (initColor != -99) {
            int bglist[] = DataHolder.instance().getBgColors();
            Pref.instance().saveIntValue(PrefKey.COMPANY_COLOR_NUM, initColor);
            selectBgColor(initColor);
            changeColors();
            mToolBar.setBackgroundColor(getCompanyColor());
        } else {
            changeColors();
            mToolBar.setBackgroundColor(getCompanyColor());
        }

        /*if (initColor != -99) {
            Log.d("DDDDDDDDDDDDD11", initColor + "");
            mToolBar.setBackgroundColor(initColor);
            toggleDrawerIcon(true);

        } else {
            mToolBar.setBackgroundColor(getCompanyColor());
            toggleDrawerIcon(true);
        }
        changeColors();
        mToolBar.setBackgroundColor(getCompanyColor());*/

        getCafeDetail();
        updateCharacter();
        //fireMainRefreshEvent();
        setBeaconManagerMode(false);

        if (getApp().isPop) {
            showStartPopup();
        }

        if (getApp().isInit) {  // 다시 화면 빠졌다 들어오면 락 클리어
            getApp().isInit = false;
            getApp().getmBLocationManager().registerLocationUpdates();
            getApp().getmBLocationManager().mDelegateFindLocation = this;
        }
        super.onResume();

        if (getApp().push != null) {

            String pushtype = getApp().push.getStringData("push_type");
            if (pushtype == null) {
                getApp().push = null;
                return;
            }

            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getApp().push = null;
                            if (pushtype.equals("JERSEY_GOLD") || pushtype.equals("JERSEY_GREEN")
                                    || pushtype.equals("JERSEY_REDDOT") || pushtype.equals("FIRST_CHALLENGE")) {
                                callActivity(NoticeRankingActivity.class, false);
                            } else {
                                callActivity(NoticeEventActivity.class, false);

                            }
                        }
                    });
                }
            };
            thread.start();
            getApp().push = null;
        }


    }

    private void getCafeDetail() {
        showSpinner("");
        AppUserBase user = DataHolder.instance().getAppUserBase();
        Map<String, Object> query = KUtil.getDefaultQueryMap();
        try {
            query.put("user_seq", user.getUser_seq());
        } catch (Exception ex) {
            SharedPreferences prefr = getSharedPreferences("userInfo", MODE_PRIVATE);
            query.put("user_seq", prefr.getInt("user_seq", -1));
        }

        Log.d("GGGGGGGGGGGGGGGGGGG", user.getUser_seq() + "");

        SharedPreferences prefr = getSharedPreferences("lastSelectedCafe", MODE_PRIVATE);
        String mCafeseq = prefr.getString("cafeseq", "");

        Log.d("GGGGGGGGGGGGGGGGGGG", mCafeseq + "_");


        if (null != mCafeseq && !"".equals(mCafeseq)) {
            query.put("cafeseq", mCafeseq);
        } else {
            // 메뉴 hide 처리
            getView(R.id.menu_cafe_name).setVisibility(View.GONE);
            getView(R.id.menu_cafe_board).setVisibility(View.GONE);
            getView(R.id.menu_cafe_daily).setVisibility(View.GONE);
            getView(R.id.menu_cafe_weekly).setVisibility(View.GONE);
            getView(R.id.menu_cafe_monthly).setVisibility(View.GONE);
            getView(R.id.seperator_1).setVisibility(View.GONE);
            getView(R.id.seperator_2).setVisibility(View.GONE);
            getView(R.id.seperator_3).setVisibility(View.GONE);

            if (!isCheck) {
                getUserCafeMainList();
            }

            closeSpinner();
            return;
        }


        Call<CafeDetail> call = getCafeService().detail(query);

        call.enqueue(new Callback<CafeDetail>() {
            @Override
            public void onResponse(Call<CafeDetail> call, Response<CafeDetail> response) {
                LogUtils.err(TAG, response.toString());
                if (response.isSuccessful()) {
                    CafeDetail cafedetail = response.body();
                    //LogUtils.err(TAG, "profile=" + profile.string());
                    if (cafedetail.isSuccess()) {
                        findViewById(R.id.menu_cafe_name).setVisibility(View.VISIBLE);
                        findViewById(R.id.menu_cafe_board).setVisibility(View.VISIBLE);
                        findViewById(R.id.menu_cafe_daily).setVisibility(View.VISIBLE);
                        findViewById(R.id.menu_cafe_weekly).setVisibility(View.VISIBLE);
                        findViewById(R.id.menu_cafe_monthly).setVisibility(View.VISIBLE);
                        findViewById(R.id.seperator_1).setVisibility(View.VISIBLE);
                        findViewById(R.id.seperator_2).setVisibility(View.VISIBLE);
                        findViewById(R.id.seperator_3).setVisibility(View.VISIBLE);

                        MenuRow mr = getView(R.id.menu_cafe_name);

                        mr.setMenuTitle("[ " + cafedetail.getCafe().getCafename() + " ]", getResources().getString(R.string.txt_cafe_connect));
                        mr.setMenuTitleColor(getResources().getColor(R.color.tab_text_color_selected));
                    } else {
                        findViewById(R.id.menu_cafe_name).setVisibility(View.GONE);
                        findViewById(R.id.menu_cafe_board).setVisibility(View.GONE);
                        findViewById(R.id.menu_cafe_daily).setVisibility(View.GONE);
                        findViewById(R.id.menu_cafe_weekly).setVisibility(View.GONE);
                        findViewById(R.id.menu_cafe_monthly).setVisibility(View.GONE);
                        findViewById(R.id.seperator_1).setVisibility(View.GONE);
                        findViewById(R.id.seperator_2).setVisibility(View.GONE);
                        findViewById(R.id.seperator_3).setVisibility(View.GONE);
                    }
                }

                closeSpinner();
                if (!"0000".equals(response.body().getResponseCode())) {
                    toast(response.body().getResponseMessage());
                    finish();
                }
            }

            @Override
            public void onFailure(Call<CafeDetail> call, Throwable t) {
                closeSpinner();
                LogUtils.err(TAG, t);
                toast(R.string.warn_server_not_smooth);
            }
        });
    }

    private void getUserCafeMainList() {
        showSpinner("");
        isCheck = true;
        AppUserBase user = DataHolder.instance().getAppUserBase();
        Map<String, Object> query = KUtil.getDefaultQueryMap();
        query.put("user_seq", user.getUser_seq());
        Call<PageResponseBase<Cafe>> call = getCafeService().selectMyCafeList(query);
        call.enqueue(new Callback<PageResponseBase<Cafe>>() {
            @Override
            public void onResponse(Call<PageResponseBase<Cafe>> call, Response<PageResponseBase<Cafe>> response) {
                closeSpinner();
                LogUtils.err(TAG, response.toString());

                if (response.isSuccessful()) {
                    PageResponseBase<Cafe> cafelist = response.body();

                    if (cafelist.isSuccess()) {
                        if (null != cafelist.getResult()) {
                            PageResponseBase<Cafe> mCMList = cafelist;

                            if (null == mCMList || CollectionUtils.isEmpty(mCMList.getResult())) {
                                return;
                            } else {
                                SharedPreferences prefr = getSharedPreferences(
                                        "lastSelectedCafe",
                                        MODE_PRIVATE
                                );
                                SharedPreferences.Editor editor = prefr.edit();
                                editor.putString("cafeseq", mCMList.getResult().get(0).getCafeseq());
                                editor.commit();
                                getCafeDetail();
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<PageResponseBase<Cafe>> call, Throwable t) {
                closeSpinner();
                LogUtils.err(TAG, t);
            }
        });
    }


    private void setBeaconManagerMode(boolean mode) {
        /*KoswApp app = (KoswApp)getApplication();
        if(app.isBeaconManagerBound()){
            app.setModeBackground(mode);
        }*/
    }

    private void sendFcmToken() {
        if (KUtil.isFcmTokenRefreshed()) {
            Map<String, Object> query = KUtil.getDefaultQueryMap();
            query.put("fcmToken", KUtil.getStringPref(PrefKey.FCM_TOKEN, ""));
            Call<ResponseBase> call = getAppService().sendFcmToken(query);
            call.enqueue(new Callback<ResponseBase>() {
                @Override
                public void onResponse(Call<ResponseBase> call, Response<ResponseBase> response) {
                    LogUtils.err(TAG, response.raw().toString());
                    if (response.isSuccessful() && response.body().isSuccess()) {
                        Pref.instance().saveBooleanValue(PrefKey.FCM_TOKEN_REFRESHED, false);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBase> call, Throwable t) {
                }
            });
        }
    }

    @Override
    public void updateCharacter() {
        /* 닉네임, 아이디 노출 */
        TextView nick = getView(R.id.txt_name);

        Profile pf = DataHolder.instance().getProfile();
        AppUserBase user = DataHolder.instance().getAppUserBase();

        nick.setText(KUtil.getStringPref(PrefKey.USER_NICK, "계단왕"));

        if (pf != null) {
            nick.setText(pf.getNickName());
        }

        TextView email = getView(R.id.txt_email);
        //email.setText(KUtil.getStringPref(PrefKey.USER_ID, ""));

        if ("kakao".equals(KUtil.getStringPref(PrefKey.LOGIN_TYPE, ""))) {
            email.setText(KUtil.getStringPref(PrefKey.OPEN_ID, ""));
        } else {
            email.setText(KUtil.getStringPref(PrefKey.USER_ID, ""));
        }
        /* 캐릭터 이미지-햄버거 메뉴 상단 */
        CircleImageView cv = getView(R.id.circleImageView);
        String imgUrl = KUtil.getSubCharacterImgUrl();
        LogUtils.err(TAG, "drawer char url=" + imgUrl);
        Glide.with(this)
                .applyDefaultRequestOptions(KUtil.getGlideCacheOption())
                .load(imgUrl).thumbnail(.5f).into(cv);
        //KUtil.getMyCharacterUrl(false)).thumbnail(.5f).into(cv);
    }

    /**
     * 선택된 회사의 색상과, 현재 상태의 회원 메인화면 캐릭터 변경
     *
     * @param event
     */
    @Subscribe
    public void acceptEvent(KsEvent event) {
        if (KsEvent.Type.CHANGE_COLOR == event.getType()) {//선택된 회사 색상으로 변경
            changeColors();//status bar, action bar, content area background color 변경
        } else if (KsEvent.Type.CHANGE_CHARACTER == event.getType()) {//현재 상태의 메인 캐릭터 변경
            updateCharacter();
        } else if (KsEvent.Type.MAIN_PUSH == event.getType()) {
            Push push = (Push) event.getValue();
            LogUtils.err(TAG, "push on main : " + push.string());
        }
    }


    @Override
    public void clearNotificationBadge() {
        ShortcutBadger.removeCount(MainActivity.this);
        Pref.instance().saveIntValue(PrefKey.FCM_BADGE_COUNT, 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        //Gps start

        getApp().getmBLocationManager().registerLocationUpdates();
        getApp().getmBLocationManager().mDelegateFindLocation = this;

        LogUtils.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        LogUtils.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());
        toast(R.string.permission_denied);
        finish();
        /*// (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }*/
    }

    @Override
    public void findLocation(Location loc, Address addr) {


        mLocation = loc;
        mAddr = addr;

        if (isTest) {
            if (mAddr != null && mAddr.getAddressLine(0) != null) {
                getTextView(R.id.txt_addr).setText(String.format("%s", mAddr.getAddressLine(0)));
            }
            if (mLocation != null) {
                getTextView(R.id.txt_gps).setText(String.format("%s,%s", String.valueOf(mLocation.getLatitude()), String.valueOf(mLocation.getLongitude())));
            }
        }

        String s_name = Pref.instance().getStringValue(PrefKey.BUILDING_NAME, "");
        String s_addr = Pref.instance().getStringValue(PrefKey.BUILDING_ADDR, "");
        String s_lat = Pref.instance().getStringValue(PrefKey.BUILDING_LAT, "");
        String s_lng = Pref.instance().getStringValue(PrefKey.BUILDING_LNG, "");

        //android.util.Log.v("kmj",s_addr ) ;

        if (StringUtil.isEmptyOrWhiteSpace(s_lat) || StringUtil.isEmptyOrWhiteSpace(s_lng) || s_lat.equals("null") || s_lng.equals("null")) {

        } else {
            Location s_loc = new Location("dbplace");
            s_loc.setLatitude(Double.valueOf(s_lat));
            s_loc.setLongitude(Double.valueOf(s_lng));

            // 설정 건물에서 떨어지면 건물 다시 설정
            /*if (loc.distanceTo(s_loc) > DefaultCode.GPS_RANGE.getValue()) {
                mStarted = false;
                initMeasure();
                getTextView(R.id.tvPauseMent).setText(R.string.pause_distanceover);
                startMeasure(false);
            }*/
        }

        getApp().getmBLocationManager().registerLocationUpdates();
        getApp().getmBLocationManager().mDelegateFindLocation = this;

    }

    //GPS 설정 체크
    private boolean chkGpsService() {
        String gps = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        //if (!(gps.matches(".*gps.*") && gps.matches(".*network.*"))) {
        if (!(gps.matches(".*gps.*"))) {

            // GPS OFF 일때 Dialog 표시
            AlertDialog.Builder gsDialog = new AlertDialog.Builder(this);
            gsDialog.setTitle("위치 서비스 설정");
            gsDialog.setMessage("무선 네트워크 사용, GPS 위성 사용을 모두 체크하셔야 정확한 위치 서비스가 가능합니다.\n위치 서비스 기능을 설정하시겠습니까?");
            gsDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // GPS설정 화면으로 이동
                    Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    startActivity(intent);
                }
            })
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    }).create().show();
            return false;

        } else {
            return true;
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
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e("name not found", e.toString());
        }
    }

    public boolean isAppOnForeground() {
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }


}

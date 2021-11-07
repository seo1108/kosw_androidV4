package kr.co.photointerior.kosw.service.stepcounter;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.blankj.utilcode.util.Utils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import kr.co.photointerior.kosw.R;
import kr.co.photointerior.kosw.conf.AppConst;
import kr.co.photointerior.kosw.db.KsDbWorker;
import kr.co.photointerior.kosw.global.DefaultCode;
import kr.co.photointerior.kosw.global.Env;
import kr.co.photointerior.kosw.global.KoswApp;
import kr.co.photointerior.kosw.pref.Pref;
import kr.co.photointerior.kosw.pref.PrefKey;
import kr.co.photointerior.kosw.rest.model.ActivityRecord;
import kr.co.photointerior.kosw.rest.model.AppUserBase;
import kr.co.photointerior.kosw.rest.model.BeaconUuid;
import kr.co.photointerior.kosw.rest.model.DataHolder;
import kr.co.photointerior.kosw.rest.model.Record;
import kr.co.photointerior.kosw.rest.model.ResponseBase;
import kr.co.photointerior.kosw.service.beacon.AltitudeManager;
import kr.co.photointerior.kosw.service.beacon.DirectionManager;
import kr.co.photointerior.kosw.service.beacon.MeasureObj;
import kr.co.photointerior.kosw.service.beacon.StepManager;
import kr.co.photointerior.kosw.service.net.NetworkConnectivityReceiver;
import kr.co.photointerior.kosw.ui.MainActivity;
import kr.co.photointerior.kosw.utils.DateUtil;
import kr.co.photointerior.kosw.utils.KUtil;
import kr.co.photointerior.kosw.utils.LogUtils;
import kr.co.photointerior.kosw.utils.SingletoneMixin;
import kr.co.photointerior.kosw.utils.StringUtil;
import kr.co.photointerior.kosw.utils.event.BusProvider;
import kr.co.photointerior.kosw.utils.event.KsEvent;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;
import static android.content.Context.POWER_SERVICE;
import static java.text.DateFormat.getDateTimeInstance;
import static java.text.DateFormat.getTimeInstance;
//import static kr.co.photointerior.kosw.conf.AppConst.LAST_SENT_WALKING_DATE;

public class StepThread extends Thread implements SingletoneMixin {
    private String TAG = LogUtils.makeLogTag(StepThread.class);


    boolean isRun = true;
    private static Context mContext = null;

    private static boolean isTest = false;
    private Location mLocation;
    private Address mAddr;
    private static boolean mStarted;
    private static double mAltitude;
    private double mAltitude2;
    private static double mOrientation;
    private static double mX;
    private static double mY;
    private static double mZ;
    private static double mStep;
    private int mFloor;
    private boolean isRestarting = false;

    private int mSleepCnt = 0;
    private int mMeasureStep = 0;
    static private int mMaxSleepCnt = 10;
    //static private  int mMaxSleepCnt = 2 ;
    static private String sleepMsg[] = {"걷기중 측정시간(30초)이 지났습니다.", "계단이용 측정시간(5분)이 지났습니다."};
    private int sleepMode = 0;


    private static double mSaveStep = 0;
    private static Boolean isSleep = false;
    private Boolean isSleepBtn = false;

    private static int mTrashStep = 0;


    private Boolean isActivity = false;

    private static long startTime = 0;
    private static long endTime = 0;
    private static Boolean isRedDot = false;

    //private StepManagerForService mStepManagerForService;
    private AltitudeManager mAltiManager;
    private StepManager mStepManager;
    private DirectionManager mDirectionManager;
    // 위경도
    private LocationManager mLocationManager;

    //private

    private boolean isStart = false;
    private boolean isTurn = false;
    private boolean isFirstRunned = false;
    private static boolean isContinue = false;
    // Create the Handler
    private Handler handler = new Handler(Looper.getMainLooper());
    //private Handler walk_handler = new Handler(Looper.getMainLooper());
    private static ArrayList<MeasureObj> mStartList = new ArrayList<>();
    private ArrayList<MeasureObj> mUpList = new ArrayList<>();
    private static int cnt = 0;
    private int checkStartTimeOut = 60;
    private int checkRotationTimeOut = 60;
    private int checkUpTimeOut = 60;
    private static Boolean is315 = false;
    private static int cnt315 = 0;
    private Boolean isCount = false;

    private static long goupTime = 0;

    // 건물 층 카운트
    private static int mBuildCount = 0;
    private static int mCurBuildCount = 0;

    // 건물 / 등산 모드
    private static String mIsBuild = ""; // Y :건물계단    N : 등산계단
    private static int mClimbCount = 0;  // 등산 카운트 (4미터 체크)
    private static int mLogicCount = 0;  // 로직 카운트 (회전 )

    private static String todayFloor = "0";

    private ActivityRecord mActivityRecord;

    private String rank;
    private String tot_floor = "";
    private String tot_cal = "";
    private String tot_sec = "";

    // create Timer
    private TimerTask mTask;
    private Timer mTimer;

    private int mUnsentStairCnt = 0;

    PowerManager.WakeLock wakeLock;

    private boolean mDebugMode = false;

    private int mMountainStepLimit = 15;
    private int mMountainHeightLimit = 4;
    private int mMountainMeasureGap = 4000;

    private int mStairStepLimit = 0;
    private int mStairMeasureGap = 3000;

    private int mMountainDistanceLimit = 15;
    private int mStairDistanceLimit = 40;

    private boolean hasGPSPermission = false;
    private Double mPreLat = 0.0, mPreLng = 0.0, mLat, mLng;

    private static String LAST_SENT_WALKING_DATE = "00000000";

    public StepThread(Context context) {
        mContext = context;
    }

    public void stopForever() {
        /*synchronized (this) {
            Log.d("999999999999777771", "STOP FOREVER");
            mStarted = false;
            this.isRun = false;
            mMeasureStep = 0;
            handler.removeCallbacks(runnable);
            try {
                if (mAltiManager != null) mAltiManager.stopMeasure();
                if (mDirectionManager != null) mDirectionManager.stopMeasure();
                if (mStepManager != null) mStepManager.stopMeasure();
            } catch (Exception ex) {
            }
        }*/
        synchronized (this) {
            mStarted = false;
            isRun = false;
            mMeasureStep = 0;
            handler.removeCallbacks(runnable);
            //walk_handler.removeCallbacks(walk_runnable);
            try {
                mStepManager.stopMeasure();

                //mStepManagerForService.stopMeasure();
            } catch (Exception e) {
            }

            try {
                if (mAltiManager != null) mAltiManager.stopMeasure();
                if (mDirectionManager != null) mDirectionManager.stopMeasure();
                //if (mLocationManager != null) mLocationManager.removeUpdates(gpsLocationListener);
            } catch (Exception ex) {
            }

            try {
                if (!wakeLock.isHeld()) wakeLock.acquire();
            } catch (Exception ex) {

            }
        }
    }

    public void run() {
        isRestarting = false;
        mMeasureStep = 0;
        startMeasure(true);
        handler.post(runnable);
        //walk_handler.post(walk_runnable);
        AppConst.IS_STEP_SENSOR_LOADED = false;
        //mStepManagerForService = new StepManagerForService(mContext);
        //mStepManagerForService.startMeasure();

        mStepManager = new StepManager(mContext);
        mAltiManager = new AltitudeManager(mContext);
        mDirectionManager = new DirectionManager(mContext);


        mStepManager.startMeasure();
        mAltiManager.startMeasure();
        mDirectionManager.startMeasure();

        /**********************************************************************
         *
         *
         *
         * GPS 모듈 시작
         *
         *
         **********************************************************************/
//        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
//        Handler mHandler = new Handler(Looper.getMainLooper());
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                // 거리 가져오기 초기화
//                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    //Toast.makeText(mContext, "no permission", Toast.LENGTH_SHORT).show();
//                    hasGPSPermission = false;
//                } else {
//                    hasGPSPermission = true;
//
//                    String locationProvider = LocationManager.GPS_PROVIDER;
//                    Location currentLocation = mLocationManager.getLastKnownLocation(locationProvider);
//                    if (currentLocation != null) {
//                        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
//                                1000,
//                                1,
//                                gpsLocationListener);
//                        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
//                                1000,
//                                1,
//                                gpsLocationListener);
//                    }
//                }
//            }
//        }, 1000);

        /**********************************************************************
         *
         *
         *
         * GPS 모듈 끝
         *
         *
         **********************************************************************/

        //acquireCPUWakelock();
        /*mTask = new TimerTask() {
            @Override
            public void run() {
                if (mStarted)  {
                    checkFloor() ;
                    //isTest = true;
                }
            }
        };

        mTimer = new Timer();

        mTimer.schedule(mTask, 0,100);*/
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // Insert custom code here
            if (mStarted) {
                checkFloor();
                //isTest = true;
            }

            // Repeat every 2 seconds
            //handler.postDelayed(runnable, 100);
            handler.postDelayed(runnable, 100);
            // postDelayed 대기 시간이 점점 짧아지는 케이스가 있어서 Thread sleep 으로 일단 처리
            //try { Thread.sleep(100); } catch (Exception e) {}
            //try { Thread.sleep(100); } catch (Exception e) {}
        }
    };

    /*private Runnable walk_runnable = new Runnable() {
        @Override
        public void run() {

            try {

                // Insert custom code here

                DateFormat d_dateFormat = new SimpleDateFormat("yyyyMMdd");
                Calendar cal = Calendar.getInstance();
                Date now = new Date();

                cal.setTime(now);
                String today_date = d_dateFormat.format(cal.getTime());

                SharedPreferences prefr = mContext.getSharedPreferences("saveWalkInfo", MODE_PRIVATE);
                String lastedate = prefr.getString("lastSendDate", "");
                Log.d("TTTTTTTTTTTTTTTTTTTTT", "SEND WALK DATA CHECK 1 : " + today_date + "___" + lastedate);

                String lastSendDate = prefr.getString("lastSendDate", "");

                SharedPreferences fit_prefr = mContext.getSharedPreferences("fitnessauth", MODE_PRIVATE);
                boolean is_fit_permission = fit_prefr.getBoolean("isPermission", false);
                if (!is_fit_permission) {
                } else {

                    if (!today_date.equals(lastSendDate)) {
                        Log.d("TTTTTTTTTTTTTTTTTTTTT", "SEND");
                        readYesterdayHistoryData();
                    } else {
                        Log.d("TTTTTTTTTTTTTTTTTTTTT", "ALREADY SEND WALK DATA");
                        //readYesterdayHistoryData();
                    }
                }
            } catch (Exception e) { }
            // Repeat every 2 seconds
            //handler.postDelayed(runnable, 100);
            // 3시간마다 한번씩 실행

            // postDelayed 대기 시간이 점점 짧아지는 케이스가 있어서 Thread sleep 으로 일단 처리
            //try { Thread.sleep(100); } catch (Exception e) {}
            //try { Thread.sleep(100); } catch (Exception e) {}

            walk_handler.postDelayed(walk_runnable, 6*60*60*1000);
        }
    };*/


    /*private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // Insert custom code here

            if (mStarted)  {
                checkFloor() ;
            }
            // Repeat every 2 seconds
            handler.postDelayed(runnable, 100);
        }
    };
*/
    public void checkFloor() {
        /*if (isMyServiceRunning(StepCounterService.class)) {
            checkStart();
        } else {
            handler.removeCallbacks(runnable);
            startMeasure(false);
        }*/

        checkStart();
    }

    //0.1 초 단위 체크시
    private void checkStart() {
        if (mAltitude == 0) {
            //return ;
        }

        Log.d("999999999999777771", String.valueOf(mStarted) + "__" + mSleepCnt + "_______" + cnt + "___" + mSaveStep + "______" + mStep);

//        if (cnt % 48 == 0) {
//            sendDataToServer(1, "notbuilding", "");
//        }


        if (mDebugMode) {
            // 5초마다 소리
            if (cnt % 48 == 0 && cnt < 10 * 30 && cnt > 0) {
                MediaPlayer mMediaPlayer = new MediaPlayer();
                try {
                    Uri mediaPath = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.init25);
                    mMediaPlayer.setDataSource(mContext.getApplicationContext(), mediaPath);
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();
                } catch (Exception e) {
                }
            }
        }

        if (cnt >= 10 * 30) {
            /******************************************************************************
             *
             *
             *
             *
             *
             *
             *
             *
             *
             *
             *
             *
             *
             *
             *
             *
             *
             *
             *
             *
             *
             *
             *
             *
             *
             *
             *
             *
             *
             *
             *
             *******************************************************************************/
            // 30초마다 미전송 데이터 재전송하도록 처리
            // 안정화까지 일단 막음 처리
/*
            SharedPreferences pref = mContext.getSharedPreferences("unsent", MODE_PRIVATE);
            int unsent = pref.getInt("unsent", 0);


            if (unsent > 0) {
                sendDataToServer(unsent, "unsent", "");
            }
*/


            if (mDebugMode) {
                Toast.makeText(mContext, "30초 초기화", Toast.LENGTH_SHORT).show();
                MediaPlayer mMediaPlayer = new MediaPlayer();
                try {
                    Uri mediaPath = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.init_all_loud);
                    mMediaPlayer.setDataSource(mContext.getApplicationContext(), mediaPath);
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();
                } catch (Exception e) {
                }
            }

            if (mSaveStep > 0) { // 120초이상 측정이 없으면 측정 잠금  mSleepCnt >= 4
                sleepMode = 1;
                mSleepCnt++;
            } else { // 걷기중이아니면
                mSleepCnt = mMaxSleepCnt;
                sleepMode = 0;
            }
            initMeasure();
            return;
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

        String m = String.format("높이 : %.2f , 방향 : %.2f , 걷기 : %.2f , 시간 : %d", gapAlitude, mDir, step, cnt);

        if (cnt > 0 && cnt % 50 == 0) {

            // 테스트용
            //sendDataToServer(1, "building", m);


            if (mDebugMode) {
                long curTime1 = System.currentTimeMillis();
                Toast.makeText(mContext, m, Toast.LENGTH_SHORT).show();
            }

/*            if (Math.abs(gapAlitude) <= 0.3 || mSaveStep <= 0)
            {

                mSleepCnt = mMaxSleepCnt;
                initMeasure();
                return ;
            }*/
        }

        if (!isContinue) {
            // 90도(135?) 에서 180도(190으로 버퍼를 둠)로 변경
            if (mDir > 135 && Math.abs(gapAlitude) > 1.5) {
                //if (mDir > 190 && Math.abs(gapAlitude) > 1.5) {
                // 자동측정일 경우, 7초 이내 측정이면  카운트 하지 않음 엘리베이터 사용자 걸음
                // 수동측정은 2초
                long curTime = System.currentTimeMillis();

                // 엘리베이터 걸음 임시 주석 처리

                /*if (cnt < 20 || (curTime - goupTime) < 2000) {
                    mSleepCnt++ ;
                    initMeasure();
                    return;
                }*/


//                    /*if (mDebugMode)
//                    {
//                        MediaPlayer mMediaPlayer = new MediaPlayer();
//                        try {
//                            Uri mediaPath = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.elevator);
//                            mMediaPlayer.setDataSource(mContext.getApplicationContext(), mediaPath);
//                            mMediaPlayer.prepare();
//                            mMediaPlayer.start();
//
//                            Toast.makeText(mContext, "[엘레베이터]" + m, Toast.LENGTH_SHORT).show();
//                        } catch (Exception e) {
//                        }
//                    }*/
//
//                    //mSleepCnt++ ;
//                    mSleepCnt = mMaxSleepCnt;
//                    initMeasure();
//                    return;
//                }

                if (step > 1) { // 걷기중이면
                    AppUserBase user = DataHolder.instance().getAppUserBase();
                    SharedPreferences prefr = mContext.getSharedPreferences("userInfo", MODE_PRIVATE);

                    try {
                        mCurBuildCount = user.getBuild_floor_amt();
                    } catch (Exception ex) {
                        if (null == prefr) {
                            mCurBuildCount = 10;
                        } else {
                            mCurBuildCount = prefr.getInt("curBuildCount", 10);
                        }
                    }

                    try {
                        mIsBuild = user.getIsbuild();
                    } catch (Exception ex) {
                        mIsBuild = "Y";
                    }

                    if (gapAlitude > 0) {
                        mFloor++;
                        mLogicCount++;
                        mClimbCount = 0;

                        if ("Y".equals(mIsBuild)) {
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
                            //getTextView(R.id.txt_m).setText(m);
                        }

                        if ((mSaveStep <= mStairStepLimit)
                                || (System.currentTimeMillis() - goupTime) < mStairMeasureGap) {

                            if (mDebugMode) {
                                MediaPlayer mMediaPlayer = new MediaPlayer();
                                try {
                                    Uri mediaPath = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.elevator);
                                    mMediaPlayer.setDataSource(mContext.getApplicationContext(), mediaPath);
                                    mMediaPlayer.prepare();
                                    mMediaPlayer.start();

                                    if (mSaveStep <= 0) {
                                        Toast.makeText(mContext, "[부정측정] 걸음수 미달 : " + mSaveStep + "걸음", Toast.LENGTH_LONG).show();
                                    } else if ((System.currentTimeMillis() - goupTime) < 4000) {
                                        Toast.makeText(mContext, "[부정측정] 3초이내 측정시도 : " + (System.currentTimeMillis() - goupTime) + "초", Toast.LENGTH_LONG).show();
                                    }
                                } catch (Exception e) {
                                }
                            }

                            //mSleepCnt = mMaxSleepCnt;
                            initMeasure();
                            return;
                        }
                        goupTime = System.currentTimeMillis();
                        sendDataToServer(1, "building", m);
                    } else {
                        mBuildCount = 0;
                        mClimbCount = 0;
                        mLogicCount = 0;

                        mFloor++;
                        isCount = true;
                        if (isTest) {
                            //getTextView(R.id.txt_m).setText(m);
                        }

                        if ((mSaveStep <= mStairStepLimit)
                                || (System.currentTimeMillis() - goupTime) < mStairMeasureGap) {

                            if (mDebugMode) {
                                MediaPlayer mMediaPlayer = new MediaPlayer();
                                try {
                                    Uri mediaPath = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.elevator);
                                    mMediaPlayer.setDataSource(mContext.getApplicationContext(), mediaPath);
                                    mMediaPlayer.prepare();
                                    mMediaPlayer.start();

                                    if (mSaveStep <= 0) {
                                        Toast.makeText(mContext, "[부정측정] 걸음수 미달 : " + mSaveStep + "걸음", Toast.LENGTH_LONG).show();
                                    } else if ((System.currentTimeMillis() - goupTime) < 4000) {
                                        Toast.makeText(mContext, "[부정측정] 3초이내 측정시도 : " + (System.currentTimeMillis() - goupTime) + "초", Toast.LENGTH_LONG).show();
                                    }
                                } catch (Exception e) {
                                }
                            }

                            //mSleepCnt = mMaxSleepCnt;
                            initMeasure();
                            return;
                        }
                        goupTime = System.currentTimeMillis();
                        sendDataToServer(1, "building", m);
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

        if (mDir > 315 && isContinue) {
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
        if (is315 && cnt > (cnt315 + 10)) {
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
            // 2초갭에서 7초까지로 변경해봄
            //if (cnt < 20  || (curTime - goupTime) < 2000 ) {
            if (cnt < 60 || (curTime - goupTime) < 4000) {
                //mSleepCnt++;
                mSleepCnt = mMaxSleepCnt;
                initMeasure();
                return;
            } else {
                if (step > 1) { // 걷기중이면
                    if (gapAlitude > 0) {

                        AppUserBase user = DataHolder.instance().getAppUserBase();
                        SharedPreferences prefr = mContext.getSharedPreferences("userInfo", MODE_PRIVATE);

                        try {
                            mCurBuildCount = user.getBuild_floor_amt();
                        } catch (Exception ex) {
                            if (null == prefr) {
                                mCurBuildCount = 10;
                            } else {
                                mCurBuildCount = prefr.getInt("curBuildCount", 10);
                            }
                        }

                        try {
                            mIsBuild = user.getIsbuild();
                        } catch (Exception ex) {
                            mIsBuild = "Y";
                        }

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

                                //if ((mSaveStep <= 5)
                                if ((mSaveStep <= mMountainStepLimit)
                                        || Math.abs(gapAlitude) > mMountainHeightLimit
                                        || (System.currentTimeMillis() - goupTime) < mMountainMeasureGap) {

                                    if (mDebugMode) {
                                        MediaPlayer mMediaPlayer = new MediaPlayer();
                                        try {
                                            Uri mediaPath = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.elevator);
                                            mMediaPlayer.setDataSource(mContext.getApplicationContext(), mediaPath);
                                            mMediaPlayer.prepare();
                                            mMediaPlayer.start();

                                            if (mSaveStep <= 10) {
                                                Toast.makeText(mContext, "[부정측정] 걸음수 미달 : " + mSaveStep + "걸음", Toast.LENGTH_LONG).show();
                                            } else if ((System.currentTimeMillis() - goupTime) < 4000) {
                                                Toast.makeText(mContext, "[부정측정] 4초이내 측정시도 : " + (System.currentTimeMillis() - goupTime) + "초", Toast.LENGTH_LONG).show();
                                            } else if (Math.abs(gapAlitude) > 4) {
                                                Toast.makeText(mContext, "[부정측정] 4m 이상 높이 : " + Math.abs(gapAlitude) + "m", Toast.LENGTH_LONG).show();
                                            }
                                        } catch (Exception e) {
                                        }
                                    }

                                    mSleepCnt = mMaxSleepCnt;
                                    initMeasure();
                                    mSaveStep = 0;
                                    return;
                                }
                                goupTime = System.currentTimeMillis();
                                sendDataToServer(1, "notbuilding", m);
                            } else {

                                /**
                                 *
                                 *
                                 *
                                 *
                                 *
                                 *
                                 *
                                 *
                                 *
                                 *
                                 *
                                 * 이 부분에 대한 로직 추가 필요
                                 * 도원빌딩으로 강제 매핑했기 때문에,
                                 * 현재 오른 층수가 도원빌딩보다 높다고 해서, 빌딩이 아닌건 아님
                                 * 일단 계단을 올랐다는 로직 추가
                                 *
                                 *
                                 *
                                 *
                                 *
                                 *
                                 *
                                 *
                                 *
                                 * */


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
                                    //getTextView(R.id.txt_m).setText(m);
                                }

                                //if ((mSaveStep <= 5)
                                if ((mSaveStep <= mMountainStepLimit)
                                        || Math.abs(gapAlitude) > mMountainHeightLimit
                                        || (System.currentTimeMillis() - goupTime) < mMountainMeasureGap) {

                                    if (mDebugMode) {
                                        MediaPlayer mMediaPlayer = new MediaPlayer();
                                        try {
                                            Uri mediaPath = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.elevator);
                                            mMediaPlayer.setDataSource(mContext.getApplicationContext(), mediaPath);
                                            mMediaPlayer.prepare();
                                            mMediaPlayer.start();

                                            if (mSaveStep <= 10) {
                                                Toast.makeText(mContext, "[부정측정] 걸음수 미달 : " + mSaveStep + "걸음", Toast.LENGTH_LONG).show();
                                            } else if ((System.currentTimeMillis() - goupTime) < 4000) {
                                                Toast.makeText(mContext, "[부정측정] 4초이내 측정시도 : " + (System.currentTimeMillis() - goupTime) + "초", Toast.LENGTH_LONG).show();
                                            } else if (Math.abs(gapAlitude) > 4) {
                                                Toast.makeText(mContext, "[부정측정] 4m 이상 높이 : " + Math.abs(gapAlitude) + "m", Toast.LENGTH_LONG).show();
                                            }
                                        } catch (Exception e) {
                                        }
                                    }

                                    mSleepCnt = mMaxSleepCnt;
                                    initMeasure();
                                    mSaveStep = 0;
                                    return;
                                }
                                goupTime = System.currentTimeMillis();
                                sendDataToServer(1, "notbuilding", m);
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
                                //getTextView(R.id.txt_m).setText(m);
                            }

                            //if ((mSaveStep <= 5)
                            if ((mSaveStep <= mMountainStepLimit)
                                    || Math.abs(gapAlitude) > mMountainHeightLimit
                                    || (System.currentTimeMillis() - goupTime) < mMountainMeasureGap) {

                                if (mDebugMode) {
                                    MediaPlayer mMediaPlayer = new MediaPlayer();
                                    try {
                                        Uri mediaPath = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.elevator);
                                        mMediaPlayer.setDataSource(mContext.getApplicationContext(), mediaPath);
                                        mMediaPlayer.prepare();
                                        mMediaPlayer.start();

                                        if (mSaveStep <= 10) {
                                            Toast.makeText(mContext, "[부정측정] 걸음수 미달 : " + mSaveStep + "걸음", Toast.LENGTH_LONG).show();
                                        } else if ((System.currentTimeMillis() - goupTime) < 4000) {
                                            Toast.makeText(mContext, "[부정측정] 4초이내 측정시도 : " + (System.currentTimeMillis() - goupTime) + "초", Toast.LENGTH_LONG).show();
                                        } else if (Math.abs(gapAlitude) > 4) {
                                            Toast.makeText(mContext, "[부정측정] 4m 이상 높이 : " + Math.abs(gapAlitude) + "m", Toast.LENGTH_LONG).show();
                                        }
                                    } catch (Exception e) {
                                    }
                                }

                                mSleepCnt = mMaxSleepCnt;
                                initMeasure();
                                mSaveStep = 0;
                                return;
                            }
                            goupTime = System.currentTimeMillis();
                            sendDataToServer(1, "notbuilding", m);
                        }
                    } else {
                        mBuildCount = 0;
                        mClimbCount = 0;
                        mLogicCount = 0;
                        if (mIsBuild.equals("Y")) {
                            //mClimbCount++;

                            /**
                             *
                             *
                             *
                             *
                             *
                             *
                             *
                             *
                             *
                             *
                             *
                             * 이 부분에 대한 로직 추가 필요
                             * 도원빌딩으로 강제 매핑했기 때문에,
                             * 현재 오른 층수가 도원빌딩보다 높다고 해서, 빌딩이 아닌건 아님
                             * 일단 계단을 올랐다는 로직 추가
                             *
                             *
                             *
                             *
                             *
                             *
                             *
                             *
                             *
                             * */

                            mFloor++;
                            if (isTest) {
                                //getTextView(R.id.txt_m).setText(m);
                            }

                            //if ((mSaveStep <= 5)
                            if ((mSaveStep <= mMountainStepLimit)
                                    || Math.abs(gapAlitude) > mMountainHeightLimit
                                    || (System.currentTimeMillis() - goupTime) < mMountainMeasureGap) {

//                                if (mDebugMode)
////                                {
////                                    MediaPlayer mMediaPlayer = new MediaPlayer();
////                                    try {
////                                        Uri mediaPath = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.elevator);
////                                        mMediaPlayer.setDataSource(mContext.getApplicationContext(), mediaPath);
////                                        mMediaPlayer.prepare();
////                                        mMediaPlayer.start();
////
////                                        Toast.makeText(mContext, "[부정측정]" + "timegap : " + (System.currentTimeMillis() - goupTime) + " " + m, Toast.LENGTH_SHORT).show();
////                                    } catch (Exception e) {
////                                    }
////                                }

                                mSleepCnt = mMaxSleepCnt;
                                mSaveStep = 0;
                                initMeasure();
                                return;
                            }
                            goupTime = System.currentTimeMillis();
                            sendDataToServer(1, "notbuilding", m);
                        } else {
                            mFloor++;
                            if (isTest) {
                                //getTextView(R.id.txt_m).setText(m);
                            }

                            //if ((mSaveStep <= 5)
                            if ((mSaveStep <= mMountainStepLimit)
                                    || Math.abs(gapAlitude) > mMountainHeightLimit
                                    || (System.currentTimeMillis() - goupTime) < mMountainMeasureGap) {

                                if (mDebugMode) {
                                    MediaPlayer mMediaPlayer = new MediaPlayer();
                                    try {
                                        Uri mediaPath = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.elevator);
                                        mMediaPlayer.setDataSource(mContext.getApplicationContext(), mediaPath);
                                        mMediaPlayer.prepare();
                                        mMediaPlayer.start();

                                        if (mSaveStep <= 10) {
                                            Toast.makeText(mContext, "[부정측정] 걸음수 미달 : " + mSaveStep + "걸음", Toast.LENGTH_LONG).show();
                                        } else if ((System.currentTimeMillis() - goupTime) < 4000) {
                                            Toast.makeText(mContext, "[부정측정] 4초이내 측정시도 : " + (System.currentTimeMillis() - goupTime) + "초", Toast.LENGTH_LONG).show();
                                        } else if (Math.abs(gapAlitude) > 4) {
                                            Toast.makeText(mContext, "[부정측정] 4m 이상 높이 : " + Math.abs(gapAlitude) + "m", Toast.LENGTH_LONG).show();
                                        }
                                    } catch (Exception e) {
                                    }
                                }

                                mSleepCnt = mMaxSleepCnt;
                                initMeasure();
                                mSaveStep = 0;
                                return;
                            }
                            goupTime = System.currentTimeMillis();
                            sendDataToServer(1, "notbuilding", m);
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

    private void initMeasure() {
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

        // 차량 이동시 계단으로 측정되는 값을 줄이기 위한 값
        mTrashStep = 0;

        isCount = false;
        goupTime = System.currentTimeMillis();
        mMeasureStep = 0;

        // 5분 지나면 잠금
        if (mSleepCnt >= mMaxSleepCnt) {
            mMeasureStep = 0;
            isSleep = true;
            ;
            mSleepCnt = 0;
            mStarted = false;
            startMeasure(false);
        }

    }

    // 빌딩 층수 카운트 (빌딩 높이 )
    private void saveBuildCount(int count) {

    }

    // 측정 시작 ,멈춤
    public void startMeasure(boolean started) {
        try {
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

                /*mAltiManager.stopMeasure();
                mDirectionManager.stopMeasure();*/
                //mStepManager.stopMeasure();
                //mStepManager.stopMeasure();
                //findViewById(R.id.LayoutPause).setVisibility(View.VISIBLE);

            } else {
                AppUserBase user = DataHolder.instance().getAppUserBase();

                if (user != null) {
                    mCurBuildCount = user.getBuild_floor_amt(); // 현재 빌딩층수 (높이)
                } else {
                    SharedPreferences prefr = mContext.getSharedPreferences("userInfo", MODE_PRIVATE);

                    if (null == prefr) {
                        mCurBuildCount = 10;
                    } else {
                        mCurBuildCount = prefr.getInt("curBuildCount", 10);
                    }
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


                //mAltiManager = new AltitudeManager(mContext);
                //mDirectionManager = new DirectionManager(mContext);

                isSleep = false;

                //mAltiManager.startMeasure();
                //mDirectionManager.startMeasure();
                Log.d("stepsensor", "START");
                //mStepManager.startMeasure();


                //findViewById(R.id.LayoutPause).setVisibility(View.INVISIBLE);
                //mValue.setText("wait...");
            }
            mStarted = started;
        } catch (Exception ex) {
            mStarted = started;
        }
    }

    public void setCurrentAltitude(double altitude) {
        mAltitude = altitude;
    }

    public void setCurrentAltitude2(double altitude) {
        mAltitude2 = altitude;
    }

    public void setCurrentOrientation(double val) {

        mOrientation = val;
    }

    public void setCurrentOrientation2(double x, double y, double z) {
        mX = x;
        mY = y;
        mZ = z;
        //displayAlti();
    }

    public void setCurrentStep(double val) {
        mStep += val;
        mTrashStep += val;
        mMeasureStep += val;
        //Log.d("999999999999777771", "[stepsensor] " + mMeasureStep + " " + mStarted + " " + mStep);
        /*if (!mStarted && mMeasureStep > 20) {
            Toast.makeText(mContext, "측정서비스재시작 " + mMeasureStep, Toast.LENGTH_SHORT).show();
            mMeasureStep = 0;
            restartTracking();
        }*/
        if (!mStarted && isSleep && !AppConst.IS_STEP_SENSOR_LOADED) {

            AppConst.IS_STEP_SENSOR_LOADED = true;
            //try { Thread.sleep(1000); } catch (Exception ex) { }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    restartTracking();
                }
            }, 1000);


        }

    }

    public void setCheckMove(double val) {
        if (!mStarted && isSleep && !AppConst.IS_STEP_SENSOR_LOADED) {

            AppConst.IS_STEP_SENSOR_LOADED = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    restartTracking();
                }
            }, 1000);

        }
    }

    private void restartTracking() {
        try {
            //mStepManager.stopMeasure();

            mStarted = true;

            startMeasure(true);
            mStartList.clear();
            for (int i = 0; i < 600; i++) {
                mStartList.add(new MeasureObj());
            }
            cnt = 0;
            isSleep = false;
            AppConst.IS_STEP_SENSOR_LOADED = false;
            mMeasureStep = 0;
        } catch (Exception e) {
        }
        /*try {
            mStepManager.stopMeasure();
            Thread.sleep(500);
        } catch (Exception e) {
            Log.d("999999999999777771", "[stepsensor] unregist failed on RestartTracking : " + e.toString());
        }*/

        /*startMeasure(false);

        Intent startintent = new Intent(mContext, StepCounterService.class);
        mContext.stopService(startintent);

        try {
            Thread.sleep(1000);
            mContext.startService(startintent);
        } catch (Exception e) { }


        //isRestarting = false;
        mMeasureStep = 0;*/


    }

    private void sendScreenRefresh() {
        Log.d("999999999999777771", "stairUp-SEND");

        mContext.sendBroadcast(new Intent(Env.Action.APP_IS_BACKGROUND_ACTION.action()));
    }

    /**
     * 계단을 올라간 데이터를 서버로 전송
     */

    private void sendDataToServer(int goupAmt, String type, String measure) {
        SharedPreferences pref = mContext.getSharedPreferences("unsent", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        try {
            // 등산 오르기 측정시, 리턴
            if ("notbuilding".equals(type)) {
                mSleepCnt = 0;
                initMeasure();
                return;
            }


            if (mDebugMode) {
                if ("notbuilding".equals(type)) {
                    Toast.makeText(mContext, "[등산 오르기] " + measure, Toast.LENGTH_SHORT).show();
                    mSleepCnt = 0;
                    initMeasure();
                } else {
                    Toast.makeText(mContext, "[계단 오르기] " + measure, Toast.LENGTH_SHORT).show();
                    mSleepCnt = 0;
                    mTrashStep = 0;
                }
            }

            mSleepCnt = 0;
            // 5걸음 이상 걷지 않았을 경우, 계단수에서 빼도록 처리
            /*if (mTrashStep < 10) {
                return;
            }*/

            /*if (mSaveStep <=  10 ) {
                return;
            }*/

            String token = KUtil.getUserToken();
            String buildCode = KUtil.getBuildingCode();

            if (StringUtil.isEmptyOrWhiteSpace(token) || StringUtil.isEmptyOrWhiteSpace(buildCode)) {
                return;
            }

            AppUserBase user = DataHolder.instance().getAppUserBase();

            SharedPreferences prefr = mContext.getSharedPreferences("userInfo", MODE_PRIVATE);
            if (null == prefr) return;
            String userToken = prefr.getString("token", "");
            if ("".equals(userToken)) {
                return;
            }

            if (mDebugMode) {
                MediaPlayer mMediaPlayer = new MediaPlayer();

                try {
                    if ("building".equals(type)) {
                        Uri mediaPath = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.alert);
                        mMediaPlayer.setDataSource(mContext.getApplicationContext(), mediaPath);
                        mMediaPlayer.prepare();
                        mMediaPlayer.start();
                    } else {
                        Uri mediaPath = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.alert2);
                        mMediaPlayer.setDataSource(mContext.getApplicationContext(), mediaPath);
                        mMediaPlayer.prepare();
                        mMediaPlayer.start();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //return;
            try {
                Map<String, Object> query = new HashMap<>();
                query.put("token", prefr.getString("token", ""));
                query.put("beacon_uuid", "");
                query.put("major_value", "");
                query.put("minor_value", "");
                query.put("install_floor", "");
                //map.put("beacon_seq", getBeaconSeq());
                query.put("beacon_seq", prefr.getInt("beacon_seq", 0));
                try {
                    if (user.getBeacon_seq() == 0) {
                        DefaultCode.BEACON_SEQ.getValue();
                    }
                } catch (Exception e) {
                    DefaultCode.BEACON_SEQ.getValue();
                }
                query.put("stair_seq", prefr.getInt("stair_seq", 0));
                query.put("build_seq", prefr.getString("build_seq", ""));
                query.put("build_name", prefr.getString("build_name", ""));
                query.put("floor_amount", 0);
                query.put("stair_amount", 0);
                query.put("cust_seq", prefr.getInt("cust_seq", 0));
                query.put("cust_name", prefr.getString("cust_name", ""));
                query.put("build_code", prefr.getString("build_code", ""));
                query.put("godo", mAltitude);
                //query.put("goup_amt", goupAmt+mUnsentStairCnt);
                query.put("goup_amt", goupAmt);
                query.put("curBuildCount", mCurBuildCount);
                query.put("buildCount", mBuildCount);
                query.put("isbuild", mIsBuild);
                query.put("country", prefr.getString("country", "대한민국"));
                query.put("city", prefr.getString("city", "서울특별시"));

                if (isRedDot) {
                    query.put("start_time", startTime);
                    query.put("end_time", endTime);
                }

                //Toast.makeText(mContext, "[계단 미전송] " + mUnsentStairCnt, Toast.LENGTH_SHORT).show();

                final String localTime = DateUtil.currentDate("yyyyMMddHHmmss");

                if (NetworkConnectivityReceiver.isConnected(mContext)) {
                    Call<BeaconUuid> call = getAppService().sendStairGoUpAmountToServer3(query);
                    final String goupSentTime = DateUtil.currentDate("yyyyMMdd HHmmss");

                    call.enqueue(new Callback<BeaconUuid>() {
                        @Override
                        public void onResponse(Call<BeaconUuid> call, Response<BeaconUuid> response) {
                            /*
                            if (isReddot) {
                                mReddotStack.clear();
                            }
                            */
                            LogUtils.e(TAG, "success-data response raw:" + response.raw().toString());
                            if (response.isSuccessful()) {
                                LogUtils.e(TAG, "success-data response body:" + response.body().string());

                                BeaconUuid uuid = response.body();
                                if (uuid != null && uuid.isSuccess()) {
                                    //broadcastServerResult(uuid);
                                    updateFloorCount(uuid);
                                    if ("unsent".equals(type)) {
                                        mUnsentStairCnt = 0;
                                        editor.putInt("unsent", mUnsentStairCnt);
                                        editor.commit();
                                    }
//                                    else {
////                                        mUnsentStairCnt = pref.getInt("unsent", 0);
////                                    }
                                } else {
                                    mUnsentStairCnt++;
                                    editor.putInt("unsent", mUnsentStairCnt);
                                    editor.commit();
                                    //saveGoUpDataToLocalDb(query, localTime);
                                }
                                //sendBeaconLog(beaconUuid, floorDiff, "go up ", goupSentTime);//층간이동 전송 성공하면 로그 전송


                                // 노티피케이션 업데이트
                                //requestgetActivityRecords();
                                // 메인 화면 업데이트
                                //sendScreenRefresh();


                            } else {
                                mUnsentStairCnt++;
                                editor.putInt("unsent", mUnsentStairCnt);
                                editor.commit();
                                //saveGoUpDataToLocalDb(query, localTime);
                            }
                        }

                        @Override
                        public void onFailure(Call<BeaconUuid> call, Throwable t) {
                            mUnsentStairCnt++;
                            editor.putInt("unsent", mUnsentStairCnt);
                            editor.commit();
                            //saveGoUpDataToLocalDb(query, localTime);
                        }
                    });
                } else {
                    mUnsentStairCnt++;
                    editor.putInt("unsent", mUnsentStairCnt);
                    editor.commit();
                    //saveGoUpDataToLocalDb(query, localTime);
                }

                if ("notbuilding".equals(type)) {
                    mSaveStep = 0;
                    initMeasure();
                }

            } catch (Exception e) {
                //Toast.makeText(mContext, e.toString(), Toast.LENGTH_SHORT);
                mUnsentStairCnt++;

                editor.putInt("unsent", mUnsentStairCnt);
                editor.commit();
            }

            // 어제자 걸음수 서버 전송
            //getYesterDayStepCount();

            //Toast.makeText(mContext, "GFITNESS go up " + LAST_SENT_WALKING_DATE, Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            mUnsentStairCnt++;
            editor.putInt("unsent", mUnsentStairCnt);
            editor.commit();
        }
    }

    /**
     * 계단 올라간 데이터 서버전송 실패하면 로컬에 저장 후 네트워크가 안정화 되면 전송
     *
     * @param goupData
     */
    private void saveGoUpDataToLocalDb(Map<String, Object> goupData, String localTime) {
        try {
            goupData.put("app_time", localTime);
            if (KsDbWorker.insertFailData(mContext, goupData)) {
                BusProvider.instance().post(
                        new KsEvent<Map<String, Object>>()
                                .setType(KsEvent.Type.UPDATE_FLOOR_AMOUNT_FAIL)
                                .setValue(goupData)
                                .setMainCharacterChanged(false));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 올라간 층수 데이터 서버전송 성공시 데이터 전파
     *
     * @param beaconUuid
     */
    public void broadcastServerResult(BeaconUuid beaconUuid) {

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

    public void updateFloorCount(BeaconUuid beaconUuid) {


        BusProvider.instance().post(
                new KsEvent<BeaconUuid>()
                        .setType(KsEvent.Type.UPDATE_FLOOR_AMOUNT)
                        .setValue(beaconUuid)
                        .setMainCharacterChanged(false)
        );
    }

    public void requestgetActivityRecords() {
        Map<String, Object> query = KUtil.getDefaultQueryMap();
        Call<ActivityRecord> call = getUserService().getActivityRecords(query);
        call.enqueue(new Callback<ActivityRecord>() {
            @Override
            public void onResponse(Call<ActivityRecord> call, Response<ActivityRecord> response) {
                LogUtils.err(TAG, response.raw().toString());
                if (response.isSuccessful()) {
                    ActivityRecord record = response.body();
                    if (record.isSuccess()) {

                        Record total = record.getTotalRecord();
                        Record r_rank = record.getTotalRank();
                        //Record r_rank = record.getTodayRank();

                        if (null != r_rank) {
                            AppConst.NOTI_RANKS = StringUtil.format(Double.parseDouble(r_rank.getRank()), "#,##0");
                        } else {
                            AppConst.NOTI_RANKS = "-";
                        }
                        AppConst.NOTI_FLOORS = StringUtil.format(total.getAmountToFloat(), "#,##0");
                        AppConst.NOTI_CALS = KUtil.calcCalorie(total.getAmountToFloat(), total.getStairAmountToFloat());
                        AppConst.NOTI_SECS = KUtil.calcLife(total.getAmountToFloat(), total.getStairAmountToFloat());

                        updateNotification(AppConst.NOTI_RANKS, AppConst.NOTI_FLOORS, AppConst.NOTI_CALS, AppConst.NOTI_SECS);
                    } else {
                    }
                } else {
                }
            }

            @Override
            public void onFailure(Call<ActivityRecord> call, Throwable t) {
                //toast(R.string.warn_commu_to_server);
                LogUtils.err(TAG, t);
            }
        });
    }

//    private int distance(double lat1, double lon1, double lat2, double lon2, String unit) {
//
//        double theta = lon1 - lon2;
//        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
//
//        dist = Math.acos(dist);
//        dist = rad2deg(dist);
//        dist = dist * 60 * 1.1515;
//
//        if (unit == "kilometer") {
//            dist = dist * 1.609344;
//        } else if(unit == "meter"){
//            dist = dist * 1609.344;
//        }
//
//        mPreLat = lat2;
//        mPreLng = lon2;
//
//        return ((int) dist);
//    }

    private int distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        float[] distance = new float[2];

        Location.distanceBetween(lat1, lon1,
                lat2, lon2, distance);

        mPreLat = lat2;
        mPreLng = lon2;

        return (int) distance[0];
    }


    // This function converts decimal degrees to radians
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    // This function converts radians to decimal degrees
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    private static void updateNotification(String ranking, String floor, String cal, String sec) {


        if (!"-".equals(ranking)) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, AppConst.NOTIFICATION_CHANNEL_ID);

            Intent intent = new Intent(mContext, MainActivity.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            PendingIntent contentPendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setContentTitle("[랭킹:" + ranking + "]")
                    .setContentText(floor + "F / " + cal + "kcal / " + sec + "sec")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_floor))
                    .setWhen(System.currentTimeMillis())
                    .setOngoing(true)
                    //.setColorized(true)
                    .setColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark))
                    .setContentIntent(contentPendingIntent);

            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(AppConst.NOTIFICATION_ID, builder.build());

            //Toast.makeText(mContext, "상태바1", Toast.LENGTH_SHORT).show();
        } else {
            //Toast.makeText(mContext, "상태바2", Toast.LENGTH_SHORT).show();
        }


    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public boolean isAppOnForeground() {
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = mContext.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    final LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {

            String provider = location.getProvider();
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            double altitude = location.getAltitude();

            mLat = latitude;
            mLng = longitude;

            //Log.d("DDDDDD", mLat + "______" + mLng);
            Toast.makeText(mContext, mLat + "________" + mPreLat + "\n" + mLng + "________" + mPreLng, Toast.LENGTH_SHORT).show();
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };

    public void acquireCPUWakelock() {
        PowerManager powerManager = (PowerManager) mContext.getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyApp::MyWakelockTag");
        if (!wakeLock.isHeld()) wakeLock.acquire();
    }


    private void getYesterDayStepCount() {
        try {

            // Insert custom code here

            DateFormat d_dateFormat = new SimpleDateFormat("yyyyMMdd");
            Calendar cal = Calendar.getInstance();
            Date now = new Date();

            cal.setTime(now);
            String today_date = d_dateFormat.format(cal.getTime());

            Log.d("TTTTTTTTTTTTTTTTTTTTT", "SEND WALK DATA CHECK 1 : " + today_date + "___" + LAST_SENT_WALKING_DATE);

            SharedPreferences fit_prefr = mContext.getSharedPreferences("fitnessauth", MODE_PRIVATE);
            boolean is_fit_permission = fit_prefr.getBoolean("isPermission", false);
            if (!is_fit_permission) {
            } else {

                if (!today_date.equals(LAST_SENT_WALKING_DATE)) {
                    Log.d("TTTTTTTTTTTTTTTTTTTTT", "SEND");
                    readYesterdayHistoryData();
                } else {
                    Log.d("TTTTTTTTTTTTTTTTTTTTT", "ALREADY SEND WALK DATA");
                    //readYesterdayHistoryData();
                }
            }
        } catch (Exception e) {
        }
    }

    private Task<DataReadResponse> readYesterdayHistoryData() {
        // Begin by creating the query.
        DataReadRequest readRequest = queryYesterdayFitnessData();

        // Invoke the History API to fetch the data with the query
        return Fitness.getHistoryClient(mContext, GoogleSignIn.getLastSignedInAccount(mContext))
                .readData(readRequest)
                .addOnSuccessListener(
                        new OnSuccessListener<DataReadResponse>() {
                            @Override
                            public void onSuccess(DataReadResponse dataReadResponse) {
                                // For the sake of the sample, we'll print the data so we can see what we just
                                // added. In general, logging fitness information should be avoided for privacy
                                // reasons.
                                printYesterDayData(dataReadResponse);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("TTTTTTTTTTTTTTTTTTTTT", e.toString());
                                //Toast.makeText(mContext, "go up " + e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
    }


    public static DataReadRequest queryYesterdayFitnessData() {
        // [START build_read_data_request]
        // Setting a start and end date using a range of 1 week before this moment.
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.getTimeInMillis();
        long startTime = cal.getTimeInMillis();

        long endTime = startTime + (24 * 60 * 60 * 1000);

        java.text.DateFormat dateFormat = getDateTimeInstance();

        DataReadRequest readRequest =
                new DataReadRequest.Builder()
                        // The data request can specify multiple data types to return, effectively
                        // combining multiple data queries into one call.
                        // In this example, it's very unlikely that the request is for several hundred
                        // datapoints each consisting of a few steps and a timestamp.  The more likely
                        // scenario is wanting to see how many steps were walked per day, for 7 days.
                        .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                        // Analogous to a "Group By" in SQL, defines how data should be aggregated.
                        // bucketByTime allows for a time span, whereas bucketBySession would allow
                        // bucketing by "sessions", which would need to be defined in code.
                        .bucketByTime(1, TimeUnit.DAYS)
                        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                        .build();


        // [END build_read_data_request]

        return readRequest;
    }

    public static void printYesterDayData(DataReadResponse dataReadResult) {
        // [START parse_read_data_result]
        // If the DataReadRequest object specified aggregated data, dataReadResult will be returned
        // as buckets containing DataSets, instead of just DataSets.
        if (dataReadResult.getBuckets().size() > 0) {
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    dumpYesterdayDataSet(dataSet);
                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                dumpYesterdayDataSet(dataSet);
            }
        } else {

        }
        // [END parse_read_data_result]
    }

    // [START parse_dataset]
    private static void dumpYesterdayDataSet(DataSet dataSet) {
        Log.d("TTTTTTTTTTTTTTTTTTTTT", "[99] Data returned for Data type: " + dataSet.getDataType().getName());
        Toast.makeText(mContext, "GFITNESS [99] Data returned for Data type: " + dataSet.getDataType().getName(), Toast.LENGTH_SHORT).show();
        DateFormat dateFormat = getTimeInstance();
        DateFormat d_dateFormat = new SimpleDateFormat("yyyyMMdd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        String s_date = d_dateFormat.format(cal.getTime());

        for (DataPoint dp : dataSet.getDataPoints()) {
            for (Field field : dp.getDataType().getFields()) {
                // 어제자 걸음수 전송
                try {
                    saveWalkStep(s_date, Integer.parseInt(dp.getValue(field).toString()));
                } catch (Exception ex) {
                    Log.d("TTTTTTTTTTTTTTTTTTTTT", ex.toString());
                }

            }
        }
    }

    public static void saveWalkStep(String date, int walk_count) {
        try {
            Log.d("TTTTTTTTTTTTTTTTTTTTT", "SEND WALK DATA 2 : " + date + " " + walk_count);
            Map<String, Object> query = KUtil.getDefaultQueryMap();
            int user_seq = Pref.instance().getIntValue(PrefKey.USER_SEQ, 0);
            Log.d("TTTTTTTTTTTTTTTTTTTTT", "USER_SEQ : " + user_seq);

            if (user_seq == 0) return;

            query.put("user_seq", String.valueOf(user_seq));
            query.put("walk_date", date);
            query.put("walk_count", walk_count);

            Call<ResponseBase> call = ((KoswApp) Utils.getApp()).userService.saveWalkStep(query);
            call.enqueue(new Callback<ResponseBase>() {
                @Override
                public void onResponse(Call<ResponseBase> call, Response<ResponseBase> response) {
                    Log.d("MAINFRAGMENT", "____________main data : " + response.raw().toString());
                    if (response.isSuccessful()) {
                        ResponseBase data = response.body();
                        if (data.isSuccess()) {
                            DateFormat dateFormat = getTimeInstance();
                            DateFormat d_dateFormat = new SimpleDateFormat("yyyyMMdd");
                            Calendar cal = Calendar.getInstance();
                            Date now = new Date();
                            cal.setTime(now);
                            String today_date = d_dateFormat.format(cal.getTime());

                            LAST_SENT_WALKING_DATE = today_date;
                            Toast.makeText(mContext, "GFITNESS send success " + walk_count + "_" + LAST_SENT_WALKING_DATE, Toast.LENGTH_SHORT).show();
                            Log.d("TTTTTTTTTTTTTTTTTTTTT", "SUCCESS");
                        } else {
                            Log.d("TTTTTTTTTTTTTTTTTTTTT", "FAILED");
                            Toast.makeText(mContext, "GFITNESS send failed " + LAST_SENT_WALKING_DATE, Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBase> call, Throwable t) {
                    Log.d("TTTTTTTTTTTTTTTTTTTTT", t.toString());
                }
            });
        } catch (Exception ex) {
            Log.d("TTTTTTTTTTTTTTTTTTTTT", ex.toString());
            ex.printStackTrace();
        }
    }

}

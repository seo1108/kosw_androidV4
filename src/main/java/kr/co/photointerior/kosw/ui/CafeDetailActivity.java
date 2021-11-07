package kr.co.photointerior.kosw.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.CollectionUtils;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import kr.co.photointerior.kosw.R;
import kr.co.photointerior.kosw.global.Env;
import kr.co.photointerior.kosw.listener.ClickListener;
import kr.co.photointerior.kosw.listener.ItemClickListener;
import kr.co.photointerior.kosw.rest.model.AppUserBase;
import kr.co.photointerior.kosw.rest.model.Cafe;
import kr.co.photointerior.kosw.rest.model.CafeDetail;
import kr.co.photointerior.kosw.rest.model.CafeNotice;
import kr.co.photointerior.kosw.rest.model.CafeRankingList;
import kr.co.photointerior.kosw.rest.model.CafeSubCategory;
import kr.co.photointerior.kosw.rest.model.DataHolder;
import kr.co.photointerior.kosw.rest.model.MyInfo;
import kr.co.photointerior.kosw.rest.model.Notice;
import kr.co.photointerior.kosw.rest.model.PageResponseBase;
import kr.co.photointerior.kosw.rest.model.RankInCafe;
import kr.co.photointerior.kosw.rest.model.ResponseBase;
import kr.co.photointerior.kosw.ui.row.RowCafeBbs;
import kr.co.photointerior.kosw.ui.row.RowCafeBbsComment;
import kr.co.photointerior.kosw.ui.row.RowDotSeparator;
import kr.co.photointerior.kosw.utils.KUtil;
import kr.co.photointerior.kosw.utils.LogUtils;
import kr.co.photointerior.kosw.widget.CircleImageView;
import kr.co.photointerior.kosw.widget.KoswButton;
import kr.co.photointerior.kosw.widget.KoswEditText;
import kr.co.photointerior.kosw.widget.KoswTextView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CafeDetailActivity extends BaseActivity {
    private String TAG = LogUtils.makeLogTag(CafeDetailActivity.class);
    private Cafe mCafe;
    private MyInfo mMyInfo;

    private int[] mBtnResId = {
            R.id.btn_notice,
            R.id.btn_daily,
            R.id.btn_weekly,
            R.id.btn_monthly
    };
    private int mSelectedBtnId = mBtnResId[0];

    private LinearLayout ll_ranking;
    private KoswTextView tv_title, txt_cafename, txt_cafe_message, txt_notice_date, txt_notice;
    private KoswTextView txt_open_date, txt_member, txt_admin;
    private KoswEditText txt_cafedesc;
    private KoswEditText bbs_count;
    private KoswButton btn_join_cafe;
    private AppCompatSpinner spinner, spinner_category;
    private ImageView btn_back, img_post, btn_config;
    private LinearLayout ll_notice, notice_linearlayout, ll_notice_detail, ll_recycler;
    private NestedScrollView sv;
    private String mCafeseq = "";
    private String mCafekey = "";
    private List<CafeSubCategory> mCate;
    private TextView tv_ranking, tv_name, tv_depart, tv_amount;

    private RecyclerView mRecyclerView;
    private RankingAdapter mAdapter;
    private CafeRankingList mRankResult;
    private PageResponseBase<CafeNotice> mBbsResult;
    private PageResponseBase<Notice> mNotifyResult;

    private String mBtnType;

    /**
     * 1, 3, 5 : 계단
     * 2, 4, 6 : 걸음
     */
    private String mSelectedSpinnerItem = "1";
    private String mSelectedCategorySpinnerItem = "-1";

    private int mBbsCreateResultCode = 1000;
    private int mCafeConfigurationResultCode = 2000;

    private int offsetY = 0;
    private String bbsType = "";

    private List<Cafe> mCAdminList = new ArrayList<>();

    private String isAdmin = "N";
    private String mMyCatename = "";

    private String mType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cafe_detail);

        mCafeseq = getIntent().getStringExtra("cafeseq");
        mCafekey = getIntent().getStringExtra("cafekey");
        mType = getIntent().getStringExtra("TYPE");

        getCafeDetail();
    }

    @Override
    protected void findViews() {
        String confirm = mCafe.getConfirm();
        String join = mCafe.getIsjoin();

        ll_recycler = findViewById(R.id.ll_recycler);

        ll_ranking = findViewById(R.id.ll_ranking);

        sv = findViewById(R.id.sv);

        btn_config = findViewById(R.id.btn_config);
        btn_config.setVisibility(View.VISIBLE);

        showCafeConfig();

        tv_title = findViewById(R.id.tv_title);
        tv_title.setText(mCafe.getCafename());

        txt_cafename = findViewById(R.id.txt_cafename);
        txt_cafename.setTypeface(txt_cafename.getTypeface(), Typeface.BOLD);
        txt_cafename.setText(mCafe.getCafename());

        txt_cafedesc = findViewById(R.id.txt_cafedesc);
        txt_cafedesc.setText(mCafe.getCafedesc());

        txt_cafe_message = findViewById(R.id.txt_cafe_message);
        txt_cafe_message.setTypeface(txt_cafe_message.getTypeface(), Typeface.BOLD);

        btn_join_cafe = findViewById(R.id.btn_join_cafe);

        // 미가입 카페일 경우
        if (!"1".equals(join)) {
            btn_join_cafe.setText(getResources().getString(R.string.txt_cafe_join_btn));
            if ("Y".equals(confirm)) {
                txt_cafe_message.setText(getResources().getString(R.string.txt_privacy_hide_exp));
            } else {
                txt_cafe_message.setText(getResources().getString(R.string.txt_privacy_open_exp));
            }
        } else    // 가입카페일 경우
        {
            btn_join_cafe.setText(getResources().getString(R.string.txt_invite));
            txt_cafe_message.setText(getResources().getString(R.string.txt_cafe_invite_message));
        }


        txt_notice_date = findViewById(R.id.txt_notice_date);
        txt_notice_date.setTypeface(txt_notice_date.getTypeface(), Typeface.BOLD);

        ll_notice_detail = findViewById(R.id.ll_notice_detail);

        txt_notice = findViewById(R.id.txt_notice);

        String confirmMessage = "";
        if ("Y".equals(confirm)) {
            confirmMessage = "비공개";
        } else {
            confirmMessage = "공개";
        }

        txt_open_date = findViewById(R.id.txt_open_date);
        txt_open_date.setText(mCafe.getOpendate() + "  " + confirmMessage);

        txt_member = findViewById(R.id.txt_member);
        txt_member.setText("맴버: " + mCafe.getTotal() + "명");

        txt_admin = findViewById(R.id.txt_admin);
        if (null == mCafe.getAdmin() || "".equals(mCafe.getAdmin())) {
            txt_admin.setVisibility(View.INVISIBLE);
        } else {
            txt_admin.setText("관리자: " + mCafe.getAdmin());
        }

        ll_notice = findViewById(R.id.ll_notice);
        notice_linearlayout = findViewById(R.id.notice_linearlayout);
        bbs_count = findViewById(R.id.bbs_count);

        spinner = findViewById(R.id.spinner);
        spinner_category = findViewById(R.id.spinner_category);

        tv_ranking = findViewById(R.id.tv_ranking);
        tv_name = findViewById(R.id.tv_name);
        tv_depart = findViewById(R.id.tv_depart);
        tv_amount = findViewById(R.id.tv_amount);

        mRecyclerView = findViewById(R.id.recycler_view);
        myInfinite = initMyInfinite(mRecyclerView);

        img_post = findViewById(R.id.img_post);
        // 미가입 카페일 경우
        if (!"1".equals(join)) {
            img_post.setVisibility(View.GONE);
        }
        btn_back = findViewById(R.id.btn_back);

        new Handler().postDelayed(() -> {
            showCafeConfig();
        }, 2000);

        new Handler().postDelayed(() -> {
            showCafeConfig();
        }, 5000);

        new Handler().postDelayed(() -> {
            showCafeConfig();
        }, 8000);

        new Handler().postDelayed(() -> {
            showCafeConfig();
        }, 10000);

    }

    @Override
    protected void attachEvents() {
        View.OnClickListener listener = new ClickListener(this);
        for (int rs : mBtnResId) {
            getView(rs).setOnClickListener(listener);
        }
        toggleBtn(mBtnResId[0]);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setSpinnerCategory();
                if (!"N".equals(mBtnType)) {
                    spinner.setVisibility(View.VISIBLE);
                    Category cate = (Category) parent.getSelectedItem();
                    mSelectedSpinnerItem = cate.getId();
                    if (Arrays.asList("5", "6").contains(mSelectedSpinnerItem)) {
                        setSpinnerCategory();
                    }
                } else {
                    spinner.setVisibility(View.GONE);
                    if (null != mAdapter) mAdapter.clear();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinner_category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if ("N".equals(mBtnType)) {
                    if (null != mAdapter) mAdapter.clear();
                } else {
                    SubCategory cate = (SubCategory) parent.getSelectedItem();
                    mSelectedCategorySpinnerItem = cate.getId();
                    myInfinite.performFirstLoad();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btn_join_cafe.setOnClickListener(v -> {
            if ("1".equals(mCafe.getIsjoin())) {
                // 초대하기 기능 추가
                shareCafe();
            } else {
                String catenames = "";
                String cateseqs = "";
                String additions = "";

                if (null != mCafe.getAdditions()) {
                    additions = mCafe.getAdditions();
                }
                if (null != mCate && mCate.size() > 0) {
                    int idx = 0;
                    catenames += "분류(부서)를 선택하세요#@#";
                    cateseqs += "0#@#";
                    for (int i = 0; i < mCate.size(); i++) {
                        catenames += mCate.get(i).getName() + "#@#";
                        cateseqs += mCate.get(i).getCateseq() + "#@#";
                    }

                    catenames = catenames.substring(0, catenames.length() - 3);
                    cateseqs = cateseqs.substring(0, cateseqs.length() - 3);
                }

                if ("".equals(additions) && "".equals(cateseqs)) {
                    // 부서 정보와 추가 정보가 없으면 바로 카페 가입
                    join();
                } else {
                    Intent intent = new Intent(this, CafeJoinActivity.class);
                    intent.putExtra("cafeseq", mCafe.getCafeseq());
                    intent.putExtra("cafename", mCafe.getCafename());
                    intent.putExtra("catenames", catenames);
                    intent.putExtra("cateseqs", cateseqs);
                    intent.putExtra("additions", additions);
                    startActivityForResult(intent, mCafeConfigurationResultCode);
                }
            }
        });

        img_post.setOnClickListener(v -> {
            bbsType = "BBS";
            Intent intent = new Intent(this, BbsPostActivity.class);
            intent.putExtra("cafeseq", mCafeseq);
            intent.putExtra("postType", "BBS");
            startActivityForResult(intent, mBbsCreateResultCode);
        });

        btn_back.setOnClickListener(v -> {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        });

        btn_config.setOnClickListener(v -> {
            if ("Y".equals(isAdmin)) {
                Intent intent = new Intent(this, CafeConfigActivity.class);
                intent.putExtra("cafeseq", mCafeseq);
                intent.putExtra("type", "MODIFY");
                startActivityForResult(intent, mCafeConfigurationResultCode);
            } else {
                //btn_config.setVisibility(View.GONE);
                toast("카페 관리자만 이용하실 수 있습니다.");
            }
        });
    }

    @Override
    protected void setInitialData() {
        showCafeConfig();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK == resultCode) {
            if (requestCode == mBbsCreateResultCode) {
                mBbsResult = null;
                getCafeBBS();
            } else if (requestCode == mCafeConfigurationResultCode) {
                Bundle bu = new Bundle();
                bu.putString("cafeseq", mCafe.getCafeseq());
                bu.putSerializable("cafekey", mCafe.getCafekey());
                // kmj mod
                callActivity(CafeDetailActivity.class, bu, true);
            }
        }
    }

    private void setSpinner() {
        ArrayList<Category> categoryList = new ArrayList<>();

        categoryList.add(new Category("1", getString(R.string.cafe_rank_individual)));
        categoryList.add(new Category("2", getString(R.string.cafe_rank_walk_individual)));
        categoryList.add(new Category("3", getString(R.string.cafe_rank_category)));
        categoryList.add(new Category("4", getString(R.string.cafe_rank_walk_category)));
        categoryList.add(new Category("5", getString(R.string.cafe_rank_cate_indi)));
        categoryList.add(new Category("6", getString(R.string.cafe_rank_walk_cate_indi)));

        ArrayAdapter<Category> adapter = new ArrayAdapter<Category>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, categoryList);
        spinner.setAdapter(adapter);

    }

    private void setSpinnerCategory() {
        if (CollectionUtils.isEmpty(mCate) ||
                mBtnType.equals("N") ||
                !Arrays.asList("5", "6").contains(mSelectedSpinnerItem)
        ) {
            spinner_category.setVisibility(View.GONE);
            return;
        }

        spinner_category.setVisibility(View.VISIBLE);
        if (spinner_category.getAdapter() != null && !spinner_category.getAdapter().isEmpty()) {
            return;
        }

        ArrayList<SubCategory> categoryList = new ArrayList<>();
        for (int idx = 0; idx < mCate.size(); idx++) {
            if (idx == 0) mSelectedCategorySpinnerItem = mCate.get(idx).getCateseq();

            if ("1".equals(mCafe.getIsjoin())) {
                categoryList.add(new SubCategory(mCate.get(idx).getCateseq(), mCate.get(idx).getName()));
            } else {
                categoryList.add(new SubCategory(mCate.get(idx).getCateseq(), "*****"));
            }
        }

        ArrayAdapter<SubCategory> adapter = new ArrayAdapter<SubCategory>(
                getApplicationContext(),
                android.R.layout.simple_spinner_dropdown_item,
                categoryList
        );
        spinner_category.setAdapter(adapter);
    }

    private void getCafeDetail() {
        AppUserBase user = DataHolder.instance().getAppUserBase();
        Map<String, Object> query = KUtil.getDefaultQueryMap();
        try {
            query.put("user_seq", user.getUser_seq());
        } catch (Exception ex) {
            SharedPreferences prefr = getSharedPreferences("userInfo", MODE_PRIVATE);
            query.put("user_seq", prefr.getInt("user_seq", -1));
        }

        if (null != mCafeseq && !"".equals(mCafeseq)) {
            query.put("cafeseq", mCafeseq);
        }
        if (null != mCafekey && !"".equals(mCafekey)) {
            query.put("cafekey", mCafekey);
        }

        showSpinner("");
        getCafeService().detail(query).enqueue(new Callback<CafeDetail>() {
            @Override
            public void onResponse(Call<CafeDetail> call, Response<CafeDetail> response) {
                closeSpinner();
                LogUtils.err(TAG, response.toString());
                if (response.isSuccessful()) {
                    CafeDetail cafedetail = response.body();
                    if (cafedetail.isSuccess()) {
                        mCafe = cafedetail.getCafe();
                        mMyInfo = cafedetail.getMyInfo();
                        mCafeseq = mCafe.getCafeseq();
                        mCate = mCafe.getCategory();

                        if ("1".equals(mCafe.getIsjoin())) {
                            try {
                                isAdmin = mMyInfo.getIsAdmin();
                            } catch (Exception e) {
                                isAdmin = "N";
                            }
                        } else {
                            isAdmin = "N";
                        }

                        findViews();
                        setSpinner();
                        attachEvents();


                        getCafeNotice();
                        if (TextUtils.isEmpty(mType)) {
                            return;
                        } else if ("BOARD".equals(mType)) {
                            return;
                        } else if ("DAILY".equals(mType)) {
                            toggleBtn(mBtnResId[1]);
                            return;
                        } else if ("WEEKLY".equals(mType)) {
                            toggleBtn(mBtnResId[2]);
                            return;
                        } else if ("MONTHLY".equals(mType)) {
                            toggleBtn(mBtnResId[3]);
                            return;
                        }
                    }
                }
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

    private void getCafeNotice() {
        showSpinner("");
        Map<String, Object> query = KUtil.getDefaultQueryMap();
        query.put("cafeseq", mCafeseq);
        Call<PageResponseBase<Notice>> call = getCafeService().notice(query);

        call.enqueue(new Callback<PageResponseBase<Notice>>() {
            @Override
            public void onResponse(Call<PageResponseBase<Notice>> call, Response<PageResponseBase<Notice>> response) {
                closeSpinner();
                LogUtils.err(TAG, response.toString());
                if (response.isSuccessful()) {
                    PageResponseBase<Notice> noticelist = response.body();
                    //LogUtils.err(TAG, "profile=" + profile.string());
                    if (noticelist.isSuccess()) {
                        mNotifyResult = noticelist;

                        if (null != mNotifyResult && CollectionUtils.isNotEmpty(mNotifyResult.getResult())) {
                            final Notice first = mNotifyResult.getResult().get(0);
                            txt_notice_date.setText("[공지] " + first.getRegdate());
                            txt_notice.setText(first.getContents());
                            ll_notice_detail.setOnClickListener(v -> {
                                // 공지사항 액티비티로 이동
                                Bundle bu = new Bundle();
                                bu.putString("cafeseq", mCafeseq);
                                callActivity(CafeNoticeActivity.class, bu, false);
                            });
                        } else {
                            ll_notice_detail.setVisibility(View.GONE);
                            txt_notice_date.setVisibility(View.GONE);
                            txt_notice.setVisibility(View.GONE);
                        }
                    }
                }
                showCafeConfig();
            }

            @Override
            public void onFailure(Call<PageResponseBase<Notice>> call, Throwable t) {
                closeSpinner();
                LogUtils.err(TAG, t);
                toast(R.string.warn_server_not_smooth);
            }
        });
    }

    private void join() {
        showSpinner("");
        AppUserBase user = DataHolder.instance().getAppUserBase();
        Map<String, Object> query = KUtil.getDefaultQueryMap();
        query.put("user_seq", user.getUser_seq());
        query.put("cafeseq", mCafeseq);

        Call<ResponseBase> call = getCafeService().join(query);

        call.enqueue(new Callback<ResponseBase>() {
            @Override
            public void onResponse(Call<ResponseBase> call, Response<ResponseBase> response) {
                LogUtils.err(TAG, response.raw().toString());
                if (response.isSuccessful()) {
                    ResponseBase base = response.body();
                    if (base.isSuccess()) {
                        toast(R.string.warn_cafe_success_join);
                        //finish();

                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();

                        /*Bundle bu = new Bundle();
                        bu.putSerializable("_CAFEMAIN_ACTIVITY_", "GOCAFEMAIN");
                        callActivity2(MainActivity.class, bu,true);*/
                    } else {
                        toast(R.string.warn_cafe_fail_join);
                    }
                } else {
                    toast(R.string.warn_cafe_fail_join);
                }

                closeSpinner();
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
    public void performClick(View view) {
        int id = view.getId();
        if (id == mSelectedBtnId) {
            return;
        }
        toggleBtn(id);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in_full, R.anim.slide_out_right);
    }

    private Call<CafeRankingList> getCafeRankingList() {
        showSpinner("");
        AppUserBase user = DataHolder.instance().getAppUserBase();
        Map<String, Object> query = KUtil.getDefaultQueryMap();
        query.put("user_seq", user.getUser_seq());
        query.put("cafeseq", mCafeseq);
        query.put("period", mBtnType);
        query = PageResponseBase.requestParam(query, mRankResult);
        return getCafeService().rankingIndividual(query);
    }

    private Call<CafeRankingList> getCafeRankingListWalk() {
        showSpinner("");
        AppUserBase user = DataHolder.instance().getAppUserBase();
        Map<String, Object> query = KUtil.getDefaultQueryMap();
        query = PageResponseBase.requestParam(query, mRankResult);
        query.put("user_seq", user.getUser_seq());
        query.put("cafeseq", mCafeseq);
        query.put("period", mBtnType);

        return getCafeService().rankingIndividualWalk(query);
    }

    private Call<CafeRankingList> getCafeRankingByCategortyList() {
        showSpinner("");
        AppUserBase user = DataHolder.instance().getAppUserBase();
        Map<String, Object> query = KUtil.getDefaultQueryMap();
        query = PageResponseBase.requestParam(query, mRankResult);
        query.put("cafeseq", mCafeseq);
        query.put("period", mBtnType);

        // 임시 하드코딩 (확인필요)
        query.put("cateseq", "999");

        return getCafeService().rankingByCategory(query);
    }

    private Call<CafeRankingList> getCafeRankingByCategortyListWalk() {
        showSpinner("");
        AppUserBase user = DataHolder.instance().getAppUserBase();
        Map<String, Object> query = KUtil.getDefaultQueryMap();
        query = PageResponseBase.requestParam(query, mRankResult);
        query.put("cafeseq", mCafeseq);
        query.put("period", mBtnType);

        // 임시 하드코딩 (확인필요)
        query.put("cateseq", "999");

        return getCafeService().rankingByCategoryWalk(query);
    }

    private Call<CafeRankingList> getCafeRankingByCategoryIndividualList() {
        showSpinner("");
        AppUserBase user = DataHolder.instance().getAppUserBase();
        Map<String, Object> query = KUtil.getDefaultQueryMap();
        query = PageResponseBase.requestParam(query, mRankResult);
        query.put("user_seq", user.getUser_seq());
        query.put("cafeseq", mCafeseq);
        query.put("cateseq", mSelectedCategorySpinnerItem);
        query.put("period", mBtnType);

        return getCafeService().rankingIndividualByCategory(query);
    }

    private Call<CafeRankingList> getCafeRankingByCategoryIndividualListWalk() {
        showSpinner("");
        AppUserBase user = DataHolder.instance().getAppUserBase();
        Map<String, Object> query = KUtil.getDefaultQueryMap();
        query = PageResponseBase.requestParam(query, mRankResult);
        query.put("user_seq", user.getUser_seq());
        query.put("cafeseq", mCafeseq);
        query.put("cateseq", mSelectedCategorySpinnerItem);
        query.put("period", mBtnType);

        return getCafeService().rankingIndividualByCategoryWalk(query);
    }

    private void getCafeBBS() {
        notice_linearlayout.removeAllViews();

        showSpinner("");
        AppUserBase user = DataHolder.instance().getAppUserBase();
        Map<String, Object> query = KUtil.getDefaultQueryMap();
        PageResponseBase.requestParam(query, mBbsResult);

        try {
            query.put("user_seq", user.getUser_seq());
        } catch (Exception ex) {
            SharedPreferences prefr = getSharedPreferences("userInfo", MODE_PRIVATE);
            query.put("user_seq", prefr.getInt("user_seq", -1));
        }
        query.put("cafeseq", mCafeseq);

        getCafeService().bbs(query).enqueue(new Callback<PageResponseBase<CafeNotice>>() {
            @Override
            public void onResponse(Call<PageResponseBase<CafeNotice>> call, Response<PageResponseBase<CafeNotice>> response) {
                closeSpinner();
                LogUtils.err(TAG, response.toString());
                if (response.isSuccessful()) {
                    PageResponseBase<CafeNotice> list = null;
                    if (!"1".equals(mCafe.getIsjoin())) return;

                    list = response.body();

                    if (null == list || CollectionUtils.isEmpty(list.getResult())) {
                        bbs_count.setText("게시글 : 0개");
                        bbs_count.setTypeface(bbs_count.getTypeface(), Typeface.BOLD);
                    } else {
                        mBbsResult = PageResponseBase.merge(mBbsResult, list, false);

                        bbs_count.setText("게시글 : " + mBbsResult.getResult().size() + "개");
                        bbs_count.setTypeface(bbs_count.getTypeface(), Typeface.BOLD);

                        for (CafeNotice notice : mBbsResult.getResult()) {
                            RowCafeBbs n_layout = new RowCafeBbs(getApplicationContext());
                            notice_linearlayout.addView(n_layout);

                            final String bbsseq = notice.getBbsseq();
                            final String bbscontent = notice.getContent();
                            final String creatorseq = notice.getUser_seq();

                            int info_id = Integer.parseInt("1" + notice.getBbsseq());
                            int content_id = Integer.parseInt("2" + notice.getBbsseq());
                            int commcount_id = Integer.parseInt("3" + notice.getBbsseq());
                            int replycomm_id = Integer.parseInt("4" + notice.getBbsseq());
                            int bbs_config_id = Integer.parseInt("5" + notice.getBbsseq());
                            int img_notice_id = Integer.parseInt("10" + notice.getBbsseq());
                            int name_id = Integer.parseInt("11" + notice.getBbsseq());

                            CircleImageView img_profile_notice = notice_linearlayout.findViewById(R.id.img_profile);
                            img_profile_notice.setId(img_notice_id);
                            String charUrl1 = KUtil.getSubCharacterImgUrl(notice.getCharImageFile());
                            Glide.with(getApplicationContext())
                                    .applyDefaultRequestOptions(KUtil.getGlideCacheOption())
                                    .load(charUrl1)
                                    .thumbnail(.5f)
                                    .into(img_profile_notice);

                            // 이미지 파일 작업 처리
                            String notice_info = notice.getRegdate();
                            KoswEditText et_info = notice_linearlayout.findViewById(R.id.notice_info);
                            et_info.setId(info_id);
                            et_info.setText(notice_info);

                            String notice_name = notice.getNickname();
                            KoswEditText et_notice_name = notice_linearlayout.findViewById(R.id.notice_name);
                            et_notice_name.setId(name_id);
                            et_notice_name.setTypeface(et_notice_name.getTypeface(), Typeface.BOLD);
                            et_notice_name.setText(notice_name);

                            KoswEditText et_context = notice_linearlayout.findViewById(R.id.notice_context);
                            et_context.setId(content_id);
                            et_context.setText(bbscontent);

                            KoswEditText et_comm_count = notice_linearlayout.findViewById(R.id.notice_comment_count);
                            et_comm_count.setId(commcount_id);

                            ImageView bbs_config = notice_linearlayout.findViewById(R.id.bbs_config);
                            bbs_config.setId(bbs_config_id);

                            if ("1".equals(mCafe.getIsjoin())) {
                                bbs_config.setVisibility(View.VISIBLE);
                                bbs_config.setOnClickListener(v -> {
                                    // 게시글 상세
                                    Intent intent = new Intent(getApplicationContext(), BbsDetailActivity.class);
                                    intent.putExtra("bbsseq", bbsseq);
                                    intent.putExtra("content", bbscontent);
                                    intent.putExtra("isAdmin", isAdmin);
                                    bbsType = "BBS";

                                    if (creatorseq.equals(String.valueOf(user.getUser_seq()))) {
                                        intent.putExtra("isCreator", true);
                                    } else {
                                        intent.putExtra("isCreator", false);
                                    }

                                    startActivityForResult(intent, mBbsCreateResultCode);

                                });
                            }


                            // 댓글달기 이벤트

                            KoswEditText et_notice_comment = notice_linearlayout.findViewById(R.id.notice_comment);
                            et_notice_comment.setId(replycomm_id);
                            if ("1".equals(mCafe.getIsjoin())) {
                                et_notice_comment.setOnClickListener(v -> {
                                    bbsType = "COMMENT";
                                    offsetY = sv.getScrollY();

                                    et_notice_comment.getTranslationY();
                                    Intent intent = new Intent(getApplicationContext(), BbsPostActivity.class);
                                    intent.putExtra("bbsseq", bbsseq);
                                    intent.putExtra("postType", "COMMENT");

                                    startActivityForResult(intent, mBbsCreateResultCode);
                                });
                            } else {
                                et_notice_comment.setVisibility(View.GONE);
                            }

                            if (null != notice.getComments() && notice.getComments().size() > 0) {
                                et_comm_count.setText(notice.getComments().size() + "");

                                for (int b_idx = 0; b_idx < notice.getComments().size(); b_idx++) {
                                    RowCafeBbsComment b_layout = new RowCafeBbsComment(getApplicationContext());
                                    notice_linearlayout.addView(b_layout);

                                    int comm_info_id = Integer.parseInt("6" + notice.getBbsseq() + notice.getComments().get(b_idx).getCommentseq());
                                    int comm_content_id = Integer.parseInt("7" + notice.getBbsseq() + notice.getComments().get(b_idx).getCommentseq());
                                    int comm_date_id = Integer.parseInt("8" + notice.getBbsseq() + notice.getComments().get(b_idx).getCommentseq());
                                    int comm_profile_id = Integer.parseInt("9" + notice.getBbsseq() + notice.getComments().get(b_idx).getCommentseq());

                                    KoswEditText et_comm_info = notice_linearlayout.findViewById(R.id.comment_context_info);
                                    et_comm_info.setId(comm_info_id);
                                    et_comm_info.setText(notice.getComments().get(b_idx).getNickname());

                                    KoswEditText et_comm_context = notice_linearlayout.findViewById(R.id.comment_context);
                                    et_comm_context.setId(comm_content_id);
                                    et_comm_context.setText(notice.getComments().get(b_idx).getContent());

                                    KoswEditText et_comm_date = notice_linearlayout.findViewById(R.id.comment_date);
                                    et_comm_date.setId(comm_date_id);
                                    et_comm_date.setText(notice.getComments().get(b_idx).getRegdate());

                                    CircleImageView img_profile = notice_linearlayout.findViewById(R.id.img_profile);
                                    img_profile.setId(comm_profile_id);
                                    String charUrl = KUtil.getSubCharacterImgUrl(notice.getComments().get(b_idx).getCharImageFile());
                                    Glide.with(getApplicationContext())
                                            .applyDefaultRequestOptions(KUtil.getGlideCacheOption())
                                            .load(charUrl).thumbnail(.5f).into(img_profile);
                                }
                            } else {
                                et_comm_count.setText("0");
                            }

                            RowDotSeparator d_layout = new RowDotSeparator(getApplicationContext());
                            LinearLayout d_con = findViewById(R.id.notice_linearlayout);
                            d_con.addView(d_layout);


                        }

                        // 댓글 작성 후, 댓글 단 위치로 이동
                        if ("COMMENT".equals(bbsType) && offsetY > 0) {
                            ViewTreeObserver vto = sv.getViewTreeObserver();
                            vto.addOnGlobalLayoutListener(() -> sv.scrollTo(0, offsetY));
                        } else {
                            ViewTreeObserver vto = sv.getViewTreeObserver();
                            vto.addOnGlobalLayoutListener(() -> sv.scrollTo(0, 0));
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<PageResponseBase<CafeNotice>> call, Throwable t) {
                closeSpinner();
                LogUtils.err(TAG, t);
                toast(R.string.warn_server_not_smooth);
            }
        });

    }

    private void toggleBtn(int clickId) {
        Log.d("TTTTTTTTTTTTTTTTTTTTT", "CAFE " + clickId);
        for (int rs : mBtnResId) {
            TextView tv = getView(rs);
            if (rs == clickId) {
                mSelectedBtnId = rs;
                tv.setTextColor(getResources().getColor(R.color.colorWhite));
                tv.setBackgroundColor(getResources().getColor(R.color.color_1a1a1a));
            } else {
                tv.setTextColor(getResources().getColor(R.color.color_545454));
                tv.setBackgroundColor(getResources().getColor(R.color.color_cccccc));
            }
        }
        if (mSelectedBtnId == R.id.btn_daily) mBtnType = "D";
        else if (mSelectedBtnId == R.id.btn_weekly) mBtnType = "W";
        else if (mSelectedBtnId == R.id.btn_monthly) mBtnType = "M";
        else if (mSelectedBtnId == R.id.btn_notice) mBtnType = "N";

        setSpinnerCategory();
        if ("N".equals(mBtnType)) {
            ll_notice.setVisibility(View.VISIBLE);
            ll_recycler.setVisibility(View.GONE);
            spinner.setVisibility(View.GONE);
        } else {
            ll_notice.setVisibility(View.GONE);
            ll_recycler.setVisibility(View.VISIBLE);
            spinner.setVisibility(View.VISIBLE);
        }

        myInfinite.performFirstLoad();
    }

    private void showCafeConfig() {
        if ("Y".equals(isAdmin)) {
            btn_config.setVisibility(View.VISIBLE);
        } else {
            //btn_config.setVisibility(View.GONE);
            btn_config.setVisibility(View.VISIBLE);
        }
    }

    private void shareCafe() {
        String subject = "계단왕 '" + mCafe.getCafename() + "' 카페로 초대합니다.";

        String text = "계단왕 '" + mCafe.getCafename() + "' 카페로 초대합니다.\n\n저희 계단왕 '" + mCafe.getCafename() + "' 카페에 가입해 주세요.\n"
                + "계단왕 앱 메뉴의 카페 페이지에서 아래의 키값으로 바로 가입할 수 있습니다. (비공개 카페는 키값을 통해서만 카페 가입이 가능합니다.)\n\n"
                + "카페키값 : " + mCafe.getCafekey() + "\n\n"
                + "계단왕은 그룹 멤버들과 함께 운동하며 이야기를 나누는 공간입니다. 아이폰, 안드로이드에서 무료로 다운받아 사용해 보세요.\n\n"
                + Env.Url.URL_SHARE.url();

        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        //intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        Intent chooser = Intent.createChooser(intent, "공유할 앱을 선택하세요.");
        startActivity(chooser);
    }

    public class Category {
        private String id;
        private String name;

        public Category(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Category category = (Category) o;
            return Objects.equals(id, category.id) &&
                    Objects.equals(name, category.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name);
        }
    }

    public class SubCategory {
        private String id;
        private String name;

        public SubCategory(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Category category = (Category) o;
            return Objects.equals(id, category.id) &&
                    Objects.equals(name, category.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name);
        }
    }

    private class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.RankingRowHolder> {
        private Context mContext;
        private List<RankInCafe> mItems;
        private LayoutInflater mInflater;
        private ItemClickListener mItemClickListener;

        RankingAdapter(Context context, List<RankInCafe> list) {
            mContext = context;
            mItems = list;
            mInflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public RankingRowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.row_ranking_my, parent, false);
            return new RankingRowHolder(view, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull RankingRowHolder holder, int position) {
            String join = mCafe.getIsjoin();

            RankInCafe item = mItems.get(position);

            int rank = item.getRank();
            String nickname = item.getNickname();
            String catename = item.getCatename();
            String act_amt = item.getAct_amt();

            // 미가입카페인 경우,
            if (!"1".equals(join)) {
                if (null != nickname) nickname = nickname.substring(0, 1) + "****";
                if (null != catename) catename = "*****";
            }

            holder.ranking.setText(String.valueOf(rank));

            // 분류내 랭킹일 경우, 닉네임 부분에 부서 표시
            if (!"3".equals(mSelectedSpinnerItem) && !"4".equals(mSelectedSpinnerItem)) {
                holder.nickname.setText(nickname);
            } else {
                holder.nickname.setText(catename);
            }

            // 분류내 랭킹일 경우, 분류 부분 미표기해야함
            if (!"3".equals(mSelectedSpinnerItem) && !"4".equals(mSelectedSpinnerItem)) {
                holder.departName.setText(catename);
            } else {
                holder.departName.setText("");
            }

            if ("1".equals(mSelectedSpinnerItem) || "3".equals(mSelectedSpinnerItem) || "5".equals(mSelectedSpinnerItem)) {
                holder.recordAmount.setText(act_amt + " F");
                tv_amount.setText("층수");
            } else {
                holder.recordAmount.setText(act_amt + " 걸음");
                tv_amount.setText("걸음수");
            }

            if ("1".equals(mSelectedSpinnerItem) || "2".equals(mSelectedSpinnerItem)) {
                tv_ranking.setText("랭킹");
                tv_name.setText("닉네임");
                tv_depart.setText("분류");

            } else if ("3".equals(mSelectedSpinnerItem) || "4".equals(mSelectedSpinnerItem)) {
                tv_ranking.setText("랭킹");
                tv_name.setText("분류");
                tv_depart.setText("");
            } else if ("5".equals(mSelectedSpinnerItem) || "6".equals(mSelectedSpinnerItem)) {
                tv_ranking.setText("랭킹");
                tv_name.setText("닉네임");
                tv_depart.setText("분류");
            }


            if (position == 0 && null != item.getIsmine() && "Y".equals(item.getIsmine())) {
                setTextBackground(holder);
            }
        }

        private void setTextBackground(RankingRowHolder holder) {
            int color;
            holder.itemView.setBackgroundResource(R.color.color_35c2ef);
            color = getResources().getColor(R.color.colorWhite);
            holder.ranking.setTextColor(color);
            holder.nickname.setTextColor(color);
            holder.departName.setTextColor(color);
            holder.recordAmount.setTextColor(color);
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        public void clear() {
            this.mItems.clear();
            notifyDataSetChanged();
        }

       /* @Override
        public int getItemViewType(int position) {
            if(mItems.get(position).isMe() && mItems.get(position).getRankingToInt() > 4){
                return 0;
            }
            return mItems.get(position).getRankingToInt();
        }*/

        class RankingRowHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView ranking;
            TextView nickname;

            TextView departName;
            TextView recordAmount;

            RankingRowHolder(View view, int viewType) {
                super(view);
                pickupViews(viewType);
            }

            /**
             * @param type 0:me, ranking 1,2,3 and else
             */
            private void pickupViews(int type) {
                ranking = itemView.findViewById(R.id.txt_ranking);
                nickname = itemView.findViewById(R.id.txt_name);
                departName = itemView.findViewById(R.id.txt_depart);
                recordAmount = itemView.findViewById(R.id.txt_amount);
            }

            @Override
            public void onClick(View view) {
                //mItemClickListener.onItemClick(view, getAdapterPosition());
            }
        }
    }

    /**
     *
     */
    @Override
    public void performLoadTask(int firstVisibleItemPosition) {
        final boolean forNotify = mBtnType.equalsIgnoreCase("N");
        if (firstVisibleItemPosition == 0) {
            if (forNotify) {
                mBbsResult = null;
            } else {
                mRankResult = null;
                if (null != mAdapter)
                    mAdapter.clear();
            }
        }

        if (Arrays.asList("D", "W", "M").contains(mBtnType)) {
            Call<CafeRankingList> apiCall = null;
            switch (mSelectedSpinnerItem) {
                case "1":
                    apiCall = getCafeRankingList();
                    break;
                case "3":
                    apiCall = getCafeRankingByCategortyList();
                    break;
                case "5":
                    apiCall = getCafeRankingByCategoryIndividualList();
                    break;
                case "2":
                    apiCall = getCafeRankingListWalk();
                    break;
                case "4":
                    apiCall = getCafeRankingByCategortyListWalk();
                    break;
                case "6":
                    apiCall = getCafeRankingByCategoryIndividualListWalk();
                    break;
            }

            if (apiCall == null) return;
            showSpinner("");
            apiCall.enqueue(new Callback<CafeRankingList>() {
                @Override
                public void onResponse(Call<CafeRankingList> call, Response<CafeRankingList> response) {
                    LogUtils.err(TAG, response.toString());
                    if (response.isSuccessful()) {
                        final CafeRankingList list = response.body();
                        if (null == list || CollectionUtils.isEmpty(list.getResult())) {
                            if (mRankResult == null) toast(R.string.warn_cafe_rank_not_exists);
                        } else {
                            mRankResult = PageResponseBase.merge(mRankResult, list, false);
                            mAdapter = new RankingAdapter(getApplicationContext(), mRankResult.getResultWithMine());
                            mRecyclerView.setAdapter(mAdapter);
                        }
                    }
                    closeSpinner();
                }

                @Override
                public void onFailure(Call<CafeRankingList> call, Throwable t) {
                    closeSpinner();
                    LogUtils.err(TAG, t);
                    toast(R.string.warn_server_not_smooth);
                }
            });
        } else if (forNotify) {
            getCafeBBS();
        }
    }
}

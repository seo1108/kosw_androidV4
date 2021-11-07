package kr.co.photointerior.kosw.ui.fragment;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kr.co.photointerior.kosw.R;
import kr.co.photointerior.kosw.listener.FragmentClickListener;
import kr.co.photointerior.kosw.listener.ItemClickListener;
import kr.co.photointerior.kosw.rest.model.AppUserBase;
import kr.co.photointerior.kosw.rest.model.DataHolder;
import kr.co.photointerior.kosw.rest.model.GGRRow;
import kr.co.photointerior.kosw.rest.model.PageResponseBase;
import kr.co.photointerior.kosw.ui.BaseActivity;
import kr.co.photointerior.kosw.utils.KUtil;
import kr.co.photointerior.kosw.utils.LogUtils;
import kr.co.photointerior.kosw.utils.StringUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 개인랭킹
 */
public class GGRFragment extends BaseFragment {
    private final String TAG = LogUtils.makeLogTag(GGRFragment.class);

    private int[] mBtnResId = {
            R.id.btn1101, // Gold
            R.id.btn1104, // Walk
            R.id.btn1102, // Green
            R.id.btn1103 // Red
    };

    private int mSelectedBtnId;

    private RecyclerView mGGRRecyclerView;
    private GGRAdapter mGGRAdapter;

    private PageResponseBase<GGRRow> mRedList;
    private PageResponseBase<GGRRow> mGreenList;
    private PageResponseBase<GGRRow> mGoldList;
    private PageResponseBase<GGRRow> mWalkList;

    public static BaseFragment newInstance(BaseActivity context) {
        GGRFragment frag = new GGRFragment();
        frag.mActivity = context;
        return frag;
    }

    @Override
    protected void followingWorksAfterInflateView() {
        findViews();
        attachEvents();
    }

    @Override
    protected void findViews() {
        mActivity.toggleActionBarResources(false);
        mGGRRecyclerView = getView(R.id.rv1101);
        myInfinite = initMyInfinite(mGGRRecyclerView);
        toggleBtn(mBtnResId[0]);
    }

    @Override
    protected void attachEvents() {
        View.OnClickListener listener = new FragmentClickListener(this);
        for (int rs : mBtnResId) {
            getView(rs).setOnClickListener(listener);
        }

    }

    @Override
    public void performFragmentClick(View view) {
        int id = view.getId();
        if (id == mSelectedBtnId) {
            return;
        }
        toggleBtn(id);
    }


    /**
     * 결과 리스트 표시
     */
    private void createAdapter() {
        List<GGRRow> ggrList = new ArrayList<>();
        boolean isKOM = false;

        if (mSelectedBtnId == R.id.btn1101) {
            if (mGoldList != null && !mGoldList.getResult().isEmpty()) {
                ggrList = mGoldList.getResult();
            }
        }
        if (mSelectedBtnId == R.id.btn1104) {
            if (mWalkList != null && !mWalkList.getResult().isEmpty()) {
                ggrList = mWalkList.getResult();
            }
        }
        if (mSelectedBtnId == R.id.btn1102) {
            if (mGreenList != null && !mGreenList.getResult().isEmpty()) {
                ggrList = mGreenList.getResult();
            }
        }
        if (mSelectedBtnId == R.id.btn1103) {
            isKOM = true;
            if (mRedList != null && !mRedList.getResult().isEmpty()) {
                ggrList = mRedList.getResult();
            }
        }
        mGGRAdapter = new GGRAdapter(mActivity, ggrList, null, isKOM);
    }

    private class GGRAdapter extends RecyclerView.Adapter<GGRAdapter.GGRViewHolder> {
        private Context mContext;
        private List<GGRRow> mItems;
        private LayoutInflater mInflater;
        private ItemClickListener mItemClickListener;
        private Boolean isKOM = false;

        GGRAdapter(Context context, List<GGRRow> list, ItemClickListener listener, Boolean kom) {
            mContext = context;
            mItems = list;
            mItemClickListener = listener;
            mInflater = LayoutInflater.from(context);
            isKOM = kom;
        }

        @NonNull
        @Override
        public GGRViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            view = mInflater.inflate(R.layout.row_ggr_all, parent, false);

            return new GGRViewHolder(view, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull GGRViewHolder holder, int position) {
            GGRRow ggrRow = mItems.get(position);

            String actDate = ggrRow.getAct_date();
            holder.tv1101.setText(actDate.substring(0, 4) + "." + actDate.substring(4, 6) + "." + actDate.substring(6, 8));
            holder.tv1102.setText(ggrRow.getNickname());
            if (isKOM) {
                Double sec = Double.valueOf(ggrRow.getAct_sec()) / 10000.0;
                holder.tv1103.setText(StringUtil.format(sec, "#,##0.00") + "초/층");
            } else {
                if (mSelectedBtnId != R.id.btn1104) {
                    holder.tv1103.setText(StringUtil.format(Double.valueOf(ggrRow.getAct_amt()), "#,##0") + "F");
                } else {
                    holder.tv1103.setText(StringUtil.format(Double.valueOf(ggrRow.getAct_amt()), "#,##0") + "걸음");
                }
            }
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }


        class GGRViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView tv1101;
            TextView tv1102;
            TextView tv1103;

            GGRViewHolder(View view, int viewType) {
                super(view);
                pickupViews(viewType);
            }

            /**
             * @param type 0:me, ranking 1,2,3 and else
             */
            private void pickupViews(int type) {
                tv1101 = itemView.findViewById(R.id.tv1101);
                tv1102 = itemView.findViewById(R.id.tv1102);
                tv1103 = itemView.findViewById(R.id.tv1103);
            }

            @Override
            public void onClick(View view) {
                //mItemClickListener.onItemClick(view, getAdapterPosition());
            }
        }
    }

    private void toggleBtn(int clickId) {
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
            if (mSelectedBtnId == R.id.btn1103) {
                getTextView(R.id.tv2103).setText("시 간");
            } else {
                getTextView(R.id.tv2103).setText("층 수");
            }
        }

        createAdapter();
        mGGRRecyclerView.setAdapter(mGGRAdapter);
        if (mGGRAdapter.mItems.isEmpty()) {
            myInfinite.performFirstLoad();
        }
    }

    @Override
    public int getViewResourceId() {
        return R.layout.fragment_ggr;
    }

    private String getActionKind() {
        switch (mSelectedBtnId) {
            case R.id.btn1101:
                return "GOLD";
            case R.id.btn1104:
                return "WALK";
            case R.id.btn1102:
                return "GREEN";
            case R.id.btn1103:
                return "RED";
            default:
                break;
        }
        return "GOLD";
    }

    @Override
    public void performLoadTask(int firstVisibleItemPosition) {
        AppUserBase user = DataHolder.instance().getAppUserBase();
        final String actionKind = getActionKind();

        Map<String, Object> query = KUtil.getDefaultQueryMap();
        query.put("build_seq", user.getBuild_seq());
        query.put("cust_seq", user.getCust_seq());
        query.put("act_kind", getActionKind());
        switch (actionKind) {
            case "GOLD":
                query = PageResponseBase.requestParam(query, mGoldList);
                break;
            case "WALK":
                query = PageResponseBase.requestParam(query, mWalkList);
                break;
            case "GREEN":
                query = PageResponseBase.requestParam(query, mGreenList);
                break;
            case "RED":
                query = PageResponseBase.requestParam(query, mRedList);
                break;
        }

        showSpinner("");
        Call<PageResponseBase<GGRRow>> call = getCustomerService().getGGRHistory(query);
        call.enqueue(new Callback<PageResponseBase<GGRRow>>() {
            @Override
            public void onResponse(
                    Call<PageResponseBase<GGRRow>> call,
                    Response<PageResponseBase<GGRRow>> response
            ) {
                LogUtils.err(TAG, response.raw().toString());
                if (response.isSuccessful()) {
                    LogUtils.err(TAG, response.body().string());
                    PageResponseBase<GGRRow> ggrList = response.body();
                    if (ggrList.isSuccess()) {
                        switch (actionKind) {
                            case "GOLD":
                                mGoldList = PageResponseBase.merge(mGoldList, ggrList, false);
                                break;
                            case "WALK":
                                mWalkList = PageResponseBase.merge(mWalkList, ggrList, false);
                                break;
                            case "GREEN":
                                mGreenList = PageResponseBase.merge(mGreenList, ggrList, false);
                                break;
                            case "RED":
                                mRedList = PageResponseBase.merge(mRedList, ggrList, false);
                                break;
                        }
                    }
                    closeSpinner();
                    createAdapter();
                    myInfinite.wrapRefreshView(mGGRAdapter, firstVisibleItemPosition);
                }
            }

            @Override
            public void onFailure(Call<PageResponseBase<GGRRow>> call, Throwable t) {
                closeSpinner();
            }
        });
    }
}

package kr.co.photointerior.kosw.ui.fragment;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blankj.utilcode.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kr.co.photointerior.kosw.R;
import kr.co.photointerior.kosw.listener.FragmentClickListener;
import kr.co.photointerior.kosw.listener.ItemClickListener;
import kr.co.photointerior.kosw.rest.model.AppUserBase;
import kr.co.photointerior.kosw.rest.model.CityRank;
import kr.co.photointerior.kosw.rest.model.DataHolder;
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
public class CityRankFragment extends BaseFragment {
    private final String TAG = LogUtils.makeLogTag(CityRankFragment.class);

    private int[] mBtnResId = {
            /*R.id.btn1101,
            R.id.btn1102,
            R.id.btn1103*/
            R.id.btn1104,
            R.id.btn1105
    };

    private int mSelectedBtnId;

    private RecyclerView mGGRRecyclerView;
    private GGRAdapter mGGRAdapter;

    private PageResponseBase<CityRank> mStairRanks;
    private PageResponseBase<CityRank> mStepRanks;

    private TextView tv2103, tv2104;

    public static BaseFragment newInstance(BaseActivity context) {
        CityRankFragment frag = new CityRankFragment();
        frag.mActivity = context;
        return frag;
    }

    @Override
    protected void followingWorksAfterInflateView() {
        findViews();
        attachEvents();

        toggleBtn(mBtnResId[0]);
    }

    @Override
    protected void findViews() {
        mActivity.toggleActionBarResources(false);
        mGGRRecyclerView = getView(R.id.rv1101);
        myInfinite = initMyInfinite(mGGRRecyclerView);

        tv2103 = getView(R.id.tv2103);
        tv2104 = getView(R.id.tv2104);
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
    private void drawList(int firstVisibleItemPosition) {
        List<CityRank> ggrList = new ArrayList<>();
        Boolean isKOM = false;

/*
        if (mSelectedBtnId == R.id.btn1101) {
            if (mGGRList != null && mGGRList.getwList() != null) {
                ggrList = mGGRList.getwList();
            }
        }
        if (mSelectedBtnId == R.id.btn1102) {
            if (mGGRList != null && mGGRList.getaList() != null) {
                ggrList = mGGRList.getaList();
            }
        }
        if (mSelectedBtnId == R.id.btn1103) {
            isKOM = true;
            if (mGGRList != null && mGGRList.getkList() != null) {
                ggrList = mGGRList.getkList();
            }
        }
*/
        if (mSelectedBtnId == R.id.btn1104) {
            tv2103.setText("층 수");
            tv2104.setVisibility(View.VISIBLE);
            if (mStairRanks != null && CollectionUtils.isNotEmpty(mStairRanks.getResult())) {
                ggrList = mStairRanks.getResult();
            }
        }
        if (mSelectedBtnId == R.id.btn1105) {
            tv2103.setText("걸음수");
            tv2104.setVisibility(View.GONE);
            if (mStepRanks != null && CollectionUtils.isNotEmpty(mStepRanks.getResult())) {
                ggrList = mStepRanks.getResult();
            }
        }
        mGGRAdapter = new CityRankFragment.GGRAdapter(mActivity, ggrList, null, isKOM);
        myInfinite.wrapRefreshView(mGGRAdapter, firstVisibleItemPosition);
        if (CollectionUtils.isEmpty(mGGRAdapter.mItems)) {
            myInfinite.performFirstLoad();
        }
    }


    private class GGRAdapter extends RecyclerView.Adapter<GGRAdapter.GGRViewHolder> {
        private Context mContext;
        private List<CityRank> mItems;
        private LayoutInflater mInflater;
        private ItemClickListener mItemClickListener;
        private Boolean isKOM = false;

        GGRAdapter(Context context, List<CityRank> list, ItemClickListener listener, Boolean kom) {
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
            view = mInflater.inflate(R.layout.row_city_rank, parent, false);

            return new GGRViewHolder(view, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull GGRViewHolder holder, int position) {
            CityRank ggrRow = mItems.get(position);

            String actDate = ggrRow.getAct_date();
            holder.tv1101.setText(actDate.substring(0, 4) + "." + actDate.substring(4, 6) + "." + actDate.substring(6, 8));
            holder.tv1102.setText(ggrRow.getNickname());
            if (mSelectedBtnId == R.id.btn1104) {
                holder.tv1103.setText(StringUtil.format(Double.valueOf(ggrRow.getAct_amt()), "#,##0") + "F");
            } else if (mSelectedBtnId == R.id.btn1105) {
                holder.tv1103.setText(StringUtil.format(Double.valueOf(ggrRow.getAct_amt()), "#,##0") + "걸음");
            }

            if (mSelectedBtnId == R.id.btn1103) {
                holder.tv1104.setText(ggrRow.getCity());
            } else if (mSelectedBtnId == R.id.btn1105) {
                holder.tv1104.setVisibility(View.GONE);
            } else {
                holder.tv1104.setText(ggrRow.getCountry());
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
            TextView tv1104;

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
                tv1104 = itemView.findViewById(R.id.tv1104);
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
                getTextView(R.id.tv2104).setText("도시명");
            } else {
                getTextView(R.id.tv2104).setText("국가명");
            }
        }

        drawList(0);
    }

    @Override
    public int getViewResourceId() {
        return R.layout.fragment_city_rank;
    }

    @Override
    public void performLoadTask(int firstVisibleItemPosition) {
        showSpinner("");
        AppUserBase user = DataHolder.instance().getAppUserBase();
        Map<String, Object> query = KUtil.getDefaultQueryMap();
        query.put("user_seq", user.getUser_seq());
        if (mSelectedBtnId == mBtnResId[0]) {
            query.put("champ_kind", "K");
            query = PageResponseBase.requestParam(query, mStairRanks);
        } else {
            query.put("champ_kind", "WALK");
            query = PageResponseBase.requestParam(query, mStepRanks);
        }

        Call<PageResponseBase<CityRank>> call = getUserService().getCityRank(query);
        call.enqueue(new Callback<PageResponseBase<CityRank>>() {
            @Override
            public void onResponse(Call<PageResponseBase<CityRank>> call, Response<PageResponseBase<CityRank>> response) {
                closeSpinner();
                LogUtils.err(TAG, response.raw().toString());
                if (response.isSuccessful()) {
                    LogUtils.err(TAG, response.body().string());
                    PageResponseBase<CityRank> ggrList = response.body();
                    if (ggrList.isSuccess()) {
                        if (mSelectedBtnId == mBtnResId[0]) {
                            mStairRanks = PageResponseBase.merge(mStairRanks, ggrList, false);
                        } else {
                            mStepRanks = PageResponseBase.merge(mStepRanks, ggrList, false);
                        }
                        drawList(firstVisibleItemPosition);
                    }
                }
            }

            @Override
            public void onFailure(Call<PageResponseBase<CityRank>> call, Throwable t) {
                closeSpinner();
            }
        });
    }
}

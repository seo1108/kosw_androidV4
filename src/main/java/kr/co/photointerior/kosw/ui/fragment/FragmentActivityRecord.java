package kr.co.photointerior.kosw.ui.fragment;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kr.co.photointerior.kosw.R;
import kr.co.photointerior.kosw.rest.model.ActivityRecord;
import kr.co.photointerior.kosw.rest.model.Club;
import kr.co.photointerior.kosw.rest.model.GGRRow;
import kr.co.photointerior.kosw.rest.model.PageResponseBase;
import kr.co.photointerior.kosw.rest.model.Ranking;
import kr.co.photointerior.kosw.rest.model.Record;
import kr.co.photointerior.kosw.ui.BaseActivity;
import kr.co.photointerior.kosw.utils.KUtil;
import kr.co.photointerior.kosw.utils.LogUtils;
import kr.co.photointerior.kosw.utils.StringUtil;
import kr.co.photointerior.kosw.widget.RowActivityRecord;
import me.mvdw.recyclerviewmergeadapter.adapter.RecyclerViewMergeAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 회원의 계단 활동 기록
 */
public class FragmentActivityRecord extends BaseFragment {
    private final String TAG = LogUtils.makeLogTag(FragmentActivityRecord.class);
    private ActivityRecord mActivityRecord;
    private RecyclerView mRecyclerView, mRecordRecyclerView;

    private RecyclerViewMergeAdapter mergeAdapter;
    private RecordAdapter mAdapter;
    private DailyRecordAdapter mDailyAdapter;

    private PageResponseBase<Record> mDailyRecordList;
    private PageResponseBase<Club> mClubList;

    private boolean is_record_loaded = false;
    private boolean is_daily_loaded = false;

    public static BaseFragment newInstance(BaseActivity context) {
        FragmentActivityRecord frag = new FragmentActivityRecord();
        frag.mActivity = context;
        return frag;
    }

    @Override
    protected void followingWorksAfterInflateView() {
        findViews();
        attachEvents();
        //setInitialData();
        requestToServer();
    }

    @Override
    protected void findViews() {
        mActivity.toggleActionBarResources(false);
        mRecyclerView = getView(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));

        mRecordRecyclerView = getView(R.id.record_recycler_view);
        //myInfinite = initMyInfinite(mRecordRecyclerView);
        myInfinite = initMyInfinite(mRecyclerView);
    }

    @Override
    protected void attachEvents() {

    }

    /**
     * 활동기록 데이터 서버에서 획득 후 처리
     */
    @Override
    public void requestToServer() {
        Map<String, Object> query = KUtil.getDefaultQueryMap();
        Log.d("TTTTTTTTTTTTTTTTTT", query.get("token").toString());
        Log.d("TTTTTTTTTTTTTTTTTT", query.get("device").toString());
        Log.d("TTTTTTTTTTTTTTTTTT", query.get("buildCode").toString());
        Call<ActivityRecord> call = getUserService().getActivityRecords1(query);
        call.enqueue(new Callback<ActivityRecord>() {
            @Override
            public void onResponse(Call<ActivityRecord> call, Response<ActivityRecord> response) {
                LogUtils.err(TAG, response.raw().toString());
                if (response.isSuccessful()) {
                    ActivityRecord record = response.body();
                    if (record.isSuccess()) {
                        mActivityRecord = record;


                    } else {
                        toast(record.getResponseMessage());
                    }
                } else {
                    toast(R.string.warn_commu_to_server);
                }
                setInitialData();
                /*createAdapter();
                mRecyclerView.setAdapter(mAdapter);
                if (mAdapter.mList.isEmpty()) {
                    myInfinite.performFirstLoad();
                }*/
            }

            @Override
            public void onFailure(Call<ActivityRecord> call, Throwable t) {
                //toast(R.string.warn_commu_to_server);
                LogUtils.err(TAG, t);
            }
        });
    }

    @Override
    protected void setInitialData() {
        setActivityData();
        drawList();
    }

    /**
     * 화면상단 활동 기록 반영
     */
    private void setActivityData() {
        TextView period = getView(R.id.txt_activity_period);
        RowActivityRecord totalFloor = getView(R.id.row_record_floor);
        RowActivityRecord totalWalk = getView(R.id.row_record_walk);
        RowActivityRecord totalCalorie = getView(R.id.row_record_calorie);
        RowActivityRecord totalLife = getView(R.id.row_record_life);
        Record total = mActivityRecord.getTotalRecord();
        LogUtils.err(TAG, "total : " + total.string());
        period.setText(total.getStartDate().concat("~").concat(total.getEndDate()));
        totalFloor.setRecordAmount(StringUtil.format(total.getAmountToFloat(), "#,##0"));
        totalWalk.setRecordAmount(StringUtil.format(total.getWalkAmountToFloat(), "#,##0"));
        totalCalorie.setRecordAmount(KUtil.calcCalorie(total.getAmountToFloat(), total.getStairAmountToFloat()));
        totalLife.setRecordAmount(KUtil.calcLife(total.getAmountToFloat(), total.getStairAmountToFloat()));
    }

    private void drawList() {
        mergeAdapter = new RecyclerViewMergeAdapter();

        List<Club> clubList = mActivityRecord.getClubList();
        //List<Record> list = mActivityRecord.getDailyRecords();
        List<Record> list = new ArrayList<>();

        if (list != null) {
            list.add(0, mActivityRecord.getMaxAmount());
            list.add(0, mActivityRecord.getDailyAverage());

            if (mAdapter == null) {
                mAdapter = new RecordAdapter(mActivity, list, clubList);

/*
                if (mAdapter.mList.isEmpty()) {
                    myInfinite.performFirstLoad();
                }*/
            } else {
                mAdapter.notifyDataSetChanged();
            }
        }

        /*createAdapter();
        mRecyclerView.setAdapter(mAdapter);
        if (mAdapter.mList.isEmpty()) {
            myInfinite.performFirstLoad();
        }*/

/*
        if (!is_record_loaded) {
            mergeAdapter.addAdapter(mAdapter);
            mRecyclerView.setAdapter(mergeAdapter);
        }
        mergeAdapter.addAdapter(mDailyAdapter);
        //mergeAdapter.addAdapter(mDailyAdapter);
        mRecyclerView.setAdapter(mergeAdapter);

*/


        createAdapter();



        //mRecordRecyclerView.setAdapter(mDailyAdapter);
        //mRecyclerView.setAdapter(mDailyAdapter);



    }

    private void createAdapter() {
        List<Record> recordList = new ArrayList<>();
        //List<Club> clubList = mActivityRecord.getClubList();

        if (mDailyRecordList != null && !mDailyRecordList.getResult().isEmpty()) {

            recordList = mDailyRecordList.getResult();
        }
        /*if (mClubList != null && !mClubList.getResult().isEmpty()) {
            clubList = mClubList.getResult();
        }*/
        //List<Record> list = mActivityRecord.getDailyRecords();

        mDailyAdapter = new FragmentActivityRecord.DailyRecordAdapter(mActivity, recordList);

        if (mDailyAdapter.mList.isEmpty()) {
            myInfinite.performFirstLoad();
        }

        /*if (!is_record_loaded) {
            Log.d("HHHHHHHHHHH", "ADD RECORD ADAPTERQQQ");
            mergeAdapter.addAdapter(mAdapter);
            mergeAdapter.addAdapter(mDailyAdapter);
            //mergeAdapter.addAdapter(mDailyAdapter);
            mRecyclerView.setAdapter(mergeAdapter);
            is_record_loaded = true;
        }*/

    }

    class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.RowHolder> {
        private Context mContext;
        private List<Record> mList;
        private List<Club> mClubList;
        private LayoutInflater mInflater;

        RecordAdapter(Context context, List<Record> list, List<Club> clubList) {
            mContext = context;
            mInflater = LayoutInflater.from(context);
            mList = list;
            mClubList = clubList;
        }

        @NonNull
        @Override
        public RowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = null;
            if (viewType < 3) {
                view = mInflater.inflate(R.layout.row_activity_record_summary, parent, false);
            } else {
                view = mInflater.inflate(R.layout.row_activity_record_daily, parent, false);
            }
            if (viewType == 1) {
                view.setBackgroundResource(R.color.color_FFCCECFC);
            } else {
                view.setBackgroundResource(R.color.colorWhite);
            }
            RowHolder holder = new RowHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RowHolder holder, int position) {

            int clubCount = mClubList.size();
            holder.topLine.setVisibility(View.INVISIBLE);

            //클럽이 먼저 나오고 활동 기록이 나옴
            if (position < clubCount) {
                Club item = mClubList.get(position);
                holder.title.setText(Club.getClubName(item.getClub_kind()) + " " + item.getRegi_date());
                holder.floor.setText(StringUtil.format(item.getStair_amtToFloat(), "#,##0"));
                holder.calorie.setText(KUtil.calcCalorieDefault(item.getStair_amtToFloat()));
                holder.life.setText(KUtil.calcLifeDefault(item.getStair_amtToFloat()));

                holder.ll_walk.setVisibility(View.GONE);
                holder.iv_walk.setVisibility(View.GONE);
            } else {
                Record item = mList.get(position - clubCount);
                if (position == clubCount) {
                    holder.title.setText("매일평균");
                } else if (position == clubCount + 1) {
                    holder.title.setText("최고기록 (".concat(item.getDate("yyyy.MM.dd") + "/" + item.getWalkDate("yyyy.MM.dd")) + ")");
                } else {
                    holder.title.setText(item.getDate("yyyy.MM.dd"));
                }

                holder.floor.setText(StringUtil.format(item.getAmountToFloat(), "#,##0"));
                holder.walk.setText(StringUtil.format(item.getWalkAmountToFloat(), "#,##0"));
                holder.calorie.setText(KUtil.calcCalorie(item.getAmountToFloat(), item.getStairAmountToFloat()));
                holder.life.setText(KUtil.calcLife(item.getAmountToFloat(), item.getStairAmountToFloat()));
            }

        }

        @Override
        public int getItemViewType(int position) {
            if (position < mClubList.size()) {//클럽달성기록
                return 1;
            } else if (position - mClubList.size() == 0) {//평균기록
                return 2;
            } else if (position - mClubList.size() == 1) {//최고기록
                return 2;
            }
            return 3;
        }

        @Override
        public int getItemCount() {
            if (null == mList) {
                return mClubList.size();
            }
            else {
                return mList.size() + mClubList.size();
            }
        }

        class RowHolder extends RecyclerView.ViewHolder {
            private View topLine;
            private TextView title;
            private TextView floor;
            private TextView walk;
            private TextView calorie;
            private TextView life;
            private LinearLayout ll_walk;
            private ImageView iv_walk;

            RowHolder(View view) {
                super(view);
                pickupView();
            }

            void pickupView() {
                topLine = itemView.findViewById(R.id.top_line);
                title = itemView.findViewById(R.id.txt_title);
                floor = itemView.findViewById(R.id.amt_floor);
                walk = itemView.findViewById(R.id.amt_walk);
                calorie = itemView.findViewById(R.id.amt_calorie);
                life = itemView.findViewById(R.id.amt_health);
                ll_walk = itemView.findViewById(R.id.ll_walk);
                iv_walk = itemView.findViewById(R.id.icon_walk);
            }
        }
    }



    class DailyRecordAdapter extends RecyclerView.Adapter<DailyRecordAdapter.RowHolder> {
        private Context mContext;
        private List<Record> mList;
        private LayoutInflater mInflater;

        DailyRecordAdapter(Context context, List<Record> list) {
            mContext = context;
            mInflater = LayoutInflater.from(context);
            mList = list;
        }

        @NonNull
        @Override
        public RowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.row_activity_record_daily, parent, false);
            view.setBackgroundResource(R.color.colorWhite);
            RowHolder holder = new RowHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RowHolder holder, int position) {

            int count = mList.size();
            holder.topLine.setVisibility(View.INVISIBLE);


            Record item = mList.get(position);
            holder.title.setText(item.getDate("yyyy.MM.dd"));

            holder.floor.setText(StringUtil.format(item.getAmountToFloat(), "#,##0"));
            holder.walk.setText(StringUtil.format(item.getWalkAmountToFloat(), "#,##0"));
            holder.calorie.setText(KUtil.calcCalorie(item.getAmountToFloat(), item.getStairAmountToFloat()));
            holder.life.setText(KUtil.calcLife(item.getAmountToFloat(), item.getStairAmountToFloat()));
        }

        /*@Override
        public int getItemViewType(int position) {
            if (position < mClubList.size()) {//클럽달성기록
                return 1;
            } else if (position - mClubList.size() == 0) {//평균기록
                return 2;
            } else if (position - mClubList.size() == 1) {//최고기록
                return 2;
            }
            return 3;
        }
*/
        @Override
        public int getItemCount() {
            return mList.size();
        }

        class RowHolder extends RecyclerView.ViewHolder {
            private View topLine;
            private TextView title;
            private TextView floor;
            private TextView walk;
            private TextView calorie;
            private TextView life;
            private LinearLayout ll_walk;
            private ImageView iv_walk;

            RowHolder(View view) {
                super(view);
                pickupView();
            }

            void pickupView() {
                topLine = itemView.findViewById(R.id.top_line);
                title = itemView.findViewById(R.id.txt_title);
                floor = itemView.findViewById(R.id.amt_floor);
                walk = itemView.findViewById(R.id.amt_walk);
                calorie = itemView.findViewById(R.id.amt_calorie);
                life = itemView.findViewById(R.id.amt_health);
                ll_walk = itemView.findViewById(R.id.ll_walk);
                iv_walk = itemView.findViewById(R.id.icon_walk);
            }
        }
    }





    @Override
    public int getViewResourceId() {
        return R.layout.fragment_activity_record;
    }


    @Override
    public void performLoadTask(int firstVisibleItemPosition) {
        showSpinner("");
        Map<String, Object> query = KUtil.getDefaultQueryMap();
        Log.d("HHHHHHHHHHHHHHHHHHHH", query.get("token").toString());
        Log.d("HHHHHHHHHHHHHHHHHHHH", query.get("device").toString());
        Log.d("HHHHHHHHHHHHHHHHHHHH", query.get("buildCode").toString());


        query = PageResponseBase.requestParam(query, mDailyRecordList);

        Call<PageResponseBase<Record>> call = getUserService().getActivityRecords2(query);
        call.enqueue(new Callback<PageResponseBase<Record>>() {
            @Override
            public void onResponse(Call<PageResponseBase<Record>> call, Response<PageResponseBase<Record>> response) {
                LogUtils.err(TAG, response.raw().toString());
                if (response.isSuccessful()) {
                    PageResponseBase<Record> record = response.body();
                    if (record.isSuccess()) {

                        //setInitialData();

                        mDailyRecordList = PageResponseBase.merge(mDailyRecordList, record, false);

                        /*if (!is_record_loaded) {
                            Log.d("HHHHHHHHHHH", "ADD RECORD ADAPTERQQQ");
                            mergeAdapter.addAdapter(mAdapter);
                            mergeAdapter.addAdapter(mDailyAdapter);
                            //mergeAdapter.addAdapter(mDailyAdapter);
                            mRecyclerView.setAdapter(mergeAdapter);
                            is_record_loaded = true;
                        }*/

                        /*if (!is_daily_loaded)
                        {
                            mergeAdapter.addAdapter(mDailyAdapter);
                            mRecyclerView.setAdapter(mergeAdapter);

                            is_daily_loaded = true;
                        }*/


                        createAdapter();
                        if (!is_record_loaded) {
                            mergeAdapter.addAdapter(mAdapter);
                            mergeAdapter.addAdapter(mDailyAdapter);
                            mRecyclerView.setAdapter(mergeAdapter);
                            is_record_loaded = true;
                        }


                        myInfinite.wrapRefreshView(mergeAdapter, firstVisibleItemPosition);
                        //myInfinite.wrapRefreshView(mergeAdapter, firstVisibleItemPosition);
                    } else {
                        toast(record.getResponseMessage());
                    }
                } else {
                    toast(R.string.warn_commu_to_server);
                }

                //createAdapter();
                //myInfinite.wrapRefreshView(mDailyAdapter, firstVisibleItemPosition);

                closeSpinner();
            }

            @Override
            public void onFailure(Call<PageResponseBase<Record>> call, Throwable t) {
                //toast(R.string.warn_commu_to_server);
                LogUtils.err(TAG, t);

                closeSpinner();
            }
        });
     }
}

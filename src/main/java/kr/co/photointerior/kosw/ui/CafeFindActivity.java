package kr.co.photointerior.kosw.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blankj.utilcode.util.CollectionUtils;

import java.util.List;
import java.util.Map;

import kr.co.photointerior.kosw.R;
import kr.co.photointerior.kosw.rest.model.Cafe;
import kr.co.photointerior.kosw.rest.model.PageResponseBase;
import kr.co.photointerior.kosw.utils.KUtil;
import kr.co.photointerior.kosw.utils.LogUtils;
import kr.co.photointerior.kosw.widget.KoswEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CafeFindActivity extends BaseActivity {
    private String TAG = LogUtils.makeLogTag(CafeFindActivity.class);
    private PageResponseBase<Cafe> cafeResults;
    private String strQuery = "";
    private CafeAdapter mAdapter;
    private KoswEditText input_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cafe_find);

        findViews();
        attachEvents();
        myInfinite.performFirstLoad();
    }

    @Override
    protected void findViews() {
        input_name = findViewById(R.id.input_name);
        RecyclerView view = findViewById(R.id.recycler_view);
        myInfinite = initMyInfinite(view);
    }

    @Override
    protected void attachEvents() {
        findViewById(R.id.btn_back).setOnClickListener(v -> {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        });
        findViewById(R.id.btn_find).setOnClickListener(v -> {
            String keyword = input_name.getText().toString();
            if (keyword.equals(strQuery)) return;

            strQuery = keyword;
            cafeResults = null;
            if (mAdapter != null) {
                mAdapter.clear();
            }
            myInfinite.performFirstLoad();
        });
    }

    class CafeAdapter extends RecyclerView.Adapter<CafeAdapter.CafeHolder> {
        private List<Cafe> list;
        private ItemClickListener itemClickListener;

        CafeAdapter(List<Cafe> list) {
            this.list = list;
        }

        @Override
        public CafeAdapter.CafeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.row_cafe_find,
                    parent,
                    false
            );
            return new CafeHolder(view, viewType);
        }

        @Override
        public void onBindViewHolder(CafeAdapter.CafeHolder holder, int position) {
            Cafe item = getItem(position);
            String cafename = item.getCafename();
            holder.tvCafename.setText(cafename);
            holder.tvCafename.setTypeface(holder.tvCafename.getTypeface(), Typeface.BOLD);

            String cafedesc = item.getCafedesc();
            holder.etCafededsc.setText(cafedesc);

            String opendate = item.getOpendate();
            holder.tvOpendate.setText(opendate);

            String confirm = item.getConfirm();
            String confirmMessage = "";
            if ("Y".equals(confirm)) {
                confirmMessage = "비공개";
            } else {
                confirmMessage = "공개";
            }

            String memcnt = item.getTotal();
            holder.tvMember.setText("멤버: " + memcnt + "명  " + confirmMessage);

            String admin = item.getAdmin();
            holder.tvAdmin.setText("관리자: " + admin);


            holder.row.setOnClickListener(v -> {
                if ("Y".equals(item.getConfirm())) {
                    toast("비공개 카페입니다.");
                    return;
                }
                Bundle bu = new Bundle();
                bu.putString("cafeseq",item.getCafeseq());
                bu.putSerializable("cafekey", item.getCafekey());

                // kmj mod
                callActivity(CafeDetailActivity.class, bu, false);
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public Cafe getItem(int id) {
            return list.get(id);
        }

        public void setClickListener(ItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        public void clear() {
            this.list.clear();
            notifyDataSetChanged();
        }


        class CafeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ConstraintLayout row;
            TextView tvCafename, tvOpendate, tvMember, tvAdmin;
            KoswEditText etCafededsc;

            public CafeHolder(View view, int viewType) {
                super(view);
                pickupViews(viewType);
            }

            private void pickupViews(int type) {
                row = itemView.findViewById(R.id.box_of_row);
                tvCafename = itemView.findViewById(R.id.txt_cafename);
                etCafededsc = itemView.findViewById(R.id.txt_cafedesc);
                tvOpendate = itemView.findViewById(R.id.txt_open_date);
                tvMember = itemView.findViewById(R.id.txt_member);
                tvAdmin = itemView.findViewById(R.id.txt_admin);
            }

            @Override
            public void onClick(View view) {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(view, getAdapterPosition());
                }
            }
        }
    }

    interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in_full, R.anim.slide_out_right);
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

    @Override
    public void performLoadTask(int firstVisibleItemPosition) {
        showSpinner("");
        Map<String, Object> query = KUtil.getDefaultQueryMap();
        query = PageResponseBase.requestParam(query, cafeResults);
        query.put("keyword", strQuery);
        Call<PageResponseBase<Cafe>> call = getCafeService().openCafeList(query);
        call.enqueue(new Callback<PageResponseBase<Cafe>>() {
            @Override
            public void onResponse(Call<PageResponseBase<Cafe>> call, Response<PageResponseBase<Cafe>> response) {
                if (response.isSuccessful()) {
                    PageResponseBase<Cafe> cafelist = response.body();
                    if (cafelist.isSuccess()) {
                        cafeResults = PageResponseBase.merge(cafeResults, cafelist, false);

                        if (null == cafeResults || CollectionUtils.isEmpty(cafeResults.getResult())) {
                            toast(R.string.warn_cafe_not_exists);
                        } else {
                            mAdapter = new CafeAdapter(cafeResults.getResult());
                            myInfinite.wrapRefreshView(mAdapter, firstVisibleItemPosition);
                        }

                    }
                }
                closeSpinner();
            }

            @Override
            public void onFailure(Call<PageResponseBase<Cafe>> call, Throwable t) {
                closeSpinner();
                LogUtils.err(TAG, t);
                toast(R.string.warn_server_not_smooth);
            }
        });
    }
}

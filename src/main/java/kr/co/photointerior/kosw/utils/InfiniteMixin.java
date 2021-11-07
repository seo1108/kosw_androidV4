package kr.co.photointerior.kosw.utils;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.github.pwittchen.infinitescroll.library.InfiniteScrollListener;

import kr.co.photointerior.kosw.global.Env;

public final class InfiniteMixin {
    public abstract static class MyInfinite<T extends RecyclerView.Adapter> extends InfiniteScrollListener {
        private RecyclerView recyclerView;
        private int lastOffset = 0;

        public MyInfinite(RecyclerView recyclerView, LinearLayoutManager layoutManager) {
            super(Env.PAGE_SIZE, layoutManager);
            this.recyclerView = recyclerView;
        }

        @Override
        public void onScrolledToEnd(int firstVisibleItemPosition) {
            int diff = Math.abs(firstVisibleItemPosition - lastOffset);
            if (diff < 3) return;
            lastOffset = Math.max(0, firstVisibleItemPosition);
            Log.d("KOSW-DBG", "Real Offset = " + lastOffset);
            onLoadMore(lastOffset);
        }

        public void wrapRefreshView(T adapter, int position) {
            super.refreshView(recyclerView, adapter, position);
        }

        public void performFirstLoad() {
            recyclerView.setAdapter(null);
            onScrolledToEnd(-100);
        }

        public abstract void onLoadMore(int firstVisibleItemPosition);
    }


    public interface MyInfiniteMixin {
        default MyInfinite initMyInfinite(RecyclerView recyclerView) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(recyclerView.getContext());
            recyclerView.setHasFixedSize(true);
            recyclerView.setNestedScrollingEnabled(false);

            final MyInfinite myInfinite = new MyInfinite(recyclerView, layoutManager) {
                @Override
                public void onLoadMore(final int firstVisibleItemPosition) {
                    performLoadTask(firstVisibleItemPosition);
                }
            };
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.addOnScrollListener(myInfinite);
            return myInfinite;
        }

        default void performLoadTask(final int firstVisibleItemPosition) {
        }
    }
}

package kr.co.photointerior.kosw.rest.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.blankj.utilcode.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import kr.co.photointerior.kosw.global.Env;

public class PageResponseBase<T> extends ResponseBase {
    private static final long serialVersionUID = -7162804077415539199L;
    private List<T> result;
    /**
     * 큐어리 전체 카운트
     */
    private int total;
    /**
     * 페이지 인덱스
     */
    private int page;
    /**
     * 페이지당 아이템 카운트
     */
    private int size;

    public List<T> getResult() {
        return result;
    }

    public void setResult(List<T> result) {
        this.result = result;
    }

    public void add(T item) {
        if (item == null)
            return;

        if (result == null)
            result = new ArrayList<>();
        result.add(item);
    }

    public void addAll(Collection<T> items) {
        if (CollectionUtils.isEmpty(items))
            return;

        if (result == null)
            result = new ArrayList<>();
        result.addAll(items);
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getCount() {
        return CollectionUtils.size(result);
    }

    public boolean canLoadMore() {
        return getCount() < total;
    }

    public static <T, R extends PageResponseBase<T>> R merge(R a, R b, boolean atBegin) {
        if (a == null) return b;
        if (b == null) return a;
        a.setPage(b.getPage());
        a.setSize(b.getSize());
        a.setTotal(b.getTotal());
        if (atBegin) {
            b.addAll(a.getResult());
            a.setResult(b.getResult());
        } else {
            a.addAll(b.getResult());
        }
        return a;
    }

    public static Map<String, Object> requestParam(
            @NonNull Map<String, Object> origin,
            @Nullable PageResponseBase current
    ) {
        int page = 0;
        int size = Env.PAGE_SIZE;
        if (current != null && current.total > 0) {
            page = current.page + 1;
            size = current.size;
        }
        origin.put("page", page);
        origin.put("size", size);
        return origin;
    }
}

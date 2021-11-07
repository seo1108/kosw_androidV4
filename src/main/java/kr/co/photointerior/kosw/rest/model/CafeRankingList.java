package kr.co.photointerior.kosw.rest.model;

import com.blankj.utilcode.util.CollectionUtils;
import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CafeRankingList extends PageResponseBase<RankInCafe> {
    @SerializedName("mine")
    private RankInCafe mine;

    public RankInCafe getMine() {
        return mine;
    }

    public void setMine(RankInCafe mine) {
        this.mine = mine;
    }

    @Override
    public String toString() {
        return "CafeRankingList{" +
                "mine=" + mine +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CafeRankingList that = (CafeRankingList) o;
        return Objects.equals(mine, that.mine);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mine);
    }


    public static CafeRankingList merge(CafeRankingList a, CafeRankingList b, boolean atBegin) {
        CafeRankingList result = PageResponseBase.merge(a, b, atBegin);
        result.mine = b.mine;
        return result;
    }

    public List<RankInCafe> getResultWithMine() {
        List<RankInCafe> list = super.getResult();
        Collections.sort(list, (a, b) -> a.getRank() - b.getRank());

        if (mine != null) {
            // Remove my rank info from list.
            try {
                //CollectionUtils.filter(list, it -> !it.getNickname().equals(mine.getNickname()));
                CollectionUtils.filter(list, it -> !it.getUser_seq().equals(mine.getUser_seq()));
            } catch (Exception ex) {}

            // Add mine to first
            mine.setIsmine("Y");
            list.add(0, mine);
        }
        return list;
    }
}

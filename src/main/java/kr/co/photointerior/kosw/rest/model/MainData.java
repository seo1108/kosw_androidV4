package kr.co.photointerior.kosw.rest.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 메인화면 데이터
 */
public class MainData extends ResponseBase {
    @SerializedName("today_record")
    private Record todayRecord;

    @SerializedName("noti_main")
    private Bbs mainNotice;
    @SerializedName("noti_customer")
    private Bbs customerNotice;

    @SerializedName("gold_jersey")
    private String goldJersey;

    @SerializedName("green_jersey")
    private String greenJersey;

    @SerializedName("reddot_jersey")
    private String redDotJersey;

    @SerializedName("build_name")
    private String buildingName;

    @SerializedName("build_seq")
    private int build_seq;


    @SerializedName("build_floor_amt")
    private int build_floor_amt;

    @SerializedName("mainCharImageFile")
    private String mainCharImageFile;

    @SerializedName("subCharImageFile")
    private String subCharImageFile;


    @SerializedName("cust_name")
    private String custName;


    @SerializedName("user_cnt")
    private int userCnt;

    @SerializedName("act_amt")
    private int actAmt;

    @SerializedName("body_type")
    private int botyType;

    @SerializedName("cust_build_seq")
    private int cust_build_seq;

    @SerializedName("userLevel")
    private String userLevel;

    @SerializedName("club_list")
    private List<Club> clubList;

    @SerializedName("ggr_list")
    private List<GGRRow> ggrList;

    @SerializedName("isbuild")
    private String isbuild;

    @SerializedName("total_amt")
    private int totalAmt;

    @SerializedName("showPopup")
    private String showPopup;

    @SerializedName("popupUrl")
    private String popupUrl;

    @SerializedName("user_seq")
    private String userSeq;

    public String getIsbuild() {
        return isbuild;
    }

    public void setIsbuild(String isbuild) {
        this.isbuild = isbuild;
    }

    public List<Club> getClubList() {
        return clubList;
    }

    public List<GGRRow> getGgrList() {
        return ggrList;
    }

    public void setGgrList(List<GGRRow> ggrList) {
        this.ggrList = ggrList;
    }

    public void setClubList(List<Club> clubList) {
        this.clubList = clubList;
    }

    public int getBuild_floor_amt() {
        return build_floor_amt;
    }

    public void setBuild_floor_amt(int build_floor_amt) {
        this.build_floor_amt = build_floor_amt;
    }

    public String getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(String userLevel) {
        this.userLevel = userLevel;
    }

    public String getMainCharImageFile() {
        return mainCharImageFile;
    }

    public void setMainCharImageFile(String mainCharImageFile) {
        this.mainCharImageFile = mainCharImageFile;
    }

    public String getSubCharImageFile() {
        return subCharImageFile;
    }

    public void setSubCharImageFile(String subCharImageFile) {
        this.subCharImageFile = subCharImageFile;
    }

    public int getBuild_seq() {
        return build_seq;
    }

    public void setBuild_seq(int build_seq) {
        this.build_seq = build_seq;
    }

    public int getCust_build_seq() {
        return cust_build_seq;
    }

    public void setCust_build_seq(int cust_build_seq) {
        this.cust_build_seq = cust_build_seq;
    }

    public int getBotyType() {
        return botyType;
    }

    public void setBotyType(int botyType) {
        this.botyType = botyType;
    }

    public int getUserCnt() {
        return userCnt;
    }

    public void setUserCnt(int userCnt) {
        this.userCnt = userCnt;
    }

    public int getActAmt() {
        return actAmt;
    }

    public void setActAmt(int actAmt) {
        this.actAmt = actAmt;
    }

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    public Record getTodayRecord() {
        return todayRecord;
    }

    public void setTodayRecord(Record todayRecord) {
        this.todayRecord = todayRecord;
    }

    public Bbs getMainNotice() {
        return mainNotice;
    }

    public void setMainNotice(Bbs mainNotice) {
        this.mainNotice = mainNotice;
    }

    public Bbs getCustomerNotice() {
        return customerNotice;
    }

    public void setCustomerNotice(Bbs customerNotice) {
        this.customerNotice = customerNotice;
    }

    public String getGreenJersey() {
        return greenJersey;
    }

    public void setGreenJersey(String greenJersey) {
        this.greenJersey = greenJersey;
    }

    public String getGoldJersey() {
        return goldJersey;
    }

    public void setGoldJersey(String goldJersey) {
        this.goldJersey = goldJersey;
    }

    public String getRedDotJersey() {
        return redDotJersey;
    }

    public void setRedDotJersey(String redDotJersey) {
        this.redDotJersey = redDotJersey;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public int getTotalAmt() {
        return totalAmt;
    }

    public void setTotalAmt(int totalAmt) {
        this.totalAmt = totalAmt;
    }

    public String getShowPopup() {
        return showPopup;
    }

    public void setShowPopup(String showPopup) {
        this.showPopup = showPopup;
    }

    public String getPopupUrl() {
        return popupUrl;
    }

    public void setPopupUrl(String popupUrl) {
        this.popupUrl = popupUrl;
    }

    public String getUserSeq() {
        return userSeq;
    }

    public void setUserSeq(String userSeq) {
        this.userSeq = userSeq;
    }

    @Override
    public String toString() {
        return "MainData{" +
                "todayRecord=" + todayRecord +
                ", mainNotice=" + mainNotice +
                ", customerNotice=" + customerNotice +
                ", goldJersey='" + goldJersey + '\'' +
                ", greenJersey='" + greenJersey + '\'' +
                ", redDotJersey='" + redDotJersey + '\'' +
                ", buildingName='" + buildingName + '\'' +
                ", build_seq=" + build_seq +
                ", build_floor_amt=" + build_floor_amt +
                ", mainCharImageFile='" + mainCharImageFile + '\'' +
                ", subCharImageFile='" + subCharImageFile + '\'' +
                ", custName='" + custName + '\'' +
                ", userCnt=" + userCnt +
                ", actAmt=" + actAmt +
                ", botyType=" + botyType +
                ", cust_build_seq=" + cust_build_seq +
                ", userLevel='" + userLevel + '\'' +
                ", clubList=" + clubList +
                ", ggrList=" + ggrList +
                ", isbuild='" + isbuild + '\'' +
                ", totalAmt=" + totalAmt +
                ", showPopup='" + showPopup + '\'' +
                ", popupUrl='" + popupUrl + '\'' +
                '}';
    }
}

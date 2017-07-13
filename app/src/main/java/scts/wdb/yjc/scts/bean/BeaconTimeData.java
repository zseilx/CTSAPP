package scts.wdb.yjc.scts.bean;

import java.sql.Timestamp;
import java.util.Calendar;

/**
 * Created by JYH on 2017-04-05.
 */

public class BeaconTimeData {
    private int beacon_mjr;
    private int beacon_mnr;
    private String user_id;
    private int cours_stay_time;
    private Timestamp cours_pasng_time;

    // 초기 객체 생성시 현재 시간 입력
    public BeaconTimeData() {

        // (1) Calendar객체를 얻는다.
        Calendar cal = Calendar.getInstance();

        // 시간 셋팅
        cours_pasng_time = new Timestamp(cal.getTimeInMillis());

        //currentTime = new Date( new java.util.Date().getTime() );
    }
    public BeaconTimeData(int beacon_mjr, int beacon_mnr) {
        this();

        this.beacon_mjr = beacon_mjr;
        this.beacon_mnr = beacon_mnr;
    }

    public int getBeacon_mjr() {
        return beacon_mjr;
    }

    public void setBeacon_mjr(int beacon_mjr) {
        this.beacon_mjr = beacon_mjr;
    }

    public int getBeacon_mnr() {
        return beacon_mnr;
    }

    public void setBeacon_mnr(int beacon_mnr) {
        this.beacon_mnr = beacon_mnr;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public int getCours_stay_time() {
        return cours_stay_time;
    }

    public void setCours_stay_time(int cours_stay_time) {
        this.cours_stay_time = cours_stay_time;
    }

    public Timestamp getCours_pasng_time() {
        return cours_pasng_time;
    }

    /*
    public void setStayTimeMil() {
        Calendar cal = Calendar.getInstance();

        // 현재시간 - 감지된 시간을 해서 머문 시간을 구함
        stayTimeMil = (int) (cal.getTimeInMillis() - currentTime.getTime());
    }
    */

}

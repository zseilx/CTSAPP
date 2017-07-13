package scts.wdb.yjc.scts.hardwaremanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.repackaged.gson_v2_3_1.com.google.gson.GsonBuilder;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import scts.wdb.yjc.scts.bean.BeaconTimeData;
import scts.wdb.yjc.scts.network.send.BeaconSet;

import static android.content.ContentValues.TAG;

/**
 * Created by JYH on 2017-04-15.
 * beacon관리하는 클래스 해당 클래스에서 비콘 리스너 및 커넥트를 시작하지만
 * 실제 시작을 해줘야 하는건 액티비티에서 실행을 시켜줘야함
 */

public class BeaconM{
    private BeaconManager beaconManager;
    private static final UUID BEACON_UUID = UUID.fromString("20CAE8A0-A9CF-11E3-A5E2-0800200C9A66");
    private Region region = new Region("ranged region", BEACON_UUID, null, null);

    // 그전에 가장 가까운 비콘
    private Beacon oldnearBeacon = null;
    // 현재 가장 가까운 비콘(감지시점)
    private Beacon currentNearBeacon;
    // 센서 관련 로직 클래스
    private SensorM sensorM;
    // 비콘이 감지된 갯수
    private int beaconCnt;
    // 비콘시간 및 데이터
    private BeaconTimeData beaconTimeData;

    // 멈춰 있을 때 비콘에서 머문 시간을 계산하기 위한 시간데이터
    private long currentTime;

    // 머문 시간 계산용
    private int stayTimeMil = 0;

    private Context mContext;

    // 안드로이드에서 Toast 창이 겹치지 않게 해주는 것.
    // 해당 메서드 말고 직접 Toast실행시 , 비콘에서 띄우는 창이 매우 많아져서
    // 겹쳐버리는 것으로 인해 제대로 된 디버깅이 안됨.
    private static Toast sToast;
    public static void showToast(Context context, String message) {
        if(sToast == null) {
            sToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        }
        else {
            sToast.setText(message);
        }
        sToast.show();
    }

    /**
     * 생성자
     * @param beaconManager
     */
    public BeaconM(BeaconManager beaconManager) {
        this.beaconManager = beaconManager;
    }

    /**
     * 생성자
     * @param beaconManager
     * @param sensorM
     */
    public BeaconM(BeaconManager beaconManager, SensorM sensorM) {
        this.beaconManager = beaconManager;
        this.sensorM = sensorM;
    }

    public void SetContext(Context context) {
        this.mContext = context;
    }

    // 리스너 등록 ( 로직까지 등록 )
    public void BeaconSetListner() {

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                beaconCnt = list.size();
                if(!list.isEmpty()) {

                    int rssi = list.get(0).getRssi();

                    if (rssi > -90 && rssi < -75) {
                        currentNearBeacon = list.get(0);
                        //showToast(mContext, "beacon" + list.get(0).toString());
                        //Toast.makeText(mContext, "beacon" + list.get(0).toString(), Toast.LENGTH_SHORT).show();
                    }
                    else {
                        currentNearBeacon = null;
                        beaconCnt = 0;
                    }
                }
                else {
                    currentNearBeacon = null;
                }
                logic();

                // 테스트용

                /*if(!list.isEmpty()) {
                    Toast.makeText(mContext, "감지비콘 : " + list.get(0).toString(), Toast.LENGTH_LONG).show();*//*
                    for (int i = 0; i < list.size(); i++) {
                        Log.d("비콘들 : :", "A = " + list.get(i).toString());
                    }*//*
                }*/
            }
        });
    }

    public void onDestroy() {
        // 종료 전에 서버로 현재 감지되어 있는 비콘 정보 전송
        endMethod();
        beaconManager.disconnect();
    }

    public void BeaconConnect() {
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });
    }

    public String getId() {
        SharedPreferences sp = mContext.getSharedPreferences("test", 0);
        String str = sp.getString("user_id", "");

        return str;
    }

    // 앱이 종료될 때 현재 감지된 비콘의 머문 시간을 마지막으로 저장하고 앱을 종료.
    public void endMethod() {
        // oldnearBeacon이 null 일 경우에는 머문 시간이 저장안된 경로정보가 없다는 것임. 그럴 경우 해당 secondSend를 실행할 필요가 없음.
        if(oldnearBeacon != null) {
            if(currentTime != 0) {
                stayTimeMil += (int) (getCurrent_Time() - currentTime);
                currentTime = 0;
            }
            // 그전에 감지되어있던 비콘은 서버로 머문시간을 함께 저장한 것을 보냄. ( mode second )
            // 서버로 전송시킴 , 두번째 전송은 머문 시간까지 저장
            sendBeaconData("secondSend");
        }
    }

    // 비콘 서버로 전송
    public void sendBeaconData(String mode) {

        // 비콘 타임데이터에 머문 시간 저장
        if(mode.equals("secondSend")) {
            beaconTimeData.setCours_stay_time(stayTimeMil / 1000);


            stayTimeMil = 0;
        }

        // 서버로 전송시키기 위해 비콘 감지 데이터를 json형태 문자열로 변환
        String json = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd hh:mm:ss.S")
                .create()
                .toJson(beaconTimeData);
        Log.d(TAG, "sendBeaconData: " + json);
        // 제이슨 형태 확인
        //Toast.makeText(mContext, "logic: 서버전송 stayTimeMil = " + json, Toast.LENGTH_LONG).show();
        // 디버깅 json
        showToast(mContext, json.toString());
        // 서버로 전송시킴
        BeaconSet networkTask = new BeaconSet(mContext);

        // 첫번째 감지되자마자 전송하는 것과 두번째 더이상 감지되지 않을때 전송하는 것을 나누기 위해서
        // 해주는 조건들
        String select = "firstSend";
        if(mode.equals("secondSend")) {
            select = "secondSend";
        }


        // 임시로 서버전송 막음
        networkTask.execute(select, json);
    }

    public long getCurrent_Time() {
        return Calendar.getInstance().getTimeInMillis();
    }

    public void logic() {

        Log.d(TAG, "logic: MoveChk = " + sensorM.getMoveChk());
        /********************************************************** 움직일 경우 **********************************************************/
        if(sensorM.getMoveChk()) {
            Log.d(TAG, "logic: beaconCnt = " + beaconCnt);
            /********************************* 비콘 감지됨 *********************************/
            if(beaconCnt > 0) {

                // 그 전에 감지된 비콘이 없다 ( 어찌되든 이부분에서는 무조건 비콘감지가 처음에 이루어진 것임
                if(oldnearBeacon == null) {
                    oldnearBeacon = currentNearBeacon;
                    // 처음 감지된 시점에서 비콘의 메이저와, 마이너, 그리고 감지된 시간을 생성
                    // 감지된 시간은 해당 BeaconTimeData클래스 자체에서 객체 생성시 자동으로 생성함.
                    beaconTimeData = new BeaconTimeData(oldnearBeacon.getMajor(), oldnearBeacon.getMinor());
                    beaconTimeData.setUser_id( getId() );
                    // 서버로 전송시킴 첫번째 전송
                    sendBeaconData("firstSend");
                }
                // 그 전에 감지된 비콘과 다르다 // 이부분은 감지된 비콘이 기존에 한개 존재하는 상태에서 해당 비콘이 멀어지고 다른 비콘이 가까워졋을때임
                else if( oldnearBeacon.getMajor() != currentNearBeacon.getMajor() || oldnearBeacon.getMinor() != currentNearBeacon.getMinor() ) {
                    if(currentTime != 0) {
                        stayTimeMil += (int) (getCurrent_Time() - currentTime);
                        currentTime = 0;
                        Log.d(TAG, "logic: 움직였는데 다른비콘 감지 stayTimeMil = " + stayTimeMil); // 디버깅용 시스템 로그
                    }
                    // 그전에 감지되어있던 비콘은 서버로 머문시간을 함께 저장한 것을 보냄. ( mode second )
                    // 서버로 전송시킴 , 두번째 전송은 머문 시간까지 저장
                    sendBeaconData("secondSend");

                    // 서버로 기존에 감지된 것을 보냇으니 새롭게 감지된 비콘을 올드 비콘에 주입
                    oldnearBeacon = currentNearBeacon;

                    // 처음 감지된 시점에서 비콘의 메이저와, 마이너, 그리고 감지된 시간을 생성
                    // 감지된 시간은 해당 BeaconTimeData클래스 자체에서 객체 생성시 자동으로 생성함.
                    beaconTimeData = new BeaconTimeData(oldnearBeacon.getMajor(), oldnearBeacon.getMinor());
                    beaconTimeData.setUser_id( getId() );
                    // 서버로 전송시킴 첫번째 전송
                    sendBeaconData("firstSend");
                }
                // 그 전에 감지된 비콘과 같다
                // 어찌됫든 원래 감지된 비콘이 있었고 해당 비콘이 다시 감지된 것이므로 서버에 재전송 할 필요도 없고, 그렇다고 바꿔줄 필요도 없음
                else {
;                   if(currentTime != 0) {
                        stayTimeMil += (int) (getCurrent_Time() - currentTime);
                        currentTime = 0;
                        //Toast.makeText(mContext, "logic: 멈춘 시간 계산 stayTimeMil = " + stayTimeMil, Toast.LENGTH_LONG).show();
                        showToast(mContext, "멈춘시간 계산 stayTimeMil = " + stayTimeMil);
                        Log.d(TAG, "logic: 움직였는데 그전과 같은 비콘 stayTimeMil = " + stayTimeMil); // 디버깅용 시스템 로그
                    }
                }

            }
            /********************************* 비콘 감지안됨 *********************************/
            else {
                if(oldnearBeacon != null) {

                    if(currentTime != 0) {
                        stayTimeMil += (int) (getCurrent_Time() - currentTime);
                        currentTime = 0;
                        Log.d(TAG, "logic: 움직였는데 비콘 감지 안됨 stayTimeMil = " + stayTimeMil); // 디버깅용 시스템 로그
                    }

                    //그전에 감지되어있던 비콘은 서버로 머문시간을 함께 저장한 것을 보냄. ( mode second )
                    // 서버로 전송시킴 , 두번째 전송은 머문 시간까지 저장
                    sendBeaconData("secondSend");

                    // 시간 계산 해놓고 비콘 널로 바꾸기
                    oldnearBeacon = null;
                }
            }
        }

        /********************************************************** 멈췃을 경우 **********************************************************/
        else {
            Log.d(TAG, "logic: beaconCnt = " + beaconCnt);
            if(beaconCnt > 0) {
                if(oldnearBeacon != null && currentTime == 0) {
                    currentTime = getCurrent_Time();
                    //Toast.makeText(mContext, "logic: 멈춘 시간 저장", Toast.LENGTH_LONG).show();
                    showToast(mContext, "멈춘시간 계산22 stayTimeMil = " + stayTimeMil);
                    Log.d(TAG, "logic: 지금 멈춰서 시간계산 시작한다" ); // 디버깅용 시스템 로그
                    Log.d(TAG, "oldBeacon = " + oldnearBeacon.toString() ); // 디버깅용 시스템 로그
                }
            }
        }
    }

}

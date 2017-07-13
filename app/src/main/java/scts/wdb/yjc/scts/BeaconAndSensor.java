package scts.wdb.yjc.scts;

import android.content.Context;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;

import java.sql.Timestamp;
import java.util.Calendar;

import scts.wdb.yjc.scts.bean.BeaconTimeData;
import scts.wdb.yjc.scts.hardwaremanager.BeaconM;
import scts.wdb.yjc.scts.hardwaremanager.SensorM;

/**
 * Created by JYH on 2017-04-15.
 * ****************************** 지금 이클래스 사용안하고 BeaconM에서 SensorM 끌어와서 사용중임
 *
 * 비콘과 센서에서 감지된 값을 이용해
 * 이 클래스에서 두개를 조합, 감지된 비콘의 시간과, 머문 시간데이터를 생성해냄
 * 해당 클래스에서는 가속도 센서의 값을 판정해서 비콘 변경을 담당함
 */

public class BeaconAndSensor {

    // 센서 관련 로직 클래스
    private SensorM sensorM;

    // 비콘 관련 로직 클래스
    private BeaconM beaconM;

    private Beacon nearBeacon;
    private BeaconTimeData beaconTimeData;

    // 멈춰 있을 때 비콘에서 머문 시간을 계산하기 위한 시간데이터
    private Timestamp currentTime;
    // 머문 시간 계산용
    private int stayTimeMil = 0;

    public BeaconAndSensor(Context context) {
        sensorM = new SensorM(context);
        beaconM = new BeaconM(new BeaconManager(context));
    }

    public void StartTraking() {
        sensorM.sensorStart();
        beaconM.BeaconConnect();
    }
    public void saveStayTime() {
        Calendar cal = Calendar.getInstance();
        if(currentTime != null) {
            stayTimeMil += (int) (cal.getTimeInMillis() - currentTime.getTime());
            currentTime = null;
        }
        else {
            currentTime = new Timestamp(cal.getTimeInMillis());
        }
    }

    public void sendStayTime() {

    }
/*

    public void logic() {
        */
/********************************************************** 움직일 경우 **********************************************************//*

        if(sensorM.getMoveChk()) {
            */
/********************************* 비콘 감지됨 *********************************//*

            if(beaconM.getBeaconCnt() > 0) {
                Beacon currentBeacon = beaconM.getNearBeacon();

                // 그 전에 감지된 비콘이 없다
                if(nearBeacon == null) {
                    nearBeacon = currentBeacon;
                    // 처음 감지된 시점에서 비콘의 메이저와, 마이너, 그리고 감지된 시간을 생성
                    beaconTimeData = new BeaconTimeData(nearBeacon.getMajor(), nearBeacon.getMinor());
                }
                // 그 전에 감지된 비콘과 다르다
                else if( nearBeacon.getMajor() != currentBeacon.getMajor() || nearBeacon.getMinor() != currentBeacon.getMinor() ) {
                    nearBeacon = currentBeacon;

                    // 처음 감지된 시점에서 비콘의 메이저와, 마이너, 그리고 감지된 시간을 생성
                    beaconTimeData = new BeaconTimeData(nearBeacon.getMajor(), nearBeacon.getMinor());

                    if(currentTime != null) {
                        Calendar cal = Calendar.getInstance();
                        stayTimeMil += (int) (cal.getTimeInMillis() - currentTime.getTime());
                        currentTime = null;
                    }
                    if(stayTimeMil != 0) {
                        // 비콘 타임데이터에 머문 시간 저장
                        beaconTimeData.setStayTimeMil( stayTimeMil );
                        stayTimeMil = 0;
                        // 서버로 전송시키는 부분 코딩해야함
                    }
                }
                // 그 전에 감지된 비콘과 같다
                else {
;                   if(currentTime != null) {
                        Calendar cal = Calendar.getInstance();
                        stayTimeMil += (int) (cal.getTimeInMillis() - currentTime.getTime());
                        currentTime = null;
                    }
                }

            }
            */
/********************************* 비콘 감지안됨 *********************************//*

            else {
                if(nearBeacon != null) {
                    // 시간 계산 해놓고 비콘 널로 바꾸기
                    nearBeacon = null;

                    if(currentTime != null) {
                        Calendar cal = Calendar.getInstance();
                        stayTimeMil += (int) (cal.getTimeInMillis() - currentTime.getTime());
                        currentTime = null;
                    }
                    if(stayTimeMil != 0) {
                        // 비콘 타임데이터에 머문 시간 저장
                        beaconTimeData.setStayTimeMil( stayTimeMil );
                        stayTimeMil = 0;
                        // 서버로 전송시키는 부분 코딩해야함
                    }
                }
            }
        }

        */
/********************************************************** 멈췃을 경우 **********************************************************//*

        else {
            if(beaconM.getBeaconCnt() > 0) {
                if(nearBeacon != null) {
                    Calendar cal = Calendar.getInstance();
                    currentTime = new Timestamp(cal.getTimeInMillis());
                }
            }
        }

    }
*/

/*

    public void logic() {
        */
/************************************************************ 비콘 한개이상 감지 ************************************************************//*

        if(beaconM.getBeaconCnt() >= 1) {
            */
/********************************* 움직일 경우 *********************************//*

            if(sensorM.getMoveChk()) {
                // 가장 가까운 비콘의 값을 받음
                Beacon newNearBeacon = beaconM.getNearBeacon();

                // 움직였는데 만약 안움직이기 시작한 때의 시간이 저장되어있으면
                if(currentTime != null) {
                    Calendar cal = Calendar.getInstance();
                    stayTimeMil += (int) (cal.getTimeInMillis() - currentTime.getTime());
                    currentTime = null;
                }

                // 감지된 가장 가까운 비콘이 그전에 있던것과 같을경우
                if(nearBeacon != null && nearBeacon.getMajor() == newNearBeacon.getMajor() && nearBeacon.getMinor() == newNearBeacon.getMinor()) {

                    // 만약 멈췃다가 다시 움직엿을 경우 현재 시간을 저장 시켜놧을것임.
                    // 해당하는 경우일 경우 현재 시간 - 멈추기 시작한 시간을 해서 머문 시간에 누적시킴
                }
                // 감지된 가장 가까운 비콘이 그전에 있던것과 다르다,
                else {
                    // 가까운 비콘에 새롭게 가장 가깝게 감지된 비콘을 저장함
                    nearBeacon = beaconM.getNearBeacon();
                    // 처음 감지된 시점에서 비콘의 메이저와, 마이너, 그리고 감지된 시간을 생성
                    beaconTimeData = new BeaconTimeData(nearBeacon.getMajor(), nearBeacon.getMinor());

                    // 비콘 타임데이터에 머문 시간 저장
                    beaconTimeData.setStayTimeMil( stayTimeMil );
                    stayTimeMil = 0;
                    // 서버로 전송시키는 부분 코딩해야함
                }


            }

            */
/********************************* 멈췃을 경우 *********************************//*

            // 멈춰 있을 때는 단지 머문 시간만 저장 시켜주면 됨.
            else {
                if(currentTime == null) {
                    Calendar cal = Calendar.getInstance();
                    currentTime = new Timestamp(cal.getTimeInMillis());
                }
                else {

                }
            }

        }
        */
/************************************************************ 감지된 비콘 없음 ************************************************************//*

        else {
            */
/********************************* 움직일 경우 *********************************//*

            if(sensorM.getMoveChk()) {
                if (nearBeacon != null) {
                    nearBeacon = null;
                }
            }
            */
/********************************* 멈췃을 경우 *********************************//*

            else {

            }
        }
    }
*/

/*

    public void logic() {
        // 움직임이 있을 때
        if(sensorM.getMoveChk()) {
            // 비콘 한개 감지
            if(beaconM.getBeaconCnt() >= 1) {
                // 가장 가까운 비콘의 값을 받음
                nearBeacon = beaconM.getNearBeacon();

                // 처음 감지된 시점에서 비콘의 메이저와, 마이너, 그리고 감지된 시간을 생성
                beaconTimeData = new BeaconTimeData(nearBeacon.getMajor(), nearBeacon.getMinor());

            }
            // 비콘 감지된거 없음
            else {
                if(nearBeacon != null) {
                    nearBeacon = null;
                }
            }
        }
        // 움직임 없을 때
        // 멈춰 있을 때는 단지 머문 시간만 저장 시켜주면 됨.
        else {
            // 비콘 한개 감지
            if(beaconM.getBeaconCnt() >= 1) {

            }
            // 비콘 감지된거 없음
            else {

            }
        }
    }
*/


}

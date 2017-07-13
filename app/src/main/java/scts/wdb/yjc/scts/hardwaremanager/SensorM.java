package scts.wdb.yjc.scts.hardwaremanager;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.text.DecimalFormat;

import static android.content.ContentValues.TAG;

/**
 * Created by JYH on 2017-04-15.
 * 가속도 센서 관리용 클래스
 */

public class SensorM implements SensorEventListener {

    // 센서 관련 객체
    SensorManager m_sensor_manager;
    Sensor m_accelerometer;

    // 실수의 출력 자리수를 지정하는 포맷 객체
    DecimalFormat m_format;

    // 데이터를 저장할 변수들
    float[] m_gravity_data = new float[3];
    float[] m_accel_data = new float[3];

    // 움직이는 속도의 절대값 저장
    float accel_abs;

    // 움직이는지 안움직이는지
    private boolean moveChk;

    // 움직인다고 판정할 값
    final private float MOVEVALUE = 0.2f;

    public SensorM(Context context) {
        // 포맷 객체를 생성한다.
        m_format = new DecimalFormat();
        // 소수점 두자리까지 출력될 수 있는 형식을 지정한다.
        m_format.applyLocalizedPattern("0.##");

        // 시스템서비스로부터 SensorManager 객체를 얻는다.
        m_sensor_manager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        // SensorManager 를 이용해서 가속센서 객체를 얻는다.
        m_accelerometer = m_sensor_manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public void sensorStart() {
        m_sensor_manager.registerListener(this, m_accelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    // 정확도 변경시 호출되는 메소드. 센서의 경우 걋?호출되지 않는다.
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
    }

    // 측정한 값을 전달해주는 메소드. 계산 로직만 포함되어 있음
    public void onSensorChanged(SensorEvent event)
    {
        // 가속 센서가 전달한 데이터인 경우
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            // 중력 데이터를 구하기 위해서 저속 통과 필터를 적용할 때 사용하는 비율 데이터.
            // t : 저속 통과 필터의 시정수. 시정수란 센서가 가속도의 63% 를 인지하는데 걸리는 시간
            // dT : 이벤트 전송율 혹은 이벤트 전송속도.
            // alpha = t / (t + Dt)
            final float alpha = (float)0.8;

            // 저속 통과 필터를 적용한 중력 데이터를 구한다.
            // 직전 중력 값에 alpha 를 곱하고, 현재 데이터에 0.2 를 곱하여 두 값을 더한다.
            m_gravity_data[0] = alpha * m_gravity_data[0] + (1 - alpha) * event.values[0];
            m_gravity_data[1] = alpha * m_gravity_data[1] + (1 - alpha) * event.values[1];
            m_gravity_data[2] = alpha * m_gravity_data[2] + (1 - alpha) * event.values[2];

            // 현재 값에 중력 데이터를 빼서 가속도를 계산한다.
            m_accel_data[0] = event.values[0] - m_gravity_data[0];
            m_accel_data[1] = event.values[1] - m_gravity_data[1];
            m_accel_data[2] = event.values[2] - m_gravity_data[2];

            // 속도 계산
            speedSet();

            // 테스트용 절대값 계산해서 표시
            //test();
        }
    }

    public void speedSet() {
        accel_abs = 0.0f;
        for(int i=0; i<m_accel_data.length; i++) {
            accel_abs += Math.abs(m_accel_data[i]);
        }

        if(accel_abs > MOVEVALUE) {
            moveChk = true;
        }
        else {
            moveChk = false;
        }
    }

    public boolean getMoveChk() {
        return moveChk;
    }

    public void test () {

        // 속도의 절대값이 1.8m/s 이상 일때 시스템 로그를 찍음
        if(accel_abs > 1.8f) {
            for(int i=0; i<m_accel_data.length; i++) {
                String str;

                if(i == 0) str = "x = ";
                else if (i == 1) str = "y = ";
                else str = "z = ";

                Log.d(TAG, "onSensorChanged: " + str + m_format.format((m_accel_data[i])));
            }
        }

    }
}

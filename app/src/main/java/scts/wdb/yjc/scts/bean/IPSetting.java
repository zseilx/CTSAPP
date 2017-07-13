package scts.wdb.yjc.scts.bean;

/**
 * Created by JYH on 2017-04-21.
 *
 * 이 클래스는 코딩 때 각자의 컴퓨터에서 테스트 하기 편하도록 하는 클래스
 * 테스트용과 실제 서버에 올릴때 일일이 주소 값을 바꾸는 것이 아닌
 * 해당 클래스에서 바꾸면 모든 서버 연결 부분 주소값 변경이 이루어 지도록 하는 클래스,
 *
 */

public class IPSetting {

    // selectMode 변수의 값만 바꿈으로서 서버에 연결할 ip 값을 바꿔줌
    // 0 = 서버용 , 1 = 정영화, 2,3 = 정혜수,
    public final static int selectMode = 0;

    public static String getIpAddress() {
        String str = "http://";
        int chk = 0;

        switch (selectMode) {
            case 0: // 서버용
                str += "zseil.cafe24.com/";
                chk = 1;
                break;

            case 1: // 정영화
                str += "172.19.2.57:8080/"; // 내꺼 아이피 wifi 잡을때마다 바뀜
                break;

            case 2: // 정혜수
                str += "106.249.38.66:8080/";
                break;

            case 3: // 정혜수 집 컴터
                str += "192.168.219.147:8080/";
                break;

            default : // 서버용
                str += "zseil.cafe24.com/";
                chk = 1;
                break;
        }
        if(chk == 1)
            str += "SCTS/";
        else
            str += "scts/";

        str += "android/";

        return str;
    }
}

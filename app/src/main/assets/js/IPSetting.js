function IPSetting() {
  var str = "http://";
        var selectMode = 0;
        var chk = 0;

        switch (selectMode) {
            case 0: // 서버용
                str += "zseil.cafe24.com/";
                chk = 1;
                break;

            case 1: // 정영화
                str += "172.19.1.169:8080/"; // 내꺼 아이피 wifi 잡을때마다 바뀜
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


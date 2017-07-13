package scts.wdb.yjc.scts.network.send;

import android.os.AsyncTask;
import android.util.Log;

import scts.wdb.yjc.scts.HttpClient;
import scts.wdb.yjc.scts.bean.IPSetting;

/**
 * Created by JYH on 2017-04-06.
 */

public class TestNetworkTask extends AsyncTask<String, String, String> {

    protected  void onPreExcute(){

        super.onPreExecute();
    }
    @Override
    protected String doInBackground(String... json) {
        // bean 안에 있는 ip 셋팅 정보를 꼭 바꾸도록 할 것
        HttpClient.Builder http = new HttpClient.Builder("POST", IPSetting.getIpAddress() + "android");

        //http.addOrReplace("json", json[0]);

        // HTTP 요청 전송
        HttpClient post = http.create();

        post.request();

        // 응답 상태코드 가져오기
        int statusCode = post.getHttpStatusCode();

        // 응답 본문 가져오기
        String body = post.getBody();


        return body;
    }

    // jsp에서 리턴한 데이터 처리부분
    protected void onPostExecute(String s){
        Log.d("HTTP_RESULT", s);
    }

}

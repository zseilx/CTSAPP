package scts.wdb.yjc.scts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.estimote.sdk.SystemRequirementsChecker;
import com.estimote.sdk.repackaged.gson_v2_3_1.com.google.gson.GsonBuilder;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import scts.wdb.yjc.scts.bean.BeaconTimeData;
import scts.wdb.yjc.scts.bean.IPSetting;
import scts.wdb.yjc.scts.network.send.BeaconSet;

public class MainActivity extends AppCompatActivity {

    private EditText user_id_input; // 서버 통신 테스트용 아이디 입력 칸
    private EditText user_pw_input; // 서버 통신 테스트용 패스워드 입력 칸
    private String user_id;          // 입력된 아이디
    private String user_pw;          // 입력된 비밀번호
    SharedPreferences sp;            // 세션 유지하기위한 preference
    private String token;

    final String TAG = "디버깅";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /****************************************************** 로그인 세션 생성하는법 *********************************************************************/
        //test();
        loginButton();

        /***************************************************** 회원가입 화면 전환 ****************************************************************/

        findViewById(R.id.join).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Join.class);
                startActivity(intent);
            }
        });

    }

   @Override
    protected void onResume() {
        super.onResume();
        // 블루투스 권한 및 활성화 코드
       SystemRequirementsChecker.checkWithDefaultDialogs(this);
    }

  protected void test() {

        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sp = getSharedPreferences("test", 0);
                String str = sp.getString("user_id", "");

                BeaconTimeData beaconTimeData = new BeaconTimeData(123, 45678);
                beaconTimeData.setUser_id(str);

                String json = new GsonBuilder()
                        .setDateFormat("yyyy-MM-dd hh:mm:ss.S")
                        .create()
                        .toJson(beaconTimeData);

                BeaconSet test = new BeaconSet(getApplicationContext());

                Toast.makeText(getApplicationContext(), json, Toast.LENGTH_LONG).show();
                Log.d(TAG, "onClick: " + json);
                test.execute(json);
            }
        });
    }

    public void loginButton() {

        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {

                user_id_input = (EditText) findViewById(R.id.userId);
                user_id = user_id_input.getText().toString();
                user_pw_input = (EditText) findViewById(R.id.userPw);
                user_pw = user_pw_input.getText().toString();

                FirebaseInstanceId.getInstance().getToken();

                token = FirebaseInstanceId.getInstance().getToken();
                Log.d("FCM_Token", token);



                JSONObject  jsonObject = new JSONObject();
                try {
                    jsonObject.put("user_id", user_id);
                    jsonObject.put("user_pw", user_pw);
                    jsonObject.put("token", token);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String json = jsonObject.toString();

                // 네트워크 연결 후 서버 전송
                NetworkTask networkTask = new NetworkTask();
                networkTask.execute(json);

                NetworkTask2 networkTask2 = new NetworkTask2();
                networkTask2.execute(user_id);

            }
        });
    }

    // 로그인 세션 관련 네트워크 통신
    private class NetworkTask extends AsyncTask<String, String, String> {

        protected  void onPreExcute(){

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... json) {
            // bean 안에 있는 ip 셋팅 정보를 꼭 바꾸도록 할 것
            HttpClient.Builder http = new HttpClient.Builder("POST", IPSetting.getIpAddress() + "androidLogin");

            http.addOrReplace("UserVO", json[0]);

            // HTTP 요청 전송
            HttpClient post = http.create();

            post.request();


            // 응답 상태코드 가져오기
            int statusCode = post.getHttpStatusCode();

            // 응답 본문 가져오기
            String body = post.getBody();


            return body;
        }

        protected void onPostExecute(String s){
            // db에 저장된 유저 정보를 성공적으로 조회햇음
            if(s.equals("SUCCESS")) {
                // 프리퍼런스 설정
                sp = getSharedPreferences("test", MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("user_id", user_id);
                editor.commit();

                // 로그인 완료 및 웹뷰 창 띄우기 등등 처리해야함
                // 현재 단순히 웹뷰만 띄움
                Intent intent = new Intent(getApplicationContext(), WebViewMain.class);
                startActivity(intent);

                // 디버깅용
                Toast.makeText(getApplicationContext(), "로그인 성공!!!!" + "저장된 아이디 : " + sp.getString("user_id",""), Toast.LENGTH_LONG).show();
                Log.d(TAG, "프리퍼런스 설정 완료");
                Log.d(TAG, "저장된 아이디 : " + sp.getString("user_id",""));

            }
            // 저장된 유저 정보가 없을 때
            else {
                // 실제 로그인 실패햇을때 아이디 비밀번호 틀렷다는 창을 보여줘야하는 부분을 코딩해야함

                // 디버깅용
                Toast.makeText(getApplicationContext(), "로그인 실패!!!!" + "입력 아이디 : " + user_id, Toast.LENGTH_LONG).show();
                Log.d(TAG, "로그인 실패함");
                Log.d(TAG, "입력 아이디 : " + user_id);
            }


        }

    }


    // 포인트 받아오기!
    private class NetworkTask2 extends AsyncTask<String, String, String> {

        protected  void onPreExcute(){

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... json) {
            // bean 안에 있는 ip 셋팅 정보를 꼭 바꾸도록 할 것
            HttpClient.Builder http = new HttpClient.Builder("POST", IPSetting.getIpAddress() + "point");

            http.addOrReplace("user_id", json[0]);

            // HTTP 요청 전송
            HttpClient post = http.create();

            post.request();


            // 응답 상태코드 가져오기
            int statusCode = post.getHttpStatusCode();

            // 응답 본문 가져오기
            String body = post.getBody();


            return body;
        }

        protected void onPostExecute(String s){

            Log.i("point", s);

            sp = getSharedPreferences("test", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("point", s);
            editor.commit();

            Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();


        }

    }




}

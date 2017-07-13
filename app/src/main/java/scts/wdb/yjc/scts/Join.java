package scts.wdb.yjc.scts;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import scts.wdb.yjc.scts.bean.IPSetting;

public class Join extends AppCompatActivity {

    EditText user_id_input;
    String user_id;
    EditText user_pw_input;
    String user_pw;
    EditText age_input;
    String age;
    RadioGroup gender_radio;
    RadioButton radioButton;
    String gender;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);



        findViewById(R.id.check_id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_id_input = (EditText) findViewById(R.id.user_id);
                user_id = user_id_input.getText().toString();
                Toast.makeText(getApplicationContext(), "버튼 눌러짐", Toast.LENGTH_LONG).show();

                NetworkTaskCheckUser networkTask = new NetworkTaskCheckUser();
                networkTask.execute(user_id);
            }
        });

        findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_id_input = (EditText) findViewById(R.id.user_id);
                user_id = user_id_input.getText().toString();
                user_pw_input = (EditText) findViewById(R.id.user_pw);
                user_pw = user_pw_input.getText().toString();
                age_input = (EditText) findViewById(R.id.age);
                age = age_input.getText().toString();
                gender_radio = (RadioGroup) findViewById(R.id.gender);
                radioButton = (RadioButton) findViewById(gender_radio.getCheckedRadioButtonId());
                gender = radioButton.getText().toString();

                if(gender.equals("Woman")){
                    gender = "w";
                }else{
                    gender="m";
                }
                JSONObject jsonObject = new JSONObject();
                String json;
                try {
                    jsonObject.put("user_id", user_id);
                    jsonObject.put("user_pw", user_pw);
                    jsonObject.put("age", age);
                    jsonObject.put("gender", gender);
                    json = jsonObject.toString();
                    NetworkTaskJoin networkTaskJoin = new NetworkTaskJoin();
                    networkTaskJoin.execute(json);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });

    }

    private class NetworkTaskCheckUser extends AsyncTask<String, String, String> {

        protected void onPreExcute() {

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... json) {
            // bean 안에 있는 ip 셋팅 정보를 꼭 바꾸도록 할 것
            HttpClient.Builder http = new HttpClient.Builder("GET", IPSetting.getIpAddress() + "checkUser");

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
            Log.i("result",s);
        }
    }

    private class NetworkTaskJoin extends AsyncTask<String, String, String> {

        protected void onPreExcute() {

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... json) {
            // bean 안에 있는 ip 셋팅 정보를 꼭 바꾸도록 할 것
            HttpClient.Builder http = new HttpClient.Builder("GET", IPSetting.getIpAddress() + "signUp");

            http.addOrReplace("json", json[0]);

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
            Log.i("result",s);
        }
    }
}

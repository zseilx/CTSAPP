package scts.wdb.yjc.scts;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.Process;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.estimote.sdk.BeaconManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;

import scts.wdb.yjc.scts.bean.IPSetting;
import scts.wdb.yjc.scts.hardwaremanager.BeaconM;
import scts.wdb.yjc.scts.hardwaremanager.SensorM;

public class WebViewMain extends AppCompatActivity{

    private final Handler handler = new Handler();

    private WebView webView;

    // 센서 관련 로직 클래스
    private SensorM sensorM;
    // 비콘 관련 로직 클래스
    private BeaconM beaconM;
    private SharedPreferences sp;
    private String str;
    private String point;
    private String coupon;
    private String bhf_code;
    private SharedPreferences.Editor editor;


    private EditText productInput;
    private String productName;
    private Button button;


    private final long	FINSH_INTERVAL_TIME    = 2000;
    private long		backPressedTime        = 0;

    private NfcAdapter mAdapter;

    private PendingIntent mPendingIntent;

    private IntentFilter[] mFilters;
    private String[][] mTechLists;




    private final static String MAIN_URL = "file:///android_asset/index.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.logo2);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                webView.loadUrl(MAIN_URL);
                getIntent().removeExtra("coupon");

            }
        });





        sp = getSharedPreferences("test", 0);

        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);

        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);

        File dir = getCacheDir();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        webView.getSettings().setAppCachePath(dir.getPath());
        webView.getSettings().setAppCacheEnabled(true);


        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.addJavascriptInterface(new AndroidBridge(), "HybridApp");

        webView.setWebViewClient(new WebViewClientTest());
        webView.loadUrl(MAIN_URL);





        productInput = (EditText) findViewById(R.id.productInput);

        button = (Button) findViewById(R.id.productSearch);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    productName = URLDecoder.decode(productInput.getText().toString(), "UTF-8");

                    Log.d("productNameName", productName);

                    if(productName.equals("")){
                        webView.loadUrl("javascript:searchProduct('no')");

                    }else{
                        NetworkTask networkTask = new NetworkTask();
                        JSONObject json = new JSONObject();
                        bhf_code = sp.getString("bhf_code", "2");
                        try {
                            json.put("productName", productName);
                            json.put("bhf_code", bhf_code);
                            networkTask.execute(json.toString());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
        });


        /****************************************************** 가속도 센서 활용 테스트 *********************************************************************/
        sensorM = new SensorM(this);

        // 센서 값을 이 컨텍스트에서 받아볼 수 있도록 리스너를 등록한다.
        sensorM.sensorStart();
        /*************************************************** 비콘 관련 **************************************************************/
        // 비콘 매니저를 생성해서 비콘 관리용 클래스에 넣어줌
        beaconM = new BeaconM(new BeaconManager(this), sensorM);
        beaconM.SetContext(this);
        beaconM.BeaconSetListner();
        beaconM.BeaconConnect();

        mAdapter = NfcAdapter.getDefaultAdapter(this);

        if(mAdapter == null){
            Toast.makeText(getApplicationContext(), "사용하기 전에 NFC 활성화하세요", Toast.LENGTH_LONG);
        }else{
            Toast.makeText(getApplicationContext(), "NFC 태그를 스캔하세요", Toast.LENGTH_LONG);
        }

        Intent targetIntent = new Intent(getApplicationContext(), WebViewMain.class);

        targetIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        mPendingIntent = PendingIntent.getActivity(this, 0, targetIntent, 0);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);

        try {
            ndef.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }

        mFilters = new IntentFilter[] {ndef ,};
        mTechLists = new String[][] { new String[] {NfcF.class.getName()}};

    }

    public void setCoupon(JsonObject json) {
        webView.loadUrl("javascript:coupon('"+ json +"')");
    }

    public void setTile(JsonObject tileJson) {
        // 여기로 타일 데이터 넘어옴
        // 형식은 : {"TILE_CRDNT_X":0,"TILE_CODE":128,"TILE_CRDNT_Y":9,"TILE_NM":"A_0_9","DRW_CODE":4}
        if(tileJson != null){

            if(webView.getUrl().equals("file:///android_asset/productDetail.html")){
                webView.loadUrl("javascript:comeBeacon(" + tileJson + ")");
                Log.d("dd", tileJson.toString());
            }

        }

        Log.d("url", webView.getUrl());

    }

    @Override
    public void onBackPressed() {
        long tempTime        = System.currentTimeMillis();
        long intervalTime    = tempTime - backPressedTime;

        if (webView.canGoBack()) {

            webView.goBack();

            if(getIntent().getExtras() != null){
                getIntent().removeExtra("coupon");
            }

        }

        if(webView.canGoBack() == false){

            if ( 0 <= intervalTime && FINSH_INTERVAL_TIME >= intervalTime ) {


                //다이아로그박스 출력
                new AlertDialog.Builder(this)
                        .setTitle("프로그램 종료")
                        .setMessage("프로그램을 종료하시겠습니까?")
                        .setPositiveButton("예", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                beaconM.onDestroy();
                                editor = sp.edit();
                                editor.remove("user_id");
                                moveTaskToBack(true);
                                Process.killProcess(Process.myPid());

                            }
                        })
                        .setNegativeButton("아니오",  null).show();

            }
            else {
                backPressedTime = tempTime;
            }

        }

    }

    @Override
    public void onNewIntent(Intent intent){
        String action = intent.getAction();

        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        Parcelable[] data = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

        if(data != null){
            for(int i = 0; i < data.length; i++){
                NdefRecord[] recs = ((NdefMessage)data[i]).getRecords();

                for(int j = 0; j < recs.length; j++){
                    if(recs[j].getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(recs[j].getType(), NdefRecord.RTD_TEXT)){

                        byte[] payload = recs[j].getPayload();
                        String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
                        int langCodeLen = payload[0] & 0077;

                        String s = new String(payload, langCodeLen+1, payload.length - langCodeLen -1);

                        Log.d( "message Come", s);

                        sp = getSharedPreferences("test", 0);
                        String user_id  = sp.getString("user_id", "");
                        bhf_code = sp.getString("bhf_code", "2");

                        JSONObject json = new JSONObject();
                        try {
                            json.put("goods_code", s);
                            json.put("bhf_code", bhf_code);
                            json.put("user_id", user_id);

                            NetworkTask2 networkTask2 = new NetworkTask2();
                            networkTask2.execute(json.toString());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }






                    }
                }
            }
        }

    }

    @Override
    public  void onResume(){

        super.onResume();

        if(mAdapter != null){
            mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
        }

    }

    @Override
    public  void onPause(){
        super.onPause();

        if(mAdapter != null){
            mAdapter.disableForegroundDispatch(this);
        }
    }
    class WebViewClientTest extends WebViewClient{
        @Override
        public void onPageFinished(WebView view, String url){

            super.onPageFinished(view, url);

            sp = getSharedPreferences("test", 0);
            str = sp.getString("user_id", "");

            point = sp.getString("point", "0");

            String bhf_code = sp.getString("bhf_code", "2");

            webView.loadUrl("javascript:setId('"+str+"', '"+point+"', '"+bhf_code+"')");

            if(getIntent().getExtras() != null){

                coupon = getIntent().getExtras().getString("coupon").toString();

                webView.loadUrl("javascript:couponHere("+ coupon +")");



            }else {
                Log.d("log", "no");
            }


        }
    }

    private class NetworkTask extends AsyncTask<String, String, String> {

        protected  void onPreExcute(){

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... json) {
            // bean 안에 있는 ip 셋팅 정보를 꼭 바꾸도록 할 것
            HttpClient.Builder http = new HttpClient.Builder("POST", IPSetting.getIpAddress() + "productSearch");


            http.addOrReplace("json", json[0]);

            Log.d("param", json[0]);

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

        Log.i("json", s);
            webView.loadUrl("javascript:searchProduct('" + s + "')");

        }
    }




    private class NetworkTask2 extends AsyncTask<String, String, String> {

        protected  void onPreExcute(){

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... json) {
            // bean 안에 있는 ip 셋팅 정보를 꼭 바꾸도록 할 것
            HttpClient.Builder http = new HttpClient.Builder("POST", IPSetting.getIpAddress() + "oneBasketInfo");

            http.addOrReplace("basket", json[0]);

            Log.d("param", json[0]);

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

            Log.i("basketInfo", s);

            webView.loadUrl("file:///android_asset/productBasket.html");


        }
    }


    private class AndroidBridge {

        @JavascriptInterface
        public void setMap() {
            handler.post(new Runnable() {
                public void run() {
                    String tileStr = sp.getString("standingTile", "");
                    JsonObject tileJson = new Gson().fromJson(tileStr, JsonObject.class);
                    try {
                        Log.d("WebViewMain: ", "standingtile: " + tileStr);
                        Log.d("WebViewMain: ", "standingtile: " + tileJson.toString());
                        setTile(tileJson);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        }

    }
}

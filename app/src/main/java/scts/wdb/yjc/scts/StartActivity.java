package scts.wdb.yjc.scts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

public class StartActivity extends AppCompatActivity {

    private TimerTask timerTask;
    private Timer timer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

       timerTask = new TimerTask() {
           @Override
           public void run() {
               Intent intent = new Intent(getApplicationContext(), MainActivity.class);
               startActivity(intent);
           }
       };

       timer = new Timer();
        timer.schedule(timerTask, 500);


    }
}

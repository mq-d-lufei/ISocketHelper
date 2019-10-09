package com.ivt.sockethelper;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    int count = 0;

    private Runnable r = new Runnable() {
        @Override
        public void run() {
            Log.e("MainActivity", "Runnable: " + count++);
        }
    };

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {

        for (int i = 0; i < 500; i++) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.e("MainActivity", "Runnable: " + count++);
                }
            });
        }
    }

}

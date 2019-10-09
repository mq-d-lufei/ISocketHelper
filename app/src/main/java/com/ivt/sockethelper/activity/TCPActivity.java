package com.ivt.sockethelper.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.ivt.sockethelper.R;
import com.ivt.sockethelper.tcp.manager.HeartBeatSocketManager;

public class TCPActivity extends AppCompatActivity {

    private Button connectBt;
    private Button disconnectBt;

    private HeartBeatSocketManager heartBeatSocketManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tcp);

        heartBeatSocketManager = new HeartBeatSocketManager();

        connectBt = (Button) findViewById(R.id.connect);
        disconnectBt = (Button) findViewById(R.id.disconnect);

        connectBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                heartBeatSocketManager.onCreate();
            }
        });

        disconnectBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                heartBeatSocketManager.onDestory();
            }
        });

    }

}

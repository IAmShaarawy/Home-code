package com.nsapps.smarthome;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SystemsActivity extends AppCompatActivity {

    private int systemPosition;
    private Socket mSocket;
    private JSONObject system = new JSONObject();
    private TextView status;
    private RelativeLayout RL;
    private boolean isActive=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_systems);

        String IP = "192.168.1.50";
        try {
            mSocket = IO.socket("http://"+IP+":3000/systems");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        systemPosition=getIntent().getIntExtra("system",0);

        status = (TextView) findViewById(R.id.textView);

        RL = (RelativeLayout) findViewById(R.id.sysLay);

        status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isActive = !isActive;
                change(isActive);
                JSONObject jo = new JSONObject();
                if(isActive){
                    try {
                        jo.put("id",system.getString("_id"));
                        jo.put("value","on");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mSocket.emit("systems/switch",jo);
                }
                else {
                    try {
                        jo.put("id",system.getString("_id"));
                        jo.put("value","off");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mSocket.emit("systems/switch",jo);
                }

            }
        });

    }


    private Emitter.Listener systemsHandler = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        system = ((JSONArray) args[0]).getJSONObject(systemPosition);
                        if (system.getString("status").equals("off")){
                            isActive=false;
                            change(isActive);
                        }
                        else if (system.getString("status").equals("on")){
                            isActive=true;
                            change(isActive);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener updateHandler = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        if (data.getString("value").equals("on")){
                            change(true);
                        }else {
                            change(false);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
    @Override
    protected void onStart() {
        super.onStart();
        mSocket.connect();
        mSocket.on("systems",systemsHandler);
        mSocket.on("systems/update",updateHandler);


    }
    @Override
    protected void onStop() {
        super.onStop();
        mSocket.disconnect();
    }

    private void change(boolean active){
        if (!active){
            RL.setBackgroundColor(getResources().getColor(R.color.off));
            status.setText("[ Tap to Activate ]");
        }
        else {
            RL.setBackgroundColor(getResources().getColor(R.color.on));
            status.setText("Active");
        }
    }
}


package com.nsapps.smarthome;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ListActivity extends AppCompatActivity {

    ListView listView;
    private CustomAdapter mAdapter;

    int intentExtra;
    JSONObject data= null;

    private Socket mSocket;

    private LinearLayout containerLayout;
    private ViewGroup.LayoutParams subContainerLayoutParams, rulerLayoutParams, headerLayoutParameters, thingLayoutParams, thingNameLayout, thingSwitchLayout, thingValueLayout;


    final HashMap<Integer, String> ids = new HashMap<Integer, String>();
    final HashMap<String, Integer> mirroIds = new HashMap<String, Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        try {
            mSocket =  IO.socket("http://192.168.1.50:3000/rooms");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        mSocket.connect();
        mSocket.on("rooms",roomsHandler);
        mSocket.on("rooms/update", switchHandler);
        mSocket.on("sensors/update", sensorHandler);

        containerLayout = (LinearLayout) findViewById(R.id.container);
        subContainerLayoutParams = new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        rulerLayoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 4);
        thingLayoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        headerLayoutParameters = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        thingNameLayout = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.75f);
        thingSwitchLayout = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.25f);
        thingValueLayout = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.25f);

//        listView = (ListView) findViewById(R.id.list_listView);
        intentExtra = getIntent().getIntExtra("data",0);



//        try {
//            //lights - devices - windows - curtains - doors - sensors
//            JSONArray lightsJsonArray= responseJsonObject.getJSONArray("lights");
//
//            JSONArray devicesJsonArray=responseJsonObject.getJSONArray("devices");
//
//            JSONObject itemJsonObject = new JSONObject();
//            itemJsonObject.put("title", "device1");
//            itemJsonObject.put("status", "");
//            itemJsonObject.put("isOn", true);
//            devicesJsonArray.put(itemJsonObject);
//
//            itemJsonObject = new JSONObject();
//            itemJsonObject.put("title", "device2");
//            itemJsonObject.put("status", "");
//            itemJsonObject.put("isOn", false);
//            devicesJsonArray.put(itemJsonObject);
//
//            itemJsonObject = new JSONObject();
//            itemJsonObject.put("title", "device3");
//            itemJsonObject.put("status", "");
//            itemJsonObject.put("isOn", true);
//            devicesJsonArray.put(itemJsonObject);
//
//            responseJsonObject.put("devices", devicesJsonArray);
//
//            JSONArray windowsJsonArray=new JSONArray();
//            responseJsonObject.put("windows", windowsJsonArray);
//
//
//            JSONArray curtainsJsonArray=new JSONArray();
//            responseJsonObject.put("curtains", curtainsJsonArray);
//
//            JSONArray doorsJsonArray=new JSONArray();
//            itemJsonObject = new JSONObject();
//            itemJsonObject.put("title", "door1");
//            itemJsonObject.put("status", "");
//            itemJsonObject.put("isOn", false);
//            doorsJsonArray.put(itemJsonObject);
//
//            itemJsonObject = new JSONObject();
//            itemJsonObject.put("title", "door2");
//            itemJsonObject.put("status", "");
//            itemJsonObject.put("isOn", true);
//            doorsJsonArray.put(itemJsonObject);
//
//            responseJsonObject.put("doors", doorsJsonArray);
//
//            JSONArray sensorsJsonArray=new JSONArray();
//            itemJsonObject = new JSONObject();
//            itemJsonObject.put("title", "sensors1");
//            itemJsonObject.put("status", "On");
//            sensorsJsonArray.put(itemJsonObject);
//
//            itemJsonObject = new JSONObject();
//            itemJsonObject.put("title", "sensors2");
//            itemJsonObject.put("status", "Off");
//            sensorsJsonArray.put(itemJsonObject);
//
//            responseJsonObject.put("sensors", sensorsJsonArray);
//        }catch (Exception e){}

//        mAdapter = new CustomAdapter(this,socket);

//        JSONArray sectionsJsonArray = responseJsonObject.names();
//
//        for(int i=0; i<sectionsJsonArray.length(); i++){
//            try {
//                String sectionName=sectionsJsonArray.getString(i);
//                if (sectionName.equals("_id")||sectionName.equals("name")||sectionName.equals("address")||
//                        sectionName.equals("value")){
//                    continue;
//                }
//                mAdapter.addSectionHeaderItem(sectionName);
//
//                JSONArray sectionItemsJsonArray=responseJsonObject.getJSONArray(sectionName);
//                for(int j=0; j<sectionItemsJsonArray.length(); j++) {
//                    mAdapter.addItem(sectionItemsJsonArray.getJSONObject(j));
//                }
//            } catch (JSONException e) {}
//        }
//        listView.setAdapter(mAdapter);
    }
    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
    }

    private Emitter.Listener roomsHandler = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    try {
                        data = ((JSONArray) args[0]).getJSONObject(intentExtra);
                        Log.i("elshaarawy",data.toString());
                        Load();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener switchHandler = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final JSONObject data;
                    try {
                        data = new JSONObject(args[0].toString());
                        updateSwitch(data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        }
    };

    private Emitter.Listener sensorHandler = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final JSONObject data;
                    try {
                        data = new JSONObject(args[0].toString());
                        updateSensor(data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };


    private void Load() throws JSONException {
        JSONObject room1 = data;
        JSONArray keys = room1.names();
        LinearLayout.LayoutParams  LL = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.setTitle(room1.getString("name"));
        for (int i = 0 ; i<keys.length();i++){
            String keyName = keys.getString(i);

            if (keyName.equals("_id")||keyName.equals("name")||keyName.equals("address")||
                    keyName.equals("value")){
                continue;
            }
            JSONArray category = room1.getJSONArray(keyName);
            LinearLayout subContainer = new LinearLayout(this);
            subContainer.setPadding(0,0,0,40);
            subContainer.setLayoutParams(subContainerLayoutParams);
            subContainer.setOrientation(LinearLayout.VERTICAL);


            TextView headerTV = new TextView(this);


            headerTV.setLayoutParams(headerLayoutParameters);
            headerTV.setTextSize(TypedValue.COMPLEX_UNIT_SP,15.0f);
            headerTV.setAllCaps(true);
            headerTV.setTypeface(Typeface.DEFAULT_BOLD);
            headerTV.setPadding(50,0,0,5);
            headerTV.setText(keyName);

            LinearLayout ruler = new LinearLayout(this);
            ruler.setLayoutParams(rulerLayoutParams);

            ruler.setBackgroundColor(getResources().getColor(R.color.gray));


            subContainer.addView(headerTV);
            subContainer.addView(ruler);

            String tempId;
            int id;
            for(int j = 0 ; j < category.length() ; j++){
                JSONObject thingObject = category.getJSONObject(j);
                LinearLayout thingLinearLayout = new LinearLayout(this);
                thingLinearLayout.setLayoutParams(thingLayoutParams);
                thingLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

                TextView thingName = new TextView(this);
                thingName.setLayoutParams(thingNameLayout);
                thingName.setTextSize(TypedValue.COMPLEX_UNIT_SP,15.0f);
                thingName.setPadding(80,10,0,10);
                thingName.setText(thingObject.getString("name"));

                thingLinearLayout.addView(thingName);

                tempId = String.valueOf(j+1)+String.valueOf(i+1);
                id = Integer.parseInt(tempId);
                ids.put(id,thingObject.getString("_id"));
                mirroIds.put(thingObject.getString("_id"),id);






                switch (thingObject.getString("status")){
                    case "on":
                        Switch thingSwitch1 = new Switch(this);
                        thingSwitch1.setId(id);
                        thingSwitch1.setLayoutParams(thingSwitchLayout);
                        thingSwitch1.setChecked(true);
                        thingLinearLayout.addView(thingSwitch1);
                        SWListner(thingSwitch1,id);

                        break;
                    case "off":
                        Switch thingSwitch2 = new Switch(this);
                        thingSwitch2.setId(id);
                        thingSwitch2.setLayoutParams(thingSwitchLayout);
                        thingSwitch2.setChecked(false);
                        thingLinearLayout.addView(thingSwitch2);
                        SWListner(thingSwitch2,id);
                        break;
                    default:
                        TextView thingValue = new TextView(this);
                        thingValue.setId(id);
                        thingValue.setLayoutParams(thingValueLayout);
                        thingValue.setText(thingObject.getString("value"));
                        thingLinearLayout.addView(thingValue);
                }


                subContainer.addView(thingLinearLayout);



            }


            containerLayout.addView(subContainer);


        }
    }
    private void SWListner(Switch sw, final int id){
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                JSONObject jo = new JSONObject();
                if (b){
                    try {
                        jo.put("id",ids.get(id));
                        jo.put("value","on");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mSocket.emit("rooms/switch",jo);
                }
                else {
                    try {
                        jo.put("id",ids.get(id));
                        jo.put("value","off");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mSocket.emit("rooms/switch",jo);
                }
            }
        });
    }
    private void updateSwitch(JSONObject data){
        try {
            Switch sw = (Switch) findViewById(mirroIds.get(data.getString("id")));

            if (data.getString("value").equals("on")){
                sw.setChecked(true);
            }
            else {
                sw.setChecked(false);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        catch (Exception e){
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();

        }
    }
    private void updateSensor(JSONObject data){
        try {
            if (mirroIds.get(data.getString("id"))!=null){
                TextView tv = (TextView)findViewById(mirroIds.get(data.getString("id")));
                tv.setText(data.getString("value"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
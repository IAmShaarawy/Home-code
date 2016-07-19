package com.nsapps.smarthome;

/**
 * Created by norha on 7/1/2016.
 */
import java.util.ArrayList;
import java.util.TreeSet;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

class CustomAdapter extends BaseAdapter {
    Socket socket;
    Activity activity;

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SEPARATOR = 1;

    private ArrayList<JSONObject> mData = new ArrayList<>();
    private TreeSet<Integer> sectionHeader = new TreeSet<Integer>();

    private LayoutInflater mInflater;



    public CustomAdapter(Activity activity,Socket socket) {
        mInflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.socket = socket;
        this.activity=activity;
    }

    public void addItem(final JSONObject jsonObject) {
        mData.add(jsonObject);
        notifyDataSetChanged();
    }

    public void addSectionHeaderItem(final String itemTitle) {
        JSONObject itemJsonObject=new JSONObject();
        try {
            itemJsonObject.put("title", itemTitle);
        } catch (JSONException e) {}
        mData.add(itemJsonObject);
        sectionHeader.add(mData.size() - 1);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return sectionHeader.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public JSONObject getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    ViewHolder holder = null;
    public View getView(final int position, View convertView, ViewGroup parent) {
        final int rowType = getItemViewType(position);
        if (convertView == null) {
            holder = new ViewHolder();
            switch (rowType) {
                case TYPE_ITEM:
                    convertView = mInflater.inflate(R.layout.list_item, null);
                    holder.nameTextView = (TextView) convertView.findViewById(R.id.list_item_textView);
                    holder.statusTextView = (TextView) convertView.findViewById(R.id.list_item_status_textView);
                    holder.statusSwitch = (Switch) convertView.findViewById(R.id.list_item_switch);
                    break;
                case TYPE_SEPARATOR:
                    convertView = mInflater.inflate(R.layout.list_item_section, null);
                    holder.nameTextView = (TextView) convertView.findViewById(R.id.list_item_section_text);
                    break;
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final JSONObject jsonObject= mData.get(position);
        String status;

        switch (rowType) {
            case TYPE_ITEM:
                try {
                    holder.nameTextView.setText(jsonObject.getString("name"));
                    status=jsonObject.getString("status");
                    switch (status){
                        case "off":{
                            holder.statusSwitch.setChecked(false);
                            holder.statusTextView.setVisibility(View.GONE);
                            holder.statusSwitch.setVisibility(View.VISIBLE);

                        }
                        break;
                        case "on":{
                            holder.statusSwitch.setChecked(true);
                            holder.statusTextView.setVisibility(View.GONE);
                            holder.statusSwitch.setVisibility(View.VISIBLE);
                        }
                        break;
                        default:{
                            holder.statusTextView.setText(jsonObject.getString("value"));
                            holder.statusTextView.setVisibility(View.VISIBLE);
                            holder.statusSwitch.setVisibility(View.GONE);
                        }
                        break;
                    }

                    if (status!=null){
                        holder.statusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean b) {
                                JSONObject jo = new JSONObject();
                                if (b){
                                    try {
                                        jo.put("id",jsonObject.getString("_id"));
                                        jo.put("value","on");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    socket.emit("rooms/switch",jo);
                                }
                                else {
                                    try {
                                        jo.put("id",jsonObject.getString("_id"));
                                        jo.put("value","off");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    socket.emit("rooms/switch",jo);
                                }
                            }
                        });
                    }
                } catch (JSONException e) {}
                break;
            case TYPE_SEPARATOR:
                try {
                    holder.nameTextView.setText(jsonObject.getString("title"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }

//        Emitter.Listener eventsHandler = new Emitter.Listener(){
//            @Override
//            public void call(final Object... args) {
//                activity.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        JSONObject data = (JSONObject) args[0];
//                        boolean listenerStatus;
//                        try {
//                            listenerStatus =
//                                    data.getString("value").equals("on")?true:false;
//                            if(getItem(position).getString("_id").equals(data.getString("id"))){
//                                holder.statusSwitch.setChecked(listenerStatus);
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//            }
//        };
//        socket.on("rooms/update",eventsHandler);
        return convertView;
    }




    public static class ViewHolder {
        public TextView nameTextView, statusTextView;
        public Switch statusSwitch;
    }
}
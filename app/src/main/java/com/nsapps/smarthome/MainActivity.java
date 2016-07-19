package com.nsapps.smarthome;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int TYPE_ROOMS=1;
    private static final int TYPE_OUTDOORS=2;
    private static final int TYPE_SYSTEMS=3;
    private static final int TYPE_ROUTINES=4;
    private int selectedType=1;

    private ListView listView;

    private JSONArray rooms = new JSONArray();
    private JSONArray routines = new JSONArray();
    private JSONArray systems = new JSONArray();

    private HashMap<Integer,String> routinesId = new HashMap<Integer, String>();
    //elshaarawy starts
    private Socket socketRooms,socketRoutines,socketSystems;

    @Override
    protected void onStart() {
        String IP = "192.168.1.50";
        super.onStart();
        try {
            socketRooms = IO.socket("http://"+IP+":3000/rooms");
            socketRoutines = IO.socket("http://"+IP+":3000/routines");
            socketSystems = IO.socket("http://"+IP+":3000/systems");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        socketRooms.connect();
        socketRoutines.connect();
        socketSystems.connect();

        socketRooms.on("rooms",roomsHandler);
        socketRoutines.on("routines",routinesHandler);
        socketSystems.on("systems",systemsHandler);


    }

    @Override
    protected void onStop() {
        super.onStop();
        socketRooms.disconnect();
        socketRoutines.disconnect();
        socketSystems.disconnect();

    }

    private Emitter.Listener roomsHandler = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    rooms = (JSONArray) args[0];
                    try {
                        loadRooms();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener routinesHandler = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    routines = (JSONArray) args[0];
                }
            });
        }
    };

    private Emitter.Listener systemsHandler = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            systems = (JSONArray) args[0];
        }
    };

    //elshaarawy ends
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //elshaarawy starts

        //elshaarawy ends

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        listView=(ListView)findViewById(R.id.main_listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(selectedType==TYPE_ROOMS) {
                    Intent intent = new Intent(MainActivity.this, ListActivity.class);

                    intent.putExtra("data",position);
                    startActivity(intent);
                }else if (selectedType==TYPE_ROUTINES){

                    JSONObject sentData = new JSONObject();
                    try {
                        sentData.put("id",routinesId.get(position));
                        socketRoutines.emit("routines/start",sentData);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else if(selectedType==TYPE_SYSTEMS){
                    Intent intent = new Intent(MainActivity.this,SystemsActivity.class);

                    intent.putExtra("system",position);
                    startActivity(intent);

                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_rooms) {
            // Handle the camera action
            selectedType=TYPE_ROOMS;
            try {
                loadRooms();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (id == R.id.nav_systems) {
            selectedType=TYPE_SYSTEMS;
            try {
                loadSystems();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (id == R.id.nav_routines) {
            selectedType=TYPE_ROUTINES;
            try {
                loadRoutines();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    public void loadRooms() throws JSONException {

        ArrayList roomsArrayList =new ArrayList();
        for (int i =0;i<rooms.length();i++){
            JSONObject roomObject = rooms.getJSONObject(i);
            roomsArrayList.add(roomObject);
        }

        ArrayAdapter ItemsAdapter=new ArrayAdapter<JSONObject>(this, R.layout.main_list_item, roomsArrayList){
            class ViewHolder{
                TextView titleTextView;
                ImageView logoImageView;
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                final JSONObject itemJsonObject = getItem(position);
                ViewHolder viewHolder;
                if (convertView == null) {
                    viewHolder = new ViewHolder();
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.main_list_item, parent, false);
                    viewHolder.titleTextView=(TextView) convertView.findViewById(R.id.main_list_item_name_textView);
                    viewHolder.logoImageView=(ImageView) convertView.findViewById(R.id.main_list_item_logo_imageView);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }
                try {
                    viewHolder.titleTextView.setText(itemJsonObject.getString("name"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return convertView;
            }
        };
        listView.setAdapter(ItemsAdapter);
    }

    public void loadSystems() throws JSONException {

        ArrayList loadSystemsArrayList =new ArrayList();
        for (int i = 0; i<systems.length();i++){
            JSONObject system = systems.getJSONObject(i);
            loadSystemsArrayList.add(system);
        }

        ArrayAdapter ItemsAdapter=new ArrayAdapter<JSONObject>(this, R.layout.main_list_item, loadSystemsArrayList){
            class ViewHolder{
                TextView titleTextView;
                ImageView logoImageView;
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                final JSONObject itemJsonObject = getItem(position);
                ViewHolder viewHolder;
                if (convertView == null) {
                    viewHolder = new ViewHolder();
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.main_list_item, parent, false);
                    viewHolder.titleTextView=(TextView) convertView.findViewById(R.id.main_list_item_name_textView);
                    viewHolder.logoImageView=(ImageView) convertView.findViewById(R.id.main_list_item_logo_imageView);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }
                try {
                    viewHolder.titleTextView.setText(itemJsonObject.getString("name"));
                    if (itemJsonObject.get("status").equals("off")){
                        viewHolder.titleTextView.setTextColor(getResources().getColor(R.color.off));
                    }
                    else {
                        viewHolder.titleTextView.setTextColor(getResources().getColor(R.color.on));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return convertView;
            }
        };
        onBackPressed();
        listView.setAdapter(ItemsAdapter);


    }

    public void loadRoutines() throws JSONException {

        ArrayList loadRoutinesArrayList =new ArrayList();
        for (int i = 0 ; i< routines.length(); i++){
            loadRoutinesArrayList.add(routines.getJSONObject(i));
            routinesId.put(i,routines.getJSONObject(i).getString("_id"));
        }

        ArrayAdapter ItemsAdapter=new ArrayAdapter<JSONObject>(this, R.layout.main_list_item, loadRoutinesArrayList){
            class ViewHolder{
                TextView titleTextView;
                ImageView logoImageView;
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                final JSONObject itemJsonObject = getItem(position);
                ViewHolder viewHolder;
                if (convertView == null) {
                    viewHolder = new ViewHolder();
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.main_list_item, parent, false);
                    viewHolder.titleTextView=(TextView) convertView.findViewById(R.id.main_list_item_name_textView);
                    viewHolder.logoImageView=(ImageView) convertView.findViewById(R.id.main_list_item_logo_imageView);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }
                try {
                    viewHolder.titleTextView.setText(itemJsonObject.getString("name"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return convertView;
            }
        };
        listView.setAdapter(ItemsAdapter);
    }

}
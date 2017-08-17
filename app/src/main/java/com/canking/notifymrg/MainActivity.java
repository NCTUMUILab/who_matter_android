package com.canking.notifymrg;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;
import android.telephony.TelephonyManager;
import android.content.SharedPreferences;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;
import org.json.JSONException;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";

    final static String UPDATE = "com.baidu.notifymgr.NOTIFICATION_LISTENER_EXAMPLE";
    final static String EVENT = "notification_event";
    final static String ICON_S = "small_icon";
    final static String VIEW_S = "view_small";
    final static String View_L = "view_large";

    private NotificationReceiver nReceiver;
    private Button mStart, mStop, mSaveName;
    private SharedPreferences sharedPreferences;
    private EditText mName;
    private TextView mCurrentName;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("data" , MODE_PRIVATE);
        initView();
        initData();

    }

    private void initView() {
        mStart = (Button) findViewById(R.id.start);
        mStop = (Button) findViewById(R.id.stop);
        mName   = (EditText) findViewById(R.id.name);
        mSaveName = (Button) findViewById(R.id.save_name);
        mCurrentName = (TextView) findViewById(R.id.current_name);

        String name = sharedPreferences.getString("name", null);
        mName.setText(name);
        mCurrentName.setText(name);

        mStop.setOnClickListener(this);
        mStart.setOnClickListener(this);
        mSaveName.setOnClickListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void initData() {
        nReceiver = new NotificationReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPDATE);
        registerReceiver(nReceiver, filter);
    }

    @Override
    public void onClick(View v) {
        if (v == mStart) {
            if (!isEnabled()) {
                Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                startActivity(intent);
            } else {
                Toast.makeText(this, "Start retrieve notification", Toast.LENGTH_LONG).show();
            }
        } else if (v == mStop) {
            Intent s = new Intent(this, NLService.class);
            stopService(s);
        } else if (v == mSaveName) {
            String name = mName.getText().toString();
            sharedPreferences.edit().putString("name", name).apply();
            mCurrentName.setText(name);
            Toast.makeText(this, "Name Set", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(nReceiver);
    }


    class NotificationReceiver extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle budle = intent.getExtras();
            JSONObject notification_json = new JSONObject();
            Set<String> keys = budle.keySet();
            for (String key : keys) {
                try {
                    notification_json.put(key, budle.get(key));
                } catch(JSONException e) {
                }
            }
            try {
                notification_json.put("name", sharedPreferences.getString("name" , "null"));
            } catch (JSONException e) {

            }

            Log.e("json", notification_json.toString());

        }

    }

    private boolean isEnabled() {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}

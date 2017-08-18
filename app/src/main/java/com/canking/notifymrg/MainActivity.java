package com.canking.notifymrg;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import java.security.SecureRandom;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.json.JSONObject;
import org.json.JSONException;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";

    final static String UPDATE = "com.baidu.notifymgr.NOTIFICATION_LISTENER_EXAMPLE";
    final static String EVENT = "notification_event";
    final static String ICON_S = "small_icon";
    final static String VIEW_S = "view_small";
    final static String View_L = "view_large";

    private NotificationReceiver nReceiver;
    private Button mStart, mStop, mSaveName, mConnectLine;
    private SharedPreferences sharedPreferences;
    private EditText mName;
    private TextView mCurrentName;
    private TextView mCurrentUUID;
    private String name;
    private String uuid;

    private SensorManager mSensorManager;
    private Sensor mSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        initView();
        initData();
        initSensor();
    }


    private void initSensor() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);

        Log.e("sensor", deviceSensors.toString());
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            Log.e("Sensor", "Get MAGNETIC_FIELD");
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        } else {
            Log.e("Sensor", "No MAGNETIC_FIELD");
        }
    }

    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();

    String randomString( int len ){
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
    }

    private void initView() {
        mStart = (Button) findViewById(R.id.start);
        mStop = (Button) findViewById(R.id.stop);
        mName = (EditText) findViewById(R.id.name);
        mSaveName = (Button) findViewById(R.id.save_name);
        mCurrentName = (TextView) findViewById(R.id.current_name);
        mCurrentUUID = (TextView) findViewById(R.id.uuid);
        mConnectLine = (Button) findViewById(R.id.connect_line);

        name = sharedPreferences.getString("name", null);
        uuid = sharedPreferences.getString("uuid", null);
        mName.setText(name);
        mCurrentName.setText(name);
        if(uuid != null) {
            mCurrentUUID.setText(uuid);
        }
        else {
            uuid = randomString(6);
            sharedPreferences.edit().putString("uuid", uuid).apply();
            mCurrentUUID.setText(uuid);
            Toast.makeText(this, "UUID Generated!", Toast.LENGTH_LONG).show();
        }

        mStop.setOnClickListener(this);
        mStart.setOnClickListener(this);
        mSaveName.setOnClickListener(this);
        mConnectLine.setOnClickListener(this);

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
        } else if (v == mConnectLine) {
            String url = String.format("https://notify-bot.line.me/oauth/authorize?response_type=code&client_id=DaVZq9zBmUGSlS4e9nLdkN&redirect_uri=http://mini.kevin.nctu.me:5555/line/callback/?uuid=%s&scope=notify&state=NO_STATE&test=gg", uuid, name);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
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
            String noti_uuid = randomString(32);
            String result;
            for (String key : keys) {
                try {
                    notification_json.put(key, budle.get(key));
                } catch (JSONException e) {
                }
            }
            try {
                notification_json.put("name", name);
                notification_json.put("uuid", uuid);
                notification_json.put("noti_uuid", noti_uuid);
            } catch (JSONException e) {

            }

            String json = notification_json.toString();

            try{
                post_result(json);
            } catch (Exception e) {

            }
//

            Log.e("json", notification_json.toString());

        }

    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

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

    private final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");


    public void post_result(String data) throws Exception {
        RequestBody body = RequestBody.create(JSON, data);
        Request request = new Request.Builder()
                .url("http://mini.kevin.nctu.me:5555/result/")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                Headers responseHeaders = response.headers();
                for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                    System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
                }

                System.out.println(response.body().string());
            }
        });
    }

}

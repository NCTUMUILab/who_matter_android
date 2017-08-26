package com.canking.notifymrg;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.os.Build;
import android.os.Bundle;



public class NLService extends NotificationListenerService {
    public final static String COMMAND = "com.canking.COMMAND_NOTIFICATION_LISTENER_SERVICE";
    public final static String COMMAND_EXTRA = "command";
    public final static String CANCEL_ALL = "clearall";
    public final static String GET_LIST = "list";

    private String TAG = "NLService";
    private NLServiceReceiver nlservicereciver;

    @Override
    public void onCreate() {
        super.onCreate();
        nlservicereciver = new NLServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(COMMAND);
        registerReceiver(nlservicereciver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(nlservicereciver);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i(TAG, "ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());

        Notification notification = sbn.getNotification();
        Intent i = new Intent(MainActivity.UPDATE);
        i.putExtra("event", "posted");
        i.putExtra("app", sbn.getPackageName());
        try {
            i.putExtra("title", notification.extras.get("android.title").toString());
        } catch (Exception e){
            i.putExtra("title", "");
        }
        try {
            i.putExtra("text", notification.extras.get("android.text").toString());
        } catch (Exception e){
            i.putExtra("text", "");
        }

        sendBroadcast(i);
        onBounReveive(sbn);
    }

    private void onBounReveive(StatusBarNotification sbn) {
        Notification notification = sbn.getNotification();
        String pkg = sbn.getPackageName();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG, "ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());
        Intent i = new Intent(MainActivity.UPDATE);
        Notification notification = sbn.getNotification();
        i.putExtra("event", "removed");
        i.putExtra("app", sbn.getPackageName());
        try {
            i.putExtra("title", notification.extras.get("android.title").toString());
        } catch (Exception e){
            i.putExtra("title", "");
        }
        try {
            i.putExtra("text", notification.extras.get("android.text").toString());
        } catch (Exception e){
            i.putExtra("text", "");
        }
        sendBroadcast(i);
    }

    class NLServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String command = intent.getStringExtra(COMMAND_EXTRA);
            Log.e(TAG, "Command receive:" + command);

            if (command.equals(CANCEL_ALL)) {
                NLService.this.cancelAllNotifications();
            } else if (command.equals(GET_LIST)) {
                int i = 1;
                for (StatusBarNotification sbn : NLService.this.getActiveNotifications()) {
                    Intent i2 = new Intent(MainActivity.UPDATE);
                    i2.putExtra(MainActivity.EVENT, i + " " + sbn.getPackageName() + "\n");
                    Notification notification = sbn.getNotification();
                    i2.putExtras(notification.extras);
                    i2.putExtra(MainActivity.VIEW_S, notification.contentView);
                    i2.putExtra(MainActivity.View_L, notification.bigContentView);
                    sendBroadcast(i2);
                    i++;
                }
            }

        }
    }


}

package com.example.test;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

// keep running in the background
public class KeepRunning extends Service {

    public static Context context;
    public static String current_android_id;

    public KeepRunning() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // do your jobs here
        context = getApplicationContext();
        current_android_id = getDeviceId((context));
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

        new Thread(new Runnable() {
            @Override
            public void run() {
//                  EventBus.getDefault().post(new EventCategory(7));
                System.out.println("99999988");//这是定时所执行的任务
                try {
                    insertUsageStats(usageStatsManager);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        int anhour=6*1000;
        long triggerAtMillis = SystemClock.elapsedRealtime()+anhour;

        Intent alarmIntent = new Intent(this,KeepRunning.class);

        PendingIntent pendingIntent = PendingIntent.getService(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {// 6.0
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//  4.4
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtMillis, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtMillis, pendingIntent);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public static String getDeviceId(Context context) {
        String id = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return id;
    }

    public static String getAppName(Context context, String pname){
        ApplicationInfo appInfo;
        try {
            appInfo = context.getPackageManager().getApplicationInfo( pname, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            appInfo = null;
        }
        final String applicationName = (String) (appInfo != null ? context.getPackageManager().getApplicationLabel(appInfo) : "(unknown)");

        return applicationName;
    }

    public static String stampToDate(long time) {
        Date date = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss "); // the format of your date
        //sdf.setTimeZone(TimeZone.getTimeZone("GMT-4"));

        return sdf.format(date);
    }

    public String insertUsageStats(UsageStatsManager usageStatsManager) throws Exception {
        String packageName = null;
        int counter = 0;
        long useTime = 0;
        long duration = 0;
        ArrayList<Integer> result = new ArrayList<>();
        final long INTERVAL = 1000 * 60 * 60 * 72 *0;
        final long end = System.currentTimeMillis();
        final long begin = end - INTERVAL;
        final UsageEvents usageEvents = usageStatsManager.queryEvents(begin, end);
        Log.d("here", String.valueOf(usageEvents));
        while (usageEvents.hasNextEvent()) {
            UsageEvents.Event curEvent = new UsageEvents.Event();
            usageEvents.getNextEvent(curEvent);
            String pname = curEvent.getPackageName();
            Log.d("activity here: ", "[" + pname + "]" + " app name: " + getAppName(context, pname));

            if(curEvent.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED){
                counter++;
                useTime = curEvent.getTimeStamp();
            }
            if(curEvent.getEventType() == UsageEvents.Event.ACTIVITY_PAUSED){
                counter++;
                String appName = getAppName(context, pname);
                duration = curEvent.getTimeStamp() - useTime;
                //result.add(new AppUsageInfoWrapper())
                Log.d("activity paused: ", "[" + appName + "]" + stampToDate(useTime) + "Use time: " + String.valueOf(useTime) + ", duration: " + duration);
                int res = insertData("http://34.216.172.247:3000", stampToDate(useTime), pname, appName, duration);
                Log.d("insert response code", String.valueOf(res));
            }

        }

//        Calendar calendar=Calendar.getInstance();
//        long endt = calendar.getTimeInMillis();//结束时间
//        calendar.add(Calendar.DAY_OF_MONTH, -1);//时间间隔为一个月
//        long statt = calendar.getTimeInMillis();//开始时间
//        UsageStatsManager usageStatsManager2=(UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
//        //获取一个月内的信息
//        List<UsageStats> queryUsageStats = usageStatsManager2.queryUsageStats(UsageStatsManager.INTERVAL_MONTHLY,statt-1000000,endt);
//        Log.d("list of usage stats: ", String.valueOf(queryUsageStats));

        Calendar beginCal = Calendar.getInstance();
        beginCal.add(Calendar.HOUR_OF_DAY, -1);
        Calendar endCal = Calendar.getInstance();
        UsageStatsManager manager = (UsageStatsManager)getApplicationContext().getSystemService(USAGE_STATS_SERVICE);
        List<UsageStats> stats = manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, beginCal.getTimeInMillis(), endCal.getTimeInMillis());
        StringBuilder sb = new StringBuilder();
        for(UsageStats us:stats){
            try {
                PackageManager pm = getApplicationContext().getPackageManager();
                ApplicationInfo applicationInfo = pm.getApplicationInfo(us.getPackageName(),PackageManager.GET_META_DATA);
                if((applicationInfo.flags&applicationInfo.FLAG_SYSTEM) <= 0){
                    SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
                    String t=format.format(new Date(us.getLastTimeUsed()));
                    sb.append(pm.getApplicationLabel(applicationInfo) + "\t" + t + "\t" + us.getTotalTimeInForeground() + "\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d("hhhhhhhhh", sb.toString());

        return packageName;
    }

    public static int insertData(String path, String useTime, String packagename, String appname, long duration) throws Exception {
        URL url = new URL(path + "/app_usage_data");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setRequestMethod("POST");
        conn.setUseCaches(false);
        conn.setDoOutput(true);

        conn.setRequestProperty("Content-Type", "application/json");

        JSONObject param = new JSONObject();
        param.put("time", useTime);
        param.put("android_id", current_android_id);
        param.put("package_name", packagename);
        param.put("app_name", appname);
        param.put("duration_ms", duration);
        conn.connect();

        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8);
        writer.write(param.toString());
        writer.flush();

        //The server isn't waiting for any data from the client, and when the server exits the connection will be closed. So add a ins.readLine() to the server code:
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        System.out.println(in.readLine());

        writer.close();

        return conn.getResponseCode();
    }

}
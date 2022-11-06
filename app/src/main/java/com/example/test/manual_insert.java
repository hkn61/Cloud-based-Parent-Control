package com.example.test;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class manual_insert extends AppCompatActivity {
    EditText startDate, startTime, endDate, endTime;
    public int sYear = -1, sMonth = -1, sDay = -1, sHour = -1, sMinute = -1, eYear = -1, eMonth = -1, eDay = -1, eHour = -1, eMinute = -1;
    Button confirmInsert;
    public static Context context;
    public static String current_android_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_insert);

        startDate = (EditText) findViewById(R.id.insertStartDate);
        startTime = (EditText) findViewById(R.id.insertStartTime);
        endDate = (EditText) findViewById(R.id.insertEndDate);
        endTime = (EditText) findViewById(R.id.insertEndTime);

        confirmInsert = findViewById(R.id.confirmInsert);
        context = getApplicationContext();
        current_android_id = getDeviceId((context));

    }

    @Override
    protected void onStart() {
        super.onStart();
        String path = "http://34.216.172.247:3000";
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        confirmInsert.setOnClickListener(view -> {
            try {
                insertUsageStats(usageStatsManager, path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    public void chooseStartDate(View v) {
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        sYear = c.get(Calendar.YEAR);
        sMonth = c.get(Calendar.MONTH);
        sDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                startDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                sYear = year;
                sMonth = monthOfYear + 1;
                sDay = dayOfMonth;
            }
            }, sYear, sMonth, sDay);

        datePickerDialog.show();
    }

    public void chooseStartTime(View v) {
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        sHour = c.get(Calendar.HOUR_OF_DAY);
        sMinute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String hour = String.format("%02d", hourOfDay);
                String min = String.format("%02d", minute);
                startTime.setText(hour + ":" + min);
                sHour = hourOfDay;
                sMinute = minute;
            }
        }, sHour, sMinute, false);
        timePickerDialog.show();

        Log.d("start hour", Integer.toString(sHour));
        Log.d("start min", Integer.toString(sMinute));
    }

    public void chooseEndDate(View v) {
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        eYear = c.get(Calendar.YEAR);
        eMonth = c.get(Calendar.MONTH);
        eDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                endDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                eYear = year;
                eMonth = monthOfYear + 1;
                eDay = dayOfMonth;
            }
            }, eYear, eMonth, eDay);

        datePickerDialog.show();
    }

    public void chooseEndTime(View v) {
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        eHour = c.get(Calendar.HOUR_OF_DAY);
        eMinute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String hour = String.format("%02d", hourOfDay);
                String min = String.format("%02d", minute);
                endTime.setText(hour + ":" + min);
                eHour = hourOfDay;
                eMinute = minute;
            }
        }, eHour, eMinute, false);
        timePickerDialog.show();
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

    public long beginDateToMillis() throws ParseException {
//        String beginDate = Integer.toString(sYear) + "/" + Integer.toString(sMonth) + "/" + Integer.toString(sDay) + " 00:00:00";
        String beginDate = Integer.toString(sYear) + "/" + Integer.toString(sMonth) + "/" + Integer.toString(sDay) + " " + Integer.toString(sHour) + ":" + Integer.toString(sMinute) + ":00";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = sdf.parse(beginDate);
        long millis = date.getTime();

        return millis;
    }

    public long endDateToMillis() throws ParseException {
//        String endDate = Integer.toString(eYear) + "/" + Integer.toString(eMonth) + "/" + Integer.toString(eDay) + " 00:00:00";
        String endDate = Integer.toString(eYear) + "/" + Integer.toString(eMonth) + "/" + Integer.toString(eDay) + " " + Integer.toString(eHour) + ":" + Integer.toString(eMinute) + ":00";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = sdf.parse(endDate);
        long millis = date.getTime();

        return millis;
    }

    public String insertUsageStats(UsageStatsManager usageStatsManager, String path) throws Exception {
        String packageName = null;
        int counter = 0;
        long useTime = 0;
        long duration = 0;
        ArrayList<Integer> result = new ArrayList<>();
        final long end = endDateToMillis();
        final long begin = beginDateToMillis();
        Log.d("begin date", Long.toString(begin));
        Log.d("end date", Long.toString(end));
        final UsageEvents usageEvents = usageStatsManager.queryEvents(begin, end);
        Log.d("here manual insert", String.valueOf(usageEvents));
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
                int res = insertData(path, stampToDate(useTime), pname, appName, duration);
                Log.d("insert response code", String.valueOf(res));
            }

        }

        removeDuplicate(path);

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

    public static int removeDuplicate(String path) throws Exception {
        Log.d("removing dup", "123");
        URL url = new URL(path + "/rpc/remove_duplicate");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setRequestMethod("POST");
        conn.setUseCaches(false);

        conn.setRequestProperty("Content-Type", "application/json");

        JSONObject param = new JSONObject();
        param.put("device_id", current_android_id);
        Log.d("remove dup code", param.toString());
        conn.connect();

        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8);
        writer.write(param.toString());
        writer.flush();

        Log.d("remove dup code", Integer.toString(conn.getResponseCode()));
        writer.close();

        return conn.getResponseCode();
    }

}
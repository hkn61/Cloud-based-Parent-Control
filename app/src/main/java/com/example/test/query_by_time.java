package com.example.test;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

//import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class query_by_time extends AppCompatActivity {
    public static Context context;
    public static String current_android_id;
    EditText startDate, startTime, endDate, endTime;
    Button confirmButton;
    public int sYear = -1, sMonth = -1, sDay = -1, sHour = -1, sMinute = -1, eYear = -1, eMonth = -1, eDay = -1, eHour = -1, eMinute = -1;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = getApplicationContext();
        current_android_id = getDeviceId((context));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_by_time);

        confirmButton=findViewById(R.id.confirmButton);
        startDate = (EditText) findViewById(R.id.queryStartDate);
        startTime = (EditText) findViewById(R.id.queryStartTime);
        endDate = (EditText) findViewById(R.id.queryEndDate);
        endTime = (EditText) findViewById(R.id.queryEndTime);

//        this.loadStatistics();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStart() {
        super.onStart();
        confirmButton.setOnClickListener(view -> {
            try {
                queryByDate("http://34.216.172.247:3000");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    public static String getDeviceId(Context context) {
        String id = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return id;
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
                Log.d("start monthOfYear", Integer.toString(monthOfYear));
                startDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                sYear = year;
                sMonth = monthOfYear + 1;
                sDay = dayOfMonth;
            }
        }, sYear, sMonth, sDay);

        datePickerDialog.show();
        Log.d("start month", Integer.toString(sMonth));
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String queryByDate(String path) throws Exception {
        String startTimeInput, endTimeInput;
        if(sYear != -1 && sMonth != -1 && sDay != -1 && sHour != -1 && sMinute != -1 && eYear != -1 && eMonth != -1 && eDay != -1 && eHour != -1 && eMinute != -1){
            startTimeInput = Integer.toString(sYear) + "-" + Integer.toString(sMonth) + "-" + Integer.toString(sDay) + "T" + Integer.toString(sHour) + ":" + Integer.toString(sMinute) + ":00";
            endTimeInput = Integer.toString(eYear) + "-" + Integer.toString(eMonth) + "-" + Integer.toString(eDay) + "T" + Integer.toString(eHour) + ":" + Integer.toString(eMinute) + ":00";
        }
        else{
            loadStatistics();
            return "";
        }
        URL url = new URL(path + "/rpc/query_app_usage_by_time");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setUseCaches(false);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        JSONObject param = new JSONObject();
        param.put("device_id", current_android_id);
        param.put("start_time", startTimeInput);
        param.put("end_time", endTimeInput);

        Log.d("req body", param.toString());

        conn.connect();
        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8);
        writer.write(param.toString());
        writer.flush();
        writer.close();
        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Fail to request url! ResponseCode: " + conn.getResponseCode());
        }

//        InputStream inStream = conn.getInputStream();
////        String jsonStr = IOUtils.toString(inStream, "UTF-8");
//        String jsonStr = convertStreamToString(inStream);
//        inStream.close();
//        Log.d("query result", jsonStr);
//        return jsonStr;

        BufferedReader in = new BufferedReader(new InputStreamReader(
                conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // print result
//        System.out.println(response.toString());
        Log.d("query response", response.toString());

        showAppsUsageReport(response.toString());

        return response.toString();
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void loadStatistics() {
        UsageStatsManager usm = (UsageStatsManager) this.getSystemService(USAGE_STATS_SERVICE);
        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  System.currentTimeMillis() - 1000*3600*24,  System.currentTimeMillis());
        appList = appList.stream().filter(app -> app.getTotalTimeInForeground() > 0).collect(Collectors.toList());

        // Group the usageStats by application and sort them by total time in foreground
        if (appList.size() > 0) {
            Map<String, UsageStats> mySortedMap = new TreeMap<>();
            for (UsageStats usageStats : appList) {
                mySortedMap.put(usageStats.getPackageName(), usageStats);
            }
            showAppsUsage(mySortedMap);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void showAppsUsageReport(String postResponse) throws JSONException {
        JSONArray appUsageArray = new JSONArray(postResponse);

        ArrayList<App> appsList = new ArrayList<>();

        // sort the applications by time spent in foreground
//        Collections.sort(usageStatsList, (z1, z2) -> Long.compare(z1.getTotalTimeInForeground(), z2.getTotalTimeInForeground()));

        // get total time of apps usage to calculate the usagePercentage for each app
        long totalTime = 0;

        for (int i = 0; i < appUsageArray.length(); i++){
            JSONObject object = appUsageArray.getJSONObject(i);
            int duration = object.getInt("total_duration");
            long durationLong = duration;
            totalTime += durationLong;
        }
        //fill the appsList
        for (int i = 0; i < appUsageArray.length(); i++) {
            try {
                JSONObject object = appUsageArray.getJSONObject(i);
                String packageName = object.getString("package_name");
                Log.d("pkg name", packageName);
                Drawable icon;
                String appName = object.getString("app_name");


                ApplicationInfo ai = getApplicationContext().getPackageManager().getApplicationInfo(packageName, 0);
                icon = getApplicationContext().getPackageManager().getApplicationIcon(ai);
//                appName = getApplicationContext().getPackageManager().getApplicationLabel(ai).toString();
                int duration = object.getInt("total_duration");

                String usageDuration = Integer.toString(duration) + "ms";
                int usagePercentage = (int) (duration * 100 / totalTime);

                App usageStatDTO = new App(icon, appName, usagePercentage, usageDuration);
                appsList.add(usageStatDTO);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        Collections.sort(appsList, Comparator.comparingLong(z -> z.usagePercentage));
        // reverse the list to get most usage first
        Collections.reverse(appsList);
        // build the adapter
        AppsAdapter adapter = new AppsAdapter(this, appsList);

        // attach the adapter to a ListView
        ListView listView = findViewById(R.id.apps_list);
        listView.setAdapter(adapter);

        showHideItemsWhenShowResults();
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void showAppsUsage(Map<String, UsageStats> mySortedMap) {
        //public void showAppsUsage(List<UsageStats> usageStatsList) {
        ArrayList<App> appsList = new ArrayList<>();
        List<UsageStats> usageStatsList = new ArrayList<>(mySortedMap.values());

        // sort the applications by time spent in foreground
        Collections.sort(usageStatsList, (z1, z2) -> Long.compare(z1.getTotalTimeInForeground(), z2.getTotalTimeInForeground()));

        // get total time of apps usage to calculate the usagePercentage for each app
        long totalTime = usageStatsList.stream().map(UsageStats::getTotalTimeInForeground).mapToLong(Long::longValue).sum();

        //fill the appsList
        for (UsageStats usageStats : usageStatsList) {
            try {
                String packageName = usageStats.getPackageName();
                Log.d("pkg name", packageName);
                Drawable icon;
                String[] packageNames = packageName.split("\\.");
                String appName = packageNames[packageNames.length-1].trim();


                ApplicationInfo ai = getApplicationContext().getPackageManager().getApplicationInfo(packageName, 0);
                icon = getApplicationContext().getPackageManager().getApplicationIcon(ai);
                appName = getApplicationContext().getPackageManager().getApplicationLabel(ai).toString();


                String usageDuration = getDurationBreakdown(usageStats.getTotalTimeInForeground());
                int usagePercentage = (int) (usageStats.getTotalTimeInForeground() * 100 / totalTime);

                App usageStatDTO = new App(icon, appName, usagePercentage, usageDuration);
                appsList.add(usageStatDTO);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }


        // reverse the list to get most usage first
        Collections.reverse(appsList);
        // build the adapter
        AppsAdapter adapter = new AppsAdapter(this, appsList);

        // attach the adapter to a ListView
        ListView listView = findViewById(R.id.apps_list);
        listView.setAdapter(adapter);

        showHideItemsWhenShowResults();
    }

    private String getDurationBreakdown(long millis){
        if(millis<0){
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long hours= TimeUnit.MILLISECONDS.toHours(millis);
        millis-=TimeUnit.HOURS.toMillis(hours);
        long minutes=TimeUnit.MILLISECONDS.toMinutes(millis);
        millis-=TimeUnit.MINUTES.toMillis(minutes);
        long seconds=TimeUnit.MILLISECONDS.toSeconds(millis);

        return(hours+"h"+minutes+"m"+seconds+"s");
    }


    public void showHideItemsWhenShowResults(){
        EditText queryStartDate=findViewById(R.id.queryStartDate);
        EditText queryStartTime=findViewById(R.id.queryStartTime);
        EditText queryEndDate=findViewById(R.id.queryEndDate);
        EditText queryEndTime=findViewById(R.id.queryEndTime);
        TextView startTime=findViewById(R.id.startTime);
        TextView endTime=findViewById(R.id.endTime);
        TextView queryInstruction=findViewById(R.id.queryInstruction);
        ListView appsList=findViewById(R.id.apps_list);

        queryStartDate.setVisibility(View.GONE);
        queryStartTime.setVisibility(View.GONE);
        queryEndDate.setVisibility(View.GONE);
        queryEndTime.setVisibility(View.GONE);
        startTime.setVisibility(View.GONE);
        endTime.setVisibility(View.GONE);
        queryInstruction.setVisibility(View.GONE);
        confirmButton.setVisibility(View.GONE);
        appsList.setVisibility(View.VISIBLE);

    }


}
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
import android.util.Pair;
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

//import com.amazonaws.auth.AWSCredentials;
//import com.amazonaws.auth.AWSStaticCredentialsProvider;
//import com.amazonaws.auth.BasicAWSCredentials;
//import com.amazonaws.regions.Regions;
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.AmazonS3Client;
//import com.amazonaws.services.s3.AmazonS3ClientBuilder;
//import com.amazonaws.services.s3.model.CSVInput;
//import com.amazonaws.services.s3.model.CSVOutput;
//import com.amazonaws.services.s3.model.CompressionType;
//import com.amazonaws.services.s3.model.ExpressionType;
//import com.amazonaws.services.s3.model.InputSerialization;
//import com.amazonaws.services.s3.model.OutputSerialization;
//import com.amazonaws.services.s3.model.SelectObjectContentEvent;
//import com.amazonaws.services.s3.model.SelectObjectContentEventVisitor;
//import com.amazonaws.services.s3.model.SelectObjectContentRequest;
//import com.amazonaws.services.s3.model.SelectObjectContentResult;
//import static com.amazonaws.util.IOUtils.copy;

public class query_by_time extends AppCompatActivity {
    public static Context context;
    public static String current_android_id;
    String startTimeDisplay, endTimeDisplay;
    EditText startDate, startTime, endDate, endTime;
    TextView usageStatHeader;
    Button confirmButton;
    public int sYear = -1, sMonth = -1, sDay = -1, sHour = -1, sMinute = -1, eYear = -1, eMonth = -1, eDay = -1, eHour = -1, eMinute = -1;
    String serverPath = "http://34.216.172.247";

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
        usageStatHeader = findViewById(R.id.usage_stat_header);

//        this.loadStatistics();
//        try {
////            s3Test();
////            queryS3();
//            dataTiering();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onStart() {
        super.onStart();
        confirmButton.setOnClickListener(view -> {
//            try {
//                queryEC2("http://34.216.172.247:3000");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            try {
                queryAll();
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
    public JSONArray queryEC2(String startTimeInput) throws Exception {
        String endTimeInput;
        if(sYear != -1 && sMonth != -1 && sDay != -1 && sHour != -1 && sMinute != -1 && eYear != -1 && eMonth != -1 && eDay != -1 && eHour != -1 && eMinute != -1){
            endTimeInput = Integer.toString(eYear) + "-" + String.format("%02d", eMonth) + "-" + String.format("%02d", eDay) + "T" + String.format("%02d", eHour) + ":" + String.format("%02d", eMinute) + ":00";
            startTimeDisplay = Integer.toString(sYear) + "-" + Integer.toString(sMonth) + "-" + Integer.toString(sDay) + " " + String.format("%02d", sHour) + ":" + String.format("%02d", sMinute);
            endTimeDisplay = Integer.toString(eYear) + "-" + Integer.toString(eMonth) + "-" + Integer.toString(eDay) + " " + String.format("%02d", eHour) + ":" + String.format("%02d", eMinute);
            usageStatHeader.setText("Your Apps usage from " + startTimeDisplay + " to " + endTimeDisplay);
        }
        else{
            usageStatHeader.setText("Your Apps usage for last 24 hours:");
            loadStatistics();
            return null;
        }
        URL url = new URL(serverPath + ":3000/rpc/query_app_usage_by_time");
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
            Log.d("ec2 input line", inputLine);
        }
        in.close();

        // print result
//        System.out.println(response.toString());
        Log.d("query response", response.toString());

        JSONArray appUsageArray = new JSONArray(response.toString());
//        showAppsUsageReport(appUsageArray);

        return appUsageArray;
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void loadStatistics() {
        UsageStatsManager usm = (UsageStatsManager) this.getSystemService(USAGE_STATS_SERVICE);
        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 1,  System.currentTimeMillis());
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
    public void showAppsUsageReport(JSONArray appUsageArray) throws JSONException {
//        JSONArray appUsageArray = new JSONArray(postResponse);

        ArrayList<App> appsList = new ArrayList<>();

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
                long duration = object.getInt("total_duration");

//                String usageDuration = Integer.toString(duration) + "ms";
                String usageDuration = getDurationBreakdown(duration);
                int usagePercentage = (int) (duration * 100 / totalTime);

                App usageStatDTO = new App(icon, appName, usagePercentage, duration, usageDuration);
                appsList.add(usageStatDTO);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        Collections.sort(appsList, Comparator.comparingLong(z -> z.durationInt));
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

                App usageStatDTO = new App(icon, appName, usagePercentage, (int)usageStats.getTotalTimeInForeground(), usageDuration);
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
        usageStatHeader.setVisibility(View.VISIBLE);
        appsList.setVisibility(View.VISIBLE);

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public String queryS3(String key) throws Exception {
        String startTimeInput, endTimeInput;
        startTimeInput = Integer.toString(sYear) + "-" + String.format("%02d", sMonth) + "-" + String.format("%02d", sDay) + " " + String.format("%02d", sHour) + ":" + String.format("%02d", sMinute) + ":00";
        endTimeInput = Integer.toString(eYear) + "-" + String.format("%02d", eMonth) + "-" + String.format("%02d", eDay) + " " + String.format("%02d", eHour) + ":" + String.format("%02d", eMinute) + ":00";
//        usageStatHeader.setText("Your Apps usage from " + startTimeDisplay + " to " + endTimeDisplay);
        Log.d("query s3 start time", startTimeInput);
        Log.d("query s3 end time", endTimeInput);
        URL url = new URL("http://34.216.172.247/s3_query");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setUseCaches(false);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        JSONObject param = new JSONObject();
//        param.put("device_id", current_android_id);
//        param.put("start_time", startTimeInput);
//        param.put("end_time", endTimeInput);
//        startTimeInput = "2022-09-29 11:54:02";
//        endTimeInput = "2022-11-29 11:54:02";
//        key = "test_chunk_2.csv";
        String sql = "Select * from S3Object s WHERE s._1 >= '" + startTimeInput + "' AND s._1 < '" + endTimeInput + "' AND s._2 = '" + current_android_id + "'";
        Log.d("s3 sql", sql);
        param.put("key", key);
        param.put("sql", sql);

        Log.d("req body", param.toString());

        conn.connect();
        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8);
        writer.write(param.toString());
        writer.flush();
        writer.close();
        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Fail to request url! ResponseCode: " + conn.getResponseCode());
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(
                conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
            Log.d("fastapi input line", inputLine);
        }
        in.close();

        // print result
//        System.out.println(response.toString());
        Log.d("fastapi query response", response.toString());
//        showAppsUsageReport(response.toString());

        return response.toString();
    }

    public void uploadToS3(String chunk, String file_name) throws Exception {
        URL url = new URL("http://34.216.172.247/data_tiering");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setUseCaches(false);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        JSONObject param = new JSONObject();
        param.put("chunk", chunk);
        param.put("file_name", file_name);

        Log.d("req body", param.toString());

        conn.connect();
        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8);
        writer.write(param.toString());
        writer.flush();
        writer.close();
        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Fail to request url! ResponseCode: " + conn.getResponseCode());
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(
                conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
            Log.d("fastapi input tiering", inputLine);
        }
        in.close();

        Log.d("fastapi tiering res", response.toString());
    }

    public String getOldChunks() throws Exception{
        URL url = new URL( "http://34.216.172.247:3000/rpc/query_old_chunks");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setUseCaches(false);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.connect();

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Fail to request url! ResponseCode: " + conn.getResponseCode());
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(
                conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
            Log.d("old chunks name line", inputLine);
        }
        in.close();

        // print result
//        System.out.println(response.toString());
        Log.d("query old chunks res", response.toString());

        return response.toString();
    }

    public String getChunkTimeRange(String chunk_input) throws Exception{
        URL url = new URL( "http://34.216.172.247:3000/rpc/get_chunk_time_range");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setUseCaches(false);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        JSONObject param = new JSONObject();
        param.put("chunk_input", chunk_input);

        Log.d("req body", param.toString());

        conn.connect();
        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8);
        writer.write(param.toString());
        writer.flush();
        writer.close();
        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Fail to request url! ResponseCode: " + conn.getResponseCode());
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(
                conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
            Log.d("chunk time itv line", inputLine);
        }
        in.close();

        Log.d("old chunks time itv res", response.toString());

        return response.toString();
    }

    public String getChunkData(String chunk) throws Exception{
        URL url = new URL( "http://34.216.172.247:3000/rpc/get_chunk_data");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setUseCaches(false);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        JSONObject param = new JSONObject();
        param.put("chunk", chunk);

        Log.d("req body", param.toString());

        conn.connect();
        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8);
        writer.write(param.toString());
        writer.flush();
        writer.close();
        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Fail to request url! ResponseCode: " + conn.getResponseCode());
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(
                conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
            Log.d("old chunks data line", inputLine);
        }
        in.close();

        Log.d("query old data res", response.toString());

        return response.toString();
    }

    public void saveChunkToS3(){

    }

    public void processChunkData(String jsonUsageData){ // change the return of get_chunk_data from json string to csv string

    }

    public String removeOldChunks() throws Exception{
        URL url = new URL( serverPath + ":3000/rpc/remove_old_chunks");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setUseCaches(false);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.connect();

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Fail to request url! ResponseCode: " + conn.getResponseCode());
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(
                conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
            Log.d("old chunks name line", inputLine);
        }
        in.close();

        // print result
//        System.out.println(response.toString());
        Log.d("query old chunks res", response.toString());

        return response.toString();
    }

    public void dataTiering() throws Exception {
        String getOldChunks = getOldChunks();
        JSONArray oldChunks = new JSONArray(getOldChunks);

        for (int i = 0; i < oldChunks.length(); i++) {
            JSONObject chunkObject = oldChunks.getJSONObject(i);
            String chunk = chunkObject.getString("chunk");
            Log.d("chunk name", chunk);
            String[] splitChunk = chunk.split("\\.");
            String chunkName = splitChunk[1];

            String getChunkTimeRange = getChunkTimeRange(chunkName);
            JSONArray chunkTimeRange = new JSONArray(getChunkTimeRange);
            JSONObject chunkTimeRangeObject = chunkTimeRange.getJSONObject(0);
            String startTime = chunkTimeRangeObject.getString("start_time");
            String endTime = chunkTimeRangeObject.getString("end_time");
            String startDate = startTime.split("T")[0];
            String endDate = endTime.split("T")[0];
//            String fileName = startDate + "to" + endDate + ".csv";
            String fileName = startDate + ".csv";

            uploadToS3(chunk, fileName);

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Pair<Boolean, Boolean> findQueryObjects() throws Exception {
//        LocalDate prevThu = LocalDate.now().with(TemporalAdjusters.previous(DayOfWeek.THURSDAY));
//        LocalDate startDate = LocalDate.parse(sYear + "-" + sMonth + "-" + sDay);
//        LocalDate endDate = LocalDate.parse(eYear + "-" + eMonth + "-" + eDay);
//        LocalDate today = LocalDate.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime prevThuTime = LocalDateTime.now().with(TemporalAdjusters.previous(DayOfWeek.THURSDAY));
        prevThuTime = prevThuTime.with(TemporalAdjusters.previous(DayOfWeek.THURSDAY));  // prev of prev, because older than one week is for the end time of an interval
        prevThuTime = prevThuTime.with(LocalTime.MIDNIGHT);
        LocalDateTime todayTime = LocalDateTime.now();
        LocalDateTime startDateTime = LocalDateTime.parse(sYear + "-" + String.format("%02d", sMonth) + "-" + String.format("%02d", sDay) + " " + String.format("%02d", sHour) + ":" + String.format("%02d", sMinute), formatter);
        LocalDateTime endDateTime = LocalDateTime.parse(eYear + "-" + String.format("%02d", eMonth) + "-" + String.format("%02d", eDay) + " " + String.format("%02d", eHour) + ":" + String.format("%02d", eMinute), formatter);

        boolean isAfter = endDateTime.isAfter(todayTime);
//        List<String> s3KeyList = new ArrayList<>();

        Log.d("find prev thu", prevThuTime.toString());

        // special cases handling
        if(!startDateTime.isBefore(endDateTime)){
            Log.d("findQueryObjects", "FF");
            return new Pair<Boolean, Boolean>(false, false);  // ec2 s3 respectively
        }

        // only need EC2: prevThu 1.3 00:00    startDate 1.3 00.00 (or 00:01)
        if(!prevThuTime.isAfter(startDateTime)){
            Log.d("findQueryObjects", "TF");
            return new Pair<Boolean, Boolean>(true, false);
        }
        // need both: startDate 1.2 23:59    prevThu 1.3 00:00    endDate 1.3 03:00  (prevThu is in the middle)
        else if(!prevThuTime.isBefore(endDateTime)){
            Log.d("findQueryObjects", "FT");
            return new Pair<Boolean, Boolean>(false, true);
        }
        else{
            Log.d("findQueryObjects", "TT");
            return new Pair<Boolean, Boolean>(true, true);
        }


//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/mm/dd");
//        Date firstDate = sdf.parse(sYear + "/" + sMonth + "/" + sDay);
//        Date secondDate = sdf.parse(eYear + "/" + eMonth + "/" + eDay);
//
//        long diffInMillies = Math.abs(secondDate.getTime() - firstDate.getTime());
//        long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
//        long daysBetween = ChronoUnit.DAYS.between(firstDate, secondDate)

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public List<String> findS3KeyList(Boolean EC2) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime prevThuTime = LocalDateTime.now().with(TemporalAdjusters.previous(DayOfWeek.THURSDAY));  // prev of prev
        LocalDateTime todayTime = LocalDateTime.now();
        LocalDateTime startDateTime = LocalDateTime.parse(Integer.toString(sYear) + "-" + String.format("%02d", sMonth) + "-" + String.format("%02d", sDay) + " " + String.format("%02d", sHour) + ":" + String.format("%02d", sMinute), formatter);
        LocalDateTime endDateTime = LocalDateTime.parse(Integer.toString(eYear) + "-" + String.format("%02d", eMonth) + "-" + String.format("%02d", eDay) + " " + String.format("%02d", eHour) + ":" + String.format("%02d", eMinute), formatter);
        List<String> s3KeyList = new ArrayList<>();

//        if(endDateTime.isAfter(prevThuTime)){
//
//        }

        while (true){
            if(prevThuTime.isAfter(endDateTime)){
                prevThuTime = prevThuTime.with(TemporalAdjusters.previous(DayOfWeek.THURSDAY));
            }
            else{
                break;
            }
        }

        if(EC2){ // EC2 can store at most 2 weeks
            prevThuTime = prevThuTime.with(TemporalAdjusters.previous(DayOfWeek.THURSDAY));
        }

        while(true){
            prevThuTime = prevThuTime.with(TemporalAdjusters.previous(DayOfWeek.THURSDAY));

            if(!prevThuTime.isAfter(startDateTime)){
                break;
            }
            else{
                String key = prevThuTime.format(formatter).substring(0, 10) + ".csv";
                s3KeyList.add(key);
            }
        }

        Log.d("findS3KeyList", s3KeyList.toString());

        return s3KeyList;
    }


//    public String mergeAppUsageData(){
//
//    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void queryAll() throws Exception {
        dataTiering();

        String startTimeInput, endTimeInput;
        if(sYear != -1 && sMonth != -1 && sDay != -1 && sHour != -1 && sMinute != -1 && eYear != -1 && eMonth != -1 && eDay != -1 && eHour != -1 && eMinute != -1){
            startTimeInput = Integer.toString(sYear) + "-" + String.format("%02d", sMonth) + "-" + String.format("%02d", sDay) + "T" + String.format("%02d", sHour) + ":" + String.format("%02d", sMinute) + ":00";
            endTimeInput = Integer.toString(eYear) + "-" + String.format("%02d", eMonth) + "-" + String.format("%02d", eDay) + "T" + String.format("%02d", eHour) + ":" + String.format("%02d", eMinute) + ":00";
            startTimeDisplay = Integer.toString(sYear) + "-" + Integer.toString(sMonth) + "-" + Integer.toString(sDay) + " " + String.format("%02d", sHour) + ":" + String.format("%02d", sMinute);
            endTimeDisplay = Integer.toString(eYear) + "-" + Integer.toString(eMonth) + "-" + Integer.toString(eDay) + " " + String.format("%02d", eHour) + ":" + String.format("%02d", eMinute);
            usageStatHeader.setText("Your Apps usage from " + startTimeDisplay + " to " + endTimeDisplay);
        }
        else{ // has empty time input(s)
            usageStatHeader.setText("Your Apps usage for last 24 hours:");
            loadStatistics();
            return;
        }

        Pair<Boolean, Boolean> queryObjects = findQueryObjects();
        boolean queryEC2 = queryObjects.first;
        boolean queryS3 = queryObjects.second;

        // only query on EC2
        if(queryEC2 && !queryS3){
            startTimeInput = Integer.toString(sYear) + "-" + String.format("%02d", sMonth) + "-" + String.format("%02d", sDay) + "T" + String.format("%02d", sHour) + ":" + String.format("%02d", sMinute) + ":00";
            JSONArray appUsageArray = queryEC2(startTimeInput);
            showAppsUsageReport(appUsageArray);
        }
        // only query on S3
        else if(!queryEC2 && queryS3){
            JSONArray appUsageArray = new JSONArray();
            appUsageArray = queryAndMergeS3(appUsageArray, false);
            showAppsUsageReport(appUsageArray);
        }
        // query on EC2 and S3
        else{
            LocalDateTime prevThuTime = LocalDateTime.now().with(TemporalAdjusters.previous(DayOfWeek.THURSDAY));
            prevThuTime = prevThuTime.with(TemporalAdjusters.previous(DayOfWeek.THURSDAY));  // prev of prev, because older than one week is for the end time of an interval
            prevThuTime = prevThuTime.with(LocalTime.MIDNIGHT);
            int year = prevThuTime.getYear();
            int month = prevThuTime.getMonthValue();
            int day = prevThuTime.getDayOfMonth();
            startTimeInput = Integer.toString(year) + "-" + String.format("%02d", month) + "-" + String.format("%02d", day) + "T" + "00:00:00";
            JSONArray appUsageArray = queryEC2(startTimeInput);
            appUsageArray = queryAndMergeS3(appUsageArray, true);
            showAppsUsageReport(appUsageArray);
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public JSONArray queryAndMergeS3(JSONArray appUsageArray, Boolean EC2) throws Exception {

        List<String> S3KeyList = findS3KeyList(EC2);
        for(String S3Key: S3KeyList){
            String curResultS3 = queryS3(S3Key);
            JSONArray curAppUsageArray = new JSONArray(curResultS3);
            for (int i = 0; i < curAppUsageArray.length(); i++){
                JSONObject curObject = curAppUsageArray.getJSONObject(i);
//                Log.d("queryAndMergeS3 cur", curObject.toString());
//                Log.d("queryAndMergeS3 array", appUsageArray.toString());

                boolean find = false;

                for(int j = 0; j < appUsageArray.length(); j++){
                    JSONObject existingObject = appUsageArray.getJSONObject(j);
//                    Log.d("queryAndMergeS3 array", appUsageArray.toString());
//                    Log.d("queryAndMergeS3 exist", existingObject.toString());

                    if(existingObject.getString("package_name") == curObject.getString("package_name")){
                        int duration = existingObject.getInt("total_duration");
                        int addDuration = curObject.getInt("total_duration");
                        existingObject.put("total_duration", duration + addDuration);
                        appUsageArray.put(j, existingObject);
                        find = true;
                        break;
                    }
                }

                if(!find){
                    appUsageArray.put(curObject);
//                    Log.d("queryAndMergeS3 append", appUsageArray.toString());
                }
            }
        }

        return appUsageArray;
    }


}


package com.example.test;

import android.graphics.drawable.Drawable;

public class App {
    public  Drawable appIcon;
    public  String appName;
    public  int usagePercentage;
    public long durationInt;
    public  String usageDuration;


    public App(Drawable appIcon, String appName, int usagePercentage, long durationInt, String usageDuration) {
        this.appIcon = appIcon;
        this.appName = appName;
        this.usagePercentage = usagePercentage;
        this.durationInt = durationInt;
        this.usageDuration = usageDuration;
    }
}

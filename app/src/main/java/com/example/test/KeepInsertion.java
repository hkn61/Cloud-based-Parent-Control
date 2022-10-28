package com.example.test;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class KeepInsertion extends Service {
    public KeepInsertion() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
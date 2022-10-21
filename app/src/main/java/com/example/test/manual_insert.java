package com.example.test;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class manual_insert extends AppCompatActivity {
    EditText startDate, endDate;
    private int sYear, sMonth, sDay, eYear, eMonth, eDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_insert);

        startDate = (EditText)findViewById(R.id.startDate);
        endDate = (EditText)findViewById(R.id.endDate);
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
            }
            }, sYear, sMonth, sDay);

        datePickerDialog.show();
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
            }
            }, eYear, eMonth, eDay);

        datePickerDialog.show();
    }

}
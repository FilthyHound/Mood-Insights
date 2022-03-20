package com.nuigalway.bct.mood_insights;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CalendarPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_page);

        BottomNavigationView bottomNavigationView = findViewById(R.id.navView);
        bottomNavigationView.setSelectedItemId(R.id.calendar);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if(item.getItemId() == R.id.factorHome){
                startActivity(new Intent(getApplicationContext(), FactorPage.class));
                overridePendingTransition(0, 0);
                return true;
            }else if (item.getItemId() == R.id.graph){
                startActivity(new Intent(getApplicationContext(), GraphPage.class));
                overridePendingTransition(0, 0);
                return true;
            }else return item.getItemId() == R.id.calendar;
        });
    }
}
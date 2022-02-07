package com.nuigalway.bct.mood_insights;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class GraphPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_page);

        BottomNavigationView bottomNavigationView = findViewById(R.id.navView);
        bottomNavigationView.setSelectedItemId(R.id.graph);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.calendar:
                    startActivity(new Intent(getApplicationContext(), CalendarPage.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.factorHome:
                    startActivity(new Intent(getApplicationContext(), FactorPage.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.graph:
                    return true;
            }
            return false;
        });
    }
}
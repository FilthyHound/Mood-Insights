package com.nuigalway.bct.mood_insights;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.nuigalway.bct.mood_insights.user.User;
import com.nuigalway.bct.mood_insights.util.Utils;

public class CalendarPage extends AppCompatActivity {

    private User userProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_page);

        userProfile = (User) getIntent().getSerializableExtra("USER");

        BottomNavigationView bottomNavigationView = findViewById(R.id.navView);
        bottomNavigationView.setSelectedItemId(R.id.calendar);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if(item.getItemId() == R.id.factorHome){
                Intent factors = new Intent(getApplicationContext(), FactorPage.class);
                factors.putExtra(Utils.USER_KEY, userProfile);
                startActivity(new Intent(getApplicationContext(), FactorPage.class));
                overridePendingTransition(0, 0);
                return true;
            }else if (item.getItemId() == R.id.graph){
                Intent graph = new Intent(getApplicationContext(), GraphPage.class);
                graph.putExtra(Utils.USER_KEY, userProfile);
                startActivity(graph);
                overridePendingTransition(0, 0);
                return true;
            }else return item.getItemId() == R.id.calendar;
        });
    }
}
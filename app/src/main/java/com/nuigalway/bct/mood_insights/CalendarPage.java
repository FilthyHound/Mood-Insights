package com.nuigalway.bct.mood_insights;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nuigalway.bct.mood_insights.data.Day;
import com.nuigalway.bct.mood_insights.data.Sleep;
import com.nuigalway.bct.mood_insights.user.User;
import com.nuigalway.bct.mood_insights.util.DateKeyParser;
import com.nuigalway.bct.mood_insights.util.Utils;

import java.io.IOException;
import java.time.Month;
import java.util.Calendar;

@RequiresApi(api = Build.VERSION_CODES.O)
public class CalendarPage extends AppCompatActivity {
    private static final String MESSAGE = "Select Day to enter / view factors";
    private FirebaseUser user;
    private DatabaseReference reference;
    private User userProfile;
    private DateKeyParser dkp;
    private CalendarView calendarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_page);

        setupUser();
        bottomNavSetup();
    }

    private void setupUser(){
        try {
            user = FirebaseAuth.getInstance().getCurrentUser();
            reference = FirebaseDatabase.getInstance(Utils.getProperty("app.database", getApplicationContext())).getReference("Users");
        }catch (IOException e){
            e.printStackTrace();
        }
        userProfile = (User) getIntent().getSerializableExtra(Utils.USER_KEY);

        if(userProfile == null) {
            String userId = user.getUid();
            getUserFromDatabase(userId);
        }else{
            dkp = new DateKeyParser(userProfile);
            setupCalendar();
        }
        userProfile.setCurrentDay(new Day());
    }

    private void getUserFromDatabase(String userId) {
        reference.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userProfile = snapshot.getValue((User.class));
                if (userProfile != null) {
                    // Set-up called here as it is reliant on an instance of User from the database
                    dkp = new DateKeyParser(userProfile);
                    setupCalendar();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CalendarPage.this, "Error", Toast.LENGTH_LONG).show();
            }
        });
    }


    private void setupCalendar(){
        TextView calendarDateHolder = findViewById(R.id.calendarDate);

        calendarView = findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Month mth = Month.of(month + 1);
            if(dkp.parseCalendarDate(dayOfMonth, mth, year)){
                handleDateSelected(dayOfMonth, mth, year);
            }
        });
        calendarDateHolder.setText(MESSAGE);
        constrainCalendarDateSelection();
    }

    private void handleDateSelected(int dayOfMonth, Month month, int year){
        String key = dayOfMonth + "-" + month + "-" + year;
        userProfile.setCurrentDay(new Day(dayOfMonth, month, year));
        if(!userProfile.getDates().contains(key)){
            userProfile.getDailySleepFactors().put(key, new Sleep());
            userProfile.getDates().add(key);
        }

        Intent factors = new Intent(getApplicationContext(), FactorPage.class);
        factors.putExtra(Utils.USER_KEY, userProfile);
        factors.putExtra(Utils.OLD_DATE_FROM_CALENDAR, true);
        startActivity(factors);
        overridePendingTransition(0, 0);
    }

    private void constrainCalendarDateSelection() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
        calendar.set(Calendar.HOUR_OF_DAY, 23);//not sure this is needed
        long endOfMonth = calendar.getTimeInMillis();
        calendarView.setMaxDate(endOfMonth);
    }

    private void bottomNavSetup(){
        BottomNavigationView bottomNavigationView = findViewById(R.id.navView);
        bottomNavigationView.setSelectedItemId(R.id.calendar);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if(item.getItemId() == R.id.factorHome){
                Intent factors = new Intent(getApplicationContext(), FactorPage.class);
                factors.putExtra(Utils.USER_KEY, userProfile);
                startActivity(factors);
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
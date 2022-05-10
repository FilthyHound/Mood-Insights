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

/**
 *
 *
 *
 * @author Karl Gordon
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class CalendarPage extends AppCompatActivity {
    // Private static final field
    private static final String MESSAGE = "Select Day to define its factors";

    // Private fields
    private FirebaseUser user;
    private DatabaseReference reference;
    private User userProfile;
    private DateKeyParser dkp;
    private CalendarView calendarView;

    /**
     * onCreate method is the first called upon the instantiation of the activity class,
     * instantiate a CalendarView object, that allows the user to select date to view or add
     * factors to.
     *
     * @param savedInstanceState - Bundle, contains the data it most recently supplied in
     *                             onSaveInstanceState.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_page);

        setupUser();
        bottomNavSetup();
    }

    /**
     * Method sets up & loads the user profile, either passed in from another activity or read in
     * from the database if the passed UserProfile is null
     */
    private void setupUser(){
        // Try to connect to the database and get the current user instance
        try {
            user = FirebaseAuth.getInstance().getCurrentUser();
            reference = FirebaseDatabase.getInstance(Utils.getProperty("app.database", getApplicationContext())).getReference("Users");
        }catch (IOException e){
            e.printStackTrace();
        }
        // Try to read the user from the intent
        userProfile = (User) getIntent().getSerializableExtra(Utils.USER_KEY);

        // Handle case if userProfile from intent is read null or not
        if(userProfile == null) {
            String userId = user.getUid();
            getUserFromDatabase(userId);
        }else{
            afterLoadUser();
        }
    }

    /**
     * Method gets the user from the Firebase database using the user ID from the
     * current user currently logged in
     *
     * @param userId - String, user ID from the current user currently logged in
     */
    private void getUserFromDatabase(String userId) {
        // Make the call to the database for the user using the given user id
        reference.child(userId).addValueEventListener(new ValueEventListener() {

            // Once the data is received from the database
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userProfile = snapshot.getValue((User.class));
                if (userProfile != null) {
                    afterLoadUser();
                }
            }

            // If the connection between the app and the database encountered an error
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CalendarPage.this, "Error", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Method called once the User Profile is received, either via an intent or database call
     */
    private void afterLoadUser(){
        // New day set to reset any day selected prior by the calendar
        userProfile.setCurrentDay(new Day());
        // Set-up called here as it is reliant on an instance of User from the database
        dkp = new DateKeyParser(userProfile);
        setupCalendar();
    }

    /**
     * Method sets up the CalendarView, including the onDateChangeListener, the TextView attached
     * to the CalendarPage, and calls methods to constrain and update the calendar
     */
    private void setupCalendar(){
        TextView calendarDateHolder = findViewById(R.id.calendarDate);

        calendarView = findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Month mth = Month.of(month + 1);
            // If the date selected is a day not equal to the current day
            if(dkp.isCalendarDateValid(dayOfMonth, mth, year)){
                handleDateSelected(dayOfMonth, mth, year);
            }
        });
        calendarDateHolder.setText(MESSAGE);
        constrainCalendarDateSelection();
    }

    /**
     * Method handles the selected date, and redirects to the FactorPage with the date to put
     * factors to
     *
     * @param dayOfMonth - int, the day of the month
     * @param month - Month, the month
     * @param year - int, the year
     */
    private void handleDateSelected(int dayOfMonth, Month month, int year){
        String key = dayOfMonth + Utils.HYPHEN + month + Utils.HYPHEN + year;
        userProfile.setCurrentDay(new Day(dayOfMonth, month, year));

        // Check to carry out setup if the key has not been modified previously by the user
        if(!userProfile.getDates().contains(key)){
            userProfile.getDailySleepFactors().put(key, new Sleep());
            userProfile.getDates().add(key);
        }

        Intent factors = new Intent(getApplicationContext(), FactorPage.class);
        factors.putExtra(Utils.USER_KEY, userProfile);
        // boolean to state if the date is an older date from the current date
        factors.putExtra(Utils.OLD_DATE_FROM_CALENDAR, true);
        startActivity(factors);
        overridePendingTransition(0, 0);
    }

    /**
     * Method adds a constraint to the calendar such that it does not go the the next month into the
     * future, so future dates aren't pre-populated with factor information
     */
    private void constrainCalendarDateSelection() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
        long endOfMonth = calendar.getTimeInMillis();
        calendarView.setMaxDate(endOfMonth);
    }

    /**
     * Method handles the BottomNavigationView attached to the activity. Will change the pages when
     * the user selects a different icon
     */
    private void bottomNavSetup(){
        BottomNavigationView bottomNavigationView = findViewById(R.id.navView);
        bottomNavigationView.setSelectedItemId(R.id.calendar);

        bottomNavigationView.setOnItemSelectedListener(item -> {

            // If statements used as switch cases rely on non final strings, which will cause errors
            // in later releases of Android
            if(item.getItemId() == R.id.factorHome){
                // Prepare Intent to send the app to the FactorPage
                Intent factors = new Intent(getApplicationContext(), FactorPage.class);
                factors.putExtra(Utils.USER_KEY, userProfile);
                startActivity(factors);
                overridePendingTransition(0, 0);
                return true;
            }else if (item.getItemId() == R.id.graph){
                // Prepare Intent to send the app to the GraphPage
                Intent graph = new Intent(getApplicationContext(), GraphPage.class);
                graph.putExtra(Utils.USER_KEY, userProfile);
                startActivity(graph);
                overridePendingTransition(0, 0);
                return true;
            }else return item.getItemId() == R.id.calendar; // Do nothing on Calendar icon click
        });
    }
}
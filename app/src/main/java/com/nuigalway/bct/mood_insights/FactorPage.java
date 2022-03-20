package com.nuigalway.bct.mood_insights;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import com.nuigalway.bct.mood_insights.data.Factor;
import com.nuigalway.bct.mood_insights.data.Sleep;
import com.nuigalway.bct.mood_insights.user.User;
import com.nuigalway.bct.mood_insights.util.FactorRecyclerAdaptor;
import com.nuigalway.bct.mood_insights.util.Utils;
import com.nuigalway.bct.mood_insights.validation.Validator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.O)
public class FactorPage extends AppCompatActivity {
    private boolean loadFactors = true;
    private FirebaseUser user;
    private DatabaseReference reference;

    private User userProfile;

    private ProgressBar progressBar;

    // Sleep Factor Variables
    private Validator validator;
    private final ArrayList<Factor> factorsList = new ArrayList<>();
    private final ArrayList<TextView> factorViewListEnabled = new ArrayList<>();
    private final ArrayList<TextView> factorViewListDisabled = new ArrayList<>();
    private RecyclerView recyclerView;
    private FactorRecyclerAdaptor.FactoryRecyclerViewClickListener listener;
    private EditText sleepRating;
    private EditText sleepAmount;
    private EditText sleepStart;
    private EditText sleepEnd;
    private TimePickerDialog sleepStartDialog, sleepEndDialog;
    private int startHour = -1, startMin = -1, endHour = -1, endMin = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_factor_page);

        validator = new Validator();
        loadFactors = true;

        initBottomNav();
        loadUserDetails();
    }

    private void initBottomNav(){
        BottomNavigationView bottomNavigationView = findViewById(R.id.navView);
        bottomNavigationView.setSelectedItemId(R.id.factorHome);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if(item.getItemId() == R.id.calendar){
                startActivity(new Intent(getApplicationContext(), CalendarPage.class));
                overridePendingTransition(0, 0);
                return true;
            }else if(item.getItemId() == R.id.graph){
                startActivity(new Intent(getApplicationContext(), GraphPage.class));
                overridePendingTransition(0, 0);
                return true;
            }else return item.getItemId() == R.id.factorHome;
        });
    }

    private void loadUserDetails(){
        progressBar = findViewById(R.id.progressBar);

        Button logOut = findViewById(R.id.signOut);
        logOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(FactorPage.this, MainActivity.class));
        });

        Button updateSleepFactorsButton = findViewById(R.id.sleepFactorSubmit);
        updateSleepFactorsButton.setOnClickListener(v -> handleUpdateSleepFactors());

        recyclerView = findViewById(R.id.sleepFactorRecyclerView);

        try {
            user = FirebaseAuth.getInstance().getCurrentUser();
            reference = FirebaseDatabase.getInstance(Utils.getProperty("app.database", getApplicationContext())).getReference("Users");
        }catch (IOException e){
            e.printStackTrace();
        }
        String userID = user.getUid();

        reference.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userProfile = snapshot.getValue((User.class));

                if(userProfile != null){
                    populateUserData();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FactorPage.this, "Signing Out", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initDialogs(){
        Sleep sleep = userProfile.getDailySleepFactors().get(userProfile.getCurrentDay().getDate());
        sleepRating = findViewById(R.id.sleepRating);
        if(sleep != null && sleep.getSleepQualityRating() > -1){
            String rating = Integer.toString(sleep.getSleepQualityRating());
            sleepRating.setText(rating);
        }

        sleepAmount = findViewById(R.id.sleepAmount);
        if(sleep != null && sleep.getNumberOfHoursSlept() > -1){
            String hours = Integer.toString((int) Math.rint(sleep.getNumberOfHoursSlept()));
            sleepAmount.setText(hours);
        }

        sleepStart = findViewById(R.id.sleepStart);
        sleepStartDialog = new TimePickerDialog(
                FactorPage.this,
                (view, hourOfDay, minute) -> {
                    startHour = hourOfDay;
                    startMin = minute;

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(0, 0, 0, startHour, startMin);

                    sleepStart.setText(DateFormat.format("HH:mm", calendar));
                }, 12, 0, true
        );
        sleepStart.setInputType(InputType.TYPE_NULL);
        sleepStart.setOnClickListener(v -> {
            sleepStartDialog.updateTime(startHour, startMin);
            sleepStartDialog.show();
        });
        sleepStart.setOnFocusChangeListener((v, hasFocus) ->{
            if(hasFocus) {
                sleepStartDialog.updateTime(startHour, startMin);
                sleepStartDialog.show();
            }
        });
        if(sleep != null && sleep.getTimeInBed() != null){
            sleepStart.setText(sleep.getTimeInBed());
        }

        sleepEnd = findViewById(R.id.sleepEnd);
        sleepEndDialog = new TimePickerDialog(
                FactorPage.this,
                (view, hourOfDay, minute) -> {
                    endHour = hourOfDay;
                    endMin = minute;

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(0, 0, 0, endHour, endMin);

                    sleepEnd.setText(DateFormat.format("HH:mm", calendar));
                }, 12, 0, true
        );
        sleepEnd.setInputType(InputType.TYPE_NULL);
        sleepEnd.setOnClickListener(v -> {
            sleepEndDialog.updateTime(endHour, endMin);
            sleepEndDialog.show();
        });
        sleepEnd.setOnFocusChangeListener((v, hasFocus) ->{
            if(hasFocus) {
                sleepEndDialog.updateTime(endHour, endMin);
                sleepEndDialog.show();
            }
        });
        if(sleep != null && sleep.getTimeLeftBed() != null){
            sleepEnd.setText(sleep.getTimeLeftBed());
        }
    }

    private void handleUpdateSleepFactors() {
        progressBar.setVisibility(View.VISIBLE);

        Sleep currentSleep = userProfile.getDailySleepFactors().get(userProfile.getCurrentDay().getDate());
        if(currentSleep == null){
            currentSleep = new Sleep();
        }

        int rating = -1, amount = -1;
        try {
            rating = Integer.parseInt(sleepRating.getText().toString().trim());
            amount = Integer.parseInt(sleepAmount.getText().toString().trim());
        }catch(NumberFormatException e){
            e.printStackTrace();
        }

        if(!validator.numberOneToTen(rating, sleepRating) || !validator.isNumberOneToTwentyThree(amount, sleepAmount)){
            return;
        }

        currentSleep.setSleepQualityRating(rating);
        currentSleep.setNumberOfHoursSlept(amount);
        currentSleep.setTimeInBed(sleepStart.getText().toString().trim());
        currentSleep.setTimeLeftBed(sleepEnd.getText().toString().trim());

        for(View v : factorViewListEnabled){
            String factor = ((TextView) v).getText().toString();
            if(currentSleep.containsSleepFactor(factor)){
                currentSleep.updateSleepFactor(factor, true);
            }else{
                currentSleep.addSleepFactor(factor);
            }
        }

        for(View v : factorViewListDisabled){
            String factor = ((TextView) v).getText().toString();
            currentSleep.updateSleepFactor(factor, false);
        }

        if(!userProfile.getDates().contains(userProfile.getCurrentDay().getDate())) {
            userProfile.getDates().add(userProfile.getCurrentDay().getDate());
        }

        String dateKey = userProfile.getDate();
        userProfile.getDailySleepFactors().put(dateKey, currentSleep);

        try {
            FirebaseDatabase.getInstance(Utils.getProperty("app.database", getApplicationContext())).getReference("Users")
                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                    .setValue(userProfile).addOnCompleteListener(taskDatabaseCommunications -> {
                if (taskDatabaseCommunications.isSuccessful()) {
                    Toast.makeText(FactorPage.this, "User has been successfully updated!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(FactorPage.this, "Failed to update, try again!", Toast.LENGTH_LONG).show();
                }
                progressBar.setVisibility(View.GONE);
            });
        }catch(IOException e){
            e.printStackTrace();
            //TODO make these logs
            Toast.makeText(FactorPage.this, "Error, couldn't connect to database", Toast.LENGTH_LONG).show();
        }catch(NullPointerException e){
            e.printStackTrace();
            //TODO make these logs
            Toast.makeText(FactorPage.this, "Error, couldn't get account", Toast.LENGTH_LONG).show();
        }finally {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void populateUserData(){
        final TextView welcomeTextView = findViewById(R.id.welcome);
        final TextView dayTextView = findViewById(R.id.day);

        String fullName = userProfile.fullName;
        Day d = userProfile.getCurrentDay();
        Day toCheck = new Day();

        if(!d.equals(toCheck)){
            if(!userProfile.getDates().contains(d.getDate())){
                userProfile.getDates().add(d.getDate());
            }
            userProfile.setCurrentDay(new Day());
            String date = userProfile.getCurrentDay().getDate();
            userProfile.getDailySleepFactors().put(date, new Sleep());
            userProfile.getDates().add(date);
        }

        String welcomeText = "Welcome, " + fullName + "!";
        welcomeTextView.setText(welcomeText);
        dayTextView.setText(userProfile.getDate());

        initDialogs();
        if(loadFactors) {
            setFactorInfo();
            setAdaptor();
            loadFactors = false;
        }
    }

    private void setFactorInfo(){
        String[] sleepFactors = getResources().getStringArray(R.array.sleep_factors);
        for(String s : sleepFactors){
            Factor f = new Factor(s);
            if(!factorsList.contains(f)) {
                factorsList.add(f);
            }
        }
    }

    private void setAdaptor() {
        setRecyclerViewOnClickListener();
        FactorRecyclerAdaptor adaptor = new FactorRecyclerAdaptor(factorsList, listener, this);
        RecyclerView.LayoutManager  layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adaptor);
    }

    /**
     * Method sets the functionality for when an element in the factor list is clicked
     */
    private void setRecyclerViewOnClickListener(){
        listener = (v, position) -> {
            if (v.getBackground() instanceof ColorDrawable) {
                ColorDrawable cd = (ColorDrawable) v.getBackground();

                TextView tv = v.findViewById(R.id.factorHolder);

                if (cd.getColor() == getResources().getColor(R.color.factorUnselected)){
                    v.setBackgroundColor(getResources().getColor(R.color.factorSelected));
                    if(!factorViewListEnabled.contains(tv)) {
                        factorViewListEnabled.add(tv);
                    }
                    factorViewListDisabled.remove(tv);
                }else{
                    v.setBackgroundColor(getResources().getColor(R.color.factorUnselected));
                    if(!factorViewListDisabled.contains(tv)) {
                        factorViewListDisabled.add(tv);
                    }
                    factorViewListEnabled.remove(tv);
                }
            }
        };
    }

    public Sleep getCurrentSleep(){
        return userProfile.dailySleepFactors.get(userProfile.getDate());
    }

    public void addTextViewFactorToFactorViewListEnabled(TextView tv){
        if(!factorViewListEnabled.contains(tv)) {
            factorViewListEnabled.add(tv);
        }
    }
}
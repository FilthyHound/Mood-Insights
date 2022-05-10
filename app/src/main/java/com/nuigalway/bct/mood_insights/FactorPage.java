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
import com.nuigalway.bct.mood_insights.util.FactorRecyclerAdapter;
import com.nuigalway.bct.mood_insights.util.Utils;
import com.nuigalway.bct.mood_insights.validation.Validator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

/**
 * FactorPage activity class that handles functionality relating to the input of factors of a User
 * for the current day, or selected day from the CalendarPage
 *
 * @author Karl Gordon
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class FactorPage extends AppCompatActivity {
    // Private final ArrayLists handling the factors
    private final ArrayList<Factor> factorsList = new ArrayList<>();
    private final ArrayList<TextView> factorViewEnabledList = new ArrayList<>();
    private final ArrayList<TextView> factorViewDisabledList = new ArrayList<>();

    // Private fields relating to Firebase
    private FirebaseUser user;
    private FirebaseAuth auth;
    private DatabaseReference reference;

    // Private fields handling the user profile, and how to load it to the users screen
    private User userProfile;
    private Boolean isOlderDateSelected;
    private Boolean doLoadFactorRecyclerAdapter = true;

    // Private fields for UI elements
    private ProgressBar progressBar;
    private Validator validator;
    private FactorRecyclerAdapter.FactoryRecyclerViewClickListener listener;

    // Private fields for user input
    private EditText sleepRating;
    private EditText sleepAmount;
    private EditText sleepStart;
    private EditText sleepEnd;
    private TimePickerDialog sleepStartDialog, sleepEndDialog;
    private int startHour = -1, startMin = -1, endHour = -1, endMin = -1;

    /**
     * onCreate method is the first called upon the instantiation of the activity class,
     * Creates the Application main page, displaying the factors of a User, loaded in from the
     * database, or passed in by either the CalendarPage or GraphPage.
     *
     * @param savedInstanceState- Bundle, contains the data it most recently supplied in
     *                            onSaveInstanceState.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_factor_page);

        validator = new Validator();
        doLoadFactorRecyclerAdapter = true;

        auth = FirebaseAuth.getInstance();

        setupUI();
        loadUserDetails();
    }

    /**
     * Method sets up UI elements not dependent on the User
     */
    private void setupUI() {
        progressBar = findViewById(R.id.progressBar);

        Button logOut = findViewById(R.id.signOut);
        logOut.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(FactorPage.this, MainActivity.class));
        });

        Button updateSleepFactorsButton = findViewById(R.id.sleepFactorSubmit);
        updateSleepFactorsButton.setOnClickListener(v -> handleUpdateSleepFactors());

        bottomNavSetup();
    }

    /**
     * Method handles all of the updating of the UserProfile from the data set by the user
     *
     * Once all data is handled, calls method to update the User in the database
     */
    private void handleUpdateSleepFactors() {
        progressBar.setVisibility(View.VISIBLE);

        String dateKey = userProfile.getDate();
        Sleep currentSleep = userProfile.getDailySleepFactors().get(dateKey);
        // If, null, then it was not defined previously, so a new Sleep object will be created
        if(currentSleep == null){
            currentSleep = new Sleep();
        }

        // Handle the data input by the EditTexts
        int rating = -1, amount = -1;
        try {
            rating = Integer.parseInt(sleepRating.getText().toString().trim());
            amount = Integer.parseInt(sleepAmount.getText().toString().trim());
        }catch(NumberFormatException e){
            e.printStackTrace();
        }

        // Verify number for sleep rating and amount of hours is set
        if(!validator.numberOneToTen(rating, sleepRating)
                || !validator.isNumberOneToTwentyThree(amount, sleepAmount)){
            return;
        }

        // Set values from EditTexts into the sleep object
        currentSleep.setSleepQualityRating(rating);
        currentSleep.setNumberOfHoursSlept(amount);
        currentSleep.setTimeInBed(sleepStart.getText().toString().trim());
        currentSleep.setTimeLeftBed(sleepEnd.getText().toString().trim());

        // Add factors if not present in the Sleep function, or if present, set them to true
        for(View v : factorViewEnabledList){
            String factor = ((TextView) v).getText().toString();
            if(currentSleep.containsSleepFactor(factor)){
                currentSleep.updateSleepFactor(factor, true);
            }else{
                currentSleep.addSleepFactor(factor);
            }
        }

        // Set disabled factors as false
        for(View v : factorViewDisabledList){
            String factor = ((TextView) v).getText().toString();
            currentSleep.updateSleepFactor(factor, false);
        }

        // If the currentDate is not included in list of Date keys, add it in
        if(!userProfile.getDates().contains(dateKey)) {
            userProfile.getDates().add(dateKey);
        }

        // Add Sleep object to the Sleep Factor map, linked to the current date key
        userProfile.getDailySleepFactors().put(dateKey, currentSleep);

        // Update user profile in database
        setUserToDatabase();
    }

    /**
     * Method sets up & loads the user profile, either passed in from another activity or read in
     * from the database if the passed UserProfile is null.
     *
     * Also checks if a boolean has been sent to the Activity to indicate if the date being
     * interacted with is the current date or an older date.
     */
    private void loadUserDetails(){
        try {
            user = auth.getCurrentUser();
            reference = FirebaseDatabase.getInstance(Utils.getProperty("app.database",
                    getApplicationContext())).getReference("Users");
            isOlderDateSelected = getIntent().getExtras().getBoolean(Utils.OLD_DATE_FROM_CALENDAR);
        }catch (IOException e){
            e.printStackTrace();
        }catch (NullPointerException npe){
            npe.printStackTrace();
            isOlderDateSelected = false;
        }

        // Try to get the user profile if coming from the graph or calendar page
        userProfile = (User) getIntent().getSerializableExtra(Utils.USER_KEY);

        if(userProfile == null){
            String userId = user.getUid();
            getUserFromDatabase(userId);
        }else{
            populateUserData();
        }
    }

    /**
     * Method gets the user from the Firebase database using the user ID from the
     * current user currently logged in
     *
     * @param userId - String, user ID from the current user currently logged in
     */
    private void getUserFromDatabase(String userId){
        // Make the call to the database for the user using the given user id
        reference.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // Once the data is received from the database
                userProfile = snapshot.getValue((User.class));

                if (userProfile != null) {
                    populateUserData();
                }
            }

            // If the connection between the app and the database encountered an error
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FactorPage.this, "Signing Out", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Method sends an updated User to the database after all of the data has been sorted
     */
    private void setUserToDatabase(){
        try {
            // Make the call to the database for the user using the given user id
            reference.child(Objects.requireNonNull(auth.getCurrentUser()).getUid())
                    .setValue(userProfile).addOnCompleteListener(taskDatabaseCommunications-> {
                if (taskDatabaseCommunications.isSuccessful()) {
                    Toast.makeText(FactorPage.this, "User has been successfully updated!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(FactorPage.this, "Failed to update, try again!", Toast.LENGTH_LONG).show();
                }
                progressBar.setVisibility(View.GONE);
            });
        }catch(NullPointerException e){
            e.printStackTrace();
            Toast.makeText(FactorPage.this, "Error, couldn't get account", Toast.LENGTH_LONG).show();
        }finally {
            progressBar.setVisibility(View.GONE);
        }
    }

    /**
     * Method carries out a sync check on the currentDay Day object, and calls methods that were
     * reliant on the userProfile being returned from the database
     */
    private void populateUserData(){
        final TextView welcomeTextView = findViewById(R.id.welcome);
        final TextView dayTextView = findViewById(R.id.day);

        String fullName = userProfile.fullName;
        Day d = userProfile.getCurrentDay();
        Day toCheck = new Day();

        if(!d.equals(toCheck) && !isOlderDateSelected){
            // Add date for cases if the user didn't add factors on that date, but we still want to
            // keep it as a potential key
            if(!userProfile.getDates().contains(d.getDate())){
                userProfile.getDates().add(d.getDate());
            }
            userProfile.setCurrentDay(new Day());
            String date = userProfile.getCurrentDay().getDate();
            userProfile.getDailySleepFactors().put(date, new Sleep());
            userProfile.getDates().add(date);
        }

        // Handle UI elements that require the User profile
        String welcomeText = "Welcome, " + fullName + "!";
        welcomeTextView.setText(welcomeText);
        dayTextView.setText(userProfile.getDate());

        initDialogs();
        if(doLoadFactorRecyclerAdapter) {
            setFactorInfo();
            setupFactorRecyclerView();
            doLoadFactorRecyclerAdapter = false;
        }
    }

    /**
     * Method initialises the EditTexts for the user input
     *  - checks if the there is data defined already in the Sleep object from the user profile
     *  - Sets up EditText for int values only if it is already present in the user profile
     *  - Sets up time dialogs for entering the time in bed and time out of bed
     */
    private void initDialogs() {
        // Current sleep object, might be null if not defined previously
        Sleep sleep = userProfile.getDailySleepFactors().get(userProfile.getDate());

        // Set sleep quality rating if defined already
        sleepRating = findViewById(R.id.sleepRating);
        if (sleep != null && sleep.getSleepQualityRating() > -1) {
            String rating = Integer.toString(sleep.getSleepQualityRating());
            sleepRating.setText(rating);
        }

        // Set sleep amount if defined already
        sleepAmount = findViewById(R.id.sleepAmount);
        if (sleep != null && sleep.getNumberOfHoursSlept() > -1) {
            String hours = Integer.toString(sleep.getNumberOfHoursSlept());
            sleepAmount.setText(hours);
        }

        // Setup for time in bed sleep start dialog
        sleepStart = findViewById(R.id.sleepStart);
        sleepStartDialog = new TimePickerDialog(
                FactorPage.this,
                (view, hourOfDay, minute) -> { // OnTimeSetListener lambda
                    startHour = hourOfDay;
                    startMin = minute;

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(0, 0, 0, startHour, startMin);

                    sleepStart.setText(DateFormat.format("HH:mm", calendar));
                }, 12, 0, true
        );
        // Disable keyboard prompt
        sleepStart.setInputType(InputType.TYPE_NULL);

        // Allow for repeated clicks to display the dialog
        sleepStart.setOnClickListener(v -> {
            sleepStartDialog.updateTime(startHour, startMin);
            sleepStartDialog.show();
        });

        // Enable highlighted focus of the EditText
        sleepStart.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                sleepStartDialog.updateTime(startHour, startMin);
                sleepStartDialog.show();
            }
        });

        // Set time in bed if defined already
        if (sleep != null && sleep.getTimeInBed() != null) {
            sleepStart.setText(sleep.getTimeInBed());
        }

        // Setup for time left bed sleep end dialog
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

        // Disable keyboard prompt
        sleepEnd.setInputType(InputType.TYPE_NULL);

        // Allow for repeated clicks to display the dialog
        sleepEnd.setOnClickListener(v -> {
            sleepEndDialog.updateTime(endHour, endMin);
            sleepEndDialog.show();
        });

        // Enable highlighted focus of the EditText
        sleepEnd.setOnFocusChangeListener((v, hasFocus) ->{
            if(hasFocus) {
                sleepEndDialog.updateTime(endHour, endMin);
                sleepEndDialog.show();
            }
        });

        // Set time left bed if defined already
        if(sleep != null && sleep.getTimeLeftBed() != null){
            sleepEnd.setText(sleep.getTimeLeftBed());
        }
    }

    /**
     * Method reads in the factors from the strings.xml array, wraps them in a Factor class, and
     * adds them to a List of Factors
     */
    private void setFactorInfo(){
        String[] sleepFactors = getResources().getStringArray(R.array.sleep_factors);
        for(String s : sleepFactors){
            Factor f = new Factor(s);
            if(!factorsList.contains(f)) {
                factorsList.add(f);
            }
        }
    }

    /**
     * Method defines the Factor Recycler view, and the required objects needed to set it up
     */
    private void setupFactorRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.sleepFactorRecyclerView);
        setRecyclerViewOnClickListener();
        FactorRecyclerAdapter adaptor = new FactorRecyclerAdapter(factorsList, listener, this);

        // Layout manager for managing the positioning of the object with respect to the
        // application context
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
            // Check if background can have its colour changed
            if (v.getBackground() instanceof ColorDrawable) {
                // Get the View colour
                ColorDrawable cd = (ColorDrawable) v.getBackground();

                // Get the clicked on TextView embedded in the CustomViewHolder
                TextView tv = v.findViewById(R.id.factorHolder);

                // If the CustomViewHolder was not enabled / had a grey colour, set it to enabled
                // and add the TextView as enabled, removing it from the disabled list
                if (cd.getColor() == getResources().getColor(R.color.factorUnselected)){
                    v.setBackgroundColor(getResources().getColor(R.color.factorSelected));
                    if(!factorViewEnabledList.contains(tv)) {
                        factorViewEnabledList.add(tv);
                    }
                    factorViewDisabledList.remove(tv);
                }else{
                    // Else it was selected prior, and will be unselected
                    v.setBackgroundColor(getResources().getColor(R.color.factorUnselected));
                    if(!factorViewDisabledList.contains(tv)) {
                        factorViewDisabledList.add(tv);
                    }
                    factorViewEnabledList.remove(tv);
                }
            }
        };
    }

    /**
     * Method handles the BottomNavigationView attached to the activity. Will change the pages when
     * the user selects a different icon
     */
    private void bottomNavSetup(){
        BottomNavigationView bottomNavigationView = findViewById(R.id.navView);
        bottomNavigationView.setSelectedItemId(R.id.factorHome);
        bottomNavigationView.setOnItemSelectedListener(item -> {

            // If statements used as switch cases rely on non final strings, which will cause errors
            // in later releases of Android
            if(item.getItemId() == R.id.calendar){
                // Prepare Intent to send the app to the CalendarPage
                Intent calendar = new Intent(getApplicationContext(), CalendarPage.class);
                calendar.putExtra(Utils.USER_KEY, userProfile);
                startActivity(calendar);
                overridePendingTransition(0, 0);
                return true;
            }else if(item.getItemId() == R.id.graph){
                // Prepare Intent to send the app to the GraphPage
                Intent graph = new Intent(getApplicationContext(), GraphPage.class);
                graph.putExtra(Utils.USER_KEY, userProfile);
                startActivity(graph);
                overridePendingTransition(0, 0);
                return true;
            }else return item.getItemId() == R.id.factorHome;
        });
    }

    /**
     * Getter method gets the currently used Sleep object, linked to the current date key
     *
     * @return the currently used Sleep object, linked to the current date key
     */
    public Sleep getCurrentSleep(){
        return userProfile.dailySleepFactors.get(userProfile.getDate());
    }

    /**
     * Setter method adds a TextView of a factor that is already enabled based off of the database
     *
     * @param tv TextView object to add as an enabled TextView factor
     */
    public void addTextViewFactorToFactorViewEnabledList(TextView tv){
        if(!factorViewEnabledList.contains(tv)) {
            factorViewEnabledList.add(tv);
        }
    }
}
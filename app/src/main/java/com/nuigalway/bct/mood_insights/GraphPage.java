package com.nuigalway.bct.mood_insights;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * GraphPage activity class that handles functionality relating to graphing and visualising the
 * entered data of the User
 *
 * @author Karl Gordon
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class GraphPage extends AppCompatActivity implements AdapterView.OnItemSelectedListener, OnChartGestureListener, OnChartValueSelectedListener {

    // Private static final field, holds the Sleep objects
    private static final ArrayList<Sleep> sleeps = new ArrayList<>();
    private static final int LAST_7_DAYS = 0;
    private static final int LAST_30_DAYS = 1;
    private static final int LAST_YEAR = 2;

    // Private static fields
    private static HashMap<String, Integer> sortedFactorEnabledCounter;
    private static List<String> xAxisLabels = new ArrayList<>();
    private static List<String> keys;
    private static int GRAPH_TYPE;
    private static double averageHoursOfSleep = 0;
    private static double averageQualityOfSleep = 0;

    // Private fields
    private FirebaseUser user;
    private DatabaseReference reference;
    private User userProfile;
    private DateKeyParser dkp;
    private List<String> graphs; // Holds the Graph types
    private TextView sleepQualityAverageTextView;
    private TextView hoursAsleepTextView;
    private TextView sleepFactorsTextView;
    private LineChart chart;

    /**
     * onCreate method is the first called upon the instantiation of the activity class,
     * Creates the GraphPage, which displays the data entered by the user from the last 7 days,
     * the last 30 days or the last year.
     *
     * @param savedInstanceState- Bundle, contains the data it most recently supplied in
     *                            onSaveInstanceState.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_page);

        sleepQualityAverageTextView = findViewById(R.id.sleepQualityAverage);
        hoursAsleepTextView = findViewById(R.id.hoursSleptAverage);
        sleepFactorsTextView = findViewById(R.id.sleepFactorSelectionCount);

        setupUser();
        spinnerSetup();
        chartSetup();
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
    private void getUserFromDatabase(String userId){
        // Make the call to the database for the user using the given user id
        reference.child(userId).addValueEventListener(new ValueEventListener() {

            // Once the data is received from the database
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userProfile = snapshot.getValue((User.class));

                if (userProfile != null) {
                    // Set-up called here as it is reliant on an instance of User from the database
                    afterLoadUser();
                }
            }

            // If the connection between the app and the database encountered an error
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GraphPage.this, "Error", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Method called once the User Profile is received, either via an intent or database call
     */
    private void afterLoadUser(){
        // New day set to reset any day selected prior by CalendarPage, or continued from FactorPage
        userProfile.setCurrentDay(new Day());
        dkp = new DateKeyParser(userProfile);
    }

    /**
     * Method sets up the Spinner, with the graph options added into the Spinner object to display
     * to the user
     */
    private void spinnerSetup(){
        graphs = Arrays.asList(getResources().getStringArray(R.array.graph_types));
        Spinner spinner = findViewById(R.id.graph_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(GraphPage.this,
                android.R.layout.simple_spinner_item, graphs);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    /**
     * Method sets up the initial chart settings upon user creation
     */
    private void chartSetup(){
        chart = findViewById(R.id.line_chart);
        chart.setEnabled(false);

        chart.setOnChartGestureListener(this);
        chart.setOnChartValueSelectedListener(this);

        chart.setDragEnabled(true);
        chart.setScaleEnabled(false);

        chart.setBackgroundColor(Color.GRAY);
        chart.getAxisRight().setEnabled(false);
    }

    /**
     * Spinner Adapter method that handles the user selection, 0-2 denoting the three options in the
     * spinner, last 7 days, last 30 days and last year
     *
     * @param parent AdapterView - Parent object of spinner view
     * @param view View - View object of spinner itself
     * @param position - int, element selected in spinner
     * @param id - long, element spinner id
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // Check if the chart has had plot values passed into it already or not
        if(!chart.isEmpty()){
            chart.clearValues();
            xAxisLabels.clear();
        }

        // Handle user selection, the defaulted graph type upon loading is the last 7 days graph
        String graphType = graphs.get(position);
        switch (position){
            case LAST_7_DAYS:
                GRAPH_TYPE = LAST_7_DAYS;
                keys = dkp.lastSevenDays();
                xAxisLabels = dkp.getDaysOnly(keys);
                break;
            case LAST_30_DAYS:
                GRAPH_TYPE = LAST_30_DAYS;
                keys = dkp.lastThirtyDays();
                xAxisLabels = dkp.getDaysOnly(keys);
                break;
            case LAST_YEAR:
                GRAPH_TYPE = LAST_YEAR;
                keys = dkp.lastYear();
                xAxisLabels = dkp.getMonthsOnly(keys);
                break;
        }
        plotDataValuesOntoChart(graphType);
        setAverages();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    /**
     * Method assigns both the XValues and the plot values to the chart, varying depending on the
     * graph type selected.
     *
     * Some UI elements with the lines and graph are also defined
     *
     * @param graphType - String, represents the graph type
     */
    private void plotDataValuesOntoChart(String graphType) {
        XAxis axis = chart.getXAxis();
        axis.setValueFormatter(new XAxisValueFormatter(xAxisLabels));
        axis.setGranularity(1);

        ArrayList<Entry> dataValues = getDataValues();

        LineDataSet plotDataSet = new LineDataSet(dataValues, graphType);
        plotDataSet.setFillAlpha(110);
        plotDataSet.setLineWidth(3f);
        plotDataSet.setValueTextSize(10f);
        plotDataSet.setColor(R.color.red);

        ArrayList<ILineDataSet> dataSetArrayList = new ArrayList<>();
        dataSetArrayList.add(plotDataSet);

        LineData data = new LineData(dataSetArrayList);
        data.setValueTextColor(R.color.teal_700);

        chart.setData(data);
        chart.setEnabled(true);
    }

    /**
     * Method gets the data plot values, by first getting all the Sleep objects for each number of
     * keys. If the key size is larger than 1 month, then average out the output to 12
     *
     * @return ArrayList of entries, which represent the data plots of the sleep quality
     */
    private ArrayList<Entry> getDataValues(){
        ArrayList<Entry> toReturn = new ArrayList<>();
        Map<String, Sleep> factors = userProfile.getDailySleepFactors();

        // Clear sleeps arraylist and populates it again for the amount of keys from the User.
        sleeps.clear();
        Sleep demoDummySleep = new Sleep();
        for (String k : keys) {
            Sleep s = factors.get(k);
            if (s != null) {
                sleeps.add(s);
            } else {
                demoDummySleep.setSleepQualityRating(5);
                sleeps.add(demoDummySleep);
            }
        }

        // If the graph 'last year' is selected
        if(GRAPH_TYPE == LAST_YEAR){
            toReturn = handleAveragingOfMonths();
        }else{
            for(int i = 0; i < sleeps.size(); i++) {
                toReturn.add(new Entry(i, sleeps.get(i).getSleepQualityRating()));
            }
        }
        return toReturn;
    }

    /**
     * Helper method takes the sleep quality ratings of each Sleep object in a month and average out
     * the values by the number of days for each month, for all 12 months of the year
     *
     * @return the 12 averages for each month as an ArrayList of Entries
     */
    private ArrayList<Entry> handleAveragingOfMonths(){
        // Local fields
        ArrayList<Entry> toReturn = new ArrayList<>();
        int sleepIndex = keys.size() - 1; // index for the amount of days for that year
        int exitLoopCondition = 0;
        String key = keys.get(sleepIndex);
        int numCurrDaysIntoMonth = dkp.getDay(key); // gets the amount of days passed this month
        int sum = 0;
        int count = 0;
        int entryIndex = 13;

        // While loop to count the sum of the months Sleep quality rating
        // Loop works backwards, starting from the latest month to the current month
        // Entry list is added from the last element to the first element too
        while (true) {
            sum += sleeps.get(sleepIndex).getSleepQualityRating();
            numCurrDaysIntoMonth--; // work backwards in the month and decrement
            count++;
            sleepIndex--;

            // If the days of the month are all accounted for and it isn't the current month
            if (numCurrDaysIntoMonth == 0 && !(sleepIndex == exitLoopCondition)) {
                // Set average of that month as a new Entry into the ArrayList of Entry objects
                toReturn.add(new Entry(entryIndex, (float) sum / count));
                key = keys.get(sleepIndex);
                sum = 0;
                count = 0;
                entryIndex--;
                numCurrDaysIntoMonth = dkp.getDay(key);
            } else if (sleepIndex == exitLoopCondition) { // it is the current month again
                toReturn.add(new Entry(entryIndex, (float) sum / count));
                Collections.reverse(toReturn); // from 12 --> 1 to 1 --> 12
                break;
            }
        }
        return toReturn;
    }

    /**
     * Method calculates the averages based on the graph type
     */
    private void setAverages(){
        // Call method to calculate the averages of the values, and store them in static fields
        processSleepValues();

        // Add sorted factor values to a Text file, indicating the amount of times factors were
        // selected by the user over the time span selected
        StringBuilder sb = new StringBuilder();
        String temp;
        for (Map.Entry<String, Integer> factorEntry : sortedFactorEnabledCounter.entrySet()) {
            temp = factorEntry.getKey() + ": " + factorEntry.getValue() + ";\n";
            sb.append(temp);
        }
        sleepFactorsTextView.setText(sb.toString());

        // Add the average sleep quality rating over the time span into its appropriate TextView
        temp = "Average Sleep Quality: " + averageQualityOfSleep + ": ";
        sleepQualityAverageTextView.setText(temp);

        // Add the average amount of hours slept over the time span into its appropriate TextView
        temp = "Average time slept (hours): " + averageHoursOfSleep + ";";
        hoursAsleepTextView.setText(temp);
    }

    /**
     * Calculate the average values for fields and lists in the Sleep objects
     *
     */
    private void processSleepValues(){
        int divisor = keys.size(); // 7, or 30, or 365/366 days
        String[] factorKeys = getResources().getStringArray(R.array.sleep_factors);
        HashMap<String, Integer> factorEnabledCounter = new HashMap<>();
        Map<String, Boolean> sleepFactors;
        int sleepQualityAverage = 0;
        int numHoursSlept = 0;

        // Parse through each Sleep object in sleeps, which was defined previously by the
        // getDataValues() function, called when calculating the chart & filling the data plots
        for(Sleep s : sleeps){
            sleepFactors = s.getSleepFactors();
            // Sort through sleep factors, check if it is enabled for that sleep object and then
            // increment the value for the HashMap of factor enabled counter.
            for(String factor : factorKeys) {
                Boolean bool = sleepFactors.get(factor);
                if(bool != null && bool){
                    if(factorEnabledCounter.containsKey(factor)){
                        Integer currCount = factorEnabledCounter.get(factor);
                        currCount += 1;
                        factorEnabledCounter.replace(factor, currCount);
                    }else{
                        factorEnabledCounter.put(factor, 1);
                    }
                }
            }

            // add the sleep quality rating and amount of hours slept into sum fields
            sleepQualityAverage += s.getSleepQualityRating();
            numHoursSlept += s.getNumberOfHoursSlept();
        }


        sortedFactorEnabledCounter = factorEnabledCounter.entrySet().stream()
                // Sort the list such that the most enabled factor within the time span is first
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                // Map the result to a new HashMap, by collecting the entries and adding them to a
                // LinkedHashMap that is returned once fully collected
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        // calculate the mean average of the sleep quality rating and amount of hours slept
        averageQualityOfSleep = (double) sleepQualityAverage / divisor;
        averageHoursOfSleep = (double) numHoursSlept / divisor;
    }

    /**
     * Method handles the BottomNavigationView attached to the activity. Will change the pages when
     * the user selects a different icon
     */
    private void bottomNavSetup(){
        BottomNavigationView bottomNavigationView = findViewById(R.id.navView);
        bottomNavigationView.setSelectedItemId(R.id.graph);
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
            }else if(item.getItemId() == R.id.factorHome){
                // Prepare Intent to send the app to the FactorPage
                Intent factors = new Intent(getApplicationContext(), FactorPage.class);
                factors.putExtra(Utils.USER_KEY, userProfile);
                startActivity(new Intent(getApplicationContext(), FactorPage.class));
                overridePendingTransition(0, 0);
                return true;
            }else return item.getItemId() == R.id.graph;
        });
    }


    // Private inner class that is used to hold and return the appropriate X-Axis labels for the
    // chart graph of user factor data
    private static class XAxisValueFormatter extends IndexAxisValueFormatter {
        private final List<String> listOfValues;

        public XAxisValueFormatter(List<String> values){
            super(values);
            this.listOfValues = values;
        }

        @Override
        public String getFormattedValue(float value){
            return listOfValues.get((int) value);
        }

    }

    /* Start Interface Methods used for OnChartGestureListener */
    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartLongPressed(MotionEvent me) {

    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {

    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {

    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {

    }

    /* End Interface Methods used for OnChartGestureListener */
    /* Start Interface Methods used forOnChartValueSelectedListener */
    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }
    /* End Interface Methods used forOnChartValueSelectedListener */
}
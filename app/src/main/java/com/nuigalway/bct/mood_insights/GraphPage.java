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

@RequiresApi(api = Build.VERSION_CODES.O)
public class GraphPage extends AppCompatActivity implements AdapterView.OnItemSelectedListener, OnChartGestureListener, OnChartValueSelectedListener {

    private static int GRAPH_TYPE;
    private static List<String> keys;
    private static List<String> xAxisLabels = new ArrayList<>();
    private static final ArrayList<Sleep> sleeps = new ArrayList<>();
    private static HashMap<String, Integer> sortedFactors;
    private static double averageHoursOfSleep = 0;
    private static double averageQualityOfSleep = 0;

    private FirebaseUser user;
    private DatabaseReference reference;
    private User userProfile;
    private DateKeyParser dkp;

    private List<String> graphs;
    private TextView sleepQualityAverageTextView;
    private TextView hoursAsleepTextView;
    private TextView sleepFactorsTextView;
    private LineChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_page);

        sleepQualityAverageTextView = findViewById(R.id.sleepQualityAverage);
        hoursAsleepTextView = findViewById(R.id.hoursSleptAverage);
        sleepFactorsTextView = findViewById(R.id.sleepFactorSelectionCount);

        userSetup();
        bottomNavSetup();
    }

    private void spinnerSetup(){
        graphs = Arrays.asList(getResources().getStringArray(R.array.graph_types));
        Spinner spinner = findViewById(R.id.graph_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(GraphPage.this,
                android.R.layout.simple_spinner_item, graphs);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

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

    private void bottomNavSetup(){
        BottomNavigationView bottomNavigationView = findViewById(R.id.navView);
        bottomNavigationView.setSelectedItemId(R.id.graph);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if(item.getItemId() == R.id.calendar){
                Intent calendar = new Intent(getApplicationContext(), CalendarPage.class);
                calendar.putExtra(Utils.USER_KEY, userProfile);
                startActivity(calendar);
                overridePendingTransition(0, 0);
                return true;
            }else if(item.getItemId() == R.id.factorHome){
                Intent factors = new Intent(getApplicationContext(), FactorPage.class);
                factors.putExtra(Utils.USER_KEY, userProfile);
                startActivity(new Intent(getApplicationContext(), FactorPage.class));
                overridePendingTransition(0, 0);
                return true;
            }else return item.getItemId() == R.id.graph;
        });
    }

    private void userSetup(){
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
            spinnerSetup();
            chartSetup();
        }
    }

    private void getUserFromDatabase(String userId){
        reference.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userProfile = snapshot.getValue((User.class));

                if (userProfile != null) {
                    // Set-up called here as it is reliant on an instance of User from the database
                    dkp = new DateKeyParser(userProfile);
                    spinnerSetup();
                    chartSetup();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GraphPage.this, "Error", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Spinner Adapter method
     * @param parent AdapterView - Parent object of spinner view
     * @param view View - View object of spinner itself
     * @param position - int, element selected in spinner
     * @param id - long, element spinner id
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(!chart.isEmpty()){
            chart.clearValues();
            xAxisLabels.clear();
        }
        String graphType = graphs.get(position);
        switch (position){
            case 0:
                GRAPH_TYPE = 0;
                keys = dkp.lastSevenDays();
                xAxisLabels = dkp.getDaysOnly(keys);
                populateData(graphType);
                setAverages();
                break;
            case 1:
                GRAPH_TYPE = 1;
                keys = dkp.lastThirtyDays();
                xAxisLabels = dkp.getDaysOnly(keys);
                populateData(graphType);
                setAverages();
                break;
            case 2:
                GRAPH_TYPE = 2;
                keys = dkp.lastYear();
                xAxisLabels = dkp.getMonthsOnly(keys);
                populateData(graphType);
                setAverages();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void populateData(String graphType) {
        XAxis axis = chart.getXAxis();
        axis.setValueFormatter(new XAxisValueFormatter(xAxisLabels));
        axis.setGranularity(1);

        ArrayList<Entry> plotValues = getDataValues();

        LineDataSet setA = new LineDataSet(plotValues, graphType);
        setA.setFillAlpha(110);
        setA.setLineWidth(3f);
        setA.setValueTextSize(10f);
        setA.setColor(R.color.red);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(setA);

        LineData data = new LineData(dataSets);
        data.setValueTextColor(R.color.teal_700);

        chart.setData(data);
        chart.setEnabled(true);
    }

    private ArrayList<Entry> getDataValues(){
        ArrayList<Entry> toReturn = new ArrayList<>();
        Map<String, Sleep> factors = userProfile.getDailySleepFactors();
        sleeps.clear();
        Sleep demoDummySleep = new Sleep();
        for (String k : keys) {
            Sleep s = factors.get(k);
            if (s != null) {
                sleeps.add(s);
            } else {
                demoDummySleep.setSleepQualityRating(5);
                sleeps.add(demoDummySleep);
                //sleeps.add(new Sleep());
            }
        }

        if(keys.size() > 31){
            toReturn = handleAveragingOfMonths();
        }else{
            for(int i = 0; i < sleeps.size(); i++) {
                toReturn.add(new Entry(i, sleeps.get(i).getSleepQualityRating()));
            }
        }
        return toReturn;
    }

    private ArrayList<Entry> handleAveragingOfMonths(){
        ArrayList<Entry> toReturn = new ArrayList<>();
        int sleepIndex = keys.size() - 1;
        int exitLoopCondition = 0;
        int nextIndex = -1;
        String key = keys.get(sleepIndex);
        int numCurrDaysIntoMonth = dkp.getDay(key);
        int sum = 0;
        int count = 0;
        int entryIndex = 13;

        while (true) {
            sum += sleeps.get(sleepIndex).getSleepQualityRating();
            numCurrDaysIntoMonth--;
            count++;
            sleepIndex += nextIndex;
            if (numCurrDaysIntoMonth == 0 && !(sleepIndex == exitLoopCondition)) {
                toReturn.add(new Entry(entryIndex, (float) sum / count));
                key = keys.get(sleepIndex);
                sum = 0;
                count = 0;
                entryIndex--;
                numCurrDaysIntoMonth = dkp.getDay(key);
            } else if (sleepIndex == exitLoopCondition) {
                // Get
                toReturn.add(new Entry(entryIndex, (float) sum / count));
                Collections.reverse(toReturn);
                break;
            }
        }
        return toReturn;
    }

    private void setAverages(){
        int divisor;

        switch(GRAPH_TYPE){
            case 0:
                divisor = 7;
                break;
            case 1:
                divisor = 30;
                break;
            case 2:
                divisor = keys.size(); //365 or 366
                break;
            default:
                divisor = -1;
                break;
        }
        processSleepValues(divisor);
        StringBuilder sb = new StringBuilder();
        String temp;
        for (Map.Entry<String, Integer> factorEntry : sortedFactors.entrySet()) {
            temp = factorEntry.getKey() + ": " + factorEntry.getValue() + ";\n";
            sb.append(temp);
        }
        temp = "Average Sleep Quality: " + averageQualityOfSleep + ": ";
        sleepQualityAverageTextView.setText(temp);

        temp = "Average time slept (hours): " + averageHoursOfSleep + ";";
        hoursAsleepTextView.setText(temp);

        sleepFactorsTextView.setText(sb.toString());
    }

    private void processSleepValues(int divisor){
        String[] factorKeys = getResources().getStringArray(R.array.sleep_factors);
        HashMap<String, Integer> factors = new HashMap<>();
        Map<String, Boolean> sleepFactors;
        int sleepQualityAverage = 0;
        int numHoursSlept = 0;

        for(Sleep s : sleeps){
            sleepFactors = s.getSleepFactors();
            for(String factor : factorKeys) {
                Boolean bool = sleepFactors.get(factor);
                if(bool != null && bool){
                    if(factors.containsKey(factor)){
                        Integer currCount = factors.get(factor);
                        currCount += 1;
                        factors.replace(factor, currCount);
                    }else{
                        factors.put(factor, 1);
                    }
                }
            }

            sleepQualityAverage += s.getSleepQualityRating();
            numHoursSlept += s.getNumberOfHoursSlept();
        }


        sortedFactors = factors.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        averageQualityOfSleep = (double) sleepQualityAverage / divisor;
        averageHoursOfSleep = (double) numHoursSlept / divisor;
    }


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

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }
}
package com.nuigalway.bct.mood_insights.user;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.nuigalway.bct.mood_insights.data.Day;
import com.nuigalway.bct.mood_insights.data.Mood;
import com.nuigalway.bct.mood_insights.data.Sleep;
import com.nuigalway.bct.mood_insights.data.Symptoms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.O)
public class User {
    public final List<Day> days = new ArrayList<>();
    public final Map<String, Mood> dailyMoodFactors = new HashMap<>();
    public final Map<String, Sleep> dailySleepFactors = new HashMap<>();
    public final Map<String, Symptoms> dailySymptomFactors = new HashMap<>();

    public String fullName, age, email;

    private Day currentDay;

    public User(){

    }

    public User(String fullName, String age, String email) {
        this.fullName = fullName;
        this.age = age;
        this.email = email;
        createNewDay();
    }

    public void createNewDay(){
        setCurrentDay(new Day());
        String date = currentDay.getDate();
        dailyMoodFactors.put(date, new Mood());
        dailySleepFactors.put(date, new Sleep());
        dailySymptomFactors.put(date, new Symptoms());
        days.add(currentDay);
    }

    public Day getCurrentDay() {
        return currentDay;
    }

    public String getDate(){
        return currentDay.getDate();
    }

    public void setCurrentDay(Day currentDay) {
        this.currentDay = currentDay;
    }

    public List<Day> getDays() {
        return days;
    }

    public Map<String, Mood> getDailyMoodFactors() {
        return dailyMoodFactors;
    }

    public Map<String, Sleep> getDailySleepFactors() {
        return dailySleepFactors;
    }

    public Map<String, Symptoms> getDailySymptomFactors() {
        return dailySymptomFactors;
    }
}

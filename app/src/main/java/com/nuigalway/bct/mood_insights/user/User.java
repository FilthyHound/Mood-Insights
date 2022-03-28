package com.nuigalway.bct.mood_insights.user;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.nuigalway.bct.mood_insights.data.Day;
import com.nuigalway.bct.mood_insights.data.Sleep;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.O)
public class User implements Serializable {
    public final ArrayList<String> dates = new ArrayList<>();
    public final Map<String, Sleep> dailySleepFactors = new HashMap<>();

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
        currentDay = new Day();
        String date = currentDay.getDate();
        dailySleepFactors.put(date, new Sleep());
        dates.add(date);
    }

    public void updateNewDay(){
        if(!dates.contains(currentDay.getDate())){
            dates.add(currentDay.getDate());
        }
        currentDay = new Day();
        String date = currentDay.getDate();
        dailySleepFactors.put(date, new Sleep());
        dates.add(date);
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

    public boolean addDate(String date){
        if(date != null && !dates.contains(date)){
            dates.add(date);
            return true;
        }
        return false;
    }

    public ArrayList<String> getDates(){
        return dates;
    }

    public Map<String, Sleep> getDailySleepFactors() {
        return dailySleepFactors;
    }
}

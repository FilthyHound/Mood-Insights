package com.nuigalway.bct.mood_insights.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sleep {
    private double numberOfHoursSlept;
    private int sleepQualityRating;
    private String timeInBed, timeLeftBed;
    private final Map<String, Boolean> sleepFactors;

    public Sleep(){
        sleepFactors = new HashMap<>();
    }

    public void addSleepFactor(String factor){
        if(!sleepFactors.containsKey(factor)){
            sleepFactors.put(factor, false);
        }
    }

    public boolean updateSleepFactor(String factor, boolean condition){
        if(sleepFactors.containsKey(factor)){
            sleepFactors.put(factor, condition);
            return true;
        }
        return false;
    }

    public void removeSleepFactor(String factor){
         sleepFactors.remove(factor);
    }

    public Boolean getSleepFactorValue(String factor){
        if(sleepFactors.containsKey(factor)){
            return sleepFactors.get(factor);
        }
        return false;
    }

    public Map<String, Boolean> getSleepFactors(){
        return sleepFactors;
    }

    public double getNumberOfHoursSlept() {
        return numberOfHoursSlept;
    }

    public void setNumberOfHoursSlept(double numberOfHoursSlept) {
        this.numberOfHoursSlept = numberOfHoursSlept;
    }

    public int getSleepQualityRating() {
        return sleepQualityRating;
    }

    public void setSleepQualityRating(int sleepQualityRating) {
        this.sleepQualityRating = sleepQualityRating;
    }

    public String getTimeInBed() {
        return timeInBed;
    }

    public void setTimeInBed(String timeInBed) {
        this.timeInBed = timeInBed;
    }

    public String getTimeLeftBed() {
        return timeLeftBed;
    }

    public void setTimeLeftBed(String timeLeftBed) {
        this.timeLeftBed = timeLeftBed;
    }
}

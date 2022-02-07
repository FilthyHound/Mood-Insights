package com.nuigalway.bct.mood_insights.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Sleep {

    private double numberOfHoursSlept;
    private int sleepQualityRating;
    private String timeInBed, timeLeftBed;
    private final Map<String, Integer> sleepFactors;

    public Sleep(){
        sleepFactors = new HashMap<>();
    }

    public void addSleepFactor(String factor){
        if(!sleepFactors.containsKey(factor)){
            sleepFactors.put(factor, 0);
        }
    }

    public void removeSleepFactor(String factor){
         sleepFactors.remove(factor);
    }

    public Integer getSleepFactorValue(String factor){
        if(sleepFactors.containsKey(factor)){
            return sleepFactors.get(factor);
        }
        return -1;
    }

    public Map<String, Integer> getSleepFactors(){
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

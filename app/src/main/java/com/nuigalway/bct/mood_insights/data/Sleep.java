package com.nuigalway.bct.mood_insights.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Sleep class holds the sleep factors of a User, with methods to update, add retrieve Sleep data
 *
 * @author Karl Gordon
 */
public class Sleep implements Serializable {
    private final Map<String, Boolean> sleepFactors;
    private int numberOfHoursSlept = 0;
    private int sleepQualityRating = 0;
    private String timeInBed = "", timeLeftBed = "";

    /**
     * Constructor class creates the Sleep object, initialises the HashMap to hold the sleep factors
     */
    public Sleep(){
        sleepFactors = new HashMap<>();
    }

    /**
     * Method adds a sleep factor to a Sleep instance
     *
     * @param factor - String, key to be placed into the internal HashMap, its value is set to true
     *               upon being placed into the HashMap
     */
    public void addSleepFactor(String factor){
        if(!sleepFactors.containsKey(factor)){
            sleepFactors.put(factor, true);
        }
    }

    /**
     * Method updates a string factor to to true or false, only if said factor is already in the
     * HashMap, otherwise it will do nothing
     *
     * @param factor - String key of the factor to have its value updated
     * @param condition - boolean, new value of the factor
     */
    public void updateSleepFactor(String factor, boolean condition){
        if(sleepFactors.containsKey(factor)){
            sleepFactors.put(factor, condition);
        }
    }

    /**
     * Getter method which gets the sleep factor value if present, will return false otherwise
     *
     * @param factor - String key to get its value
     * @return boolean value of the factor
     */
    public Boolean getSleepFactorValue(String factor){
        if(sleepFactors.containsKey(factor)){
            return sleepFactors.get(factor);
        }
        return false;
    }

    /**
     * Wrapper method which checks and returns a boolean to state if a factor is present or not,
     * used to dictate whether to update or add a factor to the HashMap
     *
     * @param factor - String key to check if present in the HashMap
     * @return boolean value of the resulting check
     */
    public Boolean containsSleepFactor(String factor){
        return sleepFactors.containsKey(factor);
    }

    /**
     * Getter method returns the sleepFactors HashMap
     *
     * @return sleep factors
     */
    public Map<String, Boolean> getSleepFactors(){
        return sleepFactors;
    }

    /**
     * Getter method returning the number of hours slept by the User
     *
     * @return number of hours slept by the User
     */
    public int getNumberOfHoursSlept() {
        return numberOfHoursSlept;
    }

    /**
     * Setter method sets the number of hours slept by the User
     *
     * @param numberOfHoursSlept - int, number of hours slept by the User
     */
    public void setNumberOfHoursSlept(int numberOfHoursSlept) {
        this.numberOfHoursSlept = numberOfHoursSlept;
    }

    /**
     * Getter method returns the sleep quality rating of the User
     *
     * @return the sleep quality rating of the User
     */
    public int getSleepQualityRating() {
        return sleepQualityRating;
    }

    /**
     * Setter method sets the sleep quality rating of the User
     *
     * @param sleepQualityRating - int, the sleep quality rating of the User
     */
    public void setSleepQualityRating(int sleepQualityRating) {
        this.sleepQualityRating = sleepQualityRating;
    }

    /**
     * Getter method gets the time the User went to bed
     *
     * @return the time the User went to bed
     */
    public String getTimeInBed() {
        return timeInBed;
    }

    /**
     * Setter method sets the time the User went to bed
     *
     * @param timeInBed - String, the time the User went to bed
     */
    public void setTimeInBed(String timeInBed) {
        this.timeInBed = timeInBed;
    }

    /**
     * Getter method gets the Time the user left the bed
     *
     * @return the Time the user left the bed
     */
    public String getTimeLeftBed() {
        return timeLeftBed;
    }

    /**
     * Setter method sets the Time the user left the bed
     *
     * @param timeLeftBed - String, the Time the user left the bed
     */
    public void setTimeLeftBed(String timeLeftBed) {
        this.timeLeftBed = timeLeftBed;
    }
}

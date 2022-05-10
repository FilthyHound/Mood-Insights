package com.nuigalway.bct.mood_insights.user;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.nuigalway.bct.mood_insights.data.Day;
import com.nuigalway.bct.mood_insights.data.Sleep;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * User class is the main class that holds the Users data, where all other data / wrapper classes
 * are linked to
 *
 * @author Karl Gordon
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class User implements Serializable {
    // Public final fields
    public final ArrayList<String> dates = new ArrayList<>();
    public final Map<String, Sleep> dailySleepFactors = new HashMap<>();

    // Public fields
    public String fullName, age, email;

    // Private field
    private Day currentDay;

    /**
     * Constructor method, used when loading the User from the database
     */
    public User(){}

    /**
     * Constructor method, used in the registering of the User for the first time starting the app.
     * This method also creates a new day to initialise the User with a current user.
     *
     * @param fullName - String, represents the Users full name
     * @param age - String, represents the users age
     * @param email - String, represents the users email
     */
    public User(String fullName, String age, String email) {
        this.fullName = fullName;
        this.age = age;
        this.email = email;

        createNewDay();
    }

    /**
     * Method initialises a Day object, which creates the first objects to store data to for that
     * specific day
     */
    private void createNewDay(){
        currentDay = new Day();
        String date = currentDay.getDate();
        dailySleepFactors.put(date, new Sleep());
        dates.add(date);
    }

    /**
     * Getter method that returns the current Day object
     *
     * @return current Day object
     */
    public Day getCurrentDay() {
        return currentDay;
    }

    /**
     * Getter method that returns the Date representation of the current Day object
     *
     * @return the Date representation of the current Day object
     */
    public String getDate(){
        return currentDay.getDate();
    }

    /**
     * Setter method sets the current Day object
     *
     * @param currentDay - Day, the current Day object
     */
    public void setCurrentDay(Day currentDay) {
        this.currentDay = currentDay;
    }

    /**
     * Getter method returns the dates ArrayList, the keys from which to get factor data with
     *
     * @return the dates ArrayList
     */
    public ArrayList<String> getDates(){
        return dates;
    }

    /**
     * Getter method retuns the Map of User sleep factors
     *
     * @return the Map of User sleep factors
     */
    public Map<String, Sleep> getDailySleepFactors() {
        return dailySleepFactors;
    }
}

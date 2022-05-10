package com.nuigalway.bct.mood_insights.data;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;

/**
 * Day wrapper class holds a single LocalDate instance and gets a date based on said
 * LocalDate instance
 *
 * @author Karl Gordon
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class Day implements Serializable {
    // LocalDate instance, once set, it cannot change
    private final LocalDate instance;

    /**
     * Constructor class, initialises the current Date
     */
    public Day(){
        instance = LocalDate.now();
    }

    /**
     * Constructor class, initialises a LocalDate based on parameters
     *
     * @param dayOfMonth - int, day of the month
     * @param month - Month, day of the month
     * @param year - int, the year
     */
    public Day(int dayOfMonth, Month month, int year){
        instance = LocalDate.of(year, month, dayOfMonth);
    }

    /**
     * Get String representation of the LocalDate object, acts as a key for the User class
     * @return - date, key representation of LocalDate object
     */
    public String getDate(){
        return getDayOfMonth() + "-" + getMonth() + "-" + getYear();
    }

    /**
     * Getter method returns the day of the week
     *
     * @return day of the week
     */
    public DayOfWeek getDayOfWeek(){
        return instance.getDayOfWeek();
    }

    /**
     * Getter method returns the day of the month
     *
     * @return day of the month
     */
    public int getDayOfMonth(){
        return instance.getDayOfMonth();
    }

    /**
     * Getter method returns the month
     *
      * @return month
     */
    public Month getMonth(){
        return instance.getMonth();
    }

    /**
     * Getter method returns the year
     *
     * @return the year
     */
    public int getYear(){
        return instance.getYear();
    }

    /**
     * Overridden equals method, which compares the day of the week, day of the month
     * the month and year of a Day object, and returns equal if they are true
     *
     * This is done to avoid having to create a LocalDate instance that match exactly in time.
     */
    @Override
    public boolean equals(Object o){
        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }

        // Check if o is an instance of Day or not
        if (!(o instanceof Day)) {
            return false;
        }

        Day toCheck = (Day) o;

        return getDayOfWeek().equals(toCheck.getDayOfWeek())
                && getDayOfMonth() == toCheck.getDayOfMonth()
                && getMonth().equals(toCheck.getMonth())
                && getYear() == toCheck.getYear();
    }
}

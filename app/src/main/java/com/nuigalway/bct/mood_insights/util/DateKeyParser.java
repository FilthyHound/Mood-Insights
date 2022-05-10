package com.nuigalway.bct.mood_insights.util;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.nuigalway.bct.mood_insights.user.User;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * DateKeyParser class parses the Date keys and returns sets of keys, derived from the date key
 * in the User class
 *
 * @author Karl Gordon
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class DateKeyParser {
    // Private final User
    private final User user;

    // Private fields
    private int currentDayOfMonth, currentYear;
    private Month currentMonth;
    private LocalDate currDate;

    /**
     * Constructor method, takes in the User from which to get the current LocalDate object from,
     * based off of the date key
     *
     * @param user User, main User profile
     */
    public DateKeyParser(User user){
        this.user = user;
        parseCurrentDate();
    }

    /**
     * Method parses the date key, and initialises a LocalDate object based on this key
     */
    private void parseCurrentDate(){
        String date = user.getDate();
        String[] dateArray = date.split(Utils.HYPHEN);
        currentDayOfMonth = Integer.parseInt(dateArray[0]);
        currentMonth = Month.valueOf(dateArray[1]);
        currentYear = Integer.parseInt(dateArray[2]);
        currDate = LocalDate.of(currentYear, currentMonth, currentDayOfMonth);
    }

    /**
     * Method checks that the date selected from the calendar is a valid date to add factors to, by
     * comparing it to the current LocalDate day object
     *
     * @param dayOfMonth - int, the day of the month, from the selected date
     * @param month - Month, the month, from the selected date
     * @param year - int, the year, from the selected date
     * @return result of the check
     */
    public boolean isCalendarDateValid(int dayOfMonth, Month month, int year){
        LocalDate dateToCheck = LocalDate.of(year, month, dayOfMonth);
        return dateToCheck.isBefore(currDate) || dateToCheck.isEqual(currDate);
    }

    /**
     * Method returns the keys 7 days after the current date, but including the current dates key
     *
     * @return the keys of the last 7 days, including the current date key
     */
    public List<String> lastSevenDays(){
        List<LocalDate> dates = new ArrayList<>();
        dates.add(currDate);

        for(int i = 1; i < 7; i++){
            dates.add(currDate.minusDays(i));
        }
        return sortLocalDates(dates);
    }

    /**
     * Method returns the keys 30 days after the current date, but including the current dates key
     *
     * @return the keys of the last 30 days, including the current date key
     */
    public List<String> lastThirtyDays(){
        List<LocalDate> dates = new ArrayList<>();
        dates.add(currDate);

        for(int i = 1; i < 30; i++){
            dates.add(currDate.minusDays(i));
        }
        return sortLocalDates(dates);
    }

    /**
     * Method returns the keys 365 / 366 after the current date, but including the current dates key
     * This is dependent on the amount of days between the current day and month one year apart from
     * each other.
     *
     * @return the keys of the last 365/366 days, including the current date key
     */
    public List<String> lastYear(){
        List<LocalDate> dates = new ArrayList<>();
        LocalDate aYearBefore = LocalDate.of(currentYear - 1, currentMonth, currentDayOfMonth);
        long limiter = ChronoUnit.DAYS.between(aYearBefore, currDate);
        dates.add(currDate);

        for(int i = 1; i < limiter; i++){
            dates.add(currDate.minusDays(i));
        }
        return sortLocalDates(dates);
    }

    /**
     * Getter method returns the day value from the passed in date key string
     *
     * @param key String, key from which to get the day from
     * @return the day, based off of the key
     */
    public int getDay(String key){
        return Integer.parseInt(key.split(Utils.HYPHEN)[0]);
    }

    /**
     * Method returns only the days from a list of keys, also appends a suffix to the days returned
     *
     * @param keys List of Strings, keys from which to get the days from
     * @return List of Strings, days based off of the list of keys, with an appended suffix
     */
    public ArrayList<String> getDaysOnly(List<String> keys){
        ArrayList<String> toReturn = new ArrayList<>();
        keys.forEach(s -> toReturn.add(appendSuffix(s.split(Utils.HYPHEN)[0])));
        return toReturn;
    }

    /**
     * Method returns only the months from a list of keys, shortened to three letters only
     * eg MARCH is sent as MAR, JANUARY as JAN, etc
     *
     * @param keys List of Strings, keys from which to get the months from
     * @return List of Strings, months based off of the list of keys, now shortened
     */
    public ArrayList<String> getMonthsOnly(List<String> keys){
        ArrayList<String> toReturn = new ArrayList<>();
        String month;
        int counter = 0;

        for(String s : keys){
            month = shortenMonthName(s.split(Utils.HYPHEN)[1]);
            // Add the first month found
            if(toReturn.isEmpty()) {
                toReturn.add(counter, month);
                continue;
            }

            // Will go through and add the next month after being incremented. This is done so
            // at the end, where the fist month is found again, it's re-add. This is done to
            // help balance out the XAxis Labels in the Graph Chart in GraphPage.java
            if(!toReturn.get(counter).equals(month)){
                counter++;
                toReturn.add(counter, month);
            }
        }
        return toReturn;
    }

    /**
     * Method takes in a String representation of a day of the month number, and appends a suffix
     * to it based off of its value in the month
     *
     * @param day - String, representing the day value
     * @return day String with an appended suffix
     */
    public String appendSuffix(String day) {
        // Convert string into a number and store it in order to compare its value
        int n;
        try {
            n = Integer.parseInt(day);
        }catch (NumberFormatException e){
            return "NaN";
        }

        // Check for exception cases, since the method only deals with a maximum of 31 days, numbers
        // such as 111 <--> 113, etc don't need to be accounted for
        if (n >= 11 && n <= 13) {
            return day + "th";
        }

        // Switch statement assigns a suffix based off of the days value modulus ten
        switch (n % 10) {
            case 1:  return day + "st";
            case 2:  return day + "nd";
            case 3:  return day + "rd";
            default: return day + "th";
        }
    }

    /**
     * Getter method returns the first three characters of a String representing the Month
     *
     * @param month String, representation of the Month
     * @return String, the firs three characters of the String representing the Month
     */
    public String shortenMonthName(String month){
        return month.substring(0, 3);
    }

    /**
     * Method sorts the List of LocalDate keys, such that the oldest is the first element and the
     * newest date is the last element. Then returns the sorted LocalDates in the key String
     * representation.
     *
     * @param dates List of LocalDate, dates to be sorted
     * @return List of Strings, keys representing the sorted LocalDates
     */
    private List<String> sortLocalDates(List<LocalDate> dates){
        List<String> keys = new ArrayList<>();
        dates.sort(Comparator.comparing(LocalDate::getChronology));
        Collections.reverse(dates);
        dates.forEach(d ->
                keys.add(d.getDayOfMonth()
                        + Utils.HYPHEN + d.getMonth()
                        + Utils.HYPHEN + d.getYear())
        );
        return keys;
    }
}

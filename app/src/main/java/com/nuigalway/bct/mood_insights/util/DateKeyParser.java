package com.nuigalway.bct.mood_insights.util;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.nuigalway.bct.mood_insights.user.User;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.O)
public class DateKeyParser {
    private static final String HYPHEN = "-";
    private final User user;
    private int currentDayOfMonth, currentYear;
    private Month currentMonth;
    private LocalDate currDate;

    public DateKeyParser(User user){
        this.user = user;
        parseCurrentDate();
    }

    private void parseCurrentDate(){
        String date = user.getDate();
        String[] dateArray = date.split(HYPHEN);
        currentDayOfMonth = Integer.parseInt(dateArray[0]);
        currentMonth = Month.valueOf(dateArray[1]);
        currentYear = Integer.parseInt(dateArray[2]);
        currDate = LocalDate.of(currentYear, currentMonth, currentDayOfMonth);
    }

    public boolean parseCalendarDate(int dayOfMonth, Month month, int year){
        return year <= currentYear && month.getValue() <= currentMonth.getValue() && dayOfMonth < currentDayOfMonth;
    }

    public List<String> lastSevenDays(){
        List<LocalDate> dates = new ArrayList<>();
        dates.add(currDate);

        for(int i = 1; i < 7; i++){
            dates.add(currDate.minusDays(i));
        }
        return sortLocalDates(dates);
    }

    public List<String> lastThirtyDays(){
        List<LocalDate> dates = new ArrayList<>();
        dates.add(currDate);

        for(int i = 1; i < 30; i++){
            dates.add(currDate.minusDays(i));
        }
        return sortLocalDates(dates);
    }

    public List<String> lastYear(){
        List<LocalDate> dates = new ArrayList<>();
        dates.add(currDate);

        for(int i = 1; i < 365; i++){
            dates.add(currDate.minusDays(i));
        }
        return sortLocalDates(dates);
    }

    public int getDay(String key){
        return Integer.parseInt(key.split(HYPHEN)[0]);
    }

    public ArrayList<String> getDaysOnly(List<String> keys){
        ArrayList<String> toReturn = new ArrayList<>();
        keys.forEach(s -> toReturn.add(appendSuffix(s.split(HYPHEN)[0])));
        return toReturn;
    }

    public ArrayList<String> getMonthsOnly(List<String> keys){
        ArrayList<String> toReturn = new ArrayList<>();
        String month;
        int counter = 0;
        for(String s : keys){
            month = shortenMonthName(s.split(HYPHEN)[1]);
            if(toReturn.isEmpty()) {
                toReturn.add(counter, month);
                continue;
            }

            if(!toReturn.get(counter).equals(month)){
                counter++;
                toReturn.add(counter, month);
            }
        }
        return toReturn;
    }

    public String appendSuffix(String day) {
        int n;
        try {
            n = Integer.parseInt(day);
        }catch (NumberFormatException e){
            return "NaN";
        }

        if (n >= 11 && n <= 13) {
            return day + "th";
        }

        switch (n % 10) {
            case 1:  return day + "st";
            case 2:  return day + "nd";
            case 3:  return day + "rd";
            default: return day + "th";
        }
    }

    public String shortenMonthName(String month){
        return month.substring(0, 3);
    }

    private List<String> sortLocalDates(List<LocalDate> dates){
        List<String> keys = new ArrayList<>();
        dates.sort(Comparator.comparing(LocalDate::getChronology));
        Collections.reverse(dates);
        dates.forEach(d -> keys.add(d.getDayOfMonth() + "-" + d.getMonth() + "-" + d.getYear()));
        return keys;
    }
}

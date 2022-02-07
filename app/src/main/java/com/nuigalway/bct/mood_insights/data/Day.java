package com.nuigalway.bct.mood_insights.data;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;

@RequiresApi(api = Build.VERSION_CODES.O)
public class Day{
    private final LocalDate instance;

    public Day(){
        instance = LocalDate.now();
    }

    public String getDate(){
        return getDayOfMonth() + "-" + getMonth() + "-" + getYear();
    }

    public DayOfWeek getDayOfWeek(){
        return instance.getDayOfWeek();
    }

    public int getDayOfMonth(){
        return instance.getDayOfMonth();
    }

    public Month getMonth(){
        return instance.getMonth();
    }

    public int getYear(){
        return instance.getYear();
    }

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

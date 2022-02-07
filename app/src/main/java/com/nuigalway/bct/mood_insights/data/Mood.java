package com.nuigalway.bct.mood_insights.data;

import java.util.HashMap;
import java.util.Map;

public class Mood {
    public static String HAPPY = "happy";
    public static String OK = "ok";
    public static String SAD = "sad";
    public static String ANGRY = "angry";
    public static String STRESSED = "stressed";

    private int moodRating;
    private final Map<String, Integer> moodDescriptor;

    public Mood(){
        moodDescriptor = new HashMap<>();
        moodDescriptor.put(HAPPY, 0);
        moodDescriptor.put(OK, 0);
        moodDescriptor.put(SAD, 0);
        moodDescriptor.put(ANGRY, 0);
        moodDescriptor.put(STRESSED, 0);
    }

    public void setMoodRating(int moodRating){
        this.moodRating = moodRating;
    }

    public int getMoodRating(){
        return moodRating;
    }

    public boolean updateMoodDescriptor(String moodFactor, Integer value){
        if(!moodDescriptor.containsKey(moodFactor)) {
            moodDescriptor.put(moodFactor, value);
            return true;
        }
        return false;
    }

    public boolean removeMoodDescriptor(String moodFactor){
        if(moodDescriptor.containsKey(moodFactor)){
            moodDescriptor.remove(moodFactor);
            return true;
        }
        return false;
    }

    public Integer getMoodDescriptorValue(String moodFactor){
        if(moodDescriptor.containsKey(moodFactor)){
            return moodDescriptor.get(moodFactor);
        }
        return null;
    }

    public Map<String, Integer> getMoodDescriptor() {
        return moodDescriptor;
    }
}

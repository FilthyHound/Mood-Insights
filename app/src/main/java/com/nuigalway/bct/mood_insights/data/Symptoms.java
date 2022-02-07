package com.nuigalway.bct.mood_insights.data;

import java.util.HashMap;
import java.util.Map;

public class Symptoms {

    public static String HEADACHE = "headache";
    public static String COLD = "cold";
    public static String ANXIETY = "anxiety";

    // May separate symptoms based on type of issue (mental/physical/illness etc)
    private final Map<String, Integer> symptoms;

    public Symptoms(){
        symptoms = new HashMap<>();
        symptoms.put(HEADACHE, 0);
        symptoms.put(COLD, 0);
        symptoms.put(ANXIETY, 0);
    }

    public boolean addSymptom(String symptomFactor, Integer value){
        if(symptoms.containsKey(symptomFactor)) {
            symptoms.put(symptomFactor, value);
            return true;
        }
        return false;
    }

    public boolean removeSymptom(String symptomFactor){
        if(symptoms.containsKey(symptomFactor)) {
            symptoms.remove(symptomFactor);
            return true;
        }
        return false;
    }

    public Map<String, Integer> getSymptoms() {
        return symptoms;
    }
}

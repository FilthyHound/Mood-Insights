package com.nuigalway.bct.mood_insights.util;;

public class DatabaseManager {
    private static DatabaseManager instance;

    private DatabaseManager(){

    }

    public DatabaseManager getDatabaseManager(){
        if(instance == null){
            instance = new DatabaseManager();
        }
        return instance;
    }

}

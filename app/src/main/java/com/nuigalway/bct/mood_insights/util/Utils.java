package com.nuigalway.bct.mood_insights.util;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utils class accesses hidden file for Tests and Connecting to the database
 *
 * @author Karl Gordon
 */
public class Utils {
    // Public static final fields, Keys for Intent extras
    public static final String USER_KEY = "user";
    public static final String OLD_DATE_FROM_CALENDAR = "old_date_from_calendar";
    public static final String HYPHEN = "-";

    /**
     * Getter method gets the value from the secret file, based on the key and context
     *
     * @param key - String, key to get the value of from the config.properties file
     * @param context - Context, needed to access the secret file
     * @return The String value linked to the String key
     * @throws IOException - Thrown when the secret file couldn't be found
     */
    public static String getProperty(String key, Context context) throws IOException {
        Properties properties = new Properties();
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open("config.properties");
        properties.load(inputStream);
        return properties.getProperty(key);
    }
}

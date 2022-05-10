package com.nuigalway.bct.mood_insights;


import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.swipeDown;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.allOf;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import androidx.test.rule.ActivityTestRule;


import com.nuigalway.bct.mood_insights.util.Utils;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;

@RunWith(JUnit4.class)
public class MainActivityUITest {

    @Rule
    public ActivityTestRule<MainActivity> mainActivityIntentsTestRule = new ActivityTestRule<>(MainActivity.class);


    @Test
    public void testMainActivity_LoadedCorrectly(){
        checkViewsOfMainActivity();
    }

    @Test
    public void testMainActivity_SuccessfulLogin() throws InterruptedException {
        try {
            onView(withId(R.id.email)).perform(typeText(Utils.getProperty("test.email", getApplicationContext())));
            onView(withId(R.id.password)).perform(typeText(Utils.getProperty("test.password", getApplicationContext())));
        }catch (IOException e){
            e.printStackTrace();
        }
        onView(withId(R.id.signIn)).perform(click());
        Thread.sleep(2000L);
        // Check if next page has loaded
        onView(withId(R.id.welcome));
        pressBack();
        checkViewsOfMainActivity();
    }

    @Test
    public void testMainActivity_TraverseToRegisterPageAndBack() {
        onView(withId(R.id.register)).perform(click());
        onView(withId(R.id.title)).perform(click());
        checkViewsOfMainActivity();
    }


    private void checkViewsOfMainActivity() {
        onView(allOf(withId(R.id.title), withText("Mood Insights")));
        onView(allOf(withId(R.id.titleDescription), withText("Track your moods")));
        onView(allOf(withId(R.id.email), withHint("Email Address")));
        onView(allOf(withId(R.id.password), withHint("Password")));
        onView(allOf(withId(R.id.signIn), withText("Login")));
        onView(allOf(withId(R.id.forgotPassword), withText("Forgot Password?")));
        onView(allOf(withId(R.id.register), withText("Register")));
    }
}

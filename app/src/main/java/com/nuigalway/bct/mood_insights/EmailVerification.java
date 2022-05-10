package com.nuigalway.bct.mood_insights;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;

/**
 * EmailVerification activity class is a page that suggests to the user to verify their email before
 * progressing to the login page
 *
 * @author Karl Gordon
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class EmailVerification extends AppCompatActivity {

    /**
     * onCreate method is the first called upon the instantiation of the activity class,
     * displays information to the user that an email verification has been sent,
     * and that they should verify their email.
     *
     * @param savedInstanceState - Bundle, contains the data it most recently supplied in
     *                             onSaveInstanceState.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        // Get Button and set the on click functionality
        Button continueButton = findViewById(R.id.continueButton);
        continueButton.setOnClickListener(v ->
                startActivity(new Intent(EmailVerification.this, MainActivity.class))
        );
    }
}
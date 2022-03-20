package com.nuigalway.bct.mood_insights;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;

@RequiresApi(api = Build.VERSION_CODES.O)
public class EmailVerification extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        Button continueButton = findViewById(R.id.continueButton);
        continueButton.setOnClickListener(v -> startActivity(new Intent(EmailVerification.this, MainActivity.class)));
    }
}
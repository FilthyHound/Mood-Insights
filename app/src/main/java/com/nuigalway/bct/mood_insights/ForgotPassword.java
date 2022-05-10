package com.nuigalway.bct.mood_insights;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.nuigalway.bct.mood_insights.validation.Validator;

/**
 * ForgotPassword class extends AppCompatActivity handles the request to send an email to a User
 * that has forgotten their password and requests to reset its password
 *
 * @author Karl Gordon
 */
public class ForgotPassword extends AppCompatActivity {
    // Private fields
    private EditText emailEditText;
    private ProgressBar progressBar;
    private FirebaseAuth auth;

    /**
     * onCreate method is the first called upon the instantiation of the activity class,
     * Creates a page for the user to enter an email, which will be emailed with the link to
     * reset its accounts password
     *
     * @param savedInstanceState- Bundle, contains the data it most recently supplied in
     *                            onSaveInstanceState.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Define UI elements
        emailEditText = findViewById(R.id.email);
        Button resetPasswordButton = findViewById(R.id.resetPassword);
        progressBar = findViewById(R.id.progressBar);

        // Get Firebase Authentication Instance
        auth = FirebaseAuth.getInstance();

        // Lambda on clicker for reset password button click
        resetPasswordButton.setOnClickListener(v -> resetPassword());
    }

    /**
     * Method handles the resetting of the password, by verifying the email, and sending a password
     * reset email request to firebase.
     *
     * If successful, redirect to the MainActivity.java page
     */
    private void resetPassword(){
        Validator validator = new Validator();
        String email = emailEditText.getText().toString().trim();

        if(!validator.isEmailValid(email, emailEditText)){
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Toast.makeText(ForgotPassword.this, "Check you email to reset your password!", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);

                //redirect to login activity
                startActivity(new Intent(ForgotPassword.this, MainActivity.class));
            }else{
                Toast.makeText(ForgotPassword.this, "Try Again! Something went wrong!", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
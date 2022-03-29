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
import com.google.firebase.auth.FirebaseUser;
import com.nuigalway.bct.mood_insights.validation.Validator;

public class ForgotPassword extends AppCompatActivity {

    private EditText emailEditText;
    private ProgressBar progressBar;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailEditText = findViewById(R.id.email);
        Button resetPasswordButton = findViewById(R.id.resetPassword);
        progressBar = findViewById(R.id.progressBar);

        auth = FirebaseAuth.getInstance();

        resetPasswordButton.setOnClickListener(v -> resetPassword());
    }

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
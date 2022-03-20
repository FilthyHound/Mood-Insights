package com.nuigalway.bct.mood_insights;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.nuigalway.bct.mood_insights.user.User;
import com.nuigalway.bct.mood_insights.util.Utils;
import com.nuigalway.bct.mood_insights.validation.Validator;

import java.io.IOException;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.O)
public class RegisterUser extends AppCompatActivity implements View.OnClickListener{

    private EditText editTextFullName, editTextAge, editTextEmail, editTextPassword;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        mAuth = FirebaseAuth.getInstance();

        TextView title = findViewById(R.id.title);
        title.setOnClickListener(this);

        TextView registerUser = findViewById(R.id.registerUser);
        registerUser.setOnClickListener(this);

        editTextFullName = findViewById(R.id.fullName);
        editTextAge = findViewById(R.id.age);
        editTextEmail =  findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);

        progressBar = findViewById(R.id.progressBar);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.title){
            startActivity(new Intent(this, MainActivity.class));
        }else if(v.getId() == R.id.registerUser){
            registerUser();
        }
    }

    private void registerUser(){
        Validator v = new Validator();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String fullName = editTextFullName.getText().toString().trim();
        String age = editTextAge.getText().toString().trim();

        if(!v.genericStringValidation(fullName, editTextFullName)
                || !v.ageStringValidation(age, editTextAge)
                || !v.isEmailValid(email, editTextEmail)
                || !v.isPasswordValid(password, editTextPassword)){
            Toast.makeText(RegisterUser.this, "Validation error, try again", Toast.LENGTH_LONG).show();
            return;
        }


        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(taskCreateUser -> {
                    if(taskCreateUser.isSuccessful()){
                        sendEmailVerification();
                        progressBar.setVisibility(View.GONE);

                        //redirect to login activity
                        handleSuccessfulUserCreation(fullName, age, email);
                    }else{
                        Toast.makeText(RegisterUser.this, "Failed to Register! First Task!", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void handleSuccessfulUserCreation(String fullName, String age, String email){
        User user = new User(fullName, age, email);

        try {
            FirebaseDatabase.getInstance(Utils.getProperty("app.database", getApplicationContext())).getReference("Users")
                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                    .setValue(user).addOnCompleteListener(taskDatabaseComms -> {
                if (taskDatabaseComms.isSuccessful()) {
                    Toast.makeText(RegisterUser.this, "User has been registered successfully!", Toast.LENGTH_LONG).show();
                    //TODO send email verification
                    progressBar.setVisibility(View.GONE);

                    //redirect to Email Verification prompt activity
                    startActivity(new Intent(RegisterUser.this, EmailVerification.class));
                } else {
                    Toast.makeText(RegisterUser.this, "Failed to Register! Second Task!", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            });
        }catch(IOException e){
            e.printStackTrace();
            //TODO make these logs
            Toast.makeText(RegisterUser.this, "Error, couldn't connect to database", Toast.LENGTH_LONG).show();
        }catch(NullPointerException e){
            e.printStackTrace();
            //TODO make these logs
            Toast.makeText(RegisterUser.this, "Error, couldn't get account", Toast.LENGTH_LONG).show();
        }
    }

    private void sendEmailVerification(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            user.sendEmailVerification();
        }
    }
}
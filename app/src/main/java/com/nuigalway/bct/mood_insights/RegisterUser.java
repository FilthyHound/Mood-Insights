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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nuigalway.bct.mood_insights.user.User;
import com.nuigalway.bct.mood_insights.util.Utils;
import com.nuigalway.bct.mood_insights.validation.Validator;

import java.io.IOException;
import java.util.Objects;

/**
 * RegisterUser activity class registers a new user to the Firebase Authentication, and the Firebase
 * database
 *
 * @author Karl Gordon
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class RegisterUser extends AppCompatActivity implements View.OnClickListener{
    //Private static final fields
    private static final String OK_MESSAGE = "User has been registered successfully!";
    private static final String ERROR_MESSAGE = "Couldn't complete registration!";

    // Private fields
    private EditText editTextFullName, editTextAge, editTextEmail, editTextPassword;
    private ProgressBar progressBar;
    private FirebaseAuth auth;

    /**
     * onCreate method is the first called upon the instantiation of the activity class,
     * Registers a user, creates a User object linked to that new instance and adds it to
     * the database
     *
     * @param savedInstanceState- Bundle, contains the data it most recently supplied in
     *                            onSaveInstanceState.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        auth = FirebaseAuth.getInstance();

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

    /**
     * OnClick method either redirects back to MainActivity or registers a user based on filled in
     * information in the EditTexts
     *
     * @param v View, the Button / Views clicked
     */
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.title){
            startActivity(new Intent(this, MainActivity.class));
        }else if(v.getId() == R.id.registerUser){
            registerUser();
        }
    }

    /**
     * Method registers the user once all validation checks have passed
     */
    private void registerUser(){
        Validator v = new Validator();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String fullName = editTextFullName.getText().toString().trim();
        String age = editTextAge.getText().toString().trim();

        if(!v.genericStringValidation(fullName, editTextFullName)
                || !v.ageStringValidation(age, editTextAge)
                || !v.isEmailValid(email, editTextEmail)
                || v.isPasswordInvalid(password, editTextPassword)){
            Toast.makeText(RegisterUser.this, "Validation error, try again", Toast.LENGTH_LONG).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(
                taskCreateUser -> {
                    if(taskCreateUser.isSuccessful()){
                        sendEmailVerification();
                        progressBar.setVisibility(View.GONE);

                        //redirect to login activity
                        handleSuccessfulUserCreation(fullName, age, email);
                    }else{
                        Toast.makeText(RegisterUser.this, ERROR_MESSAGE, Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }


    /**
     * Method sends an email verification request to the email attached to the new user
     */
    private void sendEmailVerification(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            user.sendEmailVerification();
        }
    }

    /**
     * Method creates a first instance of a User object, and passes it in into the database
     *
     * @param fullName - String, the name of the user
     * @param age - String, the age of the user
     * @param email - String, the email of the user
     */
    private void handleSuccessfulUserCreation(String fullName, String age, String email){
        User user = new User(fullName, age, email);
        String userId;
        DatabaseReference reference;

        try {
            // Get Database reference of Users and the current Users Id
            reference = FirebaseDatabase.getInstance(Utils.getProperty("app.database",
                    getApplicationContext())).getReference("Users");
            userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

            // Attempt to set the User object to the database linked to the User ID
            reference.child(userId).setValue(user).addOnCompleteListener(task -> {
                if (task.isSuccessful()) { // Redirect to the EmailVerification activity class
                    Toast.makeText(RegisterUser.this, OK_MESSAGE, Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                    //redirect to Email Verification prompt activity
                    startActivity(new Intent(RegisterUser.this, EmailVerification.class));
                } else {
                    Toast.makeText(RegisterUser.this, ERROR_MESSAGE, Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            });
        }catch(IOException | NullPointerException e){
            e.printStackTrace();
            Toast.makeText(RegisterUser.this, ERROR_MESSAGE, Toast.LENGTH_LONG).show();
        }
    }
}
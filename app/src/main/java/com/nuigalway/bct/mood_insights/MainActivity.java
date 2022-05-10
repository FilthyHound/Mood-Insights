package com.nuigalway.bct.mood_insights;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nuigalway.bct.mood_insights.validation.Validator;

/**
 * MainActivity activity class acts as the login page for the application, implements a View
 * on click listener to allow for page redirection
 *
 * @author Karl Gordon
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    // Private fields
    private EditText editTextEmail, editTextPassword;
    private FirebaseAuth auth;
    private ProgressBar progressBar;

    /**
     * onCreate method is the first called upon the instantiation of the activity class,
     * Page allows the user to either login, to register an account, or to reset a password if it
     * is forgotten
     *
     * @param savedInstanceState- Bundle, contains the data it most recently supplied in
     *                            onSaveInstanceState.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        TextView register = findViewById(R.id.register);
        register.setOnClickListener(this);

        TextView forgotPassword = findViewById(R.id.forgotPassword);
        forgotPassword.setOnClickListener(this);

        Button signIn = findViewById(R.id.signIn);
        signIn.setOnClickListener(this);

        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);

        progressBar = findViewById(R.id.progressBar);
    }

    /**
     * onClick method, overriding the View.OnClickListener method onClick, where the pare is
     * redirected based on which button is clicked. If login is selected, will begin to redirect to
     * the FactorPage
     *
     * @param v -  View, button clicked
     */
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.register){
            startActivity(new Intent(this, RegisterUser.class));
        }else if(v.getId() == R.id.signIn){
            userLogin();
        }else if(v.getId() == R.id.forgotPassword){
            startActivity(new Intent(this, ForgotPassword.class));
        }
    }

    /**
     * Method handling the logging in of the user into the application
     */
    private void userLogin(){
        Validator v = new Validator();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validate the input for the email and password
        if (!v.isEmailValid(email, editTextEmail)
                || v.isPasswordInvalid(password, editTextPassword)) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Attempt to sign into Firebase Authentication with the given email and password
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                emailVerificationHandler();
            } else {
                Toast.makeText(MainActivity.this, "Failed to login! Please check your credentials", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Method verifies that the user's email has been verified or not. Will not progress unless the
     * email has been verified
     */
    private void emailVerificationHandler(){
        // Get the instance of the singed in user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null) {
            // If the user is verified, indicate a successful sign in and redirect to the FactorPage
            if(user.isEmailVerified()){
                Toast.makeText(MainActivity.this, "User has signed in successfully!", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
                //redirect to FactorPage
                startActivity(new Intent(MainActivity.this, FactorPage.class));
            }else{ // If unverified, resent a verification email
                user.sendEmailVerification();
                Toast.makeText(MainActivity.this, "Check your email to verify your account!", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        }else{
            Toast.makeText(MainActivity.this, "No account, please register an account first!", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
        }
    }
}
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


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText editTextEmail, editTextPassword;

    private FirebaseAuth mAuth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView register = findViewById(R.id.register);
        register.setOnClickListener(this);

        TextView forgotPassword = findViewById(R.id.forgotPassword);
        forgotPassword.setOnClickListener(this);

        Button signIn = findViewById(R.id.signIn);
        signIn.setOnClickListener(this);

        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);

        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
    }

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

    private void userLogin(){
        Validator v = new Validator();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (!v.isEmailValid(email, editTextEmail)
                || !v.isPasswordValid(password, editTextPassword)) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                emailVerificationHandler();
            } else {
                Toast.makeText(MainActivity.this, "Failed to login! Please check your credentials", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void emailVerificationHandler(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            if (user.isEmailVerified()) {
                Toast.makeText(MainActivity.this, "User has signed in successfully!", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
                //redirect to factors page
                startActivity(new Intent(MainActivity.this, FactorPage.class));
            } else {
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
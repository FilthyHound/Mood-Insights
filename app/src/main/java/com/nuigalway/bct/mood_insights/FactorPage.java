package com.nuigalway.bct.mood_insights;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nuigalway.bct.mood_insights.data.Day;
import com.nuigalway.bct.mood_insights.user.User;
import com.nuigalway.bct.mood_insights.util.Utils;

import java.io.IOException;

@RequiresApi(api = Build.VERSION_CODES.O)
public class FactorPage extends AppCompatActivity {
    private boolean updateUser = false;
    private FirebaseUser user;
    private DatabaseReference reference;

    private String userID;
    private User userProfile;

    private Button logOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_factor_page);

        updateUser = false;

        initBottomNav();
        loadUserDetails();
    }

    private void initBottomNav(){
        BottomNavigationView bottomNavigationView = findViewById(R.id.navView);
        bottomNavigationView.setSelectedItemId(R.id.factorHome);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.calendar:
                    startActivity(new Intent(getApplicationContext(), CalendarPage.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.factorHome:
                    return true;
                case R.id.graph:
                    startActivity(new Intent(getApplicationContext(), GraphPage.class));
                    overridePendingTransition(0, 0);
                    return true;
            }
            return false;
        });
    }

    private void loadUserDetails(){
        logOut = findViewById(R.id.signOut);
        logOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(FactorPage.this, MainActivity.class));
        });

        try {
            user = FirebaseAuth.getInstance().getCurrentUser();
            reference = FirebaseDatabase.getInstance(Utils.getProperty("app.database", getApplicationContext())).getReference("Users");
        }catch (IOException e){
            e.printStackTrace();
        }
        userID = user.getUid();

        reference.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userProfile = snapshot.getValue((User.class));

                if(userProfile != null){
                    populateUserData();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FactorPage.this, "Something went wrong!", Toast.LENGTH_LONG).show();
            }
        });

        if(updateUser) {
            reference.child(userID).setValue(userProfile).addOnCompleteListener(taskDatabaseComms -> {
                if (taskDatabaseComms.isSuccessful()) {
                    Toast.makeText(FactorPage.this, "User has been updated!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(FactorPage.this, "Error, user couldn't update", Toast.LENGTH_LONG).show();
                }
            });
            updateUser = false;
        }
    }

    private void populateUserData(){
        final TextView welcomeTextView = findViewById(R.id.welcome);
        final TextView fullNameTextView = findViewById(R.id.fullName);
        final TextView emailTextView = findViewById(R.id.emailAddress);
        final TextView ageTextView = findViewById(R.id.age);
        final TextView dayTextView = findViewById(R.id.day);

        String fullName = userProfile.fullName;
        String email = userProfile.email;
        String age = userProfile.age;
        Day d = userProfile.getCurrentDay();
        Day toCheck = new Day();

        if(!d.equals(toCheck)){
            userProfile.createNewDay();
            updateUser = true;
//            reference.child(userID).setValue(userProfile).addOnCompleteListener(taskDatabaseComms -> {
//                if (taskDatabaseComms.isSuccessful()) {
//                    Toast.makeText(FactorPage.this, "User has been updated!", Toast.LENGTH_LONG).show();
//                } else {
//                    Toast.makeText(FactorPage.this, "Error, user couldn't update", Toast.LENGTH_LONG).show();
//                }
//            });
        }

        String welcomeText = "Welcome, " + fullName + "!";
        welcomeTextView.setText(welcomeText);
        fullNameTextView.setText(fullName);
        emailTextView.setText(email);
        ageTextView.setText(age);
        dayTextView.setText(userProfile.getDate());
    }

//    private void updateUser(){
//        reference.child(userID).setValue(userProfile).addOnCompleteListener(taskDatabaseComms -> {
//            if (taskDatabaseComms.isSuccessful()) {
//                Toast.makeText(FactorPage.this, "User has been updated!", Toast.LENGTH_LONG).show();
//            } else {
//                Toast.makeText(FactorPage.this, "Error, user couldn't update", Toast.LENGTH_LONG).show();
//            }
//        });
//    }
}
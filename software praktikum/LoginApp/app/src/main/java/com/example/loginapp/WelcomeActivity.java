package com.example.loginapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


/**
 *@author Sadio
 * Welcome Class for the welcome Activity
 */
public class WelcomeActivity extends AppCompatActivity {
    private ImageButton btnLogout;
    private Button changePassword;
    private EditText edWelcomePassword;
    private String newPassword_from_ed;
    private TextView welcomeUser;
    private final String CREDENTIAL_SHARED_PREF = "log";


    //Back Button disabled
    public void onBackPressed() {
    //    super.onBackPressed();
    }

    /**
     * onCreate method shows the View of the welcome Activity and has the methods for changing Password and to Logout
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);


        btnLogout = findViewById(R.id.btnLogout);
        changePassword = findViewById(R.id.changePassword);
        edWelcomePassword = findViewById(R.id.edWelcomePassword);
        welcomeUser = findViewById(R.id.welcomeUser);


        //user welcome text
        SharedPreferences data = getSharedPreferences(CREDENTIAL_SHARED_PREF, Context.MODE_PRIVATE);
        welcomeUser.setText("Welcome back, " + data.getString("Username", ""));


        /**
         * logoutButton.setOnClickListener is responsible for the actions when we click the logoutButton
         * contains the class OnClickListener in which we found the method to logout

         */
        btnLogout.setOnClickListener(new View.OnClickListener() {

            /**
             * send the user back to the Login page back and shows "logout successful" when it's done
             * @param v object of the View class
             */

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);

                Toast.makeText(WelcomeActivity.this, "Logout Successful", Toast.LENGTH_SHORT).show();

                startActivity(intent);
            }
        });

        /**
         * changeButton.setOnClickListener is responsible for the actions when we click the changeButton
         *  * contains the class OnClickListener in which we found the method to change the Password
         */

        changePassword.setOnClickListener(new View.OnClickListener() {

            /**
             * checks first if a Password was given if not show "Enter Password"
             * else changes the password using Sharedpreferences
             * @param v object of the View class
             */

            @Override
            public void onClick(View v) {
                newPassword_from_ed = edWelcomePassword.getText().toString(); // new given Password

                if (newPassword_from_ed.isEmpty()) { // checking if a password was given
                    edWelcomePassword.setError("Enter Password");
                    edWelcomePassword.requestFocus();
                } else {
                    // show "password changed", when the task is done
                    Toast.makeText(WelcomeActivity.this, "Password successfully changed", Toast.LENGTH_SHORT).show();
                    isValid(newPassword_from_ed);



                }
            }
        });


    }

    private void isValid(String password) {
        SharedPreferences data = getSharedPreferences(CREDENTIAL_SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = data.edit();
        editor.remove("Password");// remove the last Password
        editor.putString("Password", password);// put the new Password into the database
        editor.apply(); // apply the change

    }

    }
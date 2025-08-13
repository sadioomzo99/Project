package com.example.loginapp;



import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
    /**
     * @author  Abdulmallek Ali
     * @version  1.0
     * Sing up screen where the user has to write the desired username and password
     * if the user didn't write password or username  in one of the fields or both fields username and password
     * the user can't go the login page until the both fields are filled
     */
public class SignUpActivity extends AppCompatActivity {

    private EditText edSingUpName;
    private EditText edSingUpPassword;
    private Button btnSingUp;
    private String singUpName;
    private String singUpPassword;
    private final String CREDENTIAL_SHARED_PREF = "log";

    /**
     * onCreate method shows the View of the Sign up Activity and has the methods for registering User input and saving it in the database
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        edSingUpName = findViewById(R.id.edSingUpName);
        edSingUpPassword = findViewById(R.id.edSingUpPassword);

        btnSingUp = findViewById(R.id.btnSingUp);

        /**
         * btnSingUp.setOnClickListener is responsible for the actions when we click the Sign up Button
         * contains the class OnClickListener in which we found the method to logout
         */
        btnSingUp.setOnClickListener(new View.OnClickListener() {


            /**
             * checking if the input field is not empty else calling @method isValid
             * @param v object of the View class
             */
            @Override
            public void onClick(View v) {


                singUpName = edSingUpName.getText().toString();
                singUpPassword = edSingUpPassword.getText().toString();

                if(singUpPassword.isEmpty()){ // checking if a password was given
                    edSingUpPassword.setError("Enter Password");
                    edSingUpPassword.requestFocus();
                }else {
                    isValid(singUpName, singUpPassword);//calling the method isValid
                }
                if(singUpName.isEmpty()){
                    edSingUpName.setError("Enter Name");// checking if a username was given
                    edSingUpName.requestFocus();
                }else isValid(singUpName, singUpPassword);//calling the method isValid



            }
        });


    }

        /**
         *checking if the user inputted  a username and password in both fields if they are send the user to the login page
         * @param username  a string of the written username
         * @param userPassword a string  of the written password
         */
    public void isValid(String username, String userPassword) {

        if(!username.equals("") && !userPassword.equals("")){
            SharedPreferences data = getSharedPreferences(CREDENTIAL_SHARED_PREF, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = data.edit();
            editor.putString("Username", singUpName);//add the username to the database
            editor.putString("Password", singUpPassword); //add the password to the database
            editor.apply();
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);//go to the login page
            startActivity(intent);


        }
    }
}
package com.example.loginapp;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import android.widget.Toolbar;



/**
 * @author Tahir Begec
 *@Das ist die LoginActivity.java
 * Die Login-Page wird die Datei von Sing_Up Page abgerufen und in der Login-Page wird es die Parameter
 * für das Login definiert. Dann nächste Schritt ist die Abfrage, wenn die Datei richtig eingegeben wird
 * dann wird es auf die WelcomePage gehen können, wenn die Datei falsch eingegeben wird
 * dann wird es auf die LoginRefused gehen. Nächste Schritt ist letzte Abfrage
 * wenn der Benutzer oder Nutzer kein Passwort eingegeben wird, dann wird es nicht weiter gehen
 * bekommt der Nutzer oder Benutzer einen Hinweis über Login Page(Enter Name or Password).
 */

public class LoginActivity extends AppCompatActivity {


    private EditText edUsername;
    private EditText edPassword;
    private Button btnLogin;

    private String strUsername;
    private String username_form_ed;
    private String strPassword;
    private String password_from_ed;

    private final String CREDENTIAL_SHARED_PREF = "log";


    //Back Button disabled
    public void onBackPressed() {
    //    super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        edUsername = findViewById(R.id.edUsername);
        edPassword = findViewById(R.id.edPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences data = getSharedPreferences(CREDENTIAL_SHARED_PREF, Context.MODE_PRIVATE);
                strUsername = data.getString("Username", null);
                strPassword = data.getString("Password", null);
                username_form_ed = edUsername.getText().toString();
                password_from_ed = edPassword.getText().toString();

                CheckEmptyInput();
                isValid(username_form_ed, password_from_ed);



            }

        });
    }



    private void isValid(String username, String userPassword) {
        if(strUsername.isEmpty()||password_from_ed.isEmpty()){
            CheckEmptyInput();
        }else if (strUsername.equals(username) && strPassword.equals(userPassword)) {
            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);

        } else {
            Toast.makeText(this, "incorrect Name or Password", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginRefused.class);
            startActivity(intent);

        }
    }
    private void  CheckEmptyInput(){


        if (password_from_ed.isEmpty()) {
            edPassword.setError("Enter Password");
            edPassword.requestFocus();
        }
        if (username_form_ed.isEmpty()) {
            edUsername.setError("Enter Name");
            edUsername.requestFocus();
        }
    }
    }

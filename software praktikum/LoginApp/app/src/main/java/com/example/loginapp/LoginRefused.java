package com.example.loginapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

/**
 * LoginRefused Class for the LoginRefused
 */
public class LoginRefused extends AppCompatActivity {
    private Button btnTryAgain;
    private EditText enterCode;
    private String input;
    private String randomNumber;



    //Back Button disabled
    public void onBackPressed() {
    //    super.onBackPressed();
    }

    /**
     * a randomized four-digit number
     */

    private void NumberGen(){
        final Random rand = new Random();
        TextView randomCode = findViewById(R.id.randomCode);
        randomCode.setText(String.valueOf(rand.nextInt(5000)+1000));
        randomNumber = randomCode.getText().toString();
    }

    /**
     * onCreate method shows the View of the LoginRefused and has the methods to check if the user inputed the displayed randomized number correctly
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_refused);
        enterCode = findViewById(R.id.enterCode);
        btnTryAgain = findViewById(R.id.btnTryAgin);
        NumberGen();

        btnTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                input = enterCode.getText().toString();
                if (input.isEmpty()) {
                    enterCode.setError("Enter Number");
                    enterCode.requestFocus();
                }else isValid(input);

            }
        });


    }

    private void isValid(String userInput) {
        if (userInput.equals(randomNumber)) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "wrong Number please renter the new Numbers", Toast.LENGTH_SHORT).show();
            NumberGen();
        }
    }

}

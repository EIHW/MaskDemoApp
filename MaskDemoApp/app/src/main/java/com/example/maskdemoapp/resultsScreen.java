// Author: Adria Mallol-Ragolta, 2021
// Chair of Embedded Intelligence for Health Care and Wellbeing, University of Augsburg, Germany

package com.example.maskdemoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class resultsScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_results_screen);

        Intent intent = getIntent();

        int model_prediction = intent.getIntExtra("model_prediction", 0);

        TextView textView = (TextView) findViewById(R.id.resultText);

        if (model_prediction == 0){

            textView.setText("NEIN");
            textView.setTextColor(Color.parseColor("#B03A2E"));

        }
        else if (model_prediction == 1) {

            textView.setText("JA");
            textView.setTextColor(Color.parseColor("#1E8449"));

        }

    }

    public void toMain_click(android.view.View view){ finish(); }

    public void toEnd_click(android.view.View view){ finishAffinity(); }

}

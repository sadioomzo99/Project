package de.uni_marburg.sp21.Model;


import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import com.example.sp21.R;

import java.util.ArrayList;
import java.util.List;

import de.uni_marburg.sp21.Activity.CompaniesActivity;
import de.uni_marburg.sp21.Activity.detailsActivity;

public class OpeningHours {
    private Day  day;
    private TimeInterval timeIntervals;
    boolean open;

    public OpeningHours(Day day, TimeInterval timeIntervals) {
        this.day = day;
        this.timeIntervals = timeIntervals;
    }



    public String getDay() {
        String x="";
        switch (day){
            case monday:
               x= CompaniesActivity.getRes().getString(R.string.monday);
               break;
            case tuesday:
                x=CompaniesActivity.getRes().getString(R.string.tuesday);
                break;
            case wednesday:
                x=CompaniesActivity.getRes().getString(R.string.wednesday);
                break;
            case thursday:
                x=CompaniesActivity.getRes().getString(R.string.thursday);
                break;
            case friday:
                x=CompaniesActivity.getRes().getString(R.string.friday);
                break;
            case saturday:
                x=CompaniesActivity.getRes().getString(R.string.saturday);
                break;
            case sunday:
                x=CompaniesActivity.getRes().getString(R.string.sunday);
                break;
        }
        return x;
    }

    public TimeInterval getTimeIntervals() {
        return timeIntervals;
    }

    public boolean isOpen() {
        if (String.valueOf(timeIntervals.getTime()).contains("closed")) {
            open = false;
        } else {
            open = true;
        }
        return open;
    }




}



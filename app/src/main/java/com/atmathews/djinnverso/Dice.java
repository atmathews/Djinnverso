package com.atmathews.djinnverso;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ToggleButton;

import java.util.Random;

/**
 * Created by atmat on 2017-08-29.
 */

public class Dice extends AppCompatActivity {
    private int value;
    private int idval;
    private int min = 1;
    private int max = 8;
    MainActivity mActivity;

    private boolean isSelected = false;

    public Dice(MainActivity activity, int idNum) {
        value = 1;
        idval = idNum;
        mActivity = activity;
    }

    public int getValue() {
        return value;
    }

    public int getId() {
        return idval;
    }

    public int getMin() {
        return min;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public int getMax() {
        return max;
    }

    public void setMin(int newMin) {
        min = newMin;
    }

    public void setMax(int newMax) {
        max = newMax;
    }

    public void setValue(int num) {
        value = num;
    }

    public void setId(int num) {
        idval = num;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public void updateDisplay() {
        ToggleButton button = (ToggleButton) mActivity.findViewById(idval);
        button.setTextOff("" + value);
        button.setTextOn("" + value);
        button.setText(""+value);
    }

    public void roll() {

        ToggleButton button = (ToggleButton) mActivity.findViewById(idval);
        if (button.isChecked()) {
            Random rand = new Random();
            value = min + rand.nextInt(max);
            updateDisplay();
        }

    }

}

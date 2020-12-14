package com.here.android.tutorial;

public enum DaysOfTheWeek {


    MONDAY(0),
    TUESDAY(1),
    WEDNESDAY(2),
    THURSDAY(3),
    FRIDAY(4),
    SATURDAY(5),
    SUNDAY(6);

    int myvalue;

    private DaysOfTheWeek(int value){
        myvalue = value;
    }
}

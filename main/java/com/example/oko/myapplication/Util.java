package com.example.oko.myapplication;

import android.content.Context;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Die Klasse Benachrichtigung hat nur einen einzigen Zweck zu erfüllen - Toast Ausgabe in der entsprechnenden Activity
 */
public class Util {
    private Util(){}

    public static void showToastLong(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void showToastLong(Context context, int res) {
        Toast.makeText(context, context.getString(res), Toast.LENGTH_LONG).show();
    }

    public static void showToastShort(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static String getFormattedDate(){
        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy_M", Locale.GERMANY);
        return dataFormat.format(new Date());
    }

    public static Date parseDate(String datum) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("d.M.yyyy",Locale.GERMANY);
        try {
            return dateFormat.parse(datum);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String extractWeekDay(Date date){
        SimpleDateFormat wochentagAbkuerzung = new SimpleDateFormat("E",Locale.GERMANY);
        return wochentagAbkuerzung.format(date.getTime());
    }
}

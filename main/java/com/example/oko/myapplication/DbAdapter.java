package com.example.oko.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;


import androidx.annotation.RequiresApi;

/**
 * DbAdapter ist die Schnitstelle Klasse zur SQLite.
 * Mit den Methoden dieser Klasse können Daten in die DB geschrieben, daraus gelesen und upgedated werden.
 *
 * @author Oleg Kossjak
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class DbAdapter extends SQLiteOpenHelper {
    private static final String TAG = "DbAdapter";

    private static final String TABLE_NAME = "abrechnung_" + Util.getFormattedDate();// Vorgabe für die TABLE_NAME => Jahr_Monat
    private static final String DATUM = "Datum";     // erste Spalte (Primary Key)
    private static final String WOCHENTAG = "Wochentag";
    private static final String ORTSANGABE = "Ortsangabe";
    private static final String ARBEITSBEGINN = "Arbeitsbeginn";
    private static final String ARBEITSBEGINN_KM = "ArbeitsbeginnTachostand";
    private static final String FUHR_LOS_UM = "fuhrLosUm";
    private static final String ARBEITSENDE = "ArbeitsendeBeiAnkunftZuhause";
    private static final String ARBEITSENDE_KM = "ArbeitsendeTachostand";
    private static final String ARBEIT_ZUHAUSE_BEENDET_UM = "ArbeitZuhauseBeendetUm";
    private static final String PAUSE_INNERHALB_GLEITZEIT = "PauseInnerhalbGleitzeit";
    private static final String PAUSE_AUSSERHALB_GLEITZEIT = "PauseAusserhalbGleitzeit";
    private static final String VARCHAR_TYPE = "VARCHAR(225)";

    public static final int ORTSANGABE_ARG = 0;
    public static final int ARBEITSBEGINN_ARG = 1;
    public static final int ARBEITSBEGINN_KM_ARG = 2;
    public static final int FUHR_LOS_UM_ARG = 3;
    public static final int ARBEITSENDE_BEI_ANKUNFT_ARG = 4;
    public static final int ARBEITSENDE_KM_ARG = 5;
    public static final int ARBEITSENDE_ARG = 6;
    public static final int WOCHENTAG_ARG = 7;
    public static final int PAUSE_INNERHALB_GLEITZEIT_ARG = 8;
    public static final int PAUSE_AUSSERHALB_GLEITZEIT_ARG = 9;

    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
            DATUM + " " + VARCHAR_TYPE + " PRIMARY KEY, " +
            WOCHENTAG + " " + VARCHAR_TYPE + " ," +
            ORTSANGABE + " " + VARCHAR_TYPE + " ," +
            ARBEITSBEGINN + " " + VARCHAR_TYPE + " ," +
            ARBEITSBEGINN_KM + " " + VARCHAR_TYPE + " ," +
            FUHR_LOS_UM + " " + VARCHAR_TYPE + " ," +
            ARBEITSENDE + " " + VARCHAR_TYPE + " ," +
            ARBEITSENDE_KM + " " + VARCHAR_TYPE + " ," +
            ARBEIT_ZUHAUSE_BEENDET_UM + " " + VARCHAR_TYPE + ", " +
            PAUSE_INNERHALB_GLEITZEIT + " " + VARCHAR_TYPE + ", " +
            PAUSE_AUSSERHALB_GLEITZEIT + " " + VARCHAR_TYPE + ");";

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "monatsuebersicht.db";
    private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    private Context context;
    private SQLiteDatabase db = getWritableDatabase();

    DbAdapter(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "onCreate: ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            Util.showToastLong(context, "OnUpgrade");
            db.execSQL(DROP_TABLE);
            onCreate(db);
        } catch (Exception e) {
            Util.showToastLong(context, "" + e);
        }
    }

    public long insertData(EventItem item) {
        try {
            db.execSQL(CREATE_TABLE);
        } catch (Exception e) {
            Util.showToastLong(context, "" + e);
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(DATUM, item.getDatum());
        contentValues.put(WOCHENTAG,  item.getWochentag());
        contentValues.put(ORTSANGABE, item.getOrt());
        contentValues.put(ARBEITSBEGINN, item.getArbeitsbeginn());
        contentValues.put(ARBEITSBEGINN_KM, item.getArbeitsbeginnKM());
        contentValues.put(FUHR_LOS_UM, item.getFuhrLos());
        contentValues.put(ARBEITSENDE, item.getArbeitsende());
        contentValues.put(ARBEITSENDE_KM, item.getArbeitsendeKM());
        contentValues.put(ARBEIT_ZUHAUSE_BEENDET_UM, item.getArbeitZuhauseBeendet());
        contentValues.put(PAUSE_INNERHALB_GLEITZEIT, item.getPauseInnerhalbGleitzeit());
        contentValues.put(PAUSE_AUSSERHALB_GLEITZEIT, item.getPauseAusserhalbGleitzeit());
        return db.insert(TABLE_NAME, null, contentValues);
    }

    public String getInformationFromDB(String date, String tableName, int i) {
        String[] columns = {DATUM, WOCHENTAG, ORTSANGABE, ARBEITSBEGINN, ARBEITSBEGINN_KM,
                FUHR_LOS_UM, ARBEITSENDE, ARBEITSENDE_KM, ARBEIT_ZUHAUSE_BEENDET_UM,
                PAUSE_INNERHALB_GLEITZEIT, PAUSE_AUSSERHALB_GLEITZEIT};
        String[] whereArgs = {date};
        Cursor cursor = db.query(tableName, columns, DATUM + " = ?", whereArgs, null, null, null, null);
        StringBuilder buffer = new StringBuilder();

        while (cursor.moveToNext()) {
            String wochentagAusDB = cursor.getString(cursor.getColumnIndex(WOCHENTAG));
            String ortsangabe = cursor.getString(cursor.getColumnIndex(ORTSANGABE));
            String arbeitsbeginn = cursor.getString(cursor.getColumnIndex(ARBEITSBEGINN));
            String arbeitsbeginnKM = cursor.getString(cursor.getColumnIndex(ARBEITSBEGINN_KM));
            String fuhrLosUm = cursor.getString(cursor.getColumnIndex(FUHR_LOS_UM));
            String arbeitsende = cursor.getString(cursor.getColumnIndex(ARBEITSENDE));
            String arbeitsendeKM = cursor.getString(cursor.getColumnIndex(ARBEITSENDE_KM));
            String arbeitsendeZuhause = cursor.getString(cursor.getColumnIndex(ARBEIT_ZUHAUSE_BEENDET_UM));
            String pauseInnerhalbGleitzeit = cursor.getString(cursor.getColumnIndex(PAUSE_INNERHALB_GLEITZEIT));
            String pauseAusserhalbGleitzeit = cursor.getString(cursor.getColumnIndex(PAUSE_AUSSERHALB_GLEITZEIT));
            switch (i) {
                case DbAdapter.ORTSANGABE_ARG:
                    buffer.append(ortsangabe);
                    break;
                case DbAdapter.ARBEITSBEGINN_ARG:
                    buffer.append(arbeitsbeginn);
                    break;
                case DbAdapter.ARBEITSBEGINN_KM_ARG:
                    buffer.append(arbeitsbeginnKM);
                    break;
                case DbAdapter.FUHR_LOS_UM_ARG:
                    buffer.append(fuhrLosUm);
                    break;
                case DbAdapter.ARBEITSENDE_BEI_ANKUNFT_ARG:
                    buffer.append(arbeitsende);
                    break;
                case DbAdapter.ARBEITSENDE_KM_ARG:
                    buffer.append(arbeitsendeKM);
                    break;
                case DbAdapter.ARBEITSENDE_ARG:
                    buffer.append(arbeitsendeZuhause);
                    break;
                case DbAdapter.WOCHENTAG_ARG:
                    buffer.append(wochentagAusDB);
                    break;
                case DbAdapter.PAUSE_INNERHALB_GLEITZEIT_ARG:
                    buffer.append(pauseInnerhalbGleitzeit);
                    break;
                case DbAdapter.PAUSE_AUSSERHALB_GLEITZEIT_ARG:
                    buffer.append(pauseAusserhalbGleitzeit);
                    break;
                default:
                    break;
            }
        }
        cursor.close();
        return buffer.toString();
    }

    public int updateFuhrLos(String datum, String fuhrlosKm, String uhrzeit) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(FUHR_LOS_UM, uhrzeit);
        contentValues.put(ARBEITSBEGINN_KM, fuhrlosKm);
        String[] whereArgs = {datum};
        return db.update(TABLE_NAME, contentValues, DATUM + " = ?", whereArgs);
    }


    public int updateKMbeimEnde(String datum, String editKmBeimEnde, String uhrzeit) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ARBEITSENDE, uhrzeit);
        contentValues.put(ARBEITSENDE_KM, editKmBeimEnde);
        contentValues.put(ARBEIT_ZUHAUSE_BEENDET_UM, uhrzeit);
        String[] whereArgs = {datum};
        return db.update(TABLE_NAME, contentValues, DATUM + " = ?", whereArgs);
    }

    public int updateBeimEndeZuhause(String datum, String uhrzeit) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ARBEIT_ZUHAUSE_BEENDET_UM, uhrzeit);
        String[] whereArgs = {datum};
        return db.update(TABLE_NAME, contentValues, DATUM + " = ?", whereArgs);
    }

    public int updateOrt(String datum, String ort) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ORTSANGABE, ort);
        String[] whereArgs = {datum};
        return db.update(TABLE_NAME, contentValues, DATUM + " = ?", whereArgs);
    }

    public void erstelleTabelle(String tabelleNameUpdate) {
        //wenn ein Monat in der Übersicht gewählt wurde, für welches noch keine Tabelle angelgt wurde, so wird die Tabelle mit CREATE_TABLE erstellt
        String query = "CREATE TABLE IF NOT EXISTS " + tabelleNameUpdate +
                " (" + DATUM + " " + VARCHAR_TYPE + " PRIMARY KEY, " +
                WOCHENTAG + " " + VARCHAR_TYPE + " ," +
                ORTSANGABE + " " + VARCHAR_TYPE + " ," +
                ARBEITSBEGINN + " " + VARCHAR_TYPE + " ," +
                ARBEITSBEGINN_KM + " " + VARCHAR_TYPE + " ," +
                FUHR_LOS_UM + " " + VARCHAR_TYPE + " ," +
                ARBEITSENDE + " " + VARCHAR_TYPE + " ," +
                ARBEITSENDE_KM + " " + VARCHAR_TYPE + ", " +
                ARBEIT_ZUHAUSE_BEENDET_UM + " " + VARCHAR_TYPE + ", " +
                PAUSE_INNERHALB_GLEITZEIT + " " + VARCHAR_TYPE + ", " +
                PAUSE_AUSSERHALB_GLEITZEIT + " " + VARCHAR_TYPE + ");";
        try {
            db.execSQL(query);
        } catch (Exception e) {
            Util.showToastLong(context, "" + e.getMessage());
        }
    }

    public int update(String tableName,EventItem item) {
        ContentValues contentValues = new ContentValues();

        //nächste drei Zeilen ertellen eine Tabelle - wenn ein Tag im Monat geupdatet wurde, welches in DB nicht existiert
        contentValues.put(DATUM, item.getDatum());
        contentValues.put(WOCHENTAG,  item.getWochentag());
        db.insert(tableName, null, contentValues);

        contentValues.put(ORTSANGABE, item.getOrt());
        contentValues.put(ARBEITSBEGINN, item.getArbeitsbeginn());
        contentValues.put(ARBEITSBEGINN_KM, item.getArbeitsbeginnKM());
        contentValues.put(FUHR_LOS_UM, item.getFuhrLos());
        contentValues.put(ARBEITSENDE, item.getArbeitsende());
        contentValues.put(ARBEITSENDE_KM, item.getArbeitsendeKM());
        contentValues.put(ARBEIT_ZUHAUSE_BEENDET_UM, item.getArbeitZuhauseBeendet());
        contentValues.put(PAUSE_INNERHALB_GLEITZEIT, item.getPauseInnerhalbGleitzeit());
        contentValues.put(PAUSE_AUSSERHALB_GLEITZEIT, item.getPauseAusserhalbGleitzeit());
        String[] whereArgs = {item.getDatum()};
        return db.update(tableName, contentValues, DATUM + " = ?", whereArgs);
    }
}
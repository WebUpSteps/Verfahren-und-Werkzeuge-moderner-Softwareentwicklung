package com.example.oko.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

/**
 * Webbasierte Automatisierung der Zeitabrechnung für Customer Engineers bei NCR
 * <p>
 * Kalender Klasse ermöglicht die Monatsansicht und Änderung der in SQLite gespeicherte Daten
 *
 * @author Oleg Kossjak
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class Kalender extends Activity implements View.OnClickListener {

    // for prompt Dialog

    private static final String ABRECHNUNG = "abrechnung_";
    private static final String TAG = Kalender.class.getName();
    //nächste zwei Zeilen erlauben die Übergabe des Wochentags an DB


    CalendarView calendar;

    // helper tur DB Kommunikation
    DbAdapter dbAdapter;

    EditText editOrtTageswert;
    EditText editArbeitsbeginnTageswert;
    EditText editFuhrlosKmTageswert;
    EditText editFuhrlosUmTageswert;
    EditText editArbeitsendeBeiAnkunftTageswert;
    EditText editArbeitsendeKmTageswert;
    EditText editArbeitsendeZuhauseTageswert;
    EditText editPauseInnerhalbGleitzeit;
    EditText editPauseAusserhalbGleitzeit;
    String dateiname;
    String datum;
    String tabelleName;
    String editArbeitsbeginnString;
    String editFuhrlosKmString;
    String editFuhrlosUmString;
    String editArbeitsendeBeiAnkunftString;
    String editArbeitsendeKmString;
    String editArbeitsendeZuhauseString;
    String editOrtString;
    String editPauseInnerhalbGleitzeitString;
    String editPauseAusserhalbGleitzeitString;
    String wochentagAusDB;
    String stringMonth;
    String stringYear;
    String stringDay;
    String mWochentag;
    CheckBox cbUrlaubstag;
    CheckBox cbFeiertag;
    CheckBox cbKrankheitstag;
    TextView tvNettostundenTag;

    // dient dem Blockieren des "gewählten Tag Anschauen" Buttons. Erst nach Auswahl eines Tages im Kalender wird calendarClickBlocker auf false gesetzt
    boolean calendarClickBlocker = true;

    //eine Instanz von der Klasse saveToFile. Und an dieser kann man dann die Methoden aufrufen zur Speicherung der Daten in einer csv Datei.
    SaveToFile saveInstanz = new SaveToFile();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calender);

        Toast.makeText(getApplicationContext(), "Bitte wählen Sie einen Tag aus.", Toast.LENGTH_LONG).show();

        dbAdapter = new DbAdapter(this);

        tvNettostundenTag = findViewById(R.id.viewNettostundenTag);

        Button bntAnschauen = findViewById(R.id.anschauen);
        bntAnschauen.setOnClickListener(this);

        calendar = findViewById(R.id.calendar);
        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {
                calendarClickBlocker = false;
                month = month + 1;
                stringYear = Integer.toString(year);
                stringDay = Integer.toString(dayOfMonth);
                dateiname = month + "-" + year;

                datum = dayOfMonth + "." + month + "." + year;
                tabelleName = ABRECHNUNG + year + "_" + month;
                stringMonth = Integer.toString(month);

                // try catch beim zugriff auf DB
                try {
                    urlaubstageBerechnung();
                } catch (Exception e) {
                    Log.e(TAG, "Urlaubstage Berechnung konnte nicht durchgeführt werden: ", e);
                }

                wochentagAusDB = dbAdapter.getInformationFromDB(datum, tabelleName, DbAdapter.WOCHENTAG_ARG);
                if (wochentagAusDB.equalsIgnoreCase("u")) {
                    tvNettostundenTag.setText("Eingetragener Urlaubstag");
                } else if (wochentagAusDB.equals("f")) {
                    tvNettostundenTag.setText("Eingetragener Feiertag");
                } else if (wochentagAusDB.contentEquals("k")) {
                    tvNettostundenTag.setText("Eingetragener Krankheitstag");
                } else {
                    tvNettostundenTag.setText("");
                }

                // try catch beim zugriff auf DB
                try {
                    nettostundenBerechnung();
                    spesenBerechnung();
                    kmBerechnung();
                } catch (Exception e) {
                    Log.e(TAG, "Fehler in der Berechnung: ", e);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            if (calendarClickBlocker) {
                Toast.makeText(getApplicationContext(), "Bitte zunächst einen Tag antippen.", Toast.LENGTH_LONG).show();
            } else {


                try {
                    saveInstanz.saveInFile(dbAdapter, stringYear, stringMonth);
                    Toast.makeText(Kalender.this, "Text file Saved !", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.showToastShort(this,e.getMessage());
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void openDialog() {
        // get prompts.xml view
        View tageswerteView = getLayoutInflater().inflate(R.layout.content_tageswete_ansicht, null);

        editOrtTageswert = tageswerteView.findViewById(R.id.editOrtTageswert);
        editArbeitsbeginnTageswert = tageswerteView.findViewById(R.id.editArbeitsbeginnTageswert);
        editFuhrlosKmTageswert = tageswerteView.findViewById(R.id.editFuhrlosKmTageswert);
        editFuhrlosUmTageswert = tageswerteView.findViewById(R.id.editFuhrlosUmTageswert);
        editArbeitsendeBeiAnkunftTageswert = tageswerteView.findViewById(R.id.editArbeitsendeBeiAnkunftTageswert);
        editArbeitsendeKmTageswert = tageswerteView.findViewById(R.id.editArbeitsendeKmTageswert);
        editArbeitsendeZuhauseTageswert = tageswerteView.findViewById(R.id.editArbeitsendeZuhauseTageswert);
        editPauseInnerhalbGleitzeit = tageswerteView.findViewById(R.id.editPauseInnerhalbGleitzeit);
        editPauseAusserhalbGleitzeit = tageswerteView.findViewById(R.id.editPauseAusserhalbGleitzeit);

        readingFromDB();

        cbUrlaubstag = tageswerteView.findViewById(R.id.Urlaubstag);
        cbFeiertag = tageswerteView.findViewById(R.id.Feiertag);
        cbKrankheitstag = tageswerteView.findViewById(R.id.Krankheitstag);

        wochentagAusDB = dbAdapter.getInformationFromDB(datum, tabelleName, DbAdapter.WOCHENTAG_ARG);
        if (wochentagAusDB.equalsIgnoreCase("u")) {
            setEnableFalse();
            cbUrlaubstag.setChecked(true);
        } else if (wochentagAusDB.equals("f")) {
            setEnableFalse();
            cbFeiertag.setChecked(true);
        } else if (wochentagAusDB.contentEquals("k")) {
            setEnableFalse();
            cbKrankheitstag.setChecked(true);
        } else {
            cbUrlaubstag.setChecked(false);
            cbFeiertag.setChecked(false);
            cbKrankheitstag.setChecked(false);
        }

        cbUrlaubstag.setOnClickListener(v -> {

            cbFeiertag.setChecked(false);
            cbKrankheitstag.setChecked(false);
            if (cbUrlaubstag.isChecked()) {
                Toast.makeText(getApplicationContext(), getString(R.string.achtung), Toast.LENGTH_LONG).show();
                setEnableFalse();
            } else {
                setEnableTrue();
            }

        });
        cbFeiertag.setOnClickListener(v -> {

            cbUrlaubstag.setChecked(false);
            cbKrankheitstag.setChecked(false);
            if (cbFeiertag.isChecked()) {
                Toast.makeText(getApplicationContext(), getString(R.string.achtung), Toast.LENGTH_LONG).show();
                setEnableFalse();
            } else {
                setEnableTrue();
            }

        });
        cbKrankheitstag.setOnClickListener(v -> {

            cbFeiertag.setChecked(false);
            cbUrlaubstag.setChecked(false);
            if (cbKrankheitstag.isChecked()) {
                Toast.makeText(getApplicationContext(), getString(R.string.achtung), Toast.LENGTH_LONG).show();
                setEnableFalse();
            } else {
                setEnableTrue();
            }

        });
        new AlertDialog.Builder(this)
                .setView(tageswerteView)
                .setCancelable(true)
                .setPositiveButton("Update", (dialog, id) -> onUpdateButtonClicked())
                .create()
                .show();
    }

    private void onUpdateButtonClicked() {
        // get user input and set it to result - edit text
        editOrtString = editOrtTageswert.getText().toString();
        editArbeitsbeginnString = editArbeitsbeginnTageswert.getText().toString();
        editFuhrlosKmString = editFuhrlosKmTageswert.getText().toString();
        editFuhrlosUmString = editFuhrlosUmTageswert.getText().toString();
        editArbeitsendeBeiAnkunftString = editArbeitsendeBeiAnkunftTageswert.getText().toString();
        editArbeitsendeKmString = editArbeitsendeKmTageswert.getText().toString();
        editArbeitsendeZuhauseString = editArbeitsendeZuhauseTageswert.getText().toString();
        editPauseInnerhalbGleitzeitString = editPauseInnerhalbGleitzeit.getText().toString();
        editPauseAusserhalbGleitzeitString = editPauseAusserhalbGleitzeit.getText().toString();

        // nächster try catch block ermglicht die Umwandlung von int Datum Werten in Date Format


        Date date = Util.parseDate(datum);
        if (date != null) {
            mWochentag = Util.extractWeekDay(date);
        }

        int updateResult = saveToDb();
        if (updateResult <= 0) {
            Util.showToastLong(getApplicationContext(), "Unsuccessful");
        } else {
            Util.showToastLong(getApplicationContext(), "Datenbanktabelle aktualisiert");
        }

        // try catch beim zugriff auf DB
        try {
            //nächste MEthoden werden ausgeführt, damit die unteren Werte (in der Monatsübersicht), nach Update auch aktualisiert werden
            urlaubstageBerechnung();
            nettostundenBerechnung();
            spesenBerechnung();
            kmBerechnung();
        } catch (Exception e) {
            Log.e(TAG, "Fehler in der Berechnung: ", e);
        }
    }

    private int saveToDb() {
        int result;
        if (cbUrlaubstag.isChecked()) {
            tvNettostundenTag.setText("Eingetragener Urlaubstag");
            EventItem item = new EventItem.Builder()
                    .setDatum(datum)
                    .setWochentag("u")
                    .create();
            result = dbAdapter.update(tabelleName, item);
        } else if (cbFeiertag.isChecked()) {
            tvNettostundenTag.setText("Eingetragener Feiertag");

            EventItem item = new EventItem.Builder()
                    .setDatum(datum)
                    .setWochentag("f")
                    .create();
            result = dbAdapter.update(tabelleName, item);
        } else if (cbKrankheitstag.isChecked()) {
            tvNettostundenTag.setText("Eingetragener Krankheitstag");
            EventItem item = new EventItem.Builder()
                    .setDatum(datum)
                    .setWochentag("k")
                    .create();
            result = dbAdapter.update(tabelleName, item);
        } else {
            EventItem item = new EventItem.Builder().setDatum(datum)
                    .setWochentag(mWochentag)
                    .setOrt(editOrtString)
                    .setArbeitsbeginn(editArbeitsbeginnString)
                    .setArbeitsbeginnKM(editFuhrlosKmString)
                    .setFuhrLos(editFuhrlosUmString)
                    .setArbeitsende(editArbeitsendeBeiAnkunftString)
                    .setArbeitsendeKM(editArbeitsendeKmString)
                    .setArbeitZuhauseBeendet(editArbeitsendeZuhauseString)
                    .setPauseInnerhalbGleitzeit(editPauseInnerhalbGleitzeitString)
                    .setPauseAusserhalbGleitzeit(editPauseAusserhalbGleitzeitString)
                    .create();

            result = dbAdapter.update(tabelleName, item);
        }
        return result;
    }

    @SuppressLint("SetTextI18n")
    public void readingFromDB() {
        String ortsangabe = dbAdapter.getInformationFromDB(datum, tabelleName,DbAdapter.ORTSANGABE_ARG);
        String pauseInnerhalbGleitzeit = dbAdapter.getInformationFromDB(datum, tabelleName, DbAdapter.PAUSE_INNERHALB_GLEITZEIT_ARG);
        String pauseAusserhalbGleitzeit = dbAdapter.getInformationFromDB(datum, tabelleName, DbAdapter.PAUSE_AUSSERHALB_GLEITZEIT_ARG);
        if (ortsangabe.isEmpty()) {
            editOrtTageswert.setText("Berlin");
            editPauseInnerhalbGleitzeit.setText("1.00");
            editPauseAusserhalbGleitzeit.setText("0.00");
        } else {
            editOrtTageswert.setText(ortsangabe);
            editPauseInnerhalbGleitzeit.setText(pauseInnerhalbGleitzeit);
            editPauseAusserhalbGleitzeit.setText(pauseAusserhalbGleitzeit);
        }

        String arbeitsbeginn = dbAdapter.getInformationFromDB(datum, tabelleName, DbAdapter.ARBEITSBEGINN_ARG);
        editArbeitsbeginnTageswert.setText(arbeitsbeginn);

        String arbeitsbeginnTachostand = dbAdapter.getInformationFromDB(datum, tabelleName, DbAdapter.ARBEITSBEGINN_KM_ARG);
        editFuhrlosKmTageswert.setText(arbeitsbeginnTachostand);

        String fuhrLosUm = dbAdapter.getInformationFromDB(datum, tabelleName,DbAdapter.FUHR_LOS_UM_ARG);
        editFuhrlosUmTageswert.setText(fuhrLosUm);

        String arbeitsendeBeiAnkunft = dbAdapter.getInformationFromDB(datum, tabelleName,DbAdapter.ARBEITSENDE_BEI_ANKUNFT_ARG);
        editArbeitsendeBeiAnkunftTageswert.setText(arbeitsendeBeiAnkunft);

        String arbeitsendeKm = dbAdapter.getInformationFromDB(datum, tabelleName,DbAdapter.ARBEITSENDE_KM_ARG);
        editArbeitsendeKmTageswert.setText(arbeitsendeKm);

        String arbeitsendeZuhause = dbAdapter.getInformationFromDB(datum, tabelleName,DbAdapter.ARBEITSENDE_ARG);
        editArbeitsendeZuhauseTageswert.setText(arbeitsendeZuhause);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onClick(View view) {

        // Handle view item clicks here.
        int id = view.getId();

        if (id == R.id.anschauen) {
            if (calendarClickBlocker) {
                Toast.makeText(getApplicationContext(), "Bitte zunächst einen Tag antippen.", Toast.LENGTH_LONG).show();
            } else {
                openDialog();
            }
        }
    }

    /**
     * Mit der Methode setEnableFalse werden die Update Eingaben aus der Monatsübersicht ausgegraut.
     * Damit wird verhindert, dass Eingaben getätigt werden können, wenn Feier-, Krankheits- oder Urlaubstag gewählt wurde.
     */
    public void setEnableFalse() {
        editOrtTageswert.setEnabled(false);
        editArbeitsbeginnTageswert.setEnabled(false);
        editFuhrlosKmTageswert.setEnabled(false);
        editFuhrlosUmTageswert.setEnabled(false);
        editArbeitsendeBeiAnkunftTageswert.setEnabled(false);
        editArbeitsendeKmTageswert.setEnabled(false);
        editArbeitsendeZuhauseTageswert.setEnabled(false);
        editPauseInnerhalbGleitzeit.setEnabled(false);
        editPauseAusserhalbGleitzeit.setEnabled(false);
    }

    /**
     * setEnableTrue ist das Gegenteil von setEnableFalse und ermöglich wieder die in DB gespeicherte
     * Eingaben zu verändern.
     */
    public void setEnableTrue() {
        Toast.makeText(getApplicationContext(), "Alle obigen Eingaben bleiben erhalten.", Toast.LENGTH_LONG).show();
        editOrtTageswert.setEnabled(true);
        editArbeitsbeginnTageswert.setEnabled(true);
        editFuhrlosKmTageswert.setEnabled(true);
        editFuhrlosUmTageswert.setEnabled(true);
        editArbeitsendeBeiAnkunftTageswert.setEnabled(true);
        editArbeitsendeKmTageswert.setEnabled(true);
        editArbeitsendeZuhauseTageswert.setEnabled(true);
        editPauseInnerhalbGleitzeit.setEnabled(true);
        editPauseAusserhalbGleitzeit.setEnabled(true);
    }

    @SuppressLint("SetTextI18n")
    public void nettostundenBerechnung() {
        float nettoStundenMonat = 0;
        for (int i = 1; i <= 31; i++)            // i ist Schleifenzähler / Tageszähler
        {
            String datumViewNettostunden = i + "." + stringMonth + "." + stringYear;
            String arbeitsbeginn = dbAdapter.getInformationFromDB(datumViewNettostunden, tabelleName, DbAdapter.ARBEITSBEGINN_ARG);
            String arbeitsendeZuhause = dbAdapter.getInformationFromDB(datumViewNettostunden, tabelleName,DbAdapter.ARBEITSENDE_ARG);
            String pauseInnerhalbGleitzeit = dbAdapter.getInformationFromDB(datumViewNettostunden, tabelleName,DbAdapter.PAUSE_INNERHALB_GLEITZEIT_ARG);
            String pauseAusserhalbGleitzeit = dbAdapter.getInformationFromDB(datumViewNettostunden, tabelleName,DbAdapter.PAUSE_AUSSERHALB_GLEITZEIT_ARG);

            if (!arbeitsbeginn.isEmpty()) {
                float arbeitsbeginnFloat = Float.parseFloat(arbeitsbeginn);
                float arbeitsendeZuhauseFloat = Float.parseFloat(arbeitsendeZuhause);
                float pauseInnerhalbGleitzeitFloat = Float.parseFloat(pauseInnerhalbGleitzeit);
                float pauseAusserhalbGleitzeitFloat = Float.parseFloat(pauseAusserhalbGleitzeit);

                float nettoStundenTag = arbeitsendeZuhauseFloat - arbeitsbeginnFloat - pauseInnerhalbGleitzeitFloat - pauseAusserhalbGleitzeitFloat;
                nettoStundenMonat = nettoStundenMonat + nettoStundenTag;
                if (datumViewNettostunden.equals(datum)) {
                    //aufrunden auf zwei Nachkommastellen
                    nettoStundenTag = (float) Math.round(nettoStundenTag * 100) / 100;
                    tvNettostundenTag.setText("Nettostunden am " + stringDay + "-" + stringMonth + ": " + nettoStundenTag);
                }
            }
        }
        TextView viewNettostunden = findViewById(R.id.viewNettostunden);
        nettoStundenMonat = (float) Math.round(nettoStundenMonat * 100) / 100;
        viewNettostunden.setText("Nettostunden gesamt " + dateiname + ": " + nettoStundenMonat);
    }

    @SuppressLint("SetTextI18n")
    public void spesenBerechnung() {
        TextView viewSpesenpauschaleTag = findViewById(R.id.viewSpesenpauschaleTag);
        viewSpesenpauschaleTag.setText("");
        int spesenMonat = 0;
        for (int i = 1; i <= 31; i++)            // i ist Schleifenzähler / Tageszähler
        {
            String datumViewNettostunden = i + "." + stringMonth + "." + stringYear;
            String fuhrLosUm = dbAdapter.getInformationFromDB(datumViewNettostunden, tabelleName, DbAdapter.FUHR_LOS_UM_ARG);
            String arbeitsendeBeiAnkunft = dbAdapter.getInformationFromDB(datumViewNettostunden, tabelleName,DbAdapter.ARBEITSENDE_BEI_ANKUNFT_ARG);

            if (!fuhrLosUm.isEmpty()) {
                float fuhrLosUmFloat = Float.parseFloat(fuhrLosUm);
                float arbeitsendeBeiAnkunftFloat = Float.parseFloat(arbeitsendeBeiAnkunft);

                float unterwegsStunden = arbeitsendeBeiAnkunftFloat - fuhrLosUmFloat;
                if (unterwegsStunden > 8) {
                    spesenMonat = spesenMonat + 12;
                    if (datumViewNettostunden.equals(datum)) {
                        viewSpesenpauschaleTag.setText("Spesenpauschale am " + stringDay + "-" + stringMonth + ": 12€");
                    }
                } else {
                    if (datumViewNettostunden.equals(datum)) {
                        viewSpesenpauschaleTag.setText("Spesenpauschale am " + stringDay + "-" + stringMonth + ": 0€");
                    }
                }
            }
        }
        TextView viewSpesenpauschaleMonat = findViewById(R.id.viewSpesenpauschaleMonat);
        viewSpesenpauschaleMonat.setText("Spesen gesamt " + dateiname + ": " + spesenMonat + "€");
    }

    @SuppressLint("SetTextI18n")
    public void kmBerechnung() {
        TextView viewKmTag = findViewById(R.id.viewKmTag);
        viewKmTag.setText("");
        int kmMonat = 0;
        for (int i = 1; i <= 31; i++)            // i ist Schleifen- / Tageszähler
        {
            String datumView = i + "." + stringMonth + "." + stringYear;

            String arbeitsbeginnTachostand = dbAdapter.getInformationFromDB(datumView, tabelleName, DbAdapter.ARBEITSBEGINN_KM_ARG);
            String arbeitsendeKm = dbAdapter.getInformationFromDB(datumView, tabelleName,DbAdapter.ARBEITSENDE_KM_ARG);

            if (!arbeitsbeginnTachostand.isEmpty()) {
                int heutigeKm = Integer.parseInt(arbeitsendeKm) - Integer.parseInt(arbeitsbeginnTachostand);
                kmMonat = kmMonat + heutigeKm;
                if (datumView.equals(datum)) {
                    viewKmTag.setText("Am " + stringDay + "-" + stringMonth + " bist du dienstlich gefahren: " + heutigeKm + "km");
                }
            }
        }
        TextView viewKmMonat = findViewById(R.id.viewKmMonat);
        viewKmMonat.setText("Dienstlich gesamt gefahren " + dateiname + ": " + kmMonat + "km");
    }

    @SuppressLint("SetTextI18n")
    public void urlaubstageBerechnung() {
        TextView viewJahresUrlaub = findViewById(R.id.viewJahresUrlaub);
        int urlaubJahr = 0;

        for (int j = 1; j <= 12; j++)            // j ist Schleifen- / Monatsszähler
        {
            String monatView = ABRECHNUNG + stringYear + "_" + j;

            for (int i = 1; i <= 31; i++)            // i ist Schleifen- / Tageszähler
            {
                if (i == 1) {
                    // bei der Prüfung des genommenen Jahresurlaubes werden neue Monatstabellen angelegt, falls diese nicht vorhanden sind
                    dbAdapter.erstelleTabelle(monatView);
                }
                String datumView = i + "." + j + "." + stringYear;

                String wochentag = dbAdapter.getInformationFromDB(datumView, monatView, DbAdapter.WOCHENTAG_ARG);

                if (wochentag.equals("u")) {
                    urlaubJahr = urlaubJahr + 1;
                }
            }
        }
        viewJahresUrlaub.setText("Im Jahr " + stringYear + " Urlaubstage genommen: " + urlaubJahr);
    }
}
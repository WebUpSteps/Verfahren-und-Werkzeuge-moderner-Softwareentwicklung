package com.example.oko.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;

import android.view.View;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.util.Date;
import java.util.Locale;

/**
 * Mit MainActivity wird die erste Ansicht der Applikation gestartet.
 * Das Wichtigste hierbei ist die onClick Methode, in der auch definiert wurde, was passiert beim tippen auf die Buttons.
 *
 * @author Oleg Kossjak
 */
@RequiresApi(api =Build.VERSION_CODES.N)
public class MainActivity extends Activity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    // Initialisierung zum abgreiffen der Kalenderwerte
    SimpleDateFormat datumsformat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.GERMANY);
    SimpleDateFormat datumFormat = new SimpleDateFormat("d.M.yyyy", Locale.GERMANY);
    SimpleDateFormat stunde = new SimpleDateFormat("HH", Locale.GERMANY);
    SimpleDateFormat minuten = new SimpleDateFormat("mm", Locale.GERMANY);
    SimpleDateFormat wochentagAbkuerzung = new SimpleDateFormat("E", Locale.GERMANY);
    // für DB Einträge
    String wochentag = wochentagAbkuerzung.format(new Date());
    String datum = datumFormat.format(new Date());
    String uhrzeit;
    String ortString = "";

    //die nachfolgende Variable blockiert den Startbutton, solange Ende nicht gedrückt wurde
    boolean blockVariable = false;
    boolean blockVariableNachLosfahren = false;
    boolean arbeiteWeiterZuhause;

    // Activity Elemente
    private Button btnEnde;


    // ausgeblendete Buttons
    private Button btnFahreLos;
    private Button btnArbeitstagBeenden;

    //nachfolgend sind Zeitanzeigefelder neben den Buttons definiert
    private TextView tvArbeitstagBeginn;
    private TextView tvFahrtEnde;
    private TextView tvFahreLos;
    private TextView tvArbAitstagBeenden;
    private EditText etEditKmVormStart;

    // Tachostand Eingabefeld
    private int editKmVormStartInt;
    private int editKmBeimEndeInt;

    // Schnittstelle zur SQLite
    private DbAdapter tag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer,toolbar,  R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);


        tvArbeitstagBeginn = findViewById(R.id.ArbeitstagBeginntextView);
        Button btnStart = findViewById(R.id.start_button);
        tvFahrtEnde = findViewById(R.id.FahrtEndeTextView);
        btnEnde = findViewById(R.id.ende_button);
        tvFahreLos = findViewById(R.id.fahreLosTextView);
        btnFahreLos = findViewById(R.id.fahreLos);
        tvArbAitstagBeenden = findViewById(R.id.arbeitstagBeendenTextView);
        btnArbeitstagBeenden = findViewById(R.id.arbeitstagBeenden);
        etEditKmVormStart = findViewById(R.id.editKmVormStart);

        navigationView.setNavigationItemSelectedListener(this);
        btnStart.setOnClickListener(this);
        btnEnde.setOnClickListener(this);
        btnFahreLos.setOnClickListener(this);
        btnArbeitstagBeenden.setOnClickListener(this);

        // For android 6+, besides manifest permission you should ask permission in runtime as well
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        }, 666);

        tag = new DbAdapter(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Toast.makeText(MainActivity.this, "Bitte den HOME Button benutzen um zu einer anderen App zu wechseln.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        Toast.makeText(MainActivity.this, "on Destroy called", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_monat) {
            //An Intent is an object that provides runtime binding between separate components, such as two activities
            Intent calenderIntent = new Intent(this, Kalender.class);
            startActivity(calenderIntent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onClick(View view) {

        String startMinuteDezimal = minuten.format(new Date());
        float minuteDezimal = Float.parseFloat(startMinuteDezimal);
        minuteDezimal = minuteDezimal / 60 * 100;
        int pagesQty = (int) Math.ceil(minuteDezimal);
        Date date = new Date();
        if (pagesQty < 10) {
            uhrzeit = stunde.format(date) + ".0" + pagesQty;
        }
        if (pagesQty > 10) {
            uhrzeit = stunde.format(date) + "." + pagesQty;
        }

        // Texteingabefeld für den aktuellen Kilometerstand

        // Handle view item clicks here.
        int id = view.getId();

        if (id == R.id.start_button) {
            onStartButtonClicked(etEditKmVormStart);
        } else if (id == R.id.fahreLos) {
            onFahrelosClicked(date, etEditKmVormStart);
        } else if (id == R.id.ende_button) {
            onEndeClicked(date, etEditKmVormStart);
        } else if (id == R.id.arbeitstagBeenden) {
            onArbeitstagBeendenClicked(date);
        }
    }

    private void onStartButtonClicked(EditText editKmVormStartEditText) {
        if (!blockVariable) {
            // GUI Interface vorbereitung
            arbeiteWeiterZuhause = false;
            tvArbeitstagBeginn.setText("");
            tvFahrtEnde.setText("");
            tvFahreLos.setText("");
            tvArbAitstagBeenden.setText("");
            btnFahreLos.setVisibility(View.INVISIBLE);
            btnArbeitstagBeenden.setVisibility(View.INVISIBLE);

            AlertDialog.Builder arbeitstagBeginnAlert = new AlertDialog.Builder(this)
                    .setCancelable(true)
                    .setMessage("Fährst jetzt gleich zum Kunden oder arbeitest du zunächst von Zuhause?")
                    .setPositiveButton("Kunde", (dialogInterface, i) ->
                            onKundePositiveButtonClicked(editKmVormStartEditText))
                    .setNegativeButton("Zuhause", (dialogInterface, i) -> onZuhauseClicked());

            AlertDialog alertDialog = arbeitstagBeginnAlert.create();
            alertDialog.setCanceledOnTouchOutside(true);
            alertDialog.show();

        } else {
            Toast.makeText(MainActivity.this, "Deinen Arbeitstag hast du bereits begonnen.", Toast.LENGTH_SHORT).show();
        }
    }

    private void onKundePositiveButtonClicked(EditText editKmVormStartEditText) {
        // Tachostand Eingabefeld
        String editKmVormStart = ((EditText) findViewById(R.id.editKmVormStart)).getText().toString();
        if (editKmVormStart.isEmpty()) {
            Toast.makeText(MainActivity.this, getString(R.string.bitte_den_aktuallen), Toast.LENGTH_SHORT).show();
        } else {

            EventItem item = new EventItem.Builder()
                    .setWochentag(wochentag)
                    .setDatum(datum)
                    .setOrt("Berlin")
                    .setArbeitsbeginn(uhrzeit)
                    .setArbeitsbeginnKM(editKmVormStart)
                    .setFuhrLos(uhrzeit)
                    .setPauseInnerhalbGleitzeit("1.00")
                    .setPauseAusserhalbGleitzeit("0.00")
                    .create();
            long ubergabe = tag.insertData(item);
            if (ubergabe <= 0) {
                Toast.makeText(MainActivity.this, "Speicherung nicht möglich. Bitte Tageseinträge in Monatsübersicht prüfen.", Toast.LENGTH_LONG).show();
            } else {
                editKmVormStartInt = Integer.parseInt(editKmVormStart);
                tvArbeitstagBeginn.setText(datumsformat.format(new Date()));
                blockVariable = true;
                editKmVormStartEditText.setText("");
            }
        }
    }

    private void onZuhauseClicked() {
        EventItem item = new EventItem.Builder()
                .setWochentag(wochentag)
                .setDatum(datum)
                .setOrt("Berlin")
                .setArbeitsbeginn(uhrzeit)
                .setArbeitsbeginnKM("0")
                .setFuhrLos(uhrzeit)
                .setPauseInnerhalbGleitzeit("1.00")
                .setPauseAusserhalbGleitzeit("0.00")
                .create();

        long ubergabe = tag.insertData(item);
        if (ubergabe <= 0) {
            Toast.makeText(MainActivity.this, "Speicherung nicht möglich. Bitte Tageseinträge in Monatsübersicht prüfen.", Toast.LENGTH_LONG).show();
        } else {

            tvArbeitstagBeginn.setText(datumsformat.format(new Date()));
            blockVariable = true;
            blockVariableNachLosfahren = true;
            btnFahreLos.setVisibility(View.VISIBLE);
            btnEnde.setText("Ende");
        }
    }

    private void onArbeitstagBeendenClicked(Date date) {
        if (blockVariable) {
            int c = tag.updateBeimEndeZuhause(datum, uhrzeit);
            if (c <= 0) {
                Util.showToastLong(getApplicationContext(), R.string.unsuccessfull);
            } else {
                Util.showToastLong(getApplicationContext(), R.string.datenbanktabelle_atualisiert);
            }
            tvArbAitstagBeenden.setText(datumsformat.format(date));
            blockVariable = false;
        } else {
            Util.showToastShort(MainActivity.this, "Dein Arbeitstag hast du bereits beendet.");
        }
    }

    private void onEndeClicked(Date date, EditText editKmVormStartEditText) {
        if (blockVariable) {
            if (!arbeiteWeiterZuhause) {
                // Tachostand Eingabefeld
                String editKmBeimEnde = (editKmVormStartEditText.getText().toString());
                if (editKmBeimEnde.isEmpty()) {
                    handleEmptyData(date, editKmVormStartEditText);
                } else {
                    updateData(editKmVormStartEditText);
                }
            } else {
                Toast.makeText(MainActivity.this, "Du arbeitest nach dem Kundeinsatz Zuhause.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "Du kannst deine Fahrt nicht beenden, da du es noch nicht angefangen hast.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateData(EditText editKmVormStartEditText) {
        editKmBeimEndeInt = Integer.parseInt(editKmVormStartEditText.getText().toString());

        AlertDialog.Builder setEndeAlert = new AlertDialog.Builder(this)
                .setCancelable(true)
                .setMessage("Arbeitstag entgültig beenden oder arbeitest du noch Zuhause weiter?")
                .setPositiveButton("Beenden", (dialogInterface2, i2) -> onBeendenClicked(editKmVormStartEditText))
                .setNegativeButton("Zuhause", (dialogInterface2, i2) -> onZuhauseClicked(editKmVormStartEditText));
        AlertDialog alertDialog = setEndeAlert.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();

        // get prompts.xml view
        View promptsView = getLayoutInflater().inflate(R.layout.prompts, null);
        final EditText ort = promptsView.findViewById(R.id.editTextDialogUserInput);

        new AlertDialog.Builder(MainActivity.this)
                .setView(promptsView)
                .setCancelable(false)
                .setPositiveButton("OK",
                        (dialog, id) -> {
                            // get user input and update DB
                            ortString = ort.getText().toString();
                            tag.updateOrt(datum, ortString);
                        })
                .create()
                .show();
    }

    private void handleEmptyData(Date date, EditText editKmVormStartEditText) {
        // wenn nach Los Fahren keine Kilometer eingegeben wurden
        if (!blockVariableNachLosfahren) {
            Toast.makeText(MainActivity.this, getString(R.string.bitte_den_aktuallen), Toast.LENGTH_SHORT).show();
        }
        // sonst geht Programm davon aus, dass der Arbeitstag Zuhause verbracht wurde
        else {
            tvFahrtEnde.setText(datumsformat.format(date));
            int a = tag.updateKMbeimEnde(datum, Integer.toString(editKmBeimEndeInt), uhrzeit);
            if (a <= 0) {
                Util.showToastLong(getApplicationContext(), R.string.unsuccessfull);
            } else {
                Util.showToastLong(getApplicationContext(), R.string.datenbanktabelle_atualisiert);
            }
            blockVariable = false;
            editKmVormStartEditText.setText("");
        }
    }

    private void onZuhauseClicked(EditText editKmVormStartEditText) {
        if (editKmBeimEndeInt < editKmVormStartInt) {
            Toast.makeText(MainActivity.this, "Bitte den korrekten Tachostand eingeben.", Toast.LENGTH_SHORT).show();
        } else {
            arbeiteWeiterZuhause = true;

            tvFahrtEnde.setText(datumsformat.format(new Date()));
            int b = tag.updateKMbeimEnde(datum, Integer.toString(editKmBeimEndeInt), uhrzeit);
            if (b <= 0) {
                Util.showToastLong(getApplicationContext(), R.string.unsuccessfull);
            } else {
                Util.showToastLong(getApplicationContext(), R.string.datenbanktabelle_atualisiert);
            }
            btnArbeitstagBeenden.setVisibility(View.VISIBLE);
            editKmVormStartEditText.setText("");
        }
    }

    private void onBeendenClicked(EditText editKmVormStartEditText) {
        if (editKmBeimEndeInt <= editKmVormStartInt) {
            Toast.makeText(MainActivity.this, "Bitte den korrekten Tachostand eingeben.", Toast.LENGTH_SHORT).show();
        } else {

            tvFahrtEnde.setText(datumsformat.format(new Date()));
            int a = tag.updateKMbeimEnde(datum, Integer.toString(editKmBeimEndeInt), uhrzeit);
            if (a <= 0) {
                Util.showToastLong(getApplicationContext(), R.string.unsuccessfull);
            } else {
                Util.showToastLong(getApplicationContext(), R.string.datenbanktabelle_atualisiert);
            }
            blockVariable = false;
            editKmVormStartEditText.setText("");
        }
    }

    private void onFahrelosClicked(Date date, EditText editKmVormStartEditText) {
        if (blockVariableNachLosfahren && blockVariable && !arbeiteWeiterZuhause) {
            // Tachostand Eingabefeld
            String editKmVormStart = editKmVormStartEditText.getText().toString();
            if (editKmVormStart.isEmpty()) {
                Toast.makeText(MainActivity.this, getString(R.string.bitte_den_aktuallen), Toast.LENGTH_SHORT).show();
            } else {
                tvFahreLos.setText(datumsformat.format(date));
                editKmVormStartInt = Integer.parseInt(editKmVormStart);
                int a = tag.updateFuhrLos(datum, Integer.toString(editKmVormStartInt), uhrzeit);
                if (a <= 0) {
                    Util.showToastLong(getApplicationContext(), R.string.unsuccessfull);
                } else {
                    Util.showToastLong(getApplicationContext(), R.string.datenbanktabelle_atualisiert);
                }
                blockVariableNachLosfahren = false;
                editKmVormStartEditText.setText("");
                btnEnde.setText("Fahrt Ende");
            }
        } else {
            Toast.makeText(MainActivity.this, "Nicht möglich.", Toast.LENGTH_SHORT).show();
        }
    }
}
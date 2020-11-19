package com.example.oko.myapplication;

import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

@RequiresApi(api = Build.VERSION_CODES.N)
public class SaveToFile {
    public static final String TAG = SaveToFile.class.getName();
    //Ordner wo die Datei abgespeichert wird
    private final File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/Zeitabrechnung");

    /**
     * Methode saveInFile() schreibt im Download Ordner eine Monatsübersichtsdatei zur Weiterverwendung
     *
     * @param tag   ermöglicht den Zugriff auf die Datenbank
     * @param year  liefert das Jahr des ausgewählten Monats
     * @param month liefert den ausgewählten Monat
     * @author Oleg Kossjak
     */
    public void saveInFile(DbAdapter tag, String year, String month) throws SaveCalendarDataException {

        String tabelleName = "abrechnung_" + year + "_" + month;
        if (!path.exists()) {
            path.mkdirs();
        }
        File myFile = new File(path, tabelleName + ".csv");
        deleteOldFile(myFile);

        int tachostandMonatsEnde = 0;
        boolean schaltvariable = true; // soll verhindern, dass Monatsanfag Tachostand überschrieben wird

        for (int i = 1; i <= 31; i++) {           // i ist Schleifen- / Tageszähler

            int dienstlichGefahreneKilometer = 0;
            float unterwegsStunden;
            String spesenTag = "";
            String tachostandMonatsAnfang = "";
            String datum = i + "." + month + "." + year;

            String wochentag = tag.getInformationFromDB(datum, tabelleName, DbAdapter.WOCHENTAG_ARG);
            String ortsangabe = tag.getInformationFromDB(datum, tabelleName, DbAdapter.ORTSANGABE_ARG);
            String arbeitsbeginn = tag.getInformationFromDB(datum, tabelleName, DbAdapter.ARBEITSBEGINN_ARG);
            String arbeitsende = tag.getInformationFromDB(datum, tabelleName, DbAdapter.ARBEITSENDE_ARG);
            String pauseInnerhalbGleitzeit = tag.getInformationFromDB(datum, tabelleName, DbAdapter.PAUSE_INNERHALB_GLEITZEIT_ARG);
            String pauseAusserhalbGleitzeit = tag.getInformationFromDB(datum, tabelleName, DbAdapter.PAUSE_AUSSERHALB_GLEITZEIT_ARG);
            String fuhrLosUm = tag.getInformationFromDB(datum, tabelleName, DbAdapter.FUHR_LOS_UM_ARG);
            String arbeitsendeBeiAnkunft = tag.getInformationFromDB(datum, tabelleName, DbAdapter.ARBEITSENDE_BEI_ANKUNFT_ARG);
            String arbeitsbeginnKm = tag.getInformationFromDB(datum, tabelleName, DbAdapter.ARBEITSBEGINN_KM_ARG);
            String arbeitsendeKm = tag.getInformationFromDB(datum, tabelleName, DbAdapter.ARBEITSENDE_KM_ARG);


            if (!isWochetagEmpty(wochentag, arbeitsbeginn) && !isWochetagValid(wochentag)) {
                float fuhrLosUmFloat = Float.parseFloat(fuhrLosUm);
                float arbeitsendeBeiAnkunftFloat = Float.parseFloat(arbeitsendeBeiAnkunft);
                unterwegsStunden = arbeitsendeBeiAnkunftFloat - fuhrLosUmFloat;
                if (unterwegsStunden > 8) {
                    spesenTag = "12";
                }
                dienstlichGefahreneKilometer = getDienstlichGefahreneKilometer(datum, arbeitsbeginnKm, arbeitsendeKm);

                if (dienstlichGefahreneKilometer != 0 && schaltvariable) {
                    tachostandMonatsAnfang = arbeitsbeginnKm;
                    schaltvariable = false;
                }

                if (tachostandMonatsEnde < Integer.parseInt(arbeitsendeKm)) {
                    tachostandMonatsEnde = Integer.parseInt(arbeitsendeKm);
                }

            }

            String result = datum + ";" + wochentag + ";" + ortsangabe + ";" + arbeitsbeginn + ";" + arbeitsende + ";" + pauseInnerhalbGleitzeit + ";" + pauseAusserhalbGleitzeit + ";" + spesenTag + ";" + dienstlichGefahreneKilometer + ";" + tachostandMonatsAnfang + ";" + tachostandMonatsEnde + ";\n";
            //ab hier sollen aus der for Schleife die täglichen Monatswerte in einer Datei gespeichert werden

            writeToFile(myFile, result);
        }
    }

    private int getDienstlichGefahreneKilometer(String datum, String arbeitsbeginnKm, String arbeitsendeKm) throws SaveCalendarDataException {
        try {
            return Integer.parseInt(arbeitsendeKm) - Integer.parseInt(arbeitsbeginnKm);
        } catch (NumberFormatException nfe) {
            throw new SaveCalendarDataException("Bitte Kilometerstand Eingabe am " + datum + " prüfen und erneut exportieren");
        }
    }

    private void writeToFile(File myFile, String result) throws SaveCalendarDataException {
        try (FileWriter fw = new FileWriter(myFile, true);) {
            fw.write(result);
        } catch (IOException e) {
            //do something if an IOException occurs.
            e.printStackTrace();
            throw new SaveCalendarDataException("ERROR - Text could't be added");
        }
    }

    private boolean isWochetagEmpty(String wochentag, String arbeitsbeginn) {
        return wochentag.isEmpty() || wochentag == null || arbeitsbeginn.equals("");
    }

    private boolean isWochetagValid(String wochentag) {
        return wochentag.equals("u") || wochentag.equals("k") || wochentag.equals("f");
    }

    private void deleteOldFile(File myFile) throws SaveCalendarDataException{
        // um Redundanzen in der Datei zu vermeiden, wird die alte Version zunächst gelöscht
        try {
            Files.delete(myFile.toPath());
            Log.i(TAG, "File is deleted!");
            // der Übersichtlichkeitshalber, wird die erste Zeile der csv Datei die Spaltennamen enthalten
            try (FileWriter fw = new FileWriter(myFile, true)) {
                fw.write("Datum;" + "Wochentag;" + "Ortsangabe;" + "Arbeitsbeginn;" + "Arbeitsende;" + "Pause innerhalb der Gleitzeit;" + "Pause ausserhalb der Gleitzeit;" + "Spesenpauschale;" + "Dienst - km;" + "Tachostand M-Anfang;" + "Tachostand M-Ende;\n");
            }
        } catch (IOException e) {
            //do something if an IOException occurs.
            Log.e(TAG, "Datei konnte nicht geschrieben werden", e);
            throw new SaveCalendarDataException("ERROR - Text could't be added");

        }
    }
}
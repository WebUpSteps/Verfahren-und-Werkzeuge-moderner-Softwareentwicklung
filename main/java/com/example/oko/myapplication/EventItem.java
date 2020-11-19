package com.example.oko.myapplication;

public class EventItem {
    private String wochentag;
    private String datum;
    private String ort;
    private String arbeitsbeginn;
    private String arbeitsbeginnKM;
    private String fuhrLos;
    private String arbeitsende;
    private String arbeitsendeKM;
    private String arbeitZuhauseBeendet;
    private String pauseInnerhalbGleitzeit;
    private String pauseAusserhalbGleitzeit;

    public String getWochentag() {
        return wochentag;
    }

    public String getDatum() {
        return datum;
    }

    public String getOrt() {
        return ort;
    }

    public String getArbeitsbeginn() {
        return arbeitsbeginn;
    }

    public String getArbeitsbeginnKM() {
        return arbeitsbeginnKM;
    }

    public String getFuhrLos() {
        return fuhrLos;
    }

    public String getArbeitsende() {
        return arbeitsende;
    }

    public String getArbeitsendeKM() {
        return arbeitsendeKM;
    }

    public String getArbeitZuhauseBeendet() {
        return arbeitZuhauseBeendet;
    }

    public String getPauseInnerhalbGleitzeit() {
        return pauseInnerhalbGleitzeit;
    }

    public String getPauseAusserhalbGleitzeit() {
        return pauseAusserhalbGleitzeit;
    }

    public static class Builder {
        private String wochentag = "";
        private String datum = "";
        private String ort = "";
        private String arbeitsbeginn = "";
        private String arbeitsbeginnKM = "";
        private String fuhrLos = "";
        private String arbeitsende = "";
        private String arbeitsendeKM = "";
        private String arbeitZuhauseBeendet = "";
        private String pauseInnerhalbGleitzeit = "";
        private String pauseAusserhalbGleitzeit = "";

        public EventItem.Builder setWochentag(String wochentag) {
            this.wochentag = wochentag;
            return this;
        }

        public EventItem.Builder setDatum(String datum) {
            this.datum = datum;
            return this;
        }

        public EventItem.Builder setOrt(String ort) {
            this.ort = ort;
            return this;
        }

        public EventItem.Builder setArbeitsbeginn(String arbeitsbeginn) {
            this.arbeitsbeginn = arbeitsbeginn;
            return this;
        }

        public EventItem.Builder setArbeitsbeginnKM(String arbeitsbeginnKM) {
            this.arbeitsbeginnKM = arbeitsbeginnKM;
            return this;
        }

        public EventItem.Builder setFuhrLos(String fuhrLos) {
            this.fuhrLos = fuhrLos;
            return this;
        }

        public EventItem.Builder setArbeitsende(String arbeitsende) {
            this.arbeitsende = arbeitsende;
            return this;
        }

        public EventItem.Builder setArbeitsendeKM(String arbeitsendeKM) {
            this.arbeitsendeKM = arbeitsendeKM;
            return this;
        }

        public EventItem.Builder setArbeitZuhauseBeendet(String arbeitZuhauseBeendet) {
            this.arbeitZuhauseBeendet = arbeitZuhauseBeendet;
            return this;
        }

        public EventItem.Builder setPauseInnerhalbGleitzeit(String pauseInnerhalbGleitzeit) {
            this.pauseInnerhalbGleitzeit = pauseInnerhalbGleitzeit;
            return this;
        }

        public EventItem.Builder setPauseAusserhalbGleitzeit(String pauseAusserhalbGleitzeit) {
            this.pauseAusserhalbGleitzeit = pauseAusserhalbGleitzeit;
            return this;
        }

        public EventItem create(){
            EventItem eventItem = new EventItem();
             eventItem.wochentag = wochentag;
             eventItem.datum = datum;
             eventItem.ort = ort;
             eventItem.arbeitsbeginn = arbeitsbeginn;
             eventItem.arbeitsbeginnKM = arbeitsbeginnKM;
             eventItem.fuhrLos = fuhrLos;
             eventItem.arbeitsende = arbeitsende;
             eventItem.arbeitsendeKM = arbeitsendeKM;
             eventItem.arbeitZuhauseBeendet = arbeitZuhauseBeendet;
             eventItem.pauseInnerhalbGleitzeit = pauseInnerhalbGleitzeit;
             eventItem.pauseAusserhalbGleitzeit = pauseAusserhalbGleitzeit;
             return eventItem;
        }
    }
}










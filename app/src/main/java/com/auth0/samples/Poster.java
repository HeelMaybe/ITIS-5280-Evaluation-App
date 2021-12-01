package com.auth0.samples;

import java.io.Serializable;

public class Poster implements Serializable {
    private String Id;
    private String title;
    private String participants;
    private String NFC;

    public Poster(String id, String title, String participants, String NFC) {
        Id = id;
        this.title = title;
        this.participants = participants;
        this.NFC = NFC;
    }

    public Poster() {
    }

    public String getParticipants() {
        return participants;
    }

    public void setParticipants(String participants) {
        this.participants = participants;
    }

    public String getNFC() {
        return NFC;
    }

    public void setNFC(String NFC) {
        this.NFC = NFC;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

package com.auth0.samples;

public class Question {
    private String questionText;
    private int id;

    public Question(String questionText, int id) {
        this.questionText = questionText;
        this.id = id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

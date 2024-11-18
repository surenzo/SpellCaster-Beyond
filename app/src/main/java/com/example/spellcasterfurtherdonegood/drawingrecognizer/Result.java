package com.example.spellcasterfurtherdonegood.drawingrecognizer;

public class Result {
    private String name;
    private float score;
    private long time;

    public Result(String name, float score, long time) {
        this.name = name;
        this.score = score;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public float getScore() {
        return score;
    }

    public long getTime() {
        return time;
    }
}
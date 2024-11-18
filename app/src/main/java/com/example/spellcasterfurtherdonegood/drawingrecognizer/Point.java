package com.example.spellcasterfurtherdonegood.drawingrecognizer;


public class Point {
    private float x, y;
    private int id;

    public Point(float x, float y, int id) {
        this.x = x;
        this.y = y;
        this.id = id;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getID() {
        return id;
    }
}
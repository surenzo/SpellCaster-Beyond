package com.example.spellcasterfurtherdonegood.drawingrecognizer;

import java.util.List;

public class PointCloud {
    private String name;
    private List<Point> points;

    public PointCloud(String name, List<Point> points) {
        this.name = name;
        this.points = PDollarRecognizer.resample(points, PDollarRecognizer.NumPoints);
        this.points = PDollarRecognizer.scale(this.points);
        this.points = PDollarRecognizer.translateTo(this.points, PDollarRecognizer.Origin);
    }
    public String getName() {
        return name;
    }

    public List<Point> getPoints() {
        return points;
    }
}
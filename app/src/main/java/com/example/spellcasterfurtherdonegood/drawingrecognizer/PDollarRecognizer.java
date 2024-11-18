package com.example.spellcasterfurtherdonegood.drawingrecognizer;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class PDollarRecognizer {

    public static final int NumPointClouds = 16;
    public static final int NumPoints = 32;
    public static final Point Origin = new Point(0, 0, 0);

    public List<PointCloud> pointClouds;

    public PDollarRecognizer() {
        pointClouds = new ArrayList<>(NumPointClouds);
        pointClouds.add(new PointCloud("T", List.of(
                new Point(30, 7, 1), new Point(103, 7, 1),
                new Point(66, 7, 2), new Point(66, 87, 2)
        )));
        pointClouds.add(new PointCloud("N", List.of(
                new Point(177, 92, 1), new Point(177, 2, 1),
                new Point(182, 1, 2), new Point(246, 95, 2),
                new Point(247, 87, 3), new Point(247, 1, 3)
        )));
        //add O point cloud
        pointClouds.add(new PointCloud("O", List.of(new Point(382,310,1),new Point(377,308,1),new Point(373,307,1),new Point(366,307,1),new Point(360,310,1),new Point(356,313,1),new Point(353,316,1),new Point(349,321,1),new Point(347,326,1),new Point(344,331,1),new Point(342,337,1),new Point(341,343,1),new Point(341,350,1),new Point(341,358,1),new Point(342,362,1),new Point(344,366,1),new Point(347,370,1),new Point(351,374,1),new Point(356,379,1),new Point(361,382,1),new Point(368,385,1),new Point(374,387,1),new Point(381,387,1),new Point(390,387,1),new Point(397,385,1),new Point(404,382,1),new Point(408,378,1),new Point(412,373,1),new Point(416,367,1),new Point(418,361,1),new Point(419,353,1),new Point(418,346,1),new Point(417,341,1),new Point(416,336,1),new Point(413,331,1),new Point(410,326,1),new Point(404,320,1),new Point(400,317,1),new Point(393,313,1),new Point(392,312,1)
        )));
        Log.d("PDollarRecognizer", "pointClouds: " + pointClouds);
        Log.d("PDollarRecognizer", "pointCloud.size(): " + pointClouds.get(0).getPoints().size() + " " + pointClouds.get(1).getPoints().size());
        // Add other predefined point clouds here...
    }

    public Result recognize(List<Point> points) {
        long t0 = System.currentTimeMillis();
        PointCloud candidate = new PointCloud("", points);

        int u = -1;
        float b = Float.POSITIVE_INFINITY;
        for (int i = 0; i < pointClouds.size(); i++) {
            float d = greedyCloudMatch(candidate.getPoints(), pointClouds.get(i));
            if (d < b) {
                b = d;
                u = i;
            }
        }
        long t1 = System.currentTimeMillis();
        return (u == -1) ? new Result("No match.", 0.0f, t1 - t0) : new Result(pointClouds.get(u).getName(), b > 1.0 ? 1.0f / b : 1.0f, t1 - t0);
    }

    public int addGesture(String name, List<Point> points) {
        pointClouds.add(new PointCloud(name, points));
        int num = 0;
        for (PointCloud pc : pointClouds) {
            if (pc.getName().equals(name)) {
                num++;
            }
        }
        return num;
    }

    public int deleteUserGestures() {
        pointClouds.subList(NumPointClouds, pointClouds.size()).clear();
        return NumPointClouds;
    }

    private float greedyCloudMatch(List<Point> points, PointCloud P) {
        double e = 0.50;
        int step = (int) Math.floor(Math.pow(points.size(), 1.0 - e));
        float min = Float.POSITIVE_INFINITY;
        for (int i = 0; i < points.size(); i += step) {
            float d1 = cloudDistance(points, P.getPoints(), i);
            float d2 = cloudDistance(P.getPoints(), points, i);
            min = Math.min(min, Math.min(d1, d2));
        }
        return min;
    }

    private float cloudDistance(List<Point> pts1, List<Point> pts2, int start) {
        if(pts1.size() != pts2.size()) {
            throw new IllegalArgumentException("The number of points in both point clouds must be equal." +
                    "The number of points in the first point cloud is " + pts1.size() +
                    " and the number of points in the second point cloud is " + pts2.size() + ".");
        }
        boolean[] matched = new boolean[pts1.size()];
        float sum = 0;
        int i = start;
        do {
            int index = -1;
            float min = Float.POSITIVE_INFINITY;
            for (int j = 0; j < matched.length; j++) {
                if (!matched[j]) {
                    float d = distance(pts1.get(i), pts2.get(j));
                    if (d < min) {
                        min = d;
                        index = j;
                    }
                }
            }
            matched[index] = true;
            float weight = 1 - ((i - start + pts1.size()) % pts1.size()) / (float) pts1.size();
            sum += weight * min;
            i = (i + 1) % pts1.size();
        } while (i != start);
        return sum;
    }

    public static List<Point> resample(List<Point> points, int n) {
        float I = pathLength(points) / (n - 1);
        float D = 0;
        List<Point> newPoints = new ArrayList<>();
        newPoints.add(points.get(0));
        List<Point> mutablePoints = new ArrayList<>(points); // Create a new mutable list
        for (int i = 1; i < mutablePoints.size(); i++) {
            if (mutablePoints.get(i).getID() == mutablePoints.get(i - 1).getID()) {
                float d = distance(mutablePoints.get(i - 1), mutablePoints.get(i));
                if ((D + d) >= I) {
                    float qx = mutablePoints.get(i - 1).getX() + ((I - D) / d) * (mutablePoints.get(i).getX() - mutablePoints.get(i - 1).getX());
                    float qy = mutablePoints.get(i - 1).getY() + ((I - D) / d) * (mutablePoints.get(i).getY() - mutablePoints.get(i - 1).getY());
                    Point q = new Point(qx, qy, mutablePoints.get(i).getID());
                    newPoints.add(q);
                    mutablePoints.add(i, q);
                    D = 0;
                } else {
                    D += d;
                }
            }
        }
        if (newPoints.size() == n - 1) {
            newPoints.add(new Point(mutablePoints.get(mutablePoints.size() - 1).getX(), mutablePoints.get(mutablePoints.size() - 1).getY(), mutablePoints.get(mutablePoints.size() - 1).getID()));
        }
        return newPoints;
    }


    public static List<Point> scale(List<Point> points) {
        float minX = Float.POSITIVE_INFINITY, maxX = Float.NEGATIVE_INFINITY, minY = Float.POSITIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY;
        for (Point point : points) {
            minX = Math.min(minX, point.getX());
            minY = Math.min(minY, point.getY());
            maxX = Math.max(maxX, point.getX());
            maxY = Math.max(maxY, point.getY());
        }
        float size = Math.max(maxX - minX, maxY - minY);
        List<Point> newPoints = new ArrayList<>();
        for (Point point : points) {
            float qx = (point.getX() - minX) / size;
            float qy = (point.getY() - minY) / size;
            newPoints.add(new Point(qx, qy, point.getID()));
        }
        return newPoints;
    }

    public static List<Point> translateTo(List<Point> points, Point pt) {
        Point c = centroid(points);
        List<Point> newPoints = new ArrayList<>();
        for (Point point : points) {
            float qx = point.getX() + pt.getX() - c.getX();
            float qy = point.getY() + pt.getY() - c.getY();
            newPoints.add(new Point(qx, qy, point.getID()));
        }
        return newPoints;
    }

    private static Point centroid(List<Point> points) {
        float x = 0, y = 0;
        for (Point point : points) {
            x += point.getX();
            y += point.getY();
        }
        x /= points.size();
        y /= points.size();
        return new Point(x, y, 0);
    }

    private static float pathLength(List<Point> points) {
        float d = 0;
        for (int i = 1; i < points.size(); i++) {
            if (points.get(i).getID() == points.get(i - 1).getID()) {
                d += distance(points.get(i - 1), points.get(i));
            }
        }
        return d;
    }

    private static float distance(Point p1, Point p2) {
        float dx = p2.getX() - p1.getX();
        float dy = p2.getY() - p1.getY();
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
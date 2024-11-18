package com.example.spellcasterfurtherdonegood.drawingrecognizer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class PointCloudView extends View {
    private List<PointCloud> pointClouds;
    private Paint paint;
    private int[] colors = {0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFFFFFF00, 0xFFFF00FF, 0xFF00FFFF}; // Red, Green, Blue, Yellow, Magenta, Cyan

    public PointCloudView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        pointClouds = new ArrayList<>();
        paint = new Paint();
        paint.setStrokeWidth(0.05f);
    }

    public void setPointClouds(List<PointCloud> pointClouds) {
        this.pointClouds = pointClouds;
        invalidate(); // Request to redraw the view
    }

    public void add(List<Point> points) {
        this.pointClouds.add(new PointCloud("", points));
        invalidate(); // Request to redraw the view
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d("PointCloudView", "onDraw");
        if (pointClouds != null) {
            float[] bounds = calculateBounds();
            Log.d("PointCloudView", "Bounds: minX=" + bounds[0] + ", minY=" + bounds[1] + ", maxX=" + bounds[2] + ", maxY=" + bounds[3]);
            float scaleX = getWidth() / (bounds[2] - bounds[0]);
            float scaleY = getHeight() / (bounds[3] - bounds[1]);
            float scale = Math.min(scaleX, scaleY);
            Log.d("PointCloudView", "Scale: " + scale);

            canvas.save();
            canvas.scale(scale, scale);
            canvas.translate(-bounds[0], -bounds[1]);

            for (int i = 0; i < pointClouds.size(); i++) {
                PointCloud pointCloud = pointClouds.get(i);
                paint.setColor(colors[i % colors.length]); // Set color for each PointCloud
                for (Point point : pointCloud.getPoints()) {
                    canvas.drawPoint(point.getX(), point.getY(), paint);
                }
            }

            canvas.restore();
        }
    }

    private float[] calculateBounds() {
        float minX = Float.POSITIVE_INFINITY, minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY;
        for (PointCloud pointCloud : pointClouds) {
            for (Point point : pointCloud.getPoints()) {
                minX = Math.min(minX, point.getX());
                minY = Math.min(minY, point.getY());
                maxX = Math.max(maxX, point.getX());
                maxY = Math.max(maxY, point.getY());
            }
        }
        return new float[]{minX, minY, maxX, maxY};
    }
}
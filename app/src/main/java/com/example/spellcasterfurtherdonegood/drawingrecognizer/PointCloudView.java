package com.example.spellcasterfurtherdonegood.drawingrecognizer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class PointCloudView extends View {
    private Paint paint;
    private List<Point> currentStroke;
    private List<List<Point>> strokes;
    private int[] colors = {0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFFFFFF00, 0xFFFF00FF, 0xFF00FFFF}; // Red, Green, Blue, Yellow, Magenta, Cyan
    public List<PointCloud> pointClouds;

    public PointCloudView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(0xFF000000); // Black color for strokes
        currentStroke = new ArrayList<>();
        strokes = new ArrayList<>();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //Log.d("PointCloudView", "onDraw");
        paint.setStrokeWidth(10f); // Adjust stroke width for better visibility

        // Draw all strokes
        for (List<Point> stroke : strokes) {
            drawStroke(canvas, stroke);
        }

        // Draw current stroke
        drawStroke(canvas, currentStroke);

        DrawPointClouds(canvas, pointClouds);
    }
    protected void DrawPointClouds(Canvas canvas, List<PointCloud> pointClouds) {
        float margin = 20 * getResources().getDisplayMetrics().density; // Convert 20dp to pixels
        paint.setStrokeWidth(0.01f); // Adjust stroke width for better visibility

        if (pointClouds != null) {
            float[] bounds = calculateBounds(pointClouds);
            float scaleX = (getWidth() - 2 * margin) / (bounds[2] - bounds[0]);
            float scaleY = (getHeight() - 2 * margin) / (bounds[3] - bounds[1]);
            float scale = Math.min(scaleX, scaleY);

            float offsetX = (getWidth() - (bounds[2] - bounds[0]) * scale) / 2;
            float offsetY = (getHeight() - (bounds[3] - bounds[1]) * scale) / 2;

            canvas.save();
            canvas.translate(offsetX, offsetY);
            canvas.scale(scale, scale);
            canvas.translate(-bounds[0], -bounds[1]);

            for (int i = 0; i < pointClouds.size(); i++) {
                PointCloud pointCloud = pointClouds.get(i);
                paint.setColor(colors[i % colors.length]); // Set color for each PointCloud
                var stroke = pointCloud.getPoints();
                for (int j = 1; j < stroke.size(); j++) {
                    Point p1 = stroke.get(j - 1);
                    Point p2 = stroke.get(j);
                    canvas.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY(), paint);
                }
            }

            canvas.restore();
        }
    }

    private float[] calculateBounds(List<PointCloud> pointClouds) {
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


    private void drawStroke(Canvas canvas, List<Point> stroke) {
        for (int i = 1; i < stroke.size(); i++) {
            Point p1 = stroke.get(i - 1);
            Point p2 = stroke.get(i);
            canvas.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY(), paint);
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentStroke.clear();
                currentStroke.add(new Point(x, y, 0));
                break;
            case MotionEvent.ACTION_MOVE:
                currentStroke.add(new Point(x, y, 0));
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                currentStroke.add(new Point(x, y, 0));
                strokes.add(new ArrayList<>(currentStroke));
                if (onStrokeCompleteListener != null) {
                    onStrokeCompleteListener.onStrokeComplete(new ArrayList<>(currentStroke));
                }
                currentStroke.clear();
                invalidate();
                break;
        }
        return true;
    }

    public void clear() {
        strokes.clear();
        invalidate();
    }

    private OnStrokeCompleteListener onStrokeCompleteListener;

    public void setOnStrokeCompleteListener(OnStrokeCompleteListener listener) {
        this.onStrokeCompleteListener = listener;
    }

    public interface OnStrokeCompleteListener {
        void onStrokeComplete(List<Point> stroke);
    }
}
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

    public PointCloudView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setStrokeWidth(10f); // Adjust stroke width for better visibility
        paint.setColor(0xFF000000); // Black color for strokes
        currentStroke = new ArrayList<>();
        strokes = new ArrayList<>();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d("PointCloudView", "onDraw");

        // Draw all strokes
        for (List<Point> stroke : strokes) {
            drawStroke(canvas, stroke);
        }

        // Draw current stroke
        drawStroke(canvas, currentStroke);
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
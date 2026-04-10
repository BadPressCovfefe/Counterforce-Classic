package com.apps.fast.launch.launchviews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import java.util.List;

public class HideOverlayView extends LinearLayout {
    private Bitmap windowFrame;
    private List<Float> radii;
    private List<Point> points;

    public HideOverlayView(Context context) {
        super(context);
    }

    public HideOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        createWindowFrame();
        canvas.drawBitmap(windowFrame, 0, 0, null);
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public boolean isClickable() {
        return false;
    }

    public void reDraw(List<Point> points, List<Float> radii)
    {
        this.points = points;
        this.radii = radii;

        invalidate();
    }

    protected void createWindowFrame() {
        windowFrame = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas osCanvas = new Canvas(windowFrame);

        RectF outerRectangle = new RectF(0, 0, getWidth(), getHeight());
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        osCanvas.drawRect(outerRectangle, paint);

        if (radii != null && points != null) {
            for (Point point : points) {
                paint.setColor(Color.TRANSPARENT);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
                paint.setStyle(Paint.Style.FILL);
                osCanvas.drawCircle(point.x, point.y, radii.get(points.indexOf(point)), paint);
            }
        }
    }

    @Override
    public boolean isInEditMode() {
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        windowFrame = null;
    }
}
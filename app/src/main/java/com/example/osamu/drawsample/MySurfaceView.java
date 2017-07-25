package com.example.osamu.drawsample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder holder;
    private Paint paint;
    private Paint paintScreen;
    private Path path;

    // For saving bitmaps
    private Canvas myCanvas;
    private Canvas canvasScreen;
    private Bitmap bmp;
    private Bitmap bmpScreen;

    public MySurfaceView(Context context) {
        super(context);

        holder = this.getHolder();
        holder.addCallback(this);

        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true);

        initPaint();
    }

    private void initPaint() {

        path = new Path();
        paint = new Paint();
        paintScreen = new Paint();

        paint.setColor(0xFFFFFFFF);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(1);

        paintScreen.setColor(0xFF00FF00);
        paintScreen.setStyle(Paint.Style.STROKE);
        paintScreen.setStrokeJoin(Paint.Join.ROUND);
        paintScreen.setStrokeCap(Paint.Cap.ROUND);
        paintScreen.setStrokeWidth(10);

    }

    private void initCanvas() {

        Canvas canvas = holder.lockCanvas();

        // Create new bitmap and canvas for saving
        int w = getWidth();
        int h = getHeight();
        bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bmpScreen = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        myCanvas = new Canvas(bmp);
        canvasScreen = new Canvas(bmpScreen);

        myCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvasScreen.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawBitmap(bmpScreen, 0, 0, null);

        holder.unlockCanvasAndPost(canvas);

    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        initCanvas();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    private void doDraw() {

        Canvas canvas = holder.lockCanvas();

        myCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        myCanvas.drawPath(path, paint);

        canvasScreen.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvasScreen.drawPath(path, paintScreen);

        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawBitmap(bmpScreen, 0, 0, null);

        holder.unlockCanvasAndPost(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();

        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                path.moveTo(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                path.lineTo(x, y);
                invalidate();
                break;
            default:
                return true;
        }

        doDraw();
        return true;
    }

    public Bitmap getBitmap() {
        return bmp;
    }

    public Bitmap getBmpScreen() { return bmpScreen; }

    public void clearScreen() {

        initCanvas();
        initPaint();

    }
}

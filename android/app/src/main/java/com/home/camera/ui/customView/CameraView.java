package com.home.camera.ui.customView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraView extends SurfaceView {
    private static final String TAG = "CameraView";

    private SurfaceHolder surfaceHolder;

    public CameraView(Context context) {
        super(context);
        init();
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        surfaceHolder = getHolder();
    }

    public void renderData(byte[] imageArray) {
        Bitmap decodeBitmap = BitmapFactory.decodeByteArray(imageArray, 0, imageArray.length);
        /*int width = getWidth();
        int height = getHeight();*/
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap bitmap = Bitmap.createBitmap(decodeBitmap, 0, 0, decodeBitmap.getWidth(), decodeBitmap.getHeight(), matrix, true);
        //Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
        Canvas canvas = surfaceHolder.lockCanvas();
        if (canvas != null) {
            canvas.drawBitmap(bitmap, 0, 0, null);
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

}



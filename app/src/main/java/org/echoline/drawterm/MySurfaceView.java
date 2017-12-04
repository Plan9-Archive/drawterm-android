package org.echoline.drawterm;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.CheckBox;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Created by eli on 12/3/17.
 */
public class MySurfaceView extends SurfaceView {
    private Bitmap bmp;
    private int screenWidth, screenHeight;
    private MainActivity mainActivity;

    public MySurfaceView(Context context, int w, int h) {
        super(context);
        screenHeight = h;
        screenWidth = w;
        mainActivity = (MainActivity)context;
        mainActivity.setWidth(screenWidth);
        mainActivity.setHeight(screenHeight);
        setWillNotDraw(false);

        setOnTouchListener(new View.OnTouchListener() {
            private int[] mouse = new int[3];

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                CheckBox left = (CheckBox)mainActivity.findViewById(R.id.mouseLeft);
                CheckBox middle = (CheckBox)mainActivity.findViewById(R.id.mouseMiddle);
                CheckBox right = (CheckBox)mainActivity.findViewById(R.id.mouseRight);
                int buttons = (left.isChecked()? 1: 0) |
                                (middle.isChecked()? 2: 0) |
                                (right.isChecked()? 4: 0);
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mouse[0] = Math.round(event.getX());
                    mouse[1] = Math.round(event.getY());
                    mouse[2] = buttons;
                    mainActivity.setMouse(mouse);
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    mouse[0] = Math.round(event.getX());
                    mouse[1] = Math.round(event.getY());
                    mouse[2] = buttons;
                    mainActivity.setMouse(mouse);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    mouse[0] = Math.round(event.getX());
                    mouse[1] = Math.round(event.getY());
                    mouse[2] = 0;
                    mainActivity.setMouse(mouse);
                }
                return true;
            }
        });
        bmp = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        new Thread(new Runnable() {
            private long last = 0;
            private long ms = 15;

            @Override
            public void run() {
                while (true) {
                    try {
                        if ((SystemClock.currentThreadTimeMillis() - last) > ms) {
                            mainActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    MySurfaceView.this.invalidate();
                                }
                            });
                            last = SystemClock.currentThreadTimeMillis();
                        }
                        Thread.sleep(1);
                    } catch (Exception e) {
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        IntBuffer intBuffer = ByteBuffer.wrap(mainActivity.getScreenData()).asIntBuffer();
        int []ints = new int[intBuffer.remaining()];
        intBuffer.get(ints);
        bmp.setPixels(ints, 0, screenWidth, 0, 0, screenWidth, screenHeight);
        canvas.drawBitmap(bmp, 0, 0, new Paint());
    }
}
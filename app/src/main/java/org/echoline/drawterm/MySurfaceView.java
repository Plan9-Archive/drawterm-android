package org.echoline.drawterm;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
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
    private float ws, hs;
    private Paint paint = new Paint();

    public MySurfaceView(Context context, int w, int h, float ws, float hs) {
        super(context);
        screenHeight = h;
        screenWidth = w;
        this.ws = ws;
        this.hs = hs;
        mainActivity = (MainActivity)context;
        mainActivity.setWidth(screenWidth);
        mainActivity.setHeight(screenHeight);
        mainActivity.setWidthScale(ws);
        mainActivity.setHeightScale(hs);
        setWillNotDraw(false);

        setOnTouchListener(new View.OnTouchListener() {
            private int[] mouse = new int[3];

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                CheckBox left = (CheckBox)mainActivity.findViewById(R.id.mouseLeft);
                CheckBox middle = (CheckBox)mainActivity.findViewById(R.id.mouseMiddle);
                CheckBox right = (CheckBox)mainActivity.findViewById(R.id.mouseRight);
                CheckBox up = (CheckBox)mainActivity.findViewById(R.id.mouseUp);
                CheckBox down = (CheckBox)mainActivity.findViewById(R.id.mouseDown);
                int buttons = (left.isChecked()? 1: 0) |
                                (middle.isChecked()? 2: 0) |
                                (right.isChecked()? 4: 0) |
                                (up.isChecked()? 8: 0) |
                                (down.isChecked()? 16: 0);
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
            private long lastcb = 0;
            private long ms = 15;

            @Override
            public void run() {
                try {
                    while (true) {
                        try {
                            if ((SystemClock.currentThreadTimeMillis() - last) > ms) {
                                mainActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        MySurfaceView.this.invalidate();
                                        if ((SystemClock.currentThreadTimeMillis() - lastcb) > 1000) {
                                            String s = new String(mainActivity.getSnarf());
                                            ClipboardManager cm = (ClipboardManager)mainActivity.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                            if (cm != null) {
                                                String t = cm.getPrimaryClip().getItemAt(0).coerceToText(mainActivity.getApplicationContext()).toString();
                                                if (!t.equals(s)) {
                                                    ClipData cd = ClipData.newPlainText(null, s);
                                                    cm.setPrimaryClip(cd);
                                                }
                                            }
                                            lastcb = SystemClock.currentThreadTimeMillis();
                                        }
                                    }
                                });
                                last = SystemClock.currentThreadTimeMillis();
                            }
                            Thread.sleep(1);
                        } catch (Exception e) {
                            throw e;
                        }
                    }
                } catch (Exception e) {
                }
            }
        }).start();
        ClipboardManager cm = (ClipboardManager)mainActivity.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null)
            cm.addPrimaryClipChangedListener(new Listener());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        IntBuffer intBuffer = ByteBuffer.wrap(mainActivity.getScreenData()).asIntBuffer();
        int []ints = new int[intBuffer.remaining()];
        intBuffer.get(ints);
        bmp.setPixels(ints, 0, screenWidth, 0, 0, screenWidth, screenHeight);
        canvas.save();
        canvas.scale(ws, hs);
        canvas.drawBitmap(bmp, 0, 0, paint);
        canvas.restore();
    }

    protected class Listener implements ClipboardManager.OnPrimaryClipChangedListener {
        public void onPrimaryClipChanged() {
            ClipboardManager cm = (ClipboardManager)mainActivity.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null)
                mainActivity.setSnarf((String)(cm.getPrimaryClip().getItemAt(0).coerceToText(mainActivity.getApplicationContext()).toString()));
        }
    }
}
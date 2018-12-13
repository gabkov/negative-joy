package com.gabor.negtivejoy;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.text.TextPaint;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Random;


public class MainActivity extends Activity implements SensorEventListener, View.OnTouchListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastUpdate;

    private float xmax, ymax;

    private final int IMAGE_SIZE = 780;

    private float x = 200;
    private float y;

    private boolean topIsVisible = true;

    ImageView bottleCapImageView;
    TextView bottleTextView;

    Random rand = new Random();

    private String bottleText;

    private ViewGroup mRrootLayout;
    private int _xDelta;
    private int _yDelta;

    private boolean inMotion;

    private float xCord;
    private float yCord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        lastUpdate = System.currentTimeMillis();

        setContentView(R.layout.activity_main);

        bottleCapImageView = findViewById(R.id.bottelcap_image);
        bottleTextView = findViewById(R.id.textForCap);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        xmax = size.x - IMAGE_SIZE;
        ymax = size.y - IMAGE_SIZE;

        mRrootLayout = (ViewGroup) findViewById(R.id.root);

        bottleCapImageView.setOnTouchListener(this);

    }


    public boolean onTouch(View view, MotionEvent event) {
        final int X = (int) event.getRawX();
        final int Y = (int) event.getRawY();

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                inMotion = true;
                RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                _xDelta = X - lParams.leftMargin;
                _yDelta = Y - lParams.topMargin;
                xCord = view.getX();
                yCord = view.getY();

                break;
            case MotionEvent.ACTION_UP:
                x = view.getX();
                y = view.getY();

                if(Math.abs(xCord - x) < 10 && Math.abs(yCord - y) < 10){
                    doFlip(view);
                }
                inMotion = false;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                inMotion = true;
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view
                        .getLayoutParams();
                layoutParams.leftMargin = X - _xDelta;
                layoutParams.topMargin = Y - _yDelta;
                layoutParams.rightMargin = -250;
                layoutParams.bottomMargin = -250;
                view.setLayoutParams(layoutParams);
                break;
        }
        mRrootLayout.invalidate();
        return true;
    }

    private void doFlip(View view){
        if(topIsVisible){
            topIsVisible = false;

            bottleCapImageView.setImageResource(R.drawable.craftdown);

            bottleText = getRandomtext();

            bottleTextView.setVisibility(view.VISIBLE);
            bottleTextView.setText(bottleText);
        } else {
            topIsVisible = true;
            bottleCapImageView.setImageResource(R.drawable.bottlecap);
            bottleTextView.setVisibility(view.INVISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && !inMotion) {

            x -= event.values[0] * 2;
            y += event.values[1] * 2;

            if (x > xmax) {
                x = xmax;
            } else if (x < 0) {
                x = 0;
            }
            if (y > ymax) {
                y = ymax;
            } else if (y < 0) {
                y = 0;
            }

            bottleCapImageView.setX(x);
            bottleCapImageView.setY(y);

            bottleTextView.setX(x);
            bottleTextView.setY(y);
        }
    }

    private String getRandomtext(){
        String[] texts = {"You're adopted!", "LOOSER", "Suck a duck", "Works with long\n sentences as well", "DAMN"};
        String result = texts[rand.nextInt(texts.length)];
        return result;
    }

}

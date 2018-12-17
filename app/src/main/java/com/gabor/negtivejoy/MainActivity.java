package com.gabor.negtivejoy;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

    private final int IMAGE_SIZE = 780;
    private final int TILT_NITRO = 2;

    private float x = 200;
    private float y;
    private float xMax, yMax;
    private int xDelta;
    private int yDelta;
    private float dragStartXCoordinate;
    private float dragStartYCoordinate;

    private ImageView bottleCapImageView;
    private TextView bottleTextView;
    private ViewGroup mRrootLayout;

    private boolean topIsVisible = true;
    private boolean inMotion;

    private Random rand = new Random();

    private String bottleText;
    private final String[] texts = {"You're adopted!", "LOOSER", "Suck a duck",
            "Works with long\n sentences as well", "DAMN",
            "HA HA HA\nNO", "iOS...\nLOL", "BUS SNAKE"};

    float dX, dY;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        setContentView(R.layout.activity_main);

        bottleCapImageView = findViewById(R.id.bottelcap_image);
        bottleTextView = findViewById(R.id.textForCap);

        setScreenSizeMax();

        mRrootLayout = findViewById(R.id.root);

        bottleCapImageView.setOnTouchListener(this);
    }

    public boolean onTouch(View view, MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                inMotion = true;

                dX = view.getX() - event.getRawX();
                dY = view.getY() - event.getRawY();
                dragStartXCoordinate = view.getX();
                dragStartYCoordinate = view.getY();
                break;
            case MotionEvent.ACTION_UP:
                x = view.getX();
                y = view.getY();

                if(Math.abs(dragStartXCoordinate - x) < 10 && Math.abs(dragStartYCoordinate - y) < 10){
                    doFlip();
                }
                inMotion = false;
                break;
            case MotionEvent.ACTION_MOVE:
                inMotion = true;

                view.animate()
                        .x(event.getRawX() + dX)
                        .y(event.getRawY() + dY)
                        .setDuration(0)
                        .start();
                bottleTextView.setX(view.getX());
                bottleTextView.setY(view.getY());
                break;
        }
        return true;
    }

    private void setScreenSizeMax() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        xMax = size.x - IMAGE_SIZE;
        yMax = size.y - IMAGE_SIZE;
    }

    private void doFlip() {
        if (topIsVisible) {
            topIsVisible = false;
            bottleCapImageView.setImageResource(R.drawable.craftdown);
            bottleText = getRandomText();
            bottleTextView.setVisibility(View.VISIBLE);
            bottleTextView.setText(bottleText);
        } else {
            topIsVisible = true;
            bottleCapImageView.setImageResource(R.drawable.bottlecap);
            bottleTextView.setVisibility(View.INVISIBLE);
        }
    }

    private String getRandomText() {
        String result = texts[rand.nextInt(texts.length)];
        return result;
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

            x -= event.values[0] * TILT_NITRO;
            y += event.values[1] * TILT_NITRO;

            if (x > xMax) {
                x = xMax;
            } else if (x < 0) {
                x = 0;
            }
            if (y > yMax) {
                y = yMax;
            } else if (y < 0) {
                y = 0;
            }

            bottleCapImageView.setX(x);
            bottleCapImageView.setY(y);

            bottleTextView.setX(x);
            bottleTextView.setY(y);
        }
    }
}

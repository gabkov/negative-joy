package com.gabor.negtivejoy;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;


public class MainActivity extends Activity implements SensorEventListener, View.OnTouchListener, BottleTopVisibility {
    private SensorManager sensorManager;
    private Sensor accelerometer;

    private final int IMAGE_SIZE = 655;
    private final int TILT_NITRO = 2;

    private float x = 200;
    private float y;
    private float xMax, yMax;
    private float dX, dY;
    private float dragStartXCoordinate;
    private float dragStartYCoordinate;

    private ImageView bottleCapImageView;
    private TextView bottleTextView;

    private boolean topIsVisible = true;
    private boolean inMotion;

    private Random rand = new Random();

    /*private String bottleText;
    private final String[] texts = {"You're adopted!", "LOOSER", "Suck a duck",
            "Works with long\n sentences as well", "DAMN",
            "HA HA HA\nNO", "iOS...\nLOL", "BUS SNAKE", "You smell like crap",
            "BITCOIN", "HODL"};*/


    private Visuals visuals;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        setContentView(R.layout.activity_main);

        bottleCapImageView = findViewById(R.id.bottelcap_image);
        bottleTextView = findViewById(R.id.textForCap);

        setScreenSizeMax();

        visuals = new Visuals(bottleCapImageView, bottleTextView);

        bottleCapImageView.setOnTouchListener(this);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
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
                if (Math.abs(dragStartXCoordinate - x) < 10 && Math.abs(dragStartYCoordinate - y) < 10) {
                    visuals.doFlip(this);
                }
                changeSmileIfNeeded(view, R.drawable.bottlecap);
                inMotion = false;
                break;
            case MotionEvent.ACTION_MOVE:
                inMotion = true;
                float xAnimate = event.getRawX() + dX;
                float yAnimate = event.getRawY() + dY;

                if (xAnimate > xMax) {
                    xAnimate = xMax;
                } else if (xAnimate < 0) {
                    xAnimate = 0;
                }
                if (yAnimate > yMax) {
                    yAnimate = yMax;
                } else if (yAnimate < 0) {
                    yAnimate = 0;
                }
                view.animate().x(xAnimate);
                view.animate().y(yAnimate);
                view.animate().setDuration(0).start();
                bottleTextView.setX(view.getX());
                bottleTextView.setY(view.getY());
                changeSmileIfNeeded(view, R.drawable.bottlecapdragged);
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

    private void changeSmileIfNeeded(View view, Integer drawableId) {
        if (topIsVisible && Math.abs(dragStartXCoordinate - view.getX()) > 5 && Math.abs(dragStartYCoordinate - view.getY()) > 5) {
            bottleCapImageView.setImageResource(drawableId);
        }
    }

    /*private String getRandomText() {
        String result = texts[rand.nextInt(texts.length)];
        return result;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void doFlip() {
        if (topIsVisible) {
            topIsVisible = false;
            flipAnimation(R.drawable.craftdown);
            bottleText = getRandomText();
            bottleTextView.setText(bottleText);
        } else {
            topIsVisible = true;
            flipAnimation(R.drawable.bottlecap);
            bottleTextView.setVisibility(View.INVISIBLE);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void flipAnimation(final Integer image) {
        bottleCapImageView.animate().withLayer()
                .rotationY(90)
                .setDuration(200)
                .withEndAction(
                        new Runnable() {
                            @Override
                            public void run() {
                                // second quarter turn
                                bottleCapImageView.setImageResource(image);
                                bottleCapImageView.setRotationY(-90);
                                bottleCapImageView.animate().withLayer()
                                        .rotationY(0)
                                        .setDuration(200)
                                        .withEndAction(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (!topIsVisible) {
                                                    bottleTextView.setVisibility(View.VISIBLE);
                                                }
                                            }
                                        })
                                        .start();
                            }
                        }
                ).start();
    }*/

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
    public void changeBottleTopVisibility() {
        if (topIsVisible){
            topIsVisible = false;
        } else {
            topIsVisible = true;
        }
    }

    @Override
    public boolean getBottleTopVisibility() {
        return topIsVisible;
    }
}

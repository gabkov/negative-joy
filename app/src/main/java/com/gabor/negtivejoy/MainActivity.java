package com.gabor.negtivejoy;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import android.content.*;
import android.graphics.*;
import android.widget.*;
import android.provider.*;

import com.gabor.negtivejoy.Interfaces.BitcoinProgressDialog;
import com.gabor.negtivejoy.Interfaces.DetectionProgressDialogHandler;
import com.gabor.negtivejoy.Interfaces.Toaster;


public class MainActivity extends Activity implements SensorEventListener, View.OnTouchListener, Toaster, DetectionProgressDialogHandler, BitcoinProgressDialog {
    private SensorManager sensorManager;
    private Sensor accelerometer;

    private final int IMAGE_SIZE = 420;
    private final int TILT_NITRO = 2;

    private float x = 200;
    private float y;
    private float xMax, yMax;
    private float dX, dY;
    private float dragStartXCoordinate;
    private float dragStartYCoordinate;

    private BottleCup bottleCup;
    private Visuals visuals;
    private EmotionHandler emotionHandler;
    private Bitcoin bitcoin;
    private NotificationHandler notificationHandler;

    private final int PICK_IMAGE = 1;

    private ProgressDialog detectionProgressDialog;
    private ProgressDialog bitcoinProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        setContentView(R.layout.activity_main);
        setScreenSizeMax();

        ImageView bottleCapImageView = findViewById(R.id.bottelcap_image);
        bottleCapImageView.setOnTouchListener(this);
        TextView bottleTextView = findViewById(R.id.textForCap);

        bottleCup = new BottleCup(bottleCapImageView, bottleTextView);
        visuals = new Visuals(bottleCup);
        emotionHandler = new EmotionHandler(bottleCapImageView, this, this);
        notificationHandler = new NotificationHandler(this);
        bitcoin = new Bitcoin(this, bottleCup, this, notificationHandler, this);

        // For emotion detection
        detectionProgressDialog = new ProgressDialog(this);
        detectionProgressDialog.setMessage("Analysing...");

        // For the Bitcoin price
        bitcoinProgressDialog = new ProgressDialog(this);
        bitcoinProgressDialog.setTitle("BPI Loading");
        bitcoinProgressDialog.setMessage("Wait ...");

        // For the image capture
        Button emotionDetectionButton = findViewById(R.id.button1);
        emotionDetectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, PICK_IMAGE);
                }
            }
        });

        ImageView bitcoinImage = (ImageView) findViewById(R.id.bitcoin);
        bitcoinImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bitcoin.load();
            }
        });

        // Creates the notification channel for the app
        notificationHandler.createNotificationChannel();

    }

    private void setScreenSizeMax() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        xMax = size.x - IMAGE_SIZE;
        yMax = size.y - IMAGE_SIZE;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {

            Bundle extras = data.getExtras();
            Bitmap bitmap = (Bitmap) extras.get("data");

            emotionHandler.detectAndFrame(bitmap);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public boolean onTouch(View view, MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                bottleCup.setInMotionToTrue();
                dX = view.getX() - event.getRawX();
                dY = view.getY() - event.getRawY();
                dragStartXCoordinate = view.getX();
                dragStartYCoordinate = view.getY();
                break;
            case MotionEvent.ACTION_UP:
                x = view.getX();
                y = view.getY();
                if (Math.abs(dragStartXCoordinate - x) < 10 && Math.abs(dragStartYCoordinate - y) < 10) {
                    visuals.doFlip();
                }
                visuals.changeSmileIfNeeded(view, R.drawable.bottlecap, dragStartXCoordinate, dragStartYCoordinate);
                bottleCup.setInMotionToFalse();
                break;
            case MotionEvent.ACTION_MOVE:
                bottleCup.setInMotionToTrue();
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

                bottleCup.changeBottleTextPosition(view.getX(), view.getY());

                visuals.changeSmileIfNeeded(view, R.drawable.bottlecapdragged, dragStartXCoordinate, dragStartYCoordinate);
                break;
        }
        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && !bottleCup.isInMotion()) {

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

            bottleCup.changeBottleCupPosition(x, y);
            bottleCup.changeBottleTextPosition(x, y);
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
    public void displayToast(String message){
        Toast toast= Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 200);
        toast.show();
    }

    @Override
    public void showDetectionDialog() {
        detectionProgressDialog.show();
    }

    @Override
    public void dismissDetectionDialog() {
        detectionProgressDialog.dismiss();
    }

    @Override
    public void showBitcoinProgressDialog() {
        bitcoinProgressDialog.show();
    }

    @Override
    public void dismissBitcoinProgressDialog() {
        bitcoinProgressDialog.dismiss();
    }
}

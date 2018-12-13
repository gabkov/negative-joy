package com.gabor.negtivejoy;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.text.TextPaint;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;


public class MainActivity extends Activity implements SensorEventListener {
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

        bottleCapImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(topIsVisible){
                    topIsVisible = false;
                    bottleCapImageView.setImageResource(R.drawable.craftdown);
                    bottleText = getRandomtext();
                    bottleTextView.setVisibility(v.VISIBLE);
                } else {
                    topIsVisible = true;
                    bottleCapImageView.setImageResource(R.drawable.bottlecap);
                    bottleTextView.setVisibility(v.INVISIBLE);
                }
            }
        });
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
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

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
        String[] texts = {"LOOL EZ\nVICCES", "TE BÉNA", "NANA\nANA\nNA"};
        String result = texts[rand.nextInt(texts.length)];
        return result;
    }

}

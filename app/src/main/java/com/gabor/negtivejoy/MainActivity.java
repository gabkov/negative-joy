package com.gabor.negtivejoy;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.content.Context;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.widget.AppCompatImageView;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;

import java.util.Random;


public class MainActivity extends Activity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastUpdate;

    private Bitmap bottlecap;
    private Bitmap bottlecapDown;

    AnimatedView animatedView = null;

    private float xmax, ymax;

    private final int IMAGE_SIZE = 700;

    private float x = 200;
    private float y;

    private Bitmap toView;

    private boolean topIsVisible = true;

    ImageView bottleCapImageView;

    private TextPaint mTextPaint=new TextPaint();

    Random rand = new Random();

    private String bottleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        lastUpdate = System.currentTimeMillis();

        animatedView = new AnimatedView(this);

        animatedView.findViewById(R.id.bottelcap_image);

        setContentView(R.layout.activity_main);

        bottleCapImageView = findViewById(R.id.bottelcap_image);
        System.out.println(bottleCapImageView);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        xmax = size.x - IMAGE_SIZE;
        ymax = size.y - IMAGE_SIZE;

        animatedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(topIsVisible){
                    topIsVisible = false;
                    bottleText = getRandomtext();
                } else {
                    topIsVisible = true;
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

        }
    }

    public class AnimatedView extends AppCompatImageView {

        public AnimatedView(Context context) {
            super(context);
            // TODO Auto-generated constructor stub

            Bitmap cup = BitmapFactory.decodeResource(getResources(), R.drawable.bottlecap);
            Bitmap cupDown = BitmapFactory.decodeResource(getResources(), R.drawable.craftdown);
            final int dstWidth = IMAGE_SIZE;
            final int dstHeight = IMAGE_SIZE;
            bottlecap = Bitmap.createScaledBitmap(cup, dstWidth, dstHeight, true);
            bottlecapDown = Bitmap.createScaledBitmap(cupDown, dstWidth, dstHeight, true);
        }

        @Override
        protected void onDraw(Canvas canvas) {

            if (topIsVisible){
                toView = bottlecap;
                canvas.drawBitmap(toView, x, y, null);
            }else {
                toView = bottlecapDown;
                canvas.drawBitmap(toView, x, y, null);

                mTextPaint.setColor(Color.WHITE);
                mTextPaint.setTextSize(100);
                StaticLayout mTextLayout = new StaticLayout(bottleText, mTextPaint, canvas.getWidth(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                canvas.save();

                int textNewLines = bottleText.split("\n").length;

                canvas.translate(x+200, y+200);

                mTextLayout.draw(canvas);
                canvas.restore();
            }

            invalidate();
        }
    }

    private String getRandomtext(){
        String[] texts = {"LOOL EZ\nVICCES", "TE BÉNA", "NANA\nANA\nNA"};
        String result = texts[rand.nextInt(texts.length)];
        return result;
    }

}

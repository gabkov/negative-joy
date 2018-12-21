package com.gabor.negtivejoy;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.app.NotificationCompat;
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

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends Activity implements SensorEventListener, View.OnTouchListener, Toaster, DetectionProgressDialogHandler {
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

    private BottleCup bottleCup;
    private Visuals visuals;
    private EmotionHandler emotionHandler;
    private ProgressDialog detectionProgressDialog;

    private final int PICK_IMAGE = 1;


    public static final String BPI_ENDPOINT = "https://api.coindesk.com/v1/bpi/currentprice.json";
    private OkHttpClient okHttpClient = new OkHttpClient();
    private ProgressDialog progressDialog;
    private ImageView bitcoin;

    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";
    private NotificationManager mNotifyManager;
    private static final int NOTIFICATION_ID = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        setContentView(R.layout.activity_main);
        setScreenSizeMax();

        ImageView bottleCapImageView = findViewById(R.id.bottelcap_image);
        TextView bottleTextView = findViewById(R.id.textForCap);

        bottleCup = new BottleCup(bottleCapImageView, bottleTextView);

        visuals = new Visuals(bottleCup);
        emotionHandler = new EmotionHandler(bottleCapImageView, this, this);
        detectionProgressDialog = new ProgressDialog(this);

        bottleCapImageView.setOnTouchListener(this);

        // For the image capture
        Button button1 = findViewById(R.id.button1);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, PICK_IMAGE);
                }
            }
        });

        // For the Bitcoin price
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("BPI Loading");
        progressDialog.setMessage("Wait ...");

        bitcoin = (ImageView) findViewById(R.id.bitcoin);

        bitcoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                load();
            }
        });

        // Creates the notification channel for the app
        createNotificationChannel();

    }

    public void sendNotification(String price){
        NotificationCompat.Builder notifyBuilder = getNotificationBuilder();
        notifyBuilder.setContentText(price);
        mNotifyManager.notify(NOTIFICATION_ID, notifyBuilder.build());
    }

    public void createNotificationChannel() {
        mNotifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.O) {
            // Create a NotificationChannel
            NotificationChannel notificationChannel = new NotificationChannel(PRIMARY_CHANNEL_ID, "Mascot Notification", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("Notification from Mascot");
            mNotifyManager.createNotificationChannel(notificationChannel);
        }
    }

    private NotificationCompat.Builder getNotificationBuilder(){
        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID)
                .setContentTitle("Bitcoin price:")
                .setContentText("This is your notification text.")
                .setSmallIcon(R.drawable.ic_android);
        return notifyBuilder;
    }


    private void load() {
        Request request = new Request.Builder()
                .url(BPI_ENDPOINT)
                .build();

        progressDialog.show();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                /*Toast.makeText(MainActivity.this, "Error during BPI loading : "
                        + e.getMessage(), Toast.LENGTH_SHORT).show();*/
                displayToast("Error during BPI loading : ");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String body = response.body().string();

                //runOnUiThread(new Runnable() {
                  //  @Override
                    //public void run() {
                        progressDialog.dismiss();
                        String price = parseBpiResponse(body);
                        sendNotification(price);
                        bottleCup.setBottleTextInvisible();
                        bottleCup.changeBottleCupImage(R.drawable.btc);
                    }
                //});
            //}
        });

    }

    private String parseBpiResponse(String body) {
        try {
            StringBuilder builder = new StringBuilder();

            JSONObject jsonObject = new JSONObject(body);

            JSONObject bpiObject = jsonObject.getJSONObject("bpi");
            JSONObject usdObject = bpiObject.getJSONObject("USD");
            builder.append(usdObject.getString("rate").substring(0, usdObject.getString("rate").length()-2)).append(" $").append("\n");

            return builder.toString();

        } catch (Exception e) {
            return "No data found, please try again.";
        }
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
    public void setDetectionDialogText(String text) {
        detectionProgressDialog.setMessage(text);
    }

    @Override
    public void dismissDetectionDialog() {
        detectionProgressDialog.dismiss();
    }
}

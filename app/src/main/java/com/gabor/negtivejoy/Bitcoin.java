package com.gabor.negtivejoy;

import android.app.Activity;

import com.gabor.negtivejoy.Interfaces.BitcoinProgressDialog;
import com.gabor.negtivejoy.Interfaces.NotificationSender;
import com.gabor.negtivejoy.Interfaces.Toaster;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Bitcoin {

    private static final String BPI_ENDPOINT = "https://api.coindesk.com/v1/bpi/currentprice.json";
    private OkHttpClient okHttpClient = new OkHttpClient();
    private BottleCup bottleCup;
    private Toaster toaster;
    private NotificationSender notificationSender;
    private BitcoinProgressDialog bitcoinProgressDialog;
    private Activity activity;

    Bitcoin(Activity activity, BottleCup bottleCup, Toaster toaster, NotificationSender notificationSender, BitcoinProgressDialog bitcoinProgressDialog) {
        this.activity = activity;
        this.bottleCup = bottleCup;
        this.toaster = toaster;
        this.notificationSender = notificationSender;
        this.bitcoinProgressDialog = bitcoinProgressDialog;
    }

    public void load() {
        Request request = new Request.Builder()
                .url(BPI_ENDPOINT)
                .build();

        bitcoinProgressDialog.showBitcoinProgressDialog();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                toaster.displayToast("Error during BPI loading : ");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String body = response.body().string();

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bitcoinProgressDialog.dismissBitcoinProgressDialog();
                        String price = parseBpiResponse(body);
                        notificationSender.sendNotification(price);
                        bottleCup.setBottleTextInvisible();
                        bottleCup.changeBottleCupImage(R.drawable.btc);
                    }
                });
            }
        });
    }

    private String parseBpiResponse(String body) {
        try {
            StringBuilder builder = new StringBuilder();

            JSONObject jsonObject = new JSONObject(body);

            JSONObject bpiObject = jsonObject.getJSONObject("bpi");
            JSONObject usdObject = bpiObject.getJSONObject("USD");
            builder.append(usdObject.getString("rate").substring(0, usdObject.getString("rate").length() - 2)).append(" $").append("\n");

            return builder.toString();

        } catch (Exception e) {
            return "No data found, please try again.";
        }
    }
}

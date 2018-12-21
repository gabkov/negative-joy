package com.gabor.negtivejoy;

import android.app.ProgressDialog;
import android.widget.ImageView;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Bitcoin {

    public static final String BPI_ENDPOINT = "https://api.coindesk.com/v1/bpi/currentprice.json";
    private OkHttpClient okHttpClient = new OkHttpClient();
    private ProgressDialog progressDialog;
    private ImageView bitcoin;

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
}

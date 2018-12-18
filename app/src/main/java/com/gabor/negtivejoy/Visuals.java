package com.gabor.negtivejoy;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;

public class Visuals {
    private final String[] texts = {"You're adopted!", "LOOSER", "Suck a duck",
            "Works with long\n sentences as well", "DAMN",
            "HA HA HA\nNO", "iOS...\nLOL", "BUS SNAKE", "You smell like\ncrap",
            "BITCOIN", "HODL"};

    private Random rand = new Random();
    private ImageView bottleCapImageView;
    private TextView bottleTextView;
    private String bottleText;

    public Visuals(ImageView bottleCapImageView, TextView bottleTextView) {
        this.bottleCapImageView = bottleCapImageView;
        this.bottleTextView = bottleTextView;
    }

    private String getRandomText() {
        String result = texts[rand.nextInt(texts.length)];
        return result;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void doFlip(BottleTopVisibility topVisibility) {
        boolean topIsVisible = topVisibility.getBottleTopVisibility();

        if (topIsVisible) {
            topVisibility.changeBottleTopVisibility();
            flipAnimation(R.drawable.craftdown, topIsVisible);
            bottleText = getRandomText();
            bottleTextView.setText(bottleText);
        } else {
            topVisibility.changeBottleTopVisibility();
            flipAnimation(R.drawable.bottlecap, topIsVisible);
            bottleTextView.setVisibility(View.INVISIBLE);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void flipAnimation(final Integer image, final boolean topNotVisible) {
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
                                                if (topNotVisible) {
                                                    bottleTextView.setVisibility(View.VISIBLE);
                                                }
                                            }
                                        })
                                        .start();
                            }
                        }
                ).start();
    }
}

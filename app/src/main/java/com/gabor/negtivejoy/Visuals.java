package com.gabor.negtivejoy;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;

public class Visuals {

    private Random rand = new Random();
    private ImageView bottleCapImageView;
    private TextView bottleTextView;
    private String bottleText;
    private BottleCup bottleCup;

    private final String[] texts = {"You're adopted!", "LOOSER", "Suck a duck",
            "Works with long\n sentences as well", "DAMN",
            "HA HA HA\nNO", "iOS...\nLOL", "BUS SNAKE", "You smell like\ncrap",
            "BITCOIN", "HODL"};


    public Visuals(BottleCup bottleCup) {
        this.bottleCup = bottleCup;
        this.bottleCapImageView = bottleCup.getBottleCapImageView();
        this.bottleTextView = bottleCup.getBottleTextView();
    }

    private String getRandomText() {
        String result = texts[rand.nextInt(texts.length)];
        return result;
    }

    public void changeSmileIfNeeded(View view, Integer drawableId, float dragStartXCoordinate, float dragStartYCoordinate) {
        if (bottleCup.getBottleTopVisibility() && (Math.abs(dragStartXCoordinate - view.getX()) > 10 || Math.abs(dragStartYCoordinate - view.getY()) > 10)) {
            bottleCup.changeBottleCupImage(drawableId);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void doFlip() {

        boolean bottleTopVisibility = bottleCup.getBottleTopVisibility();

        if (bottleTopVisibility) {
            bottleCup.changeBottleTopVisibility();
            flipAnimation(R.drawable.craftdown, bottleTopVisibility);
            bottleText = getRandomText();
            bottleTextView.setText(bottleText);
        } else {
            bottleCup.changeBottleTopVisibility();
            flipAnimation(R.drawable.bottlecap, bottleTopVisibility);
            bottleTextView.setVisibility(View.INVISIBLE);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void flipAnimation(final Integer image, final boolean topNotVisible) {
        bottleCapImageView.animate().withLayer()
                .rotationY(90)
                .setDuration(150)
                .withEndAction(
                        new Runnable() {
                            @Override
                            public void run() {
                                // second quarter turn
                                bottleCapImageView.setImageResource(image);
                                bottleCapImageView.setRotationY(-90);
                                bottleCapImageView.animate().withLayer()
                                        .rotationY(0)
                                        .setDuration(150)
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

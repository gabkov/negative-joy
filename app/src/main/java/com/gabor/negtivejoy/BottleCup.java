package com.gabor.negtivejoy;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class BottleCup {
    private ImageView bottleCapImageView;

    private TextView bottleTextView;

    private boolean topIsVisible = true;
    private boolean inMotion;

    public BottleCup(ImageView bottleCapImageView, TextView bottleTextView) {
        this.bottleCapImageView = bottleCapImageView;
        this.bottleTextView = bottleTextView;
    }

    public TextView getBottleTextView() {
        return bottleTextView;
    }

    public void changeBottleCupImage(Integer imageId) {
        bottleCapImageView.setImageResource(imageId);
    }

    public ImageView getBottleCapImageView() {
        return bottleCapImageView;
    }

    public void changeBottleCupPosition(float x, float y) {
        bottleCapImageView.setX(x);
        bottleCapImageView.setY(y);
    }

    public void changeBottleTextPosition(float x, float y) {
        bottleTextView.setX(x);
        bottleTextView.setY(y);
    }

    public boolean isInMotion() {
        return inMotion;
    }

    public void setInMotionToFalse() {
        inMotion = false;
    }

    public void setInMotionToTrue() {
        inMotion = true;
    }

    public void changeBottleTopVisibility() {
        if (topIsVisible) {
            topIsVisible = false;
        } else {
            topIsVisible = true;
        }
    }

    public boolean getBottleTopVisibility() {
        return topIsVisible;
    }

    public void setBottleTextInvisible(){
        bottleTextView.setVisibility(View.INVISIBLE);
    }
}

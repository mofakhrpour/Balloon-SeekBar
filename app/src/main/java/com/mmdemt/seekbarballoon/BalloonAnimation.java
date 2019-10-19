package com.mmdemt.seekbarballoon;

import android.animation.ValueAnimator;
import android.view.animation.LinearInterpolator;

public class BalloonAnimation {

    private float currentPosition;
    private float targetPosition;
    private ValueAnimator animator;
    private UpdateListener updateListener;

    public BalloonAnimation(float targetPosition, UpdateListener updateListener) {
        this.targetPosition = targetPosition;
        this.currentPosition = targetPosition;
        this.updateListener = updateListener;
        initAnimator();
    }

    private void initAnimator() {

        animator = ValueAnimator.ofFloat(0, 100);
        animator.setDuration(1000);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                int maxNumber = 2;
                int valueToAdd = 12;
                float calcVal = currentPosition - targetPosition;

                if (calcVal > maxNumber){
                    if (calcVal > 170) {
                        valueToAdd = 34;
                    } else if (calcVal < 100) {
                        valueToAdd = 7;
                        if (calcVal < 20) {
                            valueToAdd = 2;
                        }
                    }
                    currentPosition -= valueToAdd;
                    float rotation = ((calcVal > 100 ? 100 : calcVal) / 100) * 26;

                    updateListener.onUpdate(currentPosition, rotation, 1);

                }else if (calcVal < -maxNumber){
                    if (calcVal < -170){
                        valueToAdd = 34;
                    }else if (calcVal > -100) {
                        valueToAdd = 7;
                        if (calcVal > -20) {
                            valueToAdd = 2;
                        }
                    }
                    currentPosition += valueToAdd;
                    float rotation = ((calcVal < -100 ? 100 : Math.abs(calcVal)) / 100) * 26;

                    updateListener.onUpdate(currentPosition, -rotation, -1);
                }else {
                    if (currentPosition != targetPosition){
                        currentPosition = targetPosition;
                        updateListener.onUpdate(currentPosition, 0, 0);
                    }
                }
            }
        });
    }

    public void updateValues(float targetPosition){
        this.targetPosition = targetPosition;
    }

    public void start() {
        animator.start();
    }

    public void stop() {
        animator.cancel();
    }


    public interface UpdateListener {
        void onUpdate(float position, float rotation, int status);
    }
}

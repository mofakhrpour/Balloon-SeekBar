package com.mmdemt.seekbarballoon;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.drawable.Animatable2;
import android.os.Build;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout seekBar;
    private View seekBarThumb;
    private View seekBarProgress;
    private TextView seekBarProgressText;
    private View seekBarBackground;
    private View seekBarIndicator;
    private boolean isTouching;


    private int seekBarThumbWidth = 0;
    private float indicatorTransitionX = 0;
    private BalloonAnimation balloonAnimation;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        seekBar = findViewById(R.id.seekBar);
        seekBarThumb = findViewById(R.id.thumb);
        seekBarProgress = findViewById(R.id.progress);
        seekBarProgressText = findViewById(R.id.progressText);
        seekBarBackground = findViewById(R.id.background);
        seekBarIndicator = findViewById(R.id.indicator);


        balloonAnimation = new BalloonAnimation(indicatorTransitionX, new BalloonAnimation.UpdateListener() {
            @Override
            public void onUpdate(float position, float rotation, int status) {
                seekBarIndicator.setTranslationX(position);
                seekBarIndicator.setRotation(rotation);
            }
        });

        balloonAnimation.start();

        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isTouching = true;
                        ValueAnimator animatorShow = ValueAnimator.ofFloat(0, 100);
                        animatorShow.setDuration(300);
                        animatorShow.setInterpolator(new FastOutSlowInInterpolator());
                        animatorShow.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                float val = (float)valueAnimator.getAnimatedValue();
                                seekBarIndicator.setTranslationY(150 - ((val / 100) * 150));
                                seekBarIndicator.setAlpha((val / 100));
                                seekBarIndicator.setScaleX((val / 100) * 1);
                                seekBarIndicator.setScaleY((val / 100) * 1);
                            }
                        });
                        animatorShow.start();
                        seekBarThumb.setBackground(getResources().getDrawable(R.drawable.thumb_animated_focused));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            ((Animatable2)seekBarThumb.getBackground()).start();
                        }
                        movieSeekBar(motionEvent);
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        movieSeekBar(motionEvent);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        isTouching = false;
                        seekBarThumb.setBackground(getResources().getDrawable(R.drawable.thumb_animated_release));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            ((Animatable2)seekBarThumb.getBackground()).start();
                        }
                        ValueAnimator animatorHide = ValueAnimator.ofFloat(100, 0);
                        animatorHide.setDuration(300);
                        animatorHide.setInterpolator(new FastOutSlowInInterpolator());
                        animatorHide.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                float val = (float)valueAnimator.getAnimatedValue();
                                seekBarIndicator.setTranslationY(-((val / 100) * 150) + 150);
                                seekBarIndicator.setAlpha((val / 100));
                                seekBarIndicator.setScaleX((val / 100) * 1);
                                seekBarIndicator.setScaleY((val / 100) * 1);
                            }
                        });
                        animatorHide.start();
                        break;
                }
                return false;
            }
        });
    }


    private void movieSeekBar(MotionEvent motionEvent) {
        if (seekBarThumbWidth == 0) seekBarThumbWidth = seekBarThumb.getMeasuredWidth();
        float xPos = motionEvent.getX() - (seekBarThumbWidth / 2);

        if (xPos > 0 && xPos < seekBar.getMeasuredWidth() - seekBarThumbWidth)
        {
            seekBarProgressText.setText(Math.round((xPos / (seekBar.getMeasuredWidth()-seekBarThumbWidth)) * 100)+"");

            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)seekBarThumb.getLayoutParams();
            layoutParams.leftMargin = (int)xPos;
            seekBarThumb.setLayoutParams(layoutParams);

            indicatorTransitionX = motionEvent.getX() - (seekBarIndicator.getLayoutParams().width / 2);

            balloonAnimation.updateValues(indicatorTransitionX);
        }
    }
}

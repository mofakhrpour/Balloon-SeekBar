package com.mmdemt.seekbarballoon;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class SeekBarWidget extends View {

    private int containerMarginTop;

    private Paint indicatorImagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap indicatorImage;
    private int indicatorImageWidthDivideTwo;
    private int indicatorImageHeightDivideTwo;
    private Matrix indicatorMatrix = new Matrix();
    private float indicatorPositionX;
    private float indicatorRotation;
    private ValueAnimator indicatorPopUpAnimator;
    private Paint indicatorTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);


    private Paint linePaint = new Paint();

    private Paint thumbFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint thumbStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float currentThumbFocusedWidth = dp2px(18);
    private float thumbFocusedWidth = dp2px(18);
    private float currentThumbFocusedStrokeWidth = dp2px(3);
    private float thumbFocusedStrokeWidth = dp2px(3);

    private float currentThumbReleasedWidth = dp2px(8);
    private float thumbReleasedWidth = dp2px(8);
    private float currentThumbReleasedStrokeWidth = dp2px(6);
    private float thumbReleasedStrokeWidth = dp2px(6);

    private float thumbAnimatedValue = 1;
    private float thumbPosition;
    private boolean thumbIsFocused = false;
    private ValueAnimator thumbFocusAnimator;

    private BalloonAnimation balloonAnimation;


    // Builder

    private int indicatorColor;
    private int indicatorTextColor;
    private int indicatorTextSize;
    private float progress;
    private float progressHeight;
    private int progressColor;
    private int thumbColor;
    private int thumbStrokeColor;



    public SeekBarWidget(Context context) {
        super(context);
        init();
    }

    public SeekBarWidget(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs);
        init();
    }

    public SeekBarWidget(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs);
        init();
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.SeekBarWidget, 0, 0);
        try {
            indicatorColor = ta.getColor(R.styleable.SeekBarWidget_sbw_indicatorColor, getResources().getColor(R.color.colorBlue));
            indicatorTextColor = ta.getColor(R.styleable.SeekBarWidget_sbw_indicatorTextColor, getResources().getColor(android.R.color.white));
            indicatorTextSize = ta.getInteger(R.styleable.SeekBarWidget_sbw_indicatorTextSize, (int)sp2px(11));
            progress = ta.getFloat(R.styleable.SeekBarWidget_sbw_progress, 0);
            progressHeight = ta.getDimension(R.styleable.SeekBarWidget_sbw_progressHeight, dp2px(3));
            progressColor = ta.getColor(R.styleable.SeekBarWidget_sbw_progressColor, getResources().getColor(R.color.colorBlue));
            thumbColor = ta.getColor(R.styleable.SeekBarWidget_sbw_thumbColor, getResources().getColor(android.R.color.white));
            thumbStrokeColor = ta.getColor(R.styleable.SeekBarWidget_sbw_thumbStrokeColor, getResources().getColor(R.color.colorBlue));
        } finally {
            ta.recycle();
        }
    }

    private void init() {
        applyProgressHeight();

        applyCircleColor();
        applyCircleStrokeColor();

        applyTextFontSize();
        applyTextColor();

        createAnimators();

        applyIndicatorDrawableColor();
        applyIndicatorDrawable();
    }

    private void createAnimators() {
        indicatorPopUpAnimator = ValueAnimator.ofFloat(0, 100);
        indicatorPopUpAnimator.setDuration(300);
        indicatorPopUpAnimator.setInterpolator(new FastOutSlowInInterpolator());
        indicatorPopUpAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float val = (float)valueAnimator.getAnimatedValue();

                if (!thumbIsFocused){
                    val = 100 - val;
                }

                float alpha = (val / 100f) * 255f;
                indicatorImagePaint.setAlpha((int)alpha);

                indicatorMatrix.setRotate(indicatorRotation, indicatorImageWidthDivideTwo, indicatorImageWidthDivideTwo);
                indicatorMatrix.postTranslate(indicatorPositionX ,dp2px(54) - ((val / 100) * dp2px(54)));
                indicatorMatrix.preScale((val / 100) * 1, (val / 100) * 1, indicatorImageWidthDivideTwo, indicatorImageWidthDivideTwo);
                postInvalidate();
            }
        });


        thumbFocusAnimator = ValueAnimator.ofFloat(0f, 1f);
        thumbFocusAnimator.setDuration(300);
        thumbFocusAnimator.setInterpolator(new FastOutSlowInInterpolator());
        thumbFocusAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                thumbAnimatedValue = (float)valueAnimator.getAnimatedValue();
                postInvalidate();
            }
        });

        createIndicatorAnimator();
    }

    private void applyIndicatorDrawable() {
        indicatorImage = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.balloon), (int)dp2px(58), (int)dp2px(50), false);
        indicatorImageWidthDivideTwo = indicatorImage.getWidth() / 2;
        indicatorImageHeightDivideTwo = indicatorImage.getHeight() / 2;
    }

    private void applyIndicatorDrawableColor() {
        indicatorImagePaint.setAlpha(0);
        indicatorImagePaint.setColorFilter(new PorterDuffColorFilter(indicatorColor, PorterDuff.Mode.SRC_IN));
    }

    private void applyProgressHeight() {
        linePaint.setStrokeWidth(progressHeight);
    }

    private void applyTextFontSize() {
        indicatorTextPaint.setTextSize(indicatorTextSize);
    }

    private void applyTextColor() {
        indicatorTextPaint.setColor(indicatorTextColor);
    }

    private void applyCircleColor() {
        thumbFillPaint.setStyle(Paint.Style.FILL);
        thumbFillPaint.setColor(thumbColor);
    }

    private void applyCircleStrokeColor() {
        thumbStrokePaint.setStyle(Paint.Style.STROKE);
        thumbStrokePaint.setColor(thumbStrokeColor);
    }

    private void createIndicatorAnimator() {

        balloonAnimation = new BalloonAnimation(thumbPosition - indicatorImageWidthDivideTwo, new BalloonAnimation.UpdateListener(){
            @Override
            public void onUpdate(float position, float rotation, int status) {
                indicatorPositionX = position;
                indicatorRotation = rotation;

                if (!indicatorPopUpAnimator.isRunning()) {
                    indicatorMatrix.setRotate(indicatorRotation, indicatorImageWidthDivideTwo, indicatorImageHeightDivideTwo);
                    indicatorMatrix.postTranslate(indicatorPositionX, 0);

                    postInvalidate();
                }
            }
        });
        balloonAnimation.start();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        containerMarginTop = indicatorImage.getHeight() + (int)dp2px(30);
        thumbPosition = (getMeasuredWidth() / 100f) * progress;
        balloonAnimation.updateValues(thumbPosition - indicatorImageWidthDivideTwo);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float linePosY = containerMarginTop;

        linePaint.setColor(getResources().getColor(R.color.colorGray));
        canvas.drawLine(thumbFocusedWidth, linePosY, getMeasuredWidth() - thumbFocusedWidth, linePosY, linePaint);
        linePaint.setColor(progressColor);
        canvas.drawLine(thumbFocusedWidth, linePosY, thumbPosition, linePosY, linePaint);


        if (thumbIsFocused) {
            float circleWidth = currentThumbReleasedWidth + ((thumbFocusedWidth - currentThumbReleasedWidth) * thumbAnimatedValue);
            float circleStrokeWidth = currentThumbReleasedStrokeWidth + ((thumbFocusedStrokeWidth - currentThumbReleasedStrokeWidth) * thumbAnimatedValue);
            canvas.drawCircle(thumbPosition, containerMarginTop, circleWidth, thumbFillPaint);
            thumbStrokePaint.setStrokeWidth(circleStrokeWidth);
            canvas.drawCircle(thumbPosition, containerMarginTop, circleWidth, thumbStrokePaint);
            currentThumbFocusedWidth = circleWidth;
            currentThumbFocusedStrokeWidth = circleStrokeWidth;
        }
        else
        {
            float circleWidth = currentThumbFocusedWidth + ((thumbReleasedWidth - currentThumbFocusedWidth) * thumbAnimatedValue);
            float circleStrokeWidth = currentThumbFocusedStrokeWidth + ((thumbReleasedStrokeWidth - currentThumbFocusedStrokeWidth) * thumbAnimatedValue);
            canvas.drawCircle(thumbPosition, containerMarginTop, circleWidth, thumbFillPaint);
            thumbStrokePaint.setStrokeWidth(circleStrokeWidth);
            canvas.drawCircle(thumbPosition, containerMarginTop, circleWidth, thumbStrokePaint);
            currentThumbReleasedWidth = circleWidth;
            currentThumbReleasedStrokeWidth = circleStrokeWidth;
        }

        canvas.drawBitmap(indicatorImage, indicatorMatrix, indicatorImagePaint);

        canvas.save();

        String indicatorText = ((int)progress)+"";
        Rect bounds = new Rect();
        indicatorTextPaint.getTextBounds(indicatorText, 0, indicatorText.length(), bounds);
        canvas.setMatrix(indicatorMatrix);
        canvas.drawText(indicatorText, indicatorImageWidthDivideTwo - (bounds.width() / 2), dp2px(22), indicatorTextPaint);

        canvas.restore();

        super.onDraw(canvas);
    }

    private void movieSeekBar(float pos){
        if (pos > thumbFocusedWidth && pos < getMeasuredWidth() - thumbFocusedWidth)
        {
            thumbPosition = pos;
            balloonAnimation.updateValues(thumbPosition - indicatorImageWidthDivideTwo);

            progress = Math.round(((thumbPosition - thumbFocusedWidth) / (getMeasuredWidth() - (thumbFocusedWidth * 2))) * 100);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                movieSeekBar(motionEvent.getX() - (thumbFocusedWidth / 2));

                thumbIsFocused = true;
                thumbFocusAnimator.start();
                indicatorPopUpAnimator.start();
                break;
            case MotionEvent.ACTION_MOVE:
                movieSeekBar(motionEvent.getX() - (thumbFocusedWidth / 2));
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                thumbIsFocused = false;
                thumbFocusAnimator.start();
                indicatorPopUpAnimator.start();
                break;

            default:
                return false;
        }

        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = getSuggestedMinimumWidth() + getPaddingLeft() + getPaddingRight();
        int desiredHeight = getSuggestedMinimumHeight() + getPaddingTop() + getPaddingBottom();

        int expectedHeight = indicatorImage.getHeight() + Math.max((int)progressHeight, (int) thumbFocusedWidth) + (int)dp2px(40) + getPaddingTop() + getPaddingBottom();

        if (expectedHeight > desiredHeight)
            desiredHeight = expectedHeight;

        setMeasuredDimension(
                measureDimension(desiredWidth, widthMeasureSpec),
                measureDimension(desiredHeight, heightMeasureSpec)
        );
    }

    private int measureDimension(int desiredSize, int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = desiredSize;
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }

        return result;
    }



    // Utility

    public float dp2px(float dpValue){
        return dpValue * ((float) getContext().getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }


    public float sp2px(float spValue) {
        float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return spValue * fontScale + 0.5f;
    }

    public float px2dp(float pxValue) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return pxValue / scale + 0.5f;
    }



    // Setters and Getters

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
        invalidate();
    }

    public int getIndicatorColor() {
        return indicatorColor;
    }

    public void setIndicatorColor(int indicatorColor) {
        this.indicatorColor = indicatorColor;
        applyIndicatorDrawableColor();
        invalidate();
    }

    public int getIndicatorTextColor() {
        return indicatorTextColor;
    }

    public void setIndicatorTextColor(int indicatorTextColor) {
        this.indicatorTextColor = indicatorTextColor;
        applyTextColor();
        invalidate();
    }

    public int getIndicatorTextSize() {
        return indicatorTextSize;
    }

    public void setIndicatorTextSize(int indicatorTextSize) {
        this.indicatorTextSize = indicatorTextSize;
        applyTextFontSize();
        invalidate();
    }

    public float getProgressHeight() {
        return progressHeight;
    }

    public void setProgressHeight(float progressHeight) {
        this.progressHeight = progressHeight;
        applyProgressHeight();
        invalidate();
    }

    public int getProgressColor() {
        return progressColor;
    }

    public void setProgressColor(int progressColor) {
        this.progressColor = progressColor;
        invalidate();
    }

    public int getThumbColor() {
        return thumbColor;
    }

    public void setThumbColor(int thumbColor) {
        this.thumbColor = thumbColor;
        applyCircleColor();
        invalidate();
    }

    public int getThumbStrokeColor() {
        return thumbStrokeColor;
    }

    public void setThumbStrokeColor(int thumbStrokeColor) {
        this.thumbStrokeColor = thumbStrokeColor;
        applyCircleStrokeColor();
        invalidate();
    }
}

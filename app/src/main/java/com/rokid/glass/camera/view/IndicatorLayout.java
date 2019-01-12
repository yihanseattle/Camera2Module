package com.rokid.glass.camera.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.rokid.glass.camera.R;

public class IndicatorLayout extends FrameLayout {

    private float acceleration = 1f;//0.5f;
    private float headMoveOffset = 0.65f;//0.6f;
    private float footMoveOffset = 1- headMoveOffset;
    private float radiusMax;
    private float radiusMin;
    private float radiusOffset;

    private IndicatorView mIndicatorView;

    private boolean mCurrentInCamera = true;

    public IndicatorLayout(Context context) {
        this(context, null);
    }

    public IndicatorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void initIndicatorView() {
        radiusMax = getResources().getDimension(R.dimen.indicator_radius_max);
        radiusMin = getResources().getDimension(R.dimen.indicator_radius_min);
        radiusOffset = radiusMax - radiusMin;

        mIndicatorView = new IndicatorView(getContext());
        mIndicatorView.setIndicatorColor(Color.WHITE);
        mIndicatorView.getHeadPoint().setRadius(radiusMax);
        mIndicatorView.getFootPoint().setRadius(radiusMax);
        addView(mIndicatorView);
    }


    private float startX = 0;
    private float startY = 0;
    private float endX = 0;
    private float endY = 0;

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            startX = radiusMax;
            endX = r - l - radiusMax;
            startY = endY =  (b-t) / 2;
            mIndicatorView.getHeadPoint().setX(startX);
            mIndicatorView.getHeadPoint().setY(startY);
            mIndicatorView.getFootPoint().setX(startX);
            mIndicatorView.getFootPoint().setY(startY);
            mIndicatorView.postInvalidate();
        }
    }

    public void switchMode(final boolean isCamera) {
        if (mCurrentInCamera == isCamera) {
            return;
        }
        mCurrentInCamera = isCamera;

        ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
        anim.setDuration(500);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float positionOffset = (float) animation.getAnimatedValue();
                startAinmation(positionOffset, isCamera);
            }
        });
        anim.start();
    }


    private void startAinmation(float positionOffset, final boolean isCamera) {
        // radius
        float radiusOffsetHead = 0.5f;
        if(positionOffset < radiusOffsetHead){
            mIndicatorView.getHeadPoint().setRadius(radiusMin);
        }else{
            mIndicatorView.getHeadPoint().setRadius(((positionOffset-radiusOffsetHead)/(1-radiusOffsetHead) * radiusOffset + radiusMin));
        }
        float radiusOffsetFoot = 0.5f;
        if(positionOffset < radiusOffsetFoot){
            mIndicatorView.getFootPoint().setRadius((1-positionOffset/radiusOffsetFoot) * radiusOffset + radiusMin);
        }else{
            mIndicatorView.getFootPoint().setRadius(radiusMin);
        }

        float headX = 1f;
        if (positionOffset < headMoveOffset){
            float positionOffsetTemp = positionOffset / headMoveOffset;
            headX = (float) ((Math.atan(positionOffsetTemp*acceleration*2 - acceleration ) + (Math.atan(acceleration))) / (2 * (Math.atan(acceleration))));
        }
        if (isCamera) {
            mIndicatorView.getHeadPoint().setX(endX - headX * (endX-startX));
        }
        else {
            mIndicatorView.getHeadPoint().setX(startX + headX * (endX-startX));
        }

        float footX = 0f;
        if (positionOffset > footMoveOffset){
            float positionOffsetTemp = (positionOffset- footMoveOffset) / (1- footMoveOffset);
            footX = (float) ((Math.atan(positionOffsetTemp*acceleration*2 - acceleration ) + (Math.atan(acceleration))) / (2 * (Math.atan(acceleration))));
        }
        if (isCamera) {
            mIndicatorView.getFootPoint().setX(endX - footX * (endX-startX));
        }
        else {
            mIndicatorView.getFootPoint().setX(startX + footX * (endX-startX));
        }

        // reset radius
        if(positionOffset == 0){
            mIndicatorView.getHeadPoint().setRadius(radiusMax);
            mIndicatorView.getFootPoint().setRadius(radiusMax);
        }
        mIndicatorView.postInvalidate();
    }

}

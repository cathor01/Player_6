package cn.cathor.selfview

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.Transformation

/**
 * Created by Cathor on 2016/3/3 19:55.
 */

class TouchEffectAnimator {
    companion object {
        private val DECELERATE_INTERPOLATOR = DecelerateInterpolator(2.8f)
        private val ACCELERATE_INTERPOLATOR = AccelerateInterpolator()
        private val EASE_ANIM_DURATION = 200
        private val RIPPLE_ANIM_DURATION = 300
        private val MAX_RIPPLE_ALPHA = (255 * 0.8).toInt()
    }
    private val mView: View
    private var mClipRadius = 0;
    private var mAnimDuration = RIPPLE_ANIM_DURATION;
    private var mTouchEffect = TouchEffect.Move;
    private var mAnimation : Animation? = null;

    private var mMaxRadius = 0f;
    private var mRadius = 0f;

    private var pointDown = Pointf(0f, 0f)
    private var pointCenter = Pointf(0f, 0f)
    private var pointPaint = Pointf(0f, 0f)

    private var mPaint = Paint();
    private var mRectRectR = RectF();
    private var mRectPath = Path();
    private var mRectAlpha = 0;

    private var isTouchReleased = false;
    private var isAnimatingFadeIn = false;

    constructor(view : View){
        mView = view
        onMeasure()
    }


    private val mAnimationListener = object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation) {
            isAnimatingFadeIn = true
        }

        override fun onAnimationEnd(animation: Animation) {
            isAnimatingFadeIn = false
            // Is un touch auto fadeOutEffect()
            if (isTouchReleased) fadeOutEffect()
        }

        override fun onAnimationRepeat(animation: Animation) {
        }
    }

    fun onMeasure() {
        pointCenter.x = mView.width / 2f;
        pointCenter.y = mView.height / 2f;

        mRectRectR.set(0f, 0f, mView.width.toFloat(), mView.height.toFloat());

        mRectPath.reset();
        mRectPath.addRoundRect(mRectRectR, mClipRadius.toFloat(), mClipRadius.toFloat(), Path.Direction.CW);
    }

    fun setAnimDuration(animDuration: Int) {
        this.mAnimDuration = animDuration
    }

    fun getTouchEffect(): TouchEffect {
        return mTouchEffect
    }

    fun setTouchEffect(touchEffect: TouchEffect) {
        mTouchEffect = touchEffect
        if (mTouchEffect === TouchEffect.Ease)
            mAnimDuration = EASE_ANIM_DURATION
    }

    fun setEffectColor(effectColor: Int) {
        mPaint.color = effectColor
    }

    fun setClipRadius(mClipRadius: Int) {
        this.mClipRadius = mClipRadius
    }

    fun onTouchEvent(event: MotionEvent) {
        if (event.actionMasked == MotionEvent.ACTION_CANCEL) {
            isTouchReleased = true
            if (!isAnimatingFadeIn) {
                fadeOutEffect()
            }
        }
        if (event.actionMasked == MotionEvent.ACTION_UP) {
            isTouchReleased = true
            if (!isAnimatingFadeIn) {
                fadeOutEffect()
            }
        } else if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            // Gets the bigger value (width or height) to fit the circle
            mMaxRadius = if (pointCenter.x > pointCenter.y) pointCenter.x else pointCenter.y
            // This circle radius is 75% or fill all
            if (mTouchEffect == TouchEffect.Move)
                mMaxRadius *= 0.75f
            else
                if(mTouchEffect === TouchEffect.Ripple){
                    var x = if(pointDown.x < pointCenter.x ) 2 * pointCenter.x else 0f;
                    var y = if(pointDown.y < pointCenter.y ) 2 * pointCenter.y else 0f;
                    mMaxRadius = Math.sqrt((x - pointDown.x) * (x - pointDown.x) + (y - pointDown.y) * (y - pointDown.y) + 1.0).toFloat();
                }
                else {
                    mMaxRadius *= 2.5f
                }
            // Set default operation to fadeOutEffect()
            isTouchReleased = false
            isAnimatingFadeIn = true

            // Set this start point
            pointPaint.x = event.x
            pointDown.x = event.x
            pointPaint.y = event.y
            pointDown.y = event.y

            // This color alpha
            mRectAlpha = 0

            // Cancel and Start new animation
            cancelAnimation()
            startAnimation()
        }
    }

    fun onDraw(canvas: Canvas) {
        mPaint.alpha = mRectAlpha;
        canvas.drawPath(mRectPath, mPaint);

        // Draw Ripple
        if (isAnimatingFadeIn && (mTouchEffect == TouchEffect.Move
                || mTouchEffect == TouchEffect.Ripple)) {
            // Canvas Clip
            canvas.clipPath(mRectPath);
            mPaint.alpha = MAX_RIPPLE_ALPHA;
            canvas.drawCircle(pointPaint.x, pointPaint.y, mRadius, mPaint);
        }
    }

    private fun startAnimation() {
        var animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                if (mTouchEffect == TouchEffect.Move) {
                    mRadius = mMaxRadius * interpolatedTime;
                    pointPaint.x = pointDown.x + (pointCenter.x - pointDown.x) * interpolatedTime;
                    pointPaint.y = pointDown.y + (pointCenter.y - pointDown.y) * interpolatedTime;
                } else if (mTouchEffect == TouchEffect.Ripple) {
                    mRadius = mMaxRadius * interpolatedTime;
                }

                mRectAlpha = (interpolatedTime * MAX_RIPPLE_ALPHA).toInt();
                mView.invalidate();
            }
        };
        animation.interpolator = DECELERATE_INTERPOLATOR;
        animation.duration = mAnimDuration.toLong();
        animation.setAnimationListener(mAnimationListener);
        mView.startAnimation(animation);
    }

    private fun cancelAnimation() {
        if (mAnimation != null) {
            mAnimation!!.cancel()
            mAnimation!!.setAnimationListener(null)
        }
    }

    private fun fadeOutEffect() {
        var animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                mRectAlpha = (MAX_RIPPLE_ALPHA - (MAX_RIPPLE_ALPHA * interpolatedTime)).toInt();
                mView.invalidate();
            }
        };
        animation.interpolator = ACCELERATE_INTERPOLATOR;
        animation.duration = EASE_ANIM_DURATION.toLong();
        mView.startAnimation(animation);
    }


    enum class TouchEffect {
        Move,
        Ease,
        Ripple,
        None
    }
}
package com.dd;

import android.animation.TimeInterpolator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.StateSet;
import android.widget.Button;

import com.dd.circular.progress.button.R;

public class CircularProgressButton extends Button {

    public static final int IDLE_STATE_PROGRESS = 0;
    public static final int ERROR_STATE_PROGRESS = -1;
    public static final int SUCCESS_STATE_PROGRESS = 100;
    public static final int INDETERMINATE_STATE_PROGRESS = 50;

    private StrokeGradientDrawable background;

    private CircularAnimatedDrawable mAnimatedDrawable;
    private CircularProgressDrawable mProgressDrawable;

    private int mIdleColor;
    private int mCompleteColor;
    private int mErrorColor;

    private Drawable mIdleBackground;
    private Drawable mCompleteBackground;
    private Drawable mErrorBackground;

    private StateManager mStateManager;
    private State mState;
    private String mIdleText;
    private String mCompleteText;
    private String mErrorText;
    private String mProgressText;

    private int mColorProgress;
    private int mColorIndicator;
    private int mColorIndicatorBackground;
    private int mDisabledStrokeWidth;
    private int mDisabledStrokeColor;
    private int mStrokeColor;
    private int mIdleStrokeWidth;

    private int mIconIdle;
    private int mIconComplete;
    private int mIconError;

    private int mStrokeWidth;
    private int mPaddingProgress;
    private float mCornerRadius;
    private boolean mIndeterminateProgressMode;
    private boolean mConfigurationChanged;

    private long mDelay;
    private int mDuration;
    private TimeInterpolator mInterpolator;

    private int mLeftPadding;
    private int mRightPadding;

    public enum State {
        PROGRESS, IDLE, COMPLETE, ERROR
    }

    private int mProgress;

    private boolean mMorphingInProgress;

    public CircularProgressButton(Context context) {
        super(context);

        init(context, null);
    }

    public CircularProgressButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }

    public CircularProgressButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(context, attrs);
    }

    private void init(Context context, AttributeSet attributeSet) {
        mStrokeWidth = (int) getContext().getResources().getDimension(R.dimen.cpb_stroke_width);

        initAttributes(context, attributeSet);

        mState = State.IDLE;
        mStateManager = new StateManager(this);

        setText(mIdleText);

        setBackgroundCompat(mIdleBackground);

        mLeftPadding = getPaddingLeft();
        mRightPadding = getPaddingRight();

        mDuration = MorphingAnimation.DURATION_NORMAL;
    }

    private int getNormalColor(ColorStateList colorStateList) {
        return colorStateList.getColorForState(new int[]{android.R.attr.state_enabled}, 0);
    }

    private int getPressedColor(ColorStateList colorStateList) {
        return colorStateList.getColorForState(new int[]{android.R.attr.state_pressed}, 0);
    }

    private int getFocusedColor(ColorStateList colorStateList) {
        return colorStateList.getColorForState(new int[]{android.R.attr.state_focused}, 0);
    }

    private int getDisabledColor(ColorStateList colorStateList) {
        return colorStateList.getColorForState(new int[]{-android.R.attr.state_enabled}, 0);
    }

    private StrokeGradientDrawable createDrawable(int color) {
        GradientDrawable drawable = (GradientDrawable) getResources().getDrawable(R.drawable.cpb_background).mutate();
        drawable.setColor(color);
        drawable.setCornerRadius(mCornerRadius);

        StrokeGradientDrawable strokeGradientDrawable = new StrokeGradientDrawable(drawable);
        strokeGradientDrawable.setStrokeColor(color);
        strokeGradientDrawable.setStrokeWidth(mStrokeWidth);

        return strokeGradientDrawable;
    }

    @Override
    protected void drawableStateChanged() {
        if (mState == State.COMPLETE) {
            setBackgroundCompat(mCompleteBackground);
        } else if (mState == State.IDLE) {
            setBackgroundCompat(mIdleBackground);
        } else if (mState == State.ERROR) {
            setBackgroundCompat(mErrorBackground);
        }

        if (mState != State.PROGRESS) {
            super.drawableStateChanged();
        }
    }

    private void initAttributes(Context context, AttributeSet attributeSet) {
        TypedArray attr = getTypedArray(context, attributeSet, R.styleable.CircularProgressButton);

        if (attr == null) {
            return;
        }

        try {
            mIdleText = attr.getString(R.styleable.CircularProgressButton_cpb_textIdle);
            mCompleteText = attr.getString(R.styleable.CircularProgressButton_cpb_textComplete);
            mErrorText = attr.getString(R.styleable.CircularProgressButton_cpb_textError);
            mProgressText = attr.getString(R.styleable.CircularProgressButton_cpb_textProgress);

            mIconIdle = attr.getResourceId(R.styleable.CircularProgressButton_cpb_iconIdle, 0);
            mIconComplete = attr.getResourceId(R.styleable.CircularProgressButton_cpb_iconComplete, 0);
            mIconError = attr.getResourceId(R.styleable.CircularProgressButton_cpb_iconError, 0);
            mCornerRadius = attr.getDimension(R.styleable.CircularProgressButton_cpb_cornerRadius, 0);
            mPaddingProgress = attr.getDimensionPixelSize(R.styleable.CircularProgressButton_cpb_paddingProgress, 0);

            int blue = getColor(R.color.cpb_blue);
            int white = getColor(R.color.cpb_white);
            int grey = getColor(R.color.cpb_grey);

            mDisabledStrokeColor = attr.getColor(R.styleable.CircularProgressButton_cpb_disabledStrokeColor, -1);
            mDisabledStrokeWidth = attr.getDimensionPixelSize(R.styleable.CircularProgressButton_cpb_disabledStrokeWidth, -1);
            mStrokeColor = attr.getColor(R.styleable.CircularProgressButton_cpb_strokeColor, -1);
            mStrokeWidth = attr.getDimensionPixelSize(R.styleable.CircularProgressButton_cpb_strokeWidth, mStrokeWidth);
            mIdleStrokeWidth = attr.getDimensionPixelSize(R.styleable.CircularProgressButton_cpb_idleStrokeWidth, -1);
            mIdleColor = attr.getColor(R.styleable.CircularProgressButton_cpb_idleColor, -1);
            mCompleteColor = attr.getColor(R.styleable.CircularProgressButton_cpb_completeColor, -1);
            mErrorColor = attr.getColor(R.styleable.CircularProgressButton_cpb_errorColor, -1);
            mIdleBackground = attr.getDrawable(R.styleable.CircularProgressButton_cpb_idleBackground);
            mCompleteBackground = attr.getDrawable(R.styleable.CircularProgressButton_cpb_idleBackground);
            mErrorBackground = attr.getDrawable(R.styleable.CircularProgressButton_cpb_idleBackground);

            mColorProgress = attr.getColor(R.styleable.CircularProgressButton_cpb_colorProgress, white);
            mColorIndicator = attr.getColor(R.styleable.CircularProgressButton_cpb_colorIndicator, blue);
            mColorIndicatorBackground = attr.getColor(R.styleable.CircularProgressButton_cpb_colorIndicatorBackground, grey);
        } finally {
            attr.recycle();
        }
    }

    protected int getColor(int id) {
        return getResources().getColor(id);
    }

    protected TypedArray getTypedArray(Context context, AttributeSet attributeSet, int[] attr) {
        return context.obtainStyledAttributes(attributeSet, attr, 0, 0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mProgress > 0 && mState == State.PROGRESS && !mMorphingInProgress) {
            if (mIndeterminateProgressMode) {
                drawIndeterminateProgress(canvas);
            } else {
                drawProgress(canvas);
            }
        }
    }

    private void drawIndeterminateProgress(Canvas canvas) {
        if (mAnimatedDrawable == null) {
            int offset = (getWidth() - getHeight()) / 2;
            mAnimatedDrawable = new CircularAnimatedDrawable(mColorIndicator, mStrokeWidth);
            int left = offset + mPaddingProgress;
            int right = getWidth() - offset - mPaddingProgress;
            int bottom = getHeight() - mPaddingProgress;
            int top = mPaddingProgress;
            mAnimatedDrawable.setBounds(left, top, right, bottom);
            mAnimatedDrawable.setCallback(this);
            mAnimatedDrawable.start();
        } else {
            mAnimatedDrawable.draw(canvas);
        }
    }

    private void drawProgress(Canvas canvas) {
        if (mProgressDrawable == null) {
            int offset = (getWidth() - getHeight()) / 2;
            int size = getHeight() - mPaddingProgress * 2;
            mProgressDrawable = new CircularProgressDrawable(size, mStrokeWidth, mColorIndicator);
            int left = offset + mPaddingProgress;
            mProgressDrawable.setBounds(left, mPaddingProgress, left, mPaddingProgress);
        }

        float sweepAngle = (360f / SUCCESS_STATE_PROGRESS) * mProgress;
        mProgressDrawable.setSweepAngle(sweepAngle);
        mProgressDrawable.draw(canvas);
    }

    public long getDelay() {
        return mDelay;
    }

    public void setDelay(long delay) {
        this.mDelay = delay;
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }

    public int getDuration() {
        return mDuration;
    }

    public TimeInterpolator getInterpolator() {
        return mInterpolator;
    }

    public void setInterpolator(TimeInterpolator interpolator) {
        mInterpolator = interpolator;
    }

    public boolean isIndeterminateProgressMode() {
        return mIndeterminateProgressMode;
    }

    public void setIndeterminateProgressMode(boolean indeterminateProgressMode) {
        this.mIndeterminateProgressMode = indeterminateProgressMode;
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return who == mAnimatedDrawable || super.verifyDrawable(who);
    }

    private MorphingAnimation createMorphing() {
        mMorphingInProgress = true;

        MorphingAnimation animation = new MorphingAnimation(this, background);
        animation.setFromCornerRadius(mCornerRadius);
        animation.setToCornerRadius(mCornerRadius);

        animation.setFromWidth(getWidth());
        animation.setToWidth(getWidth());

        animation.setDelay(mDelay);

        if (mConfigurationChanged) {
            animation.setDuration(MorphingAnimation.DURATION_INSTANT);
        } else {
            animation.setDuration(mDuration > 0 ? mDuration : MorphingAnimation.DURATION_NORMAL);
        }

        animation.setInterpolator(mInterpolator);

        mConfigurationChanged = false;

        return animation;
    }

    private MorphingAnimation createProgressMorphing(float fromCorner, float toCorner, int fromWidth, int toWidth) {
        mMorphingInProgress = true;

        MorphingAnimation animation = new MorphingAnimation(this, background);
        animation.setFromCornerRadius(fromCorner);
        animation.setToCornerRadius(toCorner);

        animation.setPadding(mPaddingProgress);

        animation.setFromWidth(fromWidth);
        animation.setToWidth(toWidth);

        animation.setDelay(mDelay);

        if (mConfigurationChanged) {
            animation.setDuration(MorphingAnimation.DURATION_INSTANT);
        } else {
            animation.setDuration(mDuration > 0 ? mDuration : MorphingAnimation.DURATION_NORMAL);
        }

        animation.setInterpolator(mInterpolator);

        mConfigurationChanged = false;

        return animation;
    }

    private void morphToProgress() {
        setWidth(getWidth());

        setText(mProgressText);
        removeIcon(true);

        MorphingAnimation animation = createProgressMorphing(mCornerRadius, getHeight(), getWidth(), getHeight());

        animation.setFromColor(mIdleColor);
        animation.setToColor(mColorProgress);

        animation.setFromStrokeColor(mIdleColor);
        animation.setToStrokeColor(mColorIndicatorBackground);

        animation.setListener(mProgressStateListener);

        animation.start();
    }

    private OnAnimationEndListener mProgressStateListener = new OnAnimationEndListener() {
        @Override
        public void onAnimationEnd() {
            background.setStrokeWidth(mStrokeWidth);
            mMorphingInProgress = false;
            mState = State.PROGRESS;

            mStateManager.checkState(CircularProgressButton.this);
        }
    };

    private void morphProgressToComplete() {
        MorphingAnimation animation = createProgressMorphing(getHeight(), mCornerRadius, getHeight(), getWidth());

        animation.setFromColor(mColorProgress);
        animation.setToColor(mCompleteColor);

        animation.setFromStrokeColor(mColorIndicator);
        animation.setToStrokeColor(mCompleteColor);

        animation.setListener(mCompleteStateListener);

        animation.start();
    }

    private void morphIdleToComplete() {
        MorphingAnimation animation = createMorphing();

        animation.setFromColor(mIdleColor);
        animation.setToColor(mCompleteColor);

        animation.setFromStrokeColor(mIdleColor);
        animation.setToStrokeColor(mCompleteColor);

        animation.setListener(mCompleteStateListener);

        animation.start();
    }

    private OnAnimationEndListener mCompleteStateListener = new OnAnimationEndListener() {
        @Override
        public void onAnimationEnd() {
            setText(mCompleteText);
            setIcon(mIconComplete, mCompleteText == null || mCompleteText.isEmpty());

            mMorphingInProgress = false;
            mState = State.COMPLETE;

            mStateManager.checkState(CircularProgressButton.this);
        }
    };

    private void morphCompleteToIdle() {
        MorphingAnimation animation = createMorphing();

        animation.setFromColor(mCompleteColor);
        animation.setToColor(mIdleColor);

        animation.setFromStrokeColor(mCompleteColor);
        animation.setToStrokeColor(mIdleColor);

        animation.setListener(mIdleStateListener);

        animation.start();

    }

    private void morphErrorToIdle() {
        MorphingAnimation animation = createMorphing();

        animation.setFromColor(mErrorColor);
        animation.setToColor(mIdleColor);

        animation.setFromStrokeColor(mErrorColor);
        animation.setToStrokeColor(mIdleColor);

        animation.setListener(mIdleStateListener);

        animation.start();
    }

    private OnAnimationEndListener mIdleStateListener = new OnAnimationEndListener() {
        @Override
        public void onAnimationEnd() {
            setText(mIdleText);
            setIcon(mIconIdle, mIdleText == null || mIdleText.isEmpty());

            mMorphingInProgress = false;
            mState = State.IDLE;

            mStateManager.checkState(CircularProgressButton.this);
            if (mStrokeColor != -1) {
                background.setStrokeColor(mStrokeColor);
            }
            if (mIdleStrokeWidth != -1) {
                background.setStrokeWidth(mIdleStrokeWidth);
            }
        }
    };

    private void morphIdleToError() {
        MorphingAnimation animation = createMorphing();

        animation.setFromColor(mIdleColor);
        animation.setToColor(mErrorColor);

        animation.setFromStrokeColor(mIdleColor);
        animation.setToStrokeColor(mErrorColor);

        animation.setListener(mErrorStateListener);

        animation.start();
    }

    private void morphProgressToError() {
        MorphingAnimation animation = createProgressMorphing(getHeight(), mCornerRadius, getHeight(), getWidth());

        animation.setFromColor(mColorProgress);
        animation.setToColor(mErrorColor);

        animation.setFromStrokeColor(mColorIndicator);
        animation.setToStrokeColor(mErrorColor);
        animation.setListener(mErrorStateListener);

        animation.start();
    }

    private OnAnimationEndListener mErrorStateListener = new OnAnimationEndListener() {
        @Override
        public void onAnimationEnd() {
            setText(mErrorText);
            setIcon(mIconError, mErrorText == null || mErrorText.isEmpty());

            mMorphingInProgress = false;
            mState = State.ERROR;

            mStateManager.checkState(CircularProgressButton.this);
        }
    };

    private void morphProgressToIdle() {
        MorphingAnimation animation = createProgressMorphing(getHeight(), mCornerRadius, getHeight(), getWidth());

        animation.setFromColor(mColorProgress);
        animation.setToColor(mIdleColor);

        animation.setFromStrokeColor(mColorIndicator);
        animation.setToStrokeColor(mIdleColor);
        animation.setListener(mIdleStateListener);

        animation.start();
    }

    private void setIcon(int icon, boolean center) {
        Drawable drawable = icon != 0 ? getResources().getDrawable(icon) : null;
        if (drawable != null) {
            setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);

            if (center) {
                int padding = (getWidth() / 2) - (drawable.getIntrinsicWidth() / 2);

                setPadding(padding / 2, 0, padding / 2, 0);
            } else {
                setPadding(mLeftPadding, 0, mRightPadding, 0);
            }
        } else {
            removeIcon(center || icon == 0);
        }
    }

    protected void removeIcon(boolean center) {
        setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

        if (center) {
            int padding = 0;

            setPadding(padding, 0, padding, 0);
        } else {
            setPadding(mLeftPadding, 0, mRightPadding, 0);
        }
    }

    /**
     * Set the View's background. Masks the API changes made in Jelly Bean.
     */
    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public void setBackgroundCompat(Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(drawable);
        } else {
            setBackgroundDrawable(drawable);
        }
    }

    public void setState(State state) throws Exception {
        if(state != null) {
            switch (state) {
                case IDLE:
                    setProgress(IDLE_STATE_PROGRESS);
                    break;

                case PROGRESS:
                    if (isIndeterminateProgressMode()) {
                        setProgress(INDETERMINATE_STATE_PROGRESS);
                    } else {
                        throw new InvalidStateException(state);
                    }
                    break;

                case COMPLETE:
                    setProgress(SUCCESS_STATE_PROGRESS);
                    break;

                case ERROR:
                    setProgress(ERROR_STATE_PROGRESS);
                    break;

                default:
                    throw new InvalidStateException(state);
            }
        } else {
            throw new InvalidStateException();
        }
    }

    public State getState() {
        return mState;
    }

    public void setProgress(int progress) {
        mProgress = progress;

        if (mMorphingInProgress || getWidth() == 0) {
            return;
        }

        mStateManager.saveProgress(this);

        if (mProgress >= SUCCESS_STATE_PROGRESS) {
            if (mState == State.PROGRESS) {
                morphProgressToComplete();
            } else if (mState == State.IDLE) {
                morphIdleToComplete();
            }
        } else if (mProgress > IDLE_STATE_PROGRESS) {
            if (mState == State.IDLE) {
                morphToProgress();
            } else if (mState == State.PROGRESS) {
                invalidate();
            }
        } else if (mProgress == ERROR_STATE_PROGRESS) {
            if (mState == State.PROGRESS) {
                morphProgressToError();
            } else if (mState == State.IDLE) {
                morphIdleToError();
            }
        } else if (mProgress == IDLE_STATE_PROGRESS) {
            if (mState == State.COMPLETE) {
                morphCompleteToIdle();
            } else if (mState == State.PROGRESS) {
                morphProgressToIdle();
            } else if (mState == State.ERROR) {
                morphErrorToIdle();
            }
        }
    }

    public int getProgress() {
        return mProgress;
    }

    public void setBackgroundColor(int color) {
        background.getGradientDrawable().setColor(color);
    }

    public void setStrokeColor(int color) {
        background.setStrokeColor(color);
    }

    public String getIdleText() {
        return mIdleText;
    }

    public String getCompleteText() {
        return mCompleteText;
    }

    public String getErrorText() {
        return mErrorText;
    }

    public void setIdleText(String text) {
        mIdleText = text;
    }

    public void setCompleteText(String text) {
        mCompleteText = text;
    }

    public void setErrorText(String text) {
        mErrorText = text;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (changed) {
            setProgress(mProgress);
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.mProgress = mProgress;
        savedState.mIndeterminateProgressMode = mIndeterminateProgressMode;
        savedState.mConfigurationChanged = true;

        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState savedState = (SavedState) state;
            mProgress = savedState.mProgress;
            mIndeterminateProgressMode = savedState.mIndeterminateProgressMode;
            mConfigurationChanged = savedState.mConfigurationChanged;
            super.onRestoreInstanceState(savedState.getSuperState());
            setProgress(mProgress);
        } else {
            super.onRestoreInstanceState(state);
        }
    }


    static class SavedState extends BaseSavedState {

        private boolean mIndeterminateProgressMode;
        private boolean mConfigurationChanged;
        private int mProgress;

        public SavedState(Parcelable parcel) {
            super(parcel);
        }

        private SavedState(Parcel in) {
            super(in);

            mProgress = in.readInt();
            mIndeterminateProgressMode = in.readInt() == 1;
            mConfigurationChanged = in.readInt() == 1;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);

            out.writeInt(mProgress);
            out.writeInt(mIndeterminateProgressMode ? 1 : 0);
            out.writeInt(mConfigurationChanged ? 1 : 0);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    private class InvalidStateException extends Exception {

        private final State mErrorState;

        public InvalidStateException() {
            this(null);
        }

        public InvalidStateException(State state) {
            mErrorState = state;
        }

        public State getErrorState() {
            return mErrorState;
        }

        @Override
        public String getMessage() {
            return String.format("InvalidState: " + (mErrorState != null ? mErrorState.name() : "NULL"));
        }
    }
}

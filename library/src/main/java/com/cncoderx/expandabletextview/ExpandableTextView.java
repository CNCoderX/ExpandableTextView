package com.cncoderx.expandabletextview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by cncoderx on 2017/9/1.
 */
public class ExpandableTextView extends LinearLayout implements ValueAnimator.AnimatorUpdateListener {
    private TextView mTextView;
    private ViewGroup mIndicator;
    private View mExpandedIndicator;
    private View mCollapsedIndicator;

    private int mVisibleLines = 5;
    private int mAnimDuration = 300;
    private boolean mCollapsed = true;
    private boolean mAlwaysShowIndicator = false;
    private boolean isAnimation = false;
    private boolean postInited = false;

    private int mExpandTextHeight = 0;
    private int mCollapseTextHeight = 0;

    public ExpandableTextView(Context context) {
        super(context);
    }

    public ExpandableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(LinearLayout.VERTICAL);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExpandableTextView);
        mVisibleLines = Math.max(0, a.getInt(R.styleable.ExpandableTextView_visibleLines, mVisibleLines));
        mAnimDuration = Math.max(0, a.getInt(R.styleable.ExpandableTextView_animDuration, mAnimDuration));
        mCollapsed = a.getBoolean(R.styleable.ExpandableTextView_collapsed, true);
        mAlwaysShowIndicator = a.getBoolean(R.styleable.ExpandableTextView_alwaysShowIndicator, false);
        a.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTextView = (TextView) findViewById(R.id.expandable_text);
        if (mTextView == null) {
            throw new NullPointerException("not found child view by named \"expandable_text\"");
        }
        mTextView.addTextChangedListener(mTextWatcher);
        mIndicator = (ViewGroup) findViewById(R.id.expandable_indicator);
        if (mIndicator == null) {
            throw new NullPointerException("not found child view by named \"expandable_button_group\"");
        }
        mExpandedIndicator = mIndicator.findViewById(R.id.expandable_indicator_expanded);
        if (mExpandedIndicator == null) {
            throw new NullPointerException("not found child view by named \"expandable_button_expanded\"");
        }
        mExpandedIndicator.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAnimation) {
                    mCollapsed = true;
                    notifyStateChanged(mAnimDuration > 0);
                }
            }
        });
        mCollapsedIndicator = mIndicator.findViewById(R.id.expandable_indicator_collapsed);
        if (mCollapsedIndicator == null) {
            throw new NullPointerException("not found child view by named \"expandable_button_collapsed\"");
        }
        mCollapsedIndicator.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAnimation) {
                    mCollapsed = false;
                    notifyStateChanged(mAnimDuration > 0);
                }
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!postInited && mTextView.getLayout() != null) {
            postInit();
        }
    }

    private void postInit() {
        int allLines = mTextView.getLineCount();
        int visibleLines = Math.min(allLines, mVisibleLines);
        mCollapseTextHeight = getLinesHeight(visibleLines);
        mExpandTextHeight = getLinesHeight(allLines);
        if (!mAlwaysShowIndicator && visibleLines == allLines) {
            mIndicator.setVisibility(View.GONE);
        } else {
            mIndicator.setVisibility(View.VISIBLE);
            if (mCollapsed) {
                mCollapsedIndicator.setVisibility(View.VISIBLE);
                mExpandedIndicator.setVisibility(View.INVISIBLE);
                ViewGroup.LayoutParams lParams = mTextView.getLayoutParams();
                lParams.height = mCollapseTextHeight;
                mTextView.requestLayout();
            } else {
                mExpandedIndicator.setVisibility(View.VISIBLE);
                mCollapsedIndicator.setVisibility(View.INVISIBLE);
                ViewGroup.LayoutParams lParams = mTextView.getLayoutParams();
                lParams.height = mExpandTextHeight;
                mTextView.requestLayout();
            }
        }
        postInited = true;
    }

    private int getLinesHeight(int lines) {
        int lineHeight = mTextView.getLineHeight() * lines;
        int padding = mTextView.getCompoundPaddingTop() + mTextView.getCompoundPaddingBottom();
        return lineHeight + padding;
    }

    public void toggle() {
        mCollapsed = !mCollapsed;
        notifyStateChanged(mAnimDuration > 0);
    }

    void notifyStateChanged(boolean anim) {
        if (isCollapsed()) {
            mCollapsedIndicator.setVisibility(View.VISIBLE);
            mExpandedIndicator.setVisibility(View.INVISIBLE);

            int startHeight = mExpandTextHeight;
            int endHeight = mCollapseTextHeight;
            if (startHeight > endHeight) {
                updateLayout(startHeight, endHeight, anim);
            }
        } else {
            mExpandedIndicator.setVisibility(View.VISIBLE);
            mCollapsedIndicator.setVisibility(View.INVISIBLE);

            int startHeight = mCollapseTextHeight;
            int endHeight = mExpandTextHeight;
            if (startHeight < endHeight) {
                updateLayout(startHeight, endHeight, anim);
            }
        }
    }

    void updateLayout(int startHeight, int endHeight, boolean anim) {
        if (anim) {
            ValueAnimator animator = ValueAnimator.ofInt(startHeight, endHeight);
            animator.setTarget(mTextView);
            animator.setDuration(mAnimDuration);
            animator.addUpdateListener(this);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    isAnimation = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    isAnimation = false;
                }
            });
            animator.start();
        } else {
            ViewGroup.LayoutParams lParams = mTextView.getLayoutParams();
            lParams.height = endHeight;
            mTextView.requestLayout();
        }
    }

    static class SavedState extends BaseSavedState {
        boolean collapsed;
        boolean alwaysShowIndicator;
        int visibleLines;
        int animDuration;


        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            collapsed = in.readByte() == 0;
            alwaysShowIndicator = in.readByte() == 0;
            visibleLines = in.readInt();
            animDuration = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeByte(collapsed ? (byte) 0 : 1);
            out.writeByte(alwaysShowIndicator ? (byte) 0 : 1);
            out.writeInt(visibleLines);
            out.writeInt(animDuration);
        }

        @Override
        public String toString() {
            return "ExpandableTextView.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " collapsed=" + collapsed + "}";
        }

        public static final Creator<SavedState> CREATOR
                = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.collapsed = isCollapsed();
        ss.alwaysShowIndicator = isAlwaysShowIndicator();
        ss.visibleLines = getVisibleLines();
        ss.animDuration = getAnimDuration();
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        mCollapsed = ss.collapsed;
        mAlwaysShowIndicator = ss.alwaysShowIndicator;
        mVisibleLines = ss.visibleLines;
        mAnimDuration = ss.animDuration;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        int height = (int) animation.getAnimatedValue();
        ViewGroup.LayoutParams lParams = mTextView.getLayoutParams();
        lParams.height = height;
        mTextView.requestLayout();
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (postInited) {
                ExpandableTextView.this.afterTextChanged();
            }
        }
    };

    private void afterTextChanged() {
        int allLines = mTextView.getLineCount();
        int visibleLines = Math.min(allLines, mVisibleLines);
        int collapseTextHeight = getLinesHeight(visibleLines);
        int expandTextHeight = getLinesHeight(allLines);
        if (mCollapseTextHeight != collapseTextHeight
                || mExpandTextHeight != expandTextHeight) {
            mCollapseTextHeight = collapseTextHeight;
            mExpandTextHeight = expandTextHeight;
            if (mAlwaysShowIndicator || visibleLines < allLines) {
                mIndicator.setVisibility(View.VISIBLE);
            } else {
                mIndicator.setVisibility(View.GONE);
            }
            if (isCollapsed()) {
                mCollapsedIndicator.setVisibility(View.VISIBLE);
                mExpandedIndicator.setVisibility(View.INVISIBLE);
                ViewGroup.LayoutParams lParams = mTextView.getLayoutParams();
                lParams.height = collapseTextHeight;
                mTextView.requestLayout();
            } else {
                mExpandedIndicator.setVisibility(View.VISIBLE);
                mCollapsedIndicator.setVisibility(View.INVISIBLE);
                ViewGroup.LayoutParams lParams = mTextView.getLayoutParams();
                lParams.height = expandTextHeight;
                mTextView.requestLayout();
            }
        }
    }

    public int getAnimDuration() {
        return mAnimDuration;
    }

    public void setAnimDuration(int animDuration) {
        mAnimDuration = Math.max(0, animDuration);
    }

    public int getVisibleLines() {
        return mVisibleLines;
    }

    public void setVisibleLines(int visibleLines) {
        mVisibleLines = Math.max(0, visibleLines);
        if (postInited) {
            afterTextChanged();
        }
    }

    public boolean isCollapsed() {
        return mCollapsed;
    }

    public void setCollapsed(boolean collapsed) {
        mCollapsed = collapsed;
        if (postInited) {
            notifyStateChanged(false);
        }
    }

    public boolean isAlwaysShowIndicator() {
        return mAlwaysShowIndicator;
    }

    public void setAlwaysShowIndicator(boolean alwaysShowIndicator) {
        mAlwaysShowIndicator = alwaysShowIndicator;
    }

    public TextView getTextView() {
        return mTextView;
    }

    public ViewGroup getIndicator() {
        return mIndicator;
    }

    public View getExpandedIndicator() {
        return mExpandedIndicator;
    }

    public View getCollapsedIndicator() {
        return mCollapsedIndicator;
    }
}

package cn.emoney.acg.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import cn.emoney.acg.R;

public class SegmentedGroup extends RadioGroup {
    
    private int DEFAULT_CHECKED_TEXT_COLOR;
    private int DEFAULT_UNCHECKED_TEXT_COLOR;
    private int DEFAULT_TEXT_SIZE;
    private int DEFAULT_BORDER_WIDTH;
    private int DEFAULT_BORDER_COLOR;
    private int DEFAULT_BORDER_RADIUS;
    private int DEFAULT_CHECKED_BG_COLOR;
    private int DEFAULT_UNCHECKED_BG_COLOR;
    
    private int checkedTextColor;
    private int uncheckedTextColor;
    private int textSize;
    private int borderWidth;
    private int borderColor;
    private int borderRadius;
    private int checkedBgColor;
    private int uncheckedBgColor;

	private Resources resources;
	private LayoutSelector mLayoutSelector;

	public SegmentedGroup(Context context) {
		super(context);
		resources = getResources();
		getDefaultValues(resources);
		mLayoutSelector = new LayoutSelector(borderRadius);
	}
	
	public SegmentedGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        resources = getResources();
        getDefaultValues(resources);
        initAttrs(attrs);
        mLayoutSelector = new LayoutSelector(borderRadius);
    }

	/* Reads the attributes from the layout */
	private void initAttrs(AttributeSet attrs) {
		TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.SegmentedGroup, 0, 0);

		try {
			checkedTextColor = typedArray.getColor(R.styleable.SegmentedGroup_sc_checked_text_color, DEFAULT_CHECKED_TEXT_COLOR);
		    uncheckedTextColor = typedArray.getColor(R.styleable.SegmentedGroup_sc_unchecked_text_color, DEFAULT_UNCHECKED_TEXT_COLOR);;
		    textSize = (int) typedArray.getDimensionPixelSize(R.styleable.SegmentedGroup_sc_text_size, DEFAULT_TEXT_SIZE);
		    borderWidth = (int) typedArray.getDimension(R.styleable.SegmentedGroup_sc_border_width, DEFAULT_BORDER_WIDTH);
		    borderColor = typedArray.getColor(R.styleable.SegmentedGroup_sc_border_color, DEFAULT_BORDER_COLOR);
		    borderRadius = (int) typedArray.getDimension(R.styleable.SegmentedGroup_sc_border_radius, DEFAULT_BORDER_RADIUS);
		    checkedBgColor = typedArray.getColor(R.styleable.SegmentedGroup_sc_checked_bg_color, DEFAULT_CHECKED_BG_COLOR);
		    uncheckedBgColor = typedArray.getColor(R.styleable.SegmentedGroup_sc_unchecked_bg_color, DEFAULT_UNCHECKED_BG_COLOR);
		} finally {
			typedArray.recycle();
		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		//Use holo light for default
		updateBackground();
	}

	/**
     * get default values
     * */
    private void getDefaultValues(Resources resources) {
        DEFAULT_CHECKED_TEXT_COLOR = resources.getColor(R.color.t8);
        DEFAULT_UNCHECKED_TEXT_COLOR = resources.getColor(R.color.c4);
        DEFAULT_TEXT_SIZE = (int) resources.getDimension(R.dimen.txt_s3);
        DEFAULT_BORDER_WIDTH = (int) resources.getDimension(R.dimen.radio_button_stroke_border);
        DEFAULT_BORDER_COLOR = resources.getColor(R.color.c4);
        DEFAULT_BORDER_RADIUS = (int) resources.getDimension(R.dimen.radio_button_conner_radius);
        DEFAULT_CHECKED_BG_COLOR = resources.getColor(R.color.c4);
        DEFAULT_UNCHECKED_BG_COLOR = resources.getColor(android.R.color.transparent);
    }

	public void updateBackground() {
		int count = super.getChildCount();
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			updateBackground(child);

			// If this is the last view, don't set LayoutParams
			if (i == count - 1) break;

			LayoutParams initParams = (LayoutParams) child.getLayoutParams();
			LayoutParams params = new LayoutParams(initParams.width, initParams.height, initParams.weight);
			// Check orientation for proper margins
			if (getOrientation() == LinearLayout.HORIZONTAL) {
				params.setMargins(0, 0, -borderWidth, 0);
			} else {
				params.setMargins(0, 0, 0, -borderWidth);
			}
			child.setLayoutParams(params);
		}
	}

	@SuppressLint("NewApi") 
	private void updateBackground(View view) {
		int checked = mLayoutSelector.getSelected();
		int unchecked = mLayoutSelector.getUnselected();
		
		//Set text color
		ColorStateList colorStateList = new ColorStateList(new int[][]{
				{-android.R.attr.state_checked},
				{android.R.attr.state_checked}},
				new int[]{uncheckedTextColor, checkedTextColor});
		((Button) view).setTextColor(colorStateList);
		((Button) view).setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

		//Redraw with tint color
		Drawable checkedDrawable = resources.getDrawable(checked).mutate();
		Drawable uncheckedDrawable = resources.getDrawable(unchecked).mutate();
		((GradientDrawable) checkedDrawable).setColor(checkedBgColor);
		((GradientDrawable) checkedDrawable).setStroke(borderWidth, borderColor);
		((GradientDrawable) uncheckedDrawable).setColor(uncheckedBgColor);
		((GradientDrawable) uncheckedDrawable).setStroke(borderWidth, borderColor);
		//Set proper radius
		((GradientDrawable) checkedDrawable).setCornerRadii(mLayoutSelector.getChildRadii(view));
		((GradientDrawable) uncheckedDrawable).setCornerRadii(mLayoutSelector.getChildRadii(view));

		//Create drawable
		StateListDrawable stateListDrawable = new StateListDrawable();
		stateListDrawable.addState(new int[]{-android.R.attr.state_checked}, uncheckedDrawable);
		stateListDrawable.addState(new int[]{android.R.attr.state_checked}, checkedDrawable);

		//Set button background
		if (Build.VERSION.SDK_INT >= 16) {
			view.setBackground(stateListDrawable);
		} else {
			view.setBackgroundDrawable(stateListDrawable);
		}
	}

	/*
	 * This class is used to provide the proper layout based on the view.
	 * Also provides the proper radius for corners.
	 * The layout is the same for each selected left/top middle or right/bottom button.
	 * float tables for setting the radius via Gradient.setCornerRadii are used instead
	 * of multiple xml drawables.
	 */
	private class LayoutSelector {

		private int children;
		private int child;
		private final int SELECTED_LAYOUT = R.drawable.segment_radio_checked;
		private final int UNSELECTED_LAYOUT = R.drawable.segment_radio_unchecked;

		private float r;    //this is the radios read by attributes or xml dimens
		private final float r1 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP
				, 0.1f, getResources().getDisplayMetrics());    //0.1 dp to px
		private final float[] rLeft;    // left radio button
		private final float[] rRight;   // right radio button
		private final float[] rMiddle;  // middle radio button
		private final float[] rDefault; // default radio button
		private final float[] rTop;     // top radio button
		private final float[] rBot;     // bot radio button
		private float[] radii;          // result radii float table
		
		

		public LayoutSelector(float cornerRadius) {
			children = -1; // Init this to force setChildRadii() to enter for the first time.
			child = -1; // Init this to force setChildRadii() to enter for the first time
			r = cornerRadius;
			rLeft = new float[]{r, r, r1, r1, r1, r1, r, r};
			rRight = new float[]{r1, r1, r, r, r, r, r1, r1};
			rMiddle = new float[]{r1, r1, r1, r1, r1, r1, r1, r1};
			rDefault = new float[]{r, r, r, r, r, r, r, r};
			rTop = new float[]{r, r, r, r, r1, r1, r1, r1};
			rBot = new float[]{r1, r1, r1, r1, r, r, r, r};
		}

		private int getChildren() {
			return SegmentedGroup.this.getChildCount();
		}

		private int getChildIndex(View view) {
			return SegmentedGroup.this.indexOfChild(view);
		}

		private void setChildRadii(int newChildren, int newChild) {

			// If same values are passed, just return. No need to update anything
			if (children == newChildren && child == newChild)
				return;

			// Set the new values
			children = newChildren;
			child = newChild;

			// if there is only one child provide the default radio button
			if (children == 1) {
				radii = rDefault;
			} else if (child == 0) { //left or top
				radii = (getOrientation() == LinearLayout.HORIZONTAL) ? rLeft : rTop;
			} else if (child == children - 1) {  //right or bottom
				radii = (getOrientation() == LinearLayout.HORIZONTAL) ? rRight : rBot;
			} else {  //middle
				radii = rMiddle;
			}
		}

		/* Returns the selected layout id based on view */
		public int getSelected() {
			return SELECTED_LAYOUT;
		}

		/* Returns the unselected layout id based on view */
		public int getUnselected() {
			return UNSELECTED_LAYOUT;
		}

		/* Returns the radii float table based on view for Gradient.setRadii()*/
		public float[] getChildRadii(View view) {
			int newChildren = getChildren();
			int newChild = getChildIndex(view);
			setChildRadii(newChildren, newChild);
			return radii;
		}
	}
}
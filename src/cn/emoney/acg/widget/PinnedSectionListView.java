/*
 * Copyright (C) 2013 Sergej Shafarenka, halfbit.de
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file kt in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.emoney.acg.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AbsListView;
import android.widget.HeaderViewListAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import cn.emoney.acg.BuildConfig;

/**
 * ListView, which is capable to pin section views at its top while the rest is
 * still scrolled.
 */
public class PinnedSectionListView extends ListView {
	private final static int RELEASE_To_REFRESH = 0;
	private final static int PULL_To_REFRESH = 1;
	private final static int REFRESHING = 2;
	private final static int DONE = 3;
	private final static int LOADING = 4;

	// 实际的padding的距离与界面上偏移距离的比例
	private final static int RATIO = 3;
	private LayoutInflater inflater;
	private LinearLayout headView;
	private TextView tipsTextview;
	private TextView lastUpdatedTextView;

	// 用于保证startY的值在一个完整的touch事件中只被记录一次
	private boolean isRecored;

	private int headContentWidth;
	private int headContentHeight;

	private int startY;
	private int firstItemIndex;

	private int state;

	private boolean isBack;

	private OnRefreshListener refreshListener;

	private boolean isRefreshable;

	// private int mVisiableLastIndex = 0; // 最后的可视项索引
	// private int mVisiableItemCount; // 当前窗口可见项总数

	private int mRefreshHeaderTitleId = 0;
	private int mRefreshHeaderSubTitleId = 0;

	public final static String REFRESHLISTVIEW_HEADER_TITLE = "refreshlistview_header_title";
	public final static String REFRESHLISTVIEW_HEADER_SUBTITLE = "refreshlistview_header_subtitle";

	// -- inner classes

	/**
	 * List adapter to be implemented for being used with PinnedSectionListView
	 * adapter.
	 */
	public static interface PinnedSectionListAdapter extends ListAdapter {
		/**
		 * This method shall return 'true' if views of given type has to be
		 * pinned.
		 */
		boolean isItemViewTypePinned(int viewType);
	}

	/** Wrapper class for pinned section view and its position in the list. */
	static class PinnedSection {
		public View view;
		public int position;
		public long id;
	}

	// -- class fields

	// fields used for handling touch events
	private final Rect mTouchRect = new Rect();
	private final PointF mTouchPoint = new PointF();
	private int mTouchSlop;
	private View mTouchTarget;
	private MotionEvent mDownEvent;

	// fields used for drawing shadow under a pinned section
	private GradientDrawable mShadowDrawable;
	private int mSectionsDistanceY;
	private int mShadowHeight;

	/** Delegating listener, can be null. */
	OnScrollListener mDelegateOnScrollListener;

	/** Shadow for being recycled, can be null. */
	PinnedSection mRecycleSection;

	/** shadow instance with a pinned view, can be null. */
	PinnedSection mPinnedSection;

	/**
	 * Pinned view Y-translation. We use it to stick pinned view to the next
	 * section.
	 */
	int mTranslateY;

	/** Scroll listener which does the magic */
	private final OnScrollListener mOnScrollListener = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (mDelegateOnScrollListener != null) { // delegate
				mDelegateOnScrollListener.onScrollStateChanged(view,
						scrollState);
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			// 下拉刷新相关
			firstItemIndex = firstVisibleItem;
			// mVisiableItemCount = visibleItemCount;
			// mVisiableLastIndex = firstVisibleItem + mVisiableItemCount - 1;

			if (mDelegateOnScrollListener != null) { // delegate
				mDelegateOnScrollListener.onScroll(view, firstVisibleItem,
						visibleItemCount, totalItemCount);
			}

			// get expected adapter or fail fast
			ListAdapter adapter = getAdapter();
			if (adapter == null || visibleItemCount == 0)
				return; // nothing to do

			final boolean isFirstVisibleItemSection = isItemViewTypePinned(
					adapter, adapter.getItemViewType(firstVisibleItem));

			if (isFirstVisibleItemSection) {
				View sectionView = getChildAt(0);
				if (sectionView.getTop() == getPaddingTop()) { // view sticks to
																// the top, no
																// need for
																// pinned shadow
					destroyPinnedShadow();
				} else { // section doesn't stick to the top, make sure we have
							// a pinned shadow
					ensureShadowForPosition(firstVisibleItem, firstVisibleItem,
							visibleItemCount);
				}

			} else { // section is not at the first visible position
				int sectionPosition = findCurrentSectionPosition(firstVisibleItem);
				if (sectionPosition > -1) { // we have section position
					ensureShadowForPosition(sectionPosition, firstVisibleItem,
							visibleItemCount);
				} else { // there is no section for the first visible item,
							// destroy shadow
					destroyPinnedShadow();
				}
			}
		};

	};

	/** Default change observer. */
	private final DataSetObserver mDataSetObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			recreatePinnedShadow();
		};

		@Override
		public void onInvalidated() {
			recreatePinnedShadow();
		}
	};

	// -- constructors

	public PinnedSectionListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	public PinnedSectionListView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}

	private void initView() {
		initResources();

		setOnScrollListener(mOnScrollListener);
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
		initShadow(true);
	}

	// -- public API methods

	public void setShadowVisible(boolean visible) {
		initShadow(visible);
		if (mPinnedSection != null) {
			View v = mPinnedSection.view;
			invalidate(v.getLeft(), v.getTop(), v.getRight(), v.getBottom()
					+ mShadowHeight);
		}
	}

	// -- pinned section drawing methods

	public void initShadow(boolean visible) {
		if (visible) {
			if (mShadowDrawable == null) {
				mShadowDrawable = new GradientDrawable(Orientation.TOP_BOTTOM,
						new int[] { Color.parseColor("#ffa0a0a0"),
								Color.parseColor("#50a0a0a0"),
								Color.parseColor("#00a0a0a0") });
				mShadowHeight = (int) (8 * getResources().getDisplayMetrics().density);
			}
		} else {
			if (mShadowDrawable != null) {
				mShadowDrawable = null;
				mShadowHeight = 0;
			}
		}
	}

	/** Create shadow wrapper with a pinned view for a view at given position */
	void createPinnedShadow(int position) {

		// try to recycle shadow
		PinnedSection pinnedShadow = mRecycleSection;
		mRecycleSection = null;

		// create new shadow, if needed
		if (pinnedShadow == null)
			pinnedShadow = new PinnedSection();
		// request new view using recycled view, if such
		View pinnedView = getAdapter().getView(position, pinnedShadow.view,
				PinnedSectionListView.this);

		// read layout parameters
		LayoutParams layoutParams = (LayoutParams) pinnedView.getLayoutParams();
		if (layoutParams == null) {
			layoutParams = (LayoutParams) generateDefaultLayoutParams();
			pinnedView.setLayoutParams(layoutParams);
		}

		int heightMode = MeasureSpec.getMode(layoutParams.height);
		int heightSize = MeasureSpec.getSize(layoutParams.height);

		if (heightMode == MeasureSpec.UNSPECIFIED)
			heightMode = MeasureSpec.EXACTLY;

		int maxHeight = getHeight() - getListPaddingTop()
				- getListPaddingBottom();
		if (heightSize > maxHeight)
			heightSize = maxHeight;

		// measure & layout
		int ws = MeasureSpec.makeMeasureSpec(getWidth() - getListPaddingLeft()
				- getListPaddingRight(), MeasureSpec.EXACTLY);
		int hs = MeasureSpec.makeMeasureSpec(heightSize, heightMode);
		pinnedView.measure(ws, hs);
		pinnedView.layout(0, 0, pinnedView.getMeasuredWidth(),
				pinnedView.getMeasuredHeight());
		mTranslateY = 0;

		// initialize pinned shadow
		pinnedShadow.view = pinnedView;
		pinnedShadow.position = position;
		pinnedShadow.id = getAdapter().getItemId(position);

		// store pinned shadow
		mPinnedSection = pinnedShadow;
	}

	/** Destroy shadow wrapper for currently pinned view */
	void destroyPinnedShadow() {
		if (mPinnedSection != null) {
			// keep shadow for being recycled later
			mRecycleSection = mPinnedSection;
			mPinnedSection = null;
		}
	}

	/** Makes sure we have an actual pinned shadow for given position. */
	void ensureShadowForPosition(int sectionPosition, int firstVisibleItem,
			int visibleItemCount) {
		if (visibleItemCount < 2) { // no need for creating shadow at all, we
									// have a single visible item
			destroyPinnedShadow();
			return;
		}

		if (mPinnedSection != null
				&& mPinnedSection.position != sectionPosition) { // invalidate
																	// shadow,
																	// if
																	// required
			destroyPinnedShadow();
		}

		if (mPinnedSection == null) { // create shadow, if empty
			createPinnedShadow(sectionPosition);
		}

		// align shadow according to next section position, if needed
		int nextPosition = sectionPosition + 1;
		if (nextPosition < getCount()) {
			int nextSectionPosition = findFirstVisibleSectionPosition(
					nextPosition, visibleItemCount
							- (nextPosition - firstVisibleItem));
			if (nextSectionPosition > -1) {
				View nextSectionView = getChildAt(nextSectionPosition
						- firstVisibleItem);
				final int bottom = mPinnedSection.view.getBottom()
						+ getPaddingTop();
				mSectionsDistanceY = nextSectionView.getTop() - bottom;
				if (mSectionsDistanceY < 0) {
					// next section overlaps pinned shadow, move it up
					mTranslateY = mSectionsDistanceY;
				} else {
					// next section does not overlap with pinned, stick to top
					mTranslateY = 0;
				}
			} else {
				// no other sections are visible, stick to top
				mTranslateY = 0;
				mSectionsDistanceY = Integer.MAX_VALUE;
			}
		}

	}

	int findFirstVisibleSectionPosition(int firstVisibleItem,
			int visibleItemCount) {
		ListAdapter adapter = getAdapter();

		int adapterDataCount = adapter.getCount();
		if (getLastVisiblePosition() >= adapterDataCount)
			return -1; // dataset has changed, no candidate

		if (firstVisibleItem + visibleItemCount >= adapterDataCount) {// added
																		// to
																		// prevent
																		// index
																		// Outofbound
																		// (in
																		// case)
			visibleItemCount = adapterDataCount - firstVisibleItem;
		}

		for (int childIndex = 0; childIndex < visibleItemCount; childIndex++) {
			int position = firstVisibleItem + childIndex;
			int viewType = adapter.getItemViewType(position);
			if (isItemViewTypePinned(adapter, viewType))
				return position;
		}
		return -1;
	}

	int findCurrentSectionPosition(int fromPosition) {
		ListAdapter adapter = getAdapter();

		if (fromPosition >= adapter.getCount())
			return -1; // dataset has changed, no candidate

		if (adapter instanceof SectionIndexer) {
			// try fast way by asking section indexer
			SectionIndexer indexer = (SectionIndexer) adapter;
			int sectionPosition = indexer.getSectionForPosition(fromPosition);
			int itemPosition = indexer.getPositionForSection(sectionPosition);
			int typeView = adapter.getItemViewType(itemPosition);
			if (isItemViewTypePinned(adapter, typeView)) {
				return itemPosition;
			} // else, no luck
		}

		// try slow way by looking through to the next section item above
		for (int position = fromPosition; position >= 0; position--) {
			int viewType = adapter.getItemViewType(position);
			if (isItemViewTypePinned(adapter, viewType))
				return position;
		}
		return -1; // no candidate found
	}

	void recreatePinnedShadow() {
		destroyPinnedShadow();
		ListAdapter adapter = getAdapter();
		if (adapter != null && adapter.getCount() > 0) {
			int firstVisiblePosition = getFirstVisiblePosition();
			int sectionPosition = findCurrentSectionPosition(firstVisiblePosition);
			if (sectionPosition == -1)
				return; // no views to pin, exit
			ensureShadowForPosition(sectionPosition, firstVisiblePosition,
					getLastVisiblePosition() - firstVisiblePosition);
		}
	}

	@Override
	public void setOnScrollListener(OnScrollListener listener) {
		if (listener == mOnScrollListener) {
			super.setOnScrollListener(listener);
		} else {
			mDelegateOnScrollListener = listener;
		}
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		super.onRestoreInstanceState(state);
		post(new Runnable() {
			@Override
			public void run() { // restore pinned view after configuration
								// change
				recreatePinnedShadow();
			}
		});
	}

	@Override
	public void setAdapter(ListAdapter adapter) {

		// assert adapter in debug mode
		if (BuildConfig.DEBUG && adapter != null) {
			if (!(adapter instanceof PinnedSectionListAdapter))
				throw new IllegalArgumentException(
						"Does your adapter implement PinnedSectionListAdapter?");
			if (adapter.getViewTypeCount() < 2)
				throw new IllegalArgumentException(
						"Does your adapter handle at least two types"
								+ " of views in getViewTypeCount() method: items and sections?");
		}

		// unregister observer at old adapter and register on new one
		ListAdapter oldAdapter = getAdapter();
		if (oldAdapter != null)
			oldAdapter.unregisterDataSetObserver(mDataSetObserver);
		if (adapter != null)
			adapter.registerDataSetObserver(mDataSetObserver);

		// destroy pinned shadow, if new adapter is not same as old one
		if (oldAdapter != adapter)
			destroyPinnedShadow();

		super.setAdapter(adapter);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (mPinnedSection != null) {
			int parentWidth = r - l - getPaddingLeft() - getPaddingRight();
			int shadowWidth = mPinnedSection.view.getWidth();
			if (parentWidth != shadowWidth) {
				recreatePinnedShadow();
			}
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);

		if (mPinnedSection != null) {

			// prepare variables
			int pLeft = getListPaddingLeft();
			int pTop = getListPaddingTop();
			View view = mPinnedSection.view;

			// draw child
			canvas.save();

			int clipHeight = view.getHeight()
					+ (mShadowDrawable == null ? 0 : Math.min(mShadowHeight,
							mSectionsDistanceY));
			canvas.clipRect(pLeft, pTop, pLeft + view.getWidth(), pTop
					+ clipHeight);

			canvas.translate(pLeft, pTop + mTranslateY);
			drawChild(canvas, mPinnedSection.view, getDrawingTime());

			if (mShadowDrawable != null && mSectionsDistanceY > 0) {
				mShadowDrawable.setBounds(mPinnedSection.view.getLeft(),
						mPinnedSection.view.getBottom(),
						mPinnedSection.view.getRight(),
						mPinnedSection.view.getBottom() + mShadowHeight);
				mShadowDrawable.draw(canvas);
			}

			canvas.restore();
		}
	}

	// -- touch handling methods

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {

		final float x = ev.getX();
		final float y = ev.getY();
		final int action = ev.getAction();

		if (action == MotionEvent.ACTION_DOWN && mTouchTarget == null
				&& mPinnedSection != null
				&& isPinnedViewTouched(mPinnedSection.view, x, y)) { // create
																		// touch
																		// target

			// user touched pinned view
			mTouchTarget = mPinnedSection.view;
			mTouchPoint.x = x;
			mTouchPoint.y = y;

			// copy down event for eventually be used later
			mDownEvent = MotionEvent.obtain(ev);
		}

		if (mTouchTarget != null) {
			if (isPinnedViewTouched(mTouchTarget, x, y)) { // forward event to
															// pinned view
				mTouchTarget.dispatchTouchEvent(ev);
			}

			if (action == MotionEvent.ACTION_UP) { // perform onClick on pinned
													// view
				super.dispatchTouchEvent(ev);
				performPinnedItemClick();
				clearTouchTarget();

			} else if (action == MotionEvent.ACTION_CANCEL) { // cancel
				clearTouchTarget();

			} else if (action == MotionEvent.ACTION_MOVE) {
				if (Math.abs(y - mTouchPoint.y) > mTouchSlop) {

					// cancel sequence on touch target
					MotionEvent event = MotionEvent.obtain(ev);
					event.setAction(MotionEvent.ACTION_CANCEL);
					mTouchTarget.dispatchTouchEvent(event);
					event.recycle();

					// provide correct sequence to super class for further
					// handling
					super.dispatchTouchEvent(mDownEvent);
					super.dispatchTouchEvent(ev);
					clearTouchTarget();

				}
			}

			return true;
		}

		// call super if this was not our pinned view
		return super.dispatchTouchEvent(ev);
	}

	private boolean isPinnedViewTouched(View view, float x, float y) {
		view.getHitRect(mTouchRect);

		// by taping top or bottom padding, the list performs on click on a
		// border item.
		// we don't add top padding here to keep behavior consistent.
		mTouchRect.top += mTranslateY;

		mTouchRect.bottom += mTranslateY + getPaddingTop();
		mTouchRect.left += getPaddingLeft();
		mTouchRect.right -= getPaddingRight();
		return mTouchRect.contains((int) x, (int) y);
	}

	private void clearTouchTarget() {
		mTouchTarget = null;
		if (mDownEvent != null) {
			mDownEvent.recycle();
			mDownEvent = null;
		}
	}

	private boolean performPinnedItemClick() {
		if (mPinnedSection == null)
			return false;

		OnItemClickListener listener = getOnItemClickListener();
		if (listener != null && getAdapter().isEnabled(mPinnedSection.position)) {
			View view = mPinnedSection.view;
			playSoundEffect(SoundEffectConstants.CLICK);
			if (view != null) {
				view.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED);
			}
			listener.onItemClick(this, view, mPinnedSection.position,
					mPinnedSection.id);
			return true;
		}
		return false;
	}

	public static boolean isItemViewTypePinned(ListAdapter adapter, int viewType) {
		if (adapter instanceof HeaderViewListAdapter) {
			adapter = ((HeaderViewListAdapter) adapter).getWrappedAdapter();
		}
		return ((PinnedSectionListAdapter) adapter)
				.isItemViewTypePinned(viewType);
	}

	private void initResources() {
		mRefreshHeaderTitleId = getContext().getResources().getIdentifier(
				REFRESHLISTVIEW_HEADER_TITLE, "id",
				getContext().getPackageName());
		mRefreshHeaderSubTitleId = getContext().getResources().getIdentifier(
				REFRESHLISTVIEW_HEADER_SUBTITLE, "id",
				getContext().getPackageName());
	}

	public void initWithHeader(int headerLayoutId) {
		inflater = LayoutInflater.from(getContext());
		headView = (LinearLayout) inflater.inflate(headerLayoutId, null);
		tipsTextview = (TextView) headView.findViewById(mRefreshHeaderTitleId);
		lastUpdatedTextView = (TextView) headView
				.findViewById(mRefreshHeaderSubTitleId);

		measureView(headView);
		headContentHeight = headView.getMeasuredHeight();
		headContentWidth = headView.getMeasuredWidth();

		headView.setPadding(0, -1 * headContentHeight, 0, 0);
		headView.invalidate();

		addHeaderView(headView, null, false);

		state = DONE;
		isRefreshable = false;
	}

	public void postRefresh() {
		state = REFRESHING;
		changeHeaderViewByState();
		onRefresh();

		// Log.v(TAG, "由松开刷新状态，到done状态");
	}

	public boolean onTouchEvent(MotionEvent event) {

		if (isRefreshable) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (firstItemIndex == 0 && !isRecored) {
					isRecored = true;
					startY = (int) event.getY();
					// Log.v(TAG, "在down时候记录当前位置‘");
				}
				break;

			case MotionEvent.ACTION_UP:
				if (state != REFRESHING && state != LOADING) {
					if (state == DONE) {
						// 什么都不做
					}
					if (state == PULL_To_REFRESH) {
						state = DONE;
						changeHeaderViewByState();

						// Log.v(TAG, "由下拉刷新状态，到done状态");
					}
					if (state == RELEASE_To_REFRESH) {
						postRefresh();
					}
				}

				isRecored = false;
				isBack = false;
				break;
			case MotionEvent.ACTION_MOVE:
				int tempY = (int) event.getY();

				if (!isRecored && firstItemIndex == 0) {
					// Log.v(TAG, "在move时候记录下位置");
					isRecored = true;
					startY = tempY;
				}

				if (state != REFRESHING && isRecored && state != LOADING) {

					// 保证在设置padding的过程中，当前的位置一直是在head，否则如果当列表超出屏幕的话，当在上推的时候，列表会同时进行滚动

					// 可以松手去刷新了
					if (state == RELEASE_To_REFRESH) {

						setSelection(0);

						// 往上推了，推到了屏幕足够掩盖head的程度，但是还没有推到全部掩盖的地步
						if (((tempY - startY) / RATIO < headContentHeight)
								&& (tempY - startY) > 0) {
							state = PULL_To_REFRESH;
							changeHeaderViewByState();

							// Log.v(TAG, "由松开刷新状态转变到下拉刷新状态");
						}
						// 一下子推到顶了
						else if (tempY - startY <= 0) {
							state = DONE;
							changeHeaderViewByState();

							// Log.v(TAG, "由松开刷新状态转变到done状态");
						}
						// 往下拉了，或者还没有上推到屏幕顶部掩盖head的地步
						else {
							// 不用进行特别的操作，只用更新paddingTop的值就行了
						}
					}
					// 还没有到达显示松开刷新的时候,DONE或者是PULL_To_REFRESH状态
					if (state == PULL_To_REFRESH) {

						setSelection(0);

						// 下拉到可以进入RELEASE_TO_REFRESH的状态
						if ((tempY - startY) / RATIO >= headContentHeight) {
							state = RELEASE_To_REFRESH;
							isBack = true;
							changeHeaderViewByState();

							// Log.v(TAG, "由done或者下拉刷新状态转变到松开刷新");
						}
						// 上推到顶了
						else if (tempY - startY <= 0) {
							state = DONE;
							changeHeaderViewByState();

							// Log.v(TAG, "由DOne或者下拉刷新状态转变到done状态");
						}
					}

					// done状态下
					if (state == DONE) {
						if (tempY - startY > 0) {
							state = PULL_To_REFRESH;
							changeHeaderViewByState();
						}
					}

					// 更新headView的size
					if (state == PULL_To_REFRESH) {
						headView.setPadding(0, -1 * headContentHeight
								+ (tempY - startY) / RATIO, 0, 0);
					}

					// 更新headView的paddingTop
					if (state == RELEASE_To_REFRESH) {
						headView.setPadding(0, (tempY - startY) / RATIO
								- headContentHeight, 0, 0);
					}

				}
				break;
			}
		}

		return super.onTouchEvent(event);
	}

	// 当状态改变时候，调用该方法，以更新界面
	private void changeHeaderViewByState() {
		switch (state) {
		case RELEASE_To_REFRESH:
			// arrowImageView.setVisibility(View.VISIBLE);
			// progressBar.setVisibility(View.GONE);
			tipsTextview.setVisibility(View.VISIBLE);
			lastUpdatedTextView.setVisibility(View.VISIBLE);

			// arrowImageView.clearAnimation();
			// arrowImageView.startAnimation(animation);

			tipsTextview.setText("松开刷新");

			// Log.v(TAG, "当前状态，松开刷新");
			break;
		case PULL_To_REFRESH:
			// progressBar.setVisibility(View.GONE);
			tipsTextview.setVisibility(View.VISIBLE);
			lastUpdatedTextView.setVisibility(View.VISIBLE);
			// arrowImageView.clearAnimation();
			// arrowImageView.setVisibility(View.VISIBLE);
			// 是由RELEASE_To_REFRESH状态转变来的
			if (isBack) {
				isBack = false;
				// arrowImageView.clearAnimation();
				// arrowImageView.startAnimation(reverseAnimation);

				tipsTextview.setText("下拉刷新");
			} else {
				tipsTextview.setText("下拉刷新");
			}
			if (refreshListener != null) {
				refreshListener.beforeRefresh();
			}
			// Log.v(TAG, "当前状态，下拉刷新");
			break;
		case REFRESHING:
			headView.setPadding(0, 0, 0, 0);
			// progressBar.setVisibility(View.VISIBLE);
			// arrowImageView.clearAnimation();
			// arrowImageView.setVisibility(View.GONE);
			tipsTextview.setText("正在刷新...");
			lastUpdatedTextView.setVisibility(View.VISIBLE);

			// Log.v(TAG, "当前状态,正在刷新...");
			break;
		case DONE:
			headView.setPadding(0, -1 * headContentHeight, 0, 0);
			// progressBar.setVisibility(View.GONE);
			// arrowImageView.clearAnimation();
			// arrowImageView.setImageResource(mRefreshHeaderHandleImg);
			tipsTextview.setText("下拉刷新");
			lastUpdatedTextView.setVisibility(View.VISIBLE);
			if (refreshListener != null) {
				refreshListener.afterRefresh();
			}
			// Log.v(TAG, "当前状态，done");
			break;
		}
	}

	public void setOnRefreshListener(OnRefreshListener refreshListener) {
		this.refreshListener = refreshListener;
		isRefreshable = true;
	}

	public interface OnRefreshListener {
		public void onRefresh();

		public void beforeRefresh();

		public void afterRefresh();
	}

	public void onRefreshFinished() {
		state = DONE;
		changeHeaderViewByState();
	}

	private void onRefresh() {
		if (refreshListener != null) {
			refreshListener.onRefresh();
		}
	}

	// 此方法直接照搬自网络上的一个下拉刷新的demo，此处是“估计”headView的width以及height
	private void measureView(View child) {
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
					MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);
	}

	public void updateRefreshDate(String dateString) {
		lastUpdatedTextView.setText(dateString);
	}

	public void updateTextColor(int color) {
		tipsTextview.setTextColor(color);
		lastUpdatedTextView.setTextColor(color);
	}

	public void updateRefreshBgColor(int color) {
		headView.setBackgroundColor(color);
	}

	public boolean isRelease2Refresh() {
		return state == RELEASE_To_REFRESH;
	}

	public boolean isPull2Refresh() {
		return state == PULL_To_REFRESH;
	}
}

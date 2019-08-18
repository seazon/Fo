package com.seazon.slidelayout;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Scroller;

import com.seazon.fo.DensityUtil;
import com.seazon.fo.R;

/**
 * 
 * @author seazon
 * 
 * @Summary
 * <br />
 * 一、框架必备组件：<br />
 * SlideActivity.java<br />
 * SlideLayout.java<br />
 * SlideLayoutListener.java<br />
 * res/drawable/slidelayout_left_shadow.xml	//显示在左侧的阴影<br />
 * res/drawable/slidelayout_right_shadow.xml	//显示在右侧的阴影<br />
 * res/layout/slidelayout_left.xml	//显示在左侧的透明布局，包含左侧阴影<br />
 * res/layout/slidelayout_right.xml	//显示在右侧的透明布局，包含右侧阴影<br />
 * <br />
 * 二、使用说明：<br />
 * 继承SlideActivity.java，并通过setSlideLayoutContentView(int c_resource, int l_resource, int r_resource)方法设置布局<br />
 * 左右两侧的布局文件无要求，中间布局要求如下：<br />
 * &lt;xxx.xxx.xxx.SlideLayout xmlns:android="http://schemas.android.com/apk/res/android"<br />
	android:layout_width="fill_parent"<br />
	android:layout_height="fill_parent"><br />
<br />
    &lt;LinearLayout	//可以是其他ViewGroup<br />	
		android:layout_width="fill_parent"<br />
        android:layout_height="fill_parent"><br />
<br />
		&lt;LinearLayout	//可以是其他ViewGroup<br />	
			android:id="@+id/slidelayoutCenterMainLayout"	//这个是必须的<br />	
	        android:layout_width="fill_parent"<br />
	        android:layout_height="fill_parent"><br />
<br />
... //your code
<br />
<br />
		&lt;/LinearLayout><br />
	&lt;/LinearLayout><br />
	&lt;/xxx.xxx.xxx.SlideLayout><br />
 *
 */
public class SlideLayout extends LinearLayout {

	public static final int SCREEN_LEFT = -1;
	public static final int SCREEN_CENTER = 0;

	public static int left_view_width;
	public static int center_view_width;
//	public static int RIGHT_VIEW_WIDTH;
	public static int min_snap_width; // 最小滑动距离
	public static final int MIN_SNAP_WIDTH_DP_VALUE = 80; // 最小滑动距离
	public static int top_height; // 顶部导航的高度，需要根据手机动态换算
	public static final int TOP_HEIGHT_DP_VALUE = 48; // 顶部导航的高度的dp值
	public static final int MAX_SIDE_WIDTH_DP_VALUE = 320;
	
	private Scroller scroller;
	
	private Context context;

	private SlideLlayoutListener listener;

	private int currentX = 0;	//当前中间view的左侧边的x值
	public int currentSide = SCREEN_CENTER; // 当前显示的侧边，左边还是右边
	public int currentScreen = SCREEN_CENTER;
	
	private final static int SNAP_VELOCITY = 600;
	private VelocityTracker mVelocityTracker;
	
	private static int touchSlop; // 这个值表示需要滑动多少距离的时候才翻到下一页
	
	private final static int TOUCH_STATE_REST = 0;
	private final static int TOUCH_STATE_SCROLLING = 1;
	private int touchState = TOUCH_STATE_REST;	// 记录触摸状态
	
	private float lastMotionX;
	private float lastMotionY;
	
	private int down = 0;
	private int move = 0;
	private int up = 0;
	
	public SlideLayout(Context context) {
		super(context);
		this.context = context;
//		Helper.d("SlideLayout1:");
		initView(context);
	}

	public SlideLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
//		Helper.d("SlideLayout2:");
		initView(context);
	}

	public SlideLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
//		Helper.d("SlideLayout3:");
		initView(context);
		
	}

//	protected void onConfigurationChanged (Configuration newConfig) 
//	{
//		Helper.d("onConfigurationChanged:");
////		onLayout(true, left, top, right, bottom);
//	}
	
	private void initView(final Context context) {
//		Helper.d("initView:");
		
		setWidthAndHeight(context);
        
		scroller = new Scroller(context);
		
		final ViewConfiguration configuration = ViewConfiguration.get(getContext());
		touchSlop = configuration.getScaledTouchSlop();

		LayoutInflater inflater = LayoutInflater.from(context);
		addView(inflater.inflate(R.layout.slidelayout_left, null));
		
		scrollToScreen(SCREEN_CENTER);
	}
	
	private void setWidthAndHeight(Context context)
	{
		DensityUtil du = new DensityUtil(context);
		DisplayMetrics dm = null;
		int max_side_width = du.dip2px(MAX_SIDE_WIDTH_DP_VALUE);
		
		dm = new DisplayMetrics();  
	    dm = context.getApplicationContext().getResources().getDisplayMetrics();
	    
	    center_view_width = dm.widthPixels;
        
		left_view_width = center_view_width * 4 / 5;
		if(left_view_width > max_side_width) {
			left_view_width = max_side_width;
		}
//        RIGHT_VIEW_WIDTH = CENTER_VIEW_WIDTH * 4 / 5;
		min_snap_width = du.dip2px(MIN_SNAP_WIDTH_DP_VALUE);
//        MIN_SNAP_WIDTH = LEFT_VIEW_WIDTH / 2;
        
        
        top_height = du.dip2px(TOP_HEIGHT_DP_VALUE);
	}
	
	public void setListener(SlideLlayoutListener listener) {
		this.listener = listener;
	}
	
//	protected void onFinishInflate() {
//		View c3 = getChildAt(1);
//		removeView(c3);
//		addView(c3);
//	}
	
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		Helper.d("onMeasure1:"+widthMeasureSpec+":"+ heightMeasureSpec);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

//		int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
//		int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
//		Helper.d("onMeasure2:"+parentWidth+":"+ parentHeight);
		   
//		final int count = getChildCount();
//		for (int i = 0; i < count; i++) {
//			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
//		}
		
		ViewGroup c1 = (ViewGroup)getChildAt(0);
		c1.measure(widthMeasureSpec, heightMeasureSpec);
//		c1.measure(parentWidth, parentHeight);
//		measureChile(c1, widthMeasureSpec, heightMeasureSpec);
//		c1.findViewById(R.id.slidelayoutLeftMainLayout).measure(widthMeasureSpec, heightMeasureSpec);
//		c1.findViewById(R.id.slidelayoutLeftShadowLayout).measure(widthMeasureSpec, heightMeasureSpec);
//		c1.findViewById(R.id.slidelayoutLeftMainLayout).measure(LEFT_VIEW_WIDTH, getHeight());
//		c1.findViewById(R.id.slidelayoutLeftShadowLayout).measure(15, getHeight());
//		Helper.d("c1 measure:"+parentWidth+":"+ parentHeight);
		ViewGroup c2 = (ViewGroup)getChildAt(1);
		c2.measure(widthMeasureSpec, heightMeasureSpec);
//		c2.measure(parentWidth, parentHeight);
//		measureChile(c2, widthMeasureSpec, heightMeasureSpec);
//		c2.findViewById(R.id.slidelayoutCenterMainLayout).measure(widthMeasureSpec, heightMeasureSpec);
//		c2.findViewById(R.id.slidelayoutCenterMainLayout).measure(CENTER_VIEW_WIDTH, getHeight());
//		Helper.d("c2 measure:"+parentWidth+":"+ parentHeight);
	}
	
//	private void measureChile(ViewGroup v, int widthMeasureSpec, int heightMeasureSpec)
//	{
//		int count = v.getChildCount();
//		for(int i=0;i<count;++i)
//		{
//			v.getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
//		}
//	}
	
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		
		setWidthAndHeight(context);
		
//		Helper.d("onLayout:"+left+":"+ top+":"+right+":"+ bottom);
		ViewGroup c1 = (ViewGroup)getChildAt(0);
		c1.setVisibility(View.VISIBLE);
//		Helper.e("c1 measure:"+(right - left)+":"+ (bottom - top));
//		c1.measure(right - left, bottom - top);
//		Helper.e("c1:layout:"+0+":"+0+":"+LEFT_VIEW_WIDTH+":"+getHeight());
		c1.layout(0, 0, left_view_width, getHeight());
//		layoutChild(c1, 0, 0, LEFT_VIEW_WIDTH, getHeight());
		c1.findViewById(R.id.slidelayoutLeftMainLayout).getLayoutParams().width = left_view_width;
//		c1.findViewById(R.id.slidelayoutLeftMainLayout).getLayoutParams().height = getHeight();
//		c1.findViewById(R.id.slidelayoutLeftShadowLayout).getLayoutParams().height = getHeight();
		
		ViewGroup c2 = (ViewGroup)getChildAt(1);
		c2.setVisibility(View.VISIBLE);
//		Helper.e("c2 measure:"+(right - left)+":"+ (bottom - top));
//		c2.measure(right - left, bottom - top);
//		Helper.e("c2:layout:"+LEFT_VIEW_WIDTH+":"+0+":"+(LEFT_VIEW_WIDTH + CENTER_VIEW_WIDTH)+":"+getHeight());
		c2.layout(left_view_width, 0, left_view_width + center_view_width, getHeight());
//		layoutChild(c2, LEFT_VIEW_WIDTH, 0, LEFT_VIEW_WIDTH + CENTER_VIEW_WIDTH, getHeight());
//		c2.measure(widthMeasureSpec, heightMeasureSpec)
//		c2.findViewById(R.id.slidelayoutCenterMainLayout).getLayoutParams().width = CENTER_VIEW_WIDTH;
//		c2.findViewById(R.id.slidelayoutCenterMainLayout).getLayoutParams().height = getHeight();

//		if(changed)
//		{
//			scrollToScreen(this.currentScreen);
//		}
	}
//	private void layoutChild(ViewGroup v, int left, int top, int right, int bottom)
//	{
//		int count = v.getChildCount();
//		for(int i=0;i<count;++i)
//		{
//			v.getChildAt(i).layout(left, top, right, bottom);
//		}
//	}
	
	private boolean isFinish = false;
	public void computeScroll() {
		if (scroller.computeScrollOffset()) {
			
			if(isFinish && (scroller.getCurrX() == scroller.getFinalX()))
			{
				if(listener!=null)
					listener.onSlideStop();
				
				isFinish=false;
			}
			
			scrollTo(scroller.getCurrX(), 0);
			postInvalidate();
		}
	}
	
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		if ((action == MotionEvent.ACTION_MOVE)
				&& (touchState != TOUCH_STATE_REST)) {
			return true;
		}

		final float x = ev.getX();
		final float y = ev.getY();

		if (this.currentScreen == SCREEN_LEFT && x < left_view_width) {
			return false;	//直接交给子层处理
		}
		if (this.currentScreen == SCREEN_LEFT && x > left_view_width) {
			return true;	//父层处理
		}
		if(this.currentScreen == SCREEN_CENTER && y < top_height)
		{
			return false;	//直接交给子层处理
		}

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			lastMotionX = x;
			lastMotionY = y;
			touchState = TOUCH_STATE_REST;
			break;
		case MotionEvent.ACTION_MOVE:
			final int xDiff = (int) Math.abs(x - lastMotionX);
			final int yDiff = (int) Math.abs(y - lastMotionY);
			boolean xMoved = (xDiff > touchSlop) && (xDiff >= 2 * yDiff);
			if (xMoved) {
				touchState = TOUCH_STATE_SCROLLING;
			}
			break;
		case MotionEvent.ACTION_UP:
			touchState = TOUCH_STATE_REST;
			break;
		}
		return touchState != TOUCH_STATE_REST;
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);

		final int action = event.getAction();
		final float x = event.getX();

		if (this.currentScreen == SCREEN_LEFT && x < left_view_width) {
			if (action == MotionEvent.ACTION_DOWN) {
				return false;
			}
		}

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			
			down = 1;
			move = 0;
			up = 0;
			
			lastMotionX = x;

			break;
		case MotionEvent.ACTION_MOVE:
			
			move++;
			
			final int deltaX = (int) (lastMotionX - x);
			lastMotionX = x;

			currentX += (-deltaX);

			changeBottomView();
			
			if(currentX >= 0)
				scrollBy((int) deltaX, 0);
			break;
		case MotionEvent.ACTION_UP:
			
			up++;
			
			if(down==1 && up==1 && move<=2)
			{
				this.scrollToScreen(SlideLayout.SCREEN_CENTER);
			}
			else
			{
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000);
				int velocityX = (int) velocityTracker.getXVelocity();
				
				changeBottomView();
				
				if (velocityX > SNAP_VELOCITY && currentScreen > SCREEN_LEFT) {
					// Log.d(TAG, "snap left");
					if (currentScreen == SCREEN_CENTER && this.currentX <= 0) {
						scrollToScreen(SCREEN_CENTER);
					} else {
						scrollToScreen(this.currentScreen - 1);
					}
//				} else if (velocityX < -SNAP_VELOCITY
//						&& currentScreen < SCREEN_RIGHT) {
//					// Log.d(TAG, "snap right");
//					if (currentScreen == SCREEN_CENTER && this.currentX >= 0) {
//						scrollToScreen(SCREEN_CENTER);
//					} else {
//						scrollToScreen(currentScreen + 1);
//					}
				} else {
					snapToDestination();
				}
			}
			
			down = 0;
			move = 0;
			up = 0;
			
			if (mVelocityTracker != null) {
				mVelocityTracker.recycle();
				mVelocityTracker = null;
			}

			touchState = TOUCH_STATE_REST;

			break;
		default:
			break;
		}
		return true;
	}
	
	private void changeBottomView() {
		if (listener != null) {
			if (currentX > 0) {
				if (currentSide != SCREEN_LEFT) {
					listener.onSlideStart(SCREEN_LEFT);
				}

//			} else if (currentX < 0) {
//				if (currentSide != SCREEN_RIGHT) {
//					listener.onSlideStart(SCREEN_RIGHT);
//				}
			}
		}
	}
	
	private void snapToDestination() {
		switch (currentScreen) {
		case SCREEN_LEFT:
			if (currentX < (left_view_width - min_snap_width)) {
				scrollToScreen(SCREEN_CENTER);
			} else {
				scrollToScreen(SCREEN_LEFT);
			}
			break;
		case SCREEN_CENTER:
			if (currentX > min_snap_width) {
				scrollToScreen(SCREEN_LEFT);
//			} else if (currentX < -MIN_SNAP_WIDTH) {
//				scrollToScreen(SCREEN_RIGHT);
			} else {
				scrollToScreen(SCREEN_CENTER);
			}
			break;
//		case SCREEN_RIGHT:
//			if (currentX > (-LEFT_VIEW_WIDTH + MIN_SNAP_WIDTH)) {
//				scrollToScreen(SCREEN_CENTER);
//			} else {
//				scrollToScreen(SCREEN_RIGHT);
//			}
//			break;
		}
	}
	
	public void scrollToScreen(int whichScreen) {
		int width = 0;
		switch (whichScreen) {
		case SCREEN_LEFT:
			width = 0;
			break;
		case SCREEN_CENTER:
			width = left_view_width;
			break;
//		case SCREEN_RIGHT:
//			width = LEFT_VIEW_WIDTH * 2;
//			break;
		}

		isFinish = true;
		
		final int delta = width - getScrollX();
		scroller.startScroll(getScrollX(), 0, delta, 0, Math.abs(delta) * 2);
		invalidate();

		currentScreen = whichScreen;
		
		switch (this.currentScreen) {
		case SCREEN_LEFT:
			currentX = left_view_width;
			break;
		case SCREEN_CENTER:
			currentX = 0;
			break;
//		case SCREEN_RIGHT:
//			currentX = -LEFT_VIEW_WIDTH;
//			break;
		}
	}

}

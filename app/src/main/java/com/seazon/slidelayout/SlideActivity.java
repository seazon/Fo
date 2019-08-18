package com.seazon.slidelayout;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.seazon.fo.activity.BaseActivity;

public class SlideActivity extends BaseActivity implements SlideLlayoutListener {

	private FrameLayout mainLayout;
	protected SlideLayout slideLayout;
	protected View leftView;
//	protected View rightView;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mainLayout = new FrameLayout(this);
		mainLayout.setBackgroundColor(Color.BLACK);
		setContentView(mainLayout);
	}

	public void setSlideLayoutContentView(int c_resource, int l_resource) {
		LayoutInflater mInflater = getLayoutInflater();

		leftView = mInflater.inflate(l_resource, null);
		

		slideLayout = (SlideLayout) mInflater.inflate(c_resource, null);
		slideLayout.setListener(this);

//		rightView = mInflater.inflate(r_resource, null);

//		mainLayout.addView(rightView);
		mainLayout.addView(leftView);
		mainLayout.addView(slideLayout); // slideLayout 必须放在最后面，使得出现在最上面
		
//		leftView.getLayoutParams().width = ;
//		MarginLayoutParams params = new MarginLayoutParams(SlideLayout.LEFT_VIEW_WIDTH, mainLayout.getHeight());
//		DensityUtil du = new DensityUtil(this);
//		params.setMargins(0, 0, du.px2dip(10), 0);
//		leftView.setLayoutParams(params);/
		leftView.getLayoutParams().width = SlideLayout.left_view_width;
//		leftView.getLayoutParams().height = mainLayout.getHeight();
//		
//		rightView.getLayoutParams().width = SlideLayout.RIGHT_VIEW_WIDTH;
//		rightView.getLayoutParams().height = mainLayout.getHeight();
		
	}

	public void onSlideStart(int showScreen) {
		switch (showScreen) {
		case SlideLayout.SCREEN_LEFT:
			leftView.setVisibility(View.VISIBLE);
			slideLayout.currentSide = SlideLayout.SCREEN_LEFT;
			break;
		case SlideLayout.SCREEN_CENTER:

			break;
//		case SlideLayout.SCREEN_RIGHT:
//			leftView.setVisibility(View.INVISIBLE);
//			slideLayout.currentSide = SlideLayout.SCREEN_RIGHT;
//			break;
		}
	}

	public void onSlideStop() {
	}

}

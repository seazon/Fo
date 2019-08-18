package com.seazon.fo.view.selector;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.view.View;

import com.seazon.fo.Core;
import com.seazon.fo.R;

public class FoSelector extends View {

	public FoSelector(Context context) {
		super(context);
	}

	public StateListDrawable setBackground(Drawable[] drawables) {
		StateListDrawable drawable = new StateListDrawable();
		drawable.addState(View.PRESSED_ENABLED_STATE_SET, drawables[2]);
		drawable.addState(View.ENABLED_FOCUSED_STATE_SET, drawables[1]);
		drawable.addState(View.ENABLED_STATE_SET, drawables[0]);
		drawable.addState(View.FOCUSED_STATE_SET, drawables[1]);
		drawable.addState(View.EMPTY_STATE_SET, drawables[0]);
		
		return drawable;
	}
	
	public static StateListDrawable actionBar(Context context) {
		Drawable[] drawables = new Drawable[3];
		
		Core core = (Core) context.getApplicationContext();
		
		int pressedResId = Color.parseColor(core.getMainPreferences().getUi_color_alpha());
		ShapeDrawable pressedShapeDrawable = new ShapeDrawable(new RectShape());
		pressedShapeDrawable.getPaint().setColor(pressedResId);
		
//		drawables[0] = context.getResources().getDrawable(R.color.transparent);	// 不设置颜色，如果设置透明色，在2.1、2.2下不正常
		drawables[1] = pressedShapeDrawable;
		drawables[2] = pressedShapeDrawable;
		
		return new FoSelector(context).setBackground(drawables);
	}
	
	public static StateListDrawable side(Context context) {
		Drawable[] drawables = new Drawable[3];
		
		Core core = (Core) context.getApplicationContext();
		
		int pressedResId = Color.parseColor(core.getMainPreferences().getUi_color_alpha());
		ShapeDrawable pressedShapeDrawable = new ShapeDrawable(new RectShape());
		pressedShapeDrawable.getPaint().setColor(pressedResId);
		
		drawables[0] = context.getResources().getDrawable(R.color.dark_bg);
		drawables[1] = pressedShapeDrawable;
		drawables[2] = pressedShapeDrawable;
		
		return new FoSelector(context).setBackground(drawables);
	}
	
	public static StateListDrawable normal(Context context) {
		Drawable[] drawables = new Drawable[3];
		
		Core core = (Core) context.getApplicationContext();
		
		int pressedResId = Color.parseColor(core.getMainPreferences().getUi_color_alpha());
		ShapeDrawable pressedShapeDrawable = new ShapeDrawable(new RectShape());
		pressedShapeDrawable.getPaint().setColor(pressedResId);
		
		Drawable enabledDrawable = null;
		if(core.isLightTheme()) {
			enabledDrawable = context.getResources().getDrawable(R.color.light_bg);
		} else {
			enabledDrawable = context.getResources().getDrawable(R.color.dark_bg);
		}
		
		drawables[0] = enabledDrawable;
		drawables[1] = pressedShapeDrawable;
		drawables[2] = pressedShapeDrawable;
		
		return new FoSelector(context).setBackground(drawables);
	}
//	public static StateListDrawable select(Context context) {
//		Drawable[] drawables = new Drawable[3];
//		
//		Core core = (Core) context.getApplicationContext();
//		
//		int pressedResId = Color.parseColor(core.getMainPreferences().getUi_color_alpha());
//		ShapeDrawable pressedShapeDrawable = new ShapeDrawable(new RectShape());
//		pressedShapeDrawable.getPaint().setColor(pressedResId);
//		
////		Drawable enabledDrawable = null;
////		if(core.isLightTheme()) {
////			enabledDrawable = context.getResources().getDrawable(R.color.light_bg);
////		} else {
////			enabledDrawable = context.getResources().getDrawable(R.color.dark_bg);
////		}
//		
//		drawables[0] = pressedShapeDrawable;
//		drawables[1] = pressedShapeDrawable;
//		drawables[2] = pressedShapeDrawable;
//		
//		return new FoSelector(context).setBackground(drawables);
//	}
	
	public static GradientDrawable select(Context context) {
		
		Core core = (Core) context.getApplicationContext();
		
		int color = Color.parseColor(core.getMainPreferences().getUi_color_alpha());
		
		GradientDrawable d = new GradientDrawable();
		d.setColor(color);
//		d.setStroke(core.du.dip2px(2), color);
//		d.setCornerRadius(core.du.dip2px(4));
		
		return d;
	}
	
//	public static Drawable select(Context context) {
//		
//		Core core = (Core) context.getApplicationContext();
//		
//		int pressedResId = Color.parseColor(core.getMainPreferences().getUi_color_alpha());
////		RectShape shape = new RectShape();
//		ShapeDrawable pressedShapeDrawable = new ShapeDrawable(new RectShape());
////		RectShape shape = new RectShape();
////		MyShapeDrawable pressedShapeDrawable = new MyShapeDrawable(shape);
//		pressedShapeDrawable.getPaint().setColor(pressedResId);
////		pressedShapeDrawable.getStrokePaint().setStrokeWidth(2);
//		
//		return pressedShapeDrawable;
//	}
//	
//	private static class MyShapeDrawable extends ShapeDrawable {  
//        //Paint.ANTI_ALIAS_FLAG代表这个画笔的图形是光滑的  
//        private Paint mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);  
//          
//        public MyShapeDrawable(Shape s) {  
//            super(s);  
//            mStrokePaint.setStyle(Paint.Style.STROKE);  
//        }  
//          
//        public Paint getStrokePaint() {  
//            return mStrokePaint;  
//        }  
//          
//        @Override protected void onDraw(Shape s, Canvas c, Paint p) {  
//            //绘制填充效果的图形  
//            s.draw(c, p);  
//            //绘制黑边  
//            s.draw(c, mStrokePaint);  
//        }  
//    }  
}

package com.seazon.fo.activity;

import java.util.Locale;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;

import com.seazon.fo.Core;
import com.seazon.fo.R;

public class BaseActivity extends Activity {

	public Core core;
	private String theme;
	
	public void onCreate(Bundle savedInstanceState) {
		core = (Core) getApplication();
		setTheme(core.getActivityTheme());
		setTheme();

		super.onCreate(savedInstanceState);
		
		if(core.getMainPreferences().isUseEn()) {
			Configuration config = new Configuration();
			config.locale = new Locale("en");
			getBaseContext().getResources().updateConfiguration(config, null);
		}
	}
	
	private void setTheme() {
		if (core.isLightTheme()) {
			theme = Core.UI_THEME_LIGHT;
		} else {
			theme = Core.UI_THEME_DARK;
		}
	}
	
	protected void onStart() {
		super.onStart();
		
		int mainColorRes = Color.parseColor(core.getMainPreferences().getUi_color());
		int mainColorARes = Color.parseColor(core.getMainPreferences().getUi_color_alpha());
		findViewById(R.id.navLine).setBackgroundColor(mainColorRes);
		findViewById(R.id.sideLine).setBackgroundColor(mainColorRes);
		
		findViewById(R.id.actionBarLine).setBackgroundColor(mainColorRes);
		findViewById(R.id.actionBar).setBackgroundColor(mainColorARes);
		
//		findViewById(R.id.selectBarLine).setBackgroundColor(mainColorRes);
//		findViewById(R.id.selectDesView).setBackgroundColor(mainColorARes);
	}
	
	public boolean isLight() {
		return Core.UI_THEME_LIGHT.equals(theme);
	}
	
	protected void onResume() {
		super.onResume();
		if (!core.getMainPreferences().getUi_theme().equals(theme)) {
			onCreate(getIntent().getExtras());
			onStart();
		}
	}
	
	public final static int MIN_TABLET_LENGTH_DP = 600;
	private Boolean isTablet = null;
	public boolean isTablet() {
		if(isTablet == null) {
			int width = getWindowManager().getDefaultDisplay().getWidth();
			int height = getWindowManager().getDefaultDisplay().getHeight();
			int length = Math.min(width, height);
			if(core.du.px2dip(length) > MIN_TABLET_LENGTH_DP) {
				isTablet = true;
				return true;
			}
			
			isTablet = false;
			return false;
		}
		
		return isTablet;
	}
}

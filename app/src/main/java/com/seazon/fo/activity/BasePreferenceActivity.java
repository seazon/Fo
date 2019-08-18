package com.seazon.fo.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Window;

import com.seazon.fo.Core;

public abstract class BasePreferenceActivity extends PreferenceActivity {
	private Core core;

	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);

		core = (Core) getApplication();
	}

	public Core getCore() {
		return core;
	}

}

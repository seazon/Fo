package com.seazon.fo.menu;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.seazon.fo.activity.FoSlideActivity;
import com.seazon.fo.listener.RefreshListener;
import com.seazon.utils.LogUtils;

public class ActionManager {

	private static ActionManager instance;

	private List<ActionConfig> configs;

	public static ActionManager getInstance(Context context) throws Exception {
		if (instance == null) {
			instance = new ActionManager(context);
		}
		return instance;
	}

	private ActionManager(Context context) throws Exception {
		configs = new ArrayList<ActionConfig>();

		InputStream is = context.getAssets().open("services.json");
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String s = null;
		while ((s = br.readLine()) != null) {
			sb.append(s).append(" ");
		}

		ActionConfig serviceConfig = null;
		JSONArray array = new JSONArray(sb.toString());
		for (int i = 0; i < array.length(); ++i) {
			JSONObject object = array.getJSONObject(i);
			serviceConfig = new ActionConfig();
			serviceConfig.setId(object.getInt("id"));
			serviceConfig.setType(object.getInt("type"));
			serviceConfig.setEnable(object.getBoolean("enable"));
			serviceConfig.setClassName(object.getString("className"));
			if (serviceConfig.isEnable())
				configs.add(serviceConfig);
		}

	}

	public int getId(String className) {
		return getActionConfig(className).getId();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public BaseAction newAction(String className, RefreshListener l, FoSlideActivity activity) {

		Class c = null;
		Constructor con = null;
		BaseAction s = null;
		ActionConfig serviceConfig = getActionConfig(className);

		try {
			c = Class.forName(serviceConfig.getClassName());
			con = c.getConstructor(int.class, int.class, RefreshListener.class, FoSlideActivity.class);
			s = (BaseAction) con.newInstance(serviceConfig.getId(), serviceConfig.getType(), l, activity);
			return s;
		} catch (Exception e) {
            LogUtils.error(e);
			return null;
		}
	}

	private ActionConfig getActionConfig(String className) {
		for (ActionConfig c : configs) {
			if (c.getClassName().equals(className)) {
				return c;
			}
		}
		return null;
	}
}

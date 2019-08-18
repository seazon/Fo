package com.seazon.fo.entity;

import com.seazon.fo.Core;

public class MainPreferences {
	private boolean showHidden;
	private boolean showThumb;
	private String order;
	private String order2;
	private String view;
	private String ui_theme;
	private String ui_color;
	private String home;
	private boolean useEn;

	public MainPreferences() {
		this.showHidden = false;
		this.showThumb = true;
		this.order = Core.ORDER_NAME;
		this.order = Core.ORDER2_ASC;
		this.view = Core.VIEW_LIST;
		this.ui_theme = Core.UI_THEME_LIGHT;
		this.ui_color = Core.UI_COLOR_GREEN;
		this.home = Core.PATH_SDCARD;
		this.useEn = false;
	}

	public boolean isShowHidden() {
		return showHidden;
	}

	public void setShowHidden(boolean showHidden) {
		this.showHidden = showHidden;
	}

	public boolean isShowThumb() {
		return showThumb;
	}

	public void setShowThumb(boolean showThumb) {
		this.showThumb = showThumb;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public String getOrder2() {
		return order2;
	}

	public void setOrder2(String order2) {
		this.order2 = order2;
	}

	public String getView() {
		return view;
	}

	public void setView(String view) {
		this.view = view;
	}

	public String getUi_theme() {
		return ui_theme;
	}

	public void setUi_theme(String ui_theme) {
		this.ui_theme = ui_theme;
	}

	public String getUi_color() {
		return ui_color;
	}

	public String getUi_color_alpha() {
		return "#" + Core.UI_COLOR_ALPHA + ui_color.substring(1);
	}

	public void setUi_color(String ui_color) {
		this.ui_color = ui_color;
	}

	public String getHome() {
		return home;
	}

	public void setHome(String home) {
		this.home = home;
	}

	public boolean isUseEn() {
		return useEn;
	}

	public void setUseEn(boolean useEn) {
		this.useEn = useEn;
	}

}

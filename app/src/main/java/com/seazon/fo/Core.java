package com.seazon.fo;

import java.io.File;
import java.util.HashMap;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Environment;

import com.seazon.fo.entity.Clipper;
import com.seazon.fo.entity.MainPreferences;
import com.seazon.fo.menu.ActionManager;
import com.seazon.utils.LogUtils;

public class Core extends Application
{
	public final static int MODE_NORMAL = 0;
	public final static int MODE_SELECT = 1;
	public final static int MODE_PASTE = 2;
	public final static int MODE_PICK = 3;
	
	public final static int BROWSER_NORMAL = 0;
	public final static int BROWSER_ZIP = 1;
	public final static int BROWSER_SEARCH = 2;
	
	public final static int OPERATION_TYPE_COPY = 1;
	public final static int OPERATION_TYPE_MOVE = 2;
	public final static int OPERATION_TYPE_DELETE = 3;

    public static String FILE_LOG_ERROR;

	public final static int SEARCH_ALL = 0;
	public final static int SEARCH_TEXT = 1;
	public final static int SEARCH_IMAGE = 2;
	public final static int SEARCH_AUDIO = 3;
	public final static int SEARCH_VIDEO = 4;
	public final static int SEARCH_APK = 5;
	
	public final static String ORDER_NAME = "Name";
	public final static String ORDER_TYPE = "Type";
	public final static String ORDER_SIZE = "Size";
	public final static String ORDER_DATEMODIFIED = "DateModified";
	
	public final static String ORDER2_ASC = "ASC";
	public final static String ORDER2_DESC = "DESC";
	
	public final static String VIEW_LIST = "List";
	public final static String VIEW_ICONS = "Icons";
	
	public final static String UI_THEME_LIGHT = "Light";
	public final static String UI_THEME_DARK = "Dark";
	
	public final static String UI_COLOR_GREEN = "#669900";
	public final static String UI_COLOR_BLUE = "#0099cc";
	public final static String UI_COLOR_ORANGE = "#ff8800";
	public final static String UI_COLOR_PINK = "#fbb9df";
	public final static String UI_COLOR_RED = "#cc0000";
	public final static String UI_COLOR_PURPLE = "#9933cc";
	
	public final static String UI_COLOR_ALPHA = "aa";
	
	public final static String PATH_SPLIT = "/";
	public final static String PATH_ROOT_STD = "/";
	public final static String PATH_SDCARD = Environment.getExternalStorageDirectory().getPath();
	public final static String PATH_DCIM = PATH_SDCARD+"/DCIM";
	public final static String PATH_DOWNLOAD = PATH_SDCARD+"/download";
	public final static String PATH_MOVIES = PATH_SDCARD+"/movies";
	public final static String PATH_MUSIC = PATH_SDCARD+"/music";
	public final static String PATH_PICTURES = PATH_SDCARD+"/pictures";
	
	public final static String PATH_FO_ROOT = PATH_SDCARD+"/.com.seazon.fo";
	public final static String PATH_FO_THUMB = PATH_FO_ROOT+"/thumb";
	public final static String PATH_FO_THUMB_2X = PATH_FO_ROOT+"/thumb2x";

	public final static String SHAREDPREFERENCES_PREFERENCES = "com.seazon.fo_preferences";
	public final static String CONFIG_FAVORITES = PATH_FO_ROOT + "/favorites.xml";
	
	public final static int ACTIONBAR_WIDTH = 60;
	public final static int ACTIONBAR_HEIGHT = 48;
	public final static int ACTIONBAR_PADDING = 12;
	public final static int ACTIONBAR_MORE_WIDTH = 220;
	
	public static HashMap<String, String> mimeTypeMap;

	private MainPreferences mainPreferences;
	private Clipper clipper;
	public int mode = Core.MODE_NORMAL;
	public int browser = Core.BROWSER_NORMAL;
	public DensityUtil du;
	public ActionManager am;
	
	public void onCreate(){
		super.onCreate();
		
		du = new DensityUtil(this);
		try {
			am = ActionManager.getInstance(this);
		} catch (Exception e) {
            LogUtils.error(e);
		}

        initPath();
	}

    @TargetApi(Build.VERSION_CODES.FROYO)
    private void initPath() {
        String logPath = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            File f = getExternalFilesDir("logs");
            if (f != null) {
                logPath = f.getPath();
                FILE_LOG_ERROR = logPath + "/error.log";
            } else {
                FILE_LOG_ERROR = PATH_FO_ROOT + "/logs/error.log";
            }
        } else {
            FILE_LOG_ERROR = PATH_FO_ROOT + "/logs/error.log";
        }
    }

    public Clipper getClipper()
	{
		if (clipper == null)
			clipper = new Clipper();

		return clipper;
	}
	
	public String getRootPath() {
//		return getMainPreferences().getRoot();	//TODO
//		return PATH_SDCARD;
		return PATH_ROOT_STD;
	}
	public String getRootName() {
		String rootPath = getRootPath();
		if(rootPath.equals(Core.PATH_ROOT_STD))
			return getRootNameStd();
		return rootPath.substring(rootPath.lastIndexOf(Core.PATH_SPLIT)+1);
	}
	public String getRootNameStd() {
		return "ROOT";	//TODO
	}
	
	public MainPreferences getMainPreferences()
	{
		if (mainPreferences == null)
		{
			mainPreferences = new MainPreferences();
			SharedPreferences preferences = getSharedPreferences(SHAREDPREFERENCES_PREFERENCES, 0);
			if (preferences != null)
			{
				mainPreferences.setShowHidden(preferences.getBoolean("setting_showhidden", false));
				mainPreferences.setShowThumb(preferences.getBoolean("setting_showthumb", true));
				mainPreferences.setOrder(preferences.getString("setting_order", Core.ORDER_NAME));
				mainPreferences.setOrder2(preferences.getString("setting_order2", Core.ORDER2_ASC));
				mainPreferences.setView(preferences.getString("setting_view", Core.VIEW_LIST));
				if(mainPreferences.getView().equals(Core.VIEW_LIST)==false
						&& mainPreferences.getView().equals(Core.VIEW_ICONS)==false
						) {
					mainPreferences.setView(Core.VIEW_LIST);
				}
				mainPreferences.setUi_theme(preferences.getString("setting_ui_theme", Core.UI_THEME_DARK));
				mainPreferences.setUi_color(preferences.getString("setting_ui_color", Core.UI_COLOR_GREEN));
				mainPreferences.setHome(preferences.getString("setting_home", PATH_SDCARD));
				mainPreferences.setUseEn(preferences.getBoolean("setting_useen", false));
				
				preferences.edit().putBoolean("setting_showhidden", mainPreferences.isShowHidden()).commit();
				preferences.edit().putBoolean("setting_showthumb", mainPreferences.isShowThumb()).commit();
				preferences.edit().putString("setting_order", mainPreferences.getOrder()).commit();
				preferences.edit().putString("setting_order2", mainPreferences.getOrder2()).commit();
				preferences.edit().putString("setting_view", mainPreferences.getView()).commit();
				preferences.edit().putString("setting_ui_theme", mainPreferences.getUi_theme()).commit();
				preferences.edit().putString("setting_ui_color", mainPreferences.getUi_color()).commit();
				preferences.edit().putString("setting_home", mainPreferences.getHome()).commit();
				preferences.edit().putBoolean("setting_useen", mainPreferences.isUseEn()).commit();
			}
		}
		
		return mainPreferences;
	}
	
	public void saveMainPreferences(MainPreferences mainPreferences)
	{
		SharedPreferences preferences = getSharedPreferences(SHAREDPREFERENCES_PREFERENCES, 0);
		if (preferences != null)
		{
			preferences.edit().putBoolean("setting_showhidden", mainPreferences.isShowHidden()).commit();
			preferences.edit().putBoolean("setting_showthumb", mainPreferences.isShowThumb()).commit();
			preferences.edit().putString("setting_order", mainPreferences.getOrder()).commit();
			preferences.edit().putString("setting_order2", mainPreferences.getOrder2()).commit();
			preferences.edit().putString("setting_view", mainPreferences.getView()).commit();
			preferences.edit().putString("setting_ui_theme", mainPreferences.getUi_theme()).commit();
			preferences.edit().putString("setting_ui_color", mainPreferences.getUi_color()).commit();
			preferences.edit().putString("setting_home", mainPreferences.getHome()).commit();
			preferences.edit().putBoolean("setting_useen", mainPreferences.isUseEn()).commit();
		}
	}
	
	public final static String SHARED_PREFS_CURRENT_PATH = "current_path";
	
	public String getCurrentPath()
	{
		SharedPreferences preferences = getSharedPreferences(Core.SHARED_PREFS_CURRENT_PATH, 0);
		if(preferences != null)
		{
			return preferences.getString("current_path", null);
		}
		
		return null;
	}
	public void saveCurrentPath(String current_path){
		SharedPreferences user = getSharedPreferences(Core.SHARED_PREFS_CURRENT_PATH, 0);
		user.edit().putString("current_path", current_path).commit();
	}
	
	public int getActivityTheme() {
		if (hasHoloTheme()) {
			switch (getBaseActivityTheme()) {
			case R.style.Dark:
				return R.style.HoloDark;
			case R.style.Light:
				return R.style.HoloLight;
			}
		}
		return getBaseActivityTheme();
	}
	public int getDialogTheme() {
		if (hasHoloTheme()) {
			switch (getBaseDialogTheme()) {
			case R.style.DarkDialog:
				return R.style.HoloDarkDialog;
			case R.style.LightDialog:
				return R.style.HoloLightDialog;
			}
		}
		return getBaseDialogTheme();
	}
	private int getBaseActivityTheme() {
		if (isLightTheme())
			return R.style.Light;
		else
			return R.style.Dark;
	}
	private int getBaseDialogTheme() {
		if (isLightTheme())
			return R.style.LightDialog;
		else
			return R.style.DarkDialog;
	}
	private boolean hasHoloTheme() {
		int version = VERSION.SDK_INT;
		if (version >= 11) {
			return true;
		}
		return false;
	}
	public boolean isLightTheme() {
		if (getMainPreferences().getUi_theme().equals(Core.UI_THEME_LIGHT))
			return true;
		return false;
	}
	
	static
	{
		mimeTypeMap = new HashMap<String, String>();
		mimeTypeMap.put("3gp", "video/3gpp");
		mimeTypeMap.put("7z", "application/x-7z-compressed");
		mimeTypeMap.put("aac", "audio/aac");
		mimeTypeMap.put("amr", "audio/*");
		mimeTypeMap.put("ape", "audio/*");
		mimeTypeMap.put("apk", "application/vnd.android.package-archive");
		mimeTypeMap.put("avi", "video/x-msvideo");
		mimeTypeMap.put("css", "text/css");
		mimeTypeMap.put("bmp", "image/bmp");
		mimeTypeMap.put("doc", "application/msword");
		mimeTypeMap.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		mimeTypeMap.put("epub", "application/epub+zip");
		mimeTypeMap.put("flac", "audio/*");
		mimeTypeMap.put("flv", "video/*");
		mimeTypeMap.put("gif", "image/gif");
		mimeTypeMap.put("gz", "application/x-gzip");
		mimeTypeMap.put("gzip", "application/x-gzip");
		mimeTypeMap.put("htm", "text/html");
		mimeTypeMap.put("html", "text/html");
		mimeTypeMap.put("jad", "text/vnd.sun.j2me.app-descriptor");
		mimeTypeMap.put("jar", "application/java-archive");
		mimeTypeMap.put("jpeg", "image/jpeg");
		mimeTypeMap.put("jpg", "image/jpeg");
		mimeTypeMap.put("log", "text/log");
		mimeTypeMap.put("m2ts", "video/*");
		mimeTypeMap.put("m4a", "audio/*");
		mimeTypeMap.put("midi", "audio/midi");
		mimeTypeMap.put("mka", "audio/*");
		mimeTypeMap.put("mkv", "video/*");
		mimeTypeMap.put("mov", "video/quicktime");
		mimeTypeMap.put("mp3", "audio/x-mpeg");
		mimeTypeMap.put("mp4", "video/mp4");
		mimeTypeMap.put("mpeg", "video/mpeg");
		mimeTypeMap.put("mpg", "video/mpeg");
		mimeTypeMap.put("ogg", "audio/*");
		mimeTypeMap.put("png", "image/png");
		mimeTypeMap.put("pdf", "application/pdf");
		mimeTypeMap.put("ppt", "application/vnd.ms-powerpoint");
		mimeTypeMap.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
		mimeTypeMap.put("rar", "application/x-rar-compressed");
		mimeTypeMap.put("rm", "audio/x-pn-realaudio");
		mimeTypeMap.put("rmvb", "audio/x-pn-realaudio");
		mimeTypeMap.put("svg", "image/svg+xml");
		mimeTypeMap.put("swf", "application/x-shockwave-flash");
		mimeTypeMap.put("tiff", "image/tiff");
		mimeTypeMap.put("ts", "video/*");
		mimeTypeMap.put("txt", "text/plain");
		mimeTypeMap.put("umd", "application/umd");
		mimeTypeMap.put("wav", "audio/x-wav");
		mimeTypeMap.put("wma", "audio/x-ms-wma");
		mimeTypeMap.put("wmv", "audio/x-ms-wmv");
		mimeTypeMap.put("xls", "application/vnd.ms-excel");
		mimeTypeMap.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		mimeTypeMap.put("xv", "video/*");
		mimeTypeMap.put("zip", "application/zip");
		mimeTypeMap.put(null, "*/*");
	}
}

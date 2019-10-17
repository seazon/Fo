package com.seazon.fo.activity;

import java.io.File;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import com.seazon.fo.BuildConfig;
import com.seazon.fo.R;
import com.seazon.fo.task.ClearCacheTask;
import com.seazon.fo.task.ClearCacheTaskCallback;
import com.seazon.utils.LogUtils;

public class MainPreferencesActivity extends BasePreferenceActivity
        implements OnPreferenceChangeListener, ClearCacheTaskCallback {
    private Dialog dialog;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.mainpreferences);

        findPreference("setting_home").setOnPreferenceChangeListener(this);
        findPreference("setting_ui_theme").setOnPreferenceChangeListener(this);
        findPreference("setting_ui_color").setOnPreferenceChangeListener(this);

        findPreference("setting_home").setSummary(getCore().getMainPreferences().getHome());

        findPreference("setting_about").setTitle("Fo v" + BuildConfig.VERSION_NAME);
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == null || preference.getKey() == null) {
        } else if (preference.getKey().equals("setting_showhidden")) {
            getCore().getMainPreferences().setShowHidden(((CheckBoxPreference) preference).isChecked());
        } else if (preference.getKey().equals("setting_showthumb")) {
            getCore().getMainPreferences().setShowThumb(((CheckBoxPreference) preference).isChecked());
            FileIconCache.clear();
        } else if (preference.getKey().equals("setting_clearcache")) {
            AlertDialog.Builder ab = new AlertDialog.Builder(this);
            ab.setTitle(R.string.common_confirm);
            ab.setMessage(R.string.setting_view_clear_cache_confirm);
            ab.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    MainPreferencesActivity.this.dialog = ProgressDialog.show(MainPreferencesActivity.this, null, getResources().getString(R.string.setting_view_clear_cache_ing));
                    ClearCacheTask task = new ClearCacheTask(MainPreferencesActivity.this);
                    task.execute();
                }
            }).setNegativeButton(android.R.string.cancel, null);
            ab.show();
        } else if (preference.getKey().equals("setting_about")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.setting_about)
                    .setMessage(getAbout())
                    .setCancelable(true);
            AlertDialog alert = builder.create();
            alert.setCanceledOnTouchOutside(true);
            alert.show();
        } else if (preference.getKey().equals("setting_feedback")) {
            try {
                Uri uri = Uri.parse("https://github.com/seazon/Fo");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            } catch (Exception e) {
                LogUtils.error(e);
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else if (preference.getKey().equals("setting_useen")) {
            boolean useEn = ((CheckBoxPreference) preference).isChecked();
            getCore().getMainPreferences().setUseEn(useEn);
            if (useEn) {
                Configuration config = new Configuration();
                config.locale = new Locale("en");
                getBaseContext().getResources().updateConfiguration(config, null);
            } else {
                Configuration config = new Configuration();
                config.locale = Locale.getDefault();
                getBaseContext().getResources().updateConfiguration(config, null);
            }
        }

        return false;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == null || preference.getKey() == null) {
            return false;
        } else if (preference.getKey().equals("setting_ui_theme")) {
            getCore().getMainPreferences().setUi_theme((String) newValue);
            return true;
        } else if (preference.getKey().equals("setting_ui_color")) {
            getCore().getMainPreferences().setUi_color((String) newValue);
            return true;
        } else if (preference.getKey().equals("setting_home")) {
            File directory = new File((String) newValue);
            if (directory.exists() && directory.isDirectory()) {
                getCore().getMainPreferences().setHome((String) directory.getPath());
                preference.setSummary(getCore().getMainPreferences().getHome());
                return true;
            } else {
                Toast.makeText(this, R.string.hint_set_home_failed_2, Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return false;
    }

	public void callback() {
		if(dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
	}
	
	private String getAbout() {
		StringBuilder sb = new StringBuilder();
		sb.append("SPECIAL THANKS:\n")
		.append("\n")
		.append("- Igor Nedoboy (русский)\n")
		.append("- Regi Mo (český)\n")
		.append("- Ouyi Su (日本語)\n")
		.append("- Matif (中文 (繁体))\n")
		.append("- Sick Skillz (Deutsch)\n")
		.append("- Lefteris Theodorogiannis (ελληνικά)\n")
		.append("- Tömör Gábor (magyar)\n")
		.append("- Piotr (polski)\n")
		.append("- Darshak Parikh (español)\n")
		.append("- Guilherme Baptista (Português (Brasil))\n")
		.append("- Faruk Can (Türk)\n")
		.append("- ( אלישיב סבח ( עברית\n")
		.append("- Björn Pelgrims (Nederlands)\n")
		.append("- Дмитро Сірик (Український)\n")
		.append("- Dat Cake (Français)\n")
		.append("- Antonio Fasano (Italiano)\n")
		.append("- Monitor Kdw (Tiếng Việt)\n")
		.append("- MUHAAB SHAAM (العربية)\n")
		.append("- Muhammad Arifur Rahman (বাংলা ভাষা)\n")
		.append("\n")
		.append("Internationalization is a difficult thing for a single developer. If you like this app and want a localized it , more convenient to use, please contact me. Mail : dxdroid@gmail.com\n");
		return sb.toString();
	}
}

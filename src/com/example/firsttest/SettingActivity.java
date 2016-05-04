package com.example.firsttest;


import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.view.Window;

public class SettingActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private EditTextPreference liuliangPreference;
	private EditTextPreference StartdayPreference;
	private EditTextPreference jiaozhengPreference;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		//requestWindowFeature(Window.FEATURE_NO_TITLE);   
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.setting);
		initPreferences();
		
	}

	
	private void initPreferences() {
		liuliangPreference = (EditTextPreference)findPreference("netflow");
		StartdayPreference = (EditTextPreference)findPreference("startday");
//		StartdayPreference.getEditText().setFilters(new InputFilter[]{
//                new InputFilter.LengthFilter(128)
//        });
		jiaozhengPreference = (EditTextPreference)findPreference("jiaozheng");
    }
    
    @Override
    protected void onResume() {
        super.onResume();

        // Setup the initial values
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        liuliangPreference.setSummary(sharedPreferences.getString("netflow", "linc"));
        StartdayPreference.setSummary(
    			"每月"+sharedPreferences.getString("startday", "1")+"日");
        
        // Set up a listener whenever a key changes
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }    
    
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("netflow")) {
        	liuliangPreference.setSummary(
                    sharedPreferences.getString(key, "20"));
        }
        if (key.equals("startday")) {
        	StartdayPreference.setSummary(
        			"每月"+sharedPreferences.getString(key, "1")+"日");
        }
	}
	
}

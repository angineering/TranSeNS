package uk.co.computicake.angela.thesis;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.app.Activity;

public class SettingsActivity extends Activity{
	
	@Override
	 protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	  
		getFragmentManager().beginTransaction().replace(android.R.id.content,
				new SettingsFragment()).commit();
	 }
	
	public static class SettingsFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);			
			addPreferencesFromResource(R.xml.preferences);
		}
	}
}

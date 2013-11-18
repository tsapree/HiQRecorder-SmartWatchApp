package pro.oneredpixel.hiqmp3smartwatchwidget;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class HiQWidgetPreferencesActivity extends PreferenceActivity {
	@Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    addPreferencesFromResource(R.xml.prefs);
	  }
}

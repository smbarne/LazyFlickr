package com.smbarne.lazyflickr;

import android.os.Bundle;
import android.view.View;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

public class PreferencesActivity extends SherlockPreferenceActivity {
	
	 @Override
     protected void onCreate(Bundle savedInstanceState) {
	      super.onCreate(savedInstanceState);
	      addPreferencesFromResource(R.xml.preferences);
	      setContentView(R.layout.extra_preferences);
	        
	      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	  }
	  
	  @Override
	  public boolean onOptionsItemSelected(MenuItem item) {
	     switch (item.getItemId()) {
	     case android.R.id.home:
	    	 finish();
	  	   	 return true;
	     default:
	         return super.onOptionsItemSelected(item);
	     }
	   }
	 
	 /**
	  * 
	  * @param view
	  */
	 public void onCacheClearClick(View view)
	 {
		 ImageLoader.getInstance().ClearCache(true, true);
		 DataLoader.getInstance().ClearCache(true, true);
	 }
}

package com.smbarne.lazyflickr;

import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

/**
 *  The MainActivity of LazyFlickr. 
 */
public class MainActivity extends ListActivity {
	
	//ListView ImageList;
	LazyFlickrViewAdapter LazyListAdapter;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);          
        setContentView(R.layout.activity_main);
        
        LazyListAdapter = new LazyFlickrViewAdapter(this);
        setListAdapter(LazyListAdapter);

        RefreshData();        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
	public void onRefreshMenuClick(final MenuItem item) {
		RefreshData();
	}  
	
	/**
	 * Reload feed data from Flickr. 
	 */
	public void RefreshData()
	{
	   FlickrDataLoader loadXMLData = new FlickrDataLoader(LazyListAdapter, this);
       loadXMLData.execute("http://api.flickr.com/services/feeds/photos_public.gne?tags=boston&format=rss_200"); 	
	}
}
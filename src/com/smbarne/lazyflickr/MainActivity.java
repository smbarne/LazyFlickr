package com.smbarne.lazyflickr;

import android.os.Bundle;
import android.app.ListActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

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
	   String[] tags = {"Boston"};
	   FlickrLoader loadXMLData = new FlickrLoader(LazyListAdapter, this);
	   loadXMLData.LoadFeed(tags);
	   //loadXMLData.LoadFeed("http://api.flickr.com/services/feeds/photos_public.gne?tags=boston&format=rss_200");
       //loadXMLData.execute("http://api.flickr.com/services/feeds/photos_public.gne?tags=boston&format=rss_200"); 	
	}
}
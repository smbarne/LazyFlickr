package com.smbarne.lazyflickr;

import android.os.Bundle;
import android.app.ListActivity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

/**
 *  The MainActivity of LazyFlickr. 
 */
public class MainActivity extends ListActivity {
	LazyListViewAdapter LazyListAdapter;
	DataLoader mDataLoader;
	ImageLoader mImageLoader;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);          
        setContentView(R.layout.activity_main);
        
        // Initialize the XML data loader and cache instance
        mDataLoader = DataLoader.getInstance();
        mDataLoader.Init(getApplicationContext());
        
        // Initialize the Image loader and cache instance
        mImageLoader = ImageLoader.getInstance();
        mImageLoader.init(getApplicationContext());

        LazyListAdapter = new LazyListViewAdapter(this, mImageLoader);
        setListAdapter(LazyListAdapter);
        
		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
			{
				LaunchPagerActivity(position);
			}
		});
		
        RefreshData();        
    }
    
    /**
     * Launch the gallery ViewPager activity centered at the position provided.
     * 
     * @param position	Integer position of the image to center the gallery on.
     */
    public void LaunchPagerActivity(int position)
    {
    	// TODO: adaptable tags from user input
    	String[] tags = {"Boston"};
    	
    	Intent intent = new Intent(this, PagerActivity.class);
    	intent.putExtra("tags", tags);
    	intent.putExtra("Position", position);
		startActivity(intent);
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
    	// TODO: adaptable tags from user input		
	   String[] tags = {"Boston"};
	   mDataLoader.LoadFeed(tags, LazyListAdapter, null, this);	
	}
}
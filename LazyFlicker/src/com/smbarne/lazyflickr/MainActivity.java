package com.smbarne.lazyflickr;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.smbarne.lazyflickr.R.anim;
import com.smbarne.lazyflickr.R.id;

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;

/**
 *  The MainActivity of LazyFlickr. 
 */
public class MainActivity extends SherlockListActivity {
	LazyListViewAdapter LazyListAdapter;
	DataLoader mDataLoader;
	ImageLoader mImageLoader;
	MenuItem mRefreshItem;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);         
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
    	MenuInflater inflater = getSupportMenuInflater();
    	inflater.inflate(R.menu.activity_main, (Menu) menu);
    	return super.onCreateOptionsMenu(menu);
    }
    
	public void onRefreshMenuClick(final MenuItem item) {
		if (item.getItemId() == id.refresh)
            mRefreshItem = item;
		RefreshData();
	}  
	
	/**
	 * Reload feed data from Flickr. 
	 */
	public void RefreshData()
	{
		// Note: To be done after adding ActionBarSherlock or ActionBarCompatability
    	//MenuItem refresh = (MenuItem)getSupportActionBar().getCustomView().findViewById(R.id.refresh);

		
    	// TODO: adaptable tags from user input		
        String[] tags = {"Boston"};
        mDataLoader.LoadFeed(tags, LazyListAdapter, null, mRefreshItem);	
	}
}
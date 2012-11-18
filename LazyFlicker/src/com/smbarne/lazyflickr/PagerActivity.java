package com.smbarne.lazyflickr;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import android.os.Bundle;
import android.support.v4.view.ViewPager;

public class PagerActivity extends SherlockActivity {
	LazyViewPagerAdapter LazyPagerAdapter;
	DataLoader mDataLoader;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pager_activity);

        mDataLoader = DataLoader.getInstance();
        String[] tags = getIntent().getStringArrayExtra("tags");
        int position = getIntent().getIntExtra("Position", 0);
        ImageLoader imageLoader = ImageLoader.getInstance();        
        
        LazyPagerAdapter = new LazyViewPagerAdapter(this, imageLoader);
        mDataLoader.LoadFeed(tags, null, LazyPagerAdapter, null);
        
        ViewPager vp = (ViewPager)findViewById(R.id.pager);
        vp.setAdapter(LazyPagerAdapter);
        vp.setCurrentItem(position);
        
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
}
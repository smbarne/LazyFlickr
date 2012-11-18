package com.smbarne.lazyflickr;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

public class PagerActivity extends Activity {
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
        mDataLoader.LoadFeed(tags, null, LazyPagerAdapter, this);
        
        ViewPager vp = (ViewPager)findViewById(R.id.pager);
        vp.setAdapter(LazyPagerAdapter);
        vp.setCurrentItem(position);
    }
}
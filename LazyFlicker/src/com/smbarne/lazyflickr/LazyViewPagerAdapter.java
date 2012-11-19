package com.smbarne.lazyflickr;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

public class LazyViewPagerAdapter extends PagerAdapter  {
	
    private Activity mActivity;    
    private ArrayList<FlickrItem> mData = new ArrayList<FlickrItem>();
    private final ImageLoader mImageLoader;
    
    public LazyViewPagerAdapter(Activity activity, ImageLoader imageLoader)
    {
    	mActivity = activity;
    	mImageLoader = imageLoader;
    }
    
    public Activity getActivity()
    {
    	return mActivity;
    }
    
    @Override
    public int getCount()
    {
        return mData.size();
    }
    
	public Object instantiateItem(View view, int position) {
		View inflatedView = null;
    	LayoutInflater inflater = (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	inflatedView = inflater.inflate(R.layout.flickr_pager_item, null);        

        ImageView iv = (ImageView)inflatedView.findViewById(R.id.pager_item_imageview);
        if (iv != null)
        	mImageLoader.LoadImage(mData.get(position).getImageURL(), iv);
        
		((ViewPager) view).addView(inflatedView, 0);
        return inflatedView;
	}

	@Override
	public boolean isViewFromObject(View view, Object obj) {
		return view.equals(obj);
	}
	
	@Override
	public void destroyItem(View container, int position, Object object) {
		((ViewPager) container).removeView((View) object);
	}

	@Override
	public void finishUpdate(View container) {
	}
	
	@Override
	public void restoreState(Parcelable state, ClassLoader loader) {
	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void startUpdate(View container) {
	}
	
    public void setData(ArrayList<FlickrItem> data)
    {
    	mData = data;
    	notifyDataSetChanged();
    }
    
    public void clearData()
    {
    	mData.clear();
    	notifyDataSetChanged();
    }
}

package com.smbarne.lazyflickr;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 *  An adapter that converts a list of {@link FlickrItem}s for display
 *  in a ListView. 
 */
public class LazyFlickrViewAdapter extends BaseAdapter {
    
    private Activity mActivity;
    
    // TODO: populate this from SD cache if available
    private ArrayList<FlickrItem> mData = new ArrayList<FlickrItem>();
    
    private final ImageLoader mImageLoader;
    
    public LazyFlickrViewAdapter(Activity activity)
    {
    	mActivity = activity;
    	mImageLoader = new ImageLoader(activity.getApplicationContext());
    }

    public int getCount()
    {
        return mData.size();
    }

    public Object getItem(int position)
    {
        return position;
    }

    public long getItemId(int position)
    {
        return position;
    }
    
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View inflatedView = convertView;
        if(convertView==null)
        {
        	LayoutInflater inflater = (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        	inflatedView = inflater.inflate(R.layout.flickr_list_item, null);
        }

        TextView title = (TextView)inflatedView.findViewById(R.id.flickrTitle);
        title.setText(mData.get(position).getTitle());
        title.setTag(mData.get(position).getThumbURL());
        
        mImageLoader.LoadImage(mData.get(position).getThumbURL(), title);
        
        return inflatedView;
    }
    
    public ArrayList<FlickrItem> GetData()
    {
    	return mData;
    }

	// TODO
    public void appendData(ArrayList<FlickrItem> data)
    {
    	notifyDataSetChanged();
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
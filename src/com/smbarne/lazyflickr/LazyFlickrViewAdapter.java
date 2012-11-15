package com.smbarne.lazyflickr;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class LazyFlickrViewAdapter extends BaseAdapter {
    
    private Activity HostActivity;
    private String[] Data;
    
    public LazyFlickrViewAdapter(Activity activity, String[] data)
    {
    	HostActivity = activity;
        Data = data;
    }

    public int getCount()
    {
        return Data.length;
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
        	LayoutInflater inflater = (LayoutInflater)HostActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        	inflatedView = inflater.inflate(R.layout.flickr_list_item, null);
        }

        TextView title = (TextView)inflatedView.findViewById(R.id.flickrTitle);
        title.setText(Data[position]);
        
        Drawable thumbnail = HostActivity.getResources().getDrawable(R.drawable.image_large);
        title.setCompoundDrawablesWithIntrinsicBounds(thumbnail , null, null, null);
        
        return inflatedView;
    }
}
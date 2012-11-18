package com.smbarne.lazyflickr;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.actionbarsherlock.view.MenuItem;
import com.smbarne.lazyflickr.R.anim;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Adapter;
import android.widget.ImageView;


/**
 * The FlickrLoader class facilitates storing and loading Flickr data from the Flickr Public
 * API.  Use the LoadFeed() function to load data from storage and from the internet if
 * available.
 *
 */
public class DataLoader implements Serializable	
{
	private static final long serialVersionUID = -5065727408815846209L;
    private File cacheDir;
    private HashMap<String, ArrayList<FlickrItem>> mMemoryCache = new HashMap<String, ArrayList<FlickrItem>>();
	private volatile static DataLoader instance;
	private Context mContext;

	public static DataLoader getInstance() {
		if (instance == null) {
			synchronized (ImageLoader.class) {
				if (instance == null) {
					instance = new DataLoader();
				}
			}
		}
		return instance;
	}
	
	protected DataLoader()
	{
		
	}
	
	public void Init(Context context) {	
		// Find or Create a directory to save cached feeds
		mContext = context;
		cacheDir = Utilities.GetOrCreateCacheDir(mContext, "data/lazyflickr/feeds");
	}
	
	/**
	 * Initiate loading Flickr feed data.  Previously fetched data that matches the tags passed in
	 * will be initially populated to the mAdapter's data.  After that, data will be pulled from
	 * the web and appended to the adapter.
	 * 
	 * @param tags	An array of tags to search query Flickr for.
	 */
	public void LoadFeed(String[] tags, Adapter adapter, PagerAdapter pagerAdapter, MenuItem refreshItem)
	{
		XMLDataLoader asyncWebLoad = null;
		String tagstream = Utilities.StringArrayToCSV(tags);	
	  	File f = new File(cacheDir, tagstream);
		
		if (mMemoryCache.containsKey(tagstream))
			asyncWebLoad = new XMLDataLoader(adapter, pagerAdapter, refreshItem, mMemoryCache.get(tagstream));
		else
		{
			ArrayList<FlickrItem> sd_items =  Utilities.deserializeFlickrItems(mContext, f);
			if (sd_items.size() > 0)
				asyncWebLoad = new XMLDataLoader(adapter, pagerAdapter, refreshItem, sd_items);				
			else
				asyncWebLoad = new XMLDataLoader(adapter, pagerAdapter, refreshItem, null);
		}
		
		asyncWebLoad.execute(tags);
	}

	/**
	 *  An asynchronous task that loads XML data from a list of Flickr RSS 2.0 feed URLs.  Upon
	 *  completion, the task will update a LazyFlickrViewAdapter with a list of FlickrItems 
	 *  processed from the XML feed.
	 *
	 */
	public class XMLDataLoader extends AsyncTask<String, Integer, ArrayList<FlickrItem>> {
		private final Adapter mAdapter;
		private final PagerAdapter mPagerAdapter;
		private final MenuItem mRefreshItem;
		ArrayList<FlickrItem> mItems;
		private String mTagStream = "";
	
		public XMLDataLoader(Adapter adapter, PagerAdapter pagerAdapter, MenuItem refreshItem, ArrayList<FlickrItem> items) {
			mAdapter = adapter;
			mPagerAdapter = pagerAdapter;
			mRefreshItem = refreshItem;
			mItems = items;
		}
		
		/**
		 * Apply data (items) to any available adapters.
		 * 
		 * @param items	Data to apply
		 */
		private void setData(ArrayList<FlickrItem> items)
		{
			if (mAdapter instanceof LazyListViewAdapter)
			{
				LazyListViewAdapter llva = (LazyListViewAdapter)mAdapter;
				if (llva != null)
					llva.setData(items);
			}
			
			if (mPagerAdapter instanceof LazyViewPagerAdapter)
			{
				LazyViewPagerAdapter lvpa = (LazyViewPagerAdapter)mPagerAdapter;
				if (lvpa != null)
					lvpa.setData(items);
			}
		}
		
	    protected void onPreExecute() {
	    	// If there is a refresh UI element, start it spinning
	    	if (mRefreshItem != null)
	    	{
	    		LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		        ImageView iv = (ImageView) inflater.inflate(R.layout.refresh_action_view, null);
		
		        Animation rotation = AnimationUtils.loadAnimation(mContext, anim.refresh_anim);
		        rotation.setRepeatCount(Animation.INFINITE);
		        iv.startAnimation(rotation);
		
		        mRefreshItem.setActionView(iv);
	    	}
	    	
	    	if (mItems != null)
	    		setData(mItems);
	    }
			
		protected void onPostExecute(ArrayList<FlickrItem> items)
		{
			if (items != null)
			{
				setData(mItems);
				
				if (mTagStream != "")
				{
					mMemoryCache.put(mTagStream, items);
			
		    	  	File f = new File(cacheDir, mTagStream);
					Utilities.writeFlickrItemListToFile(items, f);					
				}
			}
			
			// If there is a refresh UI element, stop it spinning
	        if (mRefreshItem != null && mRefreshItem.getActionView() != null) {
	            mRefreshItem.getActionView().clearAnimation();
	            mRefreshItem.setActionView(null);
	        }
		}
	
		@Override
		protected ArrayList<FlickrItem> doInBackground(String... tags)
		{
			 ArrayList<FlickrItem> items = null; 
			 if (mItems == null)
				items = new ArrayList<FlickrItem>();
			 else
				items = mItems;
				
        	 XMLParser parser = new XMLParser();
        	 mTagStream = Utilities.StringArrayToCSV(tags);
    	 
    		 // Attempt HTTP connection to feed
    		 String url = "http://api.flickr.com/services/feeds/photos_public.gne?tags="
    				 + mTagStream + "&format=rss_200";
        	 String xml = parser.getXmlFromUrl(url);
        	 if (xml == null || xml.length() == 0)
        		 return null;
        	         	 
        	 // Create document from XML
        	 Document doc = parser.getDomElement(xml);
        	  
        	 // Create a list of nodes from the <item> tag
        	 NodeList itemList = doc.getElementsByTagName(FlickrItem.KEY_ITEM);        	  
        	 for (int i = 0; i < itemList.getLength(); i++)
        	 {
        		 Element e = (Element) itemList.item(i);
        		 
        		 String guid     = parser.getValue(e, FlickrItem.KEY_GUID);
        		 
        		 if (!items.contains(new FlickrItem(guid, "", "", "")))
        		 {
	        	     String title 	 = parser.getValue(e, FlickrItem.KEY_TITLE); 
	        	     String thumbURL = parser.getAttributeValue(e, FlickrItem.KEY_THUMB, 
	        	    		 FlickrItem.KEY_IMAGE_ATTRIBUTE);
	        	     String imageURL = parser.getAttributeValue(e, FlickrItem.KEY_IMAGE,
	        	    		 FlickrItem.KEY_IMAGE_ATTRIBUTE);
	        	     
	        	     // Create a new FlickrItem from the processed XML
	        	     items.add(new FlickrItem(guid, title, thumbURL, imageURL));       
        		 }
        	     publishProgress((int) ((i / (float) itemList.getLength()) * 100));
        	 }
	 
			 return items;
		}
	}
}
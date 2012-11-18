package com.smbarne.lazyflickr;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.view.PagerAdapter;
import android.widget.Adapter;


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
		cacheDir = Utilities.GetOrCreateCacheDir(context, "data/lazyflickr/feeds");
	}
	
	/**
	 * Initiate loading Flickr feed data.  Previously fetched data that matches the tags passed in
	 * will be initially populated to the mAdapter's data.  After that, data will be pulled from
	 * the web and appended to the adapter.
	 * 
	 * @param tags	An array of tags to search query Flickr for.
	 */
	public void LoadFeed(String[] tags, Adapter adapter, PagerAdapter pagerAdapter, Activity activity)
	{
		XMLDataLoader asyncWebLoad = null;
		String tagstream = Utilities.StringArrayToCSV(tags);	
	  	File f = new File(cacheDir, tagstream);
		
		if (mMemoryCache.containsKey(tagstream))
			asyncWebLoad = new XMLDataLoader(adapter, pagerAdapter, activity, mMemoryCache.get(tagstream));
		else
		{
			ArrayList<FlickrItem> sd_items =  Utilities.deserializeFlickrItems(activity.getApplicationContext(), f);
			if (sd_items.size() > 0)
				asyncWebLoad = new XMLDataLoader(adapter, pagerAdapter, activity, sd_items);				
			else
				asyncWebLoad = new XMLDataLoader(adapter, pagerAdapter, activity, null);
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
		private final Activity mActivity;
		ArrayList<FlickrItem> mItems;
		private String mTagStream = "";
	
		public XMLDataLoader(Adapter adapter, PagerAdapter pagerAdapter, Activity activity, ArrayList<FlickrItem> items) {
			mAdapter = adapter;
			mPagerAdapter = pagerAdapter;
			mActivity = activity;
			mItems = items;
		}
		
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
	    	mActivity.setProgressBarIndeterminateVisibility(true);
	    	
	    	if (mItems != null)
	    		setData(mItems);
	    	    	
			// Note: To be done after adding ActionBarSherlock or ActionBarCompatability
	    	//MenuItem refresh = (MenuItem)mActivity.getActionBar().getCustomView().findViewById(R.id.refresh);
			//refresh.setVisible(false);
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
			
			mActivity.setProgressBarIndeterminateVisibility(false);
			
			// Note: To be done after adding ActionBarSherlock or ActionBarCompatability
			//MenuItem refresh = (MenuItem)mActivity.getActionBar().getCustomView().findViewById(R.id.refresh);
			//refresh.setVisible(true);
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
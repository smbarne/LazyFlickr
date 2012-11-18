package com.smbarne.lazyflickr;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.os.AsyncTask;


/**
 * The FlickrLoader class facilitates storing and loading Flickr data from the Flickr Public
 * API.  Use the LoadFeed() function to load data from storage and from the internet if
 * available.
 *
 */
public class FlickrLoader	
{
	private final LazyFlickrViewAdapter mAdapter;
	private final Activity mActivity;
	
    private File cacheDir;
    private HashMap<String, ArrayList<FlickrItem>> mMemoryCache = new HashMap<String, ArrayList<FlickrItem>>(); 
	

	public FlickrLoader(LazyFlickrViewAdapter adapter, Activity activity) {
		mAdapter = adapter;
		mActivity = activity;
		
		// Find or Create a directory to save cached feeds
		cacheDir = Utilities.GetOrCreateCacheDir(mActivity.getApplicationContext(), "data/lazyflickr/feeds");
	}
	
	/**
	 * Initiate loading Flickr feed data.  Previously fetched data that matches the tags passed in
	 * will be initially populated to the mAdapter's data.  After that, data will be pulled from
	 * the web and appended to the adapter.
	 * 
	 * @param tags	An array of tags to search query Flickr for.
	 */
	public void LoadFeed(String[] tags)
	{
		FlickrWebLoader asyncWebLoad = null;
		String tagstream = Utilities.StringArrayToCSV(tags);	
	  	File f = new File(cacheDir, tagstream);
		
		if (mMemoryCache.containsKey(tagstream))
			asyncWebLoad = new FlickrWebLoader(mAdapter, mActivity, mMemoryCache.get(tagstream));
		else
		{
			ArrayList<FlickrItem> sd_items =  Utilities.deserializeFlickrItems(mActivity.getApplicationContext(), f);
			if (sd_items.size() > 0)
				asyncWebLoad = new FlickrWebLoader(mAdapter, mActivity, sd_items);				
			else
				asyncWebLoad = new FlickrWebLoader(mAdapter, mActivity, null);
		}
		
		asyncWebLoad.execute(tags);
	}

	/**
	 *  An asynchronous task that loads XML data from a list of Flickr RSS 2.0 feed URLs.  Upon
	 *  completion, the task will update a LazyFlickrViewAdapter with a list of FlickrItems 
	 *  processed from the XML feed.
	 *
	 */
	public class FlickrWebLoader extends AsyncTask<String, Integer, ArrayList<FlickrItem>> {
		private final LazyFlickrViewAdapter mAdapter;
		private final Activity mActivity;
		ArrayList<FlickrItem> mItems;
		private String mTagStream = "";
	
		public FlickrWebLoader(LazyFlickrViewAdapter adapter, Activity activity, ArrayList<FlickrItem> items) {
			mAdapter = adapter;
			mActivity = activity;
			mItems = items;
		}
		
	    protected void onPreExecute() {
	    	mActivity.setProgressBarIndeterminateVisibility(true);
	    	
	    	if (mItems != null)
				mAdapter.setData(mItems);
	    	    	
			// Note: To be done after adding ActionBarSherlock or ActionBarCompatability
	    	//MenuItem refresh = (MenuItem)mActivity.getActionBar().getCustomView().findViewById(R.id.refresh);
			//refresh.setVisible(false);
	    }
			
		protected void onPostExecute(ArrayList<FlickrItem> items)
		{
			if (items != null)
			{
				mAdapter.setData(items);
				
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
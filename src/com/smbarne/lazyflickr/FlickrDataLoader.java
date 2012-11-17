package com.smbarne.lazyflickr;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.os.AsyncTask;

/**
 *  An asynchronous task that loads XML data from a list of Flickr RSS 2.0 feed URLs.  Upon
 *  completion, the task will update a LazyFlickrViewAdapter with a list of FlickrItems 
 *  processed from the XML feed.
 *
 */
public class FlickrDataLoader extends AsyncTask<String, Integer, ArrayList<FlickrItem>> {
	private final LazyFlickrViewAdapter mAdapter;
	private final Activity mActivity;

	public FlickrDataLoader(LazyFlickrViewAdapter adapter, Activity activity) {
		mAdapter = adapter;
		mActivity = activity;
	}
	
    protected void onPreExecute() {
    	mActivity.setProgressBarIndeterminateVisibility(true);
    	
		// Note: To be done after adding ActionBarSherlock or ActionBarCompatability
    	//MenuItem refresh = (MenuItem)mActivity.getActionBar().getCustomView().findViewById(R.id.refresh);
		//refresh.setVisible(false);
    }
		
	protected void onPostExecute(ArrayList<FlickrItem> items)
	{
		// TODO: Append items instead of replacing them
		if (items != null)
			mAdapter.setData(items);
		
		mActivity.setProgressBarIndeterminateVisibility(false);
		
		// Note: To be done after adding ActionBarSherlock or ActionBarCompatability
		//MenuItem refresh = (MenuItem)mActivity.getActionBar().getCustomView().findViewById(R.id.refresh);
		//refresh.setVisible(true);
	}

	@Override
	protected ArrayList<FlickrItem> doInBackground(String... urls)
	{
			 ArrayList<FlickrItem> items = new ArrayList<FlickrItem>();
        	 XMLParser parser = new XMLParser();
        	 
        	 // Process all URLs
        	 for (int x=0; x<urls.length; x++)
        	 {
        		 // Attempt HTTP connection to feed
	        	 String xml = parser.getXmlFromUrl(urls[0]);
	        	 if (xml == null || xml.length() == 0)
	        		 break;
	        	 
	        	 // Create document from XML
	        	 Document doc = parser.getDomElement(xml);
	        	  
	        	 // Create a list of nodes from the <item> tag
	        	 NodeList itemList = doc.getElementsByTagName(FlickrItem.KEY_ITEM);        	  
	        	 for (int i = 0; i < itemList.getLength(); i++)
	        	 {
	        		 Element e = (Element) itemList.item(i);
	        		 
	        	     String title 	 = parser.getValue(e, FlickrItem.KEY_TITLE); 
	        	     String guid     = parser.getValue(e, FlickrItem.KEY_GUID);
	        	     String thumbURL = parser.getAttributeValue(e, FlickrItem.KEY_THUMB, 
	        	    		 FlickrItem.KEY_IMAGE_ATTRIBUTE);
	        	     String imageURL = parser.getAttributeValue(e, FlickrItem.KEY_IMAGE,
	        	    		 FlickrItem.KEY_IMAGE_ATTRIBUTE);
	        	     
	        	     // Create a new FlickrItem from the processed XML
	        	     items.add(new FlickrItem(guid, title, thumbURL, imageURL));        	     
	        	     publishProgress((int) ((i / (float) itemList.getLength()) * 100));
	        	 }
        	 }
        	 
		return items;
	}
}
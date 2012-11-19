package com.smbarne.lazyflickr;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

/**
 *  An object containing basic information for a Flickr Image Item.
 *
 */
public class FlickrItem implements Serializable {
    
	/**
	 * An automatically generated Serialization UID
	 */
	private static final long serialVersionUID = 8649153866448476977L;
	
	// XML node keys
	public static final String KEY_ITEM = "item";
    public static final String KEY_GUID = "guid";
    public static final String KEY_PUBDATE = "pubDate";
    public static final String KEY_TITLE = "media:title";   // attribute "url"
    public static final String KEY_THUMB = "media:thumbnail";
    public static final String KEY_IMAGE = "media:content"; // attribute "url"
    public static final String KEY_IMAGE_ATTRIBUTE = "url";
	
	private String mGUID;
	private String mTitle;
	private String mThumbURL;
	private String mImageURL;
	private Date   mPubDate;
	
	FlickrItem(String guid, String title, Date pubDate, String thumbURL, String imageURL)
	{
		super();
		mGUID = guid;
		mTitle = title;
		mPubDate = pubDate;
		mThumbURL = thumbURL;
		mImageURL = imageURL;
	}
	
	FlickrItem()
	{
		this ("", "", null, "", "");
	}

	public String getGUID() {
		return mGUID;
	}

	public void setGUID(String gUID) {
		mGUID = gUID;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		mTitle = title;
	}

	public String getThumbURL() {
		return mThumbURL;
	}

	public void setThumbURL(String thumbURL) {
		mThumbURL = thumbURL;
	}

	public String getImageURL() {
		return mImageURL;
	}

	public void setImageURL(String imageURL) {
		mImageURL = imageURL;
	}
	
	@Override
	public boolean equals(Object other){
	    if (other == null) return false;
	    if (other == this) return true;
	    if (!(other instanceof FlickrItem))return false;
	    
	    return this.mGUID.equals(((FlickrItem)other).getGUID());
	}

	public Date getPubDate() {
		return mPubDate;
	}

	public void setPubDate(Date mPubDate) {
		this.mPubDate = mPubDate;
	}
	
	public static class FlickrItemComparable implements Comparator<FlickrItem>{
		 
	    @Override
	    public int compare(FlickrItem f1, FlickrItem f2) {
	    	if (f1.getPubDate().after(f2.getPubDate()))
	    		return -1;
	    	else 
	    		return 1;
	    }
	}	
}
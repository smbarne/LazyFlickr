package com.smbarne.lazyflickr;

/**
 *  An object containing basic information for a Flickr Image Item.
 *
 */
public class FlickrItem {
    // XML node keys
	public static final String KEY_ITEM = "item";
    public static final String KEY_GUID = "guid";
    public static final String KEY_TITLE = "media:title";   // attribute "url"
    public static final String KEY_THUMB = "media:thumbnail";
    public static final String KEY_IMAGE = "media:content"; // attribute "url"
    public static final String KEY_IMAGE_ATTRIBUTE = "url";
	
	private String GUID;
	private String Title;
	private String ThumbURL;
	private String ImageURL;
	
	FlickrItem(String guid, String title, String thumbURL, String imageURL)
	{
		GUID = guid;
		Title = title;
		ThumbURL = thumbURL;
		ImageURL = imageURL;
	}

	public String getGUID() {
		return GUID;
	}

	public void setGUID(String gUID) {
		GUID = gUID;
	}

	public String getTitle() {
		return Title;
	}

	public void setTitle(String title) {
		Title = title;
	}

	public String getThumbURL() {
		return ThumbURL;
	}

	public void setThumbURL(String thumbURL) {
		ThumbURL = thumbURL;
	}

	public String getImageURL() {
		return ImageURL;
	}

	public void setImageURL(String imageURL) {
		ImageURL = imageURL;
	}
	
	@Override
	public boolean equals(Object other){
	    if (other == null) return false;
	    if (other == this) return true;
	    if (!(other instanceof FlickrItem))return false;
	    return ((FlickrItem)other).getGUID() == this.GUID;	    
	}
}
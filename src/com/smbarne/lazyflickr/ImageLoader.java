package com.smbarne.lazyflickr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.TextView;

/**
 *  A lazy loading image loader with both memory and file based caching.
 *
 */
public class ImageLoader {
	
	private final int ThreadCount = 5;
    private File cacheDir;
    private HashMap<String, Bitmap> mMemoryCache = new HashMap<String, Bitmap>(); 
    ExecutorService mThreadPool;
	
	ImageLoader(Context context)
	{
		mThreadPool = Executors.newFixedThreadPool(ThreadCount);

	    // Find or Create a directory to save cached images
	    String sdState = android.os.Environment.getExternalStorageState();
	    if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
	      File sdDir = android.os.Environment.getExternalStorageDirectory();    
	      cacheDir = new File(sdDir,"data/lazyflickr/images");
	    }
	    else
	      cacheDir = context.getCacheDir();

	    if(!cacheDir.exists())
	      cacheDir.mkdirs();
	}
	
	/**
	 * Load an image to the left compound {@link Drawable} of a provided {@link TextView}.
	 * If the image is in memory, it will be set immediately.  If not, a thread will
	 * be spawned to load the image from the SD cache or the internet, in that order.  
	 * 
	 * @param url	The URL the image is located.
	 * @param tv	The TextView to apply the image to.  The image is applied to the 
	 * 				TextView's left compounddrawable.
	 */
	public void LoadImage(String url, TextView tv)
	{
		if (mMemoryCache.containsKey(url))
			SetTextViewLeftCompositeImage(mMemoryCache.get(url), tv);
		else
		{
			tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.image_large, 0, 0, 0);
			queueImageLoad(url, tv);
		}
	}
	
	/**
	 * Load an image to {@link Drawable} of a provided {@link ImageView}.  If the image is in
	 * memory, it will be set immediately.  If not, a thread will be spawned to load
	 * the image from the SD cache or the Internet, in that order. 
	 * 
	 * @param url	The URL the image is located.
	 * @param iv	The ImageView to apply the image to.
	 */
	public void LoadImage(String url, ImageView iv)
	{
		if (mMemoryCache.containsKey(url))
		{
			iv.setImageBitmap(mMemoryCache.get(url));
			return;
		}
		else
			queueImageLoad(url, iv);
	}
	
	/**
	 * Spawn a thread to load an image to a TextView from either the SD based cache or
	 * the Internet.
	 * 
	 * @param url	The URL the image is located.
	 * @param tv	The TextView to apply the image to.  The image is applied to the 
	 * 				TextView's left compounddrawable.
	 */
	private void queueImageLoad(String url, TextView tv)
	{
		ImageData imageToLoad = new ImageData(url, tv);
		mThreadPool.submit(new LoadImage(imageToLoad));		
	}
	
	/**
	 * Spawn a thread to load an image to an ImageView from either the SD based cache or
	 * the Internet.
	 * 
	 * @param url	The URL the image is located.
	 * @param iv	The ImageView to apply the image to.
	 */
	private void queueImageLoad(String url, ImageView iv)
	{
		ImageData imageToLoad = new ImageData(url, iv);
		mThreadPool.submit(new LoadImage(imageToLoad));		
	}
    
    /**
     * Cache a bitmap to a location specified by {@value f}.  The bitmap is cached
     * in PNG format at 80% quality.
     * 
     * @param bitmap	The bitmap to cache
     * @param f			The file location
     */
    private void writeFile(Bitmap bitmap, File f) {
	  FileOutputStream out = null;

	  try {
	    out = new FileOutputStream(f);
	    bitmap.compress(Bitmap.CompressFormat.PNG, 80, out);
	  } catch (Exception e) {
	    e.printStackTrace();
	  }
	  finally { 
	    try {  	
	    	if (out != null )
	    		out.close();
	    	} 
	    catch(Exception ex) {} 
	  }
	}

    /**
     * A convenience method to set the left composite bitmap image on a TextView
     * 
     * @param bitmap 	Bitmap to create a {@link Drawable} from
     * @param textview	TextView to apply the composite image to.
     */
    public void SetTextViewLeftCompositeImage(Bitmap bitmap, TextView textview) {
		
		BitmapDrawable thumbnail = new BitmapDrawable(((Activity)textview.getContext()).getResources(), bitmap);
		if (thumbnail != null)
			textview.setCompoundDrawablesWithIntrinsicBounds(thumbnail , null, null, null);
	}

    /**
     * Image URL and UI element(s) references to place the image when done loading.
     */
    private class ImageData
    {
        private String mURL;
		private TextView mTextView;
		private ImageView mImageView;
        
        public ImageData(String url, TextView tv){
            mURL = url; 
            mTextView = tv;
            mImageView = null;
        }
        
        public ImageData(String url, ImageView iv)
        {
        	mURL = url; 
            mTextView = null;
            mImageView = iv;
        }
        
        /*
         * Get Methods
         */
        public String getURL() {
			return mURL;
		}

		public TextView getTextView() {
			return mTextView;
		}
		
		public ImageView getImageView() {
			return mImageView;
		}		
    }
    
    /**
     * A class that implements fetching an image from SD cache or the internet, then spawns
     * an {@link ImageMarshaller} on the UI thread to apply the image to the appropriate
     * UI element.
     */
    class LoadImage implements Runnable {
    	ImageData mImageToLoad;
    	
    	/**
    	 * @param imageToLoad
    	 */
    	LoadImage(ImageData imageToLoad){
            this.mImageToLoad = imageToLoad;
        }
        
        @Override
        public void run() {
            try{
            	Bitmap bitmap = getBitmap(mImageToLoad.getURL());
            	mMemoryCache.put(mImageToLoad.getURL(), bitmap);
                
            	ImageMarshaller marshaller = new ImageMarshaller(bitmap, mImageToLoad);
            	
            	Activity a = null;            	
            	if (mImageToLoad.getTextView() != null)
            		a = (Activity)mImageToLoad.getTextView().getContext();
            	else if (mImageToLoad.getImageView() != null)
            		a = (Activity)mImageToLoad.getImageView().getContext();
            	
            	if (a != null)
            		a.runOnUiThread(marshaller);
            }catch(Throwable th){
                th.printStackTrace();
            }
        }
        
        /**
         *  A function which  
         * 
         * @param url
         * @return
         */
        private Bitmap getBitmap(String url) 
        {
        	String filename = String.valueOf(url.hashCode());
    	  	File f = new File(cacheDir, filename);
    	  	Bitmap bitmap = null;

    	  	// Attempt to load from SD card Cache
    		  try {
    			  // Note: scaling might be necessary to make things look better.  Flickr
    			  // thumbnails are 75x75 px.
    			  /*BitmapFactory.Options opts = new BitmapFactory.Options();
    			  opts.inSampleSize = 2;
    			  bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), opts);*/
    			  
    			  bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
    		  } catch (Exception e) {
    		    e.printStackTrace();
    		  }
    	  	
    	  	if(bitmap != null)
    	  		return bitmap;

            // Attempt to Load from Web
            try {
            	bitmap = BitmapFactory.decodeStream((InputStream) new URL(url)
                        .getContent());
            	writeFile(bitmap, f);
            	return bitmap;
            } catch (Exception e) {
            	// TODO: User notification - couldn't load, is Internet enabled?
                e.printStackTrace();
                return null;
            }
        }
    }
    
    /**
     * Marshal the bitmap back onto the UI thread 
     */
    class ImageMarshaller implements Runnable
    {
        Bitmap mBitmap;
        ImageData mImageToLoad;
        
        /**
         * @param bitmap	The bitmap to apply to the UI element in {@link ImageData}
         * @param imageData	The class containing UI elements and a loaded bitmap to apply
         */
        public ImageMarshaller(Bitmap bitmap, ImageData imageData)
        {
        	mBitmap = bitmap;
        	mImageToLoad = imageData;
        }
        
        public void run()
        {
        	// Apply the bitmap to any available UI elements specified in the ImageData class
        	if(mBitmap != null && mImageToLoad.getTextView() != null)
        		SetTextViewLeftCompositeImage( mBitmap, mImageToLoad.getTextView());
        	if(mBitmap != null && mImageToLoad.getImageView() != null)
        		mImageToLoad.getImageView().setImageBitmap(mBitmap);
        }
    }
}
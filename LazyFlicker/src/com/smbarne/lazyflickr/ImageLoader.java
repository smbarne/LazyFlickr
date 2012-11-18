package com.smbarne.lazyflickr;

import java.io.File;
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
import android.graphics.drawable.TransitionDrawable;
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
	private volatile static ImageLoader instance;

	public static ImageLoader getInstance() {
		if (instance == null) {
			synchronized (ImageLoader.class) {
				if (instance == null) {
					instance = new ImageLoader();
				}
			}
		}
		return instance;
	}

	protected ImageLoader() {
	}
	
	public void init(Context context)
	{
		mThreadPool = Executors.newFixedThreadPool(ThreadCount);
	    // Find or Create a directory to save cached images
		cacheDir = Utilities.GetOrCreateCacheDir(context, "data/lazyflickr/images");
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
			SetImageViewDrawable(mMemoryCache.get(url), iv);
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
     * A convenience method to set the left composite bitmap image on a TextView
     * 
     * @param bitmap 	Bitmap to create a {@link Drawable} from
     * @param textView	TextView to apply the composite image to.
     */
    public void SetTextViewLeftCompositeImage(Bitmap bitmap, TextView textView) {
    	final float density = textView.getContext().getResources().getDisplayMetrics().density;
		final int REQUIRED_SIZE = (int) (64 * density + 0.5f);

        bitmap = Bitmap.createScaledBitmap(bitmap, REQUIRED_SIZE, REQUIRED_SIZE, true);
    	
		BitmapDrawable thumbnail = new BitmapDrawable(((Activity)textView.getContext()).getResources(), bitmap);
		if (thumbnail != null)
			setLeftCompositeDrawableWithFade(textView, thumbnail);
	}
    
    /**
     *  - 
     * 
     * @param bitmap
     * @param imageView
     */
    public void SetImageViewDrawable(Bitmap bitmap, ImageView imageView)
    {
    	BitmapDrawable imageDrawable = new BitmapDrawable(((Activity)imageView.getContext()).getResources(), bitmap);
		if (imageDrawable != null)
			setImageDrawableWithFade(imageView, imageDrawable);
    }
    
    /**
     * - 
     * 
     * @param textView
     * @param drawable
     */
    public static void setLeftCompositeDrawableWithFade(final TextView textView, final Drawable drawable) {
   	 Drawable[] currentDrawables = textView.getCompoundDrawables();
   	 if (currentDrawables[0] != null)
   	 {
		  Drawable[] arrayDrawable = new Drawable[2];
		  
		  arrayDrawable[0] = textView.getContext().getResources().getDrawable(R.drawable.image_large);
		  arrayDrawable[1] = drawable;
		  
		  TransitionDrawable transitionDrawable = new TransitionDrawable(arrayDrawable);
		  transitionDrawable.setCrossFadeEnabled(true);
		  
		  transitionDrawable.startTransition(250);
		  textView.setCompoundDrawablesWithIntrinsicBounds(transitionDrawable, null, null, null);		  
	 }
	 else 
		 textView.setCompoundDrawablesWithIntrinsicBounds(drawable , null, null, null);
   }
    
    /**
     * - 
     * 
     * @param imageView
     * @param drawable
     */
    public static void setImageDrawableWithFade(final ImageView imageView, final Drawable drawable) {
    	 Drawable currentDrawable = imageView.getDrawable();
    	 
    	 if (currentDrawable != null) 
    	 {
			  Drawable[] arrayDrawable = new Drawable[2];
			  //arrayDrawable[0] = currentDrawable;
			  arrayDrawable[0] = imageView.getContext().getResources().getDrawable(R.drawable.image_large);
			  arrayDrawable[1] = drawable;
			  
			  TransitionDrawable transitionDrawable = new TransitionDrawable(arrayDrawable);
			  transitionDrawable.setCrossFadeEnabled(true);
			  
			  imageView.setImageDrawable(transitionDrawable);
			  transitionDrawable.startTransition(250);
    	 }
    	 else 
    		  imageView.setImageDrawable(drawable);
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
            	Utilities.writeBitmapToFile(bitmap, f);
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
        		SetImageViewDrawable(mBitmap, mImageToLoad.getImageView());
        }
    }
}
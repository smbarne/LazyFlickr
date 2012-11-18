package com.smbarne.lazyflickr;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;

public class Utilities {
	
	/** 
	 *  -
	 * 
	 * @param context
	 * @param dir
	 * @return
	 */
	public static File GetOrCreateCacheDir(Context context, String dir)
	{
		File cacheDir = null;
	    // Find or Create a directory to save cached images
	    String sdState = android.os.Environment.getExternalStorageState();
	    if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
	      File sdDir = android.os.Environment.getExternalStorageDirectory();    
	      cacheDir = new File(sdDir, dir);
	    }
	    else
	      cacheDir = context.getCacheDir();

	    if(!cacheDir.exists())
	      cacheDir.mkdirs();
	    
	    return cacheDir;
	}
	
	/**
	 *  - 
	 * 
	 * @param items
	 * @param f
	 */
    public static void writeFlickrItemListToFile(ArrayList<FlickrItem> items, File f) {
	  FileOutputStream out = null;
	  ObjectOutputStream oo = null;

	  try {
		out = new FileOutputStream(f);
	    oo = new ObjectOutputStream(out);
	    oo.writeObject(items);  
	    
	  } catch (Exception e) {
	    e.printStackTrace();
	  }
	  finally { 
	    try {  	
	    	if (oo != null)
	    		oo.close();
	    	if (out != null )
	    		out.close();
	    	} 
	    catch(Exception ex) {} 
	  }
	}
	
    /**
     * Cache a bitmap to a location specified by f.  The bitmap is cached
     * in PNG format at 80% quality.
     * 
     * @param bitmap	The bitmap to cache
     * @param f			The file location
     */
    public static void writeBitmapToFile(Bitmap bitmap, File f) {
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
     *  - 
     * 
     * @param array
     * @return
     */
    public static String StringArrayToCSV(String[] array)
    {
    	StringBuilder sb = new StringBuilder();
    	for (String s : array)
    	{
    		sb.append(s);
    		sb.append(",");
    	}
    	
        return sb.length()>0? sb.substring(0, sb.length() - 1): "";
    }
    
    /**
     *  - 
     * 
     * @param context
     * @param f
     * @return
     */
    @SuppressWarnings("unchecked")
	public static ArrayList<FlickrItem> deserializeFlickrItems(Context context, File f)
    {
    	ArrayList<FlickrItem> items = new ArrayList<FlickrItem>();
    	InputStream in = null;
    	
    	try {
    	    in = new BufferedInputStream(new FileInputStream(f));
            ObjectInputStream ois = new ObjectInputStream(in);
            items = (ArrayList<FlickrItem>) ois.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }catch(ClassNotFoundException e){
            e.printStackTrace();
        }
        
        return items;
    }
	
    


}

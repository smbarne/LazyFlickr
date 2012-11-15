package com.smbarne.lazyflickr;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

public class MainActivity extends Activity {
	
	ListView ImageList;
	LazyFlickrViewAdapter ListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        ImageList = (ListView)findViewById(R.id.imageListView);
        
        String[] sample_data = {"One","Two","Three"};
        ListAdapter = new LazyFlickrViewAdapter(this, sample_data);
        ImageList.setAdapter(ListAdapter);
        
        //Button refreshButton =(Button)findViewById(R.id.refreshButton);
        //refreshButton.setOnClickListener(RefreshDataFeed);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    public OnClickListener RefreshDataFeed=new OnClickListener(){
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
		}
    };
    
}

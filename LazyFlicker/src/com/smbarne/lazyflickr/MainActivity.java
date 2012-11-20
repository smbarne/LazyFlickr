package com.smbarne.lazyflickr;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnActionExpandListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.view.KeyEvent;

/**
 * The MainActivity of LazyFlickr.
 */
public class MainActivity extends SherlockListActivity {
	LazyListViewAdapter LazyListAdapter;
	DataLoader mDataLoader;
	ImageLoader mImageLoader;
	MenuItem mRefreshItem;
	MenuItem mSearchItem;
	String mTagString = "";
	LinearLayout mIntroSearch;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Initialize the XML data loader and cache instance
		mDataLoader = DataLoader.getInstance();
		mDataLoader.Init(getApplicationContext());

		// Initialize the Image loader and cache instance
		mImageLoader = ImageLoader.getInstance();
		mImageLoader.init(getApplicationContext());

		LazyListAdapter = new LazyListViewAdapter(this, mImageLoader);
		setListAdapter(LazyListAdapter);

		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				LaunchPagerActivity(position);
			}
		});

		SetupInitialScreen();

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this.getApplicationContext());
		String mTagString = sp.getString("tagString", "");
		
		if (!mTagString.equals("")) 
			RefreshData();
	}

	/**
	 * Initialize the UI for the intro screen if the user has not searched
	 * before or if they have recently cleared the cache.
	 * 
	 */
	private void SetupInitialScreen() {
		mIntroSearch = (LinearLayout) findViewById(R.id.emptySearchView);
		EditText searchText = (EditText) findViewById(R.id.emptySearchEditText);

		searchText.setOnEditorActionListener(new SearchHandler());
	}

	/**
	 * Check our state, typically from onResume, to see if our mTagString is
	 * still viable.
	 * 
	 */
	private void checkCacheState() {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this.getApplicationContext());
		String mTagString = sp.getString("tagString", "");

		if (mTagString.equals("")) {
			if (LazyListAdapter != null)
				LazyListAdapter.clearData();

			mIntroSearch.setVisibility(View.VISIBLE);
			getListView().getEmptyView().setVisibility(View.GONE);
			getSupportActionBar().setTitle(R.string.app_name);
		} else {
			getSupportActionBar().setTitle(mTagString);
			mIntroSearch.setVisibility(View.INVISIBLE);
			if (LazyListAdapter.getCount() == 0)
				getListView().getEmptyView().setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkCacheState();
	}

	/**
	 * Launch the gallery ViewPager activity centered at the position provided.
	 * 
	 * @param position
	 *            Integer position of the image to center the gallery on.
	 */
	public void LaunchPagerActivity(int position) {
		String[] tags = { mTagString };
		Intent intent = new Intent(this, PagerActivity.class);
		intent.putExtra("tags", tags);
		intent.putExtra("Position", position);
		startActivity(intent);
	}

	/**
	 * Called from the intro screen and functions as a tag search kickoff.
	 * 
	 * @param view
	 */
	public void onEmptySearchDone(View view) {
		EditText et = (EditText) findViewById(R.id.emptySearchEditText);
		ProcessSearchText(et.getText().toString());
	}

	/**
	 * Convert a user input search string into a comma separated tag string.
	 * Store the string in the common preferences manager and handle the UI
	 * transition to having a tag query setup.
	 * 
	 * Calls RefreshData() after it is done processing.
	 * 
	 * @param text
	 */
	public void ProcessSearchText(String text) {
		// Hide the keyboard, but be sure to null check getCurrentFocus due to the full screen landscape keyboard
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		View v = this.getCurrentFocus();
		if (v != null)
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		
		if (!text.equals("")) {
			mTagString = text.replaceAll("\\s+", ",");
			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(this.getApplicationContext());
			sp.edit().putString("tagString", mTagString).commit();

			getSupportActionBar().setTitle(mTagString);

			LinearLayout ll = (LinearLayout) findViewById(R.id.emptySearchView);
			ll.setVisibility(View.INVISIBLE);
			getListView().getEmptyView().setVisibility(View.VISIBLE);

			RefreshData();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.activity_main, (Menu) menu);

		mRefreshItem = menu.findItem(R.id.refresh);

		mSearchItem = menu.findItem(R.id.menu_tag_search);
		mSearchItem.setActionView(R.layout.collapsible_edittext);
		final EditText searchText = (EditText) mSearchItem.getActionView()
				.findViewById(R.id.collapsible_edittext_item);

		// Hide Keyboard when the user defocuses from the ActionBar tag search
		searchText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				}
			}
		});

		// Respond to the search keyboard input for the ActionBar tag search
		searchText.setOnEditorActionListener(new SearchHandler());

		// Focus on the EditText box in the ActionBar after it has been expanded
		// and show the keyboard
		mSearchItem.setOnActionExpandListener(new OnActionExpandListener() {
			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				return true; // Return true to collapse action view
			}

			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				searchText.post(new Runnable() {
					@Override
					public void run() {
						searchText.requestFocus();
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.showSoftInput(searchText,
								InputMethodManager.SHOW_IMPLICIT);
					}
				});
				return true; // Return true to expand action view
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	public void onRefreshMenuClick(final MenuItem item) {
		RefreshData();
	}

	public void onSettingsMenuClick(final MenuItem item) {
		Intent intent = new Intent(this, PreferencesActivity.class);
		startActivity(intent);
	}

	/**
	 * Reload feed data from Flickr.
	 */
	public void RefreshData() {
		String[] tags = { mTagString };
		mDataLoader.LoadFeed(tags, LazyListAdapter, null, mRefreshItem, false);
	}

	/**
	 * A class to handle responding to the search or done keys from a virtual
	 * keyboard.
	 * 
	 */
	class SearchHandler implements OnEditorActionListener {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_SEARCH
					|| actionId == EditorInfo.IME_ACTION_DONE
					|| event.getAction() == KeyEvent.ACTION_DOWN
					&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
				if (mSearchItem != null)
					mSearchItem.collapseActionView();

				ProcessSearchText(v.getText().toString());
				return true;
			}
			return false;
		}
	}
}
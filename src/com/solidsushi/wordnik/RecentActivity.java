package com.solidsushi.wordnik;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class RecentActivity extends Activity
{
	ListView mRecentList;
	private String[] mWords;
	private RecentDbHelper mDbHelper;
	
	@Override 
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		setTheme(android.R.style.Theme_Black);       
		setContentView(R.layout.recent);	
		
		mRecentList = (ListView)findViewById(R.id.recentList);
		
		mDbHelper = new RecentDbHelper(this);
		
		Cursor c = mDbHelper.getRecentWords();
		startManagingCursor(c);
		
		String [] from = new String[] {"word"};
		int [] to = new int[] {R.id.text1};

		mRecentList.setAdapter(new SimpleCursorAdapter(this, 
				R.layout.recent_row, c, from, to));
 		
		mRecentList.setOnItemClickListener(new ListListener());
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.recent_menu, menu);
	    return true;
	}
	

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	switch (item.getItemId()) {
    	case R.id.clearMenu:
    		mDbHelper.clearWords();
    		
    		return true;
    	default:
    		return super.onContextItemSelected(item);
    	}
    }
	
	class ListListener implements AdapterView.OnItemClickListener
	{
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			Intent i = new Intent();
			CharSequence word = ((TextView)arg1.findViewById(R.id.text1)).getText();
			i.putExtra("word",word);
			setResult(RESULT_OK, i);	
			finish();
		}	
	}
}
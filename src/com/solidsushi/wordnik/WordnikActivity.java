package com.solidsushi.wordnik;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

public class WordnikActivity extends Activity implements OnClickListener{
    
	private static final String TAG = WordnikActivity.class.getSimpleName();
	
	private static final int AOK=0,ERR=1,RANDOM=2;
	
	private ProgressDialog progressDialog = null;
	private TextView mDefinitionView;
	private EditText mWordEdit;
	private WordHandler mHandler;

	private TextView mExampleView;
	private ScrollView mScroller;
	private View mMainScreen;
	private View mFeedScreen;	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    	
        setTheme(android.R.style.Theme_Light_NoTitleBar);
        setContentView(R.layout.main);
         
		mWordEdit = (EditText)(findViewById(R.id.wordEdit));
		
		View v = findViewById(R.id.goButton);
        v.setOnClickListener(this);
        
        mScroller = (ScrollView)findViewById(R.id.scroller);
        
        // Inflate the main screen view
        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);	
        mMainScreen = inflater.inflate(R.layout.home, mScroller);
        mFeedScreen = inflater.inflate(R.layout.feed, null);
        
        View random = mMainScreen.findViewById(R.id.random);
        random.setOnClickListener(this);
        
        mDefinitionView = (TextView)mFeedScreen.findViewById(R.id.defView);
		mExampleView = (TextView)mFeedScreen.findViewById(R.id.exampleView);
		
        mHandler = new WordHandler(); 
    }

	public void onClick(View v) {
		
		String word;
		Thread thread;
		
		switch(v.getId()){
		
		case R.id.goButton:
			
			word = mWordEdit.getText().toString();
			
			progressDialog = ProgressDialog.show(this, "Working", 
					"Looking up " + word + "...", true,
	                false);
			
			thread = new Thread(new Loader(word));
		    thread.start();
			
			break;
			
		case R.id.random:
			
			progressDialog = ProgressDialog.show(this, "Working", 
					"Finding a random word...", true,
	                false);
			
			thread = new Thread(new RandomLoader());
		    thread.start();
			break;
		}
	}
	
	
	private class RandomLoader implements Runnable{

		public void run() {
			
			Bundle b = new Bundle();
			Message msg = new Message();
			msg.setData(b);
			msg.arg1 = RANDOM;

			String word = WordnikHelper.buildRandomWord();
			
			// Send our word back to the progress dialog
			b.putString("word",word);
			mHandler.sendMessage(msg);
		}
	}
	
	/** 
	 * Uses wordnikhelper to access the web content, then sends a message 
	 * back to the main GUI
	 */
	private class Loader implements Runnable{

		String mWord;
		
		public Loader(String word)
		{
			mWord = word;
		}
		
		public void run() {
			Message msg = new Message();
			
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(mWordEdit.getWindowToken(), 0);
			String word = mWord;
				
			boolean found = WordnikHelper.wordExists(word);
			
			if(found){	
				Bundle b = new Bundle();
				msg.setData(b);	
				
				String string = WordnikHelper.buildDefinition(word);
											
				b.putString("def", string);
				
				string = WordnikHelper.buildExample(word);
				b.putString("example", string);
				
				msg.arg1 = AOK;		
			}else{
				msg.arg1 = ERR;
				
			}
				
			progressDialog.dismiss();
			mHandler.sendMessage(msg);
		}		
	}
	
	
	/**
	 * Handles the results from the thread
	 */
	private class WordHandler extends Handler
	{		
    	@Override
		public void handleMessage(Message msg)
    	{
    		if(mScroller.getChildAt(0) != mFeedScreen){
    			mScroller.removeAllViews();
    			mScroller.addView(mFeedScreen, 0);
    		}
    		
    		Bundle b = msg.getData();
    			
    		switch(msg.arg1){
    		   		
    		case AOK:  			
    			mDefinitionView.setText(Html.fromHtml(
    					b.getString("def")),
    					TextView.BufferType.SPANNABLE);
    			
    			mExampleView.setText(Html.fromHtml(
    					b.getString("example")),
    					TextView.BufferType.SPANNABLE);
    			break;
    		
    		case ERR:
    			mDefinitionView.setText("Word not found!");
    			break;
    			
    		case RANDOM:
    			
    			mWordEdit.setText(b.getString("word"));
    			View v = findViewById(R.id.goButton);
    			progressDialog.dismiss();
    			v.performClick();
    			break;
    		}
    		
    	}
	}
	
}
package com.solidsushi.wordnik;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
	
	private static final int AOK=0,ERR=1,RANDOM=2,WOD=3;
	
	/** Error Codes */
	private static final int NOT_FOUND=0,NO_INTERNET=1;
	
	private TextView mDefinitionView;
	private EditText mWordEdit;
	private WordHandler mHandler;

	private TextView mExampleView;
	private ScrollView mScroller;
	private View mMainScreen;
	private View mFeedScreen;
	private ProgressView mProgressScreen;
	private View mErrorScreen;
	
	private TextView mWodText;	
	private TextView mErrText;
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
        mErrorScreen = inflater.inflate(R.layout.error, null);

        mErrText = (TextView)mErrorScreen.findViewById(R.id.errorText);
        
        mProgressScreen = new ProgressView(this);
        
        View random = mMainScreen.findViewById(R.id.random);
        random.setOnClickListener(this);
        
        mDefinitionView = (TextView)mFeedScreen.findViewById(R.id.defView);
		mExampleView = (TextView)mFeedScreen.findViewById(R.id.exampleView);
		  
        mHandler = new WordHandler(); 
        
        // Load the word of the day
        mWodText = (TextView)mMainScreen.findViewById(R.id.wodText);
        
        Thread wod = new Thread(new WodLoader());
        wod.start();    
        
        hideSoftKeyboard();
    }
    
    private void hideSoftKeyboard()
    {
    	InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mWordEdit.getWindowToken(), 0);	
    }
    
    /**
     * Removes the old scrolled view and adds a new one
     * @param View to remove.
     */
    public void setScrollView(View v){
		if(mScroller.getChildAt(0) != v){
			mScroller.removeAllViews();
			mScroller.addView(v, 0);
		}
    }
    
	public void onClick(View v) {
		
		String word;
		Thread thread;
		
		if(!haveInternet()){	
			mErrText.setText("No internet connection!");
			setScrollView(mErrorScreen);
		}else{		
			switch(v.getId()){
			
			case R.id.goButton:		
				word = mWordEdit.getText().toString();		
				mProgressScreen.setProgressText("Looking up " + word + "...");
				
				setScrollView(mProgressScreen.getView());
				
				thread = new Thread(new Loader(word));
			    thread.start();
				
				break;
				
			case R.id.random:
				mProgressScreen.setProgressText("Finding a random word...");			
				setScrollView(mProgressScreen.getView());
				
				thread = new Thread(new RandomLoader());
			    thread.start();
				break;
			}
		}
	}
	
	
	/**
	 * Runnable that loads the word of the day
	 */
	private class WodLoader implements Runnable{
		
		public void run() {
			
			Bundle b = new Bundle();
			Message msg = new Message();
			msg.setData(b);
			msg.arg1 = WOD;

			String word = WordnikHelper.buildWordOfTheDay();
	
			// Send our word back to the progress dialog
			b.putString("wod",word);
			mHandler.sendMessage(msg);
		}
		
	}
	
	/**
	 * Runnable that loads a random word
	 */
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
		
		public Loader(String word){
			mWord = word;
		}
		
		public void run() {
			Message msg = new Message();
			
			
			String word = mWord;
			
			
			boolean found = WordnikHelper.wordExists(word);
			
			if(found){	
				Bundle b = new Bundle();
				msg.setData(b);	
				
				String string = "";
				
				string = WordnikHelper.buildDefinition(word);
				b.putString("def", string);
					
				string = WordnikHelper.buildExample(word);
				b.putString("example", string);	
				msg.arg1 = AOK;	
			
			}else{
				msg.arg1 = ERR;
				msg.arg2 = NOT_FOUND;
			}
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
    		Bundle b = msg.getData();
    		hideSoftKeyboard();
    		switch(msg.arg1){
    		
    		case AOK:
    		case ERR:
    		case RANDOM:
    			setScrollView(mFeedScreen);
    		}
    		
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
    			
    			setScrollView(mErrorScreen);
    			
    			switch(msg.arg2){
    			case NOT_FOUND:
    				mErrText.setText(mWordEdit.getText().toString() + " not found!");
    				break;
    			case NO_INTERNET:
    				mErrText.setText("No data connection!");
    			}
    			break;
    				
    		case RANDOM:   			
    			mWordEdit.setText(b.getString("word"));
    			View v = findViewById(R.id.goButton);
    			v.performClick();
    			break;

    		case WOD:
    			String wod = "<b>Word of the day:</b><br/>" + b.getString("wod");
    			mWodText.setText(Html.fromHtml(wod));
    			break;
    		}
    		
    	}
	}

	
	/* 		
	 *@return boolean return true if the application can access the internet 
	 */  
	private boolean haveInternet(){  
		NetworkInfo info= ((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		if(info==null || !info.isConnected()){  
			return false;  
		}  
		if(info.isRoaming()){  
			//here is the roaming option you can change it if you want to disable internet while roaming, just return false  
			return true;  
		}  
		return true;  
	}  	
}
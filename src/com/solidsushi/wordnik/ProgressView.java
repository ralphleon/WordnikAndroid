package com.solidsushi.wordnik;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

class ProgressView
{
	private View mProgressView;
	private TextView mText;
	private ProgressBar mProgress;
	
	public ProgressView(Context c){
		LayoutInflater inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);	
	       
		mProgressView = inflater.inflate(R.layout.loading, null);    
		mText = (TextView)mProgressView.findViewById(R.id.loadingText);
		mProgress = (ProgressBar)mProgressView.findViewById(R.id.progressBar);
	}
	
	View getView(){ 
		return mProgressView; 
		}
	
	public void setProgressText(String s){
		mText.setText(s);
	}
	
	public void reAnimate()
	{
		mProgress.setVisibility(View.GONE);
		mProgress.setVisibility(View.VISIBLE);
		mProgress.setIndeterminate(false);
		mProgress.setIndeterminate(true);		
	}
	
}
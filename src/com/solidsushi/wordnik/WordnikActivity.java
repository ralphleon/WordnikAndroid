package com.solidsushi.wordnik;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class WordnikActivity extends Activity implements OnClickListener{
    
	static final String API_KEY = "7741b711eec09c12e05070046d60da6b92e03750359859fde";
	private static final String TAG = WordnikActivity.class.getSimpleName();
	
	private ProgressDialog progressDialog = null;
	private TextView mDefinitionView;
	private EditText mWordEdit;
	private Handler mHandler;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    	
        setTheme(android.R.style.Theme_Light_NoTitleBar);
        setContentView(R.layout.main);
         
    	mDefinitionView = (TextView)findViewById(R.id.defView);
		mDefinitionView.setFocusable(true);
		
		mWordEdit = (EditText)(findViewById(R.id.wordEdit));
        View v = findViewById(R.id.goButton);
        v.setOnClickListener(this);
        
        mHandler = new Handler(){
        	@Override
			public void handleMessage(Message msg)
        	{
        		mDefinitionView.setText(msg.getData().getString("def"));
        	}
        };
    }

	public void onClick(View v) {
		
		switch(v.getId()){
		case R.id.goButton:
			
			progressDialog = ProgressDialog.show(this, "Working..", 
					"Looking up " + mWordEdit.getText().toString() + "...", true,
	                false);
			
			Thread thread = new Thread(new Loader());
		    thread.start();
			
			break;
		}
	}
	
	private void buildDefinition(SpannableStringBuilder def)
	{
		String word = mWordEdit.getText().toString();
		String response = null;  
		
		try {  
               URL updateURL = new URL("http://api.wordnik.com/api/word.json/"+word+"/definitions");  
               URLConnection conn = updateURL.openConnection();  
               conn.addRequestProperty("api_key", API_KEY);
               
               InputStream is = conn.getInputStream();  
               
               BufferedInputStream bis = new BufferedInputStream(is);  
               ByteArrayBuffer baf = new ByteArrayBuffer(50);  
 
               int current = 0;  
               while((current = bis.read()) != -1){  
                   baf.append((byte)current);  
               }  
 
               /* Convert the Bytes read to a String. */  
               response = new String(baf.toByteArray());   
		} catch (Exception e) {  
			Toast.makeText(this, "Error:" + e, Toast.LENGTH_LONG);
		}  
		
        // Turn the JSON into a structure   
        try {       	
        	JSONArray obj = new JSONArray(response);       	
        	
        	for(int i=0;i<obj.length();i++){	
        		JSONObject o = obj.getJSONObject(i);
        		
        		String pos = o.optString("partOfSpeech");
        		String summary = o.getString("defTxtSummary");

        		def.append(i + ". " + pos + " " + summary + "\n\n");
 
        	}
		} catch (Exception e) {
			Log.e(TAG,"Error" + e);
		}
		
	}

	private void loadDefinition() {
	
		SpannableStringBuilder string = new SpannableStringBuilder("");
		buildDefinition(string);
		
		Message msg = new Message();
		
		Bundle b = new Bundle();
		b.putString("def", string.toString());
		
		msg.setData(b);
   		mHandler.sendMessage(msg);
	}
	
	private class Loader implements Runnable{

		public void run() {
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(mWordEdit.getWindowToken(), 0);
			
			loadDefinition();
			progressDialog.dismiss();
		}
		
	}
	
}
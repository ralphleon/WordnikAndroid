package com.solidsushi.wordnik;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

/**
 * Helper class to manage the connection to wordnik's server
 * @author Ralph Gootee (rgootee@gmail.com)
 */
class WordnikHelper{
	
	private static final String TAG = WordnikHelper.class.getSimpleName();
	private static final String API_KEY = "7741b711eec09c12e05070046d60da6b92e03750359859fde";
	private static final String URI = "http://api.wordnik.com/api-v2/word.json/";
	
	public static boolean wordExists(final String word)
	{
		boolean found = false;
		
		try {  
               URL updateURL = new URL(URI + word);  
               String response = getResponse(updateURL); 
               return true;
               
		} catch (Exception e) {  
		
			Log.e(TAG,"Error: " + e);
		}  
		
		return found;
	}
	
	public static String buildDefinition( final String word)
	{
		String response = null;  
		try {  
               URL updateURL = new URL(URI+word+"/definitions");  
               response = getResponse(updateURL);
		} catch (Exception e) {  
			Log.v(TAG,"response:" + response );	
			Log.e(TAG,"Error: " + e);
			return "";
		}  

		String builder = "";
		
        // Turn the JSON into a structure   
        try {       
        	JSONObject definitions = new JSONObject(response);
        	JSONArray obj = definitions.getJSONArray("definition");       	
        	
        	for(int i=0;i<obj.length();i++){	
        		JSONObject o = obj.getJSONObject(i);
        		
        		String pos = o.optString("partOfSpeech");
        		builder += "<p>" + (i+1) + ". <b>" + pos + "</b> " + o.getString("defTxtSummary") + "</p>";
        	}
		} catch (Exception e) {

			Log.e(TAG,"Error" + e);
		}
		
		return builder;
	}
	
	public static String buildExample( final String word)
	{
		String response = null;  
		try {  
               URL updateURL = new URL(URI+word+"/examples");  
               response = getResponse(updateURL);
		} catch (Exception e) {  
			Log.v(TAG,"response:" + response );	
			Log.e(TAG,"Error: " + e);
			return "";
		}  

		String builder = "";
		
        // Turn the JSON into a structure   
        try {
        	
        	JSONObject examples = new JSONObject(response);
        	JSONArray obj = examples.getJSONArray("example");      	
        	
        	for(int i=0;i<obj.length();i++){	
        		JSONObject o = obj.getJSONObject(i);
        		
        		String display = o.optString("display");
        		String title = o.optString("title");
        		
        		// Find the instances of our word, and then bold it
        		display = boldWord(display,word);
        		
        		builder += "<p>" + (i+1) + ". " + display + " - <i>" + title + "</i></p>";
        	
        	}
		} catch (Exception e) {

			Log.e(TAG,"Error" + e);
		}
		
		return builder;
	}
	
	public static String buildFrequency( final String word)
	{
		String response = null;  
		try {  
               URL updateURL = new URL(URI+word+"/frequency");  
               response = getResponse(updateURL);
		} catch (Exception e) {  
			Log.v(TAG,"response:" + response );	
			Log.e(TAG,"Error: " + e);
			return "";
		}  

		String builder = "";
		
        // Turn the JSON into a structure   
        try {
        	
        	JSONObject examples = new JSONObject(response);
        	JSONArray obj = examples.getJSONArray("frequency");      	
        	
        	for(int i=0;i<obj.length();i++){	
        		JSONObject o = obj.getJSONObject(i);
        		
        		String count = o.optString("count");
        		String year = o.optString("year");
        		
        		
        		builder += "<p>" + (i+1) + ". " + year + " - <i>" + count + "</i></p>";
        	
        	}
		} catch (Exception e) {

			Log.e(TAG,"Error" + e);
		}
		
		return builder;
	}

	public static String buildRandomWord()
	{
		String response = null;  
		try {  
			// Currently buggy with "?hasDictionaryDef=true"
            URL updateURL = new URL("http://api.wordnik.com/api/words.json/randomWord");          
            response = getResponse(updateURL);
             
		} catch (Exception e) {  
			Log.v(TAG,"response:" + response );	
			Log.e(TAG,"Error: " + e);
			return "" ;
		}  

		String word = "";
		
        // Turn the JSON into a structure   
        try {       	
        	JSONObject obj = new JSONObject(response);       	
        	word = obj.getString("word");	
		} catch (Exception e) {
			Log.e(TAG,"Error" + e);
		}
		
		return word;
	}

	public static String buildWordOfTheDay()
	{
		String response = null;  
		try {  
               URL updateURL = new URL("http://api.wordnik.com/api/wordoftheday.json");          
               response = getResponse(updateURL);
               
		} catch (Exception e) {  
			Log.v(TAG,"response:" + response );	
			Log.e(TAG,"Error: " + e);
			return "";
		}  

		String r = "";
		
        // Turn the JSON into a structure   
        try {      
        	
        	JSONObject obj = new JSONObject(response);       	
        	String word = obj.getString("word");
        	
        	// word of the day seems to currently be list
        	String def = obj.getString("definition");
        	JSONArray objArray = new JSONArray(def);       	
        	
        	r = "<b>"+word+"</b> ~ ";
        	
        	// Just get the last definition for now
        	JSONObject o = objArray.getJSONObject(objArray.length()-1);  		
    		String display = o.optString("text");
    		r += display;
    		
		} catch (Exception e) {
			Log.e(TAG,"Error" + e);
		}
		
		return r;
	}
	
	private static String getResponse(URL updateURL) throws IOException  {
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
          return new String(baf.toByteArray());  
	}

	private static String boldWord(String display, final String word) 
	{	
		int location = display.indexOf(word, 0);
		
		if(location != -1)
		{
			display = display.substring(0, location) + "<b>" 
						+ word + "</b>" 
						+ display.substring(location+word.length(),display.length()) ;
		}
		
		return display;
	}
}
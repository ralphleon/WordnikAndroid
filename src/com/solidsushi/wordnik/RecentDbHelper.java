package com.solidsushi.wordnik;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

class RecentDbHelper
{
	private final int MAX = 100;
	
	class Row extends Object {
		public long _Id;
		public String word;
	}
 
	private static final String DATABASE_NAME = "recent.db";
	public static final String DATABASE_TABLE = "words";
	private static final int DATABASE_VERSION = 1;
    
    private SQLiteDatabase mDb;

    public RecentDbHelper(Context ctx) {
    	
    	mDb = ctx.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
        
    	// TODO implement database version
    	
    	final String CREATE_TABLE_RECENT =
        	"CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE
        	+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT,"
        	+ "word TEXT);";
    	
    	mDb.execSQL(CREATE_TABLE_RECENT);
    }
	
    public Cursor getRecentWords(){

    	Cursor c = mDb.rawQuery("SELECT * FROM " + DATABASE_TABLE + " ORDER BY _id DESC LIMIT " + MAX, null);	
    	return c;
    }
    
    public void addWord(String word){
    	// TODO limit the database size by MAX
    	
    	ContentValues c = new ContentValues();
    	c.put("word",word);
    	mDb.insert(DATABASE_TABLE, null, c);
    }
    
    public void clearWords(){
    	// TODO Fix this
    	final String clear = "DELETE FROM " + DATABASE_TABLE;
    	mDb.execSQL(clear);
    }
}
package com.example.blucon;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DataAccess {

	private static SQLiteDatabase db;
	private static DataSQLHelper dbHelper;
	public static final String KEY_OWNER = "ownerid";
	public static final String KEY_FILENAME = "filename";
	static Context mContext;
	
	public DataAccess(Context context){
		mContext = context;
	}
	
	public void open() throws SQLException{
		if(db!=null){
			db.close();
		}
		dbHelper = new DataSQLHelper(mContext);
	    db = dbHelper.getWritableDatabase();
	}
	
	public void close(){
		dbHelper.close();
	}
	
	public FilesList cursorToContact(Cursor cursor){
		FilesList files = new FilesList();
		files.setName(cursor.getString(cursor.getColumnIndex("filename")));
		files.setOwner(cursor.getString(cursor.getColumnIndex("ownerid")));
		return files;
	}

	public List<FilesList> getAllStoredFiles(){
		
		List<FilesList> MusicListItems = new ArrayList<FilesList>();
		Cursor cursor = db.rawQuery("select * from " + DataSQLHelper.MusicFilesList_Table,null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()){
			FilesList filename = cursorToContact(cursor);
			MusicListItems.add(filename);
			cursor.moveToNext();
		}
		cursor.close();
		return MusicListItems;
	}

	public List<FilesList> getOwnerFiles(String owner) {
		List<FilesList> MusicListItems = new ArrayList<FilesList>();
		Cursor cursor = db.rawQuery("select * from " + DataSQLHelper.MusicFilesList_Table + " where ownerid = '"+ owner +"'",null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()){
			FilesList contact = cursorToContact(cursor);
			MusicListItems.add(contact);
			cursor.moveToNext();
		}
		cursor.close();
		return MusicListItems;
	}
	
	public String[] getNeighbour(String owner) {
		String[] neighbourName = new String[10];
		Cursor cursor = db.rawQuery("select * from " + DataSQLHelper.Routing_Table + " where ownerid = '"+ owner +"'",null);
		cursor.moveToFirst();
		int i = 0;
		while (!cursor.isAfterLast()){
			neighbourName[i] = cursor.getString(cursor.getColumnIndex("neighbourid"));
			cursor.moveToNext();
			i++;
		}
		cursor.close();
		return neighbourName;
	}
	
	
	public boolean addNewFile(String filename, String ownerid){
		
		int markedValue = 1;
		ContentValues values = new ContentValues();
		
		// Check if Query shows no output
		
		Cursor cursor = db.rawQuery("select * from " + DataSQLHelper.MusicFilesList_Table 
				+ " where name = '" + filename + "' and ownerid = '" + ownerid + "'",null);
		
		
		cursor.moveToFirst();
		FilesList contact = cursorToContact(cursor);
		String isFile = contact.getName();
		if(!isFile.equals(filename)){
			addNew(filename, ownerid);
			cursor.close();
			return true;
		}else{
			cursor.close();
			return false;
		}
	}
	
	
	
	
	public void addNew(String filename, String ownerid){
		ContentValues values = new ContentValues();
		values.put(KEY_FILENAME, filename);
		values.put(KEY_OWNER, ownerid);
		db.insert(DataSQLHelper.MusicFilesList_Table, null, values);
		db.close();
	}
}

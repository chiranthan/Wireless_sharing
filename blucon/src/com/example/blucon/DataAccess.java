package com.example.blucon;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DataAccess {
	public SQLiteDatabase db;
	public DataSQLHelper dbHelper;
	public static final String KEY_OWNER = "ownername";
	public static final String KEY_FILENAME = "filename";
	public static final String KEY_NEIGHBOUR = "neighbourname";
	static Context mContext;
	
	public DataAccess(Context context){
		dbHelper = DataSQLHelper.getInstance();
		mContext = context;
	}
	
	public void open() throws SQLException{
		if(db!=null){
			db.close();
		}
		//		db = dbHelper.openDB();
		db = dbHelper.getWritableDatabase();
	}
	
	public void close(){
		dbHelper.close();
	}
	
	public FilesList cursorToMusic(Cursor cursor){
		FilesList files = new FilesList();
		files.setName(cursor.getString(cursor.getColumnIndex("filename")));
		files.setOwner(cursor.getString(cursor.getColumnIndex("ownername")));
		return files;
	}

	public String[] getAllStoredFiles(){
		
		String[] fileNameList = new String [99];
		int i = 0;
//		List<FilesList> MusicListItems = new ArrayList<FilesList>();
		Cursor cursor = db.rawQuery("select * from " + DataSQLHelper.MusicFilesList_Table,null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()){
			FilesList filename = cursorToMusic(cursor);
			fileNameList[i] = filename.getName();
//			MusicListItems.add(filename);
			cursor.moveToNext();
//			fileNameList[i] = MusicListItems.get(i).getName();
			i++;
		}
		cursor.close();
		return fileNameList;
	}

	public String[] getTotalListOfStoredFiles(){
		
		String[] fileNameList = new String [99];
		int i = 0;
//		List<FilesList> MusicListItems = new ArrayList<FilesList>();
		Cursor cursor = db.rawQuery("select * from " + DataSQLHelper.MusicFilesList_Table,null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()){
			FilesList filename = cursorToMusic(cursor);
			fileNameList[i] = filename.getName();
//			MusicListItems.add(filename);
			cursor.moveToNext();
//			fileNameList[i] = MusicListItems.get(i).getName();
			i++;
		}
		cursor.close();
		return fileNameList;
	}
	
	public List<FilesList> getOwnerFiles(String owner) {
		List<FilesList> MusicListItems = new ArrayList<FilesList>();
		Cursor cursor = db.rawQuery("select * from " + DataSQLHelper.MusicFilesList_Table + " where ownername = '"+ owner +"'",null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()){
			FilesList contact = cursorToMusic(cursor);
			MusicListItems.add(contact);
			cursor.moveToNext();
		}
		cursor.close();
		return MusicListItems;
	}
	
	public String[] getNeighbour(String owner) {
		String[] neighbourName = new String[10];
		try {
			Cursor cursor = db.rawQuery("select * from " + DataSQLHelper.Routing_Table + " where " + KEY_OWNER + " = '"+ owner +"'",null);
			cursor.moveToFirst();
			int i = 0;
			while (!cursor.isAfterLast()){
				neighbourName[i] = cursor.getString(cursor.getColumnIndex(KEY_NEIGHBOUR));
				cursor.moveToNext();
				i++;
			}
			cursor.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return neighbourName;
	}
	
	public String getOwner(String fileName) {
		Cursor cursor = db.rawQuery("select * from " + DataSQLHelper.MusicFilesList_Table + " where filename" + " = '"+ fileName +"'",null);
		cursor.moveToFirst();
		String owner = cursor.getString(cursor.getColumnIndex("ownername"));
		cursor.close();
		return owner;
	}
	
	
	public boolean addNewFile(String filename, String ownerid){
		
		// Check if Query shows no output
		Cursor cursor = db.rawQuery("select * from " + DataSQLHelper.MusicFilesList_Table,null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()){
			FilesList contact = cursorToMusic(cursor);
			String isFile = contact.getName();
			if(isFile.equals(filename)){
				return false;
			}else {
				cursor.moveToNext();
			}
		}
		addNew(filename, ownerid);
		cursor.close();
		return true;
	}
	
	public void addNew(String filename, String ownerid){
		ContentValues values = new ContentValues();
		filename = filename.trim();
		values.put(KEY_FILENAME, filename);
		values.put(KEY_OWNER, ownerid);
		db.insert(DataSQLHelper.MusicFilesList_Table, null, values);
//		db.close();
	}
}

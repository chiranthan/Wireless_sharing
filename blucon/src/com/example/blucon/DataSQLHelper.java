package com.example.blucon;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

/** Helper to the database, manages versions and creation */
public class DataSQLHelper extends SQLiteOpenHelper {
	public static final int DATABASE_VERSION = 1;
	public static String PackageName = "com.example.blucon";
    //The Android's default system path of your application database.
    public static String DB_PATH = Environment.getDataDirectory()+"/data/" + PackageName + "/databases/";
    public SQLiteDatabase myDataBase;
    public static String DB_NAME = "blucon.sqlite";
    public static String MusicFilesList_Table = "music";
    public static String Routing_Table = "network";
    public final Context myContext;
	public static final String DB_FULL_PATH = DB_PATH + DB_NAME;
	public static DataSQLHelper mInstance;
	public SQLiteDatabase db;
	
	public DataSQLHelper(Context context) {
		super(context, DB_NAME, null, DATABASE_VERSION);
		this.myContext = context;
	}

	public SQLiteDatabase openDB(){
		db = this.getWritableDatabase();
		return db;
	}
	
	public static DataSQLHelper getInstance() {
        /** 
         * use the application context as suggested by CommonsWare.
         * this will ensure that you don't accidentally leak an Activities
         * context (see this article for more information: 
         * http://developer.android.com/resources/articles/avoiding-memory-leaks.html)
         */
        if (mInstance == null) {
            mInstance = new DataSQLHelper(ApplicationContext.getContext());
        }
        return mInstance;
    }
	
	  /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase() throws Exception{
 
    	boolean dbExist = checkDataBase();
 
    	if(dbExist){
    		//do nothing - database already exist
    	}else{
 
    		//By calling this method and empty database will be created into the default system path
               //of your application so we are gonna be able to overwrite that database with our database.
        	this.getReadableDatabase();
 
        	try {
        		
    			copyDataBase();
    			//SQLiteDatabase db = SQLiteDatabase.openDatabase(DB_FULL_PATH, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READWRITE);
 
    		} catch (IOException e) {
 
        		throw new Exception("Error copying database");
 
        	}
    	}
 
    }
 
    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase(){
 
    	SQLiteDatabase checkDB = null;
 
    	try{
    		checkDB = SQLiteDatabase.openDatabase(DB_FULL_PATH, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);
    		checkDB.close();
    	}catch(Exception e){
    		//database does't exist yet.
    		Log.d("DB", "Db does not exist-" + e.getMessage());
    		return false;
    	}
 
    	return checkDB != null ? true : false;
    }
 
    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException{
    	try {
 
    	// Path to the just created empty db
    	String outFileName = DB_PATH + DB_NAME;
 
    	//Open the empty db as the output stream
    	OutputStream myOutput = new FileOutputStream(outFileName);
 
    	//transfer bytes from the inputfile to the outputfile
    	byte[] buffer = new byte[1024];
    	@SuppressWarnings("unused")
		int length;
    	
    	InputStream myInput = myContext.getAssets().open(DB_NAME);
    	while ((length = myInput.read(buffer))>0){
    		myOutput.write(buffer);
    	}
    	//Close the streams
    	myOutput.flush();
    	myOutput.close();
    	myInput.close();
    	}
        catch (IOException e) {
            e.printStackTrace();
        }
 
    }
 
    public SQLiteDatabase openDataBase() throws SQLException{
 
    	//Open the database
        String myPath = DB_PATH + DB_NAME;
    	myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);
    	return myDataBase;
    }
 
    @Override
	public synchronized void close() {
 
    	    if(myDataBase != null && myDataBase.isOpen())
    		    myDataBase.close();
 
    	    super.close();
	}
    
	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d("DB","Oncreate called");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d("DB","onUpgrade called");
	}
	

}

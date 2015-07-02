package com.example.blucon;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class Splash extends Activity {
	DataSQLHelper dbHelper;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		try {
            //-- Start new thread to create db.
            new Thread(){
	            @Override
	            public void run(){
	                try {
	                    synchronized(this){
//	                    	dbHelper = new DataSQLHelper(Splash.this);
//	                		dbHelper.createDataBase();
//	                		dbHelper.openDataBase();
//	                		dbHelper.close();
	                    }
	                    sleep(2000);
	                }
	                catch(Exception ex){
	                	//-- FATAL ERROR
	                	//FileUtil.showErrorOnUiThread("Fatal Error!", "Error occured while installing the app. Contact us at developer@informationworks.in", splashActivity, splashContext);
	                	return;
	                }
        			startActivity(new Intent(Splash.this, Home.class));
	                finish();
	            }
	        }.start();        
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	@Override
    protected void onDestroy() {
      super.onDestroy();
    }
}

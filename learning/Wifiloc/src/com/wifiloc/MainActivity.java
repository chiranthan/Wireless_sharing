package com.wifiloc;

import java.util.Calendar;
import java.util.List;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.iamat.R;

public class MainActivity extends Activity {
 
    //for wifi scan
    private WifiManager wifi;
    private TextView cv;
    private TempDB timeData;
    private PermDB final_timeData;
    private LocationDB locData;
    private int size = 0;
    private List<ScanResult> results;
    private List<ScanResult> Stored_results;
    private TextView home_vector;
    private SQLiteDatabase home_db;
    private Button check_place;
    private Button start;
    private Button stop;
    private TextView current_location;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);	
        cv = (TextView)findViewById(R.id.current);
        check_place = (Button)findViewById(R.id.checkLoc);
        home_vector = (TextView)findViewById(R.id.home_vector);
        home_vector.setMovementMethod(new ScrollingMovementMethod());
        current_location = (TextView)findViewById(R.id.currLoc);
        start = (Button) findViewById(R.id.button3);
        stop = (Button) findViewById(R.id.button4);
        locData = new LocationDB(this);
        final_timeData = new PermDB(this);
        timeData = new TempDB(this);
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifi.isWifiEnabled() == false)
        {
            Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
            wifi.setWifiEnabled(true);
        }

        registerReceiver(new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context c, Intent intent) 
            {
               results = wifi.getScanResults();
               size = results.size();
            }
        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        home_db = locData.getReadableDatabase();
        Cursor cursor = home_db.query(LocationDB.TABLE, null, null, null, null, null, null);
		cursor.moveToFirst();
		if(cursor.toString() != null){
			for(int i = 0; i<cursor.getCount(); i++){
				home_vector.append("\n" + cursor.getString(1));
				cursor.moveToNext();
			}
		}
		
		if (Stored_results == null){
			check_place.setEnabled(false);
		}
		stop.setEnabled(false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void start_service(View view){
		start.setEnabled(false);
		startService(new Intent(getBaseContext(), MLearner.class));
		stop.setEnabled(true);
	}
	
	public void stop_service(View view){
		stop.setEnabled(false);
		stopService(new Intent(getBaseContext(), MLearner.class));
		start.setEnabled(true);
	}
	
	public void getWifiSignals(){
		
        wifi.startScan();
        cv.setText("");
        int i = 0;
        Toast.makeText(this, "Scanning...." + size, Toast.LENGTH_SHORT).show();
        try 
        {
            size = size - 1;
            for(i = 0; i < size; i++) 
            {   
            	cv.append(results.get(i).BSSID + ": " + lvl_conv(results.get(i).level) + "\n");
            }
            
        }
        catch (Exception e)
        { }         
    }
	
	public void scan_wifi(View view){
		getWifiSignals();
	}
	public void check_location(View view){
		check_loc();
	}
	
	public void check_loc(){
		SQLiteDatabase db = timeData.getWritableDatabase();
		ContentValues values = new ContentValues();
		Calendar calendar = Calendar.getInstance();
		int flag = calc_tc();
		if(flag == 1)
			current_location.setText("HOME");
		else
			current_location.setText("OUT");
		int day = calendar.get(Calendar.DAY_OF_WEEK);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int week = calendar.get(Calendar.WEEK_OF_YEAR);
		int hour_week = ((day-1)*24)+hour;
		String week_hour_code = "" + week + hour_week;
		Cursor cursor = db.query(TempDB.TABLE, null, TempDB.UNI + " = '" + week_hour_code + "'" , null, null, null, null);
		if(cursor.getCount() == 0){
			values.put(TempDB.HOUR, hour_week);
			values.put(TempDB.FLAG, flag);
			values.put(TempDB.UNI, week_hour_code);
			db.insert(TempDB.TABLE, null, values);
			Toast.makeText(getApplicationContext(), "Learnt " + hour_week + " hour of week as" + flag, Toast.LENGTH_SHORT).show();
		}
		else{
			Toast.makeText(getApplicationContext(), "Entry already exists", Toast.LENGTH_SHORT).show();
		}
	}
	public void build_fake_temp(View view){
		SQLiteDatabase db = timeData.getWritableDatabase();
		ContentValues values = new ContentValues();
		db.delete(TempDB.TABLE, null, null);
		for(int hour_week = 1; hour_week<=168; hour_week++){
			for(int week = 1; week <= 7; week++){
				String week_hour_code = "" + week + hour_week;
				values.put(TempDB.HOUR, hour_week);
				values.put(TempDB.FLAG, (int)(Math.random() * 2));
				values.put(TempDB.UNI, week_hour_code);
				db.insert(TempDB.TABLE, null, values);
			}
			
		}
		Cursor cursor = db.query(TempDB.TABLE, null, null, null, null, null, null);
		cursor.moveToFirst();
		for(int i = 0; i < cursor.getCount(); i++){
			home_vector.append("\tHour"+ cursor.getString(1) +":" + cursor.getString(2));
			cursor.moveToNext();
		}
	}
	
	public void build_true_final(View view){
		home_vector.setText("");
		SQLiteDatabase temp_db = timeData.getWritableDatabase();
//		SQLiteDatabase final_db = final_timeData.getWritableDatabase();
//		ContentValues values = new ContentValues();
		Cursor cursor = temp_db.query(TempDB.TABLE, null, null, null, null, null, null);
		float percent = 0;
		int count = 0, totalCount = 0;
		cursor.moveToFirst();
		String hour, pre = cursor.getString(1);
		if (cursor.moveToFirst()) {
		    do {
		    	hour = cursor.getString(1);
		    	if(hour.equals(pre) == false){
		    		percent = ((float)count/totalCount)*100;
		    		home_vector.append("\nhour: " + pre);
//		    		home_vector.append("\ncount: " + count);
//		    		home_vector.append("\ntotal: " + totalCount);
		    		home_vector.append("\nAvailabilty :" + percent);
		    		count = 0;
		    		totalCount = 0;
		    	}
		    	count += Integer.parseInt(cursor.getString(2));
		    	totalCount++;
		        pre = hour;
		    } while (cursor.moveToNext());
		}
	}
	
	public void build_fake_final(View view){
		SQLiteDatabase final_db = final_timeData.getWritableDatabase();
		ContentValues values = new ContentValues();
		final_db.delete(PermDB.TABLE, null, null);
		home_vector.setText("");
		for(int hour = 1; hour <= 168; hour++){
			values.put(PermDB.HOUR, hour);
			values.put(PermDB.FLAG, (int)(Math.random() * 2));
			final_db.insert(PermDB.TABLE, null, values);
		}
		Toast.makeText(getApplicationContext(), "Final database created", Toast.LENGTH_SHORT).show();
		Cursor cursor = final_db.query(PermDB.TABLE, null, null, null, null, null, null);
		cursor.moveToFirst();
		for(int i = 0; i < cursor.getCount(); i++){
			home_vector.append("\tHour"+ (i+1) +":" + cursor.getString(2));
			cursor.moveToNext();
		}
	}
	
	public void set_home(View view){
		home_vector.setText("");
		Stored_results = results;
		MLearner.Stored_results = results;
		size = 7;
		String bssid;
		int level;
		home_db = locData.getWritableDatabase();
		ContentValues values = new ContentValues();
		home_db.delete(LocationDB.TABLE, null, null);
		for(int i = 0; i < size; i++){
			bssid = Stored_results.get(i).BSSID;
			level = Stored_results.get(i).level;
			values.put(LocationDB.BSSID, bssid);
			values.put(LocationDB.LEVEL, level);
			home_db.insert(LocationDB.TABLE, null, values);
		}
		
		Cursor cursor = home_db.query(LocationDB.TABLE, null, null, null, null, null, null);
		cursor.moveToFirst();
		for(int i = 0; i < cursor.getCount(); i++)
        {
			home_vector.append(cursor.getString(1) + "\n");
			cursor.moveToNext();
        }
		check_place.setEnabled(true);
	}
	
	private int lvl_conv(float lvl){
		if(lvl>-80)	
			return 1;
		else
			return 0;
	}
	
	public int calc_tc(){
		size = Stored_results.size();
		size = 7;
		int i;
		float crossProd_sum = 0;
		float a_square_sum = 0;
		float b_square_sum = 0;
		float t_c = 0;
		for(i = 0; i < size; i++) 
        {   
			if(Stored_results.get(i).BSSID.equals(results.get(i).BSSID)){
				crossProd_sum += (lvl_conv(Stored_results.get(i).level) * lvl_conv(results.get(i).level));
			if(lvl_conv(Stored_results.get(i).level) == 1)
				a_square_sum += 1;
			if(lvl_conv(results.get(i).level) == 1)
				b_square_sum += 1;
			}
        }
		t_c = (crossProd_sum/(a_square_sum + b_square_sum - crossProd_sum));
		
		Log.d("cross product sum", "cross Prod: " + crossProd_sum);
		Log.d("a squared sum", "a_2_sum: " + a_square_sum);
		Log.d("b squared sum", "b_2_sum: " + b_square_sum);
		Log.d("Tanimoto Coefficient", "Tanimoto Coefficient: " + t_c);
		if(t_c > 0.7)
			return 1;
		else
			return 0;
	}
	
	protected void onResume() {
		super.onResume();
		if(results == null)
			getWifiSignals();
	}
	
}

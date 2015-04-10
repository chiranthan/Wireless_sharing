package com.wifiloc;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
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
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.iamat.R;

public class MainActivity extends Activity {
 
    //for wifi scan
    private WifiManager wifi;
    private TextView cv;
    private MySQLiteHelper timeData;
    private int size = 0;
    private List<ScanResult> results;
    private List<ScanResult> Stored_results;
    private ArrayList<HashMap<String, String>> arraylist = new ArrayList<HashMap<String, String>>();
    private TextView home_vector;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);	
        cv = (TextView)findViewById(R.id.current);
        home_vector = (TextView)findViewById(R.id.home_vector);
        timeData = new MySQLiteHelper(this);
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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	
	public void getWifiSignals(View view){
		
		arraylist.clear();
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
	
	public void at_home(View view){
		SQLiteDatabase db = timeData.getWritableDatabase();
		ContentValues values = new ContentValues();
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_WEEK);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int week = calendar.get(Calendar.WEEK_OF_YEAR);
		int hour_week = ((day-1)*24)+hour;
		String week_hour_code = "" + week + hour;
		Cursor cursor = db.query(MySQLiteHelper.TABLE, null, MySQLiteHelper.UNI + " = '" + week_hour_code + "'" , null, null, null, null);
		if(cursor.getCount() == 0){
			values.put(MySQLiteHelper.HOUR, hour_week);
			values.put(MySQLiteHelper.FLAG, "1");
			values.put(MySQLiteHelper.UNI, week_hour_code);
			db.insert(MySQLiteHelper.TABLE, null, values);
			Toast.makeText(getApplicationContext(), "Learnt that you are present on " + hour_week + " hour of week", Toast.LENGTH_SHORT).show();
		}
		else{
			Toast.makeText(getApplicationContext(), "Entry already exists", Toast.LENGTH_SHORT).show();
		}
	}
	
	private int lvl_conv(float lvl){
		if(lvl>-80)	
			return 1;
		else
			return 0;
	}
	
	public void set_home(View view){
		Stored_results = results;
		size = Stored_results.size();
		size = 7;
		int i = 0;
		home_vector.setText("");
		for(i = 0; i < size; i++)
        {
			home_vector.append(Stored_results.get(i).BSSID + "\n");
        }
		
	}
	
	public void calc_tc(View view){
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
	}
	
	protected void onResume() {
		super.onResume();
	}
	
}

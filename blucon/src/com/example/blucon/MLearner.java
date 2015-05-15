package com.example.blucon;

import java.util.Calendar;
import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class MLearner extends Service {
	public TempDB timeData;
	public WifiManager wifi;
	public static List<ScanResult> Stored_results;
	private List<ScanResult> results;

	@Override
    public void onCreate() {
		timeData = new TempDB(this);
		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		registerReceiver(new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context c, Intent intent) 
            {
               results = wifi.getScanResults();
            }
        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        Toast.makeText(this, "The new Service was Created", Toast.LENGTH_LONG).show();
        
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//TODO do something useful
		Toast.makeText(this, "Service is running", Toast.LENGTH_LONG).show();
		results = wifi.getScanResults();
		if(Stored_results != null && results != null){
			Calendar calendar = Calendar.getInstance();
			int minute = calendar.get(Calendar.MINUTE);
			if(minute == 1){
				Toast.makeText(this, "Learning location", Toast.LENGTH_LONG).show();
				check_loc();
			}
		}
		return Service.START_NOT_STICKY;
	}	
	
	public void check_loc(){
		SQLiteDatabase db = timeData.getWritableDatabase();
		ContentValues values = new ContentValues();
		Calendar calendar = Calendar.getInstance();
		int flag = calc_tc();
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
	
	public int calc_tc(){
		int size = 7;
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
	
	private int lvl_conv(float lvl){
		if(lvl>-80)	
			return 1;
		else
			return 0;
	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
    public void onDestroy() {
        // Tell the user we stopped.
        Toast.makeText(this, "service stopped", Toast.LENGTH_SHORT).show();
    }

}

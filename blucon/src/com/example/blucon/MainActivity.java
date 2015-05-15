package com.example.blucon;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	SharedPreferences sharedPreferences;
	SharedPreferences.Editor editor;
	
//	private List<String> paired = new ArrayList<String>();
	@SuppressLint("CommitPrefEdits")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
        sharedPreferences = PreferenceManager
    			.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
		
	}
	
	public void role_sender(View view){
		Toast.makeText(getBaseContext(), "Role changed to " + "Sender", Toast.LENGTH_SHORT).show();
		editor.putString("role", "sender");
		editor.commit();
		Intent intent = new Intent(this, SenderActivity.class);
		startActivity(intent);
	}
	
	public void role_reciever(View view){
		Toast.makeText(getBaseContext(), "Role changed to " + "reciever", Toast.LENGTH_SHORT).show();
		editor.putString("role", "receiver");
		editor.commit();
		Intent intent = new Intent(this, RecieverActivity.class);
		startActivity(intent);
	}
	
	public void role_relay(View view){
		Toast.makeText(getBaseContext(), "Role changed to " + "Relay", Toast.LENGTH_SHORT).show();
		editor.putString("role", "relay");
		editor.commit();		
		Intent intent = new Intent(this, RelayActivity.class);
		startActivity(intent);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.action_settings:
	            learning_module();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private void learning_module(){
		Intent intent = new Intent(this, LearningActivity.class);
		startActivity(intent);
	}
}

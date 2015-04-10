package com.example.blucon;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {
	
//	private List<String> paired = new ArrayList<String>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
	}
	
	public void role_sender(View view){
		Toast.makeText(getBaseContext(), "Role changed to " + "Sender", Toast.LENGTH_SHORT).show();
		Intent intent = new Intent(this, SenderActivity.class);
		startActivity(intent);
	}
	
	public void role_reciever(View view){
		Toast.makeText(getBaseContext(), "Role changed to " + "reciever", Toast.LENGTH_SHORT).show();
		Intent intent = new Intent(this, RecieverActivity.class);
		startActivity(intent);
	}
	
	public void role_relay(View view){
		Toast.makeText(getBaseContext(), "Role changed to " + "Relay", Toast.LENGTH_SHORT).show();
		Intent intent = new Intent(this, SenderActivity.class);
		startActivity(intent);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}

package com.example.blucon;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ChangeDB extends Activity {

	TextView MACAddress, Destination1, Destination2, Destination3, Relay1,
			Relay2, Relay3;
	EditText DestinationMAC, RelayMAC;
	Button delete1, delete2, delete3;
	DataSQLHelper dbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_db);

		MACAddress = (TextView) findViewById(R.id.textViewMACAddress);
		
		Destination1 = (TextView) findViewById(R.id.textViewDestination1);
		Destination2 = (TextView) findViewById(R.id.textViewDestination2);
		Destination3 = (TextView) findViewById(R.id.textViewDestination3);

		Relay1 = (TextView) findViewById(R.id.textViewRelay1);
		Relay2 = (TextView) findViewById(R.id.textViewRelay2);
		Relay3 = (TextView) findViewById(R.id.textViewRelay3);
		
		delete1 = (Button) findViewById(R.id.buttonDeleteRouting1);
		delete2 = (Button) findViewById(R.id.buttonDeleteRouting2);
		delete3 = (Button) findViewById(R.id.buttonDeleteRouting3);
		
		DestinationMAC = (EditText) findViewById(R.id.editTextDestinationMAC);
		RelayMAC = (EditText) findViewById(R.id.editTextRelayMAC);
		
		Bundle b = getIntent().getExtras();
		String mac = b.getString("currentMAC");
		
		MACAddress.setText(mac);

//		dbHelper = new DataSQLHelper(ChangeDB.this);
//		try {
//			dbHelper.createDataBase();
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//
//		Home.filesData.db = dbHelper.getWritableDatabase();

		String s = Home.filesData.getRoutingDBValues();

		String[] tableValue = s.split(",");

		if (tableValue.length == 6) {
			Destination1.setText(tableValue[0]);
			Destination2.setText(tableValue[1]);
			Destination3.setText(tableValue[2]);
			Relay1.setText(tableValue[3]);
			Relay2.setText(tableValue[3]);
			Relay3.setText(tableValue[3]);
			delete1.setVisibility(View.VISIBLE);
			delete2.setVisibility(View.VISIBLE);
			delete3.setVisibility(View.VISIBLE);
		} else {
			Destination1.setText("");
			Destination2.setText("");
			Destination3.setText("");

			Relay1.setText("");
			Relay2.setText("");
			Relay3.setText("");
			
			delete1.setVisibility(View.INVISIBLE);
			delete2.setVisibility(View.INVISIBLE);
			delete3.setVisibility(View.INVISIBLE);
		}

		// filesData.getAllStoredFiles();

	}

	public void updateDB(View v) {
		String des1 = DestinationMAC.getText().toString();
		String rel1 = RelayMAC.getText().toString();
		if(des1.equals("") || rel1.equals("")){
			Toast.makeText(getApplicationContext(), "Please Enter Both Values", Toast.LENGTH_SHORT).show();
		} else{
			if(Home.filesData.addRoutingDBValues(des1, rel1)){
				Toast.makeText(getApplicationContext(), "Value Added", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getApplicationContext(), "Value Not Added", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	public void deleteDB1(View v) {
		String d = Destination1.getText().toString();
		String r = Relay1.getText().toString();
		
		if(Home.filesData.deleteRoutingDBValues(d, r)){
			Toast.makeText(getApplicationContext(), "Value Deleted", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getApplicationContext(), "Value Not Deleted", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void deleteDB2(View v) {
		String d = Destination2.getText().toString();
		String r = Relay2.getText().toString();
		
		
		if(Home.filesData.deleteRoutingDBValues(d, r)){
			Toast.makeText(getApplicationContext(), "Value Deleted", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getApplicationContext(), "Value Not Deleted", Toast.LENGTH_SHORT).show();
		}
		
	}
	
	public void deleteDB3(View v) {
		String d = Destination3.getText().toString();
		String r = Relay3.getText().toString();

		if(Home.filesData.deleteRoutingDBValues(d, r)){
			Toast.makeText(getApplicationContext(), "Value Deleted", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getApplicationContext(), "Value Not Deleted", Toast.LENGTH_SHORT).show();
		}
		
	}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.change_db, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}

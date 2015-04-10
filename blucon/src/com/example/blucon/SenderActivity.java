package com.example.blucon;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class SenderActivity extends Activity{
	private static final UUID MY_UUID = UUID.fromString("0000110E-0000-1000-8000-00805F9B34FB");
	private final static int REQUEST_ENABLE_BT = 1;
	private BluetoothAdapter mBluetoothAdapter;
	private ArrayAdapter<String> mArrayAdapter;
	private ListView listView;
	public String[] paired;
	private ConnectedThread read_write;
	private EditText message;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sender);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		paired = new String[10];
		
		listView = (ListView) findViewById(R.id.listView1);
		listView.setAdapter(mArrayAdapter);
		message = (EditText) findViewById(R.id.text);
		//this.setListAdapter(mArrayAdapter);
		 
		if (mBluetoothAdapter == null) {
		    // Device does not support Bluetooth
		}
		if (!mBluetoothAdapter.isEnabled()) {
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		showPaired();
		listView.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				//Toast toast = Toast.makeText(getBaseContext(), paired[position], 3);
				//toast.show();
				createSocket(paired[position]);
			}
		});
	}
	
	@SuppressLint("NewApi")
	protected void createSocket(String devAddr){
		BluetoothSocket mSocket = null;
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(devAddr);
		Toast toast;
		//toast = Toast.makeText(getBaseContext(), device.getName(), 1);
		//toast.show();
		try{
			mSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
		}
		catch(IOException e){
			toast = Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT);
			toast.show();
		}
		try{
			mSocket.connect();
		}
		catch(IOException closeException){ 
			toast = Toast.makeText(getBaseContext(), closeException.toString(), Toast.LENGTH_SHORT);
			toast.show();
		}
		manageConnectedSocket(mSocket);
		//BluetoothSocket mSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
	}
	
	private void manageConnectedSocket(BluetoothSocket mSocket){
		Toast.makeText(getBaseContext(), "Ready to send messages", Toast.LENGTH_SHORT).show();
		read_write = new ConnectedThread(mSocket);
	}
	
	public void send(View view){
		String inputMessage = message.getText().toString();
		read_write.write(inputMessage.getBytes());
	}
	
	private void showPaired(){
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		// If there are paired devices
		if (pairedDevices.size() > 0) {
		    // Loop through paired devices
			int cnt = 0;
		    for (BluetoothDevice device : pairedDevices) {
		        // Add the name and address to an array adapter to show in a ListView
		        mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
		        paired[cnt] = device.getAddress();
		        cnt++;
		    }
		}
	}
}
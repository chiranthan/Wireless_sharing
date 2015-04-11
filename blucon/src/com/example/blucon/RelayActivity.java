package com.example.blucon;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class RelayActivity extends Activity {
	
	private BluetoothServerSocket mmServerSocketRelay;
	private BluetoothAdapter mBluetoothAdapterRelay;
	private final static int REQUEST_ENABLE_BT = 1;
	private static final UUID MY_UUID = UUID.fromString("0000110E-0000-1000-8000-00805F9B34FB");
	private String NAME = "BluCon";
	private TextView conn_status_relay;
//	private static TextView incomingMessageRelay;
	static SharedPreferences sharedPreferences;
	static String messageDisplayRelay = "";
//	static boolean relayMessageNow = false;
	
	private ArrayAdapter<String> mArrayAdapter;
	private static ListView listViewRelay;
	public String[] pairedRelay;
	private ConnectedThread read_write_relay;
	
	private EditText message;
	TextView textMessage;
	
	Button forwardMessage;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_relay);
		
		conn_status_relay = (TextView) findViewById(R.id.textViewRelayConnection);
		mBluetoothAdapterRelay = BluetoothAdapter.getDefaultAdapter();
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = mBluetoothAdapterRelay.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
        } catch (IOException e) { }
        mmServerSocketRelay = tmp;
        
		mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		pairedRelay = new String[10];
		
		listViewRelay = (ListView) findViewById(R.id.listViewRelay);
		textMessage = (TextView) findViewById(R.id.textViewMessage);
		listViewRelay.setAdapter(mArrayAdapter);
		message = (EditText) findViewById(R.id.text);
		
		
		if (mBluetoothAdapterRelay == null) {
		    // Device does not support Bluetooth
		}
		if (!mBluetoothAdapterRelay.isEnabled()) {
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		showPaired();
		listViewRelay.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				//Toast toast = Toast.makeText(getBaseContext(), paired[position], 3);
				//toast.show();
				createSocket(pairedRelay[position]);
			}
		});
		
		
        //Start of Thread
        Thread thread = new Thread(){
    		@Override
    	    public void run() {
    	        BluetoothSocket socket = null;
    	        // Keep listening until exception occurs or a socket is returned
    	        while (true) {
    	            try {
    	                socket = mmServerSocketRelay.accept();
    	            } catch (IOException e) {
    	                break;
    	            }
    	            
    	            // If a connection was accepted
    	            if (socket != null) {
    	                // Do work to manage the connection (in a separate thread)
    	            	manageConnectedSocket(socket);
    	            	//close socket
    	            	try{
    	            		mmServerSocketRelay.close();
    	            	}
    	            	catch(IOException e){
    	            		
    	            	}
    	                break;
    	            }
    	        }
    	    }
    	};
        thread.start();
		
	}
	
    /** Will cancel the listening socket, and cause the thread to finish */
    public void cancel() {
        try {
            mmServerSocketRelay.close();
        } catch (IOException e) { }
    }
    
    public static void messageRefresh() {
		messageDisplayRelay = sharedPreferences.getString("inMessage", "");
		if (!messageDisplayRelay.equals("")) {
			
//			relayMessageNow = true;
			/*incomingMessageRelay.post(new Runnable() {
				public void run() {
					String x = incomingMessageRelay.getText().toString();
					incomingMessageRelay.setText(x + "\n" + messageDisplayRelay);
				}
			});*/
		}
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.relay, menu);
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
	
	
	@SuppressLint("NewApi")
	protected void createSocket(String devAddr){
		BluetoothSocket mSocket = null;
		BluetoothDevice device = mBluetoothAdapterRelay.getRemoteDevice(devAddr);
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
		manageConnectedSocketRelaySender(mSocket);
		//BluetoothSocket mSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
	}
	
	public void manageConnectedSocket(BluetoothSocket socket){
		//Toast toast = Toast.makeText(getApplicationContext(), "connection accepted", Toast.LENGTH_SHORT);
        //toast.show();
		conn_status_relay.post(new Runnable() {
            public void run() {
                conn_status_relay.setText("Connected");
                //Toast.makeText(getBaseContext(), "Ready to recieve messages", Toast.LENGTH_LONG).show();
            }
        });
		
		ConnectedThread read_write = new ConnectedThread(socket, this);
		read_write.start();
		//String buffer = new String();
		//buffer = ;
		
		//Handler mHandler = new Handler(Looper.getMainLooper());

	}
	
	private void manageConnectedSocketRelaySender(BluetoothSocket mSocket){
		Toast.makeText(getBaseContext(), "Ready to send messages", Toast.LENGTH_SHORT).show();
		read_write_relay = new ConnectedThread(mSocket, this);
	}
	
	public void sendRelay(View view){
		if (!messageDisplayRelay.equalsIgnoreCase("")){
			String inputMessage = messageDisplayRelay;
			read_write_relay.write(inputMessage.getBytes());
		}else {
			Toast toast;
			toast = Toast.makeText(getBaseContext(), "No Incoming Message", Toast.LENGTH_SHORT);
			toast.show();
		}
		

		
/*		message.setText("");
		String mess = textMessage.getText().toString();
		textMessage.setText(mess + "\n" + inputMessage);
*/	}
	
	private void showPaired(){
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapterRelay.getBondedDevices();
		// If there are paired devices
		if (pairedDevices.size() > 0) {
		    // Loop through paired devices
			int cnt = 0;
		    for (BluetoothDevice device : pairedDevices) {
		        // Add the name and address to an array adapter to show in a ListView
		        mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
		        pairedRelay[cnt] = device.getAddress();
		        cnt++;
		    }
		}
	}
	
	
	
}

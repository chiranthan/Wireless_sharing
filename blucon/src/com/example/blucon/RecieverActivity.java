package com.example.blucon;
import java.io.IOException;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.widget.TextView;
import android.widget.Toast;


public class RecieverActivity extends Activity{
	private BluetoothServerSocket mmServerSocket;
	private BluetoothAdapter mBluetoothAdapter;
	private static final UUID MY_UUID = UUID.fromString("0000110E-0000-1000-8000-00805F9B34FB");
	private String NAME = "BluCon";
	private String connection;
	private TextView conn_status;
	private static TextView incomingMessage;
	static SharedPreferences sharedPreferences;
	static String messageDisplay = "";
	static RecieverActivity recAct;
	
	public static RecieverActivity getInstance(){
		recAct = new RecieverActivity();   
		return   recAct;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reciever);
		conn_status = (TextView) findViewById(R.id.connection);
		incomingMessage = (TextView) findViewById(R.id.incoming);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
        } catch (IOException e) { }
        mmServerSocket = tmp;
        
        //Start of Thread
        Thread thread = new Thread(){
    		@Override
    	    public void run() {
    	        BluetoothSocket socket = null;
    	        // Keep listening until exception occurs or a socket is returned
    	        while (true) {
    	            try {
    	                socket = mmServerSocket.accept();
    	                connection = "Trying to connect";
    	            } catch (IOException e) {
    	                break;
    	            }
    	            
    	            // If a connection was accepted
    	            if (socket != null) {
    	            	
    	            	connection = "Accepted";
    	            	
    	                // Do work to manage the connection (in a separate thread)
    	                
    	            	
    	            	manageConnectedSocket(socket);
    	            	//close socket
    	            	try{
    	            		mmServerSocket.close();
    	            	}
    	            	catch(IOException e){
    	            		
    	            	}
    	                break;
    	            }
    	        }
    	    }
    	};
        thread.start();
        
        //messageRefresh();
        
//        conn_status.setText(connection);
	}
	public void manageConnectedSocket(BluetoothSocket socket){
		//Toast toast = Toast.makeText(getApplicationContext(), "connection accepted", Toast.LENGTH_SHORT);
        //toast.show();
		conn_status.post(new Runnable() {
            public void run() {
                conn_status.setText("Connected");
                //Toast.makeText(getBaseContext(), "Ready to recieve messages", Toast.LENGTH_LONG).show();
            }
        });
		
		ConnectedThread read_write = new ConnectedThread(socket, this);
		read_write.start();
		//String buffer = new String();
		//buffer = ;
		
		//Handler mHandler = new Handler(Looper.getMainLooper());

	}
 
    /** Will cancel the listening socket, and cause the thread to finish */
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) { }
    }
    
    public static void messageRefresh() {
		messageDisplay = sharedPreferences.getString("inMessage", "");
		if (!messageDisplay.equals("")) {
			incomingMessage.post(new Runnable() {
				public void run() {
					String x = incomingMessage.getText().toString();
					incomingMessage.setText(x + "\n" + messageDisplay);
				}
			});
		}
	}
    
}

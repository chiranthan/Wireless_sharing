package com.example.blucon;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;





import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private String buff = "";
    public Context appContext = null;
    String userRole = "";
    
    
	public ConnectedThread(BluetoothSocket socket, Context con) {
		
		appContext = con;
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
            
            
        } catch (IOException e) { }
 
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }
 
    public void run() {
        byte[] buffer = new byte[1024];  // buffer store for the stream
        int bytes;// bytes returned from read()
        SharedPreferences sharedPreferences = PreferenceManager
    			.getDefaultSharedPreferences(appContext);
    	SharedPreferences.Editor editor = sharedPreferences.edit();

		userRole = sharedPreferences.getString("role", "");
    	
        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                // Read from the InputStream
                bytes = mmInStream.read(buffer);
                buff = new String(buffer, 0, bytes);
                Log.d("BLUECON SERVICE", new String(buffer, 0, bytes));
                if (!buff.equals("")){
        			editor.putString("inMessage", buff);
        			editor.commit();
        			if(userRole.equalsIgnoreCase("receiver")){
               			RecieverActivity.messageRefresh();
        			}else if(userRole.equalsIgnoreCase("relay")){
               			RelayActivity.messageRefresh();
        			}
        		}
                // Send the obtained bytes to the UI activity
                //mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
            } catch (IOException e) {
                break;
            }
        }
    }
    
    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) { }
    }
 
    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}

package com.example.blucon;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Home extends Activity {
	
	static String DatabaseHeader = "DDDDDDDDDD";
	static String FilesHeader = "FFFFFFFFFF";
	static String PlayMusicFileHeader = "MMMMMMMMMM";
	static String SenderNameHeader = "";
	static String ReceiverNameHeader = "";
    static String DelimeterHeader = "##########";
    static String messageStartDelimeterHeader = "SSSSSSSSSS";

	static final UUID MY_UUID = UUID.fromString("0000110E-0000-1000-8000-00805F9B34FB");
	final static int REQUEST_ENABLE_BT = 1;
	
	public String[] paired;
	public String[] pairedDeviceName;
	public String[] neighbourNames;
	
	String NAME = "blucon";
	public static String currentDeviceName = "";
	String nextHopName = "";
	public static String allFiles = "";
	String fileNames = "";
	
	static File root;
	static InputStream is;
	
	static ConnectedThread read_write;
	
	static ConnectedThread read_write_musicfile;
	
	DataSQLHelper dbHelper;
	
	static Context context;
	
	ArrayAdapter<String> mArrayAdapter;
	
	ListView listView;
	TextView deviceConnected;
	static BluetoothAdapter mBluetoothAdapter;
	BluetoothServerSocket mmServerSocketRelay;
	
	TextView saySender;
	TextView sayRelay;
	TextView conn_status;
	

	// Useful in Settings menu
	static String setSharedMusicFolder = "";
    public static DataAccess filesData;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        saySender = (TextView) findViewById(R.id.textViewSaySender);
        sayRelay = (TextView) findViewById(R.id.textViewSayRelay);
        
        conn_status = (TextView) findViewById(R.id.textViewIncomingConnection);
        
        conn_status.setVisibility(View.GONE);
        
        saySender.setVisibility(View.GONE);
        sayRelay.setVisibility(View.GONE);
        
        paired = new String[10];
        pairedDeviceName = new String[10];
        neighbourNames = new String[10];

        String s[] = new String [10];
        
    	dbHelper = new DataSQLHelper(Home.this);
		try {
			dbHelper.createDataBase();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		filesData = new DataAccess(this);
		
//		filesData.db = dbHelper.getReadableDatabase();
		
		filesData.db = dbHelper.getWritableDatabase();
	
//		dbHelper.close();
        
//        filesData.open();
		
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothServerSocket tmp = null;
        
		if (!mBluetoothAdapter.isEnabled()) {
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		    finish();
		    startActivity(getIntent());
		}
        
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
        } catch (IOException e) { }
        mmServerSocketRelay = tmp;
        
		context = this;
		
		currentDeviceName = mBluetoothAdapter.getAddress();
	    if(currentDeviceName == null){
	        System.out.println("Name is null!");
	        currentDeviceName = mBluetoothAdapter.getAddress();
	    }

        
        listView = (ListView) findViewById(R.id.listViewAvailableDevices);
        
        listView.setVisibility(View.GONE);
        
        deviceConnected = (TextView) findViewById(R.id.textViewConnectedTo);
        
        mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        
        listView.setAdapter(mArrayAdapter);
        
        showPaired();
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
//				Toast toast = Toast.makeText(getBaseContext(), paired[position], Toast.LENGTH_SHORT);
//				toast.show();
				
				if(createSocket(paired[position]) != null){
					deviceConnected.setText(pairedDeviceName[position]);
					nextHopName = paired[position];
//					startThread();
				}else{
					Toast.makeText(getBaseContext(), "Device not connected, Please try again", Toast.LENGTH_SHORT).show();
				}
			}
		});
        
		// Can be changed from the settings
		// Change root value to change the Folder to be shared
		root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
		
		allFiles = getfile(root);
		
		s = filesData.getAllStoredFiles();
		
		final String [] n = allFiles.split(",");
		
		int nLength = n.length;
		
		int sLength = 0;
		
		int zz = 0;
		while (!(s[zz] == null)){
			sLength++;
			zz++;
		}
		
//		sLength = s.length;
		
		int newFinalLength = sLength + nLength - 1;
		
		int i = 1;
		
		
		for(int j = sLength; j < newFinalLength; j++){
			s[j] = n[i];
			i++;
		}
		
//		int newFinalLength = s.length;
		
		allFiles = "";
		
		for(int x = 0; x < newFinalLength; x++){
			
			allFiles = allFiles + "," + s[x];
			
			// allFiles has , at the beginning
			// 
			
		}
		
		neighbourNames = filesData.getNeighbour(currentDeviceName);
		nextHopName = neighbourNames[0];
		
/*		if(neighbourNames[1].equals("")){
			nextHopName = neighbourNames[0];
		}else {
//			Fetch receiver's name
			for(int i = 0; i < 2; i++){
//				if(!neighbourNames[i].equals(receiverName){
//					nextHopName = neighbourNames[i];
//				}
			}
		}
*/		
		// Get Device Address

		if(nextHopName == null){
			Toast.makeText(getBaseContext(), "No Database Found", Toast.LENGTH_SHORT).show();
			Toast.makeText(getBaseContext(), "Select a device to be connected", Toast.LENGTH_SHORT).show();
	        listView.setVisibility(View.VISIBLE);
		}else {
			/*String deviceConnectedName = */
			if(createSocket(nextHopName) != null){
				deviceConnected.setText(/*deviceConnectedName + " = " + */nextHopName);

			}else{
				
				nextHopName = "";
				Toast.makeText(getBaseContext(), "Select a device to be connected", Toast.LENGTH_SHORT).show();
		        listView.setVisibility(View.VISIBLE);
			}
		}
		startThread();
    }
    
    public void startThread(){
        //Start of Thread
        Thread thread = new Thread(){
    		@Override
    	    public void run() {
    	        BluetoothSocket socket = null;
    	        // Keep listening until exception occurs or a socket is returned
    	        while (true) {
    	            try {
    	                socket = mmServerSocketRelay.accept();
    	                // Connected Thread Listen for incoming Message
    	                
    	                BluetoothDevice b = socket.getRemoteDevice();
    	                b.getAddress();
    	            } catch (IOException e) {
    	                break;
    	            }
    	            
    	            // If a connection was accepted
    	            if (socket != null) {
    	                // Do work to manage the connection (in a separate thread)
    	        		read_write = new ConnectedThread(socket, context);
    	        		read_write.mainPageIncomingMessage();

//    	            	manageConnectedSocket(socket);
    	            	//close socket
    	            	try{
    	            		mmServerSocketRelay.close();
    	            	}
    	            	catch(IOException e){
    	            		
    	            	}
    	                //break;
    	            }
    	        }
    	    }
    	};
        thread.start();
    }
    
    
    public static void createSocketMusicFile(String devAddr){
		BluetoothSocket mSocket = null;
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(devAddr);
		Toast toast;
		//toast = Toast.makeText(getBaseContext(), device.getName(), 1);
		//toast.show();
		try{
			mSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
		}
		catch(IOException e){
			toast = Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT);
			toast.show();
		}
		try{
			mSocket.connect();
		}
		catch(IOException closeException){ 
			toast = Toast.makeText(context, closeException.toString(), Toast.LENGTH_SHORT);
			toast.show();
		}
		read_write_musicfile = new ConnectedThread(mSocket, context);

	}
    
	public static String createSocket(String devAddr){
		BluetoothSocket mSocket = null;
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(devAddr);
		Toast toast;
		//toast = Toast.makeText(getBaseContext(), device.getName(), 1);
		//toast.show();
		try{
			mSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
		}
		catch(IOException e){
			toast = Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT);
			toast.show();
			return null;
		}
		try{
			mSocket.connect();
		}
		catch(IOException closeException){ 
			toast = Toast.makeText(context, closeException.toString(), Toast.LENGTH_SHORT);
			toast.show();
			return null;
		}
		read_write = new ConnectedThread(mSocket, context);

		String name = mBluetoothAdapter.getName();
		
		toast = Toast.makeText(context, "Connected to " + devAddr, Toast.LENGTH_SHORT);
		toast.show();
		
		//		manageConnectedSocket(mSocket);
		//BluetoothSocket mSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
		
		return name;
		
	}
	
	public static void closeSocket(String devAddr){
		BluetoothSocket mSocket = null;
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(devAddr);
		Toast toast;
		try{
			mSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
		}
		catch(IOException e){
			toast = Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT);
			toast.show();
		}
		try{
			mSocket.close();
		}
		catch(IOException closeException){ 
			toast = Toast.makeText(context, closeException.toString(), Toast.LENGTH_SHORT);
			toast.show();
		}
	}
    
	private void manageConnectedSocket(BluetoothSocket mSocket){
//		Toast.makeText(getBaseContext(), "Ready to send messages", Toast.LENGTH_SHORT).show();

//		read_write = new ConnectedThread(mSocket, this);
		
		read_write.mainPageIncomingMessage();
		
		//conn_status.setVisibility(View.VISIBLE);
		
//		read_write.writeFileNames(allFiles.getBytes());
		
//		read_write.receiveFileName();
	}
	
/*	public void manageConnectedSocketRelay(BluetoothSocket socket){
		//Toast toast = Toast.makeText(getApplicationContext(), "connection accepted", Toast.LENGTH_SHORT);
        //toast.show();
		ConnectedThread read_write = new ConnectedThread(socket, this);
		read_write.start();
		//String buffer = new String();
		//buffer = ;
		//Handler mHandler = new Handler(Looper.getMainLooper());
	}
*/	
	public String getfile(File dir) {
		File listFile[] = dir.listFiles();
		if (listFile != null && listFile.length > 0) {
			for (int i = 0; i < listFile.length; i++) {
					if (listFile[i].getName().endsWith(".mp3")){
						fileNames = fileNames + "," + listFile[i].getName();
					}
			}
		}
		return fileNames;
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
		        pairedDeviceName[cnt] = device.getName();
		        cnt++;
		    }
		}
	}
	
	
	
	public void fetchDB(View v){
		if(!nextHopName.equals("")){
			String bytesFetchDB = DatabaseHeader 
					+ DelimeterHeader + currentDeviceName
					+ DelimeterHeader + nextHopName
					+ DelimeterHeader + messageStartDelimeterHeader + DelimeterHeader
					+ "GetDB";
			int len = bytesFetchDB.length();
			byte[] bytes = new byte[len];
			bytes = bytesFetchDB.getBytes();
			read_write.write(bytes);
			
			//wait for the file.
			
			//read incoming file and store.
			//parse file
		}else{
			Toast toast = Toast.makeText(getBaseContext(), "Please Connect to a Device & Try again", Toast.LENGTH_SHORT);
			toast.show();
		}
	}
	
	public void playMusic(View v){
		if(!nextHopName.equals("")){
			Intent intent = new Intent(this, MusicFiles.class);
			intent.putExtra("nextHop", nextHopName);
			startActivity(intent);
		}else{
			Toast toast = Toast.makeText(getBaseContext(), "Please Connect to a Device & Try again", Toast.LENGTH_SHORT);
			toast.show();
		}
	}
	
	
	@SuppressWarnings("unused")
	public static void fileNameToBeSent(String fileName, String receiverDevice) {
		// TODO Auto-generated method stub

//		String fileReceived = new String();
//		String filename = fileReceived.substring(fileReceived.lastIndexOf("/")+1);
//		Log.e("Mess", fileReceived);
//    	File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
		File aFile = new File(root, fileName.trim());
		
		
		// 10 + 10 + 17 + 10 + 17 + 10 + 10 + 10 = 94
		// 1024 - 94 = 930
		
		final String finalMessage = PlayMusicFileHeader 
				+ DelimeterHeader + currentDeviceName
				+ DelimeterHeader + receiverDevice
				+ DelimeterHeader + messageStartDelimeterHeader + DelimeterHeader;
		
    	try {
			is = new FileInputStream(aFile);
			Thread thread = new Thread(){
				@Override
	    	    public void run() {
	    			ByteArrayOutputStream bos = null;
	    			try {
	    				bos = new ByteArrayOutputStream();
	    				int bytesRead = 0;
	    				byte[] header = new byte[94];
	    				header = finalMessage.getBytes();
	    				final byte[] toBeSent = new byte[1024];
    					int bsize = 930;
	    				byte[] b = new byte[bsize];
	    				while ((bytesRead = is.read(b)) != -1) {
	    					for (int i = 0; i < 1024; ++i){
	    						toBeSent[i] = i < 94 ? header[i] : b[i - 94];
	    					}
	    					read_write.writeMusicFilePlayed(toBeSent);
	    				}
	    			} catch (IOException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			}
	    	    }
	    	};
	        thread.start();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void incomingMessages(byte[] buffer) {
		// TODO Auto-generated method stub
		
		String incomingMessage = new String(buffer);
		
		incomingMessage = incomingMessage.trim();
		
/*		// Remove the Unwanted Null Characters
		String [] temp = incomingMessage.split("");
		
		int x = 1;
		
		
		
		int foo = Integer.parseInt(temp[x]);
		
		
		while(!temp[x].equals(null)){
			x++;
		}
		String [] temp2 = new String[x-1];
		for(int z=1; z<x; z++){
			temp2[z-1]=temp[z];
		}
		incomingMessage = Arrays.toString(temp2);
*/
		
		
		String [] n = incomingMessage.split(DelimeterHeader + messageStartDelimeterHeader + DelimeterHeader);
		
		if (n[1].equals("")){
			// error
		}else {
			
			String header = n[0];
			String[] splitHeader = header.split(DelimeterHeader);
			
			if(splitHeader[2].equals(currentDeviceName)){
				//Sender part
				
				
				
				
				if(splitHeader[0].equals(DatabaseHeader)){
					// TODO:
					// database requested
					// Send the DB File Code
					String DBfileName = "activity.db";
					File DBFile = new File(root, DBfileName.trim());
					try {
						is = new FileInputStream(DBFile);
						Thread thread = new Thread(){
							@Override
				    	    public void run() {
				    			try {
				    				int bytesRead = 0;
				    				int bsize = 1024;
				    				byte[] b = new byte[bsize];
				    				while ((bytesRead = is.read(b)) != -1) {
				    					read_write.writeDatabaseFile(b);
				    				}
				    			} catch (IOException e) {
				    				// TODO Auto-generated catch block
				    				e.printStackTrace();
				    			}
				    	    }
						};
						thread.start();
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}else if(splitHeader[0].equals(PlayMusicFileHeader)){
					// music file to be played requested
					
					// name of the music file
					String musicFileName = n[1];
					
					String [] musicFileNameArray = musicFileName.split(".mp3");
					
					musicFileName = musicFileNameArray[0] + ".mp3";

					// Get the file data and send it
					fileNameToBeSent(musicFileName, splitHeader[1]);
					
/*					
					String finalMessage = PlayMusicFileHeader 
							+ DelimeterHeader + MainActivity.currentDeviceName
							+ DelimeterHeader + splitHeader[1]
							+ DelimeterHeader + messageStartDelimeterHeader + DelimeterHeader
							+ n[1];
					read_write.receiveFileName(finalMessage.getBytes());
*/				
				}
			}else if(splitHeader[2].equals(currentDeviceName)){
				// List of Files Requested
				if(splitHeader[0].equals(FilesHeader)){
					// Music files requested
					String listOfMusicFiles = allFiles;
					
//					createSocket(splitHeader[1]);
					
/*					String finalMessage = FilesHeader 
							+ DelimeterHeader + MainActivity.currentDeviceName
							+ DelimeterHeader + splitHeader[1]
							+ DelimeterHeader + messageStartDelimeterHeader + DelimeterHeader
							+ listOfMusicFiles;
*/					read_write.writeFileNames(listOfMusicFiles.getBytes());
				}
				
			}else {
				// Relay part
				// Get neighbour
				
				DataAccess fileData = new DataAccess(ApplicationContext.getContext());
				String[] relayNeighbourNames = fileData.getNeighbour(currentDeviceName);
				String nextHop = "";
//				Fetch receiver's name
				for(int i = 0; i < 2; i++){
					if(!relayNeighbourNames[i].equals(splitHeader[1])){
						nextHop = relayNeighbourNames[i];
					}
				}
				createSocket(nextHop);
				read_write.writeForwardMessage(incomingMessage.getBytes());
				// Just Forward to whole message as it is
				// mmOutStream.write(incomingMessage.getBytes());
				//Start of Thread
				//
				if(splitHeader[0].equals(PlayMusicFileHeader)){
					createSocket(splitHeader[1]);
			        Thread thread = new Thread(){
			    		@Override
			    	    public void run() {
			    	        // Keep listening until exception occurs or a socket is returned
							read_write_musicfile.mainPageIncomingMessage();
			    	    }
			    	};
			        thread.start();
				}else {
			        Thread thread = new Thread(){
			    		@Override
			    	    public void run() {
			    	        // Keep listening until exception occurs or a socket is returned
	    	        		read_write.mainPageIncomingMessage();
			    	    }
			    	};
			        thread.start();
				}
			}
		}
	}
	
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_help) {
			startActivity(new Intent(this, Help.class));
			return true;
		} else if (id == R.id.action_changedb) {
			Intent i = new Intent(this, ChangeDB.class);
			i.putExtra("currentMAC", currentDeviceName);
			startActivity(i);
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
}

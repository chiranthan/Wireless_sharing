package com.example.blucon;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.text.Layout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MusicFiles extends Activity {

	private BluetoothServerSocket mmServerSocket;
	private static BluetoothAdapter mBluetoothAdapter;
	private static final UUID MY_UUID = UUID.fromString("0000110E-0000-1000-8000-00805F9B34FB");
	private String NAME = "BluCon";
//	private TextView conn_status;
	private static ConnectedThread read_write;
	static SharedPreferences sharedPreferences;
	private ListView listViewFiles;
	static String messageDisplay = "";
	private static ArrayAdapter<String> mArrayAdapterFiles;
//	static MusicFiles recAct;
	static MediaPlayer mediaPlayer;
	static File tempMp3;
	static FileOutputStream fos;
	static FileInputStream fis;
	SharedPreferences.Editor editor;
	String fileName = "";
	static boolean filesReceived = false;
	
	public static String currentDeviceName = "";
	static String nextHopName = "";
	
	static Context context;
	
//	static TextView fileLength;
	
    public static	DataAccess filesData;
    DataSQLHelper dbHelper;

    String[] owners = new String[55];
	List<FilesList> listOfFilesList;
	FilesList FilesListObject;
	String concatedListOfFiles = "";
	String[] listOfFiles;
//	String[] 
	
	public String[] neighbourNames;
	
    String DatabaseHeader = "DDDDDDDDDD";
    String FilesHeader = "FFFFFFFFFF";
    String PlayMusicFileHeader = "MMMMMMMMMM";
    String SenderNameHeader = "";
    String ReceiverNameHeader = "";
    static String DelimeterHeader = "##########";
    static String messageStartDelimeterHeader = "SSSSSSSSSS";
    
    static TextView text;
    
    static RelativeLayout rl;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_music_files);

		mArrayAdapterFiles = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

		listViewFiles = (ListView) findViewById(R.id.listViewFiles);
		
		rl = (RelativeLayout) findViewById(R.id.innerlayout2);
		
		rl.setVisibility(View.GONE);
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		listOfFiles = new String[99];
		
        text = (TextView)  findViewById(R.id.textViewDownload);
        
        text.setText("");
        
		context = this;
		
    	dbHelper = new DataSQLHelper(this);
		try {
			dbHelper.createDataBase();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		
		filesData = new DataAccess(this);
		
		filesData.db = dbHelper.openDataBase();
		
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
        } catch (IOException e) { }
        mmServerSocket = tmp;
        
        neighbourNames = new String[10];
        
		currentDeviceName = mBluetoothAdapter.getAddress();
        
		
		Bundle b = getIntent().getExtras();
		nextHopName = b.getString("nextHop");
		
		createFile();
		
/*		BluetoothSocket mSocket = null;
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(nextHopName);
		Toast toast;
		//toast = Toast.makeText(getBaseContext(), device.getName(), 1);
		//toast.show();
		try{
			mSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
		}
		catch(IOException e){
			toast = Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT);
			toast.show();
//			return null;
		}
		
		read_write = new ConnectedThread(mSocket, context);
*/		
/*		if(createSocket(nextHopName) != null){
//			deviceConnected.setText(/*deviceConnectedName + " = " + * /nextHopName);
		}else{
//			nextHopName = "";
			Toast.makeText(getBaseContext(), "Select a device to be connected", Toast.LENGTH_SHORT).show();
		}
		
*/		
		String finalMessage = FilesHeader 
				+ DelimeterHeader + currentDeviceName
				+ DelimeterHeader + ""
				+ DelimeterHeader + messageStartDelimeterHeader + DelimeterHeader
				+ "FetchFiles";

		Home.read_write.writeFetchMusicFiles(finalMessage.getBytes());
		
		  //Start of Thread
        Thread thread = new Thread(){
    		@Override
    	    public void run() {
    	        // Keep listening until exception occurs or a socket is returned
//    	        while (true) {
    	        	Home.read_write.receiveIncomingListofFiles();
//    	        }
    	    }
    	};
        thread.start();
		
		
		
		
/*        //Start of Thread
        Thread thread = new Thread(){
    		@Override
    	    public void run() {
    	        BluetoothSocket socket = null;
    	        // Keep listening until exception occurs or a socket is returned
    	        while (true) {
    	            try {
    	                socket = mmServerSocket.accept();
    	            } catch (IOException e) {
    	                break;
    	            }
    	            
    	            // If a connection was accepted
    	            if (socket != null) {

    	            	// Do work to manage the connection (in a separate thread)
//    	                fetchFileManageSocket(socket);
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
*/        
        listViewFiles.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//				read_write.write(fileName.getBytes());
//				String selectedFromList = listViewFiles.getItemAtPosition(position);
				String text = parent.getItemAtPosition(position).toString().trim();

				// Get owner ID from Database
				
				
				
				String ownerName = filesData.getOwner(text);
				if(!ownerName.equals(currentDeviceName)){
					String finalMessage = PlayMusicFileHeader 
							+ DelimeterHeader + currentDeviceName
							+ DelimeterHeader + ownerName
							+ DelimeterHeader + messageStartDelimeterHeader + DelimeterHeader
							+ text;
					finalMessage = finalMessage.trim();
					rl.setVisibility(View.VISIBLE);
					Home.read_write.writePlayThisMusic(finalMessage.getBytes());
			        Thread thread = new Thread(){
			    		@Override
			    	    public void run() {
			    	        // Keep listening until exception occurs or a socket is returned
//			    	        while (true) {
							Home.read_write.incomingMusicFile();
//			    	        }
			    	    }
			    	};
			        thread.start();
				}else{
					File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
					File aFile = new File(root, text);
					FileInputStream is;
					try {
						is = new FileInputStream(aFile);
						fis = is;
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					rl.setVisibility(View.VISIBLE);
			    	playMusicSong("Play");
				}
				
//				read_write.start();
			}
		});
        
//		filesData.open();
		
		
        //messageRefresh();
        
		//        conn_status.setText(connection);

	}

/*	public static String createSocket(String devAddr){
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
*/	
	public void fetchListOfItem(View v){
		
		listOfFiles = filesData.getTotalListOfStoredFiles();
		
/*		for(int i = 0; i < owners.length; i++){
			listOfFilesList = filesData.getOwnerFiles(owners[i]);
			int a = listOfFilesList.size();
			for(int j = 0; j < a; j++){
				FilesListObject = listOfFilesList.get(j);
				concatedListOfFiles = FilesListObject.getName() + ",";
			}
		}
		listOfFiles = concatedListOfFiles.split(",");
*/		
		
		int nLength = 0;
		while (!(listOfFiles[nLength] == null)){
			nLength++;
		}
		for (int i = 0; i < nLength; i++ ) {
	        mArrayAdapterFiles.add(listOfFiles[i]);
	    }
		listViewFiles.setAdapter(mArrayAdapterFiles);

		listViewFiles.setVisibility(View.VISIBLE);
		
	}
	
/*	public void fetchFileManageSocket(BluetoothSocket socket) {
		// TODO Auto-generated method stub

		read_write = new ConnectedThread(socket, this);
		read_write.receiveFiles();
		
	}
*/	
/*	public void manageConnectedSocket(BluetoothSocket socket){
		//Toast toast = Toast.makeText(getApplicationContext(), "connection accepted", Toast.LENGTH_SHORT);
        //toast.show();
		conn_status.post(new Runnable() {
            public void run() {
                conn_status.setText("Connected");
                //Toast.makeText(getBaseContext(), "Ready to recieve messages", Toast.LENGTH_SHORT).show();
            }
        });
		
		read_write = new ConnectedThread(socket, this);
		createFile();

		read_write.receiveFiles();
//		read_write.start();
		//String buffer = new String();
		//buffer = ;
		//Handler mHandler = new Handler(Looper.getMainLooper());

	}
*/ 
    private void createFile() {
		// TODO Auto-generated method stub
		
    	File dir = Environment.getExternalStorageDirectory();
        try {
			tempMp3 = File.createTempFile("temp", "mp3", dir);
	        tempMp3.deleteOnExit();
	        fos = new FileOutputStream(tempMp3, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/** Will cancel the listening socket, and cause the thread to finish */
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) { }
    }
    
/*	public void playAudio(View view){
        try {
        	mediaPlayer.setDataSource(fis.getFD());
			mediaPlayer.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        mediaPlayer.start();
    }
*/
    
    
    
    public void playMusicButtonClick(View v){
    	playMusicSong("Play");
    }
    
    public void stopMusicButtonClick(View v){
    	playMusicSong("Stop");
    }
    
    public static void playMusicSong(String str){
        try {
            mediaPlayer = new MediaPlayer();
        	mediaPlayer.setDataSource(fis.getFD());
			mediaPlayer.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	if (str.equals("Stop")){
            mediaPlayer.stop();
//            showToastMessage("Music Has Stopped");
    	}else {
            mediaPlayer.start();
//            showToastMessage("Music Has Started");
    	}

            

            
    }
    
    public static void messageRefresh(byte[] messageBytes) {
    	try {
    		
    		String incomingMessage = new String(messageBytes);
    		
    		incomingMessage = incomingMessage.trim();
    		
    		String [] n = incomingMessage.split(DelimeterHeader + messageStartDelimeterHeader + DelimeterHeader);
    		
    		int isExtra = n.length;
    		
    		if(isExtra == 2){
    			messageBytes = n[1].getBytes();
    		
/*        	fileReady.post(new Runnable() {
				public void run() {
					fileReady.setVisibility(View.VISIBLE);				
				}
			});
*/ 		
//    		int counter = 1;
    		
//    		byte[] countBytes = new byte[1]; 		
//    		for(int i = 0; )
//    		int length = messageBytes.length;
//    		fos.write(messageBytes,0,length);
    		for(int i = 0; i<messageBytes.length;i++)
    			fos.write(messageBytes[i]);
//            fos.close();
    		fis = new FileInputStream(tempMp3);
    		
    		final double bytes = tempMp3.length();
    		final double kilobytes = (bytes / 1024);
			final double megabytes = (kilobytes / 1024);
    		
    		System.out.println("bytes : " + bytes);
    		System.out.println("kilobytes : " + kilobytes);
			System.out.println("megabytes : " + megabytes);
			
			if(kilobytes == 200){
		    	playMusicSong("Play");
		    	rl.setVisibility(View.VISIBLE);
			}
			
			text.post(new Runnable() {
	            public void run() {
	            	text.setText("Size of the file is " + bytes + " bytes or " + kilobytes + "kB or " + megabytes + " MB");
	            }
	        });
			
    		}

/*			fileLength.post(new Runnable() {
	            public void run() {
	    			fileLength.setText("Size of the file is " + bytes + " bytes or " + kilobytes + "kB or " + megabytes + " MB");
	            }
	        });
*/    		
        } catch (IOException ex) {
            ex.printStackTrace();
        }
	}

    
    
	public static void getAllFiles(byte[] files) {
		// TODO Auto-generated method stub
		
		String fileList = new String(files);
		
		
		
		final String [] n = fileList.split(",");
		
		int nLength = n.length;
		
		
		
		// Add Files to database
		
		// Get Connected Owner Bluetooth ID
		
		
		// Get device names from the Bluetooth ID
		//BluetoothDevice device
		//device.getName()
		
	
		//Fetched BluetoothID name
		// Connected Bluetooth Device
		
		
//		String OwnerBlueToothID = "Galaxy Nexus";
		
		// New Music Files added to the Database
		for (int i = 1; i < nLength; i++ ) {
			filesData.addNewFile(n[i],nextHopName);
	    }
		filesReceived = true;
	}
	
	public static void showToastMessage(String str){
		
        Toast toast = Toast.makeText(ApplicationContext.getContext(), str, 1);
		toast.show();
		
	}
}

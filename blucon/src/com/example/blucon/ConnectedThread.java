package com.example.blucon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Environment;

public class ConnectedThread extends Thread {
    @SuppressWarnings("unused")
	private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    public Context appContext = null;
    String userRole = "";
    boolean isSent = false;
    boolean fileFetched = false;
    
    String DatabaseHeader = "DDDDDDDDDD";
    String FilesHeader = "FFFFFFFFFF";
    String PlayMusicFileHeader = "MMMMMMMMMM";
    String SenderNameHeader = "";
    String ReceiverNameHeader = "";
    String DelimeterHeader = "##########";
    String messageStartDelimeterHeader = "SSSSSSSSSS";
    
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
	
	
//  #2 Listen to incoming message till received.
//	once the message is received, decide the receiver and forward it or act on it
	
	@SuppressWarnings("unused")
	public void mainPageIncomingMessage(){
        byte[] buffer = new byte[1024];
        int bytes = 0;
		try {
			while((bytes = mmInStream.read(buffer)) != -1){
				Home.incomingMessages(buffer);
			}
			mmInStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

//  Send the list of Music Files to the Receiver
	
	public void writeFileNames(byte[] bytes) {
		try {
			mmOutStream.write(bytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//  #2 Call this from the home activity to send data to the remote device 
//	Forward message from relay to the next hop

	public void writeForwardMessage(byte[] bytes) {
       try {
               mmOutStream.write(bytes);
       } catch (IOException e) { }
       
	}
	
	
//	# 1 Receiver sending message to fetch music files from other devices
	
	public void writeFetchMusicFiles(byte[] bytes) {
	       try {
	               mmOutStream.write(bytes);
	       } catch (IOException e) { }
	       
	   }

//	Send Message to play the music file with music file name
	public void writePlayThisMusic(byte[] bytes) {
	       try {
	               mmOutStream.write(bytes);
	               
	   /*        	int size = files.length;
	       		byte[] sendingFileName = new byte[size];
	       		for(int i = 0; i < size; i++){
	       			sendingFileName[i] = files[i];
	       		}
	   */
	       } catch (IOException e) { }
	       
	   }

	
//	Send File to be played 1024 bytes at a time
	public void writeMusicFilePlayed(byte[] bytes) {
	       try {
	               mmOutStream.write(bytes);
	       } catch (IOException e) { }
	       
	   }

	
//	Send database file bytes asked by the receiver
	public void writeDatabaseFile(byte[] bytes) {
	       try {
	               mmOutStream.write(bytes);
	       } catch (IOException e) { }
	       
	   }

	
	
//	Send Message to fetch database
	public void write(byte[] bytes) {
	       try {
	               mmOutStream.write(bytes);
	       } catch (IOException e) { }
	       
	   }
	
//  Receiver listening to the incoming list of music files from other devices in the network
	
	public void receiveIncomingListofFiles() {
        try {
            byte[] files = new byte[9999];
			mmInStream.read(files);
			MusicFiles.getAllFiles(files);
        } catch (IOException e) { }
        
    }

//	Receiver listening to the incoming music files bytes requested by it
//	Writing it down to the temporary file
	
	public void incomingMusicFile() {
		// TODO Auto-generated method stub
        byte[] buffer = new byte[1024];  // buffer store for the stream
        @SuppressWarnings("unused")
		int bytes;// bytes returned from read()
		try {
			while((bytes = mmInStream.read(buffer, 0, buffer.length)) != -1){
					MusicFiles.messageRefresh(buffer);
			}
			mmInStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	Not Defined
//	
	public void incomingDBFile() {
		// TODO Auto-generated method stub
        byte[] buffer = new byte[1024];  // buffer store for the stream
        @SuppressWarnings("unused")
		int bytes;// bytes returned from read()
        File dir = Environment.getExternalStorageDirectory();
        try {
			File dbFile = File.createTempFile("temp", "db", dir);
	        OutputStream fos = new FileOutputStream(dbFile, true);;
		
			try {
				while((bytes = mmInStream.read(buffer, 0, buffer.length)) != -1){
						fos.write(buffer);
				}
				mmInStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	

/*	public void fetchDatabase(byte[] bytes) {
		try {
			mmOutStream.write(bytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
*/	
/* 
    @SuppressWarnings("unused")
	public void run() {
    	int bsize = 1024*8;
        byte[] buffer = new byte[bsize];  // buffer store for the stream
        int bytes;// bytes returned from read()
        
        
        byte[] files = new byte[9999];
        
        SharedPreferences sharedPreferences = PreferenceManager
    			.getDefaultSharedPreferences(appContext);
		userRole = sharedPreferences.getString("role", "");
		
		byte [] wasteBytes = new byte[1024];
		
		try {
			mmInStream.read(wasteBytes);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		
		
		if (!fileFetched){
	        try {
				mmInStream.read(files);
				RecieverActivity.getAllFiles(files);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	        fileFetched = true;
		}
		
        

    	
        while (true) {
            try {
                // Read from the InputStream
           			if(userRole.equalsIgnoreCase("receiver")){
           				while((bytes = mmInStream.read(buffer, 0, buffer.length)) != -1){
           					
//           					if (bytes != 0){
//               					RecieverActivity.messageRefresh(buffer);
 //          					}
           					mmOutStream.write(1);
           					
           				}
           				mmInStream.close();
           			}else if(userRole.equalsIgnoreCase("relay")){
           				
           				
           				while((bytes = mmInStream.read(buffer)) != -1){
 //          					RelayActivity.messageRefresh(buffer);
           				}
           				
           				while((bytes = mmInStream.read(buffer)) != -1){
//           					RelayActivity.messageRefresh(buffer);
           				}
           				
           				while((bytes = mmInStream.read(buffer)) != -1){
   //        					RelayActivity.messageRefresh(buffer);
           				}
           				mmInStream.close();
           			}else if(userRole.equalsIgnoreCase("sender")){
           				while((bytes = mmInStream.read(buffer)) != -1){
//           					RelayActivity.messageRefresh(buffer);
           				}
           				mmInStream.close();
           			}
            	
//                bytes = mmInStream.read(buffer);
       			if(userRole.equalsIgnoreCase("receiver")){
           			RecieverActivity.messageRefresh(buffer);
           			break;
       			}else if(userRole.equalsIgnoreCase("relay")){
           			RelayActivity.messageRefresh();
       			}
            } catch (IOException e) {
                break;
            }
        }
    }
    
    
    
    
    
    
 
     Call this from the main activity to shutdown the connection 
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }

	public void writeFile(byte[] bytes) {
		// TODO Auto-generated method stub
		try {
			mmOutStream.write(bytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
*/    
}

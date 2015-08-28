import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.Base64;
import java.util.Scanner;


public class ReceiveGPS {
	
	private static AsymAlgo enc;
	private static AlgoAES aesenc;
	
	private static ServerSocket serverSocket;
	private static Socket socket;
	private static InputStream is;
	private static DataInputStream dis;
	private static Base64.Decoder decoder = Base64.getDecoder();
	private final static int SERVERPORT = 4444;

	public static void main(String[] args) {

		enc = new AsymAlgo();
		enc.getKey();
	//	enc.generateKeys();
		
		aesenc = new AlgoAES();
		aesenc.getKey();
	//	aesenc.generateKey();
		
		try {
			serverThread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static Thread serverThread = new Thread(new Runnable(){
		@SuppressWarnings("deprecation")
		public void run() {
			StringBuffer rawData;
			String 	inStr,
					msgStr;
			StringBuilder sb;
			SocketAddress client = null;
			
			try {
			   	serverSocket = new ServerSocket(SERVERPORT);
			   	serverSocket.setReceiveBufferSize(100);
				serverSocket.setSoTimeout(60000);
				} catch(IOException e) {
	            e.printStackTrace();
	            }
			
		      while(true) {
		         try {
		        	rawData = new StringBuffer();
					System.out.println("Waiting for tracking data on port " + serverSocket.getLocalPort() + "...\n");
					socket = serverSocket.accept();
				       
					if (socket.isConnected()) {
						client = socket.getRemoteSocketAddress();
						System.out.println("Connected to " + client);
						is = socket.getInputStream();
					//	dis = new DataInputStream(decoder.wrap(is));
						dis = new DataInputStream(is);
						System.out.println("-----START-DATA-FEED--------------------------------------------------------");
						
						sb = new StringBuilder();
						while ((inStr = dis.readLine()) != null) {
						//	System.out.println(inStr + inStr.length());
							sb.append(inStr);
							if (sb.length() > 76){ // NASTY LF HACK
								
								msgStr = sb.toString();
								msgStr = new String(decoder.decode(msgStr.getBytes("UTF-8")));
						//		msgStr = new String(enc.decrypt(msgStr.getBytes("UTF-8"))); // RSA decrypt
						//		msgStr = new String(aesenc.decrypt(msgStr.getBytes("UTF-8"))); // AES decrypt
								System.out.println(msgStr + "\t" + msgStr.length() + "\t" + sb.length());
								
								sb = new StringBuilder();
							}
							
							rawData.append(inStr);
						}	

				   		System.out.println("-----STOP-DATA-FEED--------------------------------------------------------");
						System.out.print(client + " disconnected.\n");
						saveFile(rawData.toString()); //save raw GPS data
						
						} else {
				       	System.out.print("Socket is closed\n");
				   		socket.close();
				        }
		         } catch (SocketTimeoutException s) {
		            System.out.println("Socket timed out\n");
		         } catch(IOException e) {
		            e.printStackTrace();
		            break;
		         } catch (IllegalArgumentException e) {
		        	 System.out.println("\nIllegalArgumentException\n");
		         } finally {
		        	 try {
		        		 if (dis != null) {
		        			 dis.close();
		    			 	}
		        		 if (is != null) {
		        			 is.close();
		    			  	}
		    		  	} catch (Exception e) {
		    			  System.out.println("Error while closing streams\n");
		    		  	}
		    	 	}
		      	}
			}
		});

	@SuppressWarnings("unused")
	private static byte[] getData() throws IOException {
		try{
			System.out.print("Select raw GPS data file: ");
			Scanner var = new Scanner(System.in); //select raw input file
			String filename = var.next();
			File fGPS = new File(filename);

			Scanner scanGPS = new Scanner(fGPS);
			String rawGPS = null;

			while(scanGPS.hasNext()) {
				rawGPS += scanGPS.next();
			}
			var.close();
			scanGPS.close();
			
			return rawGPS.getBytes("UTF-8");
		}
		catch (FileNotFoundException e) {
			System.out.print("File not found\n"); System.exit(0);
		}
		return null;
	}
	
	private static void saveFile(String str) throws IOException {
		try {
			System.out.print("Save raw file as: ");
			Scanner var = new Scanner(System.in);
			String filename = var.next().trim();

			File fGPX = new File(filename + ".raw");
			FileWriter fstream = new FileWriter(fGPX);
 			BufferedWriter out = new BufferedWriter(fstream);
  			out.write(str);
  			out.close();
  			var.close();

  			System.out.print("GPS raw data saved as: " + fGPX.getPath() + "\n");
		}
		catch (IOException e) {
			System.out.print("IO error\n");
		}
	}
}

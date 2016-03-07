package org.jason.lxcoff.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.jason.lxcoff.lib.Configuration;
import org.jason.lxcoff.lib.ControlMessages;

import android.content.Context;
import android.util.Log;

public class CloneThread implements Runnable {

	private static final String TAG = "ServerThread";

	private Context context;
	private Configuration config; // The configurations read from the file or got form the DirService
	
	public static boolean inuse = false;

	public CloneThread(Context context) {
		this.context = context;
	}

	@Override
	public void run() {

		try {
			//getInfoFromDirService();

/*			config = new Configuration(ControlMessages.CLONE_CONFIG_FILE);
			config.parseConfigFile(null, null);*/
			
			ServerSocket serverSocket = new ServerSocket(4322);
			
			while (true) {
				Socket clientSocket;
				clientSocket = serverSocket.accept();
				new ClientHandler(clientSocket, context);
			}
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Config file not found: " + ControlMessages.CLONE_CONFIG_FILE);
		} catch (IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
		}
	}

	/**
	 * Read the config file to get the IP and port for DirectoryService.
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	private void getInfoFromDirService() throws UnknownHostException, IOException {
		config = new Configuration(ControlMessages.CLONE_CONFIG_FILE);
		config.parseConfigFile(null, null);

		Socket dirServiceSocket = null;
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;

		try {
			//Link to the server which hosts DirectoryService
			dirServiceSocket = new Socket(config.getDirServiceIp(), config.getDirServicePort());
			OutputStream os = dirServiceSocket.getOutputStream();
			InputStream is = dirServiceSocket.getInputStream();

			os.write(ControlMessages.CONTAINER_CONNECTION);

			oos = new ObjectOutputStream(os);
			ois = new ObjectInputStream(is);

			// Send the name and id to DirService
			os.write(ControlMessages.CLONE_AUTHENTICATION);
			oos.writeObject(config.getCloneName());
			oos.writeInt(config.getCloneId());
			oos.flush();

			// Get the port where the clone should listen for connections
			os.write(ControlMessages.GET_PORT_FOR_PHONES);
			config.setClonePortForPhones(ois.readInt());
		}
		finally {
			// Close the connection with the DirService
			try {
				oos.close();
				ois.close();
				dirServiceSocket.close();
			} catch(Exception e) {

			}
		}
	}

}

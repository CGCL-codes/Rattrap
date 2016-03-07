package org.jason.lxcoff.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.Socket;

import org.jason.lxcoff.lib.Clone;
import org.jason.lxcoff.lib.ControlMessages;
import org.jason.lxcoff.lib.ResultContainer;

import android.util.Log;

/**
 * The thread taking care of communication with the clone helpers
 * @author sokolkosta
 *
 */
public class CloneHelperThread extends Thread {

	private String				TAG = "ServerHelper-";
	private Clone				clone;
	Socket						socket;
	OutputStream 				os;
	ObjectOutputStream 			oos;
	InputStream 				is;
	ObjectInputStream 			ois;

	// This id is assigned to the clone helper by the main clone.
	// It is needed for splitting the input when parallelizing a certain method (see for example virusScanning).
	// To not be confused with the id that the clone has read from the config file.
	private int					cloneId;

	public CloneHelperThread(int cloneId, Clone clone)
	{
		this.clone		= clone;
		this.cloneId	= cloneId;
		TAG = TAG + this.cloneId;
	}

	@Override
	public void run() {

		try {

			// Try to connect to the clone helper.
			// If it was not possible to connect stop running. 
			if ( !establishConnection() )
				return;

			// Send the cloneId to this clone.
			sendCloneId();

			while (true) {

				synchronized (ClientHandler.nrClonesReady) {
					Log.d(TAG, "Server Helpers started so far: " + ClientHandler.nrClonesReady.addAndGet(1));
					if(ClientHandler.nrClonesReady.get() >= ClientHandler.numberOfCloneHelpers)
						ClientHandler.nrClonesReady.notifyAll();
				}

				/**
				 * wait() until the main server wakes up the thread
				 * then do something depending on the request
				 */
				synchronized (ClientHandler.syncObject) {
					while(ClientHandler.syncObject[cloneId])
						ClientHandler.syncObject.wait();

					ClientHandler.syncObject[cloneId] = true;
				}

				Log.d(TAG, "Sending command: " + ClientHandler.requestFromMainServer);

				switch(ClientHandler.requestFromMainServer) {

				case ControlMessages.PING:
					pingOtherServer();
					break;

				case ControlMessages.APK_REGISTER:
					os.write(ControlMessages.APK_REGISTER);
					oos.writeObject(ClientHandler.appName);

					int response = is.read();

					if (response == ControlMessages.APK_REQUEST) {
						// Send the APK file if needed
						Log.d(TAG, "Sending apk to the clone " + clone.getIp());

						File apkFile = new File(ClientHandler.apkFilePath);
						FileInputStream fin = new FileInputStream(apkFile);
						BufferedInputStream bis = new BufferedInputStream(fin);
						byte[] tempArray = new byte[(int) apkFile.length()];
						bis.read(tempArray, 0, tempArray.length);

						// Send file length first
						Log.d(TAG, "Sending apk length - " + tempArray.length);
						oos.writeInt(tempArray.length);

						Log.d(TAG, "Sending apk");
						oos.write(tempArray);
						oos.flush();

						bis.close();
					}
					else if(response == ControlMessages.APK_PRESENT)
					{
						Log.d(TAG, "Application already registered on clone " + clone.getIp());
					}
					break;

				case ControlMessages.EXECUTE:
					Log.d(TAG, "Asking clone " + clone.getIp() + " to parallelize the execution");

					os.write(ControlMessages.EXECUTE);

					// Send the number of clones needed.
					// Since this is a helper clone, only one clone should be requested.
					oos.writeInt(1);
					oos.writeObject(ClientHandler.objToExecute);
					oos.writeObject(ClientHandler.methodName);
					oos.writeObject(ClientHandler.pTypes);
					oos.writeObject(ClientHandler.pValues);
					oos.flush();

					/**
					 * This is the response from the clone helper, which is a partial result of the method execution.
					 * This partial result is stored in an array, and will be later composed with the other
					 * partial results of the other clones to obtain the total desired result to be sent
					 * back to the phone.
					 */
					Object cloneResult = ois.readObject();
					ResultContainer container = (ResultContainer) cloneResult;

					Log.d(TAG, "Received response from clone ip: " + clone.getIp() + " port: " + clone.getPortForPhone());
					Log.d(TAG, "Writing in responsesFromServer in position: " + cloneId);
					synchronized (ClientHandler.responsesFromServers) {
						Array.set(ClientHandler.responsesFromServers, cloneId, container.functionResult);
					}
					break;

				case -1:
					closeConnection();
					return;
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e)	{
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		closeConnection();
	}

	private boolean establishConnection()
	{
		try {

			Log.d(TAG, "Trying to connect to clone + " + clone.getIp());

			socket	= new Socket(clone.getIp(), clone.getPortForPhone());
			os		= socket.getOutputStream();
			oos		= new ObjectOutputStream(os);
			is		= socket.getInputStream();
			ois		= new ObjectInputStream(is);

			Log.d(TAG, "Connection established whith clone " + clone.getIp());

			return true;
		}
		catch (Exception e) {
			Log.e(TAG, "Exception not caught properly - " + e);
			return false;
		} catch (Error e) {
			Log.e(TAG, "Error not caught properly - " + e);
			return false;
		}
	}

	private void pingOtherServer()
	{
		try {
			// Send a message to the Server Helper (other server)
			Log.d(TAG, "PING other server");
			os.write(ControlMessages.PING);

			// Read and display the response message sent by server helper
			int response = is.read();

			if (response == ControlMessages.PONG)
				Log.d(TAG, "PONG from other server: " + clone.getIp() + ":" + clone.getPortForPhone());
			else {
				Log.d(TAG, "Bad Response to Ping - " + response);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendCloneId() throws IOException {
		os.write(ControlMessages.CLONE_ID_SEND);
		os.write(cloneId);
	}

	private void closeConnection() {
		try {
			ois.close();
			oos.close();
			socket.close();
			Log.d(TAG, "Bye bye");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}



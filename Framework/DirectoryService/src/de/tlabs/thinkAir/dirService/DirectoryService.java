
package de.tlabs.thinkAir.dirService;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.jason.lxcoff.lib.Clone;
import org.jason.lxcoff.lib.Configuration;
import org.jason.lxcoff.lib.ControlMessages;
import org.jason.lxcoff.lib.Clone.CloneState;

/**
 * This class is responsible for starting the clones needed for the C2C platform and to give the needed info to the phones.
 * It reads the configuration file for the number of clones that has to be started and for the other info.
 * @author Jasonniu
 */
public class DirectoryService implements Runnable{

	private ServerSocket serverSocket;
	protected Logger log=Logger.getLogger(this.getClass().getName());  

	public static boolean[]			cloneIsStarted = new boolean[ControlMessages.MAX_NUM_CLIENTS];	// Indexes will serve as cloneId
	public static ArrayList<Clone>	vbClones;
	public static ArrayList<Clone>	amazonClones;
	private static Configuration			config;

	public static void main(String[] args) {

		new Thread( new DirectoryService() ).start();

	}

	@Override
	public void run() {

		vbClones 		= new ArrayList<Clone>();
		amazonClones 	= new ArrayList<Clone>();
		config			= new Configuration(ControlMessages.DIRSERVICE_CONFIG_FILE);

		System.out.println("Connecting database and initing dbh...");
		DBHelper dbh = new DBHelper();
		
		try {
			config.parseConfigFile(vbClones, amazonClones);
			//config.printConfigFile();
			
			serverSocket = new ServerSocket(config.getDirServicePort());
			while (true) {

				//System.out.println("Waiting for clients on port: " + config.getDirServicePort());
				log.info("Waiting for clients on port: " + config.getDirServicePort());
				Socket clientSocket = serverSocket.accept();
				InputStream	is 	= clientSocket.getInputStream();
				OutputStream os	= clientSocket.getOutputStream();

				int whatIsThisClient = is.read();
				log.info("New client connected is: " + whatIsThisClient);
				//System.out.println("New client connected is: " + whatIsThisClient);

				if ( whatIsThisClient == ControlMessages.CLONE_CONNECTION ){
					log.info("Starting the CloneHandler");
					new Thread (new CloneHandler(clientSocket, is, os, config) ).start();
				}
				else if ( whatIsThisClient == ControlMessages.PHONE_CONNECTION ){
					log.info("Starting the PhoneHandler");
					new Thread( new PhoneHandler(clientSocket, is, os, dbh) ).start();
				
				}	
/*				else if ( whatIsThisClient == ControlMessages.CONTAINER_CONNECTION ){
					log.info("Starting the ContainerHandler");
					new Thread (new ContainerHandler(clientSocket, is, os, config) ).start();
				}*/
				else
					System.out.println("Client does not identify himself neither as Phone nor as Clone.");

			}

		} catch (FileNotFoundException e) {
			System.err.println("Configuration file not found, exiting...");
			return;
		} catch (IOException e) {
			System.err.println("Could not start server");
			e.printStackTrace();
			System.exit(-1);
		} catch (IllegalStateException e) {
			System.out.println( e.getMessage() );
		}finally {
			try {
				serverSocket.close();
				dbh.dbClose();
				System.err.println("Socket is now closed correctly");
			} catch (Exception e) {
				System.err.println("Socket was never opened");
			}
		}
	}

	private void initializeClones() {
		for (Clone c : vbClones) {
			c.initializeClone(config);
			c.describeClone();
		}

		for (Clone c : amazonClones) {
			c.initializeClone(config);
			c.describeClone();
		}
	}

	private void startClones() {
		new Thread(new CloneStarter()).start();
	}
	
	/**
	 * The main clone is asking for some helper clones.
	 * Try to find nrClones started and available clones (those who have authenticated to the directory service)
	 * and return the list containing them.
	 * @param nrClones Number of helper clones asked by the main clone.
	 * @return The array containing the helper clones.
	 * 
	 * TODO: consider the case when the number of available clones is not sufficient
	 * so we have to start some more clones ad-hoc.
	 */
	public static ArrayList<Clone> getAvailableClones(int nrClones) {
		ArrayList<Clone> helperClones = new ArrayList<Clone>();
		
		switch (config.getSetupType()) {
		case LOCAL:
			helperClones = getAvailableVBClones(nrClones);
			break;

		case AMAZON:
			helperClones = getAvailableAmazonClones(nrClones);
			break;

		case HYBRID:
			helperClones = getAvailableVBClones(nrClones);
			helperClones.addAll( getAvailableAmazonClones(nrClones - helperClones.size()) );
			break;
		}
		
		return helperClones;
	}
	
	private static ArrayList<Clone> getAvailableVBClones(int nrClones) {
		ArrayList<Clone> helperClones = new ArrayList<Clone>();
		
		for (Clone c : vbClones) {
			if (c.getStatus() == CloneState.AUTHENTICATED)
				helperClones.add(c);
		}
		
		return helperClones;
	}

	private static ArrayList<Clone> getAvailableAmazonClones(int nrClones) {
		ArrayList<Clone> helperClones = new ArrayList<Clone>();
		
		for (Clone c : amazonClones) {
			if (c.getStatus() == CloneState.AUTHENTICATED)
				helperClones.add(c);
		}
		
		return helperClones;
	}
	
	private class CloneStarter implements Runnable {

		public void run() {
			
			switch (config.getSetupType()) {
			case LOCAL:
				startVBClones();
				break;

			case AMAZON:
				startAmazonClones();
				break;

			case HYBRID:
				startVBClones();
				startAmazonClones();
				break;
			}
		}
		
		private void startVBClones() {
			for (int i = 0; i < config.getNrClonesVBToStartOnStartup(); i++) {
				Clone c = vbClones.get(i); 
				if (c.getStatus() == CloneState.STOPPED) {
					c.startVBClone();
					// Sleep some seconds so to let the clones start in turn
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
					}
				}
			}
		}
		
		private void startAmazonClones() {
			for (int i = 0; i < config.getNrClonesAmazonToStartOnStartup(); i++) {
				Clone c = amazonClones.get(i); 
				if (c.getStatus() == CloneState.STOPPED) {
					c.startAmazonClone();
					// Sleep some seconds so to let the clones start in turn
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
					}
				}
			}
		}
		
	}

}


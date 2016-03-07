package de.tlabs.thinkAir.dirService;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.jason.lxcoff.lib.Clone;
import org.jason.lxcoff.lib.Configuration;
import org.jason.lxcoff.lib.ControlMessages;
import org.jason.lxcoff.lib.Clone.CloneState;
import org.jason.lxcoff.lib.Clone.CloneType;

public class ContainerHandler implements Runnable {

	private static AtomicInteger 	numberOfClonesConnected = new AtomicInteger(0);
	private static String			cloneName;
	private static String			cloneIP;
	private static int				cloneId;
	
	private Configuration			config;
	private Socket					clientSocket;
	private InputStream				is;
	private OutputStream			os;
	private ObjectOutputStream 		oos;
	private ObjectInputStream 		ois;
	
	protected Logger log=Logger.getLogger(this.getClass().getName());

	public ContainerHandler(Socket clientSocket, InputStream is, OutputStream os, Configuration config) {
		this.clientSocket 	= clientSocket;
		this.is				= is;
		this.os				= os;
		this.config			= config;
		
		cloneIP 			= ((InetSocketAddress) clientSocket.getRemoteSocketAddress()).getHostName();
		//cloneIP = "192.168.155.2";
	}

	@Override
	public void run() {

//		cloneId = numberOfClonesConnected.incrementAndGet();
		
		//System.out.println("Waiting for commands from the clone...");
		log.info("Waiting for commands from the clone..." + cloneIP);
		Clone currentClone = null;
		
		int command = 0;

		try{
			ois = new ObjectInputStream(is);
			oos = new ObjectOutputStream(os);
			
			while (command != -1) {	
				command = is.read();
				//System.out.println("Command: " + command);
				log.info("Command: " + command);

				switch(command) {

				// Get the name and the ID of this clone.
				// Send the ports where to listen for phones and for clones 
				case ControlMessages.CLONE_AUTHENTICATION:
					cloneName 	= (String) ois.readObject();
					cloneId		= ois.readInt();
					
					//System.err.println("The clone with name = " + cloneName + " and id = " + cloneId + " is connected");
					log.info("The clone with name = " + cloneName + " and id = " + cloneId + " is connected");
					
					currentClone = findTheClone(cloneName);
					if (currentClone == null) {
						System.err.println("Clone not recognized, maybe it was not included in the configuration file\n" +
								"Creating a new clone object for this one.");
						
						currentClone = new Clone(cloneName);
						
						switch (Clone.detectType(cloneName)) { 
						case VIRTUALBOX:
							DirectoryService.vbClones.add(currentClone);
							break;
							
						case AMAZON:
							DirectoryService.amazonClones.add(currentClone);
							break;
							
						default:
							System.err.println("I don't know where to put the new connected clone, putting it in the VirtualBox list");
							Clone.printInfoAboutCloneName();
						}
					}
					else {
						//System.out.println("The clone with name " + cloneName + " was found");
						log.info("The clone with name " + cloneName + " was found");

						assert currentClone.getId() == cloneId;
					}

					DirectoryService.cloneIsStarted[cloneId] = true;
					currentClone.setId(cloneId);
					currentClone.initializeClone(config);
					currentClone.setStatus(CloneState.AUTHENTICATED);
					currentClone.setIp(cloneIP);
					
					break;
					
				case ControlMessages.GET_PORT_FOR_PHONES:
					// The clone is asking for the port where to listen for new connections.
					// This port will be the same for phones' and clones' connections.
					oos.writeInt(config.getClonePortForPhones());
					oos.flush();
					break;
					
				case ControlMessages.NEED_CLONE_HELPERS:
					/**
					 * The clone is asking for more clones able to help him parallelize the execution.
					 * Look if these clones can be found, or try to start them otherwise. 
					 */
					int nrCloneHelpers = is.read();
					System.out.println("The clone asked for " + nrCloneHelpers + " helper clones.");
					ArrayList<Clone> cloneHelpers = DirectoryService.getAvailableClones(nrCloneHelpers);
					
					System.out.println("Sending " + cloneHelpers.size() + " helper clones.");
					oos.writeObject(cloneHelpers);
					oos.flush();
					
					break;
					
					
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				oos.close();
				ois.close();
				clientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Given the clone name return the clone object with that name.
	 * @param cloneName
	 * @return c The clone object having the name cloneName
	 */
	private Clone findTheClone(String cloneName) {
		
		for (Clone c : DirectoryService.vbClones)
			if ( c.getName().equalsIgnoreCase(cloneName) )
				return c;
		
		for (Clone c : DirectoryService.amazonClones)
			if ( c.getName().equalsIgnoreCase(cloneName) )
				return c;
		
		return null;
	}
}

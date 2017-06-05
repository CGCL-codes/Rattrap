package de.tlabs.thinkAir.dirService;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import org.jason.lxcoff.lib.Clone;
import org.jason.lxcoff.lib.Configuration;
import org.jason.lxcoff.lib.Clone.CloneState;

public class CloneHandler implements Runnable {
	private static AtomicInteger numberOfClonesConnected = new AtomicInteger(0);
	private static String cloneName;
	private static String cloneIP;
	private static int cloneId;
	private Configuration config;
	private Socket clientSocket;
	private InputStream is;
	private OutputStream os;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	protected Logger log = Logger.getLogger(this.getClass().getName());

	public CloneHandler(Socket clientSocket, InputStream is, OutputStream os, Configuration config) {
		this.clientSocket = clientSocket;
		this.is = is;
		this.os = os;
		this.config = config;
		cloneIP = ((InetSocketAddress)clientSocket.getRemoteSocketAddress()).getHostName();
	}

	public void run() {
		this.log.info("Waiting for commands from the clone..." + cloneIP);
		Clone currentClone = null;
		int command = 0;

		try {
			this.ois = new ObjectInputStream(this.is);
			this.oos = new ObjectOutputStream(this.os);

			while(command != -1) {
				command = this.is.read();
				this.log.info("Command: " + command);
				switch(command) {
					case 32:
						int nrCloneHelpers = this.is.read();
						System.out.println("The clone asked for " + nrCloneHelpers + " helper clones.");
						ArrayList<Clone> cloneHelpers = DirectoryService.getAvailableClones(nrCloneHelpers);
						System.out.println("Sending " + cloneHelpers.size() + " helper clones.");
						this.oos.writeObject(cloneHelpers);
						this.oos.flush();
						break;
					case 33:
						cloneName = (String)this.ois.readObject();
						cloneId = this.ois.readInt();
						this.log.info("The clone with name = " + cloneName + " and id = " + cloneId + " is connected");
						currentClone = this.findTheClone(cloneName);
						if(currentClone == null) {
							System.err.println("Clone not recognized, maybe it was not included in the configuration file\nCreating a new clone object for this one.");
							currentClone = new Clone(cloneName);
							switch(Clone.detectType(cloneName).ordinal() + 1) {
								case 2:
									DirectoryService.vbClones.add(currentClone);
									break;
								case 3:
									DirectoryService.amazonClones.add(currentClone);
									break;
								default:
									System.err.println("I don't know where to put the new connected clone, putting it in the VirtualBox list");
									Clone.printInfoAboutCloneName();
							}
						} else {
							this.log.info("The clone with name " + cloneName + " was found");

							assert currentClone.getId() == cloneId;
						}

						DirectoryService.cloneIsStarted[cloneId] = true;
						currentClone.setId(cloneId);
						currentClone.initializeClone(this.config);
						currentClone.setStatus(CloneState.AUTHENTICATED);
						currentClone.setIp(cloneIP);
					case 34:
					case 35:
					case 36:
					default:
						break;
					case 37:
						this.oos.writeInt(this.config.getClonePortForPhones());
						this.oos.flush();
				}
			}
		} catch (IOException var15) {
			var15.printStackTrace();
		} catch (ClassNotFoundException var16) {
			var16.printStackTrace();
		} finally {
			try {
				this.oos.close();
				this.ois.close();
				this.clientSocket.close();
			} catch (IOException var14) {
				var14.printStackTrace();
			}

		}

	}

	private Clone findTheClone(String cloneName) {
		Iterator var3 = DirectoryService.vbClones.iterator();

		Clone c;
		while(var3.hasNext()) {
			c = (Clone)var3.next();
			if(c.getName().equalsIgnoreCase(cloneName)) {
				return c;
			}
		}

		var3 = DirectoryService.amazonClones.iterator();

		while(var3.hasNext()) {
			c = (Clone)var3.next();
			if(c.getName().equalsIgnoreCase(cloneName)) {
				return c;
			}
		}

		return null;
	}
}

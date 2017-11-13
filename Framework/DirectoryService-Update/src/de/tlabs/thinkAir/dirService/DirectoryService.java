package de.tlabs.thinkAir.dirService;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;
import org.jason.lxcoff.lib.Clone;
import org.jason.lxcoff.lib.Configuration;
import org.jason.lxcoff.lib.Clone.CloneState;

public class DirectoryService implements Runnable {
	private ServerSocket serverSocket;
	protected Logger log = Logger.getLogger(this.getClass().getName());
	public static boolean[] cloneIsStarted = new boolean[32];
	public static ArrayList<Clone> vbClones;
	public static ArrayList<Clone> amazonClones;
	private static Configuration config;
	public static String ipAddress = "";

	public DirectoryService() {
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: java -jar dirService.jar ipAddress");
			System.exit(-1);
		}
		ipAddress = args[0];
		(new Thread(new DirectoryService())).start();
	}

	public void run() {
		vbClones = new ArrayList();
		amazonClones = new ArrayList();
		config = new Configuration("/root/dirservice/config-dirservice.dat");
		System.out.println("Connecting database and initing dbh...");
		DBHelper dbh = new DBHelper();

		try {
			config.parseConfigFile(vbClones, amazonClones);
			this.serverSocket = new ServerSocket(config.getDirServicePort());

			while(true) {
				while(true) {
					this.log.info("Waiting for clients on port: " + config.getDirServicePort());
					Socket clientSocket = this.serverSocket.accept();
					InputStream is = clientSocket.getInputStream();
					OutputStream os = clientSocket.getOutputStream();
					int whatIsThisClient = is.read();
					this.log.info("New client connected is: " + whatIsThisClient);
					if(whatIsThisClient == 31) {
						this.log.info("Starting the CloneHandler");
						(new Thread(new CloneHandler(clientSocket, is, os, config))).start();
					} else if(whatIsThisClient == 30) {
						this.log.info("Starting the PhoneHandler");
						(new Thread(new PhoneHandler(clientSocket, is, os, dbh))).start();
					} else {
						System.out.println("Client does not identify himself neither as Phone nor as Clone.");
					}
				}
			}
		} catch (FileNotFoundException var17) {
			System.err.println("Configuration file not found, exiting...");
		} catch (IOException var18) {
			System.err.println("Could not start server");
			var18.printStackTrace();
			System.exit(-1);
			return;
		} catch (IllegalStateException var19) {
			System.out.println(var19.getMessage());
			return;
		} finally {
			try {
				this.serverSocket.close();
				dbh.dbClose();
				System.err.println("Socket is now closed correctly");
			} catch (Exception var16) {
				System.err.println("Socket was never opened");
			}

		}

	}

	private void initializeClones() {
		Iterator var2 = vbClones.iterator();

		Clone c;
		while(var2.hasNext()) {
			c = (Clone)var2.next();
			c.initializeClone(config);
			c.describeClone();
		}

		var2 = amazonClones.iterator();

		while(var2.hasNext()) {
			c = (Clone)var2.next();
			c.initializeClone(config);
			c.describeClone();
		}

	}

	private void startClones() {
		(new Thread(new DirectoryService.CloneStarter())).start();
	}

	public static ArrayList<Clone> getAvailableClones(int nrClones) {
		ArrayList<Clone> helperClones = new ArrayList();
		switch(config.getSetupType().ordinal() + 1) {
			case 1:
				helperClones = getAvailableVBClones(nrClones);
				break;
			case 2:
				helperClones = getAvailableAmazonClones(nrClones);
				break;
			case 3:
				helperClones = getAvailableVBClones(nrClones);
				helperClones.addAll(getAvailableAmazonClones(nrClones - helperClones.size()));
		}

		return helperClones;
	}

	private static ArrayList<Clone> getAvailableVBClones(int nrClones) {
		ArrayList<Clone> helperClones = new ArrayList();
		Iterator var3 = vbClones.iterator();

		while(var3.hasNext()) {
			Clone c = (Clone)var3.next();
			if(c.getStatus() == CloneState.AUTHENTICATED) {
				helperClones.add(c);
			}
		}

		return helperClones;
	}

	private static ArrayList<Clone> getAvailableAmazonClones(int nrClones) {
		ArrayList<Clone> helperClones = new ArrayList();
		Iterator var3 = amazonClones.iterator();

		while(var3.hasNext()) {
			Clone c = (Clone)var3.next();
			if(c.getStatus() == CloneState.AUTHENTICATED) {
				helperClones.add(c);
			}
		}

		return helperClones;
	}

	private class CloneStarter implements Runnable {
		private CloneStarter() {
		}

		public void run() {
			switch(config.getSetupType().ordinal() + 1) {
				case 1:
					this.startVBClones();
					break;
				case 2:
					this.startAmazonClones();
					break;
				case 3:
					this.startVBClones();
					this.startAmazonClones();
			}

		}

		private void startVBClones() {
			for(int i = 0; i < DirectoryService.config.getNrClonesVBToStartOnStartup(); ++i) {
				Clone c = (Clone)DirectoryService.vbClones.get(i);
				if(c.getStatus() == CloneState.STOPPED) {
					c.startVBClone();

					try {
						Thread.sleep(10000L);
					} catch (InterruptedException var4) {
						;
					}
				}
			}

		}

		private void startAmazonClones() {
			for(int i = 0; i < DirectoryService.config.getNrClonesAmazonToStartOnStartup(); ++i) {
				Clone c = (Clone)DirectoryService.amazonClones.get(i);
				if(c.getStatus() == CloneState.STOPPED) {
					c.startAmazonClone();

					try {
						Thread.sleep(10000L);
					} catch (InterruptedException var4) {
						;
					}
				}
			}

		}
	}
}

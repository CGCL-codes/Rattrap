package org.jason.lxcoff.lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.security.PublicKey;

public class Clone implements Serializable {

	//private static final long serialVersionUID = -6097868122333778588L;
	private static final long serialVersionUID = 1L;

	private int 			id = -1;
	private String 			name; // Especially useful for VirtualBox clones, the name is used to start/pause/stop the clones
	private String 			ip = null;
	private CloneState		status;
	private int				portForPhone;
	private int				portForClone;
	private int				portForDir = 4322;
	private CloneType		type;
	private PublicKey		publicKey;

	public Clone() {
		this.status = CloneState.STOPPED;
	}

	public Clone(String name) {
		this.name	= name;
		this.status = CloneState.STOPPED;;
		this.type	= detectType(this.name);
	}

	public Clone(String name, String ip) {
		this.name	= name;
		this.ip		= ip;
		this.status = CloneState.STOPPED;;
	}

	public enum CloneType {
		UNKNOWN, VIRTUALBOX, AMAZON
	}
	
	public enum CloneState {
		UNKNOWN, STOPPED, PAUSED, RESUMED, AUTHENTICATED, ASSIGNED_TO_PHONE, STARTING
	}
	
	/**
	 * Detect the type of the clone from the name
	 */
	public static CloneType detectType(String name) {
		if (name.startsWith("vb-"))
			return CloneType.VIRTUALBOX;
		else if (name.startsWith("amazon-"))
			return CloneType.AMAZON;
		else {
			System.err.println("The type of this clone could not be determined");
			printInfoAboutCloneName();
			return CloneType.UNKNOWN;
		}
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the ip
	 */
	public String getIp() {
		if(ip != null){
			return ip;
		}else{
			String out = executeCommand("/root/cloneroot/dirservice/getip.sh " + this.name);
			this.ip = out.trim();
			return ip;
		}
	}

	/**
	 * @param ip the ip to set
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * @return the status
	 */
	public CloneState getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(CloneState status) {
		this.status = status;
	}

	/**
	 * @return the portForPhone
	 */
	public int getPortForPhone() {
		return portForPhone;
	}

	/**
	 * @param portForPhone the portForPhone to set
	 */
	public void setPortForPhone(int portForPhone) {
		this.portForPhone = portForPhone;
	}

	/**
	 * @return the portForClone
	 */
	public int getPortForClone() {
		return portForClone;
	}
	
	/**
	 * @return the portForClone
	 */
	public int getPortForDir() {
		return this.portForDir;
	}

	/**
	 * @param portForClone the portForClone to set
	 */
	public void setPortForClone(int portForClone) {
		this.portForClone = portForClone;
	}

	/**
	 * @return the type
	 */
	public CloneType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(CloneType type) {
		this.type = type;
	}

	/**
	 * @return the publicKey
	 */
	public PublicKey getPublicKey() {
		return publicKey;
	}

	/**
	 * @param publicKey the publicKey to set
	 */
	public void setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}

	public void describeClone() {
		System.out.println(name + " " + id + " " + ip);
	}

	public void initializeClone(Configuration config) {
		this.portForClone	= config.getClonePortForClones();
		this.portForPhone	= config.getClonePortForPhones();
	}

	/**
	 * Find which is the status of this clone and make it available for the phone.
	 * @return True if it was possible to make the clone available<br>
	 * False otherwise
	 */
	public boolean prepareClone() {

		switch(this.type) {

		case VIRTUALBOX:

			System.out.println("Preparing the Virtualbox clone " + this.name);

			switch (getTheStateOfPhysicalMachine()) {

			case STOPPED:
				if (startVBClone()) {
					System.out.println("Started the Virtualbox clone " + this.name);
					this.status = CloneState.STARTING;
					return true;
				}
				else {
					System.err.println("Could not start the Virtualbox clone " + this.name);
					this.status = CloneState.STOPPED;
					return false;
				}

			case PAUSED:
				if (resumeVBClone()) {
					System.out.println("Resumed the Virtualbox clone " + this.name);
					this.status = CloneState.STARTING;
					return true;
				}
				else {
					System.err.println("Could not resume the Virtualbox clone " + this.name);
					this.status = CloneState.STOPPED;
					return false;
				}

				
				
			case RESUMED:
				System.out.println("The Virtualbox clone " + this.name + " was already started");
				return true;
			}

			return false;

		case AMAZON:
			System.out.println("Preparing the Amazon clone " + this.name);
			System.err.println("Not yet implemented for the amazon clones");
			break;

		case UNKNOWN:
			System.err.println("I don't know how to start the clone " + this.name);
			printInfoAboutCloneName();
			break;
		}

		return false;
	}

	// Methods to control the local VirtualBox clones.

	private CloneState getTheStateOfPhysicalMachine() {
		switch (this.type) {

		case VIRTUALBOX:
			String out = executeCommand("VBoxManage showvminfo " + this.name);
			
			if (out.contains("powered off (since")) {
				return CloneState.STOPPED;
			}
			else if (out.contains("running (since")) {
				return CloneState.RESUMED;
			}
			else if (out.contains("paused (since")) {
				return CloneState.PAUSED;
			}

			break;

		case AMAZON:
			System.err.println("Not yet implemented for the amazon clones");
			break;

		case UNKNOWN:
			System.err.println("I cant't get the physical state of the clone " + this.name);
			printInfoAboutCloneName();
			break;
		}

		return CloneState.UNKNOWN;
	}

	/**
	 * Start a VB clone
	 * @throws IllegalStateException
	 */
	public boolean startVBClone() {

		String out = executeCommand("VBoxManage startvm " + this.name + " --type headless");
		executeCommandWithoutResponse("/root/cloneroot/dirservice/start_monitor.sh");
		
		if (out.contains("has been successfully started.")) 
			return true;

		return false;
	}

	public boolean resumeVBClone() {
		executeCommand("VBoxManage controlvm " + this.name + " resume");

		switch(getTheStateOfPhysicalMachine()) {
		case STOPPED:
			return false;
		case PAUSED:
			return false;
		case RESUMED:
			return true;
		}

		return false;
	}

	public void pauseVBClone() {

	}

	public void stopVBClone() {

	}

	// Methods to control the Amazon clones
	public void startAmazonClone() {

	}

	public void resumeAmazonClone() {

	}

	public void pauseAmazonClone() {

	}

	public void stopAmazonClone() {

	}

	private String executeCommand(String command) {

		try {
			Process p = Runtime.getRuntime().exec(command);
			// you can pass the system command or a script to exec command.
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

			// read the output from the command
			StringBuilder sb = new StringBuilder();
			String s = "";
			while ((s = stdInput.readLine()) != null) {
//				System.out.println("Std OUT: "+s);
				sb.append(s);
			}

			while ((s = stdError.readLine()) != null) {
				System.out.println("Std ERROR : "+s);
			}

			return sb.toString();

		} catch (IOException e) {

			e.printStackTrace();
		}
		return null;
	}
	
	private void executeCommandWithoutResponse(String command) {
		try {
			Process p = Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void printInfoAboutCloneName() {
		System.out.println("The name of the clone should start with vb- for VirtualBox clones, and with amazon- for Amazon clones.");
	}
}





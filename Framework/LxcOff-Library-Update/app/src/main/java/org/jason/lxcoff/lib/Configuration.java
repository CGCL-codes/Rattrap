package org.jason.lxcoff.lib;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

import org.jason.lxcoff.lib.ControlMessages.SETUP_TYPE;

public class Configuration {

	private String				configFilePath;

	private String				dirServiceIp;
	private int					dirServiceport;
	private SETUP_TYPE 			setupType; // Local, Amazon, Hybrid
	private int					nrClonesVBToStartOnStartup 		= 0;
	private int					nrClonesAmazonToStartOnStartup 	= 0;
	private int					clonePortForClones;
	private int					clonePortForPhones;
	private String				cloneName;
	private int					cloneId;
	
	public Configuration (String configFilePath) {
		this.configFilePath = configFilePath;
	}
	
	/**
	 * Read the configuration file with the following format: <br>
	 * # Comment <br>
	 * [Category] <br>
	 * @throws FileNotFoundException 
	 * 
	 */
	public void parseConfigFile(ArrayList<Clone> vbClones, ArrayList<Clone> amazonClones) throws FileNotFoundException {
		Scanner configFileScanner = new Scanner(new FileReader(configFilePath) );

		try {
			while (configFileScanner.hasNext()) {
				
				// Get the next line of the file and remove any extra spaces
				String line = configFileScanner.nextLine().trim();
				
				// Empty line or comment
				if ( line.length() == 0 || line.startsWith("#") ) continue;
				
				else if (line.equals(ControlMessages.DIRSERVICE_IP)) {
					setDirServiceIp(configFileScanner.nextLine().trim());
				}
				
				else if (line.equals(ControlMessages.DIRSERVICE_PORT)) {
					setDirServicePort(configFileScanner.nextInt());
				}
				
				// If this is the type of the C2C platform to create.
				// Expected one of the alternatives: Local, Amazon, or Hybrid.
				// For the moment this implementation does not support Hybrid configuration in automatic way. 
				else if ( line.equals(ControlMessages.CLONE_TYPES) ) {
					String temp = configFileScanner.nextLine().trim();
					if (temp.equals("Local"))
						setSetupType(ControlMessages.SETUP_TYPE.LOCAL);
					else if (temp.equals("Amazon"))
						setSetupType(ControlMessages.SETUP_TYPE.AMAZON);
					else if (temp.equals("Hybrid"))
						setSetupType(ControlMessages.SETUP_TYPE.HYBRID);
				}
				
				// If this is the line containing the number of VB clones to start in this phase.
				// Next line should be a number indicating the number of clones.
				else if ( line.equals(ControlMessages.NO_CLONES_VB_TO_START) ) {
					setNrClonesVBToStartOnStartup(configFileScanner.nextInt());
				}
				
				// If this is the line containing the number of Amazon clones to start in this phase.
				// Next line should be a number indicating the number of clones.
				else if ( line.equals(ControlMessages.NO_CLONES_AMAZON_TO_START) ) {
					setNrClonesAmazonToStartOnStartup(configFileScanner.nextInt());
				}
				
				// This is the port where the clones will listen for other clone connections
				else if ( line.equals(ControlMessages.PORT_FOR_CLONES) ) {
					setClonePortForClones(configFileScanner.nextInt());
				}
				
				// This is the port where the clones will listen for phone connections
				else if ( line.equals(ControlMessages.PORT_FOR_PHONES) ) {
					clonePortForPhones = configFileScanner.nextInt();
				}
				
				else if ( line.equals(ControlMessages.CLONE_NAME) ) {
					cloneName = configFileScanner.nextLine();
				}
				
				else if ( line.equals(ControlMessages.CLONE_ID) ) {
					cloneId = configFileScanner.nextInt();
				}
				
				// Now there will be a list of clone names that can be used for starting the clones on VB 
				else if ( line.equals(ControlMessages.VB_CLONES) ) {
					String name = configFileScanner.nextLine().trim();
					while (name.length() > 0) {
						vbClones.add(new Clone(name));
						name = configFileScanner.nextLine().trim();
					}
				}
				
				// Now there will be a list of clone names (ips) that can be used for starting the clones on Amazon 
				if ( line.equals(ControlMessages.AMAZON_CLONES) ) {
					String name = configFileScanner.nextLine().trim();
					while (name.length() > 0) {
						amazonClones.add(new Clone(name));
						name = configFileScanner.nextLine().trim();
					}
				}
			}
		}
		finally {
			configFileScanner.close();
		}
	}

	public String getDirServiceIp() {
		return dirServiceIp;
	}

	public void setDirServiceIp(String dirServiceIp) {
		this.dirServiceIp = dirServiceIp;
	}

	/**
	 * @return the port
	 */
	public int getDirServicePort() {
		return dirServiceport;
	}

	/**
	 * @param port the port to set
	 */
	public void setDirServicePort(int port) {
		this.dirServiceport = port;
	}

	/**
	 * @return the setupType
	 */
	public SETUP_TYPE getSetupType() {
		return setupType;
	}

	/**
	 * @param type The setup type to set: Local, Amazon, Hybrid
	 */
	public void setSetupType(SETUP_TYPE type) {
		
		System.out.println("Setting the type to: " + type);
		
		this.setupType = type;
	}

	/**
	 * @return the nrClonesVBToStartOnStartup
	 */
	public int getNrClonesVBToStartOnStartup() {
		return nrClonesVBToStartOnStartup;
	}

	/**
	 * @param nrClonesVBToStartOnStartup the nrClonesVBToStartOnStartup to set
	 */
	public void setNrClonesVBToStartOnStartup(int nrClonesVBToStartOnStartup) {
		this.nrClonesVBToStartOnStartup = nrClonesVBToStartOnStartup;
	}

	/**
	 * @return the nrClonesAmazonToStartOnStartup
	 */
	public int getNrClonesAmazonToStartOnStartup() {
		return nrClonesAmazonToStartOnStartup;
	}

	/**
	 * @param nrClonesAmazonToStartOnStartup the nrClonesAmazonToStartOnStartup to set
	 */
	public void setNrClonesAmazonToStartOnStartup(
			int nrClonesAmazonToStartOnStartup) {
		this.nrClonesAmazonToStartOnStartup = nrClonesAmazonToStartOnStartup;
	}

	/**
	 * @return the clonePortForClones
	 */
	public int getClonePortForClones() {
		return clonePortForClones;
	}

	/**
	 * @param clonePortForClones the clonePortForClones to set
	 */
	public void setClonePortForClones(int clonePortForClones) {
		this.clonePortForClones = clonePortForClones;
	}

	/**
	 * @return the clonePortForPhones
	 */
	public int getClonePortForPhones() {
		return clonePortForPhones;
	}

	/**
	 * @param clonePortForPhones the clonePortForPhones to set
	 */
	public void setClonePortForPhones(int clonePortForPhones) {
		this.clonePortForPhones = clonePortForPhones;
	}

	public String getCloneName() {
		return cloneName;
	}

	public void setCloneName(String cloneName) {
		this.cloneName = cloneName;
	}

	public int getCloneId() {
		return cloneId;
	}

	public void setCloneId(int cloneId) {
		this.cloneId = cloneId;
	}

	public void printConfigFile() {
		System.out.println(setupType);
		System.out.println(nrClonesVBToStartOnStartup);
		System.out.println(nrClonesAmazonToStartOnStartup);
		System.out.println(clonePortForClones);
		System.out.println(clonePortForPhones);
	}
}

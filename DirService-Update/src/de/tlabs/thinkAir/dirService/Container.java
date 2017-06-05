package de.tlabs.thinkAir.dirService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Container implements Serializable {
	private static final long serialVersionUID = 1L;
	private int id = -1;
	private String name;
	private String ip = "10.0.3.2";
	private int status;
	private int portForDir = 4322;
	private Container.ContainerType type;
	private int mem;
	private String cpuset;
	private int cpushare;
	private DBHelper dbh;

	public Container() {
		this.type = Container.ContainerType.LXC;
		this.mem = 50;
		this.cpuset = null;
		this.cpushare = 1024;
		this.dbh = null;
		this.status = -1;
	}

	public Container(String name) {
		this.type = Container.ContainerType.LXC;
		this.mem = 50;
		this.cpuset = null;
		this.cpushare = 1024;
		this.dbh = null;
		this.name = name;
		this.status = -1;
	}

	public Container(String name, String ip, int status) {
		this.type = Container.ContainerType.LXC;
		this.mem = 50;
		this.cpuset = null;
		this.cpushare = 1024;
		this.dbh = null;
		this.name = name;
		this.ip = ip;
		this.status = status;
	}

	public Container(String name, String ip, int status, int mem, String cpuset, int cpushare) {
		this.type = Container.ContainerType.LXC;
		this.mem = 50;
		this.cpuset = null;
		this.cpushare = 1024;
		this.dbh = null;
		this.name = name;
		this.ip = ip;
		this.status = status;
		this.mem = mem;
		this.cpuset = cpuset;
		this.cpushare = cpushare;
	}

	protected void finalize() throws Throwable {
		super.finalize();
		this.dbh.dbClose();
		System.out.println("The Container Object was destroyed!");
	}

	public static Container.ContainerType detectType(String name) {
		if(name.startsWith("vb-")) {
			return Container.ContainerType.VIRTUALBOX;
		} else if(name.startsWith("amazon-")) {
			return Container.ContainerType.AMAZON;
		} else {
			System.err.println("The type of this clone could not be determined");
			printInfoAboutContainerName();
			return Container.ContainerType.UNKNOWN;
		}
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIp() {
		return this.ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getStatus() {
		try {
			if(this.dbh == null) {
				this.dbh = new DBHelper();
			}

			ResultSet rs = this.dbh.dbSelect("select status from lxc where name = '" + this.name + "'");
			if(rs.next()) {
				int status = rs.getInt("status");
				this.status = status;
			}
		} catch (SQLException var3) {
			var3.printStackTrace();
		}

		return this.status;
	}

	public void setStatus(int status) {
		try {
			if(this.dbh == null) {
				this.dbh = new DBHelper();
			}

			this.dbh.dbUpdate("update lxc set status = " + status + " where name = '" + this.name + "'");
		} catch (Exception var3) {
			var3.printStackTrace();
		}

		this.status = status;
	}

	public int getPortForDir() {
		return this.portForDir;
	}

	public void setPortForDir(int portForDir) {
		this.portForDir = portForDir;
	}

	public Container.ContainerType getType() {
		return this.type;
	}

	public void setType(Container.ContainerType type) {
		this.type = type;
	}

	public int getMem() {
		return this.mem;
	}

	public void setMem(int mem) {
		this.mem = mem;
	}

	public String getCpuset() {
		return this.cpuset;
	}

	public void setCpuset(String cpuset) {
		this.cpuset = cpuset;
	}

	public int getCpushare() {
		return this.mem;
	}

	public void setCpushare(int cpushare) {
		this.cpushare = cpushare;
	}

	public void describeContainer() {
		System.out.println(this.name + " " + this.id + " " + this.ip);
	}

	public boolean prepareContainer() {
		switch(this.type.ordinal() + 1) {
			case 1:
				System.err.println("I don't know how to start the clone " + this.name);
				printInfoAboutContainerName();
			case 2:
			default:
				break;
			case 3:
				System.out.println("Preparing the Amazon clone " + this.name);
				System.err.println("Not yet implemented for the amazon clones");
				break;
			case 4:
				System.out.println("Preparing the LXC " + this.name);
				switch(this.status) {
					case 0:
						if(this.startContainer()) {
							System.out.println("Started the LXC " + this.name);
							this.status = 3;
							return true;
						}

						System.err.println("Could not start the LXC" + this.name);
						this.status = 0;
						return false;
					case 1:
						if(this.resumeContainer()) {
							System.out.println("Resumed the LXC " + this.name);
							this.status = 3;
							return true;
						}

						System.err.println("Could not resume the LXC " + this.name);
						this.status = 0;
						return false;
					case 2:
						System.out.println("The LXC " + this.name + " was already started");
						this.status = 3;
						return true;
					case 3:
					case 4:
					default:
						return false;
					case 5:
						if(this.createContainer() && this.startContainer()) {
							System.out.println("Created the LXC " + this.name);
							this.status = 3;
							return true;
						}

						System.err.println("Could not create the LXC" + this.name);
						this.status = -1;
						return false;
				}
		}

		return false;
	}

	private int getTheStateOfLXC() {
		switch(this.type.ordinal() + 1) {
			case 1:
				System.err.println("I cant't get the physical state of the clone " + this.name);
				printInfoAboutContainerName();
			case 2:
			default:
				break;
			case 3:
				System.err.println("Not yet implemented for the amazon clones");
				break;
			case 4:
				String out = this.executeCommand("lxc-info -n " + this.name);
				if(out.contains("STOPPED")) {
					return 0;
				}

				if(out.contains("RUNNING")) {
					return 2;
				}

				if(out.contains("FROZEN")) {
					return 1;
				}
		}

		return -1;
	}

	public boolean startContainer() {
		String out = this.executeCommand("lxc-start -n " + this.name + " -s lxc.network.ipv4=" + this.ip + "/24");
		return out.isEmpty();
	}

	public boolean createContainer() {
		String out = this.executeCommand("/root/newlxc.py -n " + this.name + " --ip=" + this.ip);
		return out.isEmpty();
	}

	public boolean resumeContainer() {
		this.executeCommand("lxc-unfreeze -n " + this.name);
		switch(this.getTheStateOfLXC()) {
			case 0:
				return false;
			case 1:
				return false;
			case 2:
				return true;
			default:
				return false;
		}
	}

	public boolean pauseContainer() {
		this.executeCommand("lxc-freeze -n " + this.name);
		switch(this.getTheStateOfLXC()) {
			case 0:
				return false;
			case 1:
				return true;
			case 2:
				return false;
			default:
				return false;
		}
	}

	public boolean stopContainer() {
		this.executeCommand("lxc-stop -n " + this.name + " -k");
		switch(this.getTheStateOfLXC()) {
			case 0:
				return true;
			case 1:
				return false;
			case 2:
				return false;
			default:
				return false;
		}
	}

	private String executeCommand(String command) {
		try {
			Process p = Runtime.getRuntime().exec(command);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			StringBuilder sb = new StringBuilder();
			String s = "";

			while((s = stdInput.readLine()) != null) {
				sb.append(s);
			}

			while((s = stdError.readLine()) != null) {
				System.out.println("Std ERROR : " + s);
			}

			return sb.toString();
		} catch (IOException var7) {
			var7.printStackTrace();
			return null;
		}
	}

	public static void printInfoAboutContainerName() {
		System.out.println("The name of the clone should start with vb- for VirtualBox clones, and with amazon- for Amazon clones.");
	}

	public static enum ContainerType {
		UNKNOWN,
		VIRTUALBOX,
		AMAZON,
		LXC;

		private ContainerType() {
		}
	}
}

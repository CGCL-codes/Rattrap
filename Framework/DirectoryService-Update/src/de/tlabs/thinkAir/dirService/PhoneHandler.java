package de.tlabs.thinkAir.dirService;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public class PhoneHandler implements Runnable {
	private Socket clientSocket;
	private InputStream is;
	private OutputStream os;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private Socket conSocket;
	private InputStream conis;
	private OutputStream conos;
	private ObjectOutputStream conoos;
	private ObjectInputStream conois;
	private String phoneID;
	static String appName;
	static String apkFilePath;
	static String objToExecute = null;
	static String methodName;
	static Class<?>[] pTypes;
	static Object[] pValues;
	private Container worker_container = null;
	private final int BUFFER = 8192;
	private DBHelper dbh;
	private static String logFileName = null;
	private static FileWriter logFileWriter = null;
	private String RequestLog = null;

	public PhoneHandler(Socket clientSocket, InputStream is, OutputStream os, DBHelper dbh) {
		this.clientSocket = clientSocket;
		this.is = is;
		this.os = os;
		this.dbh = dbh;
		logFileName = "/root/dirservice/ExecRecord/execrecord.txt";
		File needlog = new File("/root/dirservice/ExecRecord/needlog");
		if(needlog.exists()) {
			try {
				File logFile = new File(logFileName);
				logFile.createNewFile();
				logFileWriter = new FileWriter(logFile, true);
			} catch (IOException var7) {
				var7.printStackTrace();
			}
		}

	}

	public void run() {
		System.out.println("Waiting for commands from the phone...");
		int command = 0;

		try {
			try {
				this.ois = new ObjectInputStream(this.is);
				this.oos = new ObjectOutputStream(this.os);

				while(true) {
					while(command != -1) {
						command = this.is.read();
						System.out.println("Command: " + command);
						long startTime;
						long dura;
						boolean connected;
						HashMap result;
						boolean preres;
						switch(command) {
							case 11:
								System.out.println("Reply to PING");
								this.os.write(12);
								break;
							case 21:
								appName = (String)this.ois.readObject();
								apkFilePath = "/root/system/off-app/" + appName + ".apk";
								if(this.apkPresent(apkFilePath)) {
									System.out.println("APK present " + appName);
									this.os.write(22);
									break;
								}

								startTime = System.nanoTime();
								System.out.println("request APK " + appName);
								this.os.write(23);

								for(int i = 0; i < 5; ++i) {
									System.out.println("Receving APK file for no. " + i + " time");
									this.receiveApk(this.ois, apkFilePath);
								}

								System.out.println("received APK");
								dura = System.nanoTime() - startTime;
								System.out.println("Transfering apk cost " + dura / 1000000L + " ms.");
								break;
							case 36:
								this.phoneID = (String)this.ois.readObject();
								System.out.println(this.phoneID);
								break;
							case 38:
								System.out.println("Execute request");
								connected = false;
								startTime = System.nanoTime();
								if(this.worker_container != null && this.worker_container.getStatus() == 2) {
									do {
										connected = this.waitForContainerToAuthenticate(this.worker_container);
									} while(!connected);
								} else {
									do {
										this.worker_container = this.findAvailableContainer();
										preres = this.worker_container.prepareContainer();
										if(preres) {
											connected = this.waitForContainerToAuthenticate(this.worker_container);
										}
									} while(!connected);
								}

								dura = System.nanoTime() - startTime;
								this.RequestLog = this.RequestLog + dura / 1000000L + " ";
								startTime = System.nanoTime();
								result = (HashMap)this.receiveAndRepost(this.ois, this.worker_container);
								this.releaseContainer(this.worker_container);

								try {
									System.out.println("Sending result back");
									System.out.println("Send retType is " + (String)result.get("retType"));
									this.oos.writeObject(result.get("retType"));
									System.out.println("Send retVal is " + (String)result.get("retVal"));
									this.oos.writeObject(result.get("retVal"));
									this.oos.flush();
									System.out.println("Result successfully sent");
								} catch (IOException var26) {
									System.out.println("Connection failed when sending result back");
									var26.printStackTrace();
									return;
								}

								dura = System.nanoTime() - startTime;
								this.RequestLog = this.RequestLog + dura / 1000000L;
								this.traceLog(this.RequestLog);
								this.RequestLog = "";
								break;
							case 43:
								System.out.println("Execute request with file");
								connected = false;
								startTime = System.nanoTime();
								if(this.worker_container != null && this.worker_container.getStatus() == 2) {
									do {
										connected = this.waitForContainerToAuthenticate(this.worker_container);
									} while(!connected);
								} else {
									do {
										this.worker_container = this.findAvailableContainer();
										preres = this.worker_container.prepareContainer();
										if(preres) {
											connected = this.waitForContainerToAuthenticate(this.worker_container);
										}
									} while(!connected);
								}

								dura = System.nanoTime() - startTime;
								this.RequestLog = this.RequestLog + dura / 1000000L + " ";
								startTime = System.nanoTime();
								System.out.println("The offloading need to send file first");
								String filePath = (String)this.ois.readObject();
								String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
								filePath = "/root/system/off-app/off-file/" + fileName;
								System.out.println("request File " + filePath);
								this.os.write(41);
								this.receiveApk(this.ois, filePath);
								dura = System.nanoTime() - startTime;
								this.RequestLog = this.RequestLog + dura / 1000000L + " ";
								startTime = System.nanoTime();
								result = (HashMap)this.receiveAndRepost(this.ois, this.worker_container);
								this.releaseContainer(this.worker_container);

								try {
									System.out.println("Send result back");
									this.oos.writeObject(result.get("retType"));
									this.oos.writeObject(result.get("retVal"));
									this.oos.flush();
									System.out.println("Result successfully sent");
								} catch (IOException var25) {
									System.out.println("Connection failed when sending result back");
									var25.printStackTrace();
									return;
								}

								dura = System.nanoTime() - startTime;
								this.RequestLog = this.RequestLog + dura / 1000000L;
								this.traceLog(this.RequestLog);
								this.RequestLog = "";
						}
					}

					return;
				}
			} catch (IOException var27) {
				var27.printStackTrace();
			} catch (ClassNotFoundException var28) {
				var28.printStackTrace();
			}

		} finally {
			try {
				this.oos.close();
				this.ois.close();
				this.clientSocket.close();
			} catch (IOException var24) {
				var24.printStackTrace();
			}

		}
	}

	private boolean apkPresent(String filename) {
		File apkFile = new File(filename);
		return apkFile.exists();
	}

	private File receiveApk(ObjectInputStream objIn, String apkFilePath) throws IOException {
		int apkLen = objIn.readInt();
		System.out.println("Read apk len - " + apkLen);
		byte[] tempArray = new byte[apkLen];
		System.out.println("Read apk");
		objIn.readFully(tempArray);
		File dexFile = new File(apkFilePath);
		FileOutputStream fout = new FileOutputStream(dexFile);
		BufferedOutputStream bout = new BufferedOutputStream(fout, 8192);
		bout.write(tempArray);
		bout.close();
		return dexFile;
	}

	private Object receiveAndRepost(ObjectInputStream objIn, Container container) {
		System.out.println("Read Object");

		try {
			String className = (String)objIn.readObject();
			objToExecute = (String)objIn.readObject();
			System.out.println("Read Method");
			methodName = (String)objIn.readObject();
			Object tempTypes = objIn.readObject();
			String[] pType = (String[])tempTypes;
			String pValuestr = (String)objIn.readObject();
			System.out.println("Repost Method " + methodName);
			this.conos.write(38);
			this.conoos.writeObject(appName);
			//int needApk = this.conis.read();
			this.conoos.writeObject(className);
			this.conoos.writeObject(objToExecute);
			this.conoos.writeObject(methodName);
			this.conoos.writeObject(pType);
			this.conoos.writeObject(pValuestr);
			this.conoos.flush();
			System.out.println("Reading result from container");
			HashMap<String, String> result = new HashMap();
			String retType = (String)this.conois.readObject();
			result.put("retType", retType);
			String response = (String)this.conois.readObject();
			result.put("retVal", response);
			return result;
		} catch (Exception var11) {
			var11.printStackTrace();
			return null;
		}
	}

	private boolean waitForContainerToAuthenticate(Container container) {
		Long stime = Long.valueOf(System.nanoTime());

		while(true) {
			try {
				this.conSocket = new Socket();
				System.out.println("Connecting worker container ...");
				this.conSocket.connect(new InetSocketAddress(container.getIp(), container.getPortForDir()), 0);
				Long dura = Long.valueOf(System.nanoTime() - stime.longValue());
				System.out.println("Connected worker container in " + dura.longValue() / 1000000L + "ms");
				this.worker_container.setStatus(4);
				this.conis = this.conSocket.getInputStream();
				this.conos = this.conSocket.getOutputStream();
				this.conois = new ObjectInputStream(this.conis);
				this.conoos = new ObjectOutputStream(this.conos);
				return true;
			} catch (ConnectException var7) {
				try {
					Thread.sleep(100L);
				} catch (InterruptedException var6) {
					var6.printStackTrace();
				}
			} catch (IOException var8) {
				var8.printStackTrace();

				try {
					this.conSocket.close();
				} catch (IOException var5) {
					var5.printStackTrace();
				}

				return false;
			}
		}
	}

	private boolean waitForCloneToAuthenticate() {
		this.conSocket = new Socket();

		try {
			System.out.println("Connecting worker container ...");
			this.conSocket.connect(new InetSocketAddress("192.168.155.3", 4322), 0);
			System.out.println("Connected to worker container");
			this.conis = this.conSocket.getInputStream();
			this.conos = this.conSocket.getOutputStream();
			this.conoos = new ObjectOutputStream(this.conos);
			this.conoos.flush();
			this.conois = new ObjectInputStream(this.conis);
			return true;
		} catch (IOException var2) {
			var2.printStackTrace();
			return false;
		}
	}

	private Container findAvailableContainer() {
		try {
			this.dbh.dbUpdate("lock tables lxc write");
			String sql = "select * from lxc where status in (2,1,0) order by status desc limit 1 ";
			ResultSet rs = this.dbh.dbSelect(sql);
			if(rs.next()) {
				String name = rs.getString("name");
				String ip = rs.getString("ip");
				int status = rs.getInt("status");
				int mem = rs.getInt("mem");
				String cpuset = rs.getString("cpuset");
				int cpushare = rs.getInt("cpushare");
				this.dbh.dbUpdate("update lxc set status = 3 where name = '" + name + "'");
				this.dbh.dbUpdate("unlock tables");
				if(ip == null) {
					ip = this.getAvailableIp();
					this.dbh.dbUpdate("update lxc set ip = '" + ip + "' where name = '" + name + "'");
				}

				Container con = new Container(name, ip, status, mem, cpuset, cpushare);
				return con;
			} else {
				this.dbh.dbUpdate("unlock tables");
				Container con = this.startNewContainer();
				return con;
			}
		} catch (SQLException var10) {
			var10.printStackTrace();
			return null;
		}
	}

	private Container startNewContainer() {
		try {
			String name = UUID.randomUUID().toString();
			String ip = this.getAvailableIp();
			if(ip == null) {
				return null;
			}

			Container con = new Container(name, ip, 5);
			String sql = "insert into lxc (name, ip, status) values ('" + name + "','" + ip + "', " + 5 + ")";
			boolean rs = this.dbh.dbUpdate(sql);
			if(rs) {
				return con;
			}
		} catch (Exception var6) {
			var6.printStackTrace();
		}

		return null;
	}

	private String getAvailableIp() {
		String ip = null;

		try {
			this.dbh.dbUpdate("lock tables ip write");
			ResultSet rs = this.dbh.dbSelect("select ip from ip where inuse=0 limit 1");
			if(rs.next()) {
				ip = rs.getString("ip");
			}

			if(ip != null) {
				this.dbh.dbUpdate("update ip set inuse=1 where ip='" + ip + "'");
			}

			this.dbh.dbUpdate("unlock tables");
			return ip;
		} catch (SQLException var3) {
			var3.printStackTrace();
			return null;
		}
	}

	private void releaseContainer(Container con) {
		try {
			this.conoos.close();
			this.conois.close();
			this.conos.close();
			this.conis.close();
		} catch (IOException var3) {
			var3.printStackTrace();
		}

		con.setStatus(2);
	}

	private void traceLog(String log) {
		if(logFileWriter != null) {
			try {
				logFileWriter.append(log + "\n");
				logFileWriter.flush();
			} catch (IOException var3) {
				var3.printStackTrace();
			}
		}

	}

	private void sendApk(String apkName, ObjectOutputStream objOut) throws IOException {
		File apkFile = new File(apkName);
		FileInputStream fin = new FileInputStream(apkFile);
		BufferedInputStream bis = new BufferedInputStream(fin);
		byte[] tempArray = new byte[(int)apkFile.length()];
		bis.read(tempArray, 0, tempArray.length);
		System.out.println("Sending apk length - " + tempArray.length);
		objOut.writeInt(tempArray.length);
		System.out.println("Sending apk");
		objOut.write(tempArray);
		objOut.flush();
		bis.close();
	}
}

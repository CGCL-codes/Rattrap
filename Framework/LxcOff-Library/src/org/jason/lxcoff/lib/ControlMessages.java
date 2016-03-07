package org.jason.lxcoff.lib;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

/**
 * Control Messages for client-server communication Message IDs up to 255 - one
 * byte only, as they are sent over sockets using write()/read() - only one byte
 * read/written.
 */
public class ControlMessages {

	private static final String TAG								= "ControlMessages";
	public static final int 	MAX_NUM_CLIENTS 				= 32;
	
	public static final int	STATIC_LOCAL 						= 1;
	public static final int	STATIC_REMOTE 						= 3;
	public static final int USER_CARES_ONLY_TIME				= 4;
	public static final int USER_CARES_ONLY_ENERGY 				= 5;
	public static final int USER_CARES_TIME_ENERGY 				= 6;

	public static final int PING								= 11;
	public static final int PONG								= 12;
	public static final int EXECUTE 							= 13;
	
	// Communication Phone <-> Clone
	public static final int APK_REGISTER 						= 21;
	public static final int APK_PRESENT 						= 22;
	public static final int APK_REQUEST 						= 23;
	public static final int APK_SEND 							= 24;
	public static final int CLONE_ID_SEND						= 25;

//	public static final String MNT_SDCARD 						= Environment.getExternalStorageDirectory().getAbsolutePath() + "/";// "/mnt/sdcard/";
	public static final String MNT_SDCARD 						= "/mnt/sdcard/";
	
	public static final String THINKAIR_FOLDER 					= MNT_SDCARD + "thinkAir/";
	public static final String CLONE_CONFIG_FILE 				= THINKAIR_FOLDER + "config-clone.dat";
	public static final String PHONE_CONFIG_FILE 				= THINKAIR_FOLDER + "config-phone.dat";
	
	public static final String FILE_NOT_OFFLOADED				= THINKAIR_FOLDER + "notOffloaded";
	public static final String CLONE_ID_FILE					= THINKAIR_FOLDER + "cloneId";
	public static final String FACE_PICTURE_FOLDER				= THINKAIR_FOLDER + "faceDetection/";
	public static final String FACE_PICTURE_FOLDER_CLONE		= "/system/etc/faceDetection/"; // Memory space problem for amazon clones
	public static final String FACE_PICTURE_TEST				= THINKAIR_FOLDER + "faceDetection/test.jpg";

	public static final String VIRUS_DB_PATH					= THINKAIR_FOLDER + "virusDB/";
	public static final String VIRUS_FOLDER_TO_SCAN				= THINKAIR_FOLDER + "virusFolderToScan/";
	public static final String VIRUS_FOLDER_ZIP					= VIRUS_FOLDER_TO_SCAN + ".tar.gz";

	// The constants of the configuration files
	public static final String 	DIRSERVICE_IP					= "[DIRSERVICE IP]";
	public static final String 	DIRSERVICE_PORT					= "[DIRSERVICE PORT]";
	public static final String 	CLONE_TYPES						= "[CLONE TYPES]"; // Type has to be one of: Local, Amazon, or Hybrid
	public static final String 	NO_CLONES_VB_TO_START			= "[NUMBER OF VB CLONES TO START ON STARTUP]";
	public static final String 	NO_CLONES_AMAZON_TO_START		= "[NUMBER OF AMAZON CLONES TO START ON STARTUP]";
	public static final String 	VB_CLONES						= "[VIRTUALBOX CLONES]";
	public static final String 	AMAZON_CLONES					= "[AMAZON CLONES]";
	public static final String 	PORT_FOR_PHONES					= "[PORT FOR PHONES]";
	public static final String 	PORT_FOR_CLONES					= "[PORT FOR CLONES]";
	public static final String 	CLONE_NAME						= "[CLONE NAME]";
	public static final String 	CLONE_ID						= "[CLONE ID]";
	
//	public static final String 	DIRSERVICE_RESOURCES			= "E:/dirService/configs/";
	public static final String 	DIRSERVICE_RESOURCES			= "/root/dirservice/";
	public static final String 	DIRSERVICE_CONFIG_FILE			= DIRSERVICE_RESOURCES + "config-dirservice.dat";
	public static final String  VM_DIRSERVICE_CONFIG_FILE		= "/root/cloneroot/dirservice/config-dirservice.dat";
	public static final String 	DIRSERVICE_CLONE_KEYS_FOLDER	= DIRSERVICE_RESOURCES + "clone-keys/";
	public static final String 	DIRSERVICE_CLONE_PUBLIC_KEY		= DIRSERVICE_CLONE_KEYS_FOLDER + "publickey-";
//	public static final String  DIRSERVICE_APK_DIR				= "E:/dirService/apk/";
	public static final String  DIRSERVICE_APK_DIR				= "/root/system/off-app/";
	public static final String  CONTAINER_APK_DIR				= "/system/off-app/";
	public static final String 	DEX_OUT_PATH					= "/data/data/org.jason.lxcoff.server/app_dex/";
	
	// Communication Phone/Clone <-> DirectoryService
	public static final int 	PHONE_CONNECTION 				= 30;
	public static final int 	CLONE_CONNECTION 				= 31;
	public static final int		NEED_CLONE_HELPERS 				= 32;
	public static final int 	CLONE_AUTHENTICATION 			= 33;
	public static final int 	GET_ASSOCIATED_CLONE_INFO		= 34;
	public static final int 	GET_NEW_CLONE_INFO				= 35;
	public static final int 	PHONE_AUTHENTICATION			= 36;
	public static final int 	GET_PORT_FOR_PHONES				= 37;
	public static final int		PHONE_COMPUTATION_REQUEST		= 38;
	public static final int		CONTAINER_CONNECTION			= 39;
	public static final int		SEND_FILE_FIRST					= 40;
	public static final int		SEND_FILE_REQUEST			    = 41;
	public static final int		FILE_PRESENT			    	= 42;
	public static final int		PHONE_COMPUTATION_REQUEST_WITH_FILE		= 43;

	
	public enum SETUP_TYPE {
		LOCAL, AMAZON, HYBRID
	}
	
	
	/**
	 * An empty file will be created automatically on the phone by ThinkAir-Client.
	 * The presence or absence of this file can let the method know 
	 * if it is running on the phone or on the clone.
	 * @return <b>True</b> if it is running on the clone<br>
	 * <b>False</b> if it is running on the phone.
	 */
	public static boolean checkIfOffloaded() {
		try {
			File tempFile = new File(ControlMessages.FILE_NOT_OFFLOADED);
			if ( tempFile.exists() )	return false;
			else						return true;
		} catch (Exception e) {
			return true;
		}
	}

	public static void executeShellCommand(String TAG, String cmd, boolean asRoot) {
		Process p = null;
		try {
			if (asRoot) p = Runtime.getRuntime().exec("su " + cmd);
			else        p = Runtime.getRuntime().exec(cmd);

			DataOutputStream outs = new DataOutputStream(p.getOutputStream());

			//          outs.writeBytes(cmd + "\n");
			outs.writeBytes("exit\n");
			outs.close();

			p.waitFor();
			Log.i(TAG, "Executed cmd: " + cmd);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			//            destroyProcess(p);
		}
	}

	private static final String SCRIPT_FILE = "temp_sokol.sh";

	public static int runScript(Context ctx, String script, StringBuilder res, boolean asroot) {
		final File file = new File(ctx.getDir("bin",0), SCRIPT_FILE);
		final ScriptRunner runner = new ScriptRunner(file, script, res, asroot);
		runner.start();
		try {
			runner.join();
		} catch (InterruptedException ex) {}
		return runner.exitcode;
	}

	/**
	 * Write the ID of this clone on the file "/mnt/sdcard/cloneId".<br>
	 * The IDs are assigned by the main clone during the PING.
	 * @param cloneId the ID of this clone
	 */
	public static void writeCloneId(int cloneId) {
		try {
			File cloneIdFile = new File(ControlMessages.CLONE_ID_FILE);
			FileWriter cloneIdWriter = new FileWriter(cloneIdFile);
			cloneIdWriter.write(String.valueOf(cloneId));
			cloneIdWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Read the file "/mnt/sdcard/cloneId" for the ID of this clone.
	 * @return -1 if this is the phone or the main clone (the file may even not exist in these cases)<br>
	 * CLONE_ID otherwise
	 */
	public static int readCloneId() {
		Scanner cloneIdReader = null;
		int cloneId = -1;

		try {
			File cloneIdFile = new File(ControlMessages.CLONE_ID_FILE);
			cloneIdReader = new Scanner(cloneIdFile);
			cloneId = cloneIdReader.nextInt();
		} catch (Exception e) {
			// Stay quiet, we know it.
		} finally {
			try {
				cloneIdReader.close();
			} catch (Exception e) {
				Log.e(TAG, "CloneId file is not here, this means that this is the main clone (or the phone)");
			}
		}

		return cloneId;
	}
}



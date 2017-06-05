package org.jason.lxcoff.lib;

import android.content.Context;
import android.util.Log;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class ControlMessages {
	private static final String TAG = "ControlMessages";
	public static final int MAX_NUM_CLIENTS = 32;
	public static final int STATIC_LOCAL = 1;
	public static final int STATIC_REMOTE = 3;
	public static final int USER_CARES_ONLY_TIME = 4;
	public static final int USER_CARES_ONLY_ENERGY = 5;
	public static final int USER_CARES_TIME_ENERGY = 6;
	public static final int PING = 11;
	public static final int PONG = 12;
	public static final int EXECUTE = 13;
	public static final int APK_REGISTER = 21;
	public static final int APK_PRESENT = 22;
	public static final int APK_REQUEST = 23;
	public static final int APK_SEND = 24;
	public static final int CLONE_ID_SEND = 25;
	public static final String MNT_SDCARD = "/mnt/sdcard/";
	public static final String THINKAIR_FOLDER = "/mnt/sdcard/thinkAir/";
	public static final String CLONE_CONFIG_FILE = "/mnt/sdcard/thinkAir/config-clone.dat";
	public static final String PHONE_CONFIG_FILE = "/mnt/sdcard/thinkAir/config-phone.dat";
	public static final String FILE_NOT_OFFLOADED = "/mnt/sdcard/thinkAir/notOffloaded";
	public static final String CLONE_ID_FILE = "/mnt/sdcard/thinkAir/cloneId";
	public static final String FACE_PICTURE_FOLDER = "/mnt/sdcard/thinkAir/faceDetection/";
	public static final String FACE_PICTURE_FOLDER_CLONE = "/system/etc/faceDetection/";
	public static final String FACE_PICTURE_TEST = "/mnt/sdcard/thinkAir/faceDetection/test.jpg";
	public static final String VIRUS_DB_PATH = "/mnt/sdcard/thinkAir/virusDB/";
	public static final String VIRUS_FOLDER_TO_SCAN = "/mnt/sdcard/thinkAir/virusFolderToScan/";
	public static final String VIRUS_FOLDER_ZIP = "/mnt/sdcard/thinkAir/virusFolderToScan/.tar.gz";
	public static final String DIRSERVICE_IP = "[DIRSERVICE IP]";
	public static final String DIRSERVICE_PORT = "[DIRSERVICE PORT]";
	public static final String CLONE_TYPES = "[CLONE TYPES]";
	public static final String NO_CLONES_VB_TO_START = "[NUMBER OF VB CLONES TO START ON STARTUP]";
	public static final String NO_CLONES_AMAZON_TO_START = "[NUMBER OF AMAZON CLONES TO START ON STARTUP]";
	public static final String VB_CLONES = "[VIRTUALBOX CLONES]";
	public static final String AMAZON_CLONES = "[AMAZON CLONES]";
	public static final String PORT_FOR_PHONES = "[PORT FOR PHONES]";
	public static final String PORT_FOR_CLONES = "[PORT FOR CLONES]";
	public static final String CLONE_NAME = "[CLONE NAME]";
	public static final String CLONE_ID = "[CLONE ID]";
	public static final String DIRSERVICE_RESOURCES = "/root/dirservice/";
	public static final String DIRSERVICE_CONFIG_FILE = "/root/dirservice/config-dirservice.dat";
	public static final String VM_DIRSERVICE_CONFIG_FILE = "/root/cloneroot/dirservice/config-dirservice.dat";
	public static final String DIRSERVICE_CLONE_KEYS_FOLDER = "/root/dirservice/clone-keys/";
	public static final String DIRSERVICE_CLONE_PUBLIC_KEY = "/root/dirservice/clone-keys/publickey-";
	public static final String DIRSERVICE_APK_DIR = "/root/system/off-app/";
	public static final String CONTAINER_APK_DIR = "/system/off-app/";
	public static final String DEX_OUT_PATH = "/data/data/org.jason.lxcoff.server/app_dex/";
	public static final int PHONE_CONNECTION = 30;
	public static final int CLONE_CONNECTION = 31;
	public static final int NEED_CLONE_HELPERS = 32;
	public static final int CLONE_AUTHENTICATION = 33;
	public static final int GET_ASSOCIATED_CLONE_INFO = 34;
	public static final int GET_NEW_CLONE_INFO = 35;
	public static final int PHONE_AUTHENTICATION = 36;
	public static final int GET_PORT_FOR_PHONES = 37;
	public static final int PHONE_COMPUTATION_REQUEST = 38;
	public static final int CONTAINER_CONNECTION = 39;
	public static final int SEND_FILE_FIRST = 40;
	public static final int SEND_FILE_REQUEST = 41;
	public static final int FILE_PRESENT = 42;
	public static final int PHONE_COMPUTATION_REQUEST_WITH_FILE = 43;
	private static final String SCRIPT_FILE = "temp_sokol.sh";

	public ControlMessages() {
	}

	public static boolean checkIfOffloaded() {
		try {
			File tempFile = new File("/mnt/sdcard/thinkAir/notOffloaded");
			return !tempFile.exists();
		} catch (Exception var1) {
			return true;
		}
	}

	public static void executeShellCommand(String TAG, String cmd, boolean asRoot) {
		Process p = null;

		try {
			if(asRoot) {
				p = Runtime.getRuntime().exec("su " + cmd);
			} else {
				p = Runtime.getRuntime().exec(cmd);
			}

			DataOutputStream outs = new DataOutputStream(p.getOutputStream());
			outs.writeBytes("exit\n");
			outs.close();
			p.waitFor();
			Log.i(TAG, "Executed cmd: " + cmd);
		} catch (IOException var5) {
			var5.printStackTrace();
		} catch (InterruptedException var6) {
			var6.printStackTrace();
		}

	}

	public static int runScript(Context ctx, String script, StringBuilder res, boolean asroot) {
		File file = new File(ctx.getDir("bin", 0), "temp_sokol.sh");
		ScriptRunner runner = new ScriptRunner(file, script, res, asroot);
		runner.start();

		try {
			runner.join();
		} catch (InterruptedException var7) {
			;
		}

		return runner.exitcode;
	}

	public static void writeCloneId(int cloneId) {
		try {
			File cloneIdFile = new File("/mnt/sdcard/thinkAir/cloneId");
			FileWriter cloneIdWriter = new FileWriter(cloneIdFile);
			cloneIdWriter.write(String.valueOf(cloneId));
			cloneIdWriter.close();
		} catch (IOException var3) {
			var3.printStackTrace();
		} catch (Exception var4) {
			var4.printStackTrace();
		}

	}

	public static int readCloneId() {
		Scanner cloneIdReader = null;
		int cloneId = -1;

		try {
			File cloneIdFile = new File("/mnt/sdcard/thinkAir/cloneId");
			cloneIdReader = new Scanner(cloneIdFile);
			cloneId = cloneIdReader.nextInt();
		} catch (Exception var11) {
			;
		} finally {
			try {
				cloneIdReader.close();
			} catch (Exception var10) {
				Log.e("ControlMessages", "CloneId file is not here, this means that this is the main clone (or the phone)");
			}

		}

		return cloneId;
	}

	public static enum SETUP_TYPE {
		LOCAL,
		AMAZON,
		HYBRID;

		private SETUP_TYPE() {
		}
	}
}

package org.jason.lxcoff.lib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import android.util.Log;

public final class ScriptRunner extends Thread {
	private final File file;
	private final String script;
	private final StringBuilder res;
	private final boolean asroot;
	public int exitcode = -1;
	private Process exec;
	private static final String TAG = "ScriptRunner";

	/**
	 * Creates a new script runner.
	 * @param file temporary script file
	 * @param script script to run
	 * @param res response output
	 * @param asroot if true, executes the script as root
	 */
	public ScriptRunner(File file, String script, StringBuilder res, boolean asroot) {
		this.file = file;
		this.script = script;
		this.res = res;
		this.asroot = asroot;
	}
	@Override
	public void run() {
		try {
			Log.d(TAG, "Running script: " + script);
			
			file.createNewFile();
			final String abspath = file.getAbsolutePath();
			// make sure we have execution permission on the script file
			Runtime.getRuntime().exec("chmod 777 " + abspath).waitFor();
			// Write the script to be executed
			final OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file));
			if (new File("/system/bin/sh").exists()) {
				out.write("#!/system/bin/sh\n");
			}
			out.write(script);
			if (!script.endsWith("\n")) out.write("\n");
			out.write("exit\n");
			out.flush();
			out.close();
			if (this.asroot) {
				// Create the "su" request to run the script
				exec = Runtime.getRuntime().exec("su -c "+abspath);
			} else {
				// Create the "sh" request to run the script
				exec = Runtime.getRuntime().exec("sh "+abspath);
			}
			final InputStream stdout = exec.getInputStream();
			final InputStream stderr = exec.getErrorStream();
			final byte buf[] = new byte[8192];
			int read = 0;
			while (true) {
				final Process localexec = exec;
				if (localexec == null) break;
				try {
					// get the process exit code - will raise IllegalThreadStateException if still running
					this.exitcode = localexec.exitValue();
				} catch (IllegalThreadStateException ex) {
					// The process is still running
				}
				// Read stdout
				if (stdout.available() > 0) {
					read = stdout.read(buf);
					if (res != null) res.append(new String(buf, 0, read));
				}
				// Read stderr
				if (stderr.available() > 0) {
					read = stderr.read(buf);
					if (res != null) res.append(new String(buf, 0, read));
				}
				if (this.exitcode != -1) {
					// finished
					break;
				}
				// Sleep for the next round
				Thread.sleep(50);
			}
		} catch (InterruptedException ex) {
			Log.i(TAG, "InterruptedException ");
			ex.printStackTrace();
			if (res != null) res.append("\nOperation timed-out");
		} catch (Exception ex) {
			Log.i(TAG, "Exception");
			ex.printStackTrace();
			if (res != null) res.append("\n" + ex);
		} finally {
			destroy();
		}
	}
	/**
	 * Destroy this script runner
	 */
	public synchronized void destroy() {
		if (exec != null) exec.destroy();
		exec = null;
	}
}

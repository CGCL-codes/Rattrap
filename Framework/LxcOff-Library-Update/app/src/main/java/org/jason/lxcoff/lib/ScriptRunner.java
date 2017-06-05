package org.jason.lxcoff.lib;

import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;

public final class ScriptRunner extends Thread {
	private final File file;
	private final String script;
	private final StringBuilder res;
	private final boolean asroot;
	public int exitcode = -1;
	private Process exec;
	private static final String TAG = "ScriptRunner";

	public ScriptRunner(File file, String script, StringBuilder res, boolean asroot) {
		this.file = file;
		this.script = script;
		this.res = res;
		this.asroot = asroot;
	}

	public void run() {
		try {
			Log.d("ScriptRunner", "Running script: " + this.script);
			this.file.createNewFile();
			String abspath = this.file.getAbsolutePath();
			Runtime.getRuntime().exec("chmod 777 " + abspath).waitFor();
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(this.file));
			if((new File("/system/bin/sh")).exists()) {
				out.write("#!/system/bin/sh\n");
			}

			out.write(this.script);
			if(!this.script.endsWith("\n")) {
				out.write("\n");
			}

			out.write("exit\n");
			out.flush();
			out.close();
			if(this.asroot) {
				this.exec = Runtime.getRuntime().exec("su -c " + abspath);
			} else {
				this.exec = Runtime.getRuntime().exec("sh " + abspath);
			}

			InputStream stdout = this.exec.getInputStream();
			InputStream stderr = this.exec.getErrorStream();
			byte[] buf = new byte[8192];
			boolean var6 = false;

			while(true) {
				Process localexec = this.exec;
				if(localexec == null) {
					break;
				}

				try {
					this.exitcode = localexec.exitValue();
				} catch (IllegalThreadStateException var14) {
					;
				}

				int read;
				if(stdout.available() > 0) {
					read = stdout.read(buf);
					if(this.res != null) {
						this.res.append(new String(buf, 0, read));
					}
				}

				if(stderr.available() > 0) {
					read = stderr.read(buf);
					if(this.res != null) {
						this.res.append(new String(buf, 0, read));
					}
				}

				if(this.exitcode != -1) {
					break;
				}

				Thread.sleep(50L);
			}
		} catch (InterruptedException var15) {
			Log.i("ScriptRunner", "InterruptedException ");
			var15.printStackTrace();
			if(this.res != null) {
				this.res.append("\nOperation timed-out");
			}
		} catch (Exception var16) {
			Log.i("ScriptRunner", "Exception");
			var16.printStackTrace();
			if(this.res != null) {
				this.res.append("\n" + var16);
			}
		} finally {
			this.destroy();
		}

	}

	public synchronized void destroy() {
		if(this.exec != null) {
			this.exec.destroy();
		}

		this.exec = null;
	}
}

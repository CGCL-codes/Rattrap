package org.jason.lxcoff.lib;

import java.io.File;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;

public abstract class Remoteable implements Serializable {
	private static final long serialVersionUID = 1L;

	public Remoteable() {
	}

	public abstract void copyState(Remoteable var1);

	public void loadLibraries(LinkedList<File> libFiles) {
		Iterator var3 = libFiles.iterator();

		while(var3.hasNext()) {
			File libFile = (File)var3.next();
			System.load(libFile.getAbsolutePath());
		}

	}
}

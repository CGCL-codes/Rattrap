package org.jason.lxcoff.lib;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import android.util.Log;


public class ZipHandler {

	public static void main(String[] args) {
		try {
//			byte[] tempBuf = zipFolder(args[0], 0);
			
//			extractBytes(tempBuf);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static public void zipFolder(String srcFolder, String destZipFile, int i) throws Exception {
		FileOutputStream fileWriter = new FileOutputStream(destZipFile);
		ZipOutputStream zip = new ZipOutputStream(fileWriter);

		addFolderToZip("", srcFolder, zip, i);
		zip.flush();
		zip.close();
	}

	static public byte[] zipFolder(ByteArrayOutputStream baos, String srcFolder, int i) throws Exception {
//		ByteArrayOutputStream baos 	= new ByteArrayOutputStream();
		ZipOutputStream zip 		= new ZipOutputStream(baos);

		addFolderToZip("", srcFolder, zip, i);
		zip.flush();
		zip.close();

		return baos.toByteArray();
	}

	static private void addFileToZip(String path, String srcFile, ZipOutputStream zip, int i)
			throws Exception {

		File folder = new File(srcFile);
		if (folder.isDirectory()) {
			addFolderToZip(path, srcFile, zip, i);
		} else {
			byte[] buf = new byte[1024];
			int len;
			FileInputStream in = new FileInputStream(srcFile);
			zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
			while ((len = in.read(buf)) > 0) {
				zip.write(buf, 0, len);
			}
			in.close();
		}
	}

	static private void addFolderToZip(String path, String srcFolder, ZipOutputStream zip, int i)
			throws Exception {
		File folder = new File(srcFolder);
		
		int start = i * 1000;
		int end	  = start + 1000;

		System.out.println("Start: " + start + ", End: " + end);
		
		for (String fileName : folder.list()) {
			
			if (Integer.parseInt(fileName) < start || Integer.parseInt(fileName) > end)
				continue;
			
			if (path.equals("")) {
				addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip, i);
			} else {
				addFileToZip(path + "/" + folder.getName(), srcFolder + "/" + fileName, zip, i);
			}
		}
	}

	static public String extractBytes(byte[] zipBytes) {

		Log.i("ZipHandler", "Extracting files in: " + ControlMessages.MNT_SDCARD);
		
		String destParent = null;
		try {
			ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(zipBytes));

			ZipEntry entry = null;
			while ( (entry = zipStream.getNextEntry()) != null ) {
				String currentEntry = ControlMessages.MNT_SDCARD + entry.getName();
				File destFile = new File(currentEntry);
				destParent = destFile.getParent();
				
				new File(destParent).mkdirs();
				Log.i("ZipHandler", "Creating file: " + currentEntry);
				FileOutputStream out = new FileOutputStream(currentEntry);
			    byte[] buf = new byte[4096];
			    int bytesRead = 0;
			    while ((bytesRead = zipStream.read(buf)) != -1) {
			        out.write(buf, 0, bytesRead);
			    }
			    out.close();
			    zipStream.closeEntry();
			}
			
			// TODO: this should work only if there are not subfolders
			return destParent;
			
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	static public void extractFolder(String zipFile) throws ZipException, IOException 
	{
		System.out.println(zipFile);

		ZipFile zip = new ZipFile( new File(zipFile) );
		String newPath = zipFile.substring(0, zipFile.length() - 4);

		new File(newPath).mkdir();
		Enumeration<? extends ZipEntry> zipFileEntries = zip.entries();

		// Process each entry
		while (zipFileEntries.hasMoreElements())
		{
			// grab a zip file entry
			ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
			String currentEntry = entry.getName();
			File destFile = new File(newPath, currentEntry);
			File destinationParent = destFile.getParentFile();

			// create the parent directory structure if needed
			destinationParent.mkdirs();

			if (!entry.isDirectory())
			{
				extractZipEntry(zip, entry, destFile);
			}

			if (currentEntry.endsWith(".zip"))
			{
				// found a zip file, try to open
				extractFolder(destFile.getAbsolutePath());
			}
		}
	}

	static void extractZipEntry(ZipFile zip, ZipEntry entry, File destFile) {

		int BUFFER = 2048;
		BufferedInputStream is;
		try {
			is = new BufferedInputStream(zip.getInputStream(entry));
			int currentByte;
			// establish buffer for writing file
			byte data[] = new byte[BUFFER];

			// write the current file to disk
			FileOutputStream fos = new FileOutputStream(destFile);
			BufferedOutputStream dest = new BufferedOutputStream(fos,
					BUFFER);

			// read and write until last byte is encountered
			while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
				dest.write(data, 0, currentByte);
			}
			dest.flush();
			dest.close();
			is.close();	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}

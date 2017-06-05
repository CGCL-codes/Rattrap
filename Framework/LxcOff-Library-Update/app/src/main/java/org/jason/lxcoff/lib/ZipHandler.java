package org.jason.lxcoff.lib;

import android.util.Log;
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

public class ZipHandler {
	public ZipHandler() {
	}

	public static void main(String[] args) {
	}

	public static void zipFolder(String srcFolder, String destZipFile, int i) throws Exception {
		FileOutputStream fileWriter = new FileOutputStream(destZipFile);
		ZipOutputStream zip = new ZipOutputStream(fileWriter);
		addFolderToZip("", srcFolder, zip, i);
		zip.flush();
		zip.close();
	}

	public static byte[] zipFolder(ByteArrayOutputStream baos, String srcFolder, int i) throws Exception {
		ZipOutputStream zip = new ZipOutputStream(baos);
		addFolderToZip("", srcFolder, zip, i);
		zip.flush();
		zip.close();
		return baos.toByteArray();
	}

	private static void addFileToZip(String path, String srcFile, ZipOutputStream zip, int i) throws Exception {
		File folder = new File(srcFile);
		if(folder.isDirectory()) {
			addFolderToZip(path, srcFile, zip, i);
		} else {
			byte[] buf = new byte[1024];
			FileInputStream in = new FileInputStream(srcFile);
			zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));

			int len;
			while((len = in.read(buf)) > 0) {
				zip.write(buf, 0, len);
			}

			in.close();
		}

	}

	private static void addFolderToZip(String path, String srcFolder, ZipOutputStream zip, int i) throws Exception {
		File folder = new File(srcFolder);
		int start = i * 1000;
		int end = start + 1000;
		System.out.println("Start: " + start + ", End: " + end);
		String[] var10;
		int var9 = (var10 = folder.list()).length;

		for(int var8 = 0; var8 < var9; ++var8) {
			String fileName = var10[var8];
			if(Integer.parseInt(fileName) >= start && Integer.parseInt(fileName) <= end) {
				if(path.equals("")) {
					addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip, i);
				} else {
					addFileToZip(path + "/" + folder.getName(), srcFolder + "/" + fileName, zip, i);
				}
			}
		}

	}

	public static String extractBytes(byte[] zipBytes) {
		Log.i("ZipHandler", "Extracting files in: /mnt/sdcard/");
		String destParent = null;

		try {
			ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(zipBytes));
			ZipEntry entry = null;

			while((entry = zipStream.getNextEntry()) != null) {
				String currentEntry = "/mnt/sdcard/" + entry.getName();
				File destFile = new File(currentEntry);
				destParent = destFile.getParent();
				(new File(destParent)).mkdirs();
				Log.i("ZipHandler", "Creating file: " + currentEntry);
				FileOutputStream out = new FileOutputStream(currentEntry);
				byte[] buf = new byte[4096];
				boolean var8 = false;

				int bytesRead;
				while((bytesRead = zipStream.read(buf)) != -1) {
					out.write(buf, 0, bytesRead);
				}

				out.close();
				zipStream.closeEntry();
			}

			return destParent;
		} catch (ZipException var9) {
			var9.printStackTrace();
		} catch (IOException var10) {
			var10.printStackTrace();
		}

		return null;
	}

	public static void extractFolder(String zipFile) throws ZipException, IOException {
		System.out.println(zipFile);
		ZipFile zip = new ZipFile(new File(zipFile));
		String newPath = zipFile.substring(0, zipFile.length() - 4);
		(new File(newPath)).mkdir();
		Enumeration zipFileEntries = zip.entries();

		while(zipFileEntries.hasMoreElements()) {
			ZipEntry entry = (ZipEntry)zipFileEntries.nextElement();
			String currentEntry = entry.getName();
			File destFile = new File(newPath, currentEntry);
			File destinationParent = destFile.getParentFile();
			destinationParent.mkdirs();
			if(!entry.isDirectory()) {
				extractZipEntry(zip, entry, destFile);
			}

			if(currentEntry.endsWith(".zip")) {
				extractFolder(destFile.getAbsolutePath());
			}
		}

	}

	static void extractZipEntry(ZipFile zip, ZipEntry entry, File destFile) {
		short BUFFER = 2048;

		try {
			BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
			byte[] data = new byte[BUFFER];
			FileOutputStream fos = new FileOutputStream(destFile);
			BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);

			int currentByte;
			while((currentByte = is.read(data, 0, BUFFER)) != -1) {
				dest.write(data, 0, currentByte);
			}

			dest.flush();
			dest.close();
			is.close();
		} catch (IOException var9) {
			var9.printStackTrace();
		}

	}
}

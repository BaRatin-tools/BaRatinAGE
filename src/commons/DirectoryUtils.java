package commons;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Directory Utilities (get all files, zip, etc.)
 * functions getAllFiles, writeZipFile, addToZip and unzip are modified from:
 * http://www.avajava.com/tutorials/lessons/how-do-i-zip-a-directory-and-all-its-contents.html?page=1
 * @author Deron Eriksson for the original version of the 4 functions above, the rest by Ben Renard, Irstea Lyon
 */
public class DirectoryUtils {

	/**
	 * List all files in a directory
	 * @param dir directory
	 * @param fileList list of files
	 * @throws IOException 
	 */	
	public static void getAllFiles(File dir, List<File> fileList) throws IOException {
		File[] files = dir.listFiles();
		for (File file : files) {
			fileList.add(file);
			if (file.isDirectory()) {getAllFiles(file, fileList);}
		}
	}

	/**
	 * Zip a whole folder all files in a directory
	 * @param directoryToZip 
	 * @param fileList list of files
	 * @param zip output zip file
	 * @throws FileNotFoundException,IOException 
	 */	
	public static void writeZipFile(File directoryToZip, List<File> fileList,File zip) throws FileNotFoundException,IOException {
		FileOutputStream fos = new FileOutputStream(zip.getAbsoluteFile());
		ZipOutputStream zos = new ZipOutputStream(fos);

		for (File file : fileList) {
			if (!file.isDirectory()) { // we only zip files, not directories
				addToZip(directoryToZip, file, zos);
			}
		}
		zos.close();
		fos.close();
	}


	/**
	 * Private function used by writeZipFile
	 * @param directoryToZip
	 * @param file
	 * @param zos
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static void addToZip(File directoryToZip, File file, ZipOutputStream zos) throws FileNotFoundException,IOException {
		FileInputStream fis = new FileInputStream(file);
		// we want the zipEntry's path to be a relative path that is relative
		// to the directory being zipped, so chop off the rest of the path
		String zipFilePath = file.getCanonicalPath().substring(directoryToZip.getCanonicalPath().length() + 1,
				file.getCanonicalPath().length());
		ZipEntry zipEntry = new ZipEntry(zipFilePath);
		zos.putNextEntry(zipEntry);
		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zos.write(bytes, 0, length);
		}
		zos.closeEntry();
		fis.close();
	}

	/**
	 * Unzip 
	 * @param zip zip to unzip
	 * @param folder destination folder
	 * @throws IOException
	 */
	public static void Unzip(ZipFile zip, File folder) throws IOException{
		Enumeration<?> enu = zip.entries();
		while (enu.hasMoreElements()) {
			ZipEntry zipEntry = (ZipEntry) enu.nextElement();
			String name = zipEntry.getName();
			// replace backslash by slash to accomodate windows-created zip in linux
			name=name.replace("\\","/");

			folder.mkdirs();
			File file = new File(folder,name);
			File parent = file.getParentFile();
			if (parent != null) {parent.mkdirs();}
			if(!file.exists()) {file.createNewFile();}
			/*
			if (name.endsWith("/")) {
				file.mkdirs();
				continue;
			}
			 */
			InputStream is = zip.getInputStream(zipEntry);
			FileOutputStream fos = new FileOutputStream(file);
			byte[] bytes = new byte[1024];
			int length;
			while ((length = is.read(bytes)) >= 0) {
				fos.write(bytes, 0, length);
			}
			is.close();
			fos.close();
		}
		zip.close();
	}

	/**
	 * Delete content of a directory (!!! recursive !!!)
	 * Will delete all files but keep all folders
	 * @param dir directory whose content is to be deleted
	 */
	public static void deleteDirContent(File dir) {
		File[] files = dir.listFiles();
		if(files!=null) {
			for(File f: files) {
				if(f.isDirectory()) {
					deleteDirContent(f);
				} else {
					f.delete();
				}
			}
		}
	}

	/**
	 * delete a directory and all its content (!!! recursive !!!)
	 * @param dir directory to delete
	 */
	public static void deleteDir(File dir) {
		File[] files = dir.listFiles();
		if(files!=null) {
			for(File f: files) {
				if(f.isDirectory()) {
					deleteDir(f);
				} else {
					f.delete();
				}
			}
		}
		dir.delete();
	}

	/**
	 * Copy a file
	 * Warning: copy of a FILE ONLY, not a directory
	 * Solution for copying file found here: http://www.journaldev.com/861/4-ways-to-copy-file-in-java
	 * @param source source file
	 * @param target target file
	 * @throws IOException 
	 */
	public static void copyFile(File source, File target) throws IOException {
		InputStream is = null;
		OutputStream os = null;
		try{
			is = new FileInputStream(source);
			os = new FileOutputStream(target);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {os.write(buffer, 0, length);}
		} finally {
			is.close();
			os.close();
		}
	}

	/**
	 * Copy all files from a source directory to a destination directory
	 * Warning: copy FILES ONLY, not subfolders
	 * Solution for copying file found here: http://www.journaldev.com/861/4-ways-to-copy-file-in-java
	 * @param source source directory
	 * @param destination destination directory
	 * @throws IOException 
	 */
	public static void copyFilesInDir(File source, File destination) throws IOException {
		File[] files = source.listFiles();
		if(files!=null) {
			for(File f: files) {
				if(!f.isDirectory()) {
					copyFile(f, new File(destination,f.getName()));
				}
			}
		}
	}

}

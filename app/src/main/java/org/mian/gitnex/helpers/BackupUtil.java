package org.mian.gitnex.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author M M Arif
 */
public class BackupUtil {

	public static File getTempDir(Context context) {
		String backupDirectoryPath = String.valueOf(context.getExternalFilesDir(null));
		return new File(backupDirectoryPath);
	}

	public static boolean copyFile(File fromFile, File toFile, boolean bDeleteOriginalFile)
			throws IOException {

		boolean bSuccess = true;
		FileInputStream inputStream = new FileInputStream(fromFile);
		FileOutputStream outputStream = new FileOutputStream(toFile);

		FileChannel fromChannel = null;
		FileChannel toChannel = null;
		try {
			fromChannel = inputStream.getChannel();
			toChannel = outputStream.getChannel();
			fromChannel.transferTo(0, fromChannel.size(), toChannel);
		} catch (Exception e) {
			bSuccess = false;
		} finally {
			try {
				if (fromChannel != null) {
					fromChannel.close();
				}
			} finally {
				if (toChannel != null) {
					toChannel.close();
				}
			}

			if (bDeleteOriginalFile) {
				fromFile.delete();
			}
		}
		return bSuccess;
	}

	public static File backupDatabaseFile(String pathOfFileToBackUp, String destinationFilePath)
			throws IOException {

		File currentDbFile = new File(pathOfFileToBackUp);
		File newDb = new File(destinationFilePath);
		if (currentDbFile.exists()) {
			copyFile(currentDbFile, newDb, false);
			return newDb;
		}
		return null;
	}

	public static boolean copyFileWithStreams(InputStream inputStream, OutputStream outputStream)
			throws IOException {

		boolean bSuccess = true;

		try {
			byte[] buffer = new byte[1024];
			int length;

			while ((length = inputStream.read(buffer)) > 0) {
				outputStream.write(buffer, 0, length);
			}
		} catch (Exception e) {
			bSuccess = false;
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} finally {
				if (outputStream != null) {
					outputStream.close();
				}
			}
		}
		return bSuccess;
	}

	@SuppressLint("Recycle")
	public static void checkpointIfWALEnabled(Context ctx, String DATABASE_NAME) {

		Cursor csr;
		int wal_busy = -99;
		int wal_log = -99;
		int wal_checkpointed = -99;

		SQLiteDatabase db =
				SQLiteDatabase.openDatabase(
						ctx.getDatabasePath(DATABASE_NAME).getPath(),
						null,
						SQLiteDatabase.OPEN_READWRITE);
		csr = db.rawQuery("PRAGMA journal_mode", null);

		if (csr.moveToFirst()) {

			String mode = csr.getString(0);
			if (mode.equalsIgnoreCase("wal")) {

				csr = db.rawQuery("PRAGMA wal_checkpoint", null);

				if (csr.moveToFirst()) {
					wal_busy = csr.getInt(0);
					wal_log = csr.getInt(1);
					wal_checkpointed = csr.getInt(2);
				}

				csr = db.rawQuery("PRAGMA wal_checkpoint(TRUNCATE)", null);
				csr.getCount();
				csr = db.rawQuery("PRAGMA wal_checkpoint", null);

				if (csr.moveToFirst()) {
					wal_busy = csr.getInt(0);
					wal_log = csr.getInt(1);
					wal_checkpointed = csr.getInt(2);
				}
			}
		}
		csr.close();
		db.close();
	}

	public static boolean zip(
			List<File> filesBeingZipped, String zipDirectory, String zipFileName) {

		boolean success = true;

		try {

			int BUFFER = 80000;
			BufferedInputStream origin;
			FileOutputStream dest = new FileOutputStream(zipDirectory + "/" + zipFileName);
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
			byte[] data = new byte[BUFFER];

			for (File fileBeingZipped : filesBeingZipped) {

				FileInputStream fi = new FileInputStream(fileBeingZipped);
				origin = new BufferedInputStream(fi, BUFFER);

				ZipEntry entry =
						new ZipEntry(
								fileBeingZipped
										.getPath()
										.substring(fileBeingZipped.getPath().lastIndexOf("/") + 1));
				out.putNextEntry(entry);
				int count;

				while ((count = origin.read(data, 0, BUFFER)) != -1) {
					out.write(data, 0, count);
				}
				origin.close();
			}
			out.close();
		} catch (Exception e) {
			success = false;
		}

		return success;
	}

	public static void unzip(InputStream uriInputStream, String unzipDirectory) throws Exception {

		ZipInputStream zipInputStream = new ZipInputStream(uriInputStream);
		ZipEntry ze;

		while ((ze = zipInputStream.getNextEntry()) != null) {

			FileOutputStream fileOutputStream =
					new FileOutputStream(unzipDirectory + "/" + ze.getName());

			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
			byte[] buffer = new byte[1024];
			int read = 0;
			while ((read = zipInputStream.read(buffer)) != -1) {
				bufferedOutputStream.write(buffer, 0, read);
			}

			zipInputStream.closeEntry();
			bufferedOutputStream.close();
			fileOutputStream.close();
		}
		zipInputStream.close();
	}
}

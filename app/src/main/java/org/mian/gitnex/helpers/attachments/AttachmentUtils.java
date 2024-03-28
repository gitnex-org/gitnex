package org.mian.gitnex.helpers.attachments;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/**
 * @author M M Arif
 */
public class AttachmentUtils {

	public static File getFile(Context ctx, Uri uri) {

		File destinationFilename;
		try {
			destinationFilename =
					new File(
							ctx.getFilesDir().getPath() + File.separatorChar + queryName(ctx, uri));
		} catch (AssertionError e) {
			destinationFilename = new File(Objects.requireNonNull(uri.getPath()));
		}

		try (InputStream ins = ctx.getContentResolver().openInputStream(uri)) {
			createFileFromStream(ins, destinationFilename);
		} catch (Exception ex) {
			Log.e("AttachmentUtils", Objects.requireNonNull(ex.getMessage()));
		}
		return destinationFilename;
	}

	public static String queryName(Context ctx, Uri uri) {

		Cursor returnCursor = ctx.getContentResolver().query(uri, null, null, null, null);
		assert returnCursor != null;
		int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
		returnCursor.moveToFirst();
		String name = returnCursor.getString(nameIndex);
		returnCursor.close();
		return name;
	}

	public static void createFileFromStream(InputStream ins, File destination) {

		try (OutputStream os = new FileOutputStream(destination)) {
			byte[] buffer = new byte[4096];
			int length;
			while ((length = ins.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
			os.flush();
		} catch (Exception ex) {
			Log.e("AttachmentUtils", Objects.requireNonNull(ex.getMessage()));
		}
	}
}

package org.mian.gitnex.helpers;

import static android.content.Context.DOWNLOAD_SERVICE;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

/**
 * @author M M Arif
 */
public class DownloadService {

	public void downloadFile(Context ctx, String fileUri, String filename) {

		Uri uri = Uri.parse(fileUri);
		DownloadManager.Request request = new DownloadManager.Request(uri);

		request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
		request.setNotificationVisibility(
				DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		request.setMimeType("*/*");
		request.setTitle("Downloading " + filename + " via GitNex");
		DownloadManager downloadManager = (DownloadManager) ctx.getSystemService(DOWNLOAD_SERVICE);
		downloadManager.enqueue(request);

		if (DownloadManager.STATUS_SUCCESSFUL == 8) {
			Toasty.success(ctx, "Download completed");
		}
	}
}

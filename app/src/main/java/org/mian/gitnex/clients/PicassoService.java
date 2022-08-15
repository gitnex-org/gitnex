package org.mian.gitnex.clients;

import android.content.Context;
import android.util.Log;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.helpers.PicassoCache;
import org.mian.gitnex.helpers.ssl.MemorizingTrustManager;
import java.io.File;
import java.security.SecureRandom;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient;

/**
 * @author opyale
 */

public class PicassoService {

	private static volatile PicassoService picassoService;
	private final File cachePath;
	private Picasso picasso;

	private PicassoService(Context context) {

		cachePath = ((BaseActivity) context).getAccount().getPicassoCacheDir(context);
		Picasso.Builder builder = new Picasso.Builder(context);

		try {

			SSLContext sslContext = SSLContext.getInstance("TLS");

			MemorizingTrustManager memorizingTrustManager = new MemorizingTrustManager(context);
			sslContext.init(null, new X509TrustManager[]{memorizingTrustManager}, new SecureRandom());

			OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder().sslSocketFactory(sslContext.getSocketFactory(), memorizingTrustManager)
				.hostnameVerifier(memorizingTrustManager.wrapHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier()));

			builder.downloader(new OkHttp3Downloader(okHttpClient.build()));
			/*builder.listener((picasso, uri, exception) -> {

				Log.e("PicassoService", Objects.requireNonNull(uri.toString()));
				Log.e("PicassoService", exception.toString());

			});*/

			picasso = builder.memoryCache(new PicassoCache(cachePath, context)).build();

		}
		catch(Exception e) {

			Log.e("PicassoService", e.toString());
		}
	}

	public static synchronized PicassoService getInstance(Context context) {

		if(picassoService == null) {
			synchronized(PicassoService.class) {
				if(picassoService == null) {
					picassoService = new PicassoService(context);
				}
			}
		}

		return picassoService;

	}

	public Picasso get() {

		cachePath.mkdirs();
		return picasso;

	}

}

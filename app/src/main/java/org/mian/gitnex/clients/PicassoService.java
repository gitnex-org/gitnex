package org.mian.gitnex.clients;

import android.content.Context;
import android.util.Log;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import org.mian.gitnex.helpers.ssl.MemorizingTrustManager;
import java.security.SecureRandom;
import java.util.Objects;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient;

/**
 * Author anonTree1417
 */

public class PicassoService {

	private static PicassoService picassoService;
	private Picasso picasso;

	private PicassoService(Context context) {

		Picasso.Builder builder = new Picasso.Builder(context);

		try {

			SSLContext sslContext = SSLContext.getInstance("TLS");

			MemorizingTrustManager memorizingTrustManager = new MemorizingTrustManager(context);
			sslContext.init(null, new X509TrustManager[]{memorizingTrustManager}, new SecureRandom());

			OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder()
					.sslSocketFactory(sslContext.getSocketFactory(), memorizingTrustManager)
					.hostnameVerifier(memorizingTrustManager.wrapHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier()));

			builder.downloader(new OkHttp3Downloader(okHttpClient.build()));
			builder.listener((picasso, uri, exception) -> {

				Log.e("PicassoService", Objects.requireNonNull(uri.toString())); // important!!
				Log.e("PicassoService", exception.toString());

			});

			picasso = builder.build();

		}
		catch(Exception e) {

			Log.e("PicassoService", e.toString());
		}

	}

	public Picasso get() {

		return picasso;
	}

	public static synchronized PicassoService getInstance(Context context) {

		if(picassoService == null) {
			picassoService = new PicassoService(context);
		}

		return picassoService;
	}

}

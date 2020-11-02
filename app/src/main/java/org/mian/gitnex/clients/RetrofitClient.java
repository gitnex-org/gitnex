package org.mian.gitnex.clients;

import android.content.Context;
import android.util.Log;
import org.mian.gitnex.helpers.FilesData;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.ssl.MemorizingTrustManager;
import org.mian.gitnex.interfaces.ApiInterface;
import org.mian.gitnex.interfaces.WebInterface;
import java.io.File;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Author M M Arif
 */

public class RetrofitClient {

	private static final Map<String, ApiInterface> apiInterfaces = new HashMap<>();
	private static final Map<String, WebInterface> webInterfaces = new HashMap<>();

	private static Retrofit createRetrofit(Context context, String instanceUrl) {

		TinyDB tinyDB = TinyDB.getInstance(context);

		int cacheSize = FilesData.returnOnlyNumber(tinyDB.getString("cacheSizeStr")) * 1024 * 1024;
		Cache cache = new Cache(new File(context.getCacheDir(), "responses"), cacheSize);

		try {

			SSLContext sslContext = SSLContext.getInstance("TLS");

			MemorizingTrustManager memorizingTrustManager = new MemorizingTrustManager(context);
			sslContext.init(null, new X509TrustManager[]{ memorizingTrustManager }, new SecureRandom());

			OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder().cache(cache)
				.sslSocketFactory(sslContext.getSocketFactory(), memorizingTrustManager)
				.hostnameVerifier(memorizingTrustManager.wrapHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier()))
				.addInterceptor(chain -> {

					Request request = chain.request()
						.newBuilder()
						.header("Cache-Control", "public, max-age=" + 60)
						.build();

					return chain.proceed(request);

				});

			return new Retrofit.Builder()
				.baseUrl(instanceUrl)
				.client(okHttpClient.build())
				.addConverterFactory(ScalarsConverterFactory.create())
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		} catch(Exception e) {

			Log.e("onFailure", e.toString());
		}

		return null;
	}

	public static synchronized ApiInterface getApiInterface(Context context) {

		return getApiInterface(context, TinyDB.getInstance(context).getString("instanceUrl"));
	}

	public static synchronized WebInterface getWebInterface(Context context) {

		String instanceUrl = TinyDB.getInstance(context).getString("instanceUrl");
		instanceUrl = instanceUrl.substring(0, instanceUrl.lastIndexOf("api/v1/"));

		return getWebInterface(context, instanceUrl);

	}

	public static synchronized ApiInterface getApiInterface(Context context, String url) {
		if(!apiInterfaces.containsKey(url)) {

			ApiInterface apiInterface = createRetrofit(context, url)
				.create(ApiInterface.class);

			apiInterfaces.put(url, apiInterface);
			return apiInterface;

		}

		return apiInterfaces.get(url);
	}

	public static synchronized WebInterface getWebInterface(Context context, String url) {
		if(!webInterfaces.containsKey(url)) {

			WebInterface webInterface = createRetrofit(context, url)
				.create(WebInterface.class);

			webInterfaces.put(url, webInterface);
			return webInterface;

		}

		return webInterfaces.get(url);
	}
}

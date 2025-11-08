package org.mian.gitnex.api.clients;

import android.content.Context;
import java.io.File;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.FilesData;
import org.mian.gitnex.helpers.ssl.MemorizingTrustManager;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * @author mmarif
 */
public class ApiRetrofitClient {

	private static final Map<String, ApiInterface> instances = new ConcurrentHashMap<>();
	private static final int CACHE_SIZE_MB = 50;
	private static final int MAX_STALE_SECONDS = 60 * 60 * 24 * 30;

	public static ApiInterface getInstance(Context context) {
		if (!(context instanceof BaseActivity)) return null;
		var account = ((BaseActivity) context).getAccount().getAccount();
		String url = account.getInstanceUrl();
		String token = ((BaseActivity) context).getAccount().getAuthorization();
		File cacheFile = new File(context.getCacheDir(), "http-cache");

		String key = token.hashCode() + "@" + url;
		return instances.computeIfAbsent(key, k -> createApi(context, url, token, cacheFile));
	}

	private static ApiInterface createApi(
			Context context, String url, String token, File cacheFile) {
		OkHttpClient client = buildOkHttpClient(context, token, cacheFile);
		Retrofit retrofit =
				new Retrofit.Builder()
						.baseUrl(url)
						.client(client)
						.addConverterFactory(ScalarsConverterFactory.create())
						.addConverterFactory(GsonConverterFactory.create())
						.build();
		return retrofit.create(ApiInterface.class);
	}

	private static OkHttpClient buildOkHttpClient(Context context, String token, File cacheFile) {
		try {
			SSLContext sslContext = SSLContext.getInstance("TLS");
			MemorizingTrustManager trustManager = new MemorizingTrustManager(context);
			sslContext.init(null, new X509TrustManager[] {trustManager}, new SecureRandom());

			// HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
			// logging.setLevel(HttpLoggingInterceptor.Level.BODY);

			OkHttpClient.Builder builder =
					new OkHttpClient.Builder()
							.sslSocketFactory(sslContext.getSocketFactory(), trustManager)
							// .addInterceptor(logging)
							.hostnameVerifier(
									trustManager.wrapHostnameVerifier(
											HttpsURLConnection.getDefaultHostnameVerifier()))
							.addInterceptor(userAgentInterceptor(context))
							.addInterceptor(authInterceptor(token));

			if (cacheFile != null) {
				int cacheSize = getCacheSize(context);
				File cacheDir = new File(context.getCacheDir(), "http-cache");
				if (!cacheDir.exists()) cacheDir.mkdirs();
				builder.cache(new Cache(cacheDir, cacheSize))
						.addInterceptor(cacheInterceptor(context))
						.addNetworkInterceptor(networkInterceptor());
			}

			return builder.build();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static Interceptor userAgentInterceptor(Context ctx) {
		return chain -> {
			Request req =
					chain.request()
							.newBuilder()
							.header(
									"User-Agent",
									"GitNex/"
											+ AppUtil.getAppVersion(ctx)
											+ " (Android "
											+ android.os.Build.VERSION.RELEASE
											+ ")")
							.build();
			return chain.proceed(req);
		};
	}

	private static Interceptor authInterceptor(String token) {
		return chain ->
				chain.proceed(chain.request().newBuilder().header("Authorization", token).build());
	}

	private static Interceptor cacheInterceptor(Context ctx) {
		return chain -> {
			Request req = chain.request();
			boolean hasNetwork = AppUtil.hasNetworkConnection(ctx);
			CacheControl control =
					hasNetwork
							? CacheControl.FORCE_NETWORK
							: new CacheControl.Builder()
									.onlyIfCached()
									.maxStale(MAX_STALE_SECONDS, TimeUnit.SECONDS)
									.build();
			return chain.proceed(req.newBuilder().cacheControl(control).build());
		};
	}

	private static Interceptor networkInterceptor() {
		return chain -> {
			Response resp = chain.proceed(chain.request());
			if ("GET".equals(chain.request().method()) && resp.isSuccessful()) {
				return resp.newBuilder()
						.header(
								"Cache-Control",
								"public, only-if-cached, max-stale=" + MAX_STALE_SECONDS)
						.removeHeader("Pragma")
						.build();
			}
			return resp;
		};
	}

	private static int getCacheSize(Context ctx) {
		try {
			return FilesData.returnOnlyNumberFileSize(
							AppDatabaseSettings.getSettingsValue(
									ctx, AppDatabaseSettings.APP_DATA_CACHE_SIZE_KEY))
					* 1024
					* 1024;
		} catch (Exception e) {
			return CACHE_SIZE_MB * 1024 * 1024;
		}
	}
}

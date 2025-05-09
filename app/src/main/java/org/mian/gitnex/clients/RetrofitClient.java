package org.mian.gitnex.clients;

import android.content.Context;
import androidx.annotation.NonNull;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
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
import org.gitnex.tea4j.v2.apis.AdminApi;
import org.gitnex.tea4j.v2.apis.IssueApi;
import org.gitnex.tea4j.v2.apis.MiscellaneousApi;
import org.gitnex.tea4j.v2.apis.NotificationApi;
import org.gitnex.tea4j.v2.apis.OrganizationApi;
import org.gitnex.tea4j.v2.apis.PackageApi;
import org.gitnex.tea4j.v2.apis.RepositoryApi;
import org.gitnex.tea4j.v2.apis.SettingsApi;
import org.gitnex.tea4j.v2.apis.UserApi;
import org.gitnex.tea4j.v2.apis.custom.CustomApi;
import org.gitnex.tea4j.v2.apis.custom.OTPApi;
import org.gitnex.tea4j.v2.apis.custom.WebApi;
import org.gitnex.tea4j.v2.auth.ApiKeyAuth;
import org.jetbrains.annotations.NotNull;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.FilesData;
import org.mian.gitnex.helpers.ssl.MemorizingTrustManager;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * @author M M Arif
 */
public class RetrofitClient {

	private static final Map<String, ApiInterface> apiInterfaces = new ConcurrentHashMap<>();
	private static final Map<String, WebApi> webInterfaces = new ConcurrentHashMap<>();
	private static final int CACHE_SIZE_MB = 50;
	private static final int MAX_STALE_SECONDS = 60 * 60 * 24 * 30;

	private static OkHttpClient buildOkHttpClient(Context context, String token, File cacheFile) {

		// HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
		// logging.setLevel(HttpLoggingInterceptor.Level.BODY);

		try {
			SSLContext sslContext = SSLContext.getInstance("TLS");
			MemorizingTrustManager memorizingTrustManager = new MemorizingTrustManager(context);
			sslContext.init(
					null, new X509TrustManager[] {memorizingTrustManager}, new SecureRandom());

			ApiKeyAuth auth = new ApiKeyAuth("header", "Authorization");
			auth.setApiKey(token);

			OkHttpClient.Builder okHttpClient =
					new OkHttpClient.Builder()
							// .addInterceptor(logging)
							.sslSocketFactory(sslContext.getSocketFactory(), memorizingTrustManager)
							.hostnameVerifier(
									memorizingTrustManager.wrapHostnameVerifier(
											HttpsURLConnection.getDefaultHostnameVerifier()));

			if (cacheFile != null) {
				int cacheSize = CACHE_SIZE_MB;
				try {
					cacheSize =
							FilesData.returnOnlyNumberFileSize(
									AppDatabaseSettings.getSettingsValue(
											context, AppDatabaseSettings.APP_DATA_CACHE_SIZE_KEY));
				} catch (Exception ignored) {
				}
				cacheSize = cacheSize * 1024 * 1024;

				File cacheDir = new File(context.getCacheDir(), "http-cache");
				if (!cacheDir.exists()) {
					if (!cacheDir.mkdirs()) {
						throw new RuntimeException(
								"Failed to create cache directory: " + cacheDir.getAbsolutePath());
					}
				}
				Cache cache = new Cache(cacheDir, cacheSize);
				okHttpClient.cache(cache);

				Interceptor cacheInterceptor =
						chain -> {
							Request originalRequest = chain.request();
							boolean hasNetwork = AppUtil.hasNetworkConnection(context);
							CacheControl cacheControl;

							if (hasNetwork) {
								cacheControl =
										new CacheControl.Builder()
												.maxAge(0, TimeUnit.SECONDS)
												.build();
							} else {
								cacheControl =
										new CacheControl.Builder()
												.onlyIfCached()
												.maxStale(MAX_STALE_SECONDS, TimeUnit.SECONDS)
												.build();
							}

							Request modifiedRequest =
									originalRequest.newBuilder().cacheControl(cacheControl).build();

							return chain.proceed(modifiedRequest);
						};

				Interceptor networkInterceptor =
						chain -> {
							Request request = chain.request();
							Response response = chain.proceed(request);
							if (request.method().equals("GET")
									&& response.isSuccessful()
									&& AppUtil.hasNetworkConnection(context)) {
								return response.newBuilder()
										.header(
												"Cache-Control",
												"public, only-if-cached, max-stale="
														+ MAX_STALE_SECONDS)
										.removeHeader("Pragma")
										.build();
							}
							return response;
						};

				okHttpClient
						.addInterceptor(auth)
						.addInterceptor(cacheInterceptor)
						.addNetworkInterceptor(networkInterceptor);
			} else {
				okHttpClient.addInterceptor(auth);
			}

			return okHttpClient.build();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static Retrofit createRetrofit(
			Context context, String instanceUrl, String token, File cacheFile) {
		OkHttpClient okHttpClient = buildOkHttpClient(context, token, cacheFile);
		return new Retrofit.Builder()
				.baseUrl(instanceUrl)
				.client(okHttpClient)
				.addConverterFactory(ScalarsConverterFactory.create())
				.addConverterFactory(
						GsonConverterFactory.create(
								new GsonBuilder()
										.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
										.create()))
				.addConverterFactory(DateQueryConverterFactory.create())
				.build();
	}

	public static OkHttpClient getOkHttpClient(Context context, String token) {
		File cacheFile = new File(context.getCacheDir(), "http-cache");
		return buildOkHttpClient(context, token, cacheFile);
	}

	public static ApiInterface getApiInterface(Context context) {
		if (!(context instanceof BaseActivity)
				|| ((BaseActivity) context).getAccount() == null
				|| ((BaseActivity) context).getAccount().getAccount() == null) {
			throw new IllegalStateException(
					"No active account available. Use explicit URL and token.");
		}
		return getApiInterface(
				context,
				((BaseActivity) context).getAccount().getAccount().getInstanceUrl(),
				((BaseActivity) context).getAccount().getAuthorization(),
				((BaseActivity) context).getAccount().getCacheDir(context));
	}

	public static WebApi getWebInterface(Context context) {
		String instanceUrl = ((BaseActivity) context).getAccount().getAccount().getInstanceUrl();
		instanceUrl = instanceUrl.substring(0, instanceUrl.lastIndexOf("api/v1/"));
		return getWebInterface(
				context,
				instanceUrl,
				((BaseActivity) context).getAccount().getWebAuthorization(),
				((BaseActivity) context).getAccount().getCacheDir(context));
	}

	public static WebApi getWebInterface(Context context, String url) {
		return getWebInterface(
				context,
				url,
				((BaseActivity) context).getAccount().getAuthorization(),
				((BaseActivity) context).getAccount().getCacheDir(context));
	}

	public static ApiInterface getApiInterface(
			Context context, String url, String token, File cacheFile) {
		String key = (token != null ? token.hashCode() : 0) + "@" + url;
		if (cacheFile == null || !apiInterfaces.containsKey(key)) {
			synchronized (RetrofitClient.class) {
				if (cacheFile == null || !apiInterfaces.containsKey(key)) {
					ApiInterface apiInterface =
							Objects.requireNonNull(createRetrofit(context, url, token, cacheFile))
									.create(ApiInterface.class);
					if (cacheFile != null) {
						apiInterfaces.put(key, apiInterface);
					}
					return apiInterface;
				}
			}
		}
		return apiInterfaces.get(key);
	}

	public static WebApi getWebInterface(
			Context context, String url, String token, File cacheFile) {
		String key = (token != null ? token.hashCode() : 0) + "@" + url;
		if (!webInterfaces.containsKey(key)) {
			synchronized (RetrofitClient.class) {
				if (!webInterfaces.containsKey(key)) {
					WebApi webInterface =
							Objects.requireNonNull(createRetrofit(context, url, token, cacheFile))
									.create(WebApi.class);
					webInterfaces.put(key, webInterface);
					return webInterface;
				}
			}
		}
		return webInterfaces.get(key);
	}

	public interface ApiInterface
			extends AdminApi,
					OrganizationApi,
					IssueApi,
					RepositoryApi,
					MiscellaneousApi,
					NotificationApi,
					UserApi,
					SettingsApi,
					OTPApi,
					CustomApi,
					PackageApi {}

	private static class DateQueryConverterFactory extends Converter.Factory {
		public static DateQueryConverterFactory create() {
			return new DateQueryConverterFactory();
		}

		@Override
		public Converter<?, String> stringConverter(
				@NotNull Type type,
				@NonNull @NotNull Annotation[] annotations,
				@NotNull Retrofit retrofit) {
			if (type == Date.class) {
				return DateQueryConverter.INSTANCE;
			}
			return null;
		}

		private static class DateQueryConverter implements Converter<Date, String> {
			static final DateQueryConverter INSTANCE = new DateQueryConverter();

			private static final ThreadLocal<DateFormat> DF =
					new ThreadLocal<>() {
						@Override
						public DateFormat initialValue() {
							return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
						}
					};

			@Override
			public String convert(@NotNull Date date) {
				return Objects.requireNonNull(DF.get()).format(date);
			}
		}
	}
}

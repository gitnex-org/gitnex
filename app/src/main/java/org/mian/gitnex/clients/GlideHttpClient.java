package org.mian.gitnex.clients;

import android.content.Context;
import java.security.SecureRandom;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.gitnex.tea4j.v2.auth.ApiKeyAuth;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.ssl.MemorizingTrustManager;

/**
 * @author mmarif
 */
public class GlideHttpClient {

	public static OkHttpClient getOkHttpClient(Context context, String token) {
		try {
			SSLContext sslContext = SSLContext.getInstance("TLS");
			MemorizingTrustManager memorizingTrustManager = new MemorizingTrustManager(context);
			sslContext.init(
					null, new X509TrustManager[] {memorizingTrustManager}, new SecureRandom());

			ApiKeyAuth auth = new ApiKeyAuth("header", "Authorization");
			auth.setApiKey(token);

			OkHttpClient.Builder builder =
					new OkHttpClient.Builder()
							.sslSocketFactory(sslContext.getSocketFactory(), memorizingTrustManager)
							.hostnameVerifier(
									memorizingTrustManager.wrapHostnameVerifier(
											HttpsURLConnection.getDefaultHostnameVerifier()))
							.addInterceptor(
									chain -> {
										Request originalRequest = chain.request();
										Request modifiedRequest =
												originalRequest
														.newBuilder()
														.header(
																"User-Agent",
																"GitNex/"
																		+ AppUtil.getAppVersion(
																				context)
																		+ " (Android "
																		+ android.os.Build.VERSION
																				.RELEASE
																		+ ")")
														.build();
										return chain.proceed(modifiedRequest);
									})
							.addInterceptor(auth);

			return builder.build();

		} catch (Exception e) {
			throw new RuntimeException("Failed to create Glide OkHttpClient: " + e.getMessage(), e);
		}
	}

	public static OkHttpClient getUnsafeOkHttpClient(String token) {
		try {
			@SuppressWarnings("CustomX509TrustManager")
			final X509TrustManager trustAllCerts =
					new X509TrustManager() {
						@SuppressWarnings("TrustAllX509TrustManager")
						@Override
						public void checkClientTrusted(
								java.security.cert.X509Certificate[] chain, String authType) {}

						@SuppressWarnings("TrustAllX509TrustManager")
						@Override
						public void checkServerTrusted(
								java.security.cert.X509Certificate[] chain, String authType) {}

						@Override
						public java.security.cert.X509Certificate[] getAcceptedIssuers() {
							return new java.security.cert.X509Certificate[] {};
						}
					};

			final SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, new X509TrustManager[] {trustAllCerts}, new SecureRandom());
			final javax.net.ssl.SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

			ApiKeyAuth auth = new ApiKeyAuth("header", "Authorization");
			auth.setApiKey(token);

			OkHttpClient.Builder builder =
					new OkHttpClient.Builder()
							.sslSocketFactory(sslSocketFactory, trustAllCerts)
							.hostnameVerifier((hostname, session) -> true)
							.addInterceptor(auth);

			return builder.build();

		} catch (Exception e) {
			throw new RuntimeException(
					"Failed to create unsafe Glide OkHttpClient: " + e.getMessage(), e);
		}
	}
}

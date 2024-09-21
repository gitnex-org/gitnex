package org.mian.gitnex.clients;

import android.annotation.SuppressLint;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient;

/**
 * @author mmarif
 */
public class GlideHttpClient {

	public static OkHttpClient getUnsafeOkHttpClient() {

		try {
			@SuppressLint("CustomX509TrustManager")
			final TrustManager[] trustAllCerts =
					new TrustManager[] {
						new X509TrustManager() {
							@SuppressLint("TrustAllX509TrustManager")
							@Override
							public void checkClientTrusted(
									java.security.cert.X509Certificate[] chain, String authType) {}

							@SuppressLint("TrustAllX509TrustManager")
							@Override
							public void checkServerTrusted(
									java.security.cert.X509Certificate[] chain, String authType) {}

							@Override
							public java.security.cert.X509Certificate[] getAcceptedIssuers() {
								return new java.security.cert.X509Certificate[] {};
							}
						}
					};

			// Install the all-trusting trust manager
			final SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

			// Create an ssl socket factory with our all-trusting manager
			final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

			OkHttpClient.Builder builder = new OkHttpClient.Builder();
			builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
			builder.hostnameVerifier((hostname, session) -> true);

			return builder.build();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

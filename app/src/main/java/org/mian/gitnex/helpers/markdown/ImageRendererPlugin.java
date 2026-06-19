package org.mian.gitnex.helpers.markdown;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.image.ImagesPlugin;
import io.noties.markwon.image.network.OkHttpNetworkSchemeHandler;
import io.noties.markwon.image.svg.SvgMediaDecoder;
import java.io.File;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.helpers.contexts.RepositoryContext;

/**
 * @author mmarif
 */
public class ImageRendererPlugin extends AbstractMarkwonPlugin {

	private final Context context;
	private final String baseUrl;
	private final OkHttpClient okHttpClient;

	public static ImageRendererPlugin create(Context context, RepositoryContext repo) {
		return new ImageRendererPlugin(context, repo);
	}

	public OkHttpClient getOkHttpClient() {
		return okHttpClient;
	}

	private ImageRendererPlugin(Context context, RepositoryContext repo) {
		this.context = context.getApplicationContext();
		this.okHttpClient = createImageOkHttpClient();
		String url = ((BaseActivity) context).getAccount().getAccount().getInstanceUrl();
		String instanceUrl = url.substring(0, url.lastIndexOf("api/v1/"));
		String branch = repo.getBranchRef();
		if (branch == null || branch.isEmpty()) {
			branch = "main";
		}
		this.baseUrl = instanceUrl + repo.getFullName() + "/raw/branch/" + branch + "/";
	}

	@Override
	public void configure(@NonNull Registry registry) {
		registry.require(
				ImagesPlugin.class,
				imagesPlugin -> {
					imagesPlugin.executorService(Executors.newFixedThreadPool(2));
					imagesPlugin.addSchemeHandler(OkHttpNetworkSchemeHandler.create(okHttpClient));
					imagesPlugin.addMediaDecoder(SvgMediaDecoder.create());
					imagesPlugin.placeholderProvider(
							drawable -> {
								ColorDrawable placeholder = new ColorDrawable(0xFFF0F0F0);
								placeholder.setBounds(0, 0, 1, 1);
								return placeholder;
							});
				});
	}

	public String preProcessMarkdown(String markdown) {
		return markdown.replaceAll("!\\[([^]]*)]\\((?!http)([^)]+)\\)", "![$1](" + baseUrl + "$2)");
	}

	@SuppressLint({"CustomX509TrustManager", "TrustAllX509TrustManager"})
	private OkHttpClient createImageOkHttpClient() {
		try {
			TrustManager[] trustAllCerts =
					new TrustManager[] {
						new X509TrustManager() {
							public void checkClientTrusted(
									X509Certificate[] chain, String authType) {}

							public void checkServerTrusted(
									X509Certificate[] chain, String authType) {}

							public X509Certificate[] getAcceptedIssuers() {
								return new X509Certificate[] {};
							}
						}
					};

			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, trustAllCerts, new SecureRandom());

			File cacheDir = new File(context.getCacheDir(), "image-cache");
			Cache cache = new Cache(cacheDir, 20 * 1024 * 1024);

			return new OkHttpClient.Builder()
					.sslSocketFactory(
							sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
					.hostnameVerifier((hostname, session) -> true)
					.connectTimeout(15, TimeUnit.SECONDS)
					.readTimeout(15, TimeUnit.SECONDS)
					.cache(cache)
					.build();
		} catch (Exception e) {
			return new OkHttpClient();
		}
	}
}

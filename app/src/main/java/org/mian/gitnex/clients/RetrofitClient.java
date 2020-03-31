package org.mian.gitnex.clients;

import android.content.Context;
import android.util.Log;
import org.mian.gitnex.interfaces.ApiInterface;
import org.mian.gitnex.helpers.MemorizingTrustManager;
import org.mian.gitnex.util.AppUtil;
import java.io.File;
import java.security.SecureRandom;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Author M M Arif
 */

public class RetrofitClient {

    private Retrofit retrofit;

    private RetrofitClient(String instanceUrl, Context ctx) {
        final boolean connToInternet = AppUtil.haveNetworkConnection(ctx);
        int cacheSize = 50 * 1024 * 1024; // 50MB
        File httpCacheDirectory = new File(ctx.getCacheDir(), "responses");
        Cache cache = new Cache(httpCacheDirectory, cacheSize);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");

            MemorizingTrustManager memorizingTrustManager = new MemorizingTrustManager(ctx);
            sslContext.init(null, new X509TrustManager[] { memorizingTrustManager }, new SecureRandom());

            OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder()
                    .cache(cache)
		            //.addInterceptor(logging)
                    .sslSocketFactory(sslContext.getSocketFactory(), memorizingTrustManager)
                    .hostnameVerifier(memorizingTrustManager.wrapHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier()))
                    .addInterceptor(chain -> {

                        Request request = chain.request();
                        if(connToInternet) {
                        	request = request.newBuilder().header("Cache-Control", "public, max-age=" + 60).build();
                        }
                        else {
                        	request = request.newBuilder().header("Cache-Control", "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 30).build();
                        }
                        return chain.proceed(request);
                    });

            Retrofit.Builder builder = new Retrofit.Builder()
                    .baseUrl(instanceUrl)
                    .client(okHttpClient.build())
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create());

            retrofit = builder.build();

        }
        catch(Exception e) {
	        Log.e("onFailure", e.toString());
        }
    }

    public static synchronized RetrofitClient getInstance(String instanceUrl, Context ctx) {
        return new RetrofitClient(instanceUrl, ctx);
    }

    public ApiInterface getApiInterface() {
        return retrofit.create(ApiInterface.class);
    }

}

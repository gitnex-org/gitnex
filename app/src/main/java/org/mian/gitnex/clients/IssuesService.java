package org.mian.gitnex.clients;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import org.mian.gitnex.helpers.MemorizingTrustManager;
import org.mian.gitnex.util.AppUtil;
import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Author M M Arif
 */

public class IssuesService {

    public static <S> S createService(Class<S> serviceClass, String instanceURL, Context ctx) {

        final boolean connToInternet = AppUtil.haveNetworkConnection(ctx);
        File httpCacheDirectory = new File(ctx.getCacheDir(), "responses");
        int cacheSize = 50 * 1024 * 1024; // 50MB
        Cache cache = new Cache(httpCacheDirectory, cacheSize);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");

            MemorizingTrustManager memorizingTrustManager = new MemorizingTrustManager(ctx);
            sslContext.init(null, new X509TrustManager[]{memorizingTrustManager}, new SecureRandom());

            OkHttpClient okHttpClient = new OkHttpClient.Builder().cache(cache).sslSocketFactory(sslContext.getSocketFactory(), memorizingTrustManager).hostnameVerifier(memorizingTrustManager.wrapHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier())).addInterceptor(new Interceptor() {

                @NonNull
                @Override
                public Response intercept(@NonNull Chain chain) throws IOException {

                    Request request = chain.request();
                    if(connToInternet) {
                        request = request.newBuilder().header("Cache-Control", "public, max-age=" + 60).build();
                    }
                    else {
                        request = request.newBuilder().header("Cache-Control", "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 30).build();
                    }
                    return chain.proceed(request);
                }
            }).build();

            Retrofit.Builder builder = new Retrofit.Builder().baseUrl(instanceURL).client(okHttpClient).addConverterFactory(GsonConverterFactory.create());

            Retrofit retrofit = builder.build();
            return retrofit.create(serviceClass);
        }
        catch(Exception e) {
	        Log.e("onFailure", e.toString());
        }

        return null;
    }
}

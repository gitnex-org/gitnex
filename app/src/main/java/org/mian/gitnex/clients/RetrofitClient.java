package org.mian.gitnex.clients;

import org.mian.gitnex.interfaces.ApiInterface;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Author M M Arif
 */

public class RetrofitClient {

    private Retrofit retrofit;

    private RetrofitClient(String instanceUrl) {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        httpClient.addInterceptor(logging);

        retrofit = new Retrofit.Builder()
                .baseUrl(instanceUrl)
                .addConverterFactory(GsonConverterFactory.create())
                //.client(httpClient.build())
                .build();

    }

    public static synchronized RetrofitClient getInstance(String instanceUrl) {
        return new RetrofitClient(instanceUrl);
    }

    public ApiInterface getApiInterface() {
        return retrofit.create(ApiInterface.class);
    }

}

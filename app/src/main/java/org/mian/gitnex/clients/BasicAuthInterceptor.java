package org.mian.gitnex.clients;

import androidx.annotation.NonNull;
import java.io.IOException;
import java.util.Base64;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author mmarif
 */
public class BasicAuthInterceptor implements Interceptor {

	private final String username;
	private final String password;

	public BasicAuthInterceptor(String username, String password) {
		this.username = username;
		this.password = password;
	}

	@NonNull @Override
	public Response intercept(Chain chain) throws IOException {
		Request originalRequest = chain.request();

		String credentials = username + ":" + password;
		String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

		Request modifiedRequest =
				originalRequest
						.newBuilder()
						.header("X-Proxy-Auth", "Basic " + encodedCredentials)
						.build();

		return chain.proceed(modifiedRequest);
	}
}

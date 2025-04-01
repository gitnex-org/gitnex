package org.mian.gitnex.clients;

import android.content.Context;
import androidx.annotation.NonNull;
import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import java.io.InputStream;
import okhttp3.OkHttpClient;
import org.mian.gitnex.activities.BaseActivity;

/**
 * @author mmarif
 */
@GlideModule
public class GlideService extends AppGlideModule {

	@Override
	public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
		super.applyOptions(context, builder);
	}

	@Override
	public void registerComponents(
			@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
		String token = "";
		if (context instanceof BaseActivity) {
			token = ((BaseActivity) context).getAccount().getAuthorization();
		}
		OkHttpClient okHttpClient = RetrofitClient.getOkHttpClient(context, token);
		registry.replace(
				GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(okHttpClient));
	}
}

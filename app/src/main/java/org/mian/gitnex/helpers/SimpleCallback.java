package org.mian.gitnex.helpers;

import android.util.Log;
import androidx.annotation.NonNull;
import java.io.File;
import java.util.Optional;
import java.util.stream.Collectors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author opyale
 */

public interface SimpleCallback<T> extends Callback<T> {

	void onFinished(@NonNull Call<T> call, @NonNull Optional<Response<T>> optionalResponse);

	default void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
		onFinished(call, Optional.of(response));
	}

	default void onFailure(@NonNull Call<T> call, @NonNull Throwable throwable) {
		onFinished(call, Optional.empty());

		Log.e(call.request().url()
			.pathSegments()
			.stream()
			.collect(Collectors.joining(File.pathSeparator)), throwable.toString());
	}

}

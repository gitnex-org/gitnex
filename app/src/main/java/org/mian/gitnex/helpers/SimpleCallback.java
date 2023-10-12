package org.mian.gitnex.helpers;

import androidx.annotation.NonNull;
import java.util.Optional;
import org.mian.gitnex.fragments.NotificationsFragment;
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
		NotificationsFragment.emptyErrorResponse = throwable.getMessage();
	}
}

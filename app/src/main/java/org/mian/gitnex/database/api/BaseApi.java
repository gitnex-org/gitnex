package org.mian.gitnex.database.api;

import android.content.Context;
import androidx.annotation.NonNull;
import org.mian.gitnex.database.db.GitnexDatabase;
import org.mian.gitnex.helpers.Constants;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author opyale
 */

public abstract class BaseApi {

	private static final Map<Class<? extends BaseApi>, Object> instances = new HashMap<>();

	protected static final ExecutorService executorService = Executors.newCachedThreadPool();
	protected final GitnexDatabase gitnexDatabase;

	protected BaseApi(Context context) {
		gitnexDatabase = GitnexDatabase.getDatabaseInstance(context);
	}

	public static <T extends BaseApi> T getInstance(@NonNull Context context, @NonNull Class<T> clazz) {

		try {

			if(!instances.containsKey(clazz)) {
				synchronized(BaseApi.class) {
					if(!instances.containsKey(clazz)) {

						T instance = clazz
							.getDeclaredConstructor(Context.class)
							.newInstance(context);

						instances.put(clazz, instance);
						return instance;
					}
				}
			}

			return (T) instances.get(clazz);

		} catch(NoSuchMethodException | IllegalAccessException |
			InvocationTargetException | InstantiationException ignored) {}

		return null;
	}

}

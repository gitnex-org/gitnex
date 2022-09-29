package org.mian.gitnex.actions;

import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author opyale
 */
public class ActionResult<R> {

	private final BlockingQueue<Boolean> blockingQueue;
	private final List<OnFinishedListener<R>> onFinishedListeners;
	private boolean invalidated = false;

	public ActionResult() {

		blockingQueue = new ArrayBlockingQueue<>(1);
		onFinishedListeners = new ArrayList<>();
	}

	public void finish(@NonNull Status status) {

		finish(status, null);
	}

	public void finish(@NonNull Status status, R result) {

		try {
			if (blockingQueue.poll(5, TimeUnit.SECONDS)) {

				for (OnFinishedListener<R> onFinishedListener : onFinishedListeners)
					onFinishedListener.onFinished(status, result);
			}

		} catch (InterruptedException ignored) {
		}
	}

	public void invalidate() {

		if (invalidated) {
			throw new IllegalStateException("Already invalidated");
		}
		this.invalidated = true;
	}

	@SafeVarargs
	public final synchronized void accept(@NonNull OnFinishedListener<R>... onFinishedListeners) {

		invalidate();

		this.blockingQueue.add(true);
		this.onFinishedListeners.addAll(Arrays.asList(onFinishedListeners));
	}

	public final synchronized void discard() {

		invalidate();
		this.blockingQueue.add(false);
	}

	public enum Status {
		SUCCESS,
		FAILED
	}

	public interface OnFinishedListener<R> {

		void onFinished(Status status, R result);
	}

	public static class None {}
}

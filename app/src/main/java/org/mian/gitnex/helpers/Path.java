package org.mian.gitnex.helpers;

import androidx.annotation.NonNull;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

/**
 * @author opyale
 */

public class Path {

	private final List<String> segments;
	private final List<Runnable> onChangedListeners;

	public Path(String... segments) {

		this.segments = new ArrayList<>(Arrays.asList(segments));
		this.onChangedListeners = new ArrayList<>();

	}

	public Path addListener(Runnable onChangedListener) {

		onChangedListeners.add(onChangedListener);
		return this;

	}

	public Path removeListener(Runnable onChangedListener) {

		onChangedListeners.remove(onChangedListener);
		return this;

	}

	private void pathChanged() {

		for(Runnable onChangedListener : onChangedListeners) {
			onChangedListener.run();
		}
	}

	public Path add(String segment)  {

		if(segment != null && !segment.trim().isEmpty()) {

			try {
				segments.add(URLEncoder.encode(segment, "UTF-8"));
			} catch(UnsupportedEncodingException ignored) {}
		}

		pathChanged();
		return this;

	}

	public int size() {

		return segments.size();
	}

	public Path join(Path path) {

		this.segments.addAll(path.segments);

		pathChanged();
		return this;

	}

	public Path pop(int count) {

		for(int i=0; i<count; i++)
			segments.remove(segments.size() - 1);

		pathChanged();
		return this;

	}

	public Path remove(int index) {

		segments.remove(index);
		pathChanged();

		return this;

	}

	public Path clear() {

		segments.clear();
		pathChanged();

		return this;

	}

	public String[] segments() {

		return segments.toArray(new String[]{});
	}

	public static Path of(String path) {

		String[] parsed_segments = path.split("/");

		return new Path(
			Arrays
			.stream(parsed_segments)
			.filter(s -> !s.trim().isEmpty())
			.toArray(String[]::new)
		);

	}

	@NonNull
	@Override
	public String toString() {

		StringJoiner stringJoiner = new StringJoiner("/");

		for(String segment : segments)
			stringJoiner.add(segment);

		return stringJoiner.toString();

	}

}

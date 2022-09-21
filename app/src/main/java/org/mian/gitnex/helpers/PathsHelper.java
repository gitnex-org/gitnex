package org.mian.gitnex.helpers;

/**
 * @author opyale
 */
public class PathsHelper {

	public static String join(String... paths) {

		StringBuilder stringBuilder = new StringBuilder();

		for (String path : paths) {

			if (path != null && !path.isEmpty()) {

				if (!path.startsWith("/")) {

					stringBuilder.append("/");
				}

				if (path.endsWith("/")) {

					path = path.substring(0, path.lastIndexOf("/"));
				}

				stringBuilder.append(path);
			}
		}

		return stringBuilder.append("/").toString();
	}
}

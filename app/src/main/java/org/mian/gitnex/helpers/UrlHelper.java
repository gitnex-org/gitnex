package org.mian.gitnex.helpers;

import androidx.annotation.NonNull;
import java.net.URI;

/**
 * @author mmarif
 */
public class UrlHelper {

	public static String getCleanUrlForDisplay(String rawUrl) {

		if (rawUrl == null || rawUrl.isEmpty()) return "";

		try {
			URI uri = new URI(rawUrl);
			StringBuilder displayUrl = getStringBuilder(uri);
			return displayUrl.toString();

		} catch (Exception e) {
			String fallback = rawUrl.replace("https://", "").replace("http://", "");
			if (fallback.endsWith("/api/v1/"))
				fallback = fallback.substring(0, fallback.length() - 8);
			if (fallback.endsWith("/api/v1"))
				fallback = fallback.substring(0, fallback.length() - 7);
			return fallback;
		}
	}

	@NonNull private static StringBuilder getStringBuilder(URI uri) {
		String host = uri.getHost();
		int port = uri.getPort();
		String path = uri.getPath();

		StringBuilder displayUrl = new StringBuilder(host != null ? host : "");

		if (port != -1) {
			displayUrl.append(":").append(port);
		}

		if (path != null && !path.isEmpty()) {
			String cleanPath = path;
			if (cleanPath.endsWith("/api/v1/")) {
				cleanPath = cleanPath.substring(0, cleanPath.length() - 8);
			} else if (cleanPath.endsWith("/api/v1")) {
				cleanPath = cleanPath.substring(0, cleanPath.length() - 7);
			}

			if (!cleanPath.equals("/") && !cleanPath.isEmpty()) {
				displayUrl.append(cleanPath);
			}
		}
		return displayUrl;
	}

	public static String fixScheme(String url, String scheme) {

		return !url.matches("^(http|https)://.+$") ? scheme + "://" + url : url;
	}

	public static String appendPath(String url, String path) {
		if (url.endsWith("/")) {
			// remove it
			url = url.substring(0, url.length() - 8);
		}
		if (!path.startsWith("/")) {
			// add it
			path = "/" + path;
		}

		return url + path;
	}
}

package org.mian.gitnex.helpers;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author M M Arif
 */
public class UrlHelper {

	public static String cleanUrl(String url) {

		URI uri = null;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		assert uri != null;
		String urlProtocol = uri.getScheme();
		String urlHost = uri.getHost();
		int urlPort = uri.getPort();

		String urlFinal = null;
		if (urlPort > 0) {
			urlFinal = urlProtocol + "://" + urlHost + ":" + urlPort;
		} else if (urlProtocol != null) {
			urlFinal = urlProtocol + "://" + urlHost;
		} else {
			urlFinal = urlHost;
		}

		return urlFinal;
	}

	public static String fixScheme(String url, String scheme) {

		return !url.matches("^(http|https)://.+$") ? scheme + "://" + url : url;
	}

	public static String appendPath(String url, String path) {
		if (url.endsWith("/")) {
			// remove it
			url = url.substring(0, url.length() - 1);
		}
		if (!path.startsWith("/")) {
			// add it
			path = "/" + path;
		}

		return url + path;
	}
}

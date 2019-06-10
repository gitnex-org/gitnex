package org.mian.gitnex.helpers;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Author M M Arif
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
        if(urlPort > 0) {
            urlFinal = urlProtocol + "://" + urlHost + ":" + urlPort;
        }
        else if(urlProtocol != null) {
            urlFinal = urlProtocol + "://" + urlHost;
        }
        else {
            urlFinal = urlHost;
        }

        return urlFinal;

    }

}


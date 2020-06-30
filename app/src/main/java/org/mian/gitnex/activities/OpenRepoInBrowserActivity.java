package org.mian.gitnex.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.PathsHelper;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.util.TinyDB;
import java.net.URI;
import java.net.URISyntaxException;
import io.mikael.urlbuilder.UrlBuilder;

/**
 * Author M M Arif
 */

public class OpenRepoInBrowserActivity extends AppCompatActivity {

    private Context appCtx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

	    super.onCreate(savedInstanceState);
	    appCtx = getApplicationContext();
	    TinyDB tinyDb = new TinyDB(appCtx);

	    try {

		    URI instanceUrl = new URI(tinyDb.getString("instanceUrlWithProtocol"));

		    String browserPath = PathsHelper.join(instanceUrl.getPath(), getIntent().getStringExtra("repoFullNameBrowser"));

		    String browserUrl = UrlBuilder.fromUri(instanceUrl)
			    .withPath(browserPath)
			    .toString();

		    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(browserUrl));
		    startActivity(i);
		    finish();

	    }
	    catch(URISyntaxException e) {
		    Toasty.error(appCtx, getString(R.string.genericError));
	    }

    }

}

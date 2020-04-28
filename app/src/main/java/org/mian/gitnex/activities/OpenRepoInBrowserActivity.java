package org.mian.gitnex.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import org.mian.gitnex.util.TinyDB;

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
        String instanceUrlWithProtocol = "https://" + tinyDb.getString("instanceUrlRaw");
        if (!tinyDb.getString("instanceUrlWithProtocol").isEmpty()) {
            instanceUrlWithProtocol = tinyDb.getString("instanceUrlWithProtocol");
        }

        String repoFullNameBrowser = getIntent().getStringExtra("repoFullNameBrowser");
        Uri url = Uri.parse(instanceUrlWithProtocol + "/" + repoFullNameBrowser);
        Intent i = new Intent(Intent.ACTION_VIEW, url);
        startActivity(i);
        finish();

    }

}

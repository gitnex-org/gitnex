package org.mian.gitnex.activities;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Author 6543
 */

import android.view.View;
import org.mian.gitnex.util.TinyDB;
import android.content.Intent;
import android.net.Uri;


public class OpenWebRepoActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    public void onClick(View v) {
        TinyDB tinyDb = new TinyDB(getApplicationContext());
        String repoFullName = tinyDb.getString("repoFullName");
        String instanceUrl = tinyDb.getString("instanceUrl");

        String url = instanceUrl + "/" + repoFullName;
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);

    }
}
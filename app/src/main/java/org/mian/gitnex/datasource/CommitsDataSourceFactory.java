package org.mian.gitnex.datasource;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.PageKeyedDataSource;
import org.mian.gitnex.models.Commits;

/**
 * Author M M Arif
 */

public class CommitsDataSourceFactory extends DataSource.Factory {

    private Context ctx;
    private String instanceUrl;
    private String instanceToken;
    private String repoOwner;
    private String repoName;
    private int listLimit;

    public CommitsDataSourceFactory(Context ctx, String instanceUrl, String instanceToken, String repoOwner, String repoName, int listLimit) {

        this.ctx = ctx;
        this.instanceUrl = instanceUrl;
        this.instanceToken = instanceToken;
        this.repoOwner = repoOwner;
        this.repoName = repoName;
        this.listLimit = listLimit;

    }

    private MutableLiveData<PageKeyedDataSource<Integer, Commits>> itemLiveDataSource = new MutableLiveData<>();

    @NonNull
    @Override
    public DataSource<Integer, Commits> create() {

        CommitsDataSource itemDataSource = new CommitsDataSource(ctx, instanceUrl, instanceToken, repoOwner, repoName);
        itemLiveDataSource.postValue(itemDataSource);
        return itemDataSource;

    }

    public MutableLiveData<PageKeyedDataSource<Integer, Commits>> getItemLiveDataSource() {

        return itemLiveDataSource;

    }

}

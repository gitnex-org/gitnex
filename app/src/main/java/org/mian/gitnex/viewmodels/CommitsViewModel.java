package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PageKeyedDataSource;
import androidx.paging.PagedList;
import org.mian.gitnex.datasource.CommitsDataSource;
import org.mian.gitnex.datasource.CommitsDataSourceFactory;
import org.mian.gitnex.models.Commits;
import java.util.Objects;

/**
 * Author M M Arif
 */

public class CommitsViewModel extends ViewModel {

    private CommitsDataSourceFactory itemDataSourceFactory;
    public LiveData itemPagedList;

    public CommitsViewModel(Context ctx, String instannceUrl, String instanceToken, String owner, String repo, int listLimit) {

        itemDataSourceFactory = new CommitsDataSourceFactory(ctx, instannceUrl, instanceToken, owner, repo, listLimit);
        LiveData<PageKeyedDataSource<Integer, Commits>> liveDataSource = itemDataSourceFactory.getItemLiveDataSource();
        PagedList.Config pagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(true)
                        .setPageSize(listLimit).build();
        //noinspection unchecked
        itemPagedList = new LivePagedListBuilder(itemDataSourceFactory, pagedListConfig)
                .build();

    }

    public void refresh() {

        Objects.requireNonNull(itemDataSourceFactory.getItemLiveDataSource().getValue()).invalidate();

    }

}

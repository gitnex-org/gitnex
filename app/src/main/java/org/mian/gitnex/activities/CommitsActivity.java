package org.mian.gitnex.activities;

import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.mikepenz.fastadapter.IItemAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.listeners.ItemFilterListener;
import com.mikepenz.fastadapter_extensions.items.ProgressItem;
import com.mikepenz.fastadapter_extensions.scroll.EndlessRecyclerOnScrollListener;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.items.CommitsItems;
import org.mian.gitnex.models.Commits;
import org.mian.gitnex.util.TinyDB;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import static com.mikepenz.fastadapter.adapters.ItemAdapter.items;

/**
 * Author M M Arif
 */

public class CommitsActivity extends BaseActivity implements ItemFilterListener<CommitsItems> {

    private View.OnClickListener onClickListener;
    private TextView noData;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String TAG = "CommitsActivity - ";
    private int resultLimit = 50;
    private boolean loadNextFlag = false;

    private List<CommitsItems> items = new ArrayList<>();
    private FastItemAdapter<CommitsItems> fastItemAdapter;
    private ItemAdapter footerAdapter;
    private EndlessRecyclerOnScrollListener endlessRecyclerOnScrollListener;

    @Override
    protected int getLayoutResourceId(){
        return R.layout.activity_commits;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TinyDB tinyDb = new TinyDB(getApplicationContext());
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];

        String branchName = getIntent().getStringExtra("branchName");

        TextView toolbar_title = findViewById(R.id.toolbar_title);
        toolbar_title.setMovementMethod(new ScrollingMovementMethod());
        toolbar_title.setText(branchName);

        ImageView closeActivity = findViewById(R.id.close);
        noData = findViewById(R.id.noDataCommits);
        progressBar = findViewById(R.id.progress_bar);
        swipeRefreshLayout = findViewById(R.id.pullToRefresh);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setHasFixedSize(true);

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

        fastItemAdapter = new FastItemAdapter<>();
        fastItemAdapter.withSelectable(true);

        footerAdapter = items();
        //noinspection unchecked
        fastItemAdapter.addAdapter(1, footerAdapter);

        fastItemAdapter.getItemFilter().withFilterPredicate((IItemAdapter.Predicate<CommitsItems>) (item, constraint) -> item.getCommitTitle().toLowerCase().contains(Objects.requireNonNull(constraint).toString().toLowerCase()));

        fastItemAdapter.getItemFilter().withItemFilterListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(fastItemAdapter);

        endlessRecyclerOnScrollListener = new EndlessRecyclerOnScrollListener(footerAdapter) {

            @Override
            public void onLoadMore(final int currentPage) {

                loadNext(instanceUrl, instanceToken, repoOwner, repoName, currentPage, branchName);

            }

        };

        swipeRefreshLayout.setOnRefreshListener(() -> {

            progressBar.setVisibility(View.VISIBLE);
            fastItemAdapter.clear();
            endlessRecyclerOnScrollListener.resetPageCount();
            swipeRefreshLayout.setRefreshing(false);

        });

        recyclerView.addOnScrollListener(endlessRecyclerOnScrollListener);

        loadInitial(instanceUrl, instanceToken, repoOwner, repoName, branchName);

        assert savedInstanceState != null;
        fastItemAdapter.withSavedInstanceState(savedInstanceState);

    }

    private void loadInitial(String instanceUrl, String token, String repoOwner, String repoName, String branchName) {

        Call<List<Commits>> call = RetrofitClient
                .getInstance(instanceUrl, getApplicationContext())
                .getApiInterface()
                .getRepositoryCommits(token, repoOwner, repoName,  1, branchName);

        call.enqueue(new Callback<List<Commits>>() {

            @Override
            public void onResponse(@NonNull Call<List<Commits>> call, @NonNull Response<List<Commits>> response) {

                if (response.isSuccessful()) {

                    assert response.body() != null;

                    if(response.body().size() > 0) {

                        if(response.body().size() == resultLimit) {
                            loadNextFlag = true;
                        }

                        for (int i = 0; i < response.body().size(); i++) {

                            items.add(new CommitsItems(getApplicationContext()).withNewItems(response.body().get(i).getCommit().getMessage(), response.body().get(i).getHtml_url(),
                                    response.body().get(i).getCommit().getCommitter().getName(), response.body().get(i).getCommit().getCommitter().getDate()));

                        }

                        fastItemAdapter.add(items);

                    }
                    else {

                        noData.setVisibility(View.VISIBLE);

                    }

                    progressBar.setVisibility(View.GONE);

                }
                else {

                    Log.e(TAG, String.valueOf(response.code()));

                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Commits>> call, @NonNull Throwable t) {

                Log.e(TAG, t.toString());

            }

        });

    }

    private void loadNext(String instanceUrl, String token, String repoOwner, String repoName, final int currentPage, String branchName) {

        footerAdapter.clear();
        //noinspection unchecked
        footerAdapter.add(new ProgressItem().withEnabled(false));
        Handler handler = new Handler();

        handler.postDelayed(() -> {

            Call<List<Commits>> call = RetrofitClient
                    .getInstance(instanceUrl, getApplicationContext())
                    .getApiInterface()
                    .getRepositoryCommits(token, repoOwner, repoName, currentPage + 1, branchName);

            call.enqueue(new Callback<List<Commits>>() {

                @Override
                public void onResponse(@NonNull Call<List<Commits>> call, @NonNull Response<List<Commits>> response) {

                    if (response.isSuccessful()) {

                        assert response.body() != null;

                        if (response.body().size() > 0) {

                            loadNextFlag = response.body().size() == resultLimit;

                            for (int i = 0; i < response.body().size(); i++) {

                                fastItemAdapter.add(fastItemAdapter.getAdapterItemCount(), new CommitsItems(getApplicationContext()).withNewItems(response.body().get(i).getCommit().getMessage(),
                                        response.body().get(i).getHtml_url(), response.body().get(i).getCommit().getCommitter().getName(),
                                        response.body().get(i).getCommit().getCommitter().getDate()));

                            }

                            footerAdapter.clear();

                        }
                        else {

                            footerAdapter.clear();
                        }

                        progressBar.setVisibility(View.GONE);

                    }
                    else {

                        Log.e(TAG, String.valueOf(response.code()));

                    }

                }

                @Override
                public void onFailure(@NonNull Call<List<Commits>> call, @NonNull Throwable t) {

                    Log.e(TAG, t.toString());

                }

            });

        }, 1000);

        if(!loadNextFlag) {

            footerAdapter.clear();

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                fastItemAdapter.filter(newText);
                return true;
            }

        });

        endlessRecyclerOnScrollListener.enable();
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public void itemsFiltered(@Nullable CharSequence constraint, @Nullable List<CommitsItems> results) {
        endlessRecyclerOnScrollListener.disable();
    }

    @Override
    public void onReset() {
        endlessRecyclerOnScrollListener.enable();
    }

    private void initCloseListener() {
        onClickListener = view -> {
            getIntent().removeExtra("branchName");
            finish();
        };
    }

}



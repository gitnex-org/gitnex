package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import static com.mikepenz.fastadapter.adapters.ItemAdapter.items;

/**
 * Author M M Arif
 */

public class CommitsFragment extends Fragment implements ItemFilterListener<CommitsItems> {

    private TextView noData;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String TAG = "CommitsFragment - ";
    private int resultLimit = 50;
    private boolean loadNextFlag = false;

    private List<CommitsItems> items = new ArrayList<>();
    private FastItemAdapter<CommitsItems> fastItemAdapter;
    private ItemAdapter footerAdapter;
    private EndlessRecyclerOnScrollListener endlessRecyclerOnScrollListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_commits, container, false);
        setHasOptionsMenu(true);

        TinyDB tinyDb = new TinyDB(getContext());
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];

        noData = v.findViewById(R.id.noDataCommits);
        progressBar = v.findViewById(R.id.progress_bar);
        swipeRefreshLayout = v.findViewById(R.id.pullToRefresh);

        RecyclerView recyclerView = v.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        fastItemAdapter = new FastItemAdapter<>();
        fastItemAdapter.withSelectable(true);

        footerAdapter = items();
        //noinspection unchecked
        fastItemAdapter.addAdapter(1, footerAdapter);

        fastItemAdapter.getItemFilter().withFilterPredicate(new IItemAdapter.Predicate<CommitsItems>() {

            @Override
            public boolean filter(@NonNull CommitsItems item, CharSequence constraint) {

                return item.getCommitTitle().toString().toLowerCase().contains(constraint.toString().toLowerCase());

            }

        });

        fastItemAdapter.getItemFilter().withItemFilterListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(fastItemAdapter);

        endlessRecyclerOnScrollListener = new EndlessRecyclerOnScrollListener(footerAdapter) {

            @Override
            public void onLoadMore(final int currentPage) {

                loadNext(instanceUrl, instanceToken, repoOwner, repoName, currentPage);

            }

        };

        swipeRefreshLayout.setOnRefreshListener(() -> {

            progressBar.setVisibility(View.VISIBLE);
            fastItemAdapter.clear();
            endlessRecyclerOnScrollListener.resetPageCount();
            swipeRefreshLayout.setRefreshing(false);

        });

        recyclerView.addOnScrollListener(endlessRecyclerOnScrollListener);

        loadInitial(instanceUrl, instanceToken, repoOwner, repoName);

        assert savedInstanceState != null;
        fastItemAdapter.withSavedInstanceState(savedInstanceState);

        return v;

    }

    private void loadInitial(String instanceUrl, String token, String repoOwner, String repoName) {

        Call<List<Commits>> call = RetrofitClient
                .getInstance(instanceUrl, getContext())
                .getApiInterface()
                .getRepositoryCommits(token, repoOwner, repoName,  1);

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

                            items.add(new CommitsItems(getContext()).withNewItems(response.body().get(i).getCommit().getMessage(), response.body().get(i).getHtml_url(),
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

    private void loadNext(String instanceUrl, String token, String repoOwner, String repoName, final int currentPage) {

        footerAdapter.clear();
        //noinspection unchecked
        footerAdapter.add(new ProgressItem().withEnabled(false));
        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {

            @Override
            public void run() {

                Call<List<Commits>> call = RetrofitClient
                        .getInstance(instanceUrl, getContext())
                        .getApiInterface()
                        .getRepositoryCommits(token, repoOwner, repoName, currentPage + 1);

                call.enqueue(new Callback<List<Commits>>() {

                    @Override
                    public void onResponse(@NonNull Call<List<Commits>> call, @NonNull Response<List<Commits>> response) {

                        if (response.isSuccessful()) {

                            assert response.body() != null;

                            if (response.body().size() > 0) {

                                loadNextFlag = response.body().size() == resultLimit;

                                for (int i = 0; i < response.body().size(); i++) {

                                    fastItemAdapter.add(fastItemAdapter.getAdapterItemCount(), new CommitsItems(getContext()).withNewItems(response.body().get(i).getCommit().getMessage(),
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

            }

        }, 1000);

        if(!loadNextFlag) {

            footerAdapter.clear();

        }

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        inflater.inflate(R.menu.search_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

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

    }

    @Override
    public void itemsFiltered(@Nullable CharSequence constraint, @Nullable List<CommitsItems> results) {
        endlessRecyclerOnScrollListener.disable();
    }

    @Override
    public void onReset() {
        endlessRecyclerOnScrollListener.enable();
    }

}

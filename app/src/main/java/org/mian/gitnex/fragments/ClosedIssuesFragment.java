package org.mian.gitnex.fragments;

import android.content.Context;
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
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.ClosedIssuesAdapter;
import org.mian.gitnex.clients.IssuesService;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.interfaces.ApiInterface;
import org.mian.gitnex.models.Issues;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class ClosedIssuesFragment extends Fragment {

    private ProgressBar mProgressBarClosed;
    private RecyclerView recyclerViewClosed;
    private List<Issues> issuesListClosed;
    private ClosedIssuesAdapter adapterClosed;
    private ApiInterface apiClosed;
    private String TAG = "closedIssuesListFragment - ";
    private Context context;
    private int pageSize = 1;
    private TextView noDataIssuesClosed;
    private String issueState = "closed";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_issues_closed, container, false);
        setHasOptionsMenu(true);

        TinyDB tinyDb = new TinyDB(getContext());
        String repoFullName = tinyDb.getString("repoFullName");
        //Log.i("repoFullName", tinyDb.getString("repoFullName"));
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        final SwipeRefreshLayout swipeRefresh = v.findViewById(R.id.pullToRefreshClosed);

        context = getContext();
        recyclerViewClosed = v.findViewById(R.id.recyclerViewClosed);
        issuesListClosed = new ArrayList<>();

        mProgressBarClosed = v.findViewById(R.id.progress_barClosed);
        noDataIssuesClosed = v.findViewById(R.id.noDataIssuesClosed);

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        swipeRefresh.setRefreshing(false);
                        loadInitial(instanceToken, repoOwner, repoName, issueState);
                        adapterClosed.notifyDataChanged();

                    }
                }, 200);
            }
        });

        adapterClosed = new ClosedIssuesAdapter(getContext(), issuesListClosed);
        adapterClosed.setLoadMoreListener(new ClosedIssuesAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {

                recyclerViewClosed.post(new Runnable() {
                    @Override
                    public void run() {
                        if(issuesListClosed.size() == 10 || pageSize == 10) {

                            int page = (issuesListClosed.size() + 10) / 10;
                            loadMore(Authorization.returnAuthentication(getContext(), loginUid, instanceToken), repoOwner, repoName, page, issueState);

                        }
                        /*else {

                            Toasty.info(context, getString(R.string.noMoreData));

                        }*/
                    }
                });

            }
        });

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerViewClosed.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerViewClosed.setHasFixedSize(true);
        recyclerViewClosed.addItemDecoration(dividerItemDecoration);
        recyclerViewClosed.setLayoutManager(new LinearLayoutManager(context));
        recyclerViewClosed.setAdapter(adapterClosed);

        apiClosed = IssuesService.createService(ApiInterface.class, instanceUrl, getContext());
        loadInitial(Authorization.returnAuthentication(getContext(), loginUid, instanceToken), repoOwner, repoName, issueState);

        return v;

    }

    @Override
    public void onResume() {

        super.onResume();
        TinyDB tinyDb = new TinyDB(getContext());
        final String loginUid = tinyDb.getString("loginUid");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        if(tinyDb.getBoolean("resumeClosedIssues")) {

            loadInitial(Authorization.returnAuthentication(getContext(), loginUid, instanceToken), repoOwner, repoName, issueState);
            tinyDb.putBoolean("resumeClosedIssues", false);

        }

    }

    private void loadInitial(String token, String repoOwner, String repoName, String issueState) {

        Call<List<Issues>> call = apiClosed.getClosedIssues(token, repoOwner, repoName,  1, issueState);

        call.enqueue(new Callback<List<Issues>>() {

            @Override
            public void onResponse(@NonNull Call<List<Issues>> call, @NonNull Response<List<Issues>> response) {

                if(response.isSuccessful()) {

                    assert response.body() != null;
                    if(response.body().size() > 0) {

                        issuesListClosed.clear();
                        issuesListClosed.addAll(response.body());
                        adapterClosed.notifyDataChanged();
                        noDataIssuesClosed.setVisibility(View.GONE);

                    }
                    else {
                        issuesListClosed.clear();
                        adapterClosed.notifyDataChanged();
                        noDataIssuesClosed.setVisibility(View.VISIBLE);
                    }
                    mProgressBarClosed.setVisibility(View.GONE);
                }
                else {
                    Log.e(TAG, String.valueOf(response.code()));
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Issues>> call, @NonNull Throwable t) {
                Log.e(TAG, t.toString());
            }

        });

    }

    private void loadMore(String token, String repoOwner, String repoName, int page, String issueState){

        //add loading progress view
        issuesListClosed.add(new Issues("load"));
        adapterClosed.notifyItemInserted((issuesListClosed.size() - 1));

        Call<List<Issues>> call = apiClosed.getClosedIssues(token, repoOwner, repoName, page, issueState);

        call.enqueue(new Callback<List<Issues>>() {

            @Override
            public void onResponse(@NonNull Call<List<Issues>> call, @NonNull Response<List<Issues>> response) {

                if(response.isSuccessful()){

                    //remove loading view
                    issuesListClosed.remove(issuesListClosed.size()-1);

                    List<Issues> result = response.body();

                    assert result != null;
                    if(result.size() > 0) {

                        pageSize = result.size();
                        issuesListClosed.addAll(result);

                    }
                    else {

                        Toasty.info(context, getString(R.string.noMoreData));
                        adapterClosed.setMoreDataAvailable(false);

                    }

                    adapterClosed.notifyDataChanged();

                }
                else {

                    Log.e(TAG, String.valueOf(response.code()));

                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Issues>> call, @NonNull Throwable t) {

                Log.e(TAG, t.toString());

            }

        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        boolean connToInternet = AppUtil.haveNetworkConnection(Objects.requireNonNull(getContext()));

        inflater.inflate(R.menu.search_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        //searchView.setQueryHint(getContext().getString(R.string.strFilter));

        /*if(!connToInternet) {
            return;
        }*/

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                adapterClosed.getFilter().filter(newText);
                return false;

            }

        });

    }

}

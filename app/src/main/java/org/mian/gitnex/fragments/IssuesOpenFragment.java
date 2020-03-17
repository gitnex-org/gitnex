package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
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
import org.mian.gitnex.adapters.IssuesAdapter;
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

/**
 * Author M M Arif
 */

public class IssuesOpenFragment extends Fragment {

    private ProgressBar mProgressBar;
    private RecyclerView recyclerView;
    private List<Issues> issuesList;
    private IssuesAdapter adapter;
    private ApiInterface api;
    private String TAG = "IssuesListFragment - ";
    private Context context;
    private int pageSize = 1;
    private TextView noDataIssues;
    private int resultLimit = 50;
    private String requestType = "issues" ;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_issues, container, false);
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

        final SwipeRefreshLayout swipeRefresh = v.findViewById(R.id.pullToRefresh);

        context = getContext();
        recyclerView = v.findViewById(R.id.recyclerView);
        issuesList = new ArrayList<>();

        mProgressBar = v.findViewById(R.id.progress_bar);
        noDataIssues = v.findViewById(R.id.noDataIssues);

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        swipeRefresh.setRefreshing(false);
                        loadInitial(instanceToken, repoOwner, repoName, resultLimit, requestType);
                        adapter.notifyDataChanged();

                    }
                }, 200);
            }
        });

        adapter = new IssuesAdapter(getContext(), issuesList);
        adapter.setLoadMoreListener(new IssuesAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {

                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        if(issuesList.size() == 10 || pageSize == 10) {

                            int page = (issuesList.size() + 10) / 10;
                            loadMore(Authorization.returnAuthentication(getContext(), loginUid, instanceToken), repoOwner, repoName, page, resultLimit, requestType);

                        }
                        /*else {

                            Toasty.info(context, getString(R.string.noMoreData));

                        }*/
                    }
                });

            }
        });

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        api = IssuesService.createService(ApiInterface.class, instanceUrl, getContext());
        loadInitial(Authorization.returnAuthentication(getContext(), loginUid, instanceToken), repoOwner, repoName, resultLimit, requestType);

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

        if(tinyDb.getBoolean("resumeIssues")) {

            loadInitial(Authorization.returnAuthentication(getContext(), loginUid, instanceToken), repoOwner, repoName, resultLimit, requestType);
            tinyDb.putBoolean("resumeIssues", false);

        }

    }

    private void loadInitial(String token, String repoOwner, String repoName, int resultLimit, String requestType) {

        Call<List<Issues>> call = api.getIssues(token, repoOwner, repoName,  1, resultLimit, requestType);

        call.enqueue(new Callback<List<Issues>>() {

            @Override
            public void onResponse(@NonNull Call<List<Issues>> call, @NonNull Response<List<Issues>> response) {

                if(response.isSuccessful()) {

                    assert response.body() != null;
                    if(response.body().size() > 0) {

                        issuesList.clear();
                        issuesList.addAll(response.body());
                        adapter.notifyDataChanged();
                        noDataIssues.setVisibility(View.GONE);

                    }
                    else {
                        issuesList.clear();
                        adapter.notifyDataChanged();
                        noDataIssues.setVisibility(View.VISIBLE);
                    }
                    mProgressBar.setVisibility(View.GONE);
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

    private void loadMore(String token, String repoOwner, String repoName, int page, int resultLimit, String requestType){

        //add loading progress view
        issuesList.add(new Issues("load"));
        adapter.notifyItemInserted((issuesList.size() - 1));

        Call<List<Issues>> call = api.getIssues(token, repoOwner, repoName, page, resultLimit, requestType);

        call.enqueue(new Callback<List<Issues>>() {

            @Override
            public void onResponse(@NonNull Call<List<Issues>> call, @NonNull Response<List<Issues>> response) {

                if(response.isSuccessful()){

                    //remove loading view
                    issuesList.remove(issuesList.size()-1);

                    List<Issues> result = response.body();

                    assert result != null;
                    if(result.size() > 0) {

                        pageSize = result.size();
                        issuesList.addAll(result);

                    }
                    else {

                        Toasty.info(context, getString(R.string.noMoreData));
                        adapter.setMoreDataAvailable(false);

                    }

                    adapter.notifyDataChanged();

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

                adapter.getFilter().filter(newText);
                return false;

            }

        });

    }

}

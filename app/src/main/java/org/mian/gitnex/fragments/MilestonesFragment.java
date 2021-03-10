package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.gitnex.tea4j.models.Milestones;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.adapters.MilestonesAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentMilestonesBinding;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.StaticGlobalVariables;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Version;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class MilestonesFragment extends Fragment {

    private FragmentMilestonesBinding viewBinding;

    private Menu menu;
    private List<Milestones> dataList;
    private MilestonesAdapter adapter;
    private Context ctx;
    private int pageSize = StaticGlobalVariables.milestonesPageInit;
    private String TAG = StaticGlobalVariables.tagMilestonesFragment;
    private int resultLimit = StaticGlobalVariables.resultLimitOldGiteaInstances;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        viewBinding = FragmentMilestonesBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);
        ctx = getContext();

        TinyDB tinyDb = TinyDB.getInstance(getContext());
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        viewBinding.recyclerView.setHasFixedSize(true);
        viewBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // if gitea is 1.12 or higher use the new limit
        if(new Version(tinyDb.getString("giteaVersion")).higherOrEqual("1.12.0")) {
            resultLimit = StaticGlobalVariables.resultLimitNewGiteaInstances;
        }

        dataList = new ArrayList<>();
        adapter = new MilestonesAdapter(ctx, dataList);

	    if(new Version(tinyDb.getString("giteaVersion")).higherOrEqual("1.12.0")) {

		    adapter.setLoadMoreListener(() -> viewBinding.recyclerView.post(() -> {

			    if(dataList.size() == resultLimit || pageSize == resultLimit) {

				    int page = (dataList.size() + resultLimit) / resultLimit;
				    loadMore(Authorization.get(getContext()), repoOwner, repoName, page, resultLimit, tinyDb.getString("milestoneState"));

			    }

		    }));

	    }

	    DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(viewBinding.recyclerView.getContext(), DividerItemDecoration.VERTICAL);
	    viewBinding.recyclerView.addItemDecoration(dividerItemDecoration);
        viewBinding.recyclerView.setHasFixedSize(true);
        viewBinding.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
        viewBinding.recyclerView.setAdapter(adapter);

        viewBinding.pullToRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {

            dataList.clear();
            viewBinding.pullToRefresh.setRefreshing(false);
            loadInitial(Authorization.get(getContext()), repoOwner, repoName, resultLimit, tinyDb.getString("milestoneState"));
            adapter.updateList(dataList);

        }, 50));

        ((RepoDetailActivity) requireActivity()).setFragmentRefreshListenerMilestone(milestoneState -> {

            if(milestoneState.equals("closed")) {
                menu.getItem(1).setIcon(R.drawable.ic_filter_closed);
            }
            else {
                menu.getItem(1).setIcon(R.drawable.ic_filter);
            }

            dataList.clear();

            adapter = new MilestonesAdapter(ctx, dataList);

	        if(new Version(tinyDb.getString("giteaVersion")).higherOrEqual("1.12.0")) {

		        adapter.setLoadMoreListener(() -> viewBinding.recyclerView.post(() -> {

			        if(dataList.size() == resultLimit || pageSize == resultLimit) {

				        int page = (dataList.size() + resultLimit) / resultLimit;
				        loadMore(Authorization.get(getContext()), repoOwner, repoName, page, resultLimit, milestoneState);

			        }

		        }));

	        }

            tinyDb.putString("milestoneState", milestoneState);

            viewBinding.progressBar.setVisibility(View.VISIBLE);
            viewBinding.noDataMilestone.setVisibility(View.GONE);

            loadInitial(Authorization.get(getContext()), repoOwner, repoName, resultLimit, milestoneState);
            viewBinding.recyclerView.setAdapter(adapter);

        });

        loadInitial(Authorization.get(getContext()), repoOwner, repoName, resultLimit, tinyDb.getString("milestoneState"));

        return viewBinding.getRoot();

    }

    @Override
    public void onResume() {

        super.onResume();
        TinyDB tinyDb = TinyDB.getInstance(getContext());
        final String loginUid = tinyDb.getString("loginUid");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        if(tinyDb.getBoolean("milestoneCreated")) {

            loadInitial(Authorization.get(getContext()), repoOwner, repoName, resultLimit, tinyDb.getString("milestoneState"));
            tinyDb.putBoolean("milestoneCreated", false);

        }

    }

    private void loadInitial(String token, String repoOwner, String repoName, int resultLimit, String milestoneState) {

        Call<List<Milestones>> call = RetrofitClient.getApiInterface(ctx).getMilestones(token, repoOwner, repoName, 1, resultLimit, milestoneState);

        call.enqueue(new Callback<List<Milestones>>() {

            @Override
            public void onResponse(@NonNull Call<List<Milestones>> call, @NonNull Response<List<Milestones>> response) {

                if(response.code() == 200) {

                    assert response.body() != null;
                    if(response.body().size() > 0) {

                        dataList.clear();
                        dataList.addAll(response.body());
                        adapter.notifyDataChanged();
                        viewBinding.noDataMilestone.setVisibility(View.GONE);

                    }
                    else {

                        dataList.clear();
                        adapter.notifyDataChanged();
                        viewBinding.noDataMilestone.setVisibility(View.VISIBLE);

                    }

                    viewBinding.progressBar.setVisibility(View.GONE);

                }
                else {
                    Log.e(TAG, String.valueOf(response.code()));
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Milestones>> call, @NonNull Throwable t) {

                Log.e(TAG, t.toString());
            }

        });

    }

    private void loadMore(String token, String repoOwner, String repoName, int page, int resultLimit, String milestoneState) {

    	viewBinding.progressLoadMore.setVisibility(View.VISIBLE);

        Call<List<Milestones>> call = RetrofitClient.getApiInterface(ctx).getMilestones(token, repoOwner, repoName, page, resultLimit, milestoneState);

        call.enqueue(new Callback<List<Milestones>>() {

            @Override
            public void onResponse(@NonNull Call<List<Milestones>> call, @NonNull Response<List<Milestones>> response) {

	            if(response.code() == 200) {

                    //remove loading view
                    dataList.remove(dataList.size() - 1);

                    List<Milestones> result = response.body();

                    assert result != null;
                    if(result.size() > 0) {

                        pageSize = result.size();
                        dataList.addAll(result);

                    }
                    else {

                        adapter.setMoreDataAvailable(false);

                    }

                    adapter.notifyDataChanged();
		            viewBinding.progressLoadMore.setVisibility(View.GONE);

                }
                else {

                    Log.e(TAG, String.valueOf(response.code()));

                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Milestones>> call, @NonNull Throwable t) {

                Log.e(TAG, t.toString());

            }

        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        this.menu = menu;
        inflater.inflate(R.menu.search_menu, menu);
        inflater.inflate(R.menu.filter_menu_milestone, menu);
        super.onCreateOptionsMenu(menu, inflater);

        TinyDB tinyDb = TinyDB.getInstance(ctx);

        if(tinyDb.getString("milestoneState").equals("closed")) {
            menu.getItem(1).setIcon(R.drawable.ic_filter_closed);
        }
        else {
            menu.getItem(1).setIcon(R.drawable.ic_filter);
        }

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

                filter(newText);
                return false;

            }

        });

    }

    private void filter(String text) {

        List<Milestones> arr = new ArrayList<>();

        for(Milestones d : dataList) {
	        if(d == null || d.getTitle() == null || d.getDescription() == null) {
		        continue;
	        }
            if(d.getTitle().toLowerCase().contains(text) || d.getDescription().toLowerCase().contains(text)) {
                arr.add(d);
            }
        }

        adapter.updateList(arr);
    }

}

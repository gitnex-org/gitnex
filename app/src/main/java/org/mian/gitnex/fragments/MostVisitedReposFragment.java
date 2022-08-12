package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.adapters.MostVisitedReposAdapter;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.RepositoriesApi;
import org.mian.gitnex.database.models.Repository;
import org.mian.gitnex.databinding.FragmentDraftsBinding;
import org.mian.gitnex.helpers.TinyDB;
import java.util.ArrayList;
import java.util.List;

/**
 * @author M M Arif
 */

public class MostVisitedReposFragment extends Fragment {

	private Context ctx;
	private MostVisitedReposAdapter adapter;
	private RecyclerView mRecyclerView;
	private RepositoriesApi repositoriesApi;
	private TextView noData;
	private List<Repository> mostVisitedReposList;
	private int currentActiveAccountId;
	private SwipeRefreshLayout swipeRefresh;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState) {

		FragmentDraftsBinding fragmentDraftsBinding = FragmentDraftsBinding.inflate(inflater, container, false);

		ctx = getContext();
		setHasOptionsMenu(true);

		((MainActivity) requireActivity()).setActionBarTitle(getResources().getString(R.string.navMostVisited));

		TinyDB tinyDb = TinyDB.getInstance(ctx);

		mostVisitedReposList = new ArrayList<>();
		repositoriesApi = BaseApi.getInstance(ctx, RepositoriesApi.class);

		noData = fragmentDraftsBinding.noData;
		mRecyclerView = fragmentDraftsBinding.recyclerView;
		swipeRefresh = fragmentDraftsBinding.pullToRefresh;

		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(ctx));

		adapter = new MostVisitedReposAdapter(mostVisitedReposList);
		currentActiveAccountId = tinyDb.getInt("currentActiveAccountId");
		swipeRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {

			mostVisitedReposList.clear();
			fetchDataAsync(currentActiveAccountId);
		}, 250));

		fetchDataAsync(currentActiveAccountId);

		return fragmentDraftsBinding.getRoot();
	}

	private void fetchDataAsync(int accountId) {

		repositoriesApi.fetchAllMostVisited(accountId).observe(getViewLifecycleOwner(), mostVisitedRepos -> {

			swipeRefresh.setRefreshing(false);
			assert mostVisitedRepos != null;
			if(mostVisitedRepos.size() > 0) {

				mostVisitedReposList.clear();
				noData.setVisibility(View.GONE);
				mostVisitedReposList.addAll(mostVisitedRepos);
				adapter.notifyDataChanged();
				mRecyclerView.setAdapter(adapter);
			}
			else {

				noData.setVisibility(View.VISIBLE);
			}
		});
	}
}

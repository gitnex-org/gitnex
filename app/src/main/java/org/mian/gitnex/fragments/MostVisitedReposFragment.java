package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.adapters.MostVisitedReposAdapter;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.RepositoriesApi;
import org.mian.gitnex.database.models.Repository;
import org.mian.gitnex.databinding.FragmentDraftsBinding;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

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

		adapter = new MostVisitedReposAdapter(ctx, mostVisitedReposList);
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

	public void resetAllRepositoryCounter(int accountId) {

		if(mostVisitedReposList.size() > 0) {

			Objects.requireNonNull(BaseApi.getInstance(ctx, RepositoriesApi.class)).resetAllRepositoryMostVisited(accountId);
			mostVisitedReposList.clear();
			adapter.notifyDataChanged();
			Toasty.success(ctx, getResources().getString(R.string.resetMostReposCounter));
		}
		else {
			Toasty.warning(ctx, getResources().getString(R.string.noDataFound));
		}
	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

		inflater.inflate(R.menu.reset_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if(item.getItemId() == R.id.reset_menu_item) {

			if(mostVisitedReposList.size() == 0) {
				Toasty.warning(ctx, getResources().getString(R.string.noDataFound));
			}
			else {
				new MaterialAlertDialogBuilder(ctx).setTitle(R.string.reset).setMessage(R.string.resetCounterAllDialogMessage).setPositiveButton(R.string.reset, (dialog, which) -> {

					resetAllRepositoryCounter(currentActiveAccountId);
					dialog.dismiss();
				}).setNeutralButton(R.string.cancelButton, null).show();
			}
		}

		return super.onOptionsItemSelected(item);
	}

}

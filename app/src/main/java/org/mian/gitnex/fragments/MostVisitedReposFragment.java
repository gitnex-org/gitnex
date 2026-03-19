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
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.List;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.MostVisitedReposAdapter;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.RepositoriesApi;
import org.mian.gitnex.database.models.Repository;
import org.mian.gitnex.databinding.FragmentMostVisitedBinding;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;

/**
 * @author mmarif
 */
public class MostVisitedReposFragment extends Fragment {

	private FragmentMostVisitedBinding binding;
	private Context ctx;
	private MostVisitedReposAdapter adapter;
	private RepositoriesApi repositoriesApi;
	private final List<Repository> mostVisitedReposList = new ArrayList<>();
	private int currentActiveAccountId;

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		binding = FragmentMostVisitedBinding.inflate(inflater, container, false);
		ctx = requireContext();

		setupRecyclerView();
		setupRefreshLayout();
		setupMenu();

		currentActiveAccountId = TinyDB.getInstance(ctx).getInt("currentActiveAccountId");
		repositoriesApi = BaseApi.getInstance(ctx, RepositoriesApi.class);

		fetchDataAsync(currentActiveAccountId);

		return binding.getRoot();
	}

	private void setupRecyclerView() {
		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
		adapter = new MostVisitedReposAdapter(ctx, mostVisitedReposList);
		binding.recyclerView.setAdapter(adapter);
	}

	private void setupRefreshLayout() {
		binding.pullToRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(() -> fetchDataAsync(currentActiveAccountId), 250));
	}

	private void fetchDataAsync(int accountId) {
		if (repositoriesApi == null) return;

		binding.expressiveLoader.setVisibility(View.VISIBLE);
		binding.layoutEmpty.getRoot().setVisibility(View.GONE);

		repositoriesApi
				.fetchAllMostVisited(accountId)
				.observe(
						getViewLifecycleOwner(),
						repos -> {
							binding.pullToRefresh.setRefreshing(false);
							binding.expressiveLoader.setVisibility(View.GONE);

							if (repos != null && !repos.isEmpty()) {
								binding.layoutEmpty.getRoot().setVisibility(View.GONE);
								binding.recyclerView.setVisibility(View.VISIBLE);

								mostVisitedReposList.clear();
								mostVisitedReposList.addAll(repos);
								adapter.updateList(new ArrayList<>(mostVisitedReposList));
							} else {
								mostVisitedReposList.clear();
								adapter.updateList(new ArrayList<>());

								binding.recyclerView.setVisibility(View.GONE);
								binding.layoutEmpty.getRoot().setVisibility(View.VISIBLE);
							}
						});
	}

	private void setupMenu() {
		requireActivity()
				.addMenuProvider(
						new MenuProvider() {
							@Override
							public void onCreateMenu(
									@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
								menuInflater.inflate(R.menu.reset_menu, menu);
								menuInflater.inflate(R.menu.search_menu, menu);

								MenuItem searchItem = menu.findItem(R.id.action_search);
								SearchView searchView = (SearchView) searchItem.getActionView();
								if (searchView != null) {
									searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
									searchView.setOnQueryTextListener(
											new SearchView.OnQueryTextListener() {
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
							}

							@Override
							public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
								if (menuItem.getItemId() == R.id.reset_menu_item) {
									handleResetAction();
									return true;
								}
								return false;
							}
						},
						getViewLifecycleOwner(),
						Lifecycle.State.RESUMED);
	}

	private void handleResetAction() {
		if (mostVisitedReposList.isEmpty()) {
			Toasty.show(ctx, getString(R.string.noDataFound));
			return;
		}

		new MaterialAlertDialogBuilder(ctx)
				.setTitle(R.string.reset)
				.setMessage(R.string.resetCounterAllDialogMessage)
				.setPositiveButton(R.string.reset, (dialog, which) -> resetAllCounters())
				.setNeutralButton(R.string.cancelButton, null)
				.show();
	}

	private void resetAllCounters() {
		if (repositoriesApi != null) {
			repositoriesApi.resetAllRepositoryMostVisited(currentActiveAccountId);

			mostVisitedReposList.clear();
			adapter.updateList(new ArrayList<>());
			binding.recyclerView.setVisibility(View.GONE);
			binding.layoutEmpty.getRoot().setVisibility(View.VISIBLE);

			Toasty.show(ctx, getString(R.string.resetMostReposCounter));
		}
	}

	private void filter(String text) {
		List<Repository> filteredList = new ArrayList<>();
		String query = text.toLowerCase().trim();

		for (Repository repo : mostVisitedReposList) {
			if (repo.getRepositoryOwner().toLowerCase().contains(query)
					|| repo.getRepositoryName().toLowerCase().contains(query)) {
				filteredList.add(repo);
			}
		}
		adapter.updateList(filteredList);
	}
}

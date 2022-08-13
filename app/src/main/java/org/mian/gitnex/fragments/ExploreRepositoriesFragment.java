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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.gitnex.tea4j.v2.models.Repository;
import org.gitnex.tea4j.v2.models.SearchResults;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.adapters.ExploreRepositoriesAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.CustomExploreRepositoriesDialogBinding;
import org.mian.gitnex.databinding.FragmentExploreRepoBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.SnackBar;
import org.mian.gitnex.helpers.Toasty;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */

public class ExploreRepositoriesFragment extends Fragment {

	private FragmentExploreRepoBinding viewBinding;
	private Context context;

	private int pageSize;
	private final boolean repoTypeInclude = true;
	private final String sort = "updated";
	private final String order = "desc";
	private int resultLimit;
	private List<Repository> dataList;
	private ExploreRepositoriesAdapter adapter;

	private CustomExploreRepositoriesDialogBinding filterBinding;

	private boolean includeTopic = false;
	private boolean includeDescription = false;
	private boolean includeTemplate = false;
	private boolean onlyArchived = false;
	private String searchQuery = "";

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		viewBinding = FragmentExploreRepoBinding.inflate(inflater, container, false);
		setHasOptionsMenu(true);

		context = getContext();
		dataList = new ArrayList<>();
		adapter = new ExploreRepositoriesAdapter(dataList, context);

		resultLimit = Constants.getCurrentResultLimit(context);

		viewBinding.pullToRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
			viewBinding.pullToRefresh.setRefreshing(false);
			loadInitial("", resultLimit);
			adapter.notifyDataChanged();
		}, 200));

		adapter.setLoadMoreListener(() -> viewBinding.recyclerViewReposSearch.post(() -> {
			if(dataList.size() == resultLimit || pageSize == resultLimit) {
				int page = (dataList.size() + resultLimit) / resultLimit;
				loadMore("", resultLimit, page);
			}
		}));

		viewBinding.recyclerViewReposSearch.setHasFixedSize(true);
		viewBinding.recyclerViewReposSearch.setLayoutManager(new LinearLayoutManager(context));
		viewBinding.recyclerViewReposSearch.setAdapter(adapter);

		loadInitial("", resultLimit);

		return viewBinding.getRoot();
	}

	private void loadInitial(String searchKeyword, int resultLimit) {

		Call<SearchResults> call = RetrofitClient
			.getApiInterface(context).repoSearch(searchKeyword, includeTopic, includeDescription, null, null, null, null,
				repoTypeInclude, null, includeTemplate, onlyArchived, null, null, sort, order, 1, resultLimit);
		call.enqueue(new Callback<>() {
			@Override
			public void onResponse(@NonNull Call<SearchResults> call, @NonNull Response<SearchResults> response) {
				if(response.isSuccessful()) {
					if(response.body() != null && response.body().getData().size() > 0) {
						dataList.clear();
						dataList.addAll(response.body().getData());
						adapter.notifyDataChanged();
						viewBinding.noData.setVisibility(View.GONE);
					}
					else {
						dataList.clear();
						adapter.notifyDataChanged();
						viewBinding.noData.setVisibility(View.VISIBLE);
					}
					viewBinding.progressBar.setVisibility(View.GONE);
				}
				else if(response.code() == 404) {
					viewBinding.noData.setVisibility(View.VISIBLE);
					viewBinding.progressBar.setVisibility(View.GONE);
				}
				else {
					Toasty.error(requireActivity(), requireActivity().getResources().getString(R.string.genericError));
				}
			}

			@Override
			public void onFailure(@NonNull Call<SearchResults> call, @NonNull Throwable t) {

				Toasty.error(requireActivity(), requireActivity().getResources().getString(R.string.genericServerResponseError));
			}
		});
	}

	private void loadMore(String searchKeyword, int resultLimit, int page) {

		viewBinding.progressBar.setVisibility(View.VISIBLE);
		Call<SearchResults> call = RetrofitClient.getApiInterface(context)
			.repoSearch(searchKeyword, includeTopic, includeDescription, null, null, null, null,
				repoTypeInclude, null, includeTemplate, onlyArchived, null, null, sort, order, page, resultLimit);

		call.enqueue(new Callback<>() {
			@Override
			public void onResponse(@NonNull Call<SearchResults> call, @NonNull Response<SearchResults> response) {
				if(response.isSuccessful()) {
					assert response.body() != null;
					List<Repository> result = response.body().getData();
					if(result.size() > 0) {
						pageSize = result.size();
						dataList.addAll(result);
					}
					else {
						SnackBar.info(context, viewBinding.getRoot(), getString(R.string.noMoreData));
						adapter.setMoreDataAvailable(false);
					}
					adapter.notifyDataChanged();
					viewBinding.progressBar.setVisibility(View.GONE);
				}
				else {
					Toasty.error(requireActivity(), requireActivity().getResources().getString(R.string.genericError));
				}
			}

			@Override
			public void onFailure(@NonNull Call<SearchResults> call, @NonNull Throwable t) {

				Toasty.error(requireActivity(), requireActivity().getResources().getString(R.string.genericServerResponseError));
			}
		});
	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

		menu.clear();
		inflater.inflate(R.menu.search_menu, menu);
		inflater.inflate(R.menu.filter_menu_explore, menu);
		super.onCreateOptionsMenu(menu, inflater);
		MenuItem filter = menu.findItem(R.id.filter_explore);

		filter.setOnMenuItemClickListener(filter_ -> {

			showFilterOptions();
			return false;
		});

		MenuItem searchItem = menu.findItem(R.id.action_search);
		androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
		searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

		searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {
				viewBinding.progressBar.setVisibility(View.VISIBLE);
				loadInitial(query, resultLimit);
				adapter.setLoadMoreListener(() -> viewBinding.recyclerViewReposSearch.post(() -> {
					if(dataList.size() == resultLimit || pageSize == resultLimit) {
						int page = (dataList.size() + resultLimit) / resultLimit;
						loadMore(query, resultLimit, page);
					}
				}));
				searchQuery = query;
				searchView.setQuery(null, false);
				searchItem.collapseActionView();
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				return false;
			}
		});
	}

	private void showFilterOptions() {

		MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_Material3_Dialog_Alert);
		filterBinding = CustomExploreRepositoriesDialogBinding.inflate(LayoutInflater.from(context));

		View view = filterBinding.getRoot();
		materialAlertDialogBuilder.setView(view);

		filterBinding.includeTopic.setOnClickListener(includeTopic -> this.includeTopic = filterBinding.includeTopic.isChecked());

		filterBinding.includeDesc.setOnClickListener(includeDesc -> this.includeDescription = filterBinding.includeDesc.isChecked());

		filterBinding.includeTemplate.setOnClickListener(includeTemplate -> this.includeTemplate = filterBinding.includeTemplate.isChecked());

		filterBinding.onlyArchived.setOnClickListener(onlyArchived -> this.onlyArchived = filterBinding.onlyArchived.isChecked());

		filterBinding.includeTopic.setChecked(includeTopic);
		filterBinding.includeDesc.setChecked(includeDescription);
		filterBinding.includeTemplate.setChecked(includeTemplate);
		filterBinding.onlyArchived.setChecked(onlyArchived);

		materialAlertDialogBuilder.setNeutralButton(getString(R.string.close), null);
		materialAlertDialogBuilder.show();
	}

	@Override
	public void onResume() {
		super.onResume();

		if(MainActivity.reloadRepos) {
			dataList.clear();
			loadInitial(searchQuery, resultLimit);
			MainActivity.reloadRepos = false;
		}
	}
}

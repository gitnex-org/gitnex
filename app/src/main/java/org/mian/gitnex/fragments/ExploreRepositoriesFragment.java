package org.mian.gitnex.fragments;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.gitnex.tea4j.models.ExploreRepositories;
import org.gitnex.tea4j.models.UserRepositories;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
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
	private final String TAG = Constants.exploreRepositories;
	private int resultLimit;
	private List<UserRepositories> dataList;
	private ExploreRepositoriesAdapter adapter;

	private Dialog dialogFilterOptions;
	private CustomExploreRepositoriesDialogBinding filterBinding;

	private boolean includeTopic = false;
	private boolean includeDescription = false;
	private boolean includeTemplate = false;
	private boolean onlyArchived = false;

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

		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
		viewBinding.recyclerViewReposSearch.setHasFixedSize(true);
		viewBinding.recyclerViewReposSearch.addItemDecoration(dividerItemDecoration);
		viewBinding.recyclerViewReposSearch.setLayoutManager(new LinearLayoutManager(context));
		viewBinding.recyclerViewReposSearch.setAdapter(adapter);

		loadInitial("", resultLimit);

		return viewBinding.getRoot();
	}

	private void loadInitial(String searchKeyword, int resultLimit) {

		Call<ExploreRepositories> call = RetrofitClient
			.getApiInterface(context).queryRepos(((BaseActivity) requireActivity()).getAccount().getAuthorization(), searchKeyword, repoTypeInclude, sort, order, includeTopic, includeDescription, includeTemplate, onlyArchived, resultLimit, 1);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<ExploreRepositories> call, @NonNull Response<ExploreRepositories> response) {

				if(response.isSuccessful()) {
					if(response.body() != null && response.body().getSearchedData().size() > 0) {
						dataList.clear();
						dataList.addAll(response.body().getSearchedData());
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
			public void onFailure(@NonNull Call<ExploreRepositories> call, @NonNull Throwable t) {

				Toasty.error(requireActivity(), requireActivity().getResources().getString(R.string.genericServerResponseError));
			}
		});
	}

	private void loadMore(String searchKeyword, int resultLimit, int page) {

		viewBinding.progressBar.setVisibility(View.VISIBLE);
		Call<ExploreRepositories> call = RetrofitClient.getApiInterface(context)
			.queryRepos(((BaseActivity) requireActivity()).getAccount().getAuthorization(), searchKeyword, repoTypeInclude, sort, order, includeTopic, includeDescription, includeTemplate, onlyArchived, resultLimit, page);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<ExploreRepositories> call, @NonNull Response<ExploreRepositories> response) {

				if(response.isSuccessful()) {
					assert response.body() != null;
					List<UserRepositories> result = response.body().getSearchedData();
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
			public void onFailure(@NonNull Call<ExploreRepositories> call, @NonNull Throwable t) {

				Toasty.error(requireActivity(), requireActivity().getResources().getString(R.string.genericServerResponseError));
			}
		});
	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

		menu.clear();
		inflater.inflate(R.menu.search_menu, menu);
		inflater.inflate(R.menu.filter_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
		MenuItem filter = menu.findItem(R.id.filter);

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

		dialogFilterOptions = new Dialog(context, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert);

		if (dialogFilterOptions.getWindow() != null) {
			dialogFilterOptions.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		}

		filterBinding = CustomExploreRepositoriesDialogBinding.inflate(LayoutInflater.from(context));

		View view = filterBinding.getRoot();
		dialogFilterOptions.setContentView(view);

		filterBinding.includeTopic.setOnClickListener(includeTopic -> this.includeTopic = filterBinding.includeTopic.isChecked());

		filterBinding.includeDesc.setOnClickListener(includeDesc -> this.includeDescription = filterBinding.includeDesc.isChecked());

		filterBinding.includeTemplate.setOnClickListener(includeTemplate -> this.includeTemplate = filterBinding.includeTemplate.isChecked());

		filterBinding.onlyArchived.setOnClickListener(onlyArchived -> this.onlyArchived = filterBinding.onlyArchived.isChecked());

		filterBinding.includeTopic.setChecked(includeTopic);
		filterBinding.includeDesc.setChecked(includeDescription);
		filterBinding.includeTemplate.setChecked(includeTemplate);
		filterBinding.onlyArchived.setChecked(onlyArchived);

		filterBinding.cancel.setOnClickListener(editProperties -> dialogFilterOptions.dismiss());

		dialogFilterOptions.show();
	}
}

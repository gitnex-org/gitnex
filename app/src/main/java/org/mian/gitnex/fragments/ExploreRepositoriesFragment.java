package org.mian.gitnex.fragments;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.gitnex.tea4j.models.ExploreRepositories;
import org.gitnex.tea4j.models.UserRepositories;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.ExploreRepositoriesAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.CustomExploreRepositoriesDialogBinding;
import org.mian.gitnex.databinding.FragmentExploreRepoBinding;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.SnackBar;
import org.mian.gitnex.helpers.TinyDB;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Template Author M M Arif
 * Author 6543
 * Modified M M Arif
 */

public class ExploreRepositoriesFragment extends Fragment {

	private FragmentExploreRepoBinding viewBinding;
	private Context context;
	private TinyDB tinyDb;

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

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		viewBinding = FragmentExploreRepoBinding.inflate(inflater, container, false);
		setHasOptionsMenu(true);

		context = getContext();
		tinyDb = TinyDB.getInstance(getContext());

		dataList = new ArrayList<>();
		adapter = new ExploreRepositoriesAdapter(dataList, context);

		tinyDb.putBoolean("exploreRepoIncludeTopic", false);
		tinyDb.putBoolean("exploreRepoIncludeDescription", false);
		tinyDb.putBoolean("exploreRepoIncludeTemplate", false);
		tinyDb.putBoolean("exploreRepoOnlyArchived", false);

		resultLimit = Constants.getCurrentResultLimit(context);

		viewBinding.searchKeyword.setOnEditorActionListener((v1, actionId, event) -> {
			if(actionId == EditorInfo.IME_ACTION_SEND) {
				if(!Objects.requireNonNull(viewBinding.searchKeyword.getText()).toString().equals("")) {
					InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(viewBinding.searchKeyword.getWindowToken(), 0);

					viewBinding.progressBar.setVisibility(View.VISIBLE);
					loadInitial(String.valueOf(viewBinding.searchKeyword.getText()), tinyDb.getBoolean("exploreRepoIncludeTopic"), tinyDb.getBoolean("exploreRepoIncludeDescription"), tinyDb.getBoolean("exploreRepoIncludeTemplate"), tinyDb.getBoolean("exploreRepoOnlyArchived"), resultLimit);

					adapter.setLoadMoreListener(() -> viewBinding.recyclerViewReposSearch.post(() -> {
						if(dataList.size() == resultLimit || pageSize == resultLimit) {
							int page = (dataList.size() + resultLimit) / resultLimit;
							loadMore(String.valueOf(viewBinding.searchKeyword.getText()), tinyDb.getBoolean("exploreRepoIncludeTopic"), tinyDb.getBoolean("exploreRepoIncludeDescription"), tinyDb.getBoolean("exploreRepoIncludeTemplate"), tinyDb.getBoolean("exploreRepoOnlyArchived"), resultLimit, page);
						}
					}));
				}
			}
			return false;
		});

		viewBinding.pullToRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
			viewBinding.pullToRefresh.setRefreshing(false);
			loadInitial("", tinyDb.getBoolean("exploreRepoIncludeTopic"), tinyDb.getBoolean("exploreRepoIncludeDescription"), tinyDb.getBoolean("exploreRepoIncludeTemplate"), tinyDb.getBoolean("exploreRepoOnlyArchived"), resultLimit);
			adapter.notifyDataChanged();
		}, 200));

		adapter.setLoadMoreListener(() -> viewBinding.recyclerViewReposSearch.post(() -> {
			if(dataList.size() == resultLimit || pageSize == resultLimit) {
				int page = (dataList.size() + resultLimit) / resultLimit;
				loadMore(String.valueOf(viewBinding.searchKeyword.getText()), tinyDb.getBoolean("exploreRepoIncludeTopic"), tinyDb.getBoolean("exploreRepoIncludeDescription"), tinyDb.getBoolean("exploreRepoIncludeTemplate"), tinyDb.getBoolean("exploreRepoOnlyArchived"), resultLimit, page);
			}
		}));

		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
		viewBinding.recyclerViewReposSearch.setHasFixedSize(true);
		viewBinding.recyclerViewReposSearch.addItemDecoration(dividerItemDecoration);
		viewBinding.recyclerViewReposSearch.setLayoutManager(new LinearLayoutManager(context));
		viewBinding.recyclerViewReposSearch.setAdapter(adapter);

		loadInitial("", tinyDb.getBoolean("exploreRepoIncludeTopic"), tinyDb.getBoolean("exploreRepoIncludeDescription"), tinyDb.getBoolean("exploreRepoIncludeTemplate"), tinyDb.getBoolean("exploreRepoOnlyArchived"), resultLimit);

		return viewBinding.getRoot();
	}

	private void loadInitial(String searchKeyword, boolean exploreRepoIncludeTopic, boolean exploreRepoIncludeDescription, boolean exploreRepoIncludeTemplate, boolean exploreRepoOnlyArchived, int resultLimit) {

		Call<ExploreRepositories> call = RetrofitClient
			.getApiInterface(context).queryRepos(Authorization.get(getContext()), searchKeyword, repoTypeInclude, sort, order, exploreRepoIncludeTopic, exploreRepoIncludeDescription, exploreRepoIncludeTemplate, exploreRepoOnlyArchived, resultLimit, 1);
		call.enqueue(new Callback<ExploreRepositories>() {
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
					Log.e(TAG, String.valueOf(response.code()));
				}
			}

			@Override
			public void onFailure(@NonNull Call<ExploreRepositories> call, @NonNull Throwable t) {
				Log.e(TAG, t.toString());
			}
		});
	}

	private void loadMore(String searchKeyword, boolean exploreRepoIncludeTopic, boolean exploreRepoIncludeDescription, boolean exploreRepoIncludeTemplate, boolean exploreRepoOnlyArchived, int resultLimit, int page) {

		viewBinding.loadingMoreView.setVisibility(View.VISIBLE);
		Call<ExploreRepositories> call = RetrofitClient.getApiInterface(context)
			.queryRepos(Authorization.get(getContext()), searchKeyword, repoTypeInclude, sort, order, exploreRepoIncludeTopic, exploreRepoIncludeDescription, exploreRepoIncludeTemplate, exploreRepoOnlyArchived, resultLimit, page);
		call.enqueue(new Callback<ExploreRepositories>() {
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
					viewBinding.loadingMoreView.setVisibility(View.GONE);
				}
				else {
					Log.e(TAG, String.valueOf(response.code()));
				}
			}

			@Override
			public void onFailure(@NonNull Call<ExploreRepositories> call, @NonNull Throwable t) {
				Log.e(TAG, t.toString());
			}
		});
	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

		menu.clear();
		inflater.inflate(R.menu.filter_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
		MenuItem filter = menu.findItem(R.id.filter);

		filter.setOnMenuItemClickListener(filter_ -> {

			showFilterOptions();
			return false;
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

		filterBinding.includeTopic.setOnClickListener(includeTopic -> {
			tinyDb.putBoolean("exploreRepoIncludeTopic", filterBinding.includeTopic.isChecked());
		});

		filterBinding.includeDesc.setOnClickListener(includeDesc -> {
			tinyDb.putBoolean("exploreRepoIncludeDescription", filterBinding.includeDesc.isChecked());
		});

		filterBinding.includeTemplate.setOnClickListener(includeTemplate -> {
			tinyDb.putBoolean("exploreRepoIncludeTemplate", filterBinding.includeTemplate.isChecked());
		});

		filterBinding.onlyArchived.setOnClickListener(onlyArchived -> {
			tinyDb.putBoolean("exploreRepoOnlyArchived", filterBinding.onlyArchived.isChecked());
		});

		filterBinding.includeTopic.setChecked(tinyDb.getBoolean("exploreRepoIncludeTopic"));
		filterBinding.includeDesc.setChecked(tinyDb.getBoolean("exploreRepoIncludeDescription"));
		filterBinding.includeTemplate.setChecked(tinyDb.getBoolean("exploreRepoIncludeTemplate"));
		filterBinding.onlyArchived.setChecked(tinyDb.getBoolean("exploreRepoOnlyArchived"));

		filterBinding.cancel.setOnClickListener(editProperties -> {
			dialogFilterOptions.dismiss();
		});

		dialogFilterOptions.show();
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	public interface OnFragmentInteractionListener {
		void onFragmentInteraction(Uri uri);
	}
}

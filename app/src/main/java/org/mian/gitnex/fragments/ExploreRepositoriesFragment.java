package org.mian.gitnex.fragments;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
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
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.InfiniteScrollListener;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Version;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Template Author M M Arif
 * Author 6543
 */

public class ExploreRepositoriesFragment extends Fragment {

	private FragmentExploreRepoBinding viewBinding;
	private Context ctx;
	private TinyDB tinyDb;

	private int pageCurrentIndex = 1;
	private boolean repoTypeInclude = true;
	private String sort = "updated";
	private String order = "desc";
	private int limit = 10;
	private List<UserRepositories> dataList;
	private ExploreRepositoriesAdapter adapter;

	private Dialog dialogFilterOptions;
	private CustomExploreRepositoriesDialogBinding filterBinding;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		viewBinding = FragmentExploreRepoBinding.inflate(inflater, container, false);
		setHasOptionsMenu(true);

		ctx = getContext();
		tinyDb = TinyDB.getInstance(getContext());

		dataList = new ArrayList<>();
		adapter = new ExploreRepositoriesAdapter(dataList, ctx);

		tinyDb.putBoolean("exploreRepoIncludeTopic", false);
		tinyDb.putBoolean("exploreRepoIncludeDescription", false);
		tinyDb.putBoolean("exploreRepoIncludeTemplate", false);
		tinyDb.putBoolean("exploreRepoOnlyArchived", false);

		// if gitea is 1.12 or higher use the new limit
		if(new Version(tinyDb.getString("giteaVersion")).higherOrEqual("1.12.0")) {
			limit = Constants.resultLimitNewGiteaInstances;
		}

		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ctx);

		viewBinding.recyclerViewReposSearch.setHasFixedSize(true);
		viewBinding.recyclerViewReposSearch.setLayoutManager(linearLayoutManager);
		viewBinding.recyclerViewReposSearch.setAdapter(adapter);

		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(viewBinding.recyclerViewReposSearch.getContext(),
			DividerItemDecoration.VERTICAL);
		viewBinding.recyclerViewReposSearch.addItemDecoration(dividerItemDecoration);

		viewBinding.searchKeyword.setOnEditorActionListener((v1, actionId, event) -> {

			if(actionId == EditorInfo.IME_ACTION_SEND) {

				if(!Objects.requireNonNull(viewBinding.searchKeyword.getText()).toString().equals("")) {

					InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(viewBinding.searchKeyword.getWindowToken(), 0);

					// if gitea is 1.12 or higher use the new limit
					if(new Version(tinyDb.getString("giteaVersion")).higherOrEqual("1.12.0")) {
						limit = Constants.resultLimitNewGiteaInstances;
					}
					else {
						limit = 10;
					}

					pageCurrentIndex = 1;
					viewBinding.progressBar.setVisibility(View.VISIBLE);
					loadData(false, viewBinding.searchKeyword.getText().toString(), tinyDb.getBoolean("exploreRepoIncludeTopic"), tinyDb.getBoolean("exploreRepoIncludeDescription"), tinyDb.getBoolean("exploreRepoIncludeTemplate"), tinyDb.getBoolean("exploreRepoOnlyArchived"));
				}
			}
			return false;
		});

		viewBinding.recyclerViewReposSearch.addOnScrollListener(new InfiniteScrollListener(pageCurrentIndex, linearLayoutManager) {

			@Override
			public void onScrolledToEnd(int firstVisibleItemPosition) {

				pageCurrentIndex++;
				loadData(true, Objects.requireNonNull(viewBinding.searchKeyword.getText()).toString(), tinyDb.getBoolean("exploreRepoIncludeTopic"), tinyDb.getBoolean("exploreRepoIncludeDescription"), tinyDb.getBoolean("exploreRepoIncludeTemplate"), tinyDb.getBoolean("exploreRepoOnlyArchived"));
			}
		});

		viewBinding.pullToRefresh.setOnRefreshListener(() -> {

			pageCurrentIndex = 1;

			// if gitea is 1.12 or higher use the new limit
			if(new Version(tinyDb.getString("giteaVersion")).higherOrEqual("1.12.0")) {
				limit = Constants.resultLimitNewGiteaInstances;
			}
			else {
				limit = 10;
			}

			loadData(false, Objects.requireNonNull(viewBinding.searchKeyword.getText()).toString(), tinyDb.getBoolean("exploreRepoIncludeTopic"), tinyDb.getBoolean("exploreRepoIncludeDescription"), tinyDb.getBoolean("exploreRepoIncludeTemplate"), tinyDb.getBoolean("exploreRepoOnlyArchived"));
		});

		loadData(false, "", tinyDb.getBoolean("exploreRepoIncludeTopic"), tinyDb.getBoolean("exploreRepoIncludeDescription"), tinyDb.getBoolean("exploreRepoIncludeTemplate"), tinyDb.getBoolean("exploreRepoOnlyArchived"));

		return viewBinding.getRoot();

	}

	private void loadData(boolean append, String searchKeyword, boolean exploreRepoIncludeTopic, boolean exploreRepoIncludeDescription, boolean exploreRepoIncludeTemplate, boolean exploreRepoOnlyArchived) {

		viewBinding.noData.setVisibility(View.GONE);

		int apiCallDefaultLimit = 10;
		// if gitea is 1.12 or higher use the new limit
		if(new Version(tinyDb.getString("giteaVersion")).higherOrEqual("1.12.0")) {
			apiCallDefaultLimit = Constants.resultLimitNewGiteaInstances;
		}

		if(apiCallDefaultLimit > limit) {
			return;
		}

		if(pageCurrentIndex == 1 || !append) {

			dataList.clear();
			adapter.notifyDataSetChanged();
			viewBinding.pullToRefresh.setRefreshing(false);
			viewBinding.progressBar.setVisibility(View.VISIBLE);
		}
		else {

			viewBinding.loadingMoreView.setVisibility(View.VISIBLE);
		}

		Call<ExploreRepositories> call = RetrofitClient.getApiInterface(ctx).queryRepos(Authorization.get(getContext()), searchKeyword, repoTypeInclude, sort, order, exploreRepoIncludeTopic, exploreRepoIncludeDescription, exploreRepoIncludeTemplate, exploreRepoOnlyArchived, limit, pageCurrentIndex);

		call.enqueue(new Callback<ExploreRepositories>() {

			@Override
			public void onResponse(@NonNull Call<ExploreRepositories> call, @NonNull Response<ExploreRepositories> response) {

				if(response.code() == 200) {

					assert response.body() != null;

					limit = response.body().getSearchedData().size();

					if(!append) {

						dataList.clear();
					}

					dataList.addAll(response.body().getSearchedData());
					adapter.notifyDataSetChanged();

				}
				else {

					dataList.clear();
					adapter.notifyDataChanged();
					viewBinding.noData.setVisibility(View.VISIBLE);

				}

				onCleanup();

			}

			@Override
			public void onFailure(@NonNull Call<ExploreRepositories> call, @NonNull Throwable t) {

				Log.e("onFailure", Objects.requireNonNull(t.getMessage()));
				onCleanup();

			}

			private void onCleanup() {

				AppUtil.setMultiVisibility(View.GONE, viewBinding.loadingMoreView, viewBinding.progressBar);

				if(dataList.isEmpty()) {

					viewBinding.noData.setVisibility(View.VISIBLE);
				}
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

		dialogFilterOptions = new Dialog(ctx, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert);

		if (dialogFilterOptions.getWindow() != null) {

			dialogFilterOptions.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		}

		filterBinding = CustomExploreRepositoriesDialogBinding.inflate(LayoutInflater.from(ctx));

		View view = filterBinding.getRoot();
		dialogFilterOptions.setContentView(view);

		filterBinding.includeTopic.setOnClickListener(includeTopic -> {

			if(filterBinding.includeTopic.isChecked()) {

				tinyDb.putBoolean("exploreRepoIncludeTopic", true);
			}
			else {

				tinyDb.putBoolean("exploreRepoIncludeTopic", false);
			}
		});

		filterBinding.includeDesc.setOnClickListener(includeDesc -> {

			if(filterBinding.includeDesc.isChecked()) {

				tinyDb.putBoolean("exploreRepoIncludeDescription", true);
			}
			else {

				tinyDb.putBoolean("exploreRepoIncludeDescription", false);
			}
		});

		filterBinding.includeTemplate.setOnClickListener(includeTemplate -> {

			if(filterBinding.includeTemplate.isChecked()) {

				tinyDb.putBoolean("exploreRepoIncludeTemplate", true);
			}
			else {

				tinyDb.putBoolean("exploreRepoIncludeTemplate", false);
			}
		});

		filterBinding.onlyArchived.setOnClickListener(onlyArchived -> {

			if(filterBinding.onlyArchived.isChecked()) {

				tinyDb.putBoolean("exploreRepoOnlyArchived", true);
			}
			else {

				tinyDb.putBoolean("exploreRepoOnlyArchived", false);
			}
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
